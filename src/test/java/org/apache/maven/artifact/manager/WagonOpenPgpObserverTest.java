package org.apache.maven.artifact.manager;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.openpgp.BouncyCastleKeyRing;
import org.apache.commons.openpgp.BouncyCastleOpenPgpSignatureVerifier;
import org.apache.commons.openpgp.KeyRing;
import org.apache.commons.openpgp.OpenPgpException;
import org.apache.commons.openpgp.OpenPgpSignatureVerifier;
import org.apache.commons.openpgp.SignatureStatus;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.PlexusTestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Test the wagon observer for open pgp signatures.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class WagonOpenPgpObserverTest
    extends PlexusTestCase
{
    private String keyId = "A7D16BD4";

    private static final String PASSWORD = "cop";

    private KeyRing keyRing;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        keyRing = new BouncyCastleKeyRing( getClass().getResourceAsStream( "/secring.gpg" ),
                                           getClass().getResourceAsStream( "/pubring.gpg" ), PASSWORD.toCharArray() );
    }

    public void testSign()
        throws Exception
    {
        WagonOpenPgpSignerObserver observer = new WagonOpenPgpSignerObserver( keyId, keyRing, false );

        Wagon wagon = (Wagon) lookup( Wagon.ROLE, "file" );

        wagon.addTransferListener( observer );

        File tempDir = getTestFile( "target/test-data/openpgp-repo" );
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        Repository repository = new Repository( "test", tempDir.toURL().toString() );

        wagon.connect( repository );

        wagon.put( getTestFile( "src/test/resources/test-input.txt" ), "test-input.txt" );

        byte[] signature = observer.getActualSignature();

        wagon.removeTransferListener( observer );

        wagon.disconnect();

        // check signature
        OpenPgpSignatureVerifier verifier = new BouncyCastleOpenPgpSignatureVerifier();
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( "/test-input.txt" ),
                                              new ByteArrayInputStream( signature ), keyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerify()
        throws Exception
    {
        verifySignature( "/test-input.txt.sig" );
        
        verifySignature( "/test-input.txt.asc" );
    }

    private void verifySignature( String name )
        throws OpenPgpException, IOException, Exception, MalformedURLException, ConnectionException,
        AuthenticationException, TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        WagonOpenPgpSignatureVerifierObserver observer =
            new WagonOpenPgpSignatureVerifierObserver( getClass().getResourceAsStream( name ), keyRing );

        Wagon wagon = (Wagon) lookup( Wagon.ROLE, "file" );

        wagon.addTransferListener( observer );

        File tempDir = getTestFile( "target/test-data/openpgp-repo" );
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        Repository repository = new Repository( "test", getTestFile( "src/test/resources" ).toURL().toString() );

        wagon.connect( repository );

        wagon.get( "test-input.txt", new File( tempDir, "test-input.txt" ) );

        SignatureStatus status = observer.getStatus();

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );

        wagon.removeTransferListener( observer );

        wagon.disconnect();
    }

}
