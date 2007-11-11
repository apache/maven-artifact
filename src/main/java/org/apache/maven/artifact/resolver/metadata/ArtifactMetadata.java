package org.apache.maven.artifact.resolver.metadata;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactScopeEnum;

/** @author Oleg Gusakov */
public class ArtifactMetadata
{
    protected String groupId;
    protected String artifactId;
    protected String version;
    protected String type;
    protected ArtifactScopeEnum artifactScope;
    protected String classifier;
    protected String uri;

    protected boolean resolved = false;

    //------------------------------------------------------------------
    public ArtifactMetadata( String groupId,
                             String name,
                             String version )
    {
        this( groupId, name, version, null );
    }

    //------------------------------------------------------------------
    public ArtifactMetadata( String groupId,
                             String name,
                             String version,
                             String type )
    {
        this( groupId, name, version, type, null );
    }

    //------------------------------------------------------------------
    public ArtifactMetadata( String groupId,
                             String name,
                             String version,
                             String type,
                             ArtifactScopeEnum artifactScope )
    {
        this( groupId, name, version, type, artifactScope, null );
    }

    //------------------------------------------------------------------
    public ArtifactMetadata( String groupId,
                             String name,
                             String version,
                             String type,
                             ArtifactScopeEnum artifactScope,
                             String classifier )
    {
        this.groupId = groupId;
        this.artifactId = name;
        this.version = version;
        this.type = type;
        this.artifactScope = artifactScope;
        this.classifier = classifier;
    }

    //------------------------------------------------------------------
    public ArtifactMetadata( Artifact af )
    {
        /*
        if ( af != null )
        {
            init( af );
        }
        */
    }

    //------------------------------------------------------------------
    public void init( ArtifactMetadata af )
    {
        setGroupId( af.getGroupId() );
        setArtifactId( af.getArtifactId() );
        setVersion( af.getVersion() );
        setType( af.getType() );
        setScope( af.getScope() );
        setClassifier( af.getClassifier() );
        //setUri( af.getDownloadUrl() );

        this.resolved = af.isResolved();
    }

    //------------------------------------------------------------------
    @Override
    public String toString()
    {
        return groupId + ":" + artifactId + ":" + version;
    }

    //------------------------------------------------------------------
    public String toDomainString()
    {
        return groupId + ":" + artifactId;
    }

    //------------------------------------------------------------------
    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String name )
    {
        this.artifactId = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public ArtifactScopeEnum getArtifactScope()
    {
        return artifactScope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : artifactScope;
    }

    public void setArtifactScope( ArtifactScopeEnum artifactScope )
    {
        this.artifactScope = artifactScope;
    }

    public void setScope( String scope )
    {
        this.artifactScope = scope == null
            ? ArtifactScopeEnum.DEFAULT_SCOPE
            : ArtifactScopeEnum.valueOf( scope )
            ;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public boolean isResolved()
    {
        return resolved;
    }

    public void setResolved( boolean resolved )
    {
        this.resolved = resolved;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri( String uri )
    {
        this.uri = uri;
    }

    public String getScope()
    {
        if ( artifactScope == null )
        {
            return ArtifactScopeEnum.DEFAULT_SCOPE.getScope();
        }

        return artifactScope.getScope();
    }

    public String getDependencyConflictId()
    {
        return groupId + ":" + artifactId;
    }

    //------------------------------------------------------------------
    //------------------------------------------------------------------
}
