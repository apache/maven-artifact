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
    private static final String FILE = "/gpg/test-input.txt";
    
    private String keyId = "A7D16BD4";

    private SecretKeyRing keyRing;

    private static final String PASSWORD = "cop";

    private PublicKeyRing publicKeyRing;

    private SignatureVerifier verifier = new SignatureVerifier();

    protected void setUp()
        throws Exception
    {
        super.setUp();

        keyRing = new SecretKeyRing();
        keyRing.addSecretKeyRing( getClass().getResourceAsStream( "/gpg/secring.gpg" ), PASSWORD.toCharArray() );

        publicKeyRing = new PublicKeyRing();
        publicKeyRing.addPublicKeyRing( getClass().getResourceAsStream( "/gpg/pubring.gpg" ) );
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
        InputStream signature = getClass().getResourceAsStream( "/gpg/test-input.txt.sig" );
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
        InputStream signature = getClass().getResourceAsStream( "/gpg/test-input.txt.asc" );
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

    public void testVerifyMultipleSignatureDetachedAsciiBothGood()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-both-good.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifyMultipleSignatureDetachedAsciiOneGoodOneBad()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-one-good-one-bad.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifyMultipleSignatureDetachedAsciiOneGoodOneMissing()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-one-good-one-missing.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifyMultipleSignatureDetachedAsciiOneBadOneGood()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-one-bad-one-good.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertFalse( "check it was not successful", status.isValid() );
    }

    /* Requires Bouncycastle 140 to work
    public void testVerifyMultipleSignatureDetachedAsciiOneMissingOneGood()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-one-missing-one-good.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }*/

    public void testVerifyMultipleSignatureDetachedAsciiBothMissing()
        throws IOException, OpenPgpException
    {
        try
        {
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-both-missing.asc" ),
                                              publicKeyRing );
            fail( "Expected failure due to missing keys" );
        }
        catch ( UnknownKeyException e )
        {
            assertTrue( true );
        }
    }

    public void testVerifyDualSignatureDetachedAsciiBothGood()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-dual-both-good.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifyDualSignatureDetachedAsciiOneGoodOneMissing()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature(
                                              getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream(
                                                                              "/gpg/test-input-dual-one-good-one-missing.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifyDualSignatureDetachedAsciiBad()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-dual-bad.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertFalse( "check it was not successful", status.isValid() );
    }

    public void testVerifyDualSignatureDetachedAsciiOneMissingOneGood()
        throws IOException, OpenPgpException
    {
        SignatureStatus status =
            verifier.verifyDetachedSignature(
                                              getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream(
                                                                              "/gpg/test-input-dual-one-missing-one-good.asc" ),
                                              publicKeyRing );

        assertNotNull( "check we got a status", status );
        assertTrue( "check it was successful", status.isValid() );
    }

    public void testVerifyDualSignatureDetachedAsciiBothMissing()
        throws IOException, OpenPgpException
    {
        try
        {
            verifier.verifyDetachedSignature( getClass().getResourceAsStream( FILE ),
                                              getClass().getResourceAsStream( "/gpg/test-input-dual-both-missing.asc" ),
                                              publicKeyRing );
            fail( "Expected failure due to missing keys" );
        }
        catch ( UnknownKeyException e )
        {
            assertTrue( true );
        }
    }
}
