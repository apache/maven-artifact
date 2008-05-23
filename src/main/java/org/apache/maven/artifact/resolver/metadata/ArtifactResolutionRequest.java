package org.apache.maven.artifact.resolver.metadata;

import org.apache.maven.artifact.resolver.metadata.conflict.GraphConflictResolver;
import org.apache.maven.artifact.resolver.metadata.resolver.MetadataSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A resolution request allows you to either use an existing MavenProject, or a coordinate (gid:aid:version)
 * to process a POMs dependencies.
 *
 * @author Jason van Zyl
 */
public class ArtifactResolutionRequest
{
    private Artifact artifact;

    private Set<Artifact> artifactDependencies;

    private String groupId;

    private String artifactId;

    private String version;

    private ArtifactRepository localRepository;

    private List<ArtifactRepository> remoteRepostories;

    // This should really be a component. Different impls can can be composed to account for different forms of metadata.
    private MetadataSource metadataSource;

    private Map managedVersionMap;

    private List<GraphConflictResolver> conflictResolvers;

    public Artifact getArtifact()
    {
        return artifact;
    }

    public ArtifactResolutionRequest setArtifact( Artifact artifact )
    {
        this.artifact = artifact;

        return this;
    }

    public boolean hasArtifact()
    {
        return artifact != null;
    }

    public ArtifactResolutionRequest setArtifactDependencies( Set<Artifact> artifactDependencies )
    {
        this.artifactDependencies = artifactDependencies;

        return this;
    }

    public Set<Artifact> getArtifactDependencies()
    {
        return artifactDependencies;
    }

    public String getGroupId()
    {
        if ( artifact != null )
        {
            return artifact.getGroupId();
        }

        return groupId;
    }

    public ArtifactResolutionRequest setGroupId( String groupId )
    {
        this.groupId = groupId;

        return this;
    }

    public String getArtifactId()
    {
        if ( artifact != null )
        {
            return artifact.getArtifactId();
        }

        return artifactId;
    }

    public ArtifactResolutionRequest setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;

        return this;
    }

    public String getVersion()
    {
        if ( artifact != null )
        {
            return artifact.getVersion();
        }

        return version;
    }

    public ArtifactResolutionRequest setVersion( String version )
    {
        this.version = version;

        return this;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public ArtifactResolutionRequest setLocalRepository( ArtifactRepository localRepository )
    {
        this.localRepository = localRepository;

        return this;
    }

    public List<ArtifactRepository> getRemoteRepostories()
    {
        return remoteRepostories;
    }

    public ArtifactResolutionRequest setRemoteRepostories( List<ArtifactRepository> remoteRepostories )
    {
        this.remoteRepostories = remoteRepostories;

        return this;
    }

    // ------------------------------------------------------------------------
    //
    // ------------------------------------------------------------------------

    public MetadataSource getMetadataSource()
    {
        return metadataSource;
    }

    public ArtifactResolutionRequest setMetadataSource( MetadataSource metadataSource )
    {
        this.metadataSource = metadataSource;

        return this;
    }

    public Map getManagedVersionMap()
    {
        return managedVersionMap;
    }

    public ArtifactResolutionRequest setManagedVersionMap( Map managedVersionMap )
    {
        this.managedVersionMap = managedVersionMap;

        return this;
    }

    public List<GraphConflictResolver> getConflictResolvers()
    {
        return conflictResolvers;
    }

    public ArtifactResolutionRequest setConflictResolvers( List<GraphConflictResolver> conflictResolvers )
    {
        this.conflictResolvers = conflictResolvers;

        return this;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer()
                .append(getGroupId())
                .append(":")
                .append(getArtifactId())
                .append(":")
                .append(getVersion());

        return sb.toString();
    }
}
