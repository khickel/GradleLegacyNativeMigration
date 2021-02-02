package glm

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

@CompileStatic
abstract class Installer implements Named {
    private final String name
    private final CopySpec contentSpec
    private final ObjectFactory objects
    private final InstallationManifestBaseDirectoryFactory baseDirectoryFactory
    private final List<String> emptyDirectories = new ArrayList<>()
    private final ExtensiblePolymorphicDomainObjectContainer<InstallerPackage> packages

    @Inject
    Installer(String name, ObjectFactory objects, CopySpec contentSpec, InstallationManifestBaseDirectoryFactory baseDirectoryFactory) {
        this.name = name
        this.objects = objects
        this.contentSpec = contentSpec
        this.baseDirectoryFactory = baseDirectoryFactory
        this.packages = objects.polymorphicDomainObjectContainer(InstallerPackage)
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
        // Relative and absolute path escape are caught by the stage task
        Preconditions.checkNotNull(destinationPath, "'destinationPath' must not be null")
        Preconditions.checkArgument(!Strings.isNullOrEmpty(destinationPath.trim()), "'destinationPath' must not be empty")
        emptyDirectories.add(destinationPath)
        return this
    }

    abstract DirectoryProperty getDestinationDirectory()

    ExtensiblePolymorphicDomainObjectContainer<InstallerPackage> getPackages() {
        return packages
    }
}
