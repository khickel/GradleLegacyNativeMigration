package dev.nokeesamples.sonarqube;

import lombok.val;
import org.gradle.api.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.sonarqube.gradle.SonarQubeTask;

import javax.inject.Inject;
import java.util.Set;

import static dev.nokeesamples.sonarqube.SonarQubeBuildWrapperUtils.captureIfPresentInTaskGraph;
import static dev.nokeesamples.sonarqube.SonarQubeBuildWrapperUtils.getDefaultBuildWrapperFileName;

public abstract class SonarQubePlugin implements Plugin<Project> {
    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract TaskContainer getTasks();

    @Inject
    protected abstract ProjectLayout getLayout();

    @Inject
    protected abstract ConfigurationContainer getConfigurations();

    @Inject
    protected abstract DependencyHandler getDependencies();

    @Override
    public void apply(Project project) {
        val generateTask = createGenerateTask();
        getAllNativeCompileTasks().configureEach(task -> task.dependsOn(captureIfPresentInTaskGraph(task)));
        createSonarQubeElements(generateTask);

        if (isRootProject(project)) {
            val sonarqube = createSonarQube(project.getAllprojects());
            val mergeTask = createMergeTask(sonarqube);
            getTasks().register("sonarqube", SonarQubeTask.class, task -> {
                task.dependsOn(mergeTask);
                task.setGroup("verification");
                task.setDescription("Analyze using sonarqube");

                task.getProperties()
                        .put("sonar.cfamily.build-wrapper-output", mergeTask.flatMap(MergeSonarQubeBuildWrapperTask::getBuildWrapperFile).get().getAsFile().getParent());
            });
        }
    }

    private boolean isRootProject(Project project) {
        return project.getParent() == null;
    }

    private TaskCollection<AbstractNativeCompileTask> getAllNativeCompileTasks() {
        return getTasks().withType(AbstractNativeCompileTask.class);
    }

    private Provider<Directory> temporaryDirectory(Task task) {
        return getLayout().getBuildDirectory().dir("tmp/" + task.getName());
    }

    private TaskProvider<GenerateSonarQubeBuildWrapperTask> createGenerateTask() {
        return getTasks().register("generateSonarqube", GenerateSonarQubeBuildWrapperTask.class, task -> {
            task.getBuildWrapperFile().value(temporaryDirectory(task).map(it -> it.file(getDefaultBuildWrapperFileName())));
            task.getCaptures().finalizeValueOnRead();
            task.mustRunAfter(getAllNativeCompileTasks());
        });
    }

    private void createSonarQubeElements(TaskProvider<GenerateSonarQubeBuildWrapperTask> generateTask) {
        getConfigurations().create("sonarqubeElements", configuration -> {
            configuration.setCanBeConsumed(true);
            configuration.setCanBeResolved(false);
            configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, "sonarqube"));
            configuration.getOutgoing().artifact(generateTask.flatMap(GenerateSonarQubeBuildWrapperTask::getBuildWrapperFile));
        });
    }

    private TaskProvider<MergeSonarQubeBuildWrapperTask> createMergeTask(Configuration sonarqube) {
        return getTasks().register("mergeSonarqube", MergeSonarQubeBuildWrapperTask.class, task -> {
            task.getSources().from(sonarqube.getIncoming().artifactView(it -> it.setLenient(true)).getFiles());
            task.getBuildWrapperFile().value(temporaryDirectory(task).map(it -> it.file(getDefaultBuildWrapperFileName())));
        });
    }

    private Configuration createSonarQube(Set<Project> allProjects) {
        return getConfigurations().create("sonarqube", configuration -> {
            configuration.setCanBeConsumed(false);
            configuration.setCanBeResolved(true);
            configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, "sonarqube"));
            allProjects.stream().map(getDependencies()::create).forEach(configuration.getDependencies()::add);
        });
    }
}
