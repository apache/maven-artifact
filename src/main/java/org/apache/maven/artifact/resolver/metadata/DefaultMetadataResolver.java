package org.apache.maven.artifact.resolver.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactScopeEnum;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.conflict.GraphConflictResolver;
import org.apache.maven.artifact.transform.ClasspathTransformation;
import org.codehaus.plexus.logging.AbstractLogEnabled;


/**
 * default implementation of the metadata resolver
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 * @plexus.component
 *
 */
public class DefaultMetadataResolver
    extends AbstractLogEnabled
    implements MetadataResolver
{
    //------------------------------------------------------------------------

    /** @plexus.requirement */
    ArtifactResolver artifactResolver;

    /** @plexus.requirement */
    ArtifactFactory artifactFactory;

    /** @plexus.requirement */
    MetadataSource metadataSource;

    /** @plexus.requirement */
    GraphConflictResolver conflictResolver;

    /** @plexus.requirement */
    ClasspathTransformation classpathTransformation;

    //------------------------------------------------------------------------
    public MetadataResolutionResult resolveMetadata( MetadataResolutionRequest req )
        throws MetadataResolutionException
    {
        try
        {
            getLogger().debug( "Received request for: " + req.getQuery() );

            MetadataResolutionResult res = new MetadataResolutionResult( conflictResolver, classpathTransformation);

            if ( req.type == null )
            {
                throw new MetadataResolutionException( "no type in the request" );
            }

            MetadataTreeNode tree = resolveMetadataTree( req.getQuery()
            											, null
            											, req.getLocalRepository()
            											, req.getRemoteRepositories()
            											);

            res.setTree( tree );
            return res;
        }
        catch ( MetadataResolutionException mrEx )
        {
            throw mrEx;
        }
        catch ( Exception anyEx )
        {
            throw new MetadataResolutionException( anyEx );
        }
    }

    //------------------------------------------------------------------------
    private MetadataTreeNode resolveMetadataTree( ArtifactMetadata query
    											, MetadataTreeNode parent
    											, ArtifactRepository localRepository
    											, List<ArtifactRepository> remoteRepositories
    											)
        throws MetadataResolutionException
    {
        try
        {

            Artifact pomArtifact = artifactFactory.createArtifact(
                  query.getGroupId()
                , query.getArtifactId()
                , query.getVersion()
                , null
                , "pom"
            );

            getLogger().debug( "resolveMetadata request:"
                + "\n> artifact   : " + pomArtifact.toString()
                + "\n> remoteRepos: " + remoteRepositories
                + "\n> localRepo  : " + localRepository
            );

            String error = null;

            try
            {
                ArtifactResolutionRequest arr = new ArtifactResolutionRequest();
                arr.setArtifact( pomArtifact );
                arr.setLocalRepository( localRepository );
                arr.setRemoteRepostories( remoteRepositories );

                artifactResolver.resolve( pomArtifact, remoteRepositories , localRepository );
//System.out.println("Resolved "+query+" : "+pomArtifact.isResolved() );

                if ( !pomArtifact.isResolved() )
                {
                    getLogger().info( "*************> Did not resolve " + pomArtifact.toString()
                        + "\nURL: " + pomArtifact.getDownloadUrl()
                        + "\nRepos: " + remoteRepositories
                        + "\nLocal: " + localRepository
                    );
                }
            }
            catch ( ArtifactResolutionException are )
            {
                pomArtifact.setResolved( false );
                error = are.getMessage();
            }
            catch ( ArtifactNotFoundException anfe )
            {
                pomArtifact.setResolved( false );
                error = anfe.getMessage();
            }

            if( error != null )
            {
                getLogger().info( "*************> Did not resolve " + pomArtifact.toString()
                    + "\nRepos: " + remoteRepositories
                    + "\nLocal: " + localRepository
                    + "\nerror: " + error
                );
            }

            if( pomArtifact.isResolved() )
            {
                MetadataResolution metadataResolution = metadataSource.retrieve( 
                																query
                																, localRepository
                																, remoteRepositories
                																);
                ArtifactMetadata found = metadataResolution.getArtifactMetadata();
                
                // TODO 
                // Oleg: this is a shortcut to get AMd artifact location URI
                // it is only valid because artifact (A) resolution is done BEFORE 
                // Md resolution. 
                //
                // Should be done inside Md Source instead
                //
                found.setArtifactUri( pomArtifact.getFile().toURI().toString() );

                MetadataTreeNode node = new MetadataTreeNode( found
                											, parent
                											, true
                											, found.getScopeAsEnum()
                											);
                Collection<ArtifactMetadata> dependencies 
                		= metadataResolution.getArtifactMetadata().getDependencies();

                if( dependencies != null && dependencies.size() > 0 )
                {
                	int nKids = dependencies.size();
                	node.setNChildren(nKids);
                	int kidNo = 0;
                    for ( ArtifactMetadata a : dependencies )
                    {
                        MetadataTreeNode kidNode = resolveMetadataTree( a
                        											, node
                        											, localRepository
                        											, remoteRepositories
                        											);
                        node.addChild( kidNo++, kidNode );
                    }
                }
                return node;
            } else {
                return new MetadataTreeNode( pomArtifact, parent, false, query.getArtifactScope() );
            }
        }
        catch( Exception anyEx )
        {
            throw new MetadataResolutionException( anyEx );
        }
    }
    //------------------------------------------------------------------------
    //------------------------------------------------------------------------
}
