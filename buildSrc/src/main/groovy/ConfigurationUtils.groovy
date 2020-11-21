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
                task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp('/DDEBUG', '/D_DEBUG')))
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
     * Add an external OpenSSL library, located by the environment variable LIB_OPENSSL_X86, to a library subproject.
     *
     */
    static Closure withOpenSSLLibraryConfiguration(Action<NativeConfiguration> action = ActionUtils.doNothing()) {
        return { library ->
            library.with {
                targetMachines = [machines.windows.x86]
                targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]

                variants.configureEach(NativeLibrary) { variant ->
                    binaries.withType(NativeBinary).configureEach(nativeLibraries()) { binary ->
                        binary.compileTasks.configureEach(CppCompile, withOpenSSLCompileFlags(library.buildTypes, variant, action))
                        binary.compileTasks.configureEach(CCompile, withOpenSSLCompileFlags(library.buildTypes, variant, action))
                    }
                    binaries.configureEach(SharedLibraryBinary) { binary ->
                        binary.linkTask.configure(withOpenSSLLinkFlags(library.buildTypes, variant))
                    }
                }
            }
        }
    }

    @CompileStatic
    private static Action<NativeSourceCompile> withOpenSSLCompileFlags(TargetBuildTypeFactory buildTypeFactory, Variant variant, Action<NativeConfiguration> action) {
        return { NativeSourceCompile task ->
            task.compilerArgs.addAll(task.toolChain.map(whenVisualCpp([
                "/I" + System.getenv('LIB_OPENSSL_X86') + '\\include'
            ])))
        } as Action<NativeSourceCompile>
        }

    private static Action<? super Task> withOpenSSLLinkFlags(TargetBuildTypeFactory buildTypeFactory, Variant variant) {
        return { task ->
            task.linkerArgs.addAll(task.toolChain.map(whenVisualCpp([
                '/LIBPATH:' + System.getenv('LIB_OPENSSL_X86') + '\\lib',
                'libcrypto.lib',
                'libssl.lib',
                'crypt32.lib'
            ])))
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
                    compilerArgs.addAll(whenVisualCpp(newOptions))
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
            component.binaries.configureEach {
                linkTask.configure {
                    linkerArgs.addAll(whenVisualCpp(newOptions))
                }
            }
        }
    }
}
