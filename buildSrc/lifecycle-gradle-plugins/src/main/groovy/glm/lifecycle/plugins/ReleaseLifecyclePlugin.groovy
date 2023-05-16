package glm.lifecycle.plugins

import glm.lifecycle.tasks.BuildTypeLifecycleTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class ReleaseLifecyclePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(BuildTypeLifecycleBasePlugin)
        project.tasks.register('release', BuildTypeLifecycleTask) { task ->
            task.group = 'Legacy Native Migration Example Release'
            task.description = 'Run all tasks to do a release build'
        }
    }
}
