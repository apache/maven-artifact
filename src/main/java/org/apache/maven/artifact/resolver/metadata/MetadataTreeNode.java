package org.apache.maven.artifact.resolver.metadata;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactScopeEnum;

public class MetadataTreeNode
{
    ArtifactMetadata md; // this node

    MetadataTreeNode parent; // papa of cause
    MetadataTreeNode[] children; // of cause

    boolean resolved = false; // if current node was resolved

    //------------------------------------------------------------------------
    public MetadataTreeNode()
    {
    }

    //------------------------------------------------------------------------
    public MetadataTreeNode( ArtifactMetadata md,
                             MetadataTreeNode parent,
                             boolean resolved,
                             ArtifactScopeEnum scope )
    {
        if ( md != null )
            md.setScope( scope );

        this.md = md;
        this.parent = parent;
        this.resolved = resolved;
    }

    //------------------------------------------------------------------------
    public MetadataTreeNode( Artifact af,
                             MetadataTreeNode parent,
                             boolean resolved,
                             ArtifactScopeEnum scope )
    {
        this( new ArtifactMetadata( af ), parent, resolved, scope );
    }

    //------------------------------------------------------------------------
    public void addChildren( List<MetadataTreeNode> kidList )
    {
        if ( kidList == null || kidList.size() < 1 )
            return;

        children = new MetadataTreeNode[kidList.size()];
        int i = 0;
        for ( MetadataTreeNode n : kidList )
            children[i++] = n;
    }

    //------------------------------------------------------------------
    @Override
    public String toString()
    {
        return md == null ? "no metadata" : md.toString();
    }

    //------------------------------------------------------------------
    public String graphHash()
        throws MetadataResolutionException
    {
        if ( md == null )
            throw new MetadataResolutionException( "treenode without metadata, parent: "
                + ( parent == null ? "null" : parent.toString() )
            );

        return md.groupId + ":" + md.artifactId;
    }

    //------------------------------------------------------------------------
    public boolean hasChildren()
    {
        return children != null;
    }

    //------------------------------------------------------------------------
    public ArtifactMetadata getMd()
    {
        return md;
    }

    public void setMd( ArtifactMetadata md )
    {
        this.md = md;
    }

    public MetadataTreeNode getParent()
    {
        return parent;
    }

    public void setParent( MetadataTreeNode parent )
    {
        this.parent = parent;
    }

    public boolean isResolved()
    {
        return resolved;
    }

    public void setResolved( boolean resolved )
    {
        this.resolved = resolved;
    }

    public MetadataTreeNode[] getChildren()
    {
        return children;
    }

    public void setChildren( MetadataTreeNode[] children )
    {
        this.children = children;
    }

    //------------------------------------------------------------------------
    //------------------------------------------------------------------------

}
