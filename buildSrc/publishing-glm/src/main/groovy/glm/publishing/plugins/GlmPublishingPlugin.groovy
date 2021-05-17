package glm.publishing.plugins

import glm.publishing.GlmPublication
import glm.publishing.PublishToGlmRepository
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.gradle.api.model.ObjectFactory
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.plugins.PublishingPlugin

@CompileStatic
class GlmPublishingPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(PublishingPlugin)

        def extension = project.extensions.getByType(PublishingExtension)
        registerGlmPublicationFactory(extension.publications, newGlmPublicationFactory(project.objects))

        def publishTask = project.tasks.named(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME)
        
        extension.publications.withType(GlmPublication) { GlmPublication publication ->
            extension.repositories.withType(FlatDirectoryArtifactRepository) { FlatDirectoryArtifactRepository repository ->
                def publishArtifactTask = project.tasks.register(publishTaskName(publication, repository), PublishToGlmRepository, { PublishToGlmRepository task ->
                    task.getRepository().value(repository).disallowChanges()
                    task.getPublication().value(publication).disallowChanges()
                    task.getClasspath().from(project.configurations.detachedConfiguration(project.dependencies.create('commons-codec:commons-codec:1.15')))
                } as Action<PublishToGlmRepository>)
                publishTask.configure { it.dependsOn(publishArtifactTask) }
            }
        }
    }

    // Follows the convention in core Gradle plugin
    static String publishTaskName(Publication publication, ArtifactRepository repository) {
        return "publish${publication.name.capitalize()}To${repository.name.capitalize()}"
    }

    @CompileDynamic // Groovy don't seem to let me register the factory with static compilation
    static void registerGlmPublicationFactory(PublicationContainer publications, NamedDomainObjectFactory<GlmPublication> factory) {
        publications.registerFactory(defaultClass(GlmPublication), factory);
    }

    static NamedDomainObjectFactory<GlmPublication> newGlmPublicationFactory(ObjectFactory objects) {
        return { name ->
            return objects.newInstance(defaultClass(GlmPublication), name)
        } as NamedDomainObjectFactory<GlmPublication>
    }

    private static <T> Class<T> defaultClass(Class<T> clazz) {
        return Class.forName(clazz.getSimpleName()) as Class<T>
    }
}
