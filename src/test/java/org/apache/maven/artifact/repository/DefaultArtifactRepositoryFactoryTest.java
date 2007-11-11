package org.apache.maven.artifact.repository;

import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.UnknownRepositoryLayoutException;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DefaultArtifactRepositoryFactoryTest
    extends PlexusTestCase
{
    private ArtifactRepositoryFactory repoFactory;

    private Set toDelete = new HashSet();

    public void setUp()
        throws Exception
    {
        super.setUp();

        repoFactory = (ArtifactRepositoryFactory) lookup( ArtifactRepositoryFactory.ROLE );
    }

    public void tearDown()
        throws Exception
    {
        for ( Iterator it = toDelete.iterator(); it.hasNext(); )
        {
            File f = (File) it.next();

            if ( f.exists() )
            {
                FileUtils.forceDelete( f );
            }
        }
    }

    private File createTempDir()
        throws IOException
    {
        File f = File.createTempFile( "DefaultArtifactRepositoryFactoryTest.", ".dir" );
        FileUtils.forceDelete( f );

        f.mkdirs();
        toDelete.add( f );

        return f;
    }

    public void test_createLocalRepository()
        throws IOException, InvalidRepositoryException
    {
        File dir = createTempDir();
        ArtifactRepository localRepo = repoFactory.createLocalRepository( dir );

        assertEquals( dir.getAbsolutePath(), localRepo.getBasedir() );
        assertEquals( ArtifactRepositoryFactory.LOCAL_REPOSITORY_ID, localRepo.getId() );
        assertTrue( localRepo.getLayout() instanceof DefaultRepositoryLayout );
    }

    public void test_getLayout_ReturnDefaultLayout()
        throws UnknownRepositoryLayoutException
    {
        ArtifactRepositoryLayout layout = repoFactory.getLayout( "default" );

        assertTrue( layout instanceof DefaultRepositoryLayout );
    }
}
