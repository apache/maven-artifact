package org.apache.maven.artifact.transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.maven.artifact.ArtifactScopeEnum;
import org.apache.maven.artifact.resolver.conflict.GraphConflictResolutionException;
import org.apache.maven.artifact.resolver.conflict.GraphConflictResolver;
import org.apache.maven.artifact.resolver.metadata.ArtifactMetadata;
import org.apache.maven.artifact.resolver.metadata.MetadataGraph;
import org.apache.maven.artifact.resolver.metadata.MetadataGraphEdge;
import org.apache.maven.artifact.resolver.metadata.MetadataGraphVertex;

/**
 * default implementation of the metadata classpath transformer
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 * @plexus.component
 *
 */
public class DefaultClasspathTransformation
implements ClasspathTransformation
{
    /** @plexus.requirement */
    GraphConflictResolver conflictResolver;
    //----------------------------------------------------------------------------------------------------
	public ClasspathContainer transform(
						  MetadataGraph dirtyGraph
						, ArtifactScopeEnum scope
						, boolean resolve
										)
    throws MetadataGraphTransformationException
	{
		try {
			if(    dirtyGraph == null
				|| dirtyGraph.isEmpty()
			)
				return null;
			
			MetadataGraph cleanGraph = conflictResolver.resolveConflicts( dirtyGraph, scope );

			if(    cleanGraph == null
				|| cleanGraph.isEmpty() 
			)
				return null;
			
			ClasspathContainer cpc = new ClasspathContainer( scope );
			if( cleanGraph.getEdges() == null || cleanGraph.getEdges().isEmpty() ) {
				// single entry in the classpath, populated from itself
				ArtifactMetadata amd = cleanGraph.getEntry().getMd();
				cpc.add( amd );
			} else {
				ClasspathGraphVisitor v = new ClasspathGraphVisitor( cleanGraph, cpc );
				MetadataGraphVertex entry = cleanGraph.getEntry();
				ArtifactMetadata md = entry.getMd();
				// entry point
				v.visit( entry ); //, md.getVersion(), md.getArtifactUri() );
			}
			
			return cpc;
		} catch (GraphConflictResolutionException e) {
			throw new MetadataGraphTransformationException(e);
		}
	}
    //===================================================================================================
	/**
	 * helper class to store visitation data
	 */
    private class MetadataGraphEdgeWithNodes
    {
    	MetadataGraphEdge edge;
    	String source;
    	String target;

    	public MetadataGraphEdgeWithNodes(MetadataGraphEdge edge, String source, String target)
    	{
    		super();
    		this.edge = edge;
    		this.target = target;
    	}
    }
    //===================================================================================================
    /**
     * Helper class to traverse graph. Required to make the containing method thread-safe
     * and yet use class level data to lessen stack usage in recursion
     */
    private class ClasspathGraphVisitor
    {
    	Map<String, List<MetadataGraphEdge>> edges;
    	Map< String, MetadataGraphVertex> vertices;
    	ClasspathContainer cpc;
    	List<String> visited;
    	//-----------------------------------------------------------------------
    	protected ClasspathGraphVisitor( MetadataGraph cleanGraph, ClasspathContainer cpc )
    	{
    		this.cpc = cpc;
    		
    		edges = cleanGraph.getEdges();
    		vertices = cleanGraph.getVertices();
    		visited = new ArrayList<String>( cleanGraph.getVertices().size() );
    	}
    	//-----------------------------------------------------------------------
    	protected void visit( MetadataGraphVertex node ) //, String version, String artifactUri )
    	{
    		ArtifactMetadata md = node.getMd();
    		String nodeKey = md.toDomainString();
    		if( visited.contains(nodeKey) )
    			return;

    		cpc.add( md );
    		
    		TreeSet<MetadataGraphEdgeWithNodes> deps = new TreeSet<MetadataGraphEdgeWithNodes>(
    					new Comparator<MetadataGraphEdgeWithNodes>() 
    					{
    						public int compare( MetadataGraphEdgeWithNodes e1
    										  , MetadataGraphEdgeWithNodes e2
    										  )
    						{
    							if( e1.edge.getDepth() == e2.edge.getDepth() )
    								return e2.edge.getPomOrder() - e1.edge.getPomOrder();

    							return e2.edge.getDepth() - e1.edge.getDepth();
    						}
    					}
    				);

    		for( Map.Entry<String, List<MetadataGraphEdge>> el : edges.entrySet() ) {
    			if( el.getKey().startsWith(nodeKey) ) {
    				String edgeKey = el.getKey();
    				int ind = edgeKey.indexOf(MetadataGraph.DEFAULT_DOMAIN_SEPARATOR);
    				String source = edgeKey.substring( 0, ind );
    				String target = edgeKey.substring( ind+1 );
    				for( MetadataGraphEdge e : el.getValue() )
    					deps.add( new MetadataGraphEdgeWithNodes(e,source,target) );
    			}
    		}
    		
    		if( deps.size() > 0 )
    			for( MetadataGraphEdgeWithNodes e : deps ) {
    				MetadataGraphVertex targetNode = vertices.get(e.target);
    				
    				// explain where this link is from
    				ArtifactMetadata tmd = targetNode.getMd();
    				ArtifactMetadata smd = e.edge.getSource();
//    				if( tmd != null && smd != null )
//    					tmd.setWhy( "originated from "+smd.toString() );
    				
    				visit( targetNode );//, e.edge.getVersion(), e.edge.getArtifactUri() );
    			}
    	}
    	//-----------------------------------------------------------------------
    	//-----------------------------------------------------------------------
    }
    //----------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------
}



