package glm.installer.zip

import glm.installer.InstallerPackage
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
abstract class ZipInstallerPackage extends InstallerPackage {
    @Inject
    ZipInstallerPackage(String name) {
        super(name)
    }
}
