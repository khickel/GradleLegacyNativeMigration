package glm.installer.plugins

import glm.installer.InstallerPackage
import glm.installer.PkgZipInstallerPackage
import glm.installer.tasks.PkgZip
import org.gradle.api.Task

class PkgZipInstallerPackageBasePluginTest extends AbstractInstallerPackageBasePluginTest {
    @Override
    protected String getPluginIdUnderTest() {
        return 'glm.pkgzip-installer-package-base'
    }

    @Override
    protected Class<? extends InstallerPackage> getPackageTypeUnderTest() {
        return PkgZipInstallerPackage
    }

    @Override
    protected Class<? extends Task> getPackageTaskType() {
        return PkgZip
    }
}
