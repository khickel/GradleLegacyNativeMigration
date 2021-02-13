package glm.installer.pkgzip

import glm.installer.InstallerPackage
import glm.installer.fixtures.AbstractInstallerPackagePluginTest

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
