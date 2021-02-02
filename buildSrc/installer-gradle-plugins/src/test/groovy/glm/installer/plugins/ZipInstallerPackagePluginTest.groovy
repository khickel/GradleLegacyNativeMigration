package glm.installer.plugins

import glm.installer.InstallerPackage
import glm.installer.ZipInstallerPackage

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
