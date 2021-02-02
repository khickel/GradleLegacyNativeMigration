package glm.installer.plugins

import glm.installer.Installer
import glm.installer.InstallerPackage
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.TaskContainer

import javax.inject.Inject

import static glm.installer.plugins.InstallerBasePlugin.INSTALLERS_EXTENSION_TYPE

abstract class AbstractInstallerPackageBasePlugin<T extends InstallerPackage> implements Plugin<Project> {
    @Override
    final void apply(Project project) {
        project.plugins.withType(InstallerBasePlugin) {
            project.extensions.getByType(INSTALLERS_EXTENSION_TYPE).all { Installer installer ->
                installer.packages.registerFactory(installerPackageType, newFactory())
                installer.packages.withType(installerPackageType).all { InstallerPackage pkg ->
                    pkg.installerBaseName.convention("${installer.name}Installer".toString())
                    pkg.installerFile.value(createPackageTask(installer, (T) pkg)).disallowChanges()
                }
            }
        }
    }

    @Inject
    protected abstract ObjectFactory getObjects()

    @Inject
    protected abstract TaskContainer getTasks()

    @Inject
    protected abstract ProjectLayout getLayout()

    @Inject
    protected abstract ProviderFactory getProviders()

    protected abstract Class<T> getInstallerPackageType()

    protected abstract Provider<RegularFile> createPackageTask(Installer installer, T pkg)

    protected NamedDomainObjectFactory<T> newFactory() {
        return new NamedDomainObjectFactory<T>() {
            @Override
            T create(String name) {
                return objects.newInstance(installerPackageType, name)
            }
        }
    }

    protected String taskName(Installer installer, InstallerPackage pkg) {
        return "create${pkg.name.capitalize()}${installer.name.capitalize()}Installer".toString()
    }
}
