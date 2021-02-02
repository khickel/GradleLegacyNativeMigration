package glm.plugins

import glm.Installer
import glm.ZipInstallerPackage
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

import static glm.plugins.InstallerBasePlugin.INSTALLERS_EXTENSION_TYPE

@CompileStatic
class ZipInstallerPackagePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(InstallerBasePlugin)
        project.pluginManager.apply(ZipInstallerPackageBasePlugin)

        project.extensions.getByType(INSTALLERS_EXTENSION_TYPE).all { Installer installer ->
            installer.packages.create('zip', ZipInstallerPackage)
        }
    }
}
