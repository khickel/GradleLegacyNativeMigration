package glm

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

abstract class Installer implements Named {
    private final String name
    private final CopySpec contentSpec
    private final ObjectFactory objects
    private final InstallationManifestBaseDirectoryFactory baseDirectoryFactory

    @Inject
    Installer(String name, ObjectFactory objects, CopySpec contentSpec, InstallationManifestBaseDirectoryFactory baseDirectoryFactory) {
        this.name = name
        this.objects = objects
        this.contentSpec = contentSpec
        this.baseDirectoryFactory = baseDirectoryFactory
    }

    String getName() {
        return name
    }

    CopySpec getContentSpec() {
        return contentSpec
    }

    Installer from(Object notation, Action<? super InstallerSpec> action) {
        def baseDirectory = baseDirectoryFactory.create(notation, name)

        action.execute(objects.newInstance(InstallerSpec, baseDirectory, contentSpec))

        return this
    }

    abstract DirectoryProperty getDestinationDirectory()
}
