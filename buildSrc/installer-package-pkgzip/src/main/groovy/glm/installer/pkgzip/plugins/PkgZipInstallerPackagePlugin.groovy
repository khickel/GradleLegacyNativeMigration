package glm.installer.pkgzip.plugins

import glm.installer.Installer
import glm.installer.pkgzip.PkgZipInstallerPackage
import glm.installer.plugins.InstallerBasePlugin
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class PkgZipInstallerPackagePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(InstallerBasePlugin)
        project.pluginManager.apply(PkgZipInstallerPackageBasePlugin)

        project.extensions.getByType(InstallerBasePlugin.INSTALLERS_EXTENSION_TYPE).all { Installer installer ->
            installer.packages.create('pkgZip', PkgZipInstallerPackage)
        }
    }
}
