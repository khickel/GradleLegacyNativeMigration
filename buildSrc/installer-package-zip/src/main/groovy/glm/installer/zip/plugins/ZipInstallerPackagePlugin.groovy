package glm.installer.zip.plugins

import glm.installer.Installer
import glm.installer.plugins.InstallerBasePlugin
import glm.installer.zip.ZipInstallerPackage
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class ZipInstallerPackagePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(InstallerBasePlugin)
        project.pluginManager.apply(ZipInstallerPackageBasePlugin)

        project.extensions.getByType(InstallerBasePlugin.INSTALLERS_EXTENSION_TYPE).all { Installer installer ->
            installer.packages.create('zip', ZipInstallerPackage)
        }
    }
}
