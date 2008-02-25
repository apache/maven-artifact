package org.apache.maven.artifact.resolver;

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

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.AbstractArtifactComponentTestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.FileUtils;

public class ArtifactUpdatePolicyTest
    extends AbstractArtifactComponentTestCase
{

    private static final long TWO_SECONDS = 2 * 1000L;
    
    private static final long TWO_DAYS = 2 * 86400L * 1000L;

    private ArtifactResolver artifactResolver;

    private List remoteRepositories;

    private WagonManager wagonManager;

    private TestTransferListener listener;

    private ArtifactRepository localRepository;

    private DefaultArtifactRepository remoteRepository;

    protected String component()
    {
        return "artifact-update-policy";
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        artifactResolver = (ArtifactResolver) lookup( ArtifactResolver.ROLE );

        remoteRepositories = remoteRepositories();
        remoteRepository = (DefaultArtifactRepository) remoteRepositories.get( 0 );
        remoteRepository.setProtocol( "testfile" );
        remoteRepository.getSnapshots().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE );
        remoteRepository.getReleases().setChecksumPolicy( ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE );
        FileUtils.deleteDirectory( new File( remoteRepository.getBasedir() ) );

        wagonManager = (WagonManager) lookup( WagonManager.ROLE );
        listener = new TestTransferListener();
        wagonManager.setDownloadMonitor( listener );

        localRepository = localRepository();
        FileUtils.deleteDirectory( new File( localRepository.getBasedir() ) );
    }

    protected void tearDown()
        throws Exception
    {
        wagonManager.setDownloadMonitor( null );
        wagonManager.setOnline( true );
        super.tearDown();
    }


    private void assertTransfers( String[] expected )
    {
        StringBuffer expectedSB = new StringBuffer();
        for ( int i = 0; i < expected.length; i++ )
        {
            expectedSB.append( expected[ i ] ).append( "\n" );
        }

        List actual = listener.getTransfers();
        StringBuffer actualSB = new StringBuffer();
        for ( int i = 0; i < actual.size(); i++ )
        {
            actualSB.append( actual.get( i ) ).append( "\n" );
        }

        assertEquals( expectedSB.toString(), actualSB.toString() );
    }

    private void deleteFromLocalRepository( Artifact o )
    {
        File file = new File( localRepository.getBasedir(), localRepository.pathOf( o ) );
        file.delete();
    }


    private Artifact createLocalCopy( String artifactId, String version ) throws Exception
    {
        Artifact a = createArtifact( artifactId, version );

        createArtifact( a, localRepository );

        SnapshotArtifactRepositoryMetadata snapshotMetadata = new SnapshotArtifactRepositoryMetadata( a );
        Metadata metadata = new Metadata();
        Versioning versioning = new Versioning();
        Snapshot snapshot = new Snapshot();
        snapshot.setLocalCopy( true );
        versioning.setSnapshot( snapshot );
        metadata.setVersioning( versioning );
        snapshotMetadata.setMetadata( metadata );
        a.addMetadata( snapshotMetadata );
        
        return a;
    }

    public void testForceLocalDoesNotExist()
        throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );

        artifactResolver.resolveAlways( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { 
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "getTransfer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.sha1", 
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.md5" } );
    }

    public void testForceButNoNewUpdates() throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );
        createArtifact( a, localRepository );

        artifactResolver.resolveAlways( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { 
            "getIfNewer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar" } );
    }

    public void testForceNewUpdate() throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );
        createArtifact( a, localRepository );
        setLastModified( a, System.currentTimeMillis() - 2000L, localRepository );

        artifactResolver.resolveAlways( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { 
            "getIfNewer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "getTransfer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.sha1", 
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.md5" } );
    }

    public void testForceUpdateMissing() throws Exception
    {
        Artifact a = createArtifact( "o", "0.0.1-SNAPSHOT" );

        try
        {
            artifactResolver.resolveAlways( a, remoteRepositories, localRepository );
            fail( "Expected missing artifact" );
        }
        catch ( ArtifactNotFoundException expected )
        {
            // expected
        }

        try
        {
            artifactResolver.resolveAlways( a, remoteRepositories, localRepository );
            fail( "Expected missing artifact" );
        }
        catch ( ArtifactNotFoundException expected )
        {
            // expected
        }

        assertTransfers( new String[] { 
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar" } );
        
    }

    public void testSnapshotUpdate() throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );
        createArtifact( a, localRepository );
        setLastModified( a, System.currentTimeMillis() - TWO_DAYS, localRepository );

        artifactResolver.resolve( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { 
            "getIfNewer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "getTransfer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.sha1", 
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.md5" } );
    }

    public void testSnapshotNoUpdates() throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );
        createArtifact( a, localRepository );
        long timestamp = System.currentTimeMillis() - TWO_DAYS;
        setLastModified( a, timestamp, localRepository );
        setLastModified( a, timestamp, remoteRepository );

        artifactResolver.resolve( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { 
            "getIfNewer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar" } );
    }

    public void testSnapshotPolicyCheck() throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );
        createArtifact( a, localRepository );
        long timestamp = System.currentTimeMillis() - TWO_SECONDS;
        setLastModified( a, timestamp, localRepository );
        setLastModified( a, timestamp, remoteRepository );

        artifactResolver.resolve( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { } );
    }

    public void testLocalCopy() throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );
        a = createLocalCopy( a.getArtifactId(), a.getVersion() );
        long timestamp = System.currentTimeMillis() - TWO_DAYS;
        setLastModified( a, timestamp, localRepository );

        artifactResolver.resolve( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { } );
    }

    public void testForceUpdateLocalCopy() throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "0.0.1-SNAPSHOT" );
        a = createLocalCopy( a.getArtifactId(), a.getVersion() );
        long timestamp = System.currentTimeMillis() - TWO_SECONDS;
        setLastModified( a, timestamp, localRepository );

        artifactResolver.resolveAlways( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { 
            "getIfNewer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "getTransfer org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar",
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.sha1", 
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar.md5" } );
    }

    private void setLastModified( Artifact a, long timestamp, ArtifactRepository repository )
    {
        File file = new File ( repository.getBasedir(),
                               repository.pathOf( a ) );
        file.setLastModified( timestamp );
    }

    public void testSnapshotUpdatePolicyForMissingArtifacts()
        throws Exception
    {
        Artifact j = createArtifact( "o", "0.0.1-SNAPSHOT" );

        try
        {
            artifactResolver.resolve( j, remoteRepositories, localRepository );
            fail( "Expected missing artifact" );
        }
        catch ( ArtifactNotFoundException expected )
        {
            // expected
        }

        try
        {
            artifactResolver.resolve( j, remoteRepositories, localRepository );
            fail( "Expected missing artifact" );
        }
        catch ( ArtifactNotFoundException expected )
        {
            // expected
        }

        assertTransfers( new String[] { 
            "get org.apache.maven/jars/o-0.0.1-SNAPSHOT.jar" } );
    }

    public void testResolutionOfArtifactsDeletedFromLocalRepo()
        throws Exception
    {
        Artifact j = createRemoteArtifact( "j", "0.0.1-SNAPSHOT" );

        artifactResolver.resolve( j, remoteRepositories, localRepository() );

        // sanity check
        assertTrue( j.isResolved() );
        assertTrue( j.getFile().canRead() );

        j.getFile().delete();

        j = createArtifact( j.getArtifactId(), j.getVersion() );
        artifactResolver.resolve( j, remoteRepositories, localRepository() );

        assertTrue( j.isResolved() );
        assertTrue( j.getFile().canRead() );
    }

    public void testReleaseUpdate()
        throws Exception
    {
        Artifact a = createRemoteArtifact( "o", "1.0.1" );
        createArtifact( a, localRepository );
        setLastModified( a, System.currentTimeMillis() - TWO_DAYS, localRepository );

        artifactResolver.resolve( a, remoteRepositories, localRepository );

        assertTransfers( new String[] { 
            "getIfNewer org.apache.maven/jars/o-1.0.1.jar",
            "getTransfer org.apache.maven/jars/o-1.0.1.jar",
            "get org.apache.maven/jars/o-1.0.1.jar.sha1", 
            "get org.apache.maven/jars/o-1.0.1.jar.md5" } );
    }

    public void testResolveExistingLocalArtifactInOfflineMode()
        throws Exception
    {
        Artifact a = createLocalArtifact( "a", "1.0.0" );

        wagonManager.setOnline( false );

        artifactResolver.resolve( a, remoteRepositories(), localRepository() );

        assertTrue( a.isResolved() );
    }

}
