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
    private final List<String> emptyDirectories = new ArrayList<>();

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

    List<String> getEmptyDirectories() {
        return Collections.unmodifiableList(emptyDirectories);
    }

    Installer manifest(Object notation, Action<? super InstallerSpec> action) {
        def baseDirectory = baseDirectoryFactory.create(notation, name)

        action.execute(objects.newInstance(InstallerSpec, objects.newInstance(InstallerSpec.Spec, baseDirectory, contentSpec)))

        return this
    }

    Installer manifest(Object notation, String identity, Action<? super InstallerSpec> action) {
        def baseDirectory = baseDirectoryFactory.create(notation, identity)

        action.execute(objects.newInstance(InstallerSpec, objects.newInstance(InstallerSpec.Spec, baseDirectory, contentSpec)))

        return this
    }

    Installer emptyDirectory(String destinationPath) {
        // TODO: Disallow relative path that goes up one directory
        // TODO: Disallow absolute path
        // TODO: Disallow null
        // TODO: Disallow empty string
        emptyDirectories.add(destinationPath)
        return this
    }

    abstract DirectoryProperty getDestinationDirectory()
}
