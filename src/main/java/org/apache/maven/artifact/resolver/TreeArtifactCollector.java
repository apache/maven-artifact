package org.apache.maven.artifact.resolver;

import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;

public class TreeArtifactCollector
{
    public ArtifactResolutionResult collect( Set<Artifact> artifacts,
                                             Artifact originatingArtifact,
                                             ArtifactRepository localRepository,
                                             List<ArtifactRepository> remoteRepositories,
                                             ArtifactMetadataSource source )
    {
        return null;        
    }
}
