package glm.installer.plugins

import glm.installer.Installer
import glm.installer.PkgZipInstallerPackage
import glm.installer.tasks.PkgZip
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

@CompileStatic
abstract class PkgZipInstallerPackageBasePlugin extends AbstractInstallerPackageBasePlugin<PkgZipInstallerPackage> {
    @Override
    protected Class<PkgZipInstallerPackage> getInstallerPackageType() {
        return PkgZipInstallerPackage
    }

    @Override
    protected Provider<RegularFile> createPackageTask(Installer installer, PkgZipInstallerPackage pkg) {
        pkg.installerExtension.convention('exe')
        def createTask = tasks.register(taskName(installer, pkg), PkgZip, { PkgZip task ->
            task.BDRY.value(pkg.BDRY).disallowChanges()
            task.ZSFX.value(pkg.ZSFX).disallowChanges()
            task.ZNST.value(pkg.ZNST).disallowChanges()
            task.silentInstaller.value(pkg.silentInstaller).disallowChanges()
            task.codeSignCert.value(pkg.codeSignCert).disallowChanges()
            task.codeSignPassword.value(pkg.codeSignPassword).disallowChanges()
            task.sourceDirectory.value(installer.destinationDirectory).disallowChanges()
            task.installerFile.value(layout.buildDirectory.file(providers.zip(pkg.installerBaseName, pkg.installerExtension) { String baseName, String extension -> "tmp/${task.name}/${baseName}.${extension}".toString() }))
        } as Action<PkgZip>)

        return createTask.flatMap { it.installerFile }
    }
}
