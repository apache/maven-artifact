package org.apache.maven.artifact.transform;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.maven.artifact.ArtifactScopeEnum;
import org.apache.maven.artifact.resolver.metadata.ArtifactMetadata;
import org.apache.maven.artifact.resolver.metadata.MetadataGraph;
import org.apache.maven.artifact.resolver.metadata.MetadataGraphEdge;
import org.apache.maven.artifact.resolver.metadata.MetadataGraphVertex;
import org.codehaus.plexus.PlexusTestCase;

/**
 *
 * @author <a href="mailto:oleg@codehaus.org">Oleg Gusakov</a>
 * 
 * @version $Id$
 */

public class DefaultClasspathTransformationTest
extends PlexusTestCase
{
	ClasspathTransformation transform;

	MetadataGraph graph;

	MetadataGraphEdge e1;
	MetadataGraphEdge e2;
	MetadataGraphEdge e3;

	MetadataGraphVertex v1;
	MetadataGraphVertex v2;
	MetadataGraphVertex v3;
	MetadataGraphVertex v4;
    //------------------------------------------------------------------------------------------
    @Override
	protected void setUp() throws Exception
	{
		super.setUp();
		transform = (ClasspathTransformation) lookup( ClasspathTransformation.ROLE, "default" );
    	
		e1 = new MetadataGraphEdge( "1.1", true, null, null, 2, 1 );
    	e2 = new MetadataGraphEdge( "1.2", true, null, null, 3, 2 );
    	e3 = new MetadataGraphEdge( "1.2", true, null, null, 2, 3 );
    	
    	v1 = new MetadataGraphVertex(new ArtifactMetadata("g","a1","1.0"));
    	v2 = new MetadataGraphVertex(new ArtifactMetadata("g","a2","1.0"));
    	v3 = new MetadataGraphVertex(new ArtifactMetadata("g","a3","1.0"));
    	v4 = new MetadataGraphVertex(new ArtifactMetadata("g","a4","1.0"));
    	
    	graph = new MetadataGraph( 4, 3 );
    	graph.setEntry(v1);
    	Map< String, MetadataGraphVertex> v = graph.getVertices();
    	Map<String,List<MetadataGraphEdge>> e = graph.getEdges();
    	
    	v.put(v1.getMd().toDomainString(), v1);
    	v.put(v2.getMd().toDomainString(), v2);
    	v.put(v3.getMd().toDomainString(), v3);
    	v.put(v4.getMd().toDomainString(), v4);
    	/*
    	 *       v2
    	 *   v1<
    	 *      v3-v4
    	 * 
    	 */
    	String key;
    	List<MetadataGraphEdge> le;
    	
    	// v1-->v2
    	key = MetadataGraph.edgeHash(v1, v2);
    	le = new ArrayList<MetadataGraphEdge>(2);
    	le.add( new MetadataGraphEdge( "1.1", true, null, null, 2, 1 ) );
    	le.add( new MetadataGraphEdge( "1.2", true, null, null, 2, 2 ) );
    	e.put( key, le );
    	
    	// v1-->v3
    	key = MetadataGraph.edgeHash(v1, v3);
    	le = new ArrayList<MetadataGraphEdge>(2);
    	le.add( new MetadataGraphEdge( "1.1", true, null, null, 2, 1 ) );
    	le.add( new MetadataGraphEdge( "1.2", true, null, null, 4, 2 ) );
    	e.put( key, le );
    	
    	// v3-->v4
    	key = MetadataGraph.edgeHash(v3, v4);
    	le = new ArrayList<MetadataGraphEdge>(2);
//    	le.add( new MetadataGraphEdge( "1.1", true, ArtifactScopeEnum.runtime, null, 2, 1 ) );
    	le.add( new MetadataGraphEdge( "1.2", true, ArtifactScopeEnum.test, null, 2, 2 ) );
    	e.put( key, le );
	}
    //------------------------------------------------------------------------------------------
    public void testCompileClasspathTransform()
    throws Exception
    {
    	ClasspathContainer res;
    	
    	res = transform.transform( graph, ArtifactScopeEnum.compile, false );

       	assertNotNull("null classpath container after compile transform", res );
       	assertNotNull("null classpath after compile transform", res.getClasspath() );
       	assertEquals("compile classpath should have 4 entries", 4, res.getClasspath().size() );
    }
    //------------------------------------------------------------------------------------------
    public void testProvidedClasspathTransform()
    throws Exception
    {
    	ClasspathContainer res;
    	
    	res = transform.transform( graph, ArtifactScopeEnum.runtime, false );

       	assertNotNull("null classpath container after runtime transform", res );
       	assertNotNull("null classpath after runtime transform", res.getClasspath() );
       	assertEquals("runtime classpath should have 3 entries", 3, res.getClasspath().size() );
    }
    //------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------
}
