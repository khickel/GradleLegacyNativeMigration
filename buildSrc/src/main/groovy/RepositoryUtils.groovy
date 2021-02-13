import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository

@CompileStatic
final class RepositoryUtils {
    private RepositoryUtils() {}

    static Action<MavenArtifactRepository> systemOpenSsl(Project project) {
        return new Action<MavenArtifactRepository>() {
            @Override
            void execute(MavenArtifactRepository repo) {
                repo.url = new OpenSslLocator(project.layout.buildDirectory.dir('repository-cache'))
                repo.metadataSources {
                    it.gradleMetadata()
                }
            }
        }
    }
}
