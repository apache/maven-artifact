package org.apache.maven.artifact.aspect;

import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;

import org.codehaus.plexus.logging.Logger;

import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.metadata.DefaultRepositoryMetadataManager;

import java.io.File;
import java.util.Date;

public privileged aspect ResolverDebugAspect
{

    private pointcut resolveAlwaysCall( ArtifactMetadata metadata, ArtifactRepository repo, File file ):
        execution( void DefaultRepositoryMetadataManager.resolveAlways( ArtifactMetadata, ArtifactRepository, File, .. ) )
        && args( metadata, repo, file, .. );

    void around( ArtifactMetadata metadata, ArtifactRepository repo, File file ):
        resolveAlwaysCall( metadata, repo, file )
    {
        System.out.println( "Resolving repository metadata (" + metadata + ") for repository: " + repo.getId() + ". Possible reasons:" );
        System.out.println( "Last-Modified date: " + new Date( file.lastModified() ) + " is beyond the updateInterval." );
        System.out.println( "File: " + file.getAbsolutePath() + " exists? " + ( file.exists() ) );
        System.out.println();

        proceed( metadata, repo, file );

        System.out.println( "After resolving to file:" + file.getAbsolutePath() + ":" );
        System.out.println( "Last-Modified date: " + new Date( file.lastModified() ) );
        System.out.println( "File exists? " + ( file.exists() ) );
        System.out.println();
    }

    void around( String message, Logger logger ):
        withincode( void DefaultRepositoryMetadataManager.resolveAlways( .. ) )
        && call( void Logger.debug( String ) )
        && args( message )
        && target( logger )
    {
        logger.info( message );
    }

    void around( String message, Throwable e, Logger logger ):
        withincode( void DefaultRepositoryMetadataManager.resolveAlways( .. ) )
        && call( void Logger.debug( String, Throwable ) )
        && args( message, e )
        && target( logger )
    {
        logger.info( message, e );
    }

    private pointcut checkOutOfDateExecution( Date date, ArtifactRepositoryPolicy policy ):
        execution( boolean ArtifactRepositoryPolicy.checkOutOfDate( Date ) )
        && args( date )
        && this( policy );

    boolean around( Date date, ArtifactRepositoryPolicy policy ):
        checkOutOfDateExecution( date, policy )
    {
        boolean result = proceed( date, policy );

        if ( !ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER.equals( policy.getUpdatePolicy() ) )
        {
            System.out.println( "Is repository-update policy: \'" + policy.getUpdatePolicy() + "\' out of date for: " + date + "? " + result );
        }

        return result;
    }
}
