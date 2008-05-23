package org.apache.maven.artifact.resolver.metadata.resolver;

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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
 * @TODO carlos: all these possible has*Exceptions and get*Exceptions methods make the clients too
 * complex requiring a long list of checks, need to create a parent/interfact/encapsulation
 * for the types of exceptions
 */
public class ResolutionResult
{
    private Artifact originatingArtifact;

    private List versionRangeViolations;

    private List metadataResolutionExceptions;

    private List missingArtifacts;

    private List errorArtifactExceptions;

    // file system errors

    private List repositories;

    private Set artifacts;

    //

    public Artifact getOriginatingArtifact()
    {
        return originatingArtifact;
    }

    public ResolutionResult ListOriginatingArtifact( Artifact originatingArtifact )
    {
        this.originatingArtifact = originatingArtifact;

        return this;
    }

    /**
     * 
     * @return set of Artifact instances
     */
    
    public Set getArtifacts()
    {
        return artifacts;
    }

    public List getMissingArtifacts()
    {
        return missingArtifacts == null ? Collections.EMPTY_LIST : missingArtifacts;
    }

    public ResolutionResult addMissingArtifact( Artifact artifact )
    {
        missingArtifacts = initList( missingArtifacts );

        missingArtifacts.add( artifact );

        return this;
    }

    public ResolutionResult setUnresolvedArtifacts( List unresolvedArtifacts )
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

    /**
     * @TODO this needs to accept a {@link OverConstrainedVersionException} as returned by
     * {@link #getVersionRangeViolation(int)} but it's not used like that in
     * {@link DefaultArtifactCollector}
     */
    public ResolutionResult addVersionRangeViolation( Exception e )
    {
        versionRangeViolations = initList( versionRangeViolations );

        versionRangeViolations.add( e );

        return this;
    }

    public OverConstrainedVersionException getVersionRangeViolation( int i )
    {
        return (OverConstrainedVersionException) versionRangeViolations.get( i );
    }

    public List getVersionRangeViolations()
    {
        return versionRangeViolations == null ? Collections.EMPTY_LIST : versionRangeViolations;
    }

    // ------------------------------------------------------------------------
    // Metadata Resolution Exceptions: ArtifactResolutionExceptions
    // ------------------------------------------------------------------------

    public boolean hasMetadataResolutionExceptions()
    {
        return metadataResolutionExceptions != null;
    }

    public ResolutionResult addMetadataResolutionException( ArtifactResolutionException e )
    {
        metadataResolutionExceptions = initList( metadataResolutionExceptions );

        metadataResolutionExceptions.add( e );

        return this;
    }

    public ArtifactResolutionException getMetadataResolutionException( int i )
    {
        return (ArtifactResolutionException) metadataResolutionExceptions.get( i );
    }

    public List getMetadataResolutionExceptions()
    {
        return metadataResolutionExceptions == null ? Collections.EMPTY_LIST : metadataResolutionExceptions;
    }

    // ------------------------------------------------------------------------
    // ErrorArtifactExceptions: ArtifactResolutionExceptions
    // ------------------------------------------------------------------------

    public boolean hasErrorArtifactExceptions()
    {
        return errorArtifactExceptions != null;
    }

    public ResolutionResult addErrorArtifactException( ArtifactResolutionException e )
    {
        errorArtifactExceptions = initList( errorArtifactExceptions );

        errorArtifactExceptions.add( e );

        return this;
    }

    public List getErrorArtifactExceptions()
    {
        return errorArtifactExceptions == null ? Collections.EMPTY_LIST : errorArtifactExceptions;
    }

    // ------------------------------------------------------------------------
    //
    // ------------------------------------------------------------------------

    // Repositories

    public List getRepositories()
    {
        return repositories == null ? Collections.EMPTY_LIST : repositories;
    }

    public ResolutionResult setRepositories( List repositories )
    {
        this.repositories = repositories;

        return this;
    }

    private List initList( List l )
    {
        if ( l == null )
        {
            return new ArrayList();
        }
        return l;
    }
}
