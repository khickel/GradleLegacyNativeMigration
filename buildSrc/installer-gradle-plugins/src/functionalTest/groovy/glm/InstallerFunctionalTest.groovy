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
                manifest(project(':manifest')) {
                    from('a1.txt')
                    from('c/c2.txt')
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
                manifest(project(':manifest')) {
                    from('b')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('b1.txt', 'b2.txt'))
    }

    def "can relocate directory into a directory"() {
        buildFile << '''
            installers.debug {
                manifest(project(':manifest')) {
                    from('b') {
                        into('subsystem-b')
                    }
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('subsystem-b/b1.txt', 'subsystem-b/b2.txt'))
    }

    def "can relocate files into a directory"() {
        buildFile << '''
            installers.debug {
                manifest(project(':manifest')) {
                    from('b/b1.txt') {
                        into('first')
                    }
                    from('b/b2.txt') {
                        into('second')
                    }
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('first/b1.txt', 'second/b2.txt'))
    }

    def "can add multiple files into directory"() {
        buildFile << '''
            installers.debug {
                manifest(project(':manifest')) {
                    into('subsystem-b') {
                        from('b')
                        from('a1.txt')
                    }
                    into('subsystem-c') {
                        from('c')
                        from('a1.txt')
                    }
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants(
                'subsystem-b/a1.txt', 'subsystem-b/b1.txt', 'subsystem-b/b2.txt',
                'subsystem-c/a1.txt', 'subsystem-c/c1.txt', 'subsystem-c/c2.txt'))
    }
}
