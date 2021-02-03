package glm.installer

import org.gradle.api.Transformer
import org.gradle.api.provider.Provider

/**
 * A helper representing an installation manifest base directory when building an installer.
 * @see InstallationManifestBaseDirectoryFactory use to create an instance
 */
final class InstallationManifestBaseDirectory {
    private final Provider<File> baseDirectory

    InstallationManifestBaseDirectory(Provider<File> baseDirectory) {
        this.baseDirectory = baseDirectory
    }

    /**
     * Returns a file provider of the file/directory represented by the specified path.
     * The path is evaluated relative to the manifest base directory.
     * When the provider is realized, it will assert the file/directory exists.
     *
     * @param path  a file/directory path to select inside the manifest
     * @return a file provider asserting existence when realized
     */
    Provider<File> file(String path) {
        return baseDirectory.map(fileAt(path))
    }

    private static Transformer<File, File> fileAt(String path) {
        return new Transformer<File, File>() {
            @Override
            File transform(File file) {
                def result = new File(file, path)
                if (!result.exists()) {
                    throw new IllegalArgumentException("File at '${path}' in manifest at '${file.absolutePath}' does not exists.")
                }
                return result
            }
        }
    }
}
