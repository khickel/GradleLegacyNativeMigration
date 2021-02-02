package glm.installer.plugins

import glm.installer.InstallerPackage
import glm.installer.PkgZipInstallerPackage

class PkgZipInstallerPackagePluginTest extends AbstractInstallerPackagePluginTest {
    @Override
    protected String getPluginIdUnderTest() {
        return 'glm.pkgzip-installer-package'
    }

    @Override
    protected Class<? extends InstallerPackage> getPackageTypeUnderTest() {
        return PkgZipInstallerPackage
    }
}
