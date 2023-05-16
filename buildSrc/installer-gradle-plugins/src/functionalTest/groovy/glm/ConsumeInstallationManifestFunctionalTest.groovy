package glm

import spock.lang.Specification
import spock.util.matcher.HamcrestSupport

import static glm.DirectoryMatchers.hasDescendants
import static spock.util.matcher.HamcrestSupport.that

class ConsumeInstallationManifestFunctionalTest extends AbstractFunctionalTest {
    def "can consume installation manifest from another project"() {
        given:
        file('producer/output/a1.txt')
        file('producer/output/b/b1.txt')
        file('producer/output/c/c1.txt')
        file('producer/output/c/c2.txt')

        and:
        settingsFile << '''
            include 'producer', 'consumer'
        '''
        file('producer/build.gradle') << '''
            plugins {
                id 'glm.installation-manifest-base'
            }
            
            installationManifests.create('debug') {
                from('output')
            }
        '''
        file('consumer/build.gradle') << '''
            configurations.create('manifest') {
                canBeConsumed = false
                canBeResolved = true
                attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, 'installation-manifest'))
            }
            
            dependencies {
                manifest project(':producer')
            }
            
            tasks.create('verify', Sync) {
                from configurations.manifest
                destinationDir = file('build/manifest')
            }
        '''

        expect:
        succeeds(':consumer:verify')
        that(file('consumer/build/manifest'),
                hasDescendants('a1.txt', 'b/b1.txt', 'c/c1.txt', 'c/c2.txt'))
    }
}
