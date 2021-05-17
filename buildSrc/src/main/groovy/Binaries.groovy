import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import glm.codesigning.SignedBinary
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

@CompileStatic
final class Binaries {
    static <T extends Variant> Transformer<Provider<RegularFile>, T> selectDevelopmentBinaryFile() {
        return { T variant ->
            def binary = variant.developmentBinary.get()
            if (binary instanceof ExecutableBinary) {
                return ((ExecutableBinary) binary).linkTask.flatMap { it.linkedFile }
            } else if (binary instanceof SharedLibraryBinary) {
                return ((SharedLibraryBinary) binary).linkTask.flatMap { it.linkedFile }
            } else if (binary instanceof SignedBinary) {
                return ((SignedBinary) binary).signTask.flatMap { it.signedFile }
            } else {
                throw new UnsupportedOperationException("Unknown ${binary}")
            }
        } as Transformer<Provider<RegularFile>, T>
    }
}
