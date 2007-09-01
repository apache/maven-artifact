package org.apache.maven.artifact.resolver;

import org.apache.maven.artifact.Artifact;

import java.util.List;

/**
 * A simple recording of the Artifacts that could not be resolved for a given resolution request, along with
 * the remote repositories where attempts were made to resolve the artifacts.
 *
 * @author Jason van Zyl
 */
public class UnresolvedArtifacts
{
    private Artifact originatingArtifact;

    private List artifacts;

    private List remoteRepositories;

    public UnresolvedArtifacts( Artifact originatingArtifact,
                                List artifacts,
                                List remoteRepositories )
    {
        this.originatingArtifact = originatingArtifact;

        this.artifacts = artifacts;

        this.remoteRepositories = remoteRepositories;
    }

    public Artifact getOriginatingArtifact()
    {
        return originatingArtifact;
    }

    public List getArtifacts()
    {
        return artifacts;
    }

    public List getRemoteRepositories()
    {
        return remoteRepositories;
    }
}
