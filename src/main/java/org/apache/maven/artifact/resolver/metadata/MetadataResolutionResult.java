package org.apache.maven.artifact.resolver.metadata;

import org.apache.maven.artifact.ArtifactScopeEnum;
import org.apache.maven.artifact.resolver.conflict.GraphConflictResolutionException;
import org.apache.maven.artifact.resolver.conflict.GraphConflictResolver;
import org.apache.maven.artifact.transform.ClasspathContainer;
import org.apache.maven.artifact.transform.ClasspathTransformation;
import org.apache.maven.artifact.transform.MetadataGraphTransformationException;

/** 
 * This object is tinted with ClasspathTransformation and GraphConflictResolver. 
 * Get rid of them after debugging
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */
public class MetadataResolutionResult
{
    MetadataTreeNode treeRoot;
    ClasspathTransformation classpathTransformation;
    GraphConflictResolver conflictResolver;

    //----------------------------------------------------------------------------
    public MetadataResolutionResult()
    {
    }
    //----------------------------------------------------------------------------
    public MetadataResolutionResult( MetadataTreeNode root )
    {
        this.treeRoot = root;
    }
    //----------------------------------------------------------------------------
    public MetadataResolutionResult( GraphConflictResolver conflictResolver, ClasspathTransformation cpt )
    {
        this.classpathTransformation = cpt;
        this.conflictResolver = conflictResolver;
    }
    //----------------------------------------------------------------------------
    public MetadataTreeNode getTree()
    {
        return treeRoot;
    }

    public void setTree( MetadataTreeNode root )
    {
        this.treeRoot = root;
    }

    //----------------------------------------------------------------------------
    public MetadataGraph getGraph()
    throws MetadataResolutionException
    {
        return treeRoot == null ? null : new MetadataGraph(treeRoot);
    }
    //----------------------------------------------------------------------------
    public MetadataGraph getGraph( ArtifactScopeEnum scope )
    throws MetadataResolutionException, GraphConflictResolutionException
    {
    	if( treeRoot == null )
    		return null;
    	
    	if( conflictResolver == null )
    		return null;
    	
        return conflictResolver.resolveConflicts( getGraph(), scope );
    }
    //----------------------------------------------------------------------------
    public ClasspathContainer getClasspath( ArtifactScopeEnum scope )
    throws MetadataGraphTransformationException, MetadataResolutionException
    {
        if( classpathTransformation == null )
        	return null;
        
        MetadataGraph dirtyGraph = getGraph();
        if( dirtyGraph == null )
        	return null;
        
        ClasspathContainer cpc = classpathTransformation.transform( dirtyGraph, scope, false );
        
        return cpc;
    }

    public MetadataTreeNode getClasspathTree( ArtifactScopeEnum scope )
    throws MetadataGraphTransformationException, MetadataResolutionException
    {
        ClasspathContainer cpc = getClasspath(scope);
        if( cpc == null )
        	return null;
        
        return cpc.getClasspathAsTree();
    }
    //----------------------------------------------------------------------------
    //----------------------------------------------------------------------------
}
