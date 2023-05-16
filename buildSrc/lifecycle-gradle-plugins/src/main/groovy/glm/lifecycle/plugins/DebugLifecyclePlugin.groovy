package glm.lifecycle.plugins

import glm.lifecycle.tasks.BuildTypeLifecycleTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class DebugLifecyclePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.pluginManager.apply(BuildTypeLifecycleBasePlugin)
        project.tasks.register('debug', BuildTypeLifecycleTask) { task ->
            task.group = 'Legacy Native Migration Example Debug'
            task.description = 'Run all tasks to do a debug build'
        }
    }
}
