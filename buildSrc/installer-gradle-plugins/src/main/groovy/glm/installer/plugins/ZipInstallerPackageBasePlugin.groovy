package glm.installer.plugins

import glm.installer.Installer
import glm.installer.ZipInstallerPackage
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.Zip

@CompileStatic
abstract class ZipInstallerPackageBasePlugin extends AbstractInstallerPackageBasePlugin<ZipInstallerPackage> {
    @Override
    protected Class<ZipInstallerPackage> getInstallerPackageType() {
        return ZipInstallerPackage
    }

    @Override
    protected Provider<RegularFile> createPackageTask(Installer installer, ZipInstallerPackage pkg) {
        pkg.installerExtension.convention('zip')
        def createTask = tasks.register(taskName(installer, pkg), Zip, { Zip task ->
            task.from(installer.destinationDirectory)
            task.archiveBaseName.value(pkg.installerBaseName).disallowChanges()
            task.archiveExtension.value(pkg.installerExtension).disallowChanges()
            task.destinationDirectory.set(layout.buildDirectory.dir("tmp/${task.name}"))
        } as Action<Zip>)

        return createTask.flatMap { it.archiveFile }
    }
}
