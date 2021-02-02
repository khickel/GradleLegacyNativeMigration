package glm.installer.plugins

import glm.installer.Installer
import glm.installer.ZipInstallerPackage
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

import static glm.installer.plugins.InstallerBasePlugin.INSTALLERS_EXTENSION_TYPE

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
