package glm.publishing

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.api.internal.component.SoftwareComponentInternal
import org.gradle.api.publish.PublicationArtifact
import org.gradle.api.publish.internal.PublicationArtifactSet
import org.gradle.api.publish.internal.PublicationInternal
import org.gradle.api.publish.internal.versionmapping.VersionMappingStrategyInternal
import org.gradle.internal.DisplayName

// Include internal dependency on Gradle publishing... waiting for Gradle team response on removing assumption on publications
@CompileStatic
abstract class AbstractPublication implements PublicationInternal<PublicationArtifact> {
    @Override
    ModuleVersionIdentifier getCoordinates() {
        throw new UnsupportedOperationException()
    }

    @Override
    ImmutableAttributes getAttributes() {
        throw new UnsupportedOperationException()
    }

    @Override
    void setAlias(boolean alias) {
        throw new UnsupportedOperationException()
    }

    @Override
    PublicationArtifactSet<PublicationArtifact> getPublishableArtifacts() {
        throw new UnsupportedOperationException()
    }

    @Override
    void allPublishableArtifacts(Action<? super PublicationArtifact> action) {
        throw new UnsupportedOperationException()
    }

    @Override
    void whenPublishableArtifactRemoved(Action<? super PublicationArtifact> action) {
        throw new UnsupportedOperationException()
    }

    @Override
    PublicationArtifact addDerivedArtifact(PublicationArtifact originalArtifact, PublicationInternal.DerivedArtifact file) {
        throw new UnsupportedOperationException()
    }

    @Override
    void removeDerivedArtifact(PublicationArtifact artifact) {
        throw new UnsupportedOperationException()
    }

    @Override
    PublicationInternal.PublishedFile getPublishedFile(PublishArtifact source) {
        throw new UnsupportedOperationException()
    }

    @Override
    VersionMappingStrategyInternal getVersionMappingStrategy() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean isPublishBuildId() {
        throw new UnsupportedOperationException()
    }

    @Override
    def <T> T getCoordinates(Class<T> type) {
        throw new UnsupportedOperationException()
    }

    @Override
    SoftwareComponentInternal getComponent() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean isAlias() {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean isLegacy() {
        throw new UnsupportedOperationException()
    }

    @Override
    DisplayName getDisplayName() {
        throw new UnsupportedOperationException()
    }
}
