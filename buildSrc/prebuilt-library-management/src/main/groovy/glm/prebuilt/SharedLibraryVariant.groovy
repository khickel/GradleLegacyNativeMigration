package glm.prebuilt

import org.gradle.api.file.RegularFileProperty

abstract class SharedLibraryVariant extends NativeVariant {
    SharedLibraryVariant(String name) {
        super(name)
    }

    abstract RegularFileProperty getImportLibraryFile()
    abstract RegularFileProperty getRuntimeLibraryFile()
}
