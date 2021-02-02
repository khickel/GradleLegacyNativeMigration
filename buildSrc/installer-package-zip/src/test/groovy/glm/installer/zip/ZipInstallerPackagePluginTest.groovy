package glm.installer.zip

import glm.installer.InstallerPackage
import glm.installer.fixtures.AbstractInstallerPackagePluginTest

class ZipInstallerPackagePluginTest extends AbstractInstallerPackagePluginTest {

    @Override
    protected String getPluginIdUnderTest() {
        return 'glm.zip-installer-package'
    }

    @Override
    protected Class<? extends InstallerPackage> getPackageTypeUnderTest() {
        return ZipInstallerPackage
    }
}
