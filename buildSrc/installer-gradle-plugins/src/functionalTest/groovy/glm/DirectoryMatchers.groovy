package glm

import org.gradle.api.file.FileVisitDetails
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher

import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.io.FileMatchers.anExistingDirectory

final class DirectoryMatchers {
    private DirectoryMatchers() {}

    static Matcher<File> hasDescendants(String... descendants) {
        return allOf(anExistingDirectory(), new FeatureMatcher<File, Set<String>>(containsInAnyOrder(descendants), "", "") {
            @Override
            protected Set<String> featureValueOf(File actual) {
                return walkFileTree(actual)
            }
        })
    }

    private static Set<String> walkFileTree(File dir) {
        def result = [] as Set
        ProjectBuilder.builder().build().fileTree(dir).visit { FileVisitDetails details ->
            if (!details.isDirectory()) {
                result.add(details.relativePath.getPathString())
            }
        }
        return result
    }
}
