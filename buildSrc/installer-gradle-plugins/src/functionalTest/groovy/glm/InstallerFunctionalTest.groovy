package glm

import static glm.DirectoryMatchers.*
import static spock.util.matcher.HamcrestSupport.that

class InstallerFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        file('manifest/output/a1.txt')
        file('manifest/output/b/b1.txt')
        file('manifest/output/b/b2.txt')
        file('manifest/output/c/c1.txt')
        file('manifest/output/c/c2.txt')

        file('manifest/docs/readme')

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
            installationManifests.create('docs') {
                from('docs')
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

    def "can rename files"() {
        buildFile << '''
            installers.debug {
                manifest(project(':manifest')) {
                    from('a1.txt') {
                        rename('a1.txt', 'a1')
                    }
                    from('b') {
                        rename('(.*).txt', '$1')
                    }
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('a1', 'b1', 'b2'))
    }

    def "can consume different manifest identity"() {
        buildFile << '''
            installers.debug {
                manifest(project(':manifest'), 'docs') {
                    from('readme')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendants('readme'))
    }

    def "can ensure empty directories are present"() {
        buildFile << '''
            installers.debug {
                // Make sure the installer has sources
                manifest(project(':manifest'), 'docs') { from('readme') }

                // Test empty directory
                emptyDirectory('config')
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendantDirectories('config'))
        that(file('build/installer/config'), anEmptyDirectory())
    }

    def "can ensure nested empty directories are present"() {
        buildFile << '''
            installers.debug {
                // Make sure the installer has sources
                manifest(project(':manifest'), 'docs') { from('readme') }

                // Test nested empty directory
                emptyDirectory('sys/config')
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/installer'), hasDescendantDirectories('sys', 'sys/config'))
        that(file('build/installer/sys/config'), anEmptyDirectory())
    }

    def "throws exception when empty directories are not empty"() {
        buildFile << '''
            installers.debug {
                manifest(project(':manifest'), 'docs') {
                    from('readme') { into('config') }
                }
                emptyDirectory('config')
            }
        '''

        expect:
        failure('verify')
    }

    def "throws exception when empty directories are outside the base directory (relative)"() {
        buildFile << '''
            installers.debug {
                manifest(project(':manifest')) {
                    from('b')
                }
                emptyDirectory('../config')
            }
        '''

        expect:
        failure('verify')
    }

    def "throws exception when empty directories are outside the base directory (absolute)"() {
        buildFile << """
            installers.debug {
                manifest(project(':manifest')) {
                    from('b')
                }
                emptyDirectory('${file('config').absolutePath.replace('\\', '/')}')
            }
        """

        expect:
        failure('verify')
    }

    def "throws exception when picked file or directory is missing"() {
        buildFile << """
            installers.debug {
                manifest(project(':manifest')) {
                    from('missing')
                }
            }
        """

        expect:
        failure('verify')
    }
}
