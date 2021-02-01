package glm

import static glm.DirectoryMatchers.hasDescendants
import static spock.util.matcher.HamcrestSupport.that

class InstallerSelfManifestFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        file('output/a1.txt')
        file('output/b/b1.txt')
        file('output/b/b2.txt')
        file('output/c/c1.txt')
        file('output/c/c2.txt')

        file('docs/readme')

        buildFile << '''
            plugins {
                id 'glm.installation-manifest-base'
                id 'glm.installer-base'
            }
            
            installationManifests.create('debug') {
                from('output')
            }
            installationManifests.create('docs') {
                from('docs')
            }
            
            installers.create('debug')
            
            tasks.create('verify', Sync) {
                from(installers.debug.destinationDirectory)
                destinationDir = file('build/installer')
            }
        '''
    }

    def "can consume self manifest using the project instance"() {
        buildFile << '''
            installers.debug {
                manifest(project) {
                    from('b')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('b1.txt', 'b2.txt'))
    }

    def "can consume self manifest using a project instance with project path"() {
        buildFile << '''
            installers.debug {
                manifest(project(':')) {
                    from('c')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('c1.txt', 'c2.txt'))
    }

    def "can consume self manifest using identity"() {
        buildFile << '''
            installers.debug {
                manifest(project, 'docs') {
                    from('readme')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('readme'))
    }

    def "throws exception when identity represent a missing manifest"() {
        buildFile << '''
            installers.debug {
                manifest(project, 'missing') {
                    from('some-file')
                }
            }
        '''

        expect:
        failure('verify')
    }

    def "throws exception when installer represent a missing manifest"() {
        buildFile << '''
            installers.missing {
                manifest(project) {
                    from('some-file')
                }
            }
        '''

        expect:
        failure('verify')
    }
}
