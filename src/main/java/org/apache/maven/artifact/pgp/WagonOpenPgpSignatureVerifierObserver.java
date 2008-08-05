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

import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.observers.AbstractTransferListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * Listener to the download process that can verify the artifact.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class WagonOpenPgpSignatureVerifierObserver
    extends AbstractTransferListener
{
    private final StreamingSignatureVerifier verifier;

    private SignatureStatus status;

    private Exception failure;

    public WagonOpenPgpSignatureVerifierObserver( InputStream signatureInputStream, PublicKeyRing keyRing )
        throws OpenPgpException, IOException
    {
        verifier = new StreamingSignatureVerifier( signatureInputStream, keyRing );
    }

    public void transferInitiated( TransferEvent transferEvent )
    {
        this.status = null;
        this.failure = null;
    }

    public void transferProgress( TransferEvent transferEvent, byte[] buffer, int length )
    {
        if ( failure == null )
        {
            try
            {
                verifier.update( buffer, 0, length );
            }
            catch ( OpenPgpException e )
            {
                failure = e;
            }
        }
    }

    public void transferCompleted( TransferEvent transferEvent )
    {
        if ( failure == null )
        {
            try
            {
                status = verifier.finish();
            }
            catch ( OpenPgpException e )
            {
                failure = e;
            }
            catch ( IOException e )
            {
                failure = e;
            }
        }
    }

    public SignatureStatus getStatus()
    {
        return status;
    }
    
    public Exception getFailure()
    {
        return failure;
    }
}
