package org.apache.maven.artifact.pgp;

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

import org.apache.maven.artifact.pgp.WagonOpenPgpSignatureVerifierObserver;
import org.apache.maven.artifact.pgp.WagonOpenPgpSignerObserver;
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

    private PublicKeyRing keyRing;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        keyRing = new PublicKeyRing();
        keyRing.addPublicKeyRing( getClass().getResourceAsStream( "/gpg/pubring.gpg" ) );
    }

    public void testSign()
        throws Exception
    {
        SecretKeyRing keyRing = new SecretKeyRing();
        keyRing.addSecretKeyRing( getClass().getResourceAsStream( "/gpg/secring.gpg" ), "cop".toCharArray() );

        WagonOpenPgpSignerObserver observer = new WagonOpenPgpSignerObserver( keyId, keyRing, false );

        Wagon wagon = (Wagon) lookup( Wagon.ROLE, "file" );

        wagon.addTransferListener( observer );

        File tempDir = getTestFile( "target/test-data/openpgp-repo" );
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        Repository repository = new Repository( "test", tempDir.toURL().toString() );

        wagon.connect( repository );

        wagon.put( getTestFile( "src/test/resources/gpg/test-input.txt" ), "/gpg/test-input.txt" );

        byte[] signature = observer.getActualSignature();

        wagon.removeTransferListener( observer );

        wagon.disconnect();

        // check signature
        SignatureVerifier verifier = new SignatureVerifier();
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( "/gpg/test-input.txt" ),
                                              new ByteArrayInputStream( signature ), this.keyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerify()
        throws Exception
    {
        verifySignature( "/gpg/test-input.txt.sig" );

        verifySignature( "/gpg/test-input.txt.asc" );
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

        wagon.get( "gpg/test-input.txt", new File( tempDir, "gpg/test-input.txt" ) );

        SignatureStatus status = observer.getStatus();

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );

        wagon.removeTransferListener( observer );

        wagon.disconnect();
    }

}
