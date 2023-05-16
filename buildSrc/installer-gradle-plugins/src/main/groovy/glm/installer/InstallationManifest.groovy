package glm.installer

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty

import javax.inject.Inject

/**
 * Represent an installation manifest.
 * The model serve a way to configure the input and access the output of the manifest staging process.
 *
 * See working with files chapter in Gradle documentation: https://docs.gradle.org/current/userguide/working_with_files.html
 */
abstract class InstallationManifest implements Named {
    private final String name
    private final CopySpec contentSpec

    @Inject
    InstallationManifest(String name, CopySpec contentSpec) {
        this.name = name
        this.contentSpec = contentSpec
    }

    /**
     * The name of the installation manifest, e.g.:
     * <pre>
     * installationManifests {
     *     debug {
     *         assert name == 'debug'
     *     }
     * }
     * </pre>
     *
     * @return the manifest name, never null
     */
    String getName() {
        return name
    }

    /**
     * The staging directory for the manifest.
     * The property contains an implicit dependency to the staging task, e.g.:
     * <pre>
     * installationManifests {
     *     debug {
     *         // ...
     *     }
     * }
     *
     * tasks.register('copyManifest', Sync) {
     *     from(installationManifests.debug.destinationDirectory)
     *     destinationDir = 'build/manifest'
     * }
     * </pre>
     * The previous code will copy the 'debug' manifest to 'build/manifest'.
     *
     * @return a property representing the staging directory of the manifest
     */
    abstract DirectoryProperty getDestinationDirectory()

    // Access the content spec for the manifest.
    CopySpec getContentSpec() {
        return contentSpec
    }

    /**
     * Select a list of source paths to copy into the staging directory.
     * See {@link Project#files(Object...)} learn more on which "file" type are accepted.
     * Any sourcePaths will be added to in the staging directory directly, i.e.
     * <pre>
     * installationManifest {
     *     debug {
     *         from('foo.txt')
     *         from('dir') // containing bar.txt, nested/far.txt
     *     }
     * }
     * </pre>
     * Would result in {@literal <base>/foo.txt}, {@literal <base>/bar.txt}, and {@literal <base>/far.txt}.
     *
     * @param sourcePaths source paths to include in the manifest.
     * @return this manifest
     */
    InstallationManifest from(Object... sourcePaths) {
        contentSpec.from(sourcePaths)
        return this
    }

    /**
     * Select a source path and further configure how it should be copied into the manifest.
     * We can use the action to rename the file/directory, specify include/exclude patterns, specify a destination directory within the manifest, etc.
     *
     * @param sourcePath  a source path to include and configure
     * @param action  a configure action
     * @return this manifest
     */
    InstallationManifest from(Object sourcePath, Action<? super CopySpec> action) {
        contentSpec.from(sourcePath, action)
        return this
    }

    /**
     * Reverse configuration of files to include in the manifest by specifying the destination directory within the manifest before specifying the files to include.
     *
     * Note that:
     * <pre>
     * installationManifests.debug {
     *     from('foo') { into('bar') }
     * }
     * </pre>
     * and:
     * <pre>
     * installationManifests.debug {
     *     into('bar') { from('foo') }
     * }
     * </pre>
     * are functionally the same.
     *
     * It's more a matter of specifying a source path into a directory vs specifying a directory to put multiple source paths.
     *
     * @param destinationPath  a destination path relative to the manifest staging directory
     * @param action  a configure action
     * @return this manifest
     */
    InstallationManifest into(Object destinationPath, Action<? super CopySpec> action) {
        contentSpec.into(destinationPath, action)
        return this
    }
}
