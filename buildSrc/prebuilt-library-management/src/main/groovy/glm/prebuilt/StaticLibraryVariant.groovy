package glm.prebuilt

import org.gradle.api.file.RegularFileProperty

abstract class StaticLibraryVariant extends NativeVariant {
    StaticLibraryVariant(String name) {
        super(name)
    }

    abstract RegularFileProperty getLibraryFile()
}
