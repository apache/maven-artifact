package org.apache.maven.artifact.resolver;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A resolution request allows you to either use an existing MavenProject, or a coordinate (gid:aid:version)
 * to process a POMs dependencies.
 *
 * @author Jason van Zyl */
public class ArtifactResolutionRequest
{
    private Artifact artifact;

    private Set artifactDependencies;

    private String groupId;

    private String artifactId;

    private String version;

    private ArtifactRepository localRepository;

    private List remoteRepostories;

    private ArtifactFilter filter;

    private List listeners = new ArrayList();

    // This should really be a component. Different impls can can be composed to account for different forms of metadata.
    private ArtifactMetadataSource metadataSource;

    private Map managedVersionMap;

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

    public ArtifactResolutionRequest setArtifactDependencies( Set artifactDependencies )
    {
        this.artifactDependencies = artifactDependencies;

        return this;
    }

    public Set getArtifactDependencies()
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

    public List getRemoteRepostories()
    {
        return remoteRepostories;
    }

    public ArtifactResolutionRequest setRemoteRepostories( List remoteRepostories )
    {
        this.remoteRepostories = remoteRepostories;

        return this;
    }

    public ArtifactFilter getFilter()
    {
        return filter;
    }

    public ArtifactResolutionRequest setFilter( ArtifactFilter filter )
    {
        this.filter = filter;

        return this;
    }

    public List getListeners()
    {
        return listeners;
    }

    public ArtifactResolutionRequest addListener( ResolutionListener listener )
    {
        listeners.add( listener );

        return this;
    }

    // ------------------------------------------------------------------------
    //
    // ------------------------------------------------------------------------

    public ArtifactMetadataSource getMetadataSource()
    {
        return metadataSource;
    }

    public ArtifactResolutionRequest setMetadataSource( ArtifactMetadataSource metadataSource )
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

    public String toString()
    {
        StringBuffer sb = new StringBuffer()
            .append( "groupId = " + getGroupId() )
            .append( "artifactId = " + getArtifactId() )
            .append( "version = " + getVersion() );

        return sb.toString();
    }
}
