package org.apache.maven.artifact.manager;

import java.io.File;

import org.apache.maven.artifact.AbstractArtifactComponentTestCase;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

public class DefaultUpdateCheckManagerTest
    extends AbstractArtifactComponentTestCase
{

    DefaultUpdateCheckManager updateCheckManager;

    @Override
    protected String component()
    {
        return "updateCheckManager";
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        updateCheckManager = new DefaultUpdateCheckManager( new ConsoleLogger( Logger.LEVEL_DEBUG, "test" ) );
    }

    public void testArtifact() throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();

        ArtifactRepository localRepository = localRepository();

        Artifact a = createArtifact( "a", "0.0.1-SNAPSHOT" );
        File file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        file.delete();
        a.setFile( file );

        File touchFile = new File ( file.getParent(), DefaultUpdateCheckManager.TOUCHFILE_NAME );
        touchFile.delete();

        assertTrue( updateCheckManager.isUpdateRequired( a, remoteRepository ) );

        file.getParentFile().mkdirs();
        file.createNewFile();
        updateCheckManager.touch( a, remoteRepository );

        assertFalse( updateCheckManager.isUpdateRequired( a, remoteRepository ) );

        assertNull( updateCheckManager.getLastModifiedFromTouchfile( file, remoteRepository.getId() ) );
    }

    public void testMissingArtifact()
        throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();

        ArtifactRepository localRepository = localRepository();

        Artifact a = createArtifact( "a", "0.0.1-SNAPSHOT" );
        File file = new File( localRepository.getBasedir(), 
                              localRepository.pathOf( a ) );
        file.delete();
        a.setFile( file );

        File touchFile = new File ( file.getParent(), DefaultUpdateCheckManager.TOUCHFILE_NAME );
        touchFile.delete();

        assertTrue( updateCheckManager.isUpdateRequired( a, remoteRepository ) );

        updateCheckManager.touch( a, remoteRepository );

        assertFalse( updateCheckManager.isUpdateRequired( a, remoteRepository ) );

        assertFalse( file.exists() );
        assertNotNull( updateCheckManager.getLastModifiedFromTouchfile( file, remoteRepository.getId() ) );
    }

    public void testMetadata() throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();

        ArtifactRepository localRepository = localRepository();

        Artifact a = createRemoteArtifact( "a", "0.0.1-SNAPSHOT" );
        RepositoryMetadata metadata = new ArtifactRepositoryMetadata( a );

        File file = new File( localRepository.getBasedir(),
                              localRepository.pathOfLocalRepositoryMetadata( metadata, localRepository ) );
        file.delete();

        File touchFile = new File ( file.getParent(), DefaultUpdateCheckManager.TOUCHFILE_NAME );
        touchFile.delete();

        assertTrue( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );

        file.getParentFile().mkdirs();
        file.createNewFile();
        updateCheckManager.touch( metadata, remoteRepository, file );

        assertFalse( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );

        assertNotNull( updateCheckManager.getLastModifiedFromTouchfile( file, remoteRepository.getId() ) );
    }

    public void testMissingMetadata() throws Exception
    {
        ArtifactRepository remoteRepository = remoteRepository();

        ArtifactRepository localRepository = localRepository();

        Artifact a = createRemoteArtifact( "a", "0.0.1-SNAPSHOT" );
        RepositoryMetadata metadata = new ArtifactRepositoryMetadata( a );

        File file = new File( localRepository.getBasedir(),
                              localRepository.pathOfLocalRepositoryMetadata( metadata, localRepository ) );
        file.delete();

        File touchFile = new File ( file.getParent(), DefaultUpdateCheckManager.TOUCHFILE_NAME );
        touchFile.delete();

        assertTrue( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );

        updateCheckManager.touch( metadata, remoteRepository, file );

        assertFalse( updateCheckManager.isUpdateRequired( metadata, remoteRepository, file ) );

        assertNotNull( updateCheckManager.getLastModifiedFromTouchfile( file, remoteRepository.getId() ) );
    }

}
