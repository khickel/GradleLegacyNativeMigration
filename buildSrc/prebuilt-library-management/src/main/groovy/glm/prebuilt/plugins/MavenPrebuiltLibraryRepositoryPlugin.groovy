package glm.prebuilt.plugins

import glm.prebuilt.GradleMetadataCacheBuilder
import glm.prebuilt.NativeLibraryComponent
import glm.prebuilt.PrebuiltLibraryManagement
import glm.prebuilt.PrebuiltMavenRepository
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

import javax.inject.Inject

@CompileStatic
abstract class MavenPrebuiltLibraryRepositoryPlugin implements Plugin<Project> {
    private GradleMetadataCacheBuilder cacheBuilder

    @Override
    void apply(Project project) {
        project.pluginManager.apply(PrebuiltLibraryManagementBasePlugin)


        def extension = project.extensions.getByType(PrebuiltLibraryManagement)
        cacheBuilder = new GradleMetadataCacheBuilder(extension.libraries, project.layout.buildDirectory.dir('prebuilt-libraries'))
        extension.repositories.addAllLater(repositories(extension))

        project.afterEvaluate {
            project.subprojects { Project prj ->
                extension.repositories.all { PrebuiltMavenRepository prebuiltRepository ->
                    prj.repositories.maven { MavenArtifactRepository repository ->
                        repository.name = prebuiltRepository.name
                        repository.url = prebuiltRepository.url
                        repository.mavenContent { content ->
                            content.includeGroup(prebuiltRepository.group)
                        }
                        repository.metadataSources.gradleMetadata()
                    }
                }
            }
        }
    }

    @Inject
    abstract ObjectFactory getObjects()

    @Inject
    abstract ProviderFactory getProviders()

    private Provider<List<PrebuiltMavenRepository>> repositories(PrebuiltLibraryManagement extension) {
        return objects.listProperty(PrebuiltMavenRepository).value(providers.provider { this.computeRepositories(extension.libraries) })
    }

    private Iterable<PrebuiltMavenRepository> computeRepositories(Collection<NativeLibraryComponent> libraries) {
        return libraries.collect { it.groupId.get() }.unique().collect { groupId ->
            def repository = objects.newInstance(PrebuiltMavenRepository, groupId, cacheBuilder)
            repository.group = groupId
            return repository
        }
    }
}
