package glm.installer

import groovy.transform.CompileStatic
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

@CompileStatic
abstract class InstallerPackage implements Named {
    private final String name

    protected InstallerPackage(String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    /**
     * The installer file for the installer package.
     * The property contains an implicit dependency to the package creation task, e.g.:
     * <pre>
     * installers {
     *     debug {
     *         packages {
     *             zip(ZipInstallerPackage) {}
     *         }
     *     }
     * }
     *
     * tasks.register('copyPackage', Sync) {
     *     from(installers.debug.packages.zip.installerFile)
     *     destinationDir = 'build/installer'
     * }
     * </pre>
     * The previous code will copy the 'debug' 'zip' installer package to 'build/installer'.
     *
     * By default, the installer filename is <code>${installerBaseName}.${installerExtension}</code>.
     *
     * @return a property representing the installer file for the created package
     */
    abstract RegularFileProperty getInstallerFile()

    /**
     * @return the base name of the installer filename
     */
    abstract Property<String> getInstallerBaseName()

    /**
     * @return the extension of the installer filename, i.e. zip, exe
     */
    abstract Property<String> getInstallerExtension()
}
