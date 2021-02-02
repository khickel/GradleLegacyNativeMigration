package glm.plugins

import glm.Installer
import glm.InstallerPackage
import glm.ZipInstallerPackage
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.bundling.Zip

import javax.inject.Inject

import static glm.plugins.InstallerBasePlugin.INSTALLERS_EXTENSION_TYPE

@CompileStatic
class ZipInstallerPackageBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.plugins.withType(InstallerBasePlugin) {
            project.extensions.getByType(INSTALLERS_EXTENSION_TYPE).all { Installer installer ->
                installer.packages.registerFactory(ZipInstallerPackage) { String name -> project.objects.newInstance(ZipInstallerPackage, name) }

                installer.packages.withType(ZipInstallerPackage).all { ZipInstallerPackage pkg ->
                    pkg.baseName.convention("${installer.name}Installer".toString())
                    def createTask = project.tasks.register("create${pkg.name.capitalize()}${installer.name.capitalize()}Installer".toString(), Zip, { Zip task ->
                        task.from(installer.destinationDirectory)
                        task.archiveBaseName.value(pkg.baseName).disallowChanges()
                        task.destinationDirectory.set(project.layout.buildDirectory.dir("tmp/${task.name}"))
                    } as Action<Zip>)
                    pkg.installerFile.value(createTask.flatMap { it.archiveFile }).disallowChanges()
                }
            }
        }
    }
}
