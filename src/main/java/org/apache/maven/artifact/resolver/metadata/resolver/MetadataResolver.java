package org.apache.maven.artifact.resolver.metadata.resolver;

import java.util.List;

import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.metadata.Artifact;
import org.apache.maven.artifact.resolver.metadata.ArtifactRepository;

/**
 * entry point into metadata resolution component
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 */
public interface MetadataResolver
{
    String ROLE = MetadataResolver.class.getName();

    /**
     * collect all dependency metadata into one "dirty" tree
     * 
     * @param request
     * @return
     * @throws MetadataResolutionException
     */
    MetadataResolutionResult resolveMetadata( MetadataResolutionRequest request )
        throws MetadataResolutionException;

    /**
     * resolve artifact List, given metadata List (order matters!)
     * 
     * @param mdCollection - collection of artifact metadata's
     * @param localRepository
     * @param remoteRepositories
     * @return collection of resolved artifacts
     * @throws ArtifactResolutionException
     */
    public List<Artifact> resolveArtifact( List<ArtifactMetadata> mdCollection, ArtifactRepository localRepository,
                                           List<ArtifactRepository> remoteRepositories )
        throws ArtifactResolutionException;
}
