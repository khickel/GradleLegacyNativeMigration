import groovy.transform.CompileStatic
import org.apache.commons.io.FilenameUtils
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Provider

import java.util.concurrent.Callable
import java.util.regex.Pattern

/**
 * This locator is just an example on how Nokee intend to solve the discovery of system libraries, toolchains, Conan dependencies, Nuget dependencies, etc.
 * Nokee will provide a configuration hook in settings.gradle allowing users to add new locators and possibly mutation of the discovered components.
 * Upon execution, the locator will find the interesting bit of information and publish modeling elements for them.
 * In this example, the modeling elements are straight Gradle module metadata but that will be abstracted by Nokee.
 *
 * Nokee will generate a cache of all the model element.
 * In this example, we generate a cache per-subproject but it should really be single cache for the entire project.
 */
final class OpenSslLocator implements Callable<String> {
    private static final Logger LOGGER = Logging.getLogger(OpenSslLocator)
    private final String group = 'system.OpenSSL'
    private String versionCache = null
    private final Provider<Directory> cachingDirectory

    OpenSslLocator(Provider<Directory> cachingDirectory) {
        this.cachingDirectory = cachingDirectory
    }

    private File repositoryFile(String path) {
        def result = cachingDirectory.get().file("${group.replace('.', '/')}/${path}").asFile
        result.parentFile.mkdirs()
        result.createNewFile()
        return result
    }

    void buildCacheIfAbsent() {
        if (versionCache != null) {
            return;
        }

        module("libcrypto") {
            [ """{
                "name": "apiShared",
                "attributes": ${cppApiAttributes},
                "files": [${file('include')}]
            }""",
            """{
                "name": "linkShared",
                "attributes": ${nativeLinkAttributes},
                "files": [${file('lib/libcrypto.lib')}]
            }""",
            """{
                "name": "runtimeShared",
                "attributes": ${nativeRuntimeAttributes},
                "files": [${file('bin/libcrypto-1_1.dll')}]
            }"""]
        }

        module("libssl") {
            [ """{
                "name": "apiShared",
                "attributes": ${cppApiAttributes},
                "files": [${file('include')}]
            }""",
              """{
                "name": "linkShared",
                "attributes": ${nativeLinkAttributes},
                "files": [${file('lib/libssl.lib')}]
            }""",
              """{
                "name": "runtimeShared",
                "attributes": ${nativeRuntimeAttributes},
                "files": [${file('bin/libssl-1_1.dll')}]
            }"""]
        }
    }

    private void module(String name, @DelegatesTo(value = ModuleSpec, strategy = Closure.DELEGATE_FIRST) Closure<List<String>> closure) {
        def moduleSpec = new ModuleSpec(name)
        closure.delegate = moduleSpec
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        repositoryFile("${name}/${version}/${name}-${version}.module").text = """{
            |    "formatVersion": "1.0",
            |    "component": {
            |        "group": "${group}",
            |        "module": "${name}",
            |        "version": "${version}"
            |    },
            |    "createdBy": {
            |        "gradle": {
            |            "version": "4.3",
            |            "buildId": "abc123"
            |        }
            |    },
            |    "variants": [
            |       ${closure.call(moduleSpec).join(', ')}
            |    ]
            |}
            |""".stripMargin()
    }

    private File getOpenSslLocation() {
        if (System.getenv().containsKey('LIB_OPENSSL_X86')) {
            return new File(System.getenv('LIB_OPENSSL_X86'))
        }
        return new File('C:\\Program Files (x86)\\OpenSSL-Win32')
    }

    private String getVersion() {
        if (versionCache == null) {
            def changesFile = new File(getOpenSslLocation(), 'changes.txt')
            if (changesFile.exists()) {
                def versionPattern = Pattern.compile('\\d+\\.\\d+\\.\\d+[a-z]?')
                def matches = versionPattern.matcher(changesFile.text)
                if (matches.find() && matches.find()) {
                    versionCache = matches.group()
                } else {
                    // TODO: Do not throw exception, instead don't generate cache
                    throw new IllegalStateException("Could not find version")
                }
            } else {
                versionCache = "1.1.1g" // hardcoded
            }
        }

        return versionCache
    }

    private File targetFile(String path) {
        def result = new File(getOpenSslLocation(), path)
        if (!result.exists()) {
            throw new RuntimeException("Target file '${result.absolutePath}' does not exists".toString())
        }
        return result
    }

    @Override
    String call() throws Exception {
        try {
            buildCacheIfAbsent()
        } catch (Throwable ex) {
            LOGGER.warn("Open SSL dependency cache could not be built because of an exception: ${ex.message}")
        }
        return cachingDirectory.get().asFile.absolutePath
    }

    private final class ModuleSpec {
        final String name;
        ModuleSpec(String moduleName) {
            this.name = moduleName
        }

        String relativize(String path) {
            def pathBackToRoot = []
            def n = repositoryFile("${name}/${version}").absolutePath.split('\\\\').length
            n.times {
                pathBackToRoot << '..'
            }
            return pathBackToRoot.join('/') + '/' + targetFile(path).absolutePath.replace('\\', '/')
        }

        String file(String path) {
            return """{ 
                    "name": "${FilenameUtils.getName(path)}",
                    "url": "${relativize(path)}",
                    "size": "TODO",
                    "sha1": "TODO",
                    "md5": "TODO"
                }"""
        }

        String getCppApiAttributes() {
            return """{
                    "org.gradle.usage": "cplusplus-api"
                }"""
        }

        String getNativeRuntimeAttributes() {
            return """{
                    "org.gradle.usage": "native-runtime",
                    "org.gradle.native.operatingSystem": "windows",
                    "org.gradle.native.architecture": "x86",
                    "dev.nokee.buildType": "release",
                    "dev.nokee.linkage": "shared"
                }"""
        }

        String getNativeLinkAttributes() {
            return """{
                    "org.gradle.usage": "native-link",
                    "org.gradle.native.operatingSystem": "windows",
                    "org.gradle.native.architecture": "x86",
                    "dev.nokee.buildType": "release",
                    "dev.nokee.linkage": "shared"
                }"""
        }
    }
}
