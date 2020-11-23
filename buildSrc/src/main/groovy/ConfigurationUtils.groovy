import dev.nokee.language.c.tasks.CCompile
import dev.nokee.language.cpp.tasks.CppCompile
import dev.nokee.language.nativebase.tasks.NativeSourceCompile
import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeApplication
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.platform.nativebase.NativeLibrary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.StaticLibraryBinary
import dev.nokee.platform.nativebase.TargetBuildTypeFactory
import dev.nokee.platform.nativebase.tasks.LinkExecutable
import dev.nokee.utils.ActionUtils
import groovy.transform.Canonical
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.Transformer
import org.gradle.api.specs.Spec
import org.gradle.nativeplatform.toolchain.NativeToolChain
import org.gradle.nativeplatform.toolchain.VisualCpp
import org.gradle.language.rc.tasks.WindowsResourceCompile
import org.gradle.api.model.ObjectFactory

final class ConfigurationUtils {
    private ConfigurationUtils() {}

    static Closure withDefaultApplicationConfiguration(Action<NativeConfiguration> action = ActionUtils.doNothing()) {
        return { application ->
            application.with {
                targetMachines = [machines.windows.x86]
                targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]

                variants.configureEach(NativeApplication) { variant ->
                    binaries.configureEach(ExecutableBinary) { binary ->
                        binary.compileTasks.configureEach(CppCompile, withDefaultCompileFlags(application.buildTypes, variant, action))
                        binary.compileTasks.configureEach(CCompile, withDefaultCompileFlags(application.buildTypes, variant, action))
                        binary.linkTask.configure(withDefaultLinkFlags(application.buildTypes, variant))
                    }
                }
                binaries.configureEach(ExecutableBinary) { binary ->
                    binary.linkTask.configure(defaultWindowsLibraries())
                }
            }
        }
    }

    static Closure withDefaultLibraryConfiguration(Action<NativeConfiguration> action = ActionUtils.doNothing()) {
        return { library ->
            library.with {
                targetMachines = [machines.windows.x86]
                targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]

                variants.configureEach(NativeLibrary) { variant ->
                    binaries.withType(NativeBinary).configureEach(nativeLibraries()) { binary ->
                        binary.compileTasks.configureEach(CppCompile, withDefaultCompileFlags(library.buildTypes, variant, action))
                        binary.compileTasks.configureEach(CCompile, withDefaultCompileFlags(library.buildTypes, variant, action))
                    }
                    binaries.configureEach(SharedLibraryBinary) { binary ->
                        binary.linkTask.configure(withDefaultLinkFlags(library.buildTypes, variant))
                    }
                }
            }
        }
    }

    @CompileStatic
    private static Spec<Binary> nativeLibraries() {
        return { binary -> binary instanceof SharedLibraryBinary || binary instanceof StaticLibraryBinary } as Spec<Binary>
    }

    @Canonical
    static class NativeConfiguration {
        enum ConsoleDef { SUPPRESS, DEFINED }
        ConsoleDef consoleDef = ConsoleDef.DEFINED
    }

    @CompileStatic
    private static Action<NativeSourceCompile> withDefaultCompileFlags(TargetBuildTypeFactory buildTypeFactory, Variant variant, Action<NativeConfiguration> action) {
        return { NativeSourceCompile task ->
            if (variant.buildVariant.hasAxisOf(buildTypeFactory.named('release'))) {
                task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp('/DNDEBUG')))
                task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp('/MT')))
            } else if (variant.buildVariant.hasAxisOf(buildTypeFactory.named('debug'))) {
                task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp('/D_DEBUG')))
                task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp('/MTd')))
                task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp('/DEBUG', '/ZI', '/FS')))
            }

            def configuration = new NativeConfiguration()
            action.execute(configuration)

            task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp([
                    '/D_CRT_SECURE_NO_WARNINGS', '/wd4996',
                    '/nologo',

                    '/EHsc',
                    '/DWIN32',
                    '/D_WIN32_WINNT=0x0601',

                    '/W3', '/WX',

                    // We'd like to be able to do a build time analysis, intellisense will work,
                    // but you have to open every source file in the IDE to get that to scan it all, which is impractical.
                    // Since Gradle only sends compilation errors to stdout, not warnings, we either need to someone get the entire
                    // compiler output to stdout, or cause all warnings to be errors.

                    // Comment this line back in to get all analysis warnings in the VS error list panel, but turning this on for an
                    // existing codebase will cause a lot of issues that need to be fixed (unless you turn it on
                    // only on demand) and may prevent other projects from being built (based on dependencies), so leave it off normally.
                    // Ideally turn this on by some command line switch, VS doesn't seem to let us control the analyze command as it does
                    // for debug or release.
                    //'/analyze',

                    // TODO: There is also this solution at https://stackoverflow.com/questions/44126969/gradle-compiler-output-when-building-c-c-code/44691786#44691786.
                    // It sends the compiler output from the build\tmp directory to gradle stdout.


                    // Tell some of the modules in SourceT to use a relative include path for siglib.
                    '/DUSE_REL_SIGLIB_PATH',
            ])))

            if (configuration.consoleDef == NativeConfiguration.ConsoleDef.DEFINED) {
                task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp('/D_CONSOLE')))
            }
        } as Action<NativeSourceCompile>
    }

    private static Action<? super Task> withDefaultLinkFlags(TargetBuildTypeFactory buildTypeFactory, Variant variant) {
        return { task ->
            if (variant.buildVariant.hasAxisOf(buildTypeFactory.named('debug'))) {
                task.linkerArgs.addAll(task.toolChain.map(whenVisualCpp('/debug')))
            }
        } as Action<NativeSourceCompile>
    }

    /**
     * Configures the default Windows libraries as linker arguments.
     *
     * @return a configuration action for {@link LinkExecutable} task, never null.
     */
    @CompileStatic
    private static Action<LinkExecutable> defaultWindowsLibraries() {
        return { LinkExecutable task ->
            task.linkerArgs.addAll(task.toolChain.map(whenVisualCpp([
                    "iphlpapi.lib",
                    "kernel32.lib",
                    "user32.lib",
                    "gdi32.lib",
                    "winspool.lib",
                    "comdlg32.lib",
                    "advapi32.lib",
                    "shell32.lib",
                    "ole32.lib",
                    "oleaut32.lib",
                    "odbc32.lib",
                    "odbccp32.lib",
                    "ws2_32.lib",
                    "wsock32.lib"
            ])))
        } as Action<LinkExecutable>
    }

    /**
     * Returns the specified list if the mapping toolchain is Visual C++, else returns an empty list.
     *
     * @param value the values to return if the mapping toolchain is Visual C++
     * @return a transformer to use in provider mapping, never null.
     */
    @CompileStatic
    private static <T> Transformer<List<T>, NativeToolChain> whenVisualCpp(List<T> values) {
        return {
            if (it in VisualCpp) {
                return values
            }
            return []
        } as Transformer<List<T>, NativeToolChain>
    }

    /**
     * Returns the specified list if the mapping toolchain is Visual C++, else returns an empty list.
     *
     * @param value the values to return if the mapping toolchain is Visual C++
     * @return a transformer to use in provider mapping, never null.
     */
    @CompileStatic
    private static <T> Transformer<List<T>, NativeToolChain> whenVisualCpp(T... values) {
        return {
            if (it in VisualCpp) {
                return values as List<T>
            }
            return []
        } as Transformer<List<T>, NativeToolChain>
    }

    /**
     * Adds VS compiler options for all languages.
     *
     * @param newOptions  the new options to add
     * @return a configuration closure to execute in the application or library extension
     */
    static Closure addVSCompilerOption(String... newOptions) {
        return { component ->
            component.binaries.configureEach {
                compileTasks.configureEach { // Note: configures all compiler
                    compilerArgs.addAll(toolChain.map(whenVisualCpp(newOptions)))
                }
            }
        }
    }

    /**
     * Adds VS linker options.
     *
     * @param newOptions  the new options to add
     * @return a configuration closure to execute in the application or library extension
     */
    static Closure addVSLinkerOption(String... newOptions) {
        return { component ->
            component.binaries.configureEach(sharedLibraryOrExecutable()) {
                linkTask.configure {
                    linkerArgs.addAll(toolChain.map(whenVisualCpp(newOptions)))
                }
            }
        }
    }

    @CompileStatic
    private static Spec<Binary> sharedLibraryOrExecutable() {
        return { binary -> binary instanceof SharedLibraryBinary || binary instanceof ExecutableBinary } as Spec<Binary>
    }

    /**
     * Adds Compiler and Linker options to use Boost.
     *
     * @return a configuration closure to execute in the application or library extension
     */
    static Closure buildWithBoost() {
        return { component ->
            component.binaries.configureEach {
                compileTasks.configureEach { // Note: configures all compiler
                    compilerArgs.addAll(toolChain.map(whenVisualCpp([
                        '/DBOOST_NETWORK_ENABLE_HTTPS',

                        "/I" + System.getenv('LIB_BOOST_X86'),
                        // TODO:NOKEE: make this work from the root project ext var.
                        //"/I${getRootProject().property(libBoostIncludePath)}"

                        // daniel:nokee:  21 minutes ago
                        // I'm not 100% sure but I think this would work in Groovy:
                        // static Closure buildWithOpenSSL(String pathToOpenSslIncludes = rootProject.property('libOpenSSLIncludePath')) {
                        //         return { component -> addVSCompileOption("/I${pathToOpenSslIncludes}") }
                        // }
                        // The alternative would be using 2 methods:
                        // static Closure buildWithOpenSSL(String pathToOpenSslIncludes) {
                        //         return { component -> addVSCompileOption("/I${pathToOpenSslIncludes}") }
                        // }
                        // static String openSslIncludePath(Project project) {
                        //     return project.rootProject.property('libOpenSSLIncludePath')
                        // }
                        // Then you can use it like this:
                        // library(buildWithOpenSSL(openSslIncludePath(project)))

                        // The reason getRootProject() doesn't work in the closure is a bit confusing
                        // but basically in the build.gradle file, the Groovy delegate is the Project instance.
                        // This means that is the method isn't define anywhere in the current scope it''s going to go
                        // look at the delegate object to find the method.
                        // In this case the getRootProject is available on the Project instance.
                        // Inside the buildWithOpenSsl method, the delegate isn't set to Project so there is no
                        // way to find and call the method. My assumption is the default parameter value is
                        // evaluated inside the caller context which should find the method but that is just a wild assumption,
                        // I would have to try it to confirm.

                        // Kelly Hickel  2 minutes ago
                        // I can't make the getRootProject work, I'll add a comment and maybe come back to it later, it's a pretty minor "issue"
                        // daniel:nokee:  1 minute ago
                        // If you use the 2 methods example, that should work without issues.

                        // Kelly Hickel: or just pass project as an arg and let it call get property and so on.

                        // daniel:nokee:  2 minutes ago
                        // You could use Java system properties.
                        // You can pass them via -Dkey=value  or you can use gradle.properties using sysProp.key=value

                        // Kelly Hickel  < 1 minute ago
                        // hmm.  that's useful too.
                        // I can just use the env vars, but I already have the gradle project set to 
                        // figure out the include and lib paths and fail with a useful error if they aren't
                        // there (one that is or should be relevant to any of our developers that
                        // should be building this code), so I didn't want to lose that.
                        // probably better to just move to the repository based one you laid out, but that's not something I want to deal with at the moment.

                        // Kelly Hickel  < 1 minute ago
                        // the env vars are what I'm doing now, and that's OK for the moment.
                    ])))
                }
            }
            component.binaries.configureEach(sharedLibraryOrExecutable()) {
                linkTask.configure {
                    linkerArgs.addAll(toolChain.map(whenVisualCpp([
                        //"/LIBPATH:${getRootProject().property(libBoostLibPath)}",
                        // TODO:NOKEE: make this work from the root project ext var.
                        '/LIBPATH:' + System.getenv('LIB_BOOST_X86') + '\\lib32-msvc-14.2'
                    ])))
                }
            }
        }
    }

    /**
     * Adds Compiler and Linker options to use OpenSSL.
     *
     * @return a configuration closure to execute in the application or library extension
     */
    static Closure buildWithOpenSSL() {
        return { component ->
            component.binaries.configureEach {
                compileTasks.configureEach { // Note: configures all compiler
                    compilerArgs.addAll(toolChain.map(whenVisualCpp([
                        // TODO:NOKEE: make this work from the root project ext var.
                        '/I' + System.getenv('LIB_OPENSSL_X86') + '\\include'
                        //"/I${getRootProject().property(libOpenSSLIncludePath)}"
                    ])))
                }
            }
            component.binaries.configureEach(sharedLibraryOrExecutable()) {
                linkTask.configure {
                    linkerArgs.addAll(toolChain.map(whenVisualCpp([
                        // TODO:NOKEE: make this work from the root project ext var.
                        //'/LIBPATH:${getRootProject().property(libOpenSSLLibPath)}',
                        '/LIBPATH:' + System.getenv('LIB_OPENSSL_X86') + '\\lib',

                        'libcrypto.lib',
                        'libssl.lib',
                        'crypt32.lib'
                    ])))
                }
            }
        }
    }

    /**
     * Adds Compiler and Linker options to build with Windows resource files.
     *
     * @return a configuration closure to execute in the application or library extension
     */
    static Closure addWindowsResourceFile(ObjectFactory of, String taskNamePrefix, String dirPath = '.', String filePattern='*.rc') {
        return { component ->
            def idx = 0
            component.binaries.configureEach { bin->
                idx++
                def taskName = taskNamePrefix + "_${idx}"
                def compileResources = tasks.register(taskName, WindowsResourceCompile) {
                    source.from(of.fileTree().setDir(dirPath).include(filePattern))
                    
                    // For some as yet not understood reason, if you just run "rc /v /l 0x409 Version.rc" in a VS command prompt, it works,
                    // but when Gradle runs the command, rc.exe can't find the standard include files, so we have to use the INCLUDE env var.
                    includes.from(System.getenv('INCLUDE'))

                    compilerArgs.addAll("/v", "/l", "0x409")
                }

                if(bin instanceof SharedLibraryBinary || bin instanceof ExecutableBinary) {
                    linkTask.configure {
                        // This next line throws the error:
                        // * What went wrong:
                        // Execution failed for task ':SourceT:Winsend:WinSendResources_1'.
                        // > Cannot query the value of this property because it has no value available.
                        dependsOn compileResources
                        source.from(of.fileTree().setDir(dirPath).include('**/*.res'))
                        linkerArgs.addAll("user32.lib")
                    }
                }
            }
        }
    }
    
    /**
     * Adds Compiler and Linker options to build an AFX application
     *
     * @return a configuration closure to execute in the application or library extension
     */
    static Closure buildWithAFX() {
        return { component ->
            component.binaries.configureEach {
                compileTasks.configureEach { // Note: configures all compiler
                    compilerArgs.addAll(toolChain.map(whenVisualCpp([
                        '/I' + System.getenv('VCToolsInstallDir') + 'atlmfc\\include'
                    ])))
                }
            }
            component.binaries.configureEach(sharedLibraryOrExecutable()) {
                linkTask.configure {
                    linkerArgs.addAll(toolChain.map(whenVisualCpp([
                        '/LIBPATH:' + System.getenv('VCToolsInstallDir') + 'atlmfc\\lib\\x86',
                        '/subsystem:windows'
                    ])))
                }
            }
            // I tried this way, but it didn't work, these options were not part of the build commands.
            // addVSCompilerOption('/I' + System.getenv('VCToolsInstallDir') + 'atlmfc\\include')
            // addVSLinkerOption('/LIBPATH:"' + System.getenv('VCToolsInstallDir') + 'atlmfc\\lib\\x86"')
            // addVSLinkerOption('/subsystem:windows')
        }
    }

}
