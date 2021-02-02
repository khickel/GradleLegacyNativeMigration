package glm.installer.pkgzip

import glm.installer.InstallerPackage
import glm.installer.fixtures.AbstractInstallerPackageBasePluginTest
import glm.installer.pkgzip.tasks.PkgZip
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
