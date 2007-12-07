package org.apache.maven.artifact.repository.metadata;

import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.testutils.MockManager;
import org.apache.maven.artifact.testutils.TestFileManager;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.easymock.MockControl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class DefaultRepositoryMetadataManagerTest
    extends TestCase
{

    private MockManager mockManager = new MockManager();

    private TestFileManager testFileManager = new TestFileManager(
                                                                   "DefaultRepositoryMetadataManager.test.",
                                                                   "" );

    private MockControl wagonManagerCtl;

    private WagonManager wagonManager;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        wagonManagerCtl = MockControl.createControl( WagonManager.class );
        mockManager.add( wagonManagerCtl );

        wagonManager = (WagonManager) wagonManagerCtl.getMock();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        testFileManager.cleanUp();

        super.tearDown();
    }

    public void testResolveAlways_StubMetadataOnResourceNotFoundException()
        throws RepositoryMetadataResolutionException, IOException, XmlPullParserException,
        ParseException, InterruptedException
    {
        Date start = new Date();

        // helps the lastUpdate interval be significantly different.
        Thread.sleep( 1000 );

        MockControl localRepoCtl = MockControl.createControl( ArtifactRepository.class );

        mockManager.add( localRepoCtl );

        ArtifactRepository localRepo = (ArtifactRepository) localRepoCtl.getMock();

        File dir = testFileManager.createTempDir();
        String path = "path/to/metadata.xml";

        localRepo.getBasedir();
        localRepoCtl.setReturnValue( dir.getAbsolutePath(), MockControl.ZERO_OR_MORE );

        localRepo.pathOfLocalRepositoryMetadata( null, null );
        localRepoCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        localRepoCtl.setReturnValue( path, MockControl.ZERO_OR_MORE );

        localRepo.getId();
        localRepoCtl.setReturnValue( "local", MockControl.ZERO_OR_MORE );

        wagonManager.isOnline();
        wagonManagerCtl.setReturnValue( true, MockControl.ZERO_OR_MORE );

        try
        {
            wagonManager.getArtifactMetadata( null, null, null, null );
            wagonManagerCtl.setMatcher( MockControl.ALWAYS_MATCHER );
            wagonManagerCtl.setThrowable( new ResourceDoesNotExistException( "Test error" ) );
        }
        catch ( TransferFailedException e )
        {
            fail( "Should not happen during mock setup." );
        }
        catch ( ResourceDoesNotExistException e )
        {
            fail( "Should not happen during mock setup." );
        }

        MockControl metadataCtl = MockControl.createControl( RepositoryMetadata.class );
        mockManager.add( metadataCtl );

        RepositoryMetadata metadata = (RepositoryMetadata) metadataCtl.getMock();

        String groupId = "group";
        String artifactId = "artifact";
        String baseVersion = "1-SNAPSHOT";

        metadata.getGroupId();
        metadataCtl.setReturnValue( groupId, MockControl.ZERO_OR_MORE );

        metadata.getArtifactId();
        metadataCtl.setReturnValue( artifactId, MockControl.ZERO_OR_MORE );

        metadata.getBaseVersion();
        metadataCtl.setReturnValue( baseVersion, MockControl.ZERO_OR_MORE );

        metadata.setMetadata( null );
        metadataCtl.setMatcher( MockControl.ALWAYS_MATCHER );
        metadataCtl.setVoidCallable( 1 );

        mockManager.replayAll();

        Logger logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "test" );

        new DefaultRepositoryMetadataManager( wagonManager, logger ).resolveAlways( metadata,
                                                                            localRepo,
                                                                            localRepo );

        // helps the lastUpdate interval be significantly different.
        Thread.sleep( 1000 );

        Date end = new Date();

        FileReader reader = null;
        try
        {
            reader = new FileReader( new File( dir, path ) );

            Metadata md = new MetadataXpp3Reader().read( reader );
            Versioning versioning = md.getVersioning();
            assertNotNull( versioning );
            assertTrue( ( versioning.getVersions() == null ) || versioning.getVersions().isEmpty() );
            assertNull( versioning.getSnapshot() );
            assertNull( versioning.getLatest() );
            assertNull( versioning.getRelease() );

            String lastUpdate = versioning.getLastUpdated();
            assertNotNull( lastUpdate );

            TimeZone timezone = TimeZone.getTimeZone( "UTC" );
            SimpleDateFormat fmt = new SimpleDateFormat( "yyyyMMddHHmmss" );
            fmt.setTimeZone( timezone );

            Date lastUpd = fmt.parse( lastUpdate );

            SimpleDateFormat debugFmt = new SimpleDateFormat( "HH:mm:ss.SSS" );

            System.out.println( "Start time of test: " + debugFmt.format( start ) );
            System.out.println( "last update is set to: " + debugFmt.format( lastUpd ) );
            System.out.println( "End time of test: " + debugFmt.format( end ) );

            assertFalse( lastUpd.before( start ) );
            assertFalse( lastUpd.after( end ) );
        }
        finally
        {
            IOUtil.close( reader );
        }

        mockManager.verifyAll();
    }

}
