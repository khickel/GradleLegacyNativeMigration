package glm.installer

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

/**
 * Represent an installer to assemble.
 * The model serves to configure the file selection from the manifest into a staging directory before packaging the files into an installer package, i.e. Zip, self-extracting Zip, etc.
 *
 * See working with files chapter in Gradle documentation: https://docs.gradle.org/current/userguide/working_with_files.html
 */
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

    /**
     * The name of the installer, e.g.:
     * <pre>
     * installers {
     *     debug {
     *         assert name == 'debug'
     *     }
     * }
     * </pre>
     *
     * @return the installer name, never null
     */
    String getName() {
        return name
    }

    // Access the content spec for the manifest.
    CopySpec getContentSpec() {
        return contentSpec
    }

    // Access the declared empty directories
    List<String> getEmptyDirectories() {
        return Collections.unmodifiableList(emptyDirectories);
    }

    /**
     * Select a manifest from the specified dependency notation with an identity matching the installer name.
     *
     * <pre>
     * installers {
     *     debug {
     *         manifest(project(':foo')) {
     *             from('text.txt')
     *             from('base.exe') { into('bin') }
     *         }
     *     }
     * }
     * </pre>
     *
     * @param notation  a dependency notation, i.e. project(':foo') or project
     * @param action  an configure action to select the files required by the installer
     * @return this installer
     * @see #manifest(Object, String, Action) when the consuming manifest identity doesn't match the installer name
     */
    Installer manifest(Object notation, Action<? super InstallerSpec> action) {
        def baseDirectory = baseDirectoryFactory.create(notation, name)

        action.execute(objects.newInstance(InstallerSpec, objects.newInstance(InstallerSpec.Spec, baseDirectory, contentSpec)))

        return this
    }

    /**
     * Select a manifest from the specified dependency notation and identity.
     *
     * <pre>
     * installers {
     *     debug {
     *         //      Note the manifest vvvvvv identity!
     *         manifest(project(':foo'), 'base') {
     *             //                    ^^^^^^
     *             from('text.txt')
     *             from('base.exe') { into('bin') }
     *         }
     *     }
     * }
     * </pre>
     *
     * @param notation  a dependency notation, i.e. project(':foo') or project
     * @param identity  the manifest identity, e.g. name, to consume
     * @param action  an configure action to select the files required by the installer
     * @return this installer
     * @return
     */
    Installer manifest(Object notation, String identity, Action<? super InstallerSpec> action) {
        def baseDirectory = baseDirectoryFactory.create(notation, identity)

        action.execute(objects.newInstance(InstallerSpec, objects.newInstance(InstallerSpec.Spec, baseDirectory, contentSpec)))

        return this
    }

    /**
     * Declare an empty directory to create and assert that is left empty.
     *
     * @param destinationPath  the relative path of the empty directory
     * @return this installer
     */
    Installer emptyDirectory(String destinationPath) {
        // Relative and absolute path escape are caught by the stage task
        Preconditions.checkNotNull(destinationPath, "'destinationPath' must not be null")
        Preconditions.checkArgument(!Strings.isNullOrEmpty(destinationPath.trim()), "'destinationPath' must not be empty")
        emptyDirectories.add(destinationPath)
        return this
    }

    /**
     * The staging directory for the installer.
     * The property contains an implicit dependency to the staging task, e.g.:
     * <pre>
     * installers {
     *     debug {
     *         // ...
     *     }
     * }
     *
     * tasks.register('copyInstaller', Sync) {
     *     from(installers.debug.destinationDirectory)
     *     destinationDir = 'build/installer'
     * }
     * </pre>
     * The previous code will copy the 'debug' installer to 'build/installer'.
     *
     * @return a property representing the staging directory of the installer
     */
    abstract DirectoryProperty getDestinationDirectory()

    /**
     * Returns a container with all installer packages.
     * Plugins can register new package type to the container.
     * Each installer can have any number of package created from the staged installer directory.
     *
     * @return a container of all the installer packages
     */
    ExtensiblePolymorphicDomainObjectContainer<InstallerPackage> getPackages() {
        return packages
    }
}
