package org.apache.maven.artifact.resolver;

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
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Specific problems during resolution that we want to account for:
 * <p/>
 * - missing metadata
 * - version range violations
 * - version circular dependencies
 * - missing artifacts
 * - network/transfer errors
 * - file system errors: permissions
 *
 * @author Jason van Zyl
 * @version $Id$
 */
public class ArtifactResolutionResult
{
    private Artifact originatingArtifact;

    private List versionRangeViolations;

    private List metadataResolutionExceptions;

    private List missingArtifacts;

    private List circularDependencyExceptions;

    private List errorArtifactExceptions;

    // file system errors

    private List repositories;

    private Set resolutionNodes;

    private Set artifacts;

    //

    public Artifact getOriginatingArtifact()
    {
        return originatingArtifact;
    }

    public ArtifactResolutionResult ListOriginatingArtifact( Artifact originatingArtifact )
    {
        this.originatingArtifact = originatingArtifact;

        return this;
    }

    public Set getArtifacts()
    {
        if ( artifacts == null )
        {
            artifacts = new HashSet();

            for ( Iterator i = resolutionNodes.iterator(); i.hasNext(); )
            {
                ResolutionNode node = (ResolutionNode) i.next();

                artifacts.add( node.getArtifact() );
            }
        }

        return artifacts;
    }

    public Set getArtifactResolutionNodes()
    {
        return resolutionNodes;
    }

    public ArtifactResolutionResult setArtifactResolutionNodes( Set resolutionNodes )
    {
        this.resolutionNodes = resolutionNodes;

        // clear the cache
        this.artifacts = null;

        return this;
    }

    public List getMissingArtifacts()
    {
        return missingArtifacts;
    }

    public ArtifactResolutionResult addMissingArtifact( Artifact artifact )
    {
        if ( missingArtifacts == null )
        {
            missingArtifacts = new ArrayList();
        }

        missingArtifacts.add( artifact );

        return this;
    }

    public ArtifactResolutionResult setUnresolvedArtifacts( List unresolvedArtifacts )
    {
        this.missingArtifacts = unresolvedArtifacts;

        return this;
    }

    // ------------------------------------------------------------------------
    // Version Range Violations
    // ------------------------------------------------------------------------

    public boolean hasVersionRangeViolations()
    {
        return versionRangeViolations != null;
    }

    public ArtifactResolutionResult addVersionRangeViolation( Exception e )
    {
        if ( versionRangeViolations == null )
        {
            versionRangeViolations = new ArrayList();
        }

        versionRangeViolations.add( e );

        return this;
    }

    public OverConstrainedVersionException getVersionRangeViolation( int i )
    {
        return (OverConstrainedVersionException) versionRangeViolations.get( i );
    }

    public List getVersionRangeViolations()
    {
        return versionRangeViolations;
    }

    // ------------------------------------------------------------------------
    // Metadata Resolution Exceptions: ArtifactResolutionExceptions
    // ------------------------------------------------------------------------

    public boolean hasMetadataResolutionExceptions()
    {
        return metadataResolutionExceptions != null;
    }

    public ArtifactResolutionResult addMetadataResolutionException( Exception e )
    {
        if ( metadataResolutionExceptions == null )
        {
            metadataResolutionExceptions = new ArrayList();
        }

        metadataResolutionExceptions.add( e );

        return this;
    }

    public ArtifactResolutionException getMetadataResolutionException( int i )
    {
        return (ArtifactResolutionException) metadataResolutionExceptions.get( i );
    }

    public List getMetadataResolutionExceptions()
    {
        return metadataResolutionExceptions;
    }

    // ------------------------------------------------------------------------
    // ErrorArtifactExceptions: ArtifactResolutionExceptions
    // ------------------------------------------------------------------------

    public boolean hasErrorArtifactExceptions()
    {
        return errorArtifactExceptions != null;
    }

    public ArtifactResolutionResult addErrorArtifactException( Exception e )
    {
        if ( errorArtifactExceptions == null )
        {
            errorArtifactExceptions = new ArrayList();
        }

        errorArtifactExceptions.add( e );

        return this;
    }

    public List getErrorArtifactExceptions()
    {
        return errorArtifactExceptions;
    }

    // ------------------------------------------------------------------------
    // Circular Dependency Exceptions
    // ------------------------------------------------------------------------

    public boolean hasCircularDependencyExceptions()
    {
        return circularDependencyExceptions != null;
    }

    public ArtifactResolutionResult addCircularDependencyException( Exception e )
    {
        if ( circularDependencyExceptions == null )
        {
            circularDependencyExceptions = new ArrayList();
        }

        errorArtifactExceptions.add( e );

        return this;
    }

    public CyclicDependencyException getCircularDependencyException( int i )
    {
        return (CyclicDependencyException) circularDependencyExceptions.get( i );
    }

    public List getCircularDependencyExceptions()
    {
        return circularDependencyExceptions;
    }

    // ------------------------------------------------------------------------
    //
    // ------------------------------------------------------------------------

    // Repositories

    public List getRepositories()
    {
        return repositories;
    }

    public ArtifactResolutionResult setRepositories( List repositories )
    {
        this.repositories = repositories;

        return this;
    }


}
