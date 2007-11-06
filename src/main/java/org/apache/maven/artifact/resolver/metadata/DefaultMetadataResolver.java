package org.apache.maven.artifact.resolver.metadata;

import java.util.ArrayList;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.codehaus.plexus.logging.AbstractLogEnabled;

/** @author Oleg Gusakov */
public class DefaultMetadataResolver
    extends AbstractLogEnabled
    implements MetadataResolver
{
    //------------------------------------------------------------------------
    ArtifactResolver ar;
    ArtifactFactory af;
    ArtifactMetadataSource ams;

    //------------------------------------------------------------------------
    public MetadataResolutionResult resolveMetadata( MetadataResolutionRequest req )
        throws MetadataResolutionException
    {
        try
        {
            getLogger().debug( "Received request for: " + req.getQuery() );
            MetadataResolutionResult res = new MetadataResolutionResult();
            if ( req.type == null )
            {
                throw new MetadataResolutionException( "no type in the request" );
            }

            MetadataTreeNode tree = resolveTree( req, null );
            MetadataGraph graph = null;

            if ( MetadataResolutionRequestTypeEnum.tree.equals( req.type ) )
            {
                res.setTree( tree );
            }
            if ( MetadataResolutionRequestTypeEnum.graph.equals( req.type ) )
            {
                graph = new MetadataGraph( tree );
                res.setTree( tree );
                res.setGraph( graph );
            }

            return res;
        }
        catch ( MetadataResolutionException mrEx )
        {
            throw mrEx;
        }
        catch ( Exception anyEx )
        {
            anyEx.printStackTrace();
            throw new MetadataResolutionException( anyEx );
        }
    }

    //------------------------------------------------------------------------
    private MetadataTreeNode resolveTree( MetadataResolutionRequest req,
                                          MetadataTreeNode parent )
        throws MetadataResolutionException
    {
        try
        {
            ArtifactMetadata query = req.getQuery();
            Artifact pomArtifact = af.createArtifact(
                query.getGroupId()
                , query.getArtifactId()
                , query.getVersion()
                , null
                , "pom"
            );
            getLogger().debug( "resolveMetadata request:"
                + "\n> artifact   : " + pomArtifact.toString()
                + "\n> remoteRepos: " + req.getRemoteRepositories()
                + "\n> localRepo  : " + req.getLocalRepository()
            );
            String error = null;
            try
            {
                ArtifactResolutionRequest arr = new ArtifactResolutionRequest();
                arr.setArtifact( pomArtifact );
                arr.setLocalRepository( req.getLocalRepository() );
                arr.setRemoteRepostories( req.getRemoteRepositories() );
                ar.resolve( pomArtifact
                    , req.getRemoteRepositories()
                    , req.getLocalRepository()
                );
                if ( !pomArtifact.isResolved() )
                {
                    getLogger().info( "*************> Did not resolve " + pomArtifact.toString()
                        + "\nURL: " + pomArtifact.getDownloadUrl()
                        + "\nRepos: " + req.getRemoteRepositories()
                        + "\nLocal: " + req.getLocalRepository()
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

            if ( error != null )
            {
                getLogger().info( "*************> Did not resolve " + pomArtifact.toString()
                    + "\nRepos: " + req.getRemoteRepositories()
                    + "\nLocal: " + req.getLocalRepository()
                    + "\nerror: " + error
                );
            }
            if ( pomArtifact.isResolved() )
            {
                ResolutionGroup rg = ams.retrieve( pomArtifact, req.getLocalRepository(), req.getRemoteRepositories() );
                MetadataTreeNode node = new MetadataTreeNode( pomArtifact, parent, true, query.getScope() );
                Set<Artifact> dependencies = (Set<Artifact>) rg.getArtifacts();
                if ( dependencies != null && dependencies.size() > 0 )
                {
                    ArrayList<MetadataTreeNode> kids = new ArrayList<MetadataTreeNode>( dependencies.size() );
                    for ( Artifact a : dependencies )
                    {
                        req.query.init( a );
                        req.query.setType( "pom" );
                        kids.add( resolveTree( req, node ) );
                    }
                    node.addChildren( kids );
                }
                return node;
            }
            else
            {
                return new MetadataTreeNode( pomArtifact, parent, false, query.getScope() );
            }
        }
        catch ( Exception anyEx )
        {
            anyEx.printStackTrace();
            throw new MetadataResolutionException( anyEx );
        }
    }
    //------------------------------------------------------------------------
    //------------------------------------------------------------------------
}
