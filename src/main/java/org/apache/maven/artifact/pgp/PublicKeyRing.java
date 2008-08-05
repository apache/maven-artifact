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
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;

/**
 * Bouncy Castle implementation of the OpenPGP key ring.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class PublicKeyRing
{
    private final Map<Long,PGPPublicKey> keyRingMap = new HashMap<Long,PGPPublicKey>();

    private static final long MASK = 0xFFFFFFFFL;

    public PublicKeyRing()
    {
    }
    
    public void addPublicKeyRing( InputStream publicKeyRingStream )
        throws IOException, OpenPgpException
    {
        PGPObjectFactory pgpFact = new PGPObjectFactory( PGPUtil.getDecoderStream( publicKeyRingStream ) );
        Object obj;

        while ( ( obj = pgpFact.nextObject() ) != null )
        {
            if ( !( obj instanceof PGPPublicKeyRing ) )
            {
                throw new OpenPgpException( "Invalid key ring (" + obj.getClass().getName()
                    + " found where PGPPublicKeyRing expected)" );
            }

            PGPPublicKeyRing keyRing = (PGPPublicKeyRing) obj;
            Long key = new Long( keyRing.getPublicKey().getKeyID() & MASK );

            this.keyRingMap.put( key, keyRing.getPublicKey() );
        }
    }

    PGPPublicKey getPublicKey( String keyId )
    {
        return (PGPPublicKey) keyRingMap.get( Long.valueOf( keyId, 16 ) );
    }

    PGPPublicKey getPublicKey( long keyId )
    {
        return (PGPPublicKey) keyRingMap.get( new Long( keyId & MASK ) );
    }
}
