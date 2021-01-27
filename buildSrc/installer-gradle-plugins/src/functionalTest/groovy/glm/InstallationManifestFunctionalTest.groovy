package glm

import static glm.DirectoryMatchers.hasDescendants
import static spock.util.matcher.HamcrestSupport.that

class InstallationManifestFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        file('output/a1.txt')
        file('output/a2/b1.txt')
        file('output/a2/b2.txt')
        file('output/a3/b3.txt')
        file('output/a3/b4.txt')

        buildFile << '''
            plugins {
                id 'glm.installation-manifest-base'
            }
            
            installationManifests.create('debug')
            
            tasks.create('verify', Sync) {
                from(installationManifests.debug.destinationDirectory)
                destinationDir = file('build/manifest')
            }
        '''
    }

    def "can select folders"() {
        buildFile << '''
            installationManifests.debug {
                from('output/a2')
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/manifest'), hasDescendants('b1.txt', 'b2.txt'))
    }

    def "can select individual files"() {
        buildFile << '''
            installationManifests.debug {
                from('output/a2/b1.txt')
                from('output/a3/b3.txt')
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/manifest'), hasDescendants('b1.txt', 'b3.txt'))
    }
}
