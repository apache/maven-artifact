package org.apache.maven.artifact.deployer;

import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.resolver.metadata.Artifact;
import org.apache.maven.artifact.resolver.metadata.ArtifactRepository;
import org.apache.maven.artifact.resolver.metadata.version.DefaultArtifactVersion;

import java.util.ArrayList;
import java.util.List;

/** @author Jason van Zyl */
public class SimpleArtifactMetadataSource
    implements ArtifactMetadataSource
{
    public ResolutionGroup retrieve( Artifact artifact,
                                     ArtifactRepository localRepository,
                                     List remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        ResolutionGroup rg = new ResolutionGroup( null, null, null );

        return rg;
    }

    public List retrieveAvailableVersions( Artifact artifact,
                                           ArtifactRepository localRepository,
                                           List remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        List versions = new ArrayList();

        versions.add( new DefaultArtifactVersion( "10.1.3" ) );

        return versions;
    }
}
