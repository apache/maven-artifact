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

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;

/**
 * Bouncy Castle implementation of the OpenPGP signer.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class StreamingSigner
{
    private static final String PROVIDER = "BC";

    private PGPSignatureGenerator sGen;

    private final ByteArrayOutputStream signatureBytes;

    private BCPGOutputStream bOut;

    public StreamingSigner( String keyId, SecretKeyRing keyRing, boolean asciiArmor )
        throws OpenPgpException
    {
        signatureBytes = new ByteArrayOutputStream();
        init( asciiArmor, signatureBytes, keyRing, keyId );
    }

    public StreamingSigner( OutputStream signature, String keyId, SecretKeyRing keyRing, boolean asciiArmor )
        throws OpenPgpException
    {
        signatureBytes = null;
        init( asciiArmor, signature, keyRing, keyId );
    }

    private void init( boolean asciiArmor, OutputStream signature, SecretKeyRing keyRing, String keyId )
        throws OpenPgpException
    {
        // TODO: better location for this?
        Security.addProvider( new BouncyCastleProvider() );

        OutputStream out;
        if ( asciiArmor )
        {
            out = new ArmoredOutputStream( signature );
        }
        else
        {
            out = signature;
        }
        bOut = new BCPGOutputStream( out );

        try
        {
            PGPSecretKey pgpSec = keyRing.getSecretKey( keyId );
            PGPPrivateKey pgpPrivKey = pgpSec.extractPrivateKey( keyRing.getPassword(), PROVIDER );
            sGen = new PGPSignatureGenerator( pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA1, PROVIDER );
            sGen.initSign( PGPSignature.BINARY_DOCUMENT, pgpPrivKey );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new OpenPgpException(
                                        "Unable to find the correct algorithm for PGP - check that the Bouncy Castle provider is correctly installed",
                                        e );
        }
        catch ( NoSuchProviderException e )
        {
            throw new OpenPgpException(
                                        "Unable to find the correct provider for PGP - check that the Bouncy Castle provider is correctly installed",
                                        e );
        }
        catch ( PGPException e )
        {
            throw new OpenPgpException( "Error calculating detached signature: " + e.getMessage(), e );
        }
    }

    public void update( byte[] buf )
        throws OpenPgpException
    {
        update( buf, 0, buf.length );
    }

    public void update( byte[] buf, int offset, int length )
        throws OpenPgpException
    {
        try
        {
            sGen.update( buf, offset, length );
        }
        catch ( SignatureException e )
        {
            throw new OpenPgpException( "Error calculating detached signature: " + e.getMessage(), e );
        }
    }

    public byte[] finish()
        throws OpenPgpException, IOException
    {
        try
        {
            sGen.generate().encode( bOut );
        }
        catch ( PGPException e )
        {
            throw new OpenPgpException( "Error calculating detached signature: " + e.getMessage(), e );
        }
        catch ( SignatureException e )
        {
            throw new OpenPgpException( "Error calculating detached signature: " + e.getMessage(), e );
        }
        bOut.close();
        return signatureBytes != null ? signatureBytes.toByteArray() : null;
    }
}
