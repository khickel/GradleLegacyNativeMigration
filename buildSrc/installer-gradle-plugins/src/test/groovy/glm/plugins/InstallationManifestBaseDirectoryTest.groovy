package glm.plugins

import glm.InstallationManifestBaseDirectory
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

import static glm.plugins.TestUtils.providerFactory

class InstallationManifestBaseDirectoryTest extends Specification {
    @TempDir Path testDirectory

    private InstallationManifestBaseDirectory createSubject() {
        return new InstallationManifestBaseDirectory(providerFactory().provider { testDirectory.toFile().canonicalFile })
    }

    private InstallationManifestBaseDirectory createThrowingSubject() {
        return new InstallationManifestBaseDirectory(providerFactory().provider { throw new UnsupportedOperationException("should not realize") })
    }

    private File file(String path) {
        return testDirectory.resolve(path).toFile().canonicalFile
    }

    def "can create file provider for paths under base directory"() {
        expect:
        createSubject().file('foo').get() == file('foo')
        createSubject().file('foo/bar').get() == file('foo/bar')
        createSubject().file('f/a/r').get() == file('f/a/r')
    }

    def "does not realize base directory provider when creating child file provider"() {
        when:
        createThrowingSubject().file('foo')

        then:
        noExceptionThrown()
    }
}
