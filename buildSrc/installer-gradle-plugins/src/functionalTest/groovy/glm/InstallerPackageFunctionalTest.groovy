package glm

import dev.gradleplugins.runnerkit.TaskOutcome

class InstallerPackageFunctionalTest extends AbstractFunctionalTest {
    def setup() {
        file('output/a1.txt')
        file('output/b/b1.txt')
        file('output/b/b2.txt')
        file('output/c/c1.txt')
        file('output/c/c2.txt')

        file('docs/readme')

        buildFile << '''
            plugins {
                id 'glm.zip-installer-package'
                id 'glm.installation-manifest-base'
            }

            installationManifests {
                debug {
                    from('output')
                }
                docs {
                    from('docs')
                }
            }

            installers.create('debug') {
                manifest(project) {
                    from('b')
                }
                manifest(project, 'docs') {
                    from('readme')
                }
            }

            tasks.create('verify', Sync) {
                from(installers.debug.packages.zip.installerFile)
                destinationDir = file('build/installer')
            }
        '''
    }

    def "builds package via installer lifecycle task"() {
        expect:
        def result = succeeds('debugInstaller')
        result.task(':createZipDebugInstaller').outcome == TaskOutcome.SUCCESS
    }
}
