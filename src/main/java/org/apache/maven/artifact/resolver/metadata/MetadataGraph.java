package org.apache.maven.artifact.resolver.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author Oleg Gusakov */

public class MetadataGraph
{
	MetadataGraphVertice entry;
	Map< String, MetadataGraphVertice > vertices;
	Map< String, List<MetadataGraphEdge> > edges; 
	//------------------------------------------------------------------------
	public MetadataGraph( MetadataTreeNode tree )
	throws MetadataResolutionException
	{
		if( tree == null )
			throw new MetadataResolutionException("tree is null");

		int count = countNodes(tree);
		vertices = new HashMap<String, MetadataGraphVertice>( count );
		edges = new HashMap<String, List<MetadataGraphEdge>>( count + (count/2) );

		processNodes( null, tree, 0 );
	}
	//------------------------------------------------------------------------
	private void processNodes( MetadataGraphVertice parentVertice, MetadataTreeNode node, int depth )
	throws MetadataResolutionException
	{
		if( node == null )
			return;
		
		String nodeHash = node.graphHash();
		MetadataGraphVertice vertice = vertices.get(nodeHash);
		if( vertice == null ) { // does not exist yet ?
			vertice = new MetadataGraphVertice( node.md );
			vertices.put( nodeHash, vertice );
		}
		
		if( parentVertice != null ) { // then create links
			String edgeId = edgeHash( parentVertice, vertice );
			List<MetadataGraphEdge> edgeList = edges.get(edgeId);
			if( edgeList == null ) {
				edgeList = new ArrayList<MetadataGraphEdge>(4);
				edges.put( edgeId, edgeList );
			}
			
			MetadataGraphEdge e = new MetadataGraphEdge( node.md.version, node.md.scope, depth );
			if( !edgeList.contains(e) ) {
				edgeList.add(e);
			} else {
				e = null;
			}
		}

		MetadataTreeNode [] kids = node.getChildren(); 
		if( kids == null || kids.length < 1 )
			return;

		for( MetadataTreeNode n : kids )
			processNodes( vertice, n, depth+1 );
	}
	//------------------------------------------------------------------------
	public static String edgeHash( MetadataGraphVertice v1, MetadataGraphVertice v2 )
	{
		return v1.md.toDomainString()+">"+v2.md.toDomainString();
//		return h1.compareTo(h2) > 0
//				? h1.hashCode()+""+h2.hashCode()
//				: h2.hashCode()+""+h1.hashCode()
//		;
	}
	//------------------------------------------------------------------------
	private static int countNodes(MetadataTreeNode tree )
	{
		if( tree == null )
			return 0;
		
		int count = 1;
		MetadataTreeNode [] kids = tree.getChildren(); 
		if( kids == null || kids.length < 1 )
			return count;
		for( MetadataTreeNode n : kids )
			count += countNodes(n);
		
		return count;
	}
	//------------------------------------------------------------------------
	public MetadataGraphVertice getEntry()
	{
		return entry;
	}
	public void setEntry(MetadataGraphVertice entry)
	{
		this.entry = entry;
	}
	public Map<String, MetadataGraphVertice> getVertices()
	{
		return vertices;
	}
	public void setVertices(Map<String, MetadataGraphVertice> vertices)
	{
		this.vertices = vertices;
	}
	public Map<String, List<MetadataGraphEdge>> getEdges()
	{
		return edges;
	}
	public void setEdges(Map<String, List<MetadataGraphEdge>> edges)
	{
		this.edges = edges;
	}
	//------------------------------------------------------------------------
	//------------------------------------------------------------------------
}
