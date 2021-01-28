package glm

import dev.gradleplugins.runnerkit.BuildResult
import dev.gradleplugins.runnerkit.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static dev.gradleplugins.runnerkit.GradleExecutor.gradleTestKit

abstract class AbstractFunctionalTest extends Specification {
    private GradleRunner runner
    @TempDir
    Path testDirectory

    protected File getBuildFile() {
        return file('build.gradle')
    }

    protected File getSettingsFile() {
        return file('settings.gradle')
    }

    protected File file(String path) {
        def result = testDirectory.resolve(path)
        if (Files.notExists(result)) {
            Files.createDirectories(result.getParent())
            return Files.createFile(result).toFile()
        }
        return result.toFile()
    }

    def setup() {
        runner = GradleRunner.create(gradleTestKit()).withPluginClasspath().ignoresMissingSettingsFile().inDirectory(testDirectory.toFile())
    }

    protected BuildResult succeeds(String... tasks) {
        return runner.withTasks(tasks).build()
    }

    protected BuildResult failure(String... tasks) {
        return runner.withTasks(tasks).build()
    }
}
