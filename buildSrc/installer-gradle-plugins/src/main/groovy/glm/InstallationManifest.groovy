package glm

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty

import javax.inject.Inject

abstract class InstallationManifest implements Named {
    private final String name
    private final CopySpec contentSpec

    @Inject
    InstallationManifest(String name, CopySpec contentSpec) {
        this.name = name
        this.contentSpec = contentSpec
    }

    String getName() {
        return name
    }

    abstract DirectoryProperty getDestinationDirectory()

    CopySpec getContentSpec() {
        return contentSpec
    }

    InstallationManifest from(Object... sourcePaths) {
        contentSpec.from(sourcePaths)
        return this
    }

    InstallationManifest from(Object sourcePaths, Action<? super CopySpec> action) {
        contentSpec.from(sourcePaths, action)
        return this
    }
}
