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

import junit.framework.TestCase;

/**
 * Test the open pgp key ring.
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class PublicKeyRingTest
    extends TestCase
{
    private String[] pubKeyId = { "A7D16BD4" };

    private PublicKeyRing keyRing;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        keyRing = new PublicKeyRing();
        keyRing.addPublicKeyRing( getClass().getResourceAsStream( "/gpg/pubring.gpg" ) );
    }

    public void testPublicKeys()
        throws OpenPgpException, IOException
    {
        for ( int i = 0; i < pubKeyId.length; i++ )
        {
            assertNotNull( "Unable to find key " + pubKeyId[i], keyRing.getPublicKey( pubKeyId[i] ) );
        }
    }
}
