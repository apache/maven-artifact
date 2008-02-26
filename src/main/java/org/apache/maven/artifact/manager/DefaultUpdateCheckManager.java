package org.apache.maven.artifact.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;

/** @plexus.component */
public class DefaultUpdateCheckManager
    extends AbstractLogEnabled
    implements UpdateCheckManager
{

    public DefaultUpdateCheckManager()
    {

    }

    public DefaultUpdateCheckManager( Logger logger )
    {
        enableLogging( logger );
    }

    public static final String LAST_UPDATE_TAG = ".lastUpdated";

    public static final String TOUCHFILE_NAME = "resolver-status.properties";

    public boolean isUpdateRequired( Artifact artifact, ArtifactRepository repository )
    {
        ArtifactRepositoryPolicy policy = artifact.isSnapshot() ? repository.getSnapshots() : repository.getReleases();

        File file = artifact.getFile();

        if ( !policy.isEnabled() )
        {
            return false;
        }

        if ( file == null )
        {
            // TODO throw something instead?
            return true;
        }

        Date lastCheckDate;
        
        if ( file.exists() )
        {
            lastCheckDate = new Date ( file.lastModified() );
        }
        else
        {
            lastCheckDate = getLastModifiedFromTouchfile( file, repository.getId() );
        }

        return lastCheckDate == null || policy.checkOutOfDate( lastCheckDate );
    }

    public boolean isUpdateRequired( RepositoryMetadata metadata, ArtifactRepository repository, File file )
    {
        ArtifactRepositoryPolicy policy = metadata.isSnapshot() ? repository.getSnapshots() : repository.getReleases();

        if ( !policy.isEnabled() )
        {
            return false;
        }

        if ( file == null )
        {
            // TODO throw something instead?
            return true;
        }

        Date lastCheckDate = getLastModifiedFromTouchfile( file, repository.getId() );

        return lastCheckDate == null || policy.checkOutOfDate( lastCheckDate );
    }

    public void touch( Artifact artifact, ArtifactRepository repository )
    {
        File file = artifact.getFile();

        touch( file, repository, false );
    }

    public void touch( RepositoryMetadata metadata, ArtifactRepository repository, File file )
    {
        touch( file, repository, true );
    }

    private void touch( File file, ArtifactRepository repository, boolean forceTouchFile )
    {
        String name = file.getName();

        File touchfile = getTouchfile( file );

        synchronized ( touchfile.getAbsolutePath().intern() )
        {
            if ( !touchfile.getParentFile().exists() && !touchfile.getParentFile().mkdirs() )
            {
                getLogger().debug(
                                   "Failed to create directory: " + touchfile.getParent() +
                                       " for tracking artifact metadata resolution." );
                return;
            }

            FileChannel channel = null;
            FileLock lock = null;
            try
            {
                Properties props = new Properties();

                channel = new RandomAccessFile( touchfile, "rw" ).getChannel();
                lock = channel.lock( 0, channel.size(), false );

                if ( touchfile.canRead() )
                {
                    getLogger().debug( "Reading resolution-state from: " + touchfile );
                    ByteBuffer buffer = ByteBuffer.allocate( (int) channel.size() );

                    channel.read( buffer );
                    buffer.flip();

                    ByteArrayInputStream stream = new ByteArrayInputStream( buffer.array() );
                    props.load( stream );
                }

                String key = getKey( name, repository.getId() );

                boolean modified = false;

                if ( !forceTouchFile && file.exists() )
                {
                    modified = props.remove( key ) != null;
                }
                else
                {
                    props.setProperty( key, Long.toString( System.currentTimeMillis() ) );

                    modified = true;
                }

                if ( modified )
                {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    getLogger().debug( "Writing resolution-state to: " + touchfile );
                    props.store( stream, "Last modified on: " + new Date() );

                    byte[] data = stream.toByteArray();
                    ByteBuffer buffer = ByteBuffer.allocate( data.length );
                    buffer.put( data );
                    buffer.flip();

                    channel.position( 0 );
                    channel.write( buffer );
                }
            }
            catch ( IOException e )
            {
                getLogger().debug(
                                   "Failed to record lastUpdated information for metadata resolution.\nMetadata type: " +
                                       name, e );
            }
            finally
            {
                if ( lock != null )
                {
                    try
                    {
                        lock.release();
                    }
                    catch ( IOException e )
                    {
                        getLogger().debug(
                                           "Error releasing exclusive lock for metadata resolution tracking file: " +
                                               touchfile, e );
                    }
                }

                if ( channel != null )
                {
                    try
                    {
                        channel.close();
                    }
                    catch ( IOException e )
                    {
                        getLogger().debug(
                                           "Error closing FileChannel for metadata resolution tracking file: " +
                                               touchfile, e );
                    }
                }
            }
        }
    }

    private String getKey( String name, String repositoryId )
    {
        return repositoryId + "." + name + LAST_UPDATE_TAG;
    }

    public Date getLastModifiedFromTouchfile( File file, String repositoryId )
    {
        File touchfile = getTouchfile( file );

        if ( !touchfile.canRead() )
        {
            return null;
        }

        String name = file.getName();

        synchronized ( touchfile.getAbsolutePath().intern() )
        {
            String key = getKey( name, repositoryId );

            getLogger().debug( "Searching for: " + key + " in touchfile." );

            Date result = null;
            FileInputStream stream = null;
            FileLock lock = null;
            try
            {
                Properties props = new Properties();

                if ( touchfile.exists() )
                {
                    stream = new FileInputStream( touchfile );
                    FileChannel channel = stream.getChannel();
                    lock = channel.lock( 0, channel.size(), true );

                    getLogger().debug( "Reading resolution-state from: " + touchfile );
                    props.load( stream );

                    String rawVal = props.getProperty( key );
                    if ( rawVal != null )
                    {
                        try
                        {
                            result = new Date( Long.parseLong( rawVal ) );
                        }
                        catch ( NumberFormatException e )
                        {
                            getLogger().debug( "Cannot parse lastUpdated date: \'" + rawVal + "\'. Ignoring.", e );
                            result = null;
                        }
                    }
                }
            }
            catch ( IOException e )
            {
                getLogger().debug(
                                   "Failed to read lastUpdated information for metadata resolution.\nMetadata type: " +
                                       name, e );
            }
            finally
            {
                if ( lock != null )
                {
                    try
                    {
                        lock.release();
                    }
                    catch ( IOException e )
                    {
                        getLogger().debug(
                                           "Error releasing shared lock for metadata resolution tracking file: " +
                                               touchfile, e );
                    }
                }

                IOUtil.close( stream );
            }

            return result;
        }
    }

    private File getTouchfile( File file )
    {
        return new File( file.getParent(), TOUCHFILE_NAME );
    }
}
