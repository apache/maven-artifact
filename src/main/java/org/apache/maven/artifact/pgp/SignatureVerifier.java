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

/**
 * Verify signatures using the Bouncy Castle OpenPGP provider.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class SignatureVerifier
{
    private static final int BUFFER_SIZE = 1024;

    public SignatureStatus verifyDetachedSignature( InputStream data, InputStream signature, PublicKeyRing keyRing )
        throws OpenPgpException, UnknownKeyException, IOException
    {
        StreamingSignatureVerifier verifier = new StreamingSignatureVerifier( signature, keyRing );

        byte[] buf = new byte[BUFFER_SIZE];

        int len;
        do
        {
            len = data.read( buf );
            if ( len > 0 )
            {
                verifier.update( buf, 0, len );
            }
        }
        while ( len >= 0 );

        return verifier.finish();
    }
}
