package org.apache.maven.artifact.resolver.metadata;

import org.apache.maven.artifact.ArtifactScopeEnum;

/** @author Oleg Gusakov */
public class MetadataGraphEdge
{
    String version;
    ArtifactScopeEnum scope;
    int depth = -1;

    public MetadataGraphEdge( String version,
                              ArtifactScopeEnum scope,
                              int depth )
    {
        super();
        this.version = version;
        this.scope = scope;
        this.depth = depth;
    }

    //----------------------------------------------------------------------------
    private static boolean objectsEqual( Object o1,
                                         Object o2 )
    {
        if ( o1 == null && o2 == null )
            return true;
        if ( ( o1 == null && o2 != null )
            || ( o1 != null && o2 == null )
            ) return false;
        return o1.equals( o2 );
    }

    //----------------------------------------------------------------------------
    @Override
    public boolean equals( Object o )
    {
        if ( o instanceof MetadataGraphEdge )
        {
            MetadataGraphEdge e = (MetadataGraphEdge) o;
            return
                objectsEqual( version, e.version )
                    && objectsEqual( scope, e.scope )
                    && depth == e.depth
                ;
        }
        return false;
    }

    //----------------------------------------------------------------------------
    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public ArtifactScopeEnum getScope()
    {
        return scope;
    }

    public void setScope( ArtifactScopeEnum scope )
    {
        this.scope = scope;
    }

    public int getDepth()
    {
        return depth;
    }

    public void setDepth( int depth )
    {
        this.depth = depth;
    }
    //----------------------------------------------------------------------------
    //----------------------------------------------------------------------------
}
