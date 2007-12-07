package org.apache.maven.artifact.aspect;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.context.Context;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.conflict.ConflictResolver;

public privileged aspect ResolverCacheAspect
{

//    declare parents: DefaultArtifactResolver implements Contextualizable;
//
//    private Map<String, Artifact> DefaultArtifactResolver.cachedSingleResolutions =
//        new HashMap<String, Artifact>();
//
//    private Map<String, ArtifactResolutionResult> DefaultArtifactResolver.cachedResolutionResults =
//        new HashMap<String, ArtifactResolutionResult>();
//
//    private PlexusContainer DefaultArtifactResolver.container;
//    private Logger DefaultArtifactResolver.logger;
//
//    // NOTE: We implement advice on this method rather than setting the container instance
//    // directly, so we can still recover the container even if the resolver implementation
//    // happens to override the method.
//    public void DefaultArtifactResolver.contextualize( Context context ) throws ContextException
//    {
//        if ( container == null )
//        {
//            container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
//            logger = container.getLoggerManager().getLoggerForComponent( ArtifactResolver.ROLE );
//
//            logger.debug( "DefaultArtifactResolver caching aspect captured PlexusContainer instance." );
//        }
//    }
//
//    private pointcut deprecatedSingleResolveCall( Artifact artifact, DefaultArtifactResolver resolver ):
//        execution( void DefaultArtifactResolver+.resolve( Artifact, .. ) )
//        && args( artifact, .. )
//        && this( resolver );
//
//    void around( Artifact artifact, DefaultArtifactResolver resolver )
//        throws ArtifactResolutionException, ArtifactNotFoundException:
//        deprecatedSingleResolveCall( artifact, resolver )
//    {
//        System.out.println( "Cache check for: " + artifact + " using resolver: " + resolver + " (has logger? " + ( resolver != null && resolver.logger != null ) + ")" );
//
//        String key = createCacheKey( null, (Set<Artifact>) Collections.singleton( artifact ), null, null );
//        resolver.logger.debug( "Checking artifact resolution cache for: " + key );
//
//        Artifact cached = resolver.cachedSingleResolutions.get( key );
//        if ( cached != null )
//        {
//            resolver.logger.debug( "Using cached copy of: " + key );
//            copyFromCached( artifact, cached );
//
//            return;
//        }
//
//        proceed( artifact, resolver );
//
//        resolver.logger.debug( "Caching: " + key );
//        resolver.cachedSingleResolutions.put( key, artifact );
//    }
//
//    private pointcut deprecatedTransitiveResolveCall( Set artifacts, Artifact originatingArtifact, DefaultArtifactResolver resolver ):
//        execution( ArtifactResolutionResult DefaultArtifactResolver+.resolveTransitively( Set, Artifact, .. ) )
//        && args( artifacts, originatingArtifact, .. )
//        && this( resolver );
//
//    @SuppressWarnings("unchecked")
//    ArtifactResolutionResult around( Set artifacts, Artifact originatingArtifact, DefaultArtifactResolver resolver )
//        throws ArtifactResolutionException, ArtifactNotFoundException:
//        deprecatedTransitiveResolveCall( artifacts, originatingArtifact, resolver )
//    {
//        ArtifactFilter filter = null;
//        for ( Object arg : thisJoinPoint.getArgs() )
//        {
//            if ( arg instanceof ArtifactFilter )
//            {
//                filter = (ArtifactFilter) arg;
//                break;
//            }
//        }
//
//        String key = createCacheKey( originatingArtifact, artifacts, null, filter );
//        resolver.logger.debug( "Checking artifact resolution cache for: " + key );
//
//        ArtifactResolutionResult cached = resolver.cachedResolutionResults.get( key );
//        if ( cached != null )
//        {
//            resolver.logger.debug( "Using cached copy of: " + key );
//            return cached;
//        }
//
//        cached = proceed( artifacts, originatingArtifact, resolver );
//
//        resolver.logger.debug( "Caching: " + key );
//        resolver.cachedResolutionResults.put( key, cached );
//
//        return cached;
//    }
//
//    private pointcut resolveCallWithRequest( ArtifactResolutionRequest request, DefaultArtifactResolver resolver ):
//        execution( ArtifactResolutionResult DefaultArtifactResolver+.resolve( ArtifactResolutionRequest ) )
//        && args( request )
//        && this( resolver );
//
//    @SuppressWarnings("unchecked")
//    ArtifactResolutionResult around( ArtifactResolutionRequest request, DefaultArtifactResolver resolver ):
//        resolveCallWithRequest( request, resolver )
//    {
//        String key = createCacheKey( request.getArtifact(), request.getArtifactDependencies(),
//                                     request.getConflictResolvers(), request.getFilter() );
//
//        resolver.logger.debug( "Checking artifact resolution cache for: " + key );
//
//        ArtifactResolutionResult cached = resolver.cachedResolutionResults.get( key );
//        if ( cached != null )
//        {
//            resolver.logger.debug( "Using cached copy of: " + key );
//            return cached;
//        }
//
//        cached = proceed( request, resolver );
//
//        resolver.logger.debug( "Caching: " + key );
//        resolver.cachedResolutionResults.put( key, cached );
//
//        return cached;
//    }
//
//    private void copyFromCached( Artifact artifact, Artifact cached )
//    {
//        artifact.setFile( cached.getFile() );
//        artifact.setResolved( cached.isResolved() );
//        artifact.setRepository( cached.getRepository() );
//        artifact.setDownloadUrl( cached.getDownloadUrl() );
//        artifact.setVersion( cached.getVersion() );
//
//        artifact.setAvailableVersions( cached.getAvailableVersions() );
//        artifact.setBaseVersion( cached.getBaseVersion() );
//        artifact.setDependencyTrail( cached.getDependencyTrail() );
//    }
//
//    private String createCacheKey( Artifact originatingArtifact, Set<Artifact> artifacts, List<ConflictResolver> conflictResolvers, ArtifactFilter filter )
//    {
//        // NOTE: artifact sets in different orders can produce different results in transitive resolution.
//        // Therefore, DO NOT sort this alphabetically first...preserve the set ordering, if there is any.
//        // String[] ids = new String[artifacts.size()];
//        // int i=0;
//        // for ( Artifact artifact : artifacts )
//        // {
//        //     ids[i] = artifact.getId();
//        //     i++;
//        // }
//        //
//        // Arrays.sort( ids );
//
//        // TODO: Review. This will be ugly, and may not be great on the memory, but it should work for now.
//        StringBuilder sb = new StringBuilder();
//
//        if ( originatingArtifact != null )
//        {
//            sb.append( originatingArtifact.getId() );
//            sb.append( ':' );
//        }
//
//        for ( Artifact artifact : artifacts )
//        {
//            sb.append( artifact.getId() );
//            sb.append( ':' );
//        }
//
//        if ( conflictResolvers != null )
//        {
//            for ( ConflictResolver conflictResolver : conflictResolvers )
//            {
//                sb.append( conflictResolver.hashCode() );
//                sb.append( ':' );
//            }
//        }
//
//        if ( filter != null )
//        {
//            sb.append( filter.hashCode() );
//            sb.append( ':' );
//        }
//
//        sb.setLength( sb.length() - 1 );
//
//        return sb.toString();
//    }

}
