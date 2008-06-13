package org.apache.maven.artifact.deployer;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.ArrayList;
import java.util.List;

/** @author Jason van Zyl */
public class SimpleArtifactMetadataSource
    implements ArtifactMetadataSource
{
    public ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository, List remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        return new ResolutionGroup( null, null, null );
    }

    public List<ArtifactVersion> retrieveAvailableVersions( Artifact artifact, ArtifactRepository localRepository,
                                                            List<ArtifactRepository> remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        List<ArtifactVersion> versions = new ArrayList<ArtifactVersion>();

        versions.add( new DefaultArtifactVersion( "10.1.3" ) );

        return versions;
    }
}
