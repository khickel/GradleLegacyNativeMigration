package glm.installer.plugins

import glm.installer.Installer
import glm.installer.PkgZipInstallerPackage
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

import static glm.installer.plugins.InstallerBasePlugin.INSTALLERS_EXTENSION_TYPE

@CompileStatic
class PkgZipInstallerPackagePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(InstallerBasePlugin)
        project.pluginManager.apply(PkgZipInstallerPackageBasePlugin)

        project.extensions.getByType(INSTALLERS_EXTENSION_TYPE).all { Installer installer ->
            installer.packages.create('pkgZip', PkgZipInstallerPackage)
        }
    }
}
