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
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPUtil;

/**
 * Bouncy Castle implementation of the OpenPGP key ring.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @todo password is not secure
 */
public class SecretKeyRing
{
    private final Map<Long,PGPSecretKey> pgpSec = new HashMap<Long, PGPSecretKey>();

    private char[] password;

    private static final long MASK = 0xFFFFFFFFL;

    public SecretKeyRing()
    {
    }

    public void addSecretKeyRing( InputStream secretKeyRingStream, char[] password )
        throws IOException, OpenPgpException
    {
        PGPObjectFactory pgpFact = new PGPObjectFactory( PGPUtil.getDecoderStream( secretKeyRingStream ) );
        Object obj;

        while ( ( obj = pgpFact.nextObject() ) != null )
        {
            if ( !( obj instanceof PGPSecretKeyRing ) )
            {
                throw new OpenPgpException( obj.getClass().getName() + " found where PGPSecretKeyRing expected" );
            }

            PGPSecretKeyRing pgpSecret = (PGPSecretKeyRing) obj;
            Long key = new Long( pgpSecret.getSecretKey().getKeyID() & MASK );

            pgpSec.put( key, pgpSecret.getSecretKey() );
        }

        this.password = password;
    }

    public char[] getPassword()
    {
        return password;
    }

    public PGPSecretKey getSecretKey( String keyId )
    {
        return (PGPSecretKey) pgpSec.get( Long.valueOf( keyId, 16 ) );
    }

    public PGPSecretKey getSecretKey( long keyId )
    {
        return (PGPSecretKey) pgpSec.get( new Long( keyId & MASK ) );
    }
}
