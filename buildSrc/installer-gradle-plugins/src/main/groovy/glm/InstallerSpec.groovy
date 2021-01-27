package glm

import groovy.transform.CompileStatic
import org.gradle.api.file.CopySpec

import javax.inject.Inject

@CompileStatic
abstract class InstallerSpec {
    private final InstallationManifestBaseDirectory baseDirectory
    private final CopySpec spec

    @Inject
    InstallerSpec(InstallationManifestBaseDirectory baseDirectory, CopySpec spec) {
        this.baseDirectory = baseDirectory
        this.spec = spec
    }

    InstallerSpec from(String sourcePath) {
        spec.from(baseDirectory.file(sourcePath))
        return this
    }
}
