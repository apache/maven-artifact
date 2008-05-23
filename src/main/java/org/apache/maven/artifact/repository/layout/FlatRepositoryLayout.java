package org.apache.maven.artifact.repository.layout;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.resolver.metadata.Artifact;
import org.apache.maven.artifact.resolver.metadata.ArtifactRepository;

/**
 * The code in this class is taken from DefaultRepositorylayout, located at:
 * http://svn.apache.org/viewvc/maven/components/trunk/maven-artifact/src/main/java/org/apache/maven/artifact/repository/layout/DefaultRepositoryLayout.java
 *
 * @version $Id$
 * @plexus.component role="org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout" role-hint="flat" 
 */
public class FlatRepositoryLayout
    implements ArtifactRepositoryLayout
{
    private static final char ARTIFACT_SEPARATOR = '-';

    private static final char GROUP_SEPARATOR = '.';

    /** @plexus.requirement */
    private ArtifactHandlerManager artifactHandlerManager;

    public String pathOf( Artifact artifact )
    {
        ArtifactHandler artifactHandler = artifactHandlerManager.getArtifactHandler( artifact.getType() );

        StringBuffer path = new StringBuffer();

        path.append( artifact.getArtifactId() ).append( ARTIFACT_SEPARATOR ).append( artifact.getVersion() );

        if ( artifact.hasClassifier() )
        {
            path.append( ARTIFACT_SEPARATOR ).append( artifact.getClassifier() );
        }

        if ( artifactHandler.getExtension() != null && artifactHandler.getExtension().length() > 0 )
        {
            path.append( GROUP_SEPARATOR ).append( artifactHandler.getExtension() );
        }

        return path.toString();
    }

    public String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata,
                                                 ArtifactRepository repository )
    {
        return pathOfRepositoryMetadata( metadata.getLocalFilename( repository ) );
    }

    private String pathOfRepositoryMetadata( String filename )
    {
        StringBuffer path = new StringBuffer();

        path.append( filename );

        return path.toString();
    }

    public String pathOfRemoteRepositoryMetadata( ArtifactMetadata metadata )
    {
        return pathOfRepositoryMetadata( metadata.getRemoteFilename() );
    }
}
