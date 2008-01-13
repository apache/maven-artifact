package org.apache.maven.artifact.repository.metadata;

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

import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class MetadataTouchfile
{

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );

    private static final String LAST_UPDATE_TAG = ".lastUpdated";

    private static final String TOUCHFILE_NAME = "resolver-status.properties";

    private final ArtifactMetadata metadata;

    private final ArtifactRepository localRepository;

    private final File touchfile;

    public MetadataTouchfile( ArtifactMetadata metadata,
                              ArtifactRepository localRepository )
    {
        this.metadata = metadata;
        this.localRepository = localRepository;

        File metadataFile = new File(
                                      localRepository.getBasedir(),
                                      localRepository.pathOfLocalRepositoryMetadata( metadata,
                                                                                     localRepository ) );
        metadataFile = metadataFile.getAbsoluteFile();

        touchfile = new File( metadataFile.getParentFile(), TOUCHFILE_NAME );
    }

    public ArtifactMetadata getMetadata()
    {
        return metadata;
    }

    public ArtifactRepository getLocalRepository()
    {
        return localRepository;
    }

    public void touch( String repositoryId, String name,
                       Logger logger )
    {
        synchronized ( touchfile.getAbsolutePath().intern() )
        {
            if ( !touchfile.getParentFile().exists() && !touchfile.getParentFile().mkdirs() )
            {
                logger.debug( "Failed to create directory: " + touchfile.getParent()
                                       + " for tracking artifact metadata resolution." );
                return;
            }

            FileChannel channel = null;
            FileLock lock = null;
            try
            {
                Properties props = new Properties();

                channel = new RandomAccessFile( touchfile, "rw" ).getChannel();
                lock = channel.lock( 0, channel.size(), false );

                if ( touchfile.exists() )
                {
                    logger.debug( "Reading resolution-state from: " + touchfile );
                    ByteBuffer buffer = ByteBuffer.allocate( (int) channel.size() );

                    channel.read( buffer );
                    buffer.flip();

                    ByteArrayInputStream stream = new ByteArrayInputStream( buffer.array() );
                    props.load( stream );
                }

                Date date = new Date();

                logger.debug( "Adding " + repositoryId + "." + name + LAST_UPDATE_TAG + "=" + FORMAT.format( date ) + " to: " + touchfile );

                props.setProperty( repositoryId + "." + name + LAST_UPDATE_TAG, FORMAT.format( date ) );

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                logger.debug( "Writing resolution-state to: " + touchfile );
                props.store( stream, "Last modified on: " + new Date() );

                byte[] data = stream.toByteArray();
                ByteBuffer buffer = ByteBuffer.allocate( data.length );
                buffer.put( data );
                buffer.flip();

                channel.position( 0 );
                channel.write( buffer );
            }
            catch( IOException e )
            {
                logger.debug( "Failed to record lastUpdated information for metadata resolution.\nMetadata type: "
                                              + metadata + "\nRepository: " + repositoryId,
                              e );
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
                        logger.debug( "Error releasing exclusive lock for metadata resolution tracking file: " + touchfile, e );
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
                        logger.debug( "Error closing FileChannel for metadata resolution tracking file: " + touchfile, e );
                    }
                }
            }
        }
    }

    public Date getLastCheckDate( String repositoryId, String name,
                       Logger logger )
    {
        synchronized ( touchfile.getAbsolutePath().intern() )
        {
            logger.debug( "Searching for: " + repositoryId + "." + name + LAST_UPDATE_TAG + " in touchfile." );

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

                    logger.debug( "Reading resolution-state from: " + touchfile );
                    props.load( stream );

                    String rawVal = props.getProperty( repositoryId + "." + name + LAST_UPDATE_TAG );
                    if ( rawVal != null )
                    {
                        try
                        {
                            result = FORMAT.parse( rawVal );
                        }
                        catch ( ParseException e )
                        {
                            logger.debug( "Cannot parse lastUpdated date: \'" + rawVal + "\' using format: " + FORMAT.toLocalizedPattern() + ". Ignoring.", e );
                            result = null;
                        } 
                        catch ( NumberFormatException e ) 
                        {
                            logger.debug( "Cannot parse lastUpdated date: \'" + rawVal + "\' using format: " + FORMAT.toLocalizedPattern() + ". Touchfile: " + touchfile.getAbsolutePath() + " Ignoring.", e );
                            result = null;
                        }
                    }
                }
            }
            catch( IOException e )
            {
                logger.debug( "Failed to read lastUpdated information for metadata resolution.\nMetadata type: "
                                              + metadata + "\nRepository: " + repositoryId,
                              e );
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
                        logger.debug( "Error releasing shared lock for metadata resolution tracking file: " + touchfile, e );
                    }
                }

                IOUtil.close( stream );
            }

            return result;
        }
    }

}
