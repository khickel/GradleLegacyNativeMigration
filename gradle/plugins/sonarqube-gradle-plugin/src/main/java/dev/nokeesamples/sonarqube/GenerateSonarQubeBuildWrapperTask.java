package dev.nokeesamples.sonarqube;

import com.google.gson.GsonBuilder;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static dev.nokeesamples.sonarqube.SonarQubeBuildWrapperUtils.getCurrentVersion;
import static java.nio.file.Files.write;

public abstract class GenerateSonarQubeBuildWrapperTask extends DefaultTask {
    @Nested
    public abstract ListProperty<SonarQubeBuildWrapper.Capture> getCaptures();

    @OutputFile
    public abstract RegularFileProperty getBuildWrapperFile();

    @TaskAction
    public void doGenerate() throws IOException {
        getBuildWrapperFile().getAsFile().get().createNewFile();
        val buildWrapper = new SonarQubeBuildWrapper(getCurrentVersion(), getCaptures().get());
        write(getBuildWrapperFile().getAsFile().get().toPath(),
                new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(buildWrapper).getBytes(StandardCharsets.UTF_8));
    }
}
