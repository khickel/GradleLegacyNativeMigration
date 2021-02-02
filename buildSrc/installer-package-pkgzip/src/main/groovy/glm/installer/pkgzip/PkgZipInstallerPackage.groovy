package glm.installer.pkgzip

import glm.installer.InstallerPackage
import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

import javax.inject.Inject

@CompileStatic
abstract class PkgZipInstallerPackage extends InstallerPackage {
    @Inject
    PkgZipInstallerPackage(String name) {
        super(name)
    }

    abstract Property<Boolean> getSilentInstaller() // Consider using a enum for the values

    abstract RegularFileProperty getBDRY()
    abstract RegularFileProperty getZSFX()
    abstract RegularFileProperty getCSETUP()
    abstract RegularFileProperty getZNST()
    abstract RegularFileProperty getCodeSignCert()
    abstract Property<String> getCodeSignPassword()
    abstract Property<String> getZipInputSpec()
}
