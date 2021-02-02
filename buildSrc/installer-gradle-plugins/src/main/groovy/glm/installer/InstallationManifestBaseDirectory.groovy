package glm.installer

import org.gradle.api.Transformer
import org.gradle.api.provider.Provider

import java.nio.file.FileVisitResult
import java.nio.file.FileVisitor
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

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
                def result = new File(file, path)
                if (!result.exists()) {
                    throw new IllegalArgumentException("File at '${path}' in manifest at '${file.absolutePath}' does not exists.")
                }
                return result
            }
        }
    }
}
