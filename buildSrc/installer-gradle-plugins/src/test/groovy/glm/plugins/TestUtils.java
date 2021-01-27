package glm.plugins;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;

public class TestUtils {
    public static Project rootProject() {
        return ProjectBuilder.builder().build();
    }
}
