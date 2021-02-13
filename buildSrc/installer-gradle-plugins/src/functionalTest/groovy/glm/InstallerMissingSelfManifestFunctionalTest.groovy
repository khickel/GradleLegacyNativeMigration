package glm

class InstallerMissingSelfManifestFunctionalTest extends AbstractFunctionalTest {
    def setup() {
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

    def "throws exception when project does not apply installation manifest plugin"() {
        buildFile << '''
            installers.debug {
                manifest(project) {
                    from('readme')
                }
            }
        '''

        expect:
        failure('verify')
    }
}
