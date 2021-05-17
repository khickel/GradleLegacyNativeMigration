package glm.publishing

import com.google.common.collect.Iterables
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.repositories.FlatDirectoryArtifactRepository
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

@CompileStatic
abstract class PublishToGlmRepository extends DefaultTask {
    @Internal
    abstract Property<GlmPublication> getPublication()

    @Internal
    abstract Property<FlatDirectoryArtifactRepository> getRepository()

    @Internal
    abstract ConfigurableFileCollection getClasspath()

    PublishToGlmRepository() {
        dependsOn({ publication.get().artifacts })
    }

    @Inject
    protected abstract WorkerExecutor getWorker()

    @TaskAction
    private void doPublish() {
        def publishingDirectory = Iterables.getOnlyElement(repository.get().getDirs())
        publishingDirectory.mkdirs()
        def workQueue = getWorker().classLoaderIsolation { it.classpath.from(classpath) }
        publication.get().artifacts.each { File artifactFile ->
            workQueue.submit(GlmPublishAction) { GlmPublishAction.Parameter parameter ->
                parameter.artifactFile.fileValue(artifactFile)
                parameter.publishingDirectory.fileValue(publishingDirectory)
            }
        }
    }
}
