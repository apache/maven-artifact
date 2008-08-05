package org.apache.maven.artifact.pgp;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Test the open pgp signer.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @todo test text input as well as binary - apparently it fails cross platform
 */
public class StreamingSignerTest
    extends TestCase
{
    private static final String FILE = "/test-input.txt";

    private String keyId = "A7D16BD4";

    private SecretKeyRing keyRing;

    private static final String PASSWORD = "cop";

    private PublicKeyRing publicKeyRing;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        keyRing = new SecretKeyRing();
        keyRing.addSecretKeyRing( getClass().getResourceAsStream( "/secring.gpg" ), PASSWORD.toCharArray() );

        publicKeyRing = new PublicKeyRing();
        publicKeyRing.addPublicKeyRing( getClass().getResourceAsStream( "/pubring.gpg" ) );
    }

    public void testSignDataDetachedBinary()
        throws OpenPgpException, IOException
    {
        StreamingSigner signer = new StreamingSigner( keyId, keyRing, false );

        InputStream in = getClass().getResourceAsStream( FILE );
        byte[] buf = new byte[8192];
        int len;
        try
        {
            do
            {
                len = in.read( buf, 0, 8192 );
                if ( len > 0 )
                {
                    signer.update( buf, 0, len );
                }
            }
            while ( len >= 0 );
        }
        finally
        {
            in.close();
        }

        byte[] signature = signer.finish();

        SignatureVerifier verifier = new SignatureVerifier();

        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              new ByteArrayInputStream( signature ), publicKeyRing );
        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifySignatureDetachedBinaryGpg()
        throws IOException, OpenPgpException
    {
        InputStream signature = getClass().getResourceAsStream( "/test-input.txt.sig" );
        StreamingSignatureVerifier verifier = new StreamingSignatureVerifier( signature, publicKeyRing );

        InputStream in = getClass().getResourceAsStream( FILE );
        byte[] buf = new byte[8192];
        int len;
        try
        {
            do
            {
                len = in.read( buf, 0, 8192 );
                if ( len > 0 )
                {
                    verifier.update( buf, 0, len );
                }
            }
            while ( len >= 0 );
        }
        finally
        {
            in.close();
        }

        SignatureStatus status = verifier.finish();

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testSignDataDetachedAscii()
        throws OpenPgpException, IOException
    {
        StreamingSigner signer = new StreamingSigner( keyId, keyRing, true );

        InputStream in = getClass().getResourceAsStream( FILE );
        byte[] buf = new byte[8192];
        int len;
        try
        {
            do
            {
                len = in.read( buf, 0, 8192 );
                if ( len > 0 )
                {
                    signer.update( buf, 0, len );
                }
            }
            while ( len >= 0 );
        }
        finally
        {
            in.close();
        }

        byte[] signature = signer.finish();

        SignatureVerifier verifier = new SignatureVerifier();

        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              new ByteArrayInputStream( signature ), publicKeyRing );
        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifySignatureDetachedAscii()
        throws IOException, OpenPgpException
    {
        InputStream signature = getClass().getResourceAsStream( "/test-input.txt.asc" );
        StreamingSignatureVerifier verifier = new StreamingSignatureVerifier( signature, publicKeyRing );

        InputStream in = getClass().getResourceAsStream( FILE );
        byte[] buf = new byte[8192];
        int len;
        try
        {
            do
            {
                len = in.read( buf, 0, 8192 );
                if ( len > 0 )
                {
                    verifier.update( buf, 0, len );
                }
            }
            while ( len >= 0 );
        }
        finally
        {
            in.close();
        }

        SignatureStatus status = verifier.finish();

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }
}
