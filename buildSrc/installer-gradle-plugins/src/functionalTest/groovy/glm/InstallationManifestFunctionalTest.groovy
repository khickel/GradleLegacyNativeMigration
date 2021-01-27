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

        file('docs/readme')
        file('docs/ch1.txt')
        file('docs/ch2.txt')
        file('docs/ch3.txt')
        file('docs/ch4.txt')

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

    def "can select from distinct file tree"() {
        buildFile << '''
            installationManifests.debug {
                from('output/a2/b2.txt')
                from('docs/readme')
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/manifest'), hasDescendants('b2.txt', 'readme'))
    }

    def "can select the output of another task"() {
        buildFile << '''
            def generateTask = tasks.register('generate') {
                outputs.dir(temporaryDir)
                doLast {
                    new File(temporaryDir, 'gen') << 'some-generated-file'
                    def fooFile = new File(temporaryDir, 'bin/foo.exe')
                    fooFile.parentFile.mkdirs()
                    fooFile << 'compiled PE'
                }
            }

            installationManifests.debug {
                from(generateTask)
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/manifest'), hasDescendants('gen', 'bin/foo.exe'))
    }

    def "can include specific files"() {
        buildFile << '''
            installationManifests.debug {
                from('output/a3') {
                    include('*3*')
                }
                from('docs') {
                    include('*1*')
                    include('readme')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/manifest'), hasDescendants('b3.txt', 'readme', 'ch1.txt'))
    }

    def "can exclude specific files"() {
        buildFile << '''
            installationManifests.debug {
                from('output/a3') {
                    exclude('b3.txt')
                }
                from('docs') {
                    exclude('ch3.txt')
                }
            }
        '''

        expect:
        succeeds('verify')
        that(file('build/manifest'), hasDescendants('b4.txt', 'readme', 'ch1.txt', 'ch2.txt', 'ch4.txt'))
    }
}
