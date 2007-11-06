package org.apache.maven.artifact.resolver.metadata;

import org.apache.maven.artifact.Artifact;

/** @author Oleg Gusakov */
public class MetadataResolutionResult
{
	MetadataTreeNode root;
	MetadataGraph graph;
	//----------------------------------------------------------------------------
	public MetadataResolutionResult()
	{
	}
	//----------------------------------------------------------------------------
	public MetadataResolutionResult( MetadataTreeNode root )
	{
		this.root = root;
	}
	//----------------------------------------------------------------------------
	public MetadataTreeNode getTree() {
		return root;
	}
	public void setTree(MetadataTreeNode root) {
		this.root = root;
	}
	public MetadataGraph getGraph()
	{
		return graph;
	}
	public void setGraph(MetadataGraph graph)
	{
		this.graph = graph;
	}
	
	//----------------------------------------------------------------------------
	//----------------------------------------------------------------------------
}
