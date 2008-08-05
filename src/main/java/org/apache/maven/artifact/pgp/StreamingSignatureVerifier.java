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

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;

/**
 * Bouncy Castle implementation of the OpenPGP signer.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class StreamingSignatureVerifier
{
    private PGPSignature sig;

    public StreamingSignatureVerifier( InputStream signature, PublicKeyRing keyRing )
        throws OpenPgpException, IOException
    {
        init( signature, keyRing );
    }

    private void init( InputStream signature, PublicKeyRing keyRing )
        throws OpenPgpException, IOException
    {
        // TODO: better location for this?
        Security.addProvider( new BouncyCastleProvider() );

        try
        {
            signature = PGPUtil.getDecoderStream( signature );

            PGPPublicKey key = null;
            while ( key == null && signature.available() > 0 )
            {
                PGPObjectFactory pgpFact = new PGPObjectFactory( signature );

                PGPSignatureList p3;

                Object o = pgpFact.nextObject();
                if ( o == null )
                {
                    break;
                }
                
                if ( o instanceof PGPCompressedData )
                {
                    PGPCompressedData c1 = (PGPCompressedData) o;

                    pgpFact = new PGPObjectFactory( c1.getDataStream() );

                    p3 = (PGPSignatureList) pgpFact.nextObject();
                }
                else
                {
                    p3 = (PGPSignatureList) o;
                }

                for ( int i = 0; i < p3.size(); i++ )
                {
                    sig = p3.get( i );
                    key = keyRing.getPublicKey( sig.getKeyID() );
                    if ( key != null )
                    {
                        break;
                    }
                    else
                    {
                        // TODO: log them all
                    }
                }

            }

            if ( key == null )
            {
                throw new UnknownKeyException( "Unable to find key with key ID '"
                    + Long.toHexString( sig.getKeyID() ).toUpperCase() + "' in public key ring" );
            }

            sig.initVerify( key, "BC" );
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
            sig.update( buf, offset, length );
        }
        catch ( SignatureException e )
        {
            throw new OpenPgpException( "Error calculating detached signature: " + e.getMessage(), e );
        }
    }

    public SignatureStatus finish()
        throws OpenPgpException, IOException
    {
        try
        {
            if ( sig.verify() )
            {
                // TODO: how do we assess trust?
                return SignatureStatus.VALID_UNTRUSTED;
            }
            else
            {
                return SignatureStatus.INVALID;
            }
        }
        catch ( PGPException e )
        {
            throw new OpenPgpException( "Error calculating detached signature: " + e.getMessage(), e );
        }
        catch ( SignatureException e )
        {
            throw new OpenPgpException( "Error calculating detached signature: " + e.getMessage(), e );
        }
    }
}
