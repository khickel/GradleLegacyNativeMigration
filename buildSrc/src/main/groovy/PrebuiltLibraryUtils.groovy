import org.gradle.api.GradleException
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import java.util.concurrent.Callable

final class PrebuiltLibraryUtils {
    private PrebuiltLibraryUtils() {}

    private static boolean ignoresMissingFiles = false

    static Transformer<RegularFile, RegularFile> assertFileExists() {
        return { it ->
            if (!ignoresMissingFiles) {
                if (!it.asFile.exists() || !it.asFile.isFile()) {
                    throw new GradleException("The file '${it} does not exist.")
                }
            }
            return it
        }
    }

    static Transformer<Directory, Directory>  assertDirectoryExists() {
        return { it ->
            if (!ignoresMissingFiles) {
                if (!it.asFile.exists() || !it.asFile.isDirectory()) {
                    throw new GradleException("The directory '${it} does not exist.")
                }
            }
            return it
        }
    }

    private static Provider<File> envDir(ProviderFactory providers, String name) {
        return providers.environmentVariable(name).forUseAtConfigurationTime().map { new File(it) }
    }

    private static Callable<File> fails(String message) {
        return {
            if (!ignoresMissingFiles) {
                throw new GradleException(message)
            }
            return null
        }
    }

    private static String envVarRequired(String name, String where, String because = null) {
        def builder = new StringBuilder()
        builder.append("Please set the environment variable '${name}' to ${where}")
        if (because) {
            builder.append(" because ${because}")
        }
        builder.append('.')
        return builder.toString()
    }

    static Provider<File> openSslRoot(ProviderFactory providers) {
        return envDir(providers, 'GLM_OPENSSL_ROOT').orElse(providers.provider(fails(envVarRequired('GLM_OPENSSL_ROOT', 'top of an OpenSSL installation'))))
    }
}
