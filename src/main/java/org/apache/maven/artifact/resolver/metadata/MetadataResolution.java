package org.apache.maven.artifact.resolver.metadata;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;

import java.util.Set;

/** @author Jason van Zyl */
public class MetadataResolution
{
    private Set<ArtifactMetadata> dependencies;

    private Set<ArtifactRepository> metadataRepositories;

    public MetadataResolution( Set<ArtifactMetadata> dependencies,
                               Set<ArtifactRepository> metadataRepositories )
    {
        this.dependencies = dependencies;

        this.metadataRepositories = metadataRepositories;
    }

    public Set<ArtifactMetadata> getDependencies()
    {
        return dependencies;
    }
}
