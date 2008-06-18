package org.apache.maven.artifact.deployer;

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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.DefaultRepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataDeploymentException;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataReadException;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException;
import org.apache.maven.artifact.transform.ArtifactTransformationManager;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/** @plexus.component */
public class DefaultArtifactDeployer
    extends AbstractLogEnabled
    implements ArtifactDeployer
{
    /** @plexus.requirement */
    private WagonManager wagonManager;

    /** @plexus.requirement */
    private ArtifactTransformationManager transformationManager;

    /** @plexus.requirement */
    private RepositoryMetadataManager repositoryMetadataManager;

    /** @plexus.requirement */
    private ArtifactMetadataSource metadataSource;

    /** @plexus.requirement role-hint="default" */
    private ArtifactRepositoryLayout defaultLayout;

    /** @deprecated we want to use the artifact method only, and ensure artifact.file is set correctly. */
    @Deprecated
    public void deploy( String basedir, String finalName, Artifact artifact, ArtifactRepository deploymentRepository,
                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        String extension = artifact.getArtifactHandler().getExtension();
        File source = new File( basedir, finalName + "." + extension );
        deploy( source, artifact, deploymentRepository, localRepository );
    }

    public void deploy( File source, Artifact artifact, ArtifactRepository deploymentRepository,
                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        if ( !wagonManager.isOnline() )
        {
            // deployment shouldn't silently fail when offline
            throw new ArtifactDeploymentException( "System is offline. Cannot deploy artifact: " + artifact + "." );
        }

        if ( !artifactHasBeenDeployed( artifact, localRepository, deploymentRepository ) )
        {
            try
            {
                transformationManager.transformForDeployment( artifact, deploymentRepository, localRepository );

                // Copy the original file to the new one if it was transformed
                File artifactFile = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
                if ( !artifactFile.equals( source ) )
                {
                    FileUtils.copyFile( source, artifactFile );
                }

                wagonManager.putArtifact( source, artifact, deploymentRepository );

                // must be after the artifact is installed
                for ( ArtifactMetadata metadata : artifact.getMetadataList() )
                {
                    repositoryMetadataManager.deploy( metadata, localRepository, deploymentRepository );
                }
            }
            catch ( TransferFailedException e )
            {
                throw new ArtifactDeploymentException( "Error deploying artifact: " + e.getMessage(), e );
            }
            catch ( IOException e )
            {
                throw new ArtifactDeploymentException( "Error deploying artifact: " + e.getMessage(), e );
            }
            catch ( RepositoryMetadataDeploymentException e )
            {
                throw new ArtifactDeploymentException( "Error installing artifact's metadata: " + e.getMessage(), e );
            }
        }
        else
        {
            throw new ArtifactDeploymentException( "The artifact " + artifact + " was already deployed to repository "
                + deploymentRepository.getId() );
        }
    }

    private boolean artifactHasBeenDeployed( Artifact artifact, ArtifactRepository localRepository,
                                             ArtifactRepository remoteRepository )
        throws ArtifactDeploymentException
    {
        try
        {
            // We will just let people deploy snapshots over and over again even if they want
            // to deploy something different with the same name.

            if ( artifact.isSnapshot() )
            {
                return false;
            }

            // We need to guard against repsositories in distribution management sections that
            // don't have any default policies set so all functions that expect policies
            // present don't fail.

            if ( remoteRepository.getReleases() == null )
            {
                ArtifactRepositoryPolicy releasesPolicy = new ArtifactRepositoryPolicy();

                releasesPolicy.setEnabled( true );
                ( (DefaultArtifactRepository) remoteRepository ).setReleases( releasesPolicy );
            }

            ArtifactVersion artifactVersion = new DefaultArtifactVersion( artifact.getVersion() );

            // results in an unfortunate double-retrieval of this metadata file (later for the update)
            // it would be better to atomically lock, retrieve, check, update, deploy, unlock 
            List<ArtifactVersion> versions =
                metadataSource.retrieveAvailableVersionsFromDeploymentRepository( artifact, localRepository,
                                                                                  remoteRepository );

            for ( ArtifactVersion deployedArtifactVersion : versions )
            {
                if ( artifactVersion.compareTo( deployedArtifactVersion ) == 0 )
                {
                    getLogger().warn(
                                      "The artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
                                          + artifact.getVersion() + " has already been deployed. Not deploying again." );

                    return true;
                }
            }
        }
        catch ( ArtifactMetadataRetrievalException e )
        {
            getLogger().warn(
                              "Cannot retrieve the artifact metadata, or it does not exist. Assuming this artifact needs to be deployed." );
            getLogger().debug( e.getMessage(), e );
            return false;
        }

        return false;
    }
}
