package glm.prebuilt

import org.gradle.api.NamedDomainObjectContainer

interface PrebuiltLibraryManagement {
    NamedDomainObjectContainer<NativeLibraryComponent> getLibraries()
    NamedDomainObjectContainer<PrebuiltMavenRepository> getRepositories()
}