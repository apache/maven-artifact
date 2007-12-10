package org.apache.maven.artifact.repository.metadata;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.testutils.MockManager;
import org.apache.maven.artifact.testutils.TestFileManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.easymock.MockControl;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

public class MetadataTouchfileTest
    extends TestCase
{

    private MockManager mockManager = new MockManager();

    private TestFileManager testFileManager = new TestFileManager( "MetadataTouchfile.test.", "" );

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Override
    public void tearDown()
        throws Exception
    {
        testFileManager.cleanUp();

        super.tearDown();
    }

    public void testStraightThrough()
        throws InterruptedException
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

        MockControl metadataCtl = MockControl.createControl( RepositoryMetadata.class );
        mockManager.add( metadataCtl );

        RepositoryMetadata metadata = (RepositoryMetadata) metadataCtl.getMock();

        mockManager.replayAll();

        Logger logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "test" );

        MetadataTouchfile touchfile = new MetadataTouchfile( metadata, localRepo );

        String repoId = "codehaus.snasphots";
        String filename = "metadata.xml";

        touchfile.touch( repoId, filename, logger );

        // helps the lastUpdate interval be significantly different.
        Thread.sleep( 1000 );

        Date end = new Date();

        Date checkDate = touchfile.getLastCheckDate( repoId, filename, logger );

        assertNotNull( checkDate );
        assertTrue( checkDate.after( start ) );
        assertTrue( checkDate.before( end ) );

        mockManager.verifyAll();
    }

}
