package glm.installer.pkgzip.plugins

import glm.installer.Installer
import glm.installer.pkgzip.tasks.PkgZip
import glm.installer.pkgzip.PkgZipInstallerPackage
import glm.installer.plugins.AbstractInstallerPackageBasePlugin
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * The PkgZip installer package creates a self-extracting zip package.
 * It's more evolved that the basic Zip installer package, but the structure is the same.
 */
@CompileStatic
abstract class PkgZipInstallerPackageBasePlugin extends AbstractInstallerPackageBasePlugin<PkgZipInstallerPackage> {
    @Override
    protected Class<PkgZipInstallerPackage> getInstallerPackageType() {
        return PkgZipInstallerPackage
    }

    @Override
    protected Provider<RegularFile> createPackageTask(Installer installer, PkgZipInstallerPackage pkg) {
        // Defaults
        pkg.installerExtension.convention('exe')
        pkg.silentInstaller.convention(false)
        pkg.zipInputSpec.convention('.')

        // Create task
        def createTask = tasks.register(taskName(installer, pkg), PkgZip, { PkgZip task ->
            // Wiring between package model and task
            task.BDRY.value(pkg.BDRY).disallowChanges()
            task.ZSFX.value(pkg.ZSFX).disallowChanges()
            task.ZNST.value(pkg.ZNST).disallowChanges()
            task.zipInputSpec.value(pkg.zipInputSpec).disallowChanges()
            task.silentInstaller.value(pkg.silentInstaller).disallowChanges()
            task.codeSignCert.value(pkg.codeSignCert).disallowChanges()
            task.codeSignPassword.value(pkg.codeSignPassword).disallowChanges()
            task.sourceDirectory.value(installer.destinationDirectory).disallowChanges()
            task.installerFile.value(layout.buildDirectory.file(providers.zip(pkg.installerBaseName, pkg.installerExtension) { String baseName, String extension -> "tmp/${task.name}/${baseName}.${extension}".toString() }))
        } as Action<PkgZip>)

        return createTask.flatMap { it.installerFile }
    }
}
