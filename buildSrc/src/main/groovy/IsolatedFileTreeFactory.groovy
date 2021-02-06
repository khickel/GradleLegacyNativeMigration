import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectCollectionSchema
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider

import java.security.MessageDigest

@CompileStatic
class IsolatedFileTreeFactory {
    private final Project project

    IsolatedFileTreeFactory(Project project) {
        this.project = project
    }

    FileTree create(Map<String, ?> args) {
        project.fileTree(args)

        // Creates a stable, unique task name
        MessageDigest digest = MessageDigest.getInstance("MD5")
        def keys = new ArrayList<>(args.keySet())
        Collections.sort(keys) // sort keys to ensure hash stability
        keys.each { key ->
            digest.update(key.bytes)
            // we assume all values are convertible to string...
            // ...and the conversion is stable between build runs
            digest.update(args.get(key).toString().bytes)
        }
        def taskName = "isolate${digest.digest().encodeHex()}FileTree"

        TaskProvider<Sync> syncTask = null
        if (hasTask(taskName)) {
            syncTask = project.tasks.named(taskName, Sync)
        } else {
            syncTask = project.tasks.register(taskName, Sync, { Sync task ->
                task.from(project.fileTree(args))
                task.destinationDir = project.layout.buildDirectory.dir("tmp/${task.name}").get().asFile
            } as Action<Sync>)
        }

        return project.objects.directoryProperty().fileProvider(syncTask.map { it.destinationDir }).asFileTree
    }

    private boolean hasTask(String name) {
        def iter = project.tasks.collectionSchema.elements.iterator()
        while (iter.hasNext()) {
            def schema = iter.next()
            if (schema.name == name) {
                return true
            }
        }
        return false
    }
}
