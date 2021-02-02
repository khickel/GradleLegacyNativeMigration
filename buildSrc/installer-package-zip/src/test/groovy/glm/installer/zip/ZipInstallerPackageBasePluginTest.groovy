package glm.installer.zip

import glm.installer.InstallerPackage
import glm.installer.fixtures.AbstractInstallerPackageBasePluginTest
import org.gradle.api.Task
import org.gradle.api.tasks.bundling.Zip

class ZipInstallerPackageBasePluginTest extends AbstractInstallerPackageBasePluginTest {
    @Override
    protected String getPluginIdUnderTest() {
        return 'glm.zip-installer-package-base'
    }

    @Override
    protected Class<? extends InstallerPackage> getPackageTypeUnderTest() {
        return ZipInstallerPackage
    }

    @Override
    protected Class<? extends Task> getPackageTaskType() {
        return Zip
    }
}
