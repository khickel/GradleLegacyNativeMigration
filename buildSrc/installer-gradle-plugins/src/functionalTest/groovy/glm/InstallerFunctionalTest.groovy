package glm

import static glm.DirectoryMatchers.hasDescendants
import static spock.util.matcher.HamcrestSupport.that

class InstallerFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        file('manifest/output/a1.txt')
        file('manifest/output/b/b1.txt')
        file('manifest/output/b/b2.txt')
        file('manifest/output/c/c1.txt')
        file('manifest/output/c/c2.txt')

        settingsFile << '''
            include 'manifest'
        '''
        file('manifest/build.gradle') << '''
            plugins {
                id 'glm.installation-manifest-base'
            }
            
            installationManifests.create('debug') {
                from('output')
            }
        '''

        buildFile << '''
            plugins {
                id 'glm.installer-base'
            }
            
            installers.create('debug')
            
            tasks.create('verify', Sync) {
                from(installers.debug.destinationDirectory)
                destinationDir = file('build/installer')
            }
        '''
    }

    def "can pick individual files from manifest into installer root"() {
        buildFile << '''
            installers.debug {
                from(project(':manifest')) {
                    select('a1.txt')
                    select('c/c2.txt')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('a1.txt', 'c2.txt'))
    }

    def "can pick individual folder from manifest into installer root"() {
        buildFile << '''
            installers.debug {
                from(project(':manifest')) {
                    select('b')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('b1.txt', 'b2.txt'))
    }
}
