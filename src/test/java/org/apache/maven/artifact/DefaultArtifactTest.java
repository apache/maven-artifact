package org.apache.maven.artifact;

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

import org.apache.maven.artifact.handler.ArtifactHandlerMock;
import org.apache.maven.artifact.versioning.VersionRange;

import junit.framework.TestCase;

public class DefaultArtifactTest
    extends TestCase
{

    private DefaultArtifact artifact;

    private DefaultArtifact snapshotArtifact;

    private String groupId = "groupid", artifactId = "artifactId", version = "1.0", scope = "artifactScope", type = "type",
        classifier = "classifier";

    private String snapshotSpecVersion = "1.0-SNAPSHOT";
    private String snapshotResolvedVersion = "1.0-20070606.010101-1";

    private ArtifactHandlerMock artifactHandler;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        artifact = new DefaultArtifact( groupId, artifactId, version, type, classifier, false, scope, null );

        snapshotArtifact = new DefaultArtifact( groupId, artifactId, snapshotResolvedVersion, type, classifier, false, scope, null );
    }

    public void testGetVersionReturnsResolvedVersionOnSnapshot()
    {
        assertEquals( snapshotResolvedVersion, snapshotArtifact.getVersion() );

        // this is FOUL!
//        snapshotArtifact.isSnapshot();

        assertEquals( snapshotSpecVersion, snapshotArtifact.getBaseVersion() );
    }

    public void testGetDependencyConflictId()
    {
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier, artifact.getDependencyConflictId() );
    }

    public void testGetDependencyConflictIdNullGroupId()
    {
        artifact.setGroupId( null );
        assertEquals( null + ":" + artifactId + ":" + type + ":" + classifier, artifact.getDependencyConflictId() );
    }

    public void testGetDependencyConflictIdNullClassifier()
        throws Exception
    {
        artifact = new DefaultArtifact( groupId, artifactId, version, type, null, false, scope, null );
        assertEquals( groupId + ":" + artifactId + ":" + type, artifact.getDependencyConflictId() );
    }

    public void testGetDependencyConflictIdNullScope()
        throws Exception
    {
        artifact.setScope( null );
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier, artifact.getDependencyConflictId() );
    }

    public void testToString()
        throws Exception
    {
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier + ":" + version + ":" + scope,
                      artifact.toString() );
    }

    public void testToStringNullGroupId()
        throws Exception
    {
        artifact.setGroupId( null );
        assertEquals( artifactId + ":" + type + ":" + classifier + ":" + version + ":" + scope, artifact.toString() );
    }

    public void testToStringNullClassifier()
        throws Exception
    {
        artifact = new DefaultArtifact( groupId, artifactId, version, type, null, false, scope, null );
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + version + ":" + scope, artifact.toString() );
    }

    public void testToStringNullScope()
    {
        artifact.setScope( null );
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier + ":" + version, artifact.toString() );
    }
}
