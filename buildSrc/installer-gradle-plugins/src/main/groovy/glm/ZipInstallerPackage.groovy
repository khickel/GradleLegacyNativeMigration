package glm

import groovy.transform.CompileStatic
import org.gradle.api.provider.Property

import javax.inject.Inject

@CompileStatic
abstract class ZipInstallerPackage extends InstallerPackage {
    @Inject
    ZipInstallerPackage(String name) {
        super(name)
    }

    abstract Property<String> getBaseName()
}
