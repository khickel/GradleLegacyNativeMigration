package glm.prebuilt

import groovy.transform.CompileStatic
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

@CompileStatic
final class GradleMetadataCacheBuilder {
    private final Map<String, GradleMetadataCache> metadataCaches = new HashMap<>();
    private final Set<NativeLibraryComponent> libraries
    private final Provider<Directory> cachingDirectory

    GradleMetadataCacheBuilder(Set<NativeLibraryComponent> libraries, Provider<Directory> cachingDirectory) {
        this.cachingDirectory = cachingDirectory
        this.libraries = libraries
    }

    GradleMetadataCache create(String groupId) {
        return metadataCaches.computeIfAbsent(groupId) {
            return new GradleMetadataCache(libraries.findAll { it.groupId.get() == groupId }, cachingDirectory)
        }
    }
}
