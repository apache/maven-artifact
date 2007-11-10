package org.apache.maven.artifact.resolver.metadata;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactScopeEnum;

import sun.security.action.GetLongAction;

/** @author Oleg Gusakov */
public class ArtifactMetadata
{
    protected String groupId;
    protected String artifactId;
    protected String version;
    protected String type;
    protected ArtifactScopeEnum scope;
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
                             ArtifactScopeEnum scope )
    {
        this( groupId, name, version, type, scope, null );
    }

    //------------------------------------------------------------------
    public ArtifactMetadata( String groupId,
                             String name,
                             String version,
                             String type,
                             ArtifactScopeEnum scope,
                             String classifier )
    {
        this.groupId = groupId;
        this.artifactId = name;
        this.version = version;
        this.type = type;
        this.scope = scope;
        this.classifier = classifier;
    }

    //------------------------------------------------------------------
    public ArtifactMetadata( Artifact af )
    {
        if ( af != null )
        {
            init( af );
        }
    }

    //------------------------------------------------------------------
    public void init( Artifact af )
    {
        setGroupId( af.getGroupId() );
        setArtifactId( af.getArtifactId() );
        setVersion( af.getVersion() );
        setType( af.getType() );
        setScope( af.getScope() );
        setClassifier( af.getClassifier() );
        setUri( af.getDownloadUrl() );

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

    public ArtifactScopeEnum getScope()
    {
        return scope == null ? ArtifactScopeEnum.DEFAULT_SCOPE : scope;
    }

    public void setScope( ArtifactScopeEnum scope )
    {
        this.scope = scope;
    }

    public void setScope( String scope )
    {
        this.scope = scope == null
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

    //------------------------------------------------------------------
    //------------------------------------------------------------------
}
