package glm.installer

import org.gradle.api.Transformer
import org.gradle.api.provider.Provider

final class InstallationManifestBaseDirectory {
    private final Provider<File> baseDirectory

    InstallationManifestBaseDirectory(Provider<File> baseDirectory) {
        this.baseDirectory = baseDirectory
    }

    Provider<File> file(String path) {
        return baseDirectory.map(fileAt(path))
    }

    private static Transformer<File, File> fileAt(String path) {
        return new Transformer<File, File>() {
            @Override
            File transform(File file) {
                return new File(file, path)
            }
        }
    }
}
