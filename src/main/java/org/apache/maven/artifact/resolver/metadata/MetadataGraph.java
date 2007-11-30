package org.apache.maven.artifact.resolver.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.ArtifactScopeEnum;

/**
 * maven dependency metadata graph
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 */

public class MetadataGraph
{
	public static char DEFAULT_DOMAIN_SEPARATOR = '>';
	/**
	 * the entry point we started building the graph from
	 */
    MetadataGraphVertex entry;
    
    Map<String, MetadataGraphVertex> vertices;
    Map<String, List<MetadataGraphEdge>> edges;
    
    /**
     *  null in dirty graph, actual scope for transformed graph
     */
    ArtifactScopeEnum scope;

    //------------------------------------------------------------------------
    /**
     * construct graph from a "dirty" tree
     */
    public MetadataGraph( int nVertices, int nEdges )
    throws MetadataResolutionException
    {
    	edges = new HashMap<String, List<MetadataGraphEdge>>( nEdges );
    	vertices = new HashMap<String, MetadataGraphVertex>( nVertices );
    }
    //------------------------------------------------------------------------
    /**
     * construct a single vertice
     */
    public MetadataGraph( MetadataGraphVertex entry )
    throws MetadataResolutionException
    {
    	if( entry == null || entry.getMd() == null )
    		throw new MetadataResolutionException("cannot create a MetadataGraph out of empty vertice");
    	vertices = new HashMap<String, MetadataGraphVertex>( 1 );
    	vertices.put( entry.getMd().toDomainString(), entry );
    	
    	this.entry = entry;
    }
    //------------------------------------------------------------------------
    /**
     * construct graph from a "dirty" tree
     */
    public MetadataGraph( MetadataTreeNode tree )
    throws MetadataResolutionException
    {
        if ( tree == null )
        {
            throw new MetadataResolutionException( "tree is null" );
        }

        int count = countNodes( tree );
        vertices = new HashMap<String, MetadataGraphVertex>( count );
        edges = new HashMap<String, List<MetadataGraphEdge>>( count + ( count / 2 ) );

        processNodes( null, tree, 0, 0 );
    }

    //------------------------------------------------------------------------
    private void processNodes(   MetadataGraphVertex parentVertice
                               , MetadataTreeNode node
                               , int depth
                               , int pomOrder
    						)
    throws MetadataResolutionException
    {
        if ( node == null )
        {
            return;
        }

        String nodeHash = node.graphHash();
        MetadataGraphVertex vertice = vertices.get( nodeHash );
        if ( vertice == null )
        { // does not exist yet ?
            vertice = new MetadataGraphVertex( node.md );
            vertices.put( nodeHash, vertice );
        }

        if ( parentVertice != null )
        { // then create edges
            String edgeId = edgeHash( parentVertice, vertice );
            List<MetadataGraphEdge> edgeList = edges.get( edgeId );
            if ( edgeList == null )
            {
                edgeList = new ArrayList<MetadataGraphEdge>( 4 );
                edges.put( edgeId, edgeList );
            }

            ArtifactMetadata md = node.getMd();
            MetadataGraphEdge e = new MetadataGraphEdge( md.version, md.resolved, md.artifactScope, md.artifactUri, depth, pomOrder );
            if ( !edgeList.contains( e ) )
            {
            	e.setSource( parentVertice.getMd() );
            	e.setTarget( md );
                edgeList.add( e );
            }
            else
            {
                e = null;
            }
        }
        else
        {
        	entry = vertice;
        }

        MetadataTreeNode[] kids = node.getChildren();
        if ( kids == null || kids.length < 1 )
        {
            return;
        }

        for( int i = 0; i< kids.length; i++ )
        {
        	MetadataTreeNode n = kids[i];
            processNodes( vertice, n, depth + 1, i );
        }
    }

    //------------------------------------------------------------------------
    public static String edgeHash( MetadataGraphVertex v1,
                                   MetadataGraphVertex v2 )
    {
        return v1.md.toDomainString() + DEFAULT_DOMAIN_SEPARATOR + v2.md.toDomainString();
//		return h1.compareTo(h2) > 0
//				? h1.hashCode()+""+h2.hashCode()
//				: h2.hashCode()+""+h1.hashCode()
//		;
    }

    //------------------------------------------------------------------------
    public MetadataGraph addVertice( MetadataGraphVertex v )
    {
    	if( v == null || v.getMd() == null )
    		return this;
   
    	if( vertices == null )
    		vertices = new HashMap<String, MetadataGraphVertex>();
    	vertices.put(v.getMd().toDomainString(), v );
    	
    	return this;
    }
    //------------------------------------------------------------------------
    public MetadataGraph addEdge( String key, MetadataGraphEdge e )
    {
    	if( e == null )
    		return this;
   
    	if( edges == null )
    		edges = new HashMap<String, List<MetadataGraphEdge>>();
    	
    	List<MetadataGraphEdge> eList = edges.get(key);
    	if( eList == null ) {
    		eList = new ArrayList<MetadataGraphEdge>();
        	edges.put( key, eList );
    	}
    	
    	if( ! eList.contains(e) )
    		eList.add(e);
    	
    	return this;
    }
    //------------------------------------------------------------------------
    private static int countNodes( MetadataTreeNode tree )
    {
        if ( tree == null )
        {
            return 0;
        }

        int count = 1;
        MetadataTreeNode[] kids = tree.getChildren();
        if ( kids == null || kids.length < 1 )
        {
            return count;
        }
        for ( MetadataTreeNode n : kids )
        {
            count += countNodes( n );
        }

        return count;
    }

    //------------------------------------------------------------------------
    public MetadataGraphVertex getEntry()
    {
        return entry;
    }

    public void setEntry( MetadataGraphVertex entry )
    {
        this.entry = entry;
    }

    public Map<String, MetadataGraphVertex> getVertices()
    {
        return vertices;
    }

    public void setVertices( Map<String, MetadataGraphVertex> vertices )
    {
        this.vertices = vertices;
    }

    public Map<String, List<MetadataGraphEdge>> getEdges()
    {
        return edges;
    }

    public void setEdges( Map<String, List<MetadataGraphEdge>> edges )
    {
        this.edges = edges;
    }
	public ArtifactScopeEnum getScope()
	{
		return scope;
	}
	public void setScope(ArtifactScopeEnum scope)
	{
		this.scope = scope;
	}
    //------------------------------------------------------------------------
	public boolean isEmpty()
	{
		return
			entry == null
			|| vertices == null
			|| vertices.isEmpty()
		;
	}
    //------------------------------------------------------------------------
	public boolean isEmptyEdges()
	{
		return
			   isEmpty()
			|| edges == null
			|| edges.isEmpty()
		;
	}
    //------------------------------------------------------------------------
    //------------------------------------------------------------------------
}
