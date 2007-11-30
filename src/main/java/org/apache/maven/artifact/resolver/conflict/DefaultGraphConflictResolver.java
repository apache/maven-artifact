package org.apache.maven.artifact.resolver.conflict;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.ArtifactScopeEnum;
import org.apache.maven.artifact.resolver.metadata.ArtifactMetadata;
import org.apache.maven.artifact.resolver.metadata.MetadataGraph;
import org.apache.maven.artifact.resolver.metadata.MetadataGraphEdge;
import org.apache.maven.artifact.resolver.metadata.MetadataGraphVertex;
import org.apache.maven.artifact.resolver.metadata.MetadataResolutionException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Default conflict resolver.Implements closer newer first policy 
 * by default, but could be configured via plexus 
 * 
 * @plexus.component
 * 
 * @author <a href="mailto:oleg@codehaus.org">Oleg Gusakov</a>
 * 
 * @version $Id$
 */

public class DefaultGraphConflictResolver
implements GraphConflictResolver
{
	/**
     * artifact, closer to the entry point, is selected
     * 
     * @plexus.requirement role="org.apache.maven.artifact.resolver.conflict.GraphConflictResolutionPolicy"
     */
	protected GraphConflictResolutionPolicy policy;
	//-------------------------------------------------------------------------------------
	private class EdgeHolder
	{
		String            key;
		MetadataGraphEdge edge;
		
		public EdgeHolder(String key, MetadataGraphEdge edge )
		{
			set( key, edge );
		}
		
		public EdgeHolder()
		{
		}
		
		public void set( String key, MetadataGraphEdge edge )
		{
			this.key = key;
			this.edge = edge;
		}
		
		public void set( EdgeHolder eh )
		{
			set( eh.key, eh.edge );
		}

		public boolean eq(EdgeHolder eh)
		{
			return 		key != null
					&& edge != null
					&& key.equals(eh.key)
					&& edge.equals(eh.edge)
			;
		}
		
	}
	//-------------------------------------------------------------------------------------
	public MetadataGraph resolveConflicts( MetadataGraph graph, ArtifactScopeEnum scope )
	throws GraphConflictResolutionException
	{
		if( policy == null )
			throw new GraphConflictResolutionException("no GraphConflictResolutionPolicy injected");
		
		if( graph == null )
			return null;

		final MetadataGraphVertex entry = graph.getEntry();
		if( entry == null )
			return null;

		final Map<String, List<MetadataGraphEdge>> edges = graph.getEdges();
		if( edges == null || edges.size() < 1 )
			return null;
		
		final Map<String, MetadataGraphVertex> vertices = graph.getVertices();
		if( vertices == null || vertices.size() < 1 )
			throw new GraphConflictResolutionException("graph with an entry, but not vertices do not exist");
		
		try {
			// edge case - single vertice graph
			if( vertices.size() == 1 )
					return new MetadataGraph( entry );
	
			final ArtifactScopeEnum requestedScope = ArtifactScopeEnum.checkScope(scope);
			
			MetadataGraph res = new MetadataGraph( vertices.size(), edges.size() );
			res.setEntry( graph.getEntry() );
			res.setScope(requestedScope);
	
			for( Map.Entry<String, MetadataGraphVertex> mv : vertices.entrySet() ) {
				final MetadataGraphVertex v = mv.getValue();
				final EdgeHolder eh = cleanEdges( v, edges, requestedScope );
				
				if( eh == null ) { // no edges - don't need this vertice any more
					if( entry.getMd().toDomainString().equals(v.getMd().toDomainString() ) ) {
						// currently processing the entry point - it should not have any dependents
						// so add it anyway
						entry.getMd().setWhy("This is a graph entry point. No links.");
						res.addVertice(v);
					}
				}
				else
				{
					// fill in domain md with actual version data
					ArtifactMetadata md = v.getMd();
					ArtifactMetadata newMd = new ArtifactMetadata(
							md.getGroupId()
							, md.getArtifactId()
							, eh.edge.getVersion()
							, md.getType()
							, md.getScopeAsEnum()
							, md.getClassifier()
							, eh.edge.getArtifactUri()
							, eh.edge.getSource() == null 
														? ""
														:  eh.edge.getSource().toString()
							, eh.edge.isResolved()
							, eh.edge.getTarget() == null ? null : eh.edge.getTarget().getError()
									);
					v.setMd(newMd);
					res.addVertice(v);
					res.addEdge( eh.key, eh.edge );
				}
			}

			return findLinkedSubgraph( res );
		} catch (MetadataResolutionException e) {
			throw new GraphConflictResolutionException(e);
		}
	}
	//-------------------------------------------------------------------------------------
	private final MetadataGraph findLinkedSubgraph( MetadataGraph g )
	{
		if( g.getVertices().size() == 1 )
			return g;
		
		List<String> visited = new ArrayList<String>( g.getVertices().size() );
		visit( g.getEntry().getMd().toDomainString(), visited, g.getEdges() );
		
		List<String> dropList = new ArrayList<String>( g.getVertices().size() );

		// collect drop list
		for( Map.Entry<String, MetadataGraphVertex> mv : g.getVertices().entrySet() )
		{
			if( !visited.contains(mv.getKey()) )
				dropList.add( mv.getKey() );
		}
		
		if( dropList.size() < 1 )
			return g;
		
		// now - drop vertices
		Map<String, MetadataGraphVertex> vertices = g.getVertices();
		for( String v : dropList ) 
		{
			vertices.remove(v);
		}
		
		// collect edgeDropList
		List<String> edgeDropList = new ArrayList<String>( g.getEdges().size() );
		for( Map.Entry<String, List<MetadataGraphEdge>> me : g.getEdges().entrySet() )
		{
			String eKey = me.getKey();
			int ind = eKey.indexOf( MetadataGraph.DEFAULT_DOMAIN_SEPARATOR );
			String v1 = eKey.substring(0,ind);
			String v2 = eKey.substring(ind+1);
			
			if( visited.contains(v1) && visited.contains(v2) )
				continue;
			
			edgeDropList.add(eKey);
		}
		
		if( edgeDropList.size() < 1 )
			return g;

		// now - drop edges
		Map<String, List<MetadataGraphEdge>> edges = g.getEdges();
		for( String v : edgeDropList ) 
		{
			edges.remove(v);
		}
		
		return g;
	}
	//-------------------------------------------------------------------------------------
	private final void visit( String from, List<String> visited, Map<String, List<MetadataGraphEdge>> edges )
	{
		if( visited.contains( from ) )
			return;
		
		visited.add(from);
		
		for( Map.Entry<String, List<MetadataGraphEdge>> e : edges.entrySet() ) 
		{
			if( e.getKey().startsWith(from) ) {
				String to = e.getKey().substring( e.getKey().indexOf(MetadataGraph.DEFAULT_DOMAIN_SEPARATOR)+1 );
				visit( to, visited, edges );
			}
		}
	}
	//-------------------------------------------------------------------------------------
	private final EdgeHolder cleanEdges( MetadataGraphVertex v
										, Map< String, List<MetadataGraphEdge> > edges
										, ArtifactScopeEnum scope
													)
	{
		List< EdgeHolder > dirtyEdges = new ArrayList< EdgeHolder >(32);
		String vKey = v.getMd().toDomainString();
		for( Map.Entry<String, List<MetadataGraphEdge>> el : edges.entrySet() ) {
			if( el.getKey().endsWith(vKey) ) {
				for( MetadataGraphEdge e : el.getValue() ) {
					dirtyEdges.add( new EdgeHolder(el.getKey(), e) );
				}
			}
		}
		
		if( dirtyEdges.size() < 1 )
			return null;
		
		EdgeHolder res = new EdgeHolder();
		
		if( dirtyEdges.size() == 1 ) {
			if( scope.encloses( dirtyEdges.get(0).edge.getScope()) )
				return dirtyEdges.get(0);
			
			return null;
		}
			
		for( EdgeHolder eh : dirtyEdges )
		{
			if( !scope.encloses(eh.edge.getScope()) )
				continue;
			
			if( res.key == null )
			{
				res.set( eh );
			}
			else 
			{
				MetadataGraphEdge winner = policy.apply( eh.edge, res.edge );
				if( ! res.edge.equals(winner) ) {
					res.set(eh);
				}
			}
		}

		if( res.key == null )
			return null;

		return res;
	}
	//-------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------
}
