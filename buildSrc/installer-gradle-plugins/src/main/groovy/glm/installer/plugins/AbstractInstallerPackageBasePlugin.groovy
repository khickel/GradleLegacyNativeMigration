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

/**
 * Base implementation for installer package plugin.
 *
 * This plugin will register a new installer package to every installer.
 * The implementation assume one task per package
 * @param <T>
 */
abstract class AbstractInstallerPackageBasePlugin<T extends InstallerPackage> implements Plugin<Project> {
    @Override
    final void apply(Project project) {
        // Wait until the glm.installer-base plugin is applied
        project.pluginManager.withPlugin('glm.installer-base') {
            // Configure each installer...
            project.extensions.getByType(INSTALLERS_EXTENSION_TYPE).all { Installer installer ->
                // By registering a factory for installer package type:
                //  installers.debug.packages {
                //    foo(<packageType>) {
                //      /* configure the installer package */
                //    }
                //  }
                installer.packages.registerFactory(installerPackageType, newFactory())

                // ... for each installer package provided by this base plugin...
                installer.packages.withType(installerPackageType).all { InstallerPackage pkg ->
                    // Configure the installer base name
                    pkg.installerBaseName.convention("${installer.name}Installer".toString())

                    // Create the package task
                    def packageTask = createPackageTask(installer, (T) pkg)

                    // Bind the package file as the package installer file
                    pkg.installerFile.value(packageTask).disallowChanges()
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

    /**
     * @return the installer package type provided by this plugin
     */
    protected abstract Class<T> getInstallerPackageType()

    /**
     * Creates the package task and returns the task output as a file provider.
     *
     * @param installer  the installer
     * @param pkg  the installer package
     * @return a provider to the installer package file (e.g. the installer file)
     */
    protected abstract Provider<RegularFile> createPackageTask(Installer installer, T pkg)

    /**
     * @return a factory for the installer package model type
     */
    protected NamedDomainObjectFactory<T> newFactory() {
        return new NamedDomainObjectFactory<T>() {
            @Override
            T create(String name) {
                return objects.newInstance(installerPackageType, name)
            }
        }
    }

    // Utility for composing the installer package task name
    protected String taskName(Installer installer, InstallerPackage pkg) {
        return "create${pkg.name.capitalize()}${installer.name.capitalize()}Installer".toString()
    }
}
