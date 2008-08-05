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

/**
 * Enumerated type indicating the status of data that was signed.
 * <p/>
 * Values:
 * <ul>
 * <li><code>VALID_TRUSTED</code></li>
 * <li><code>VALID_UNTRUSTED</code></li>
 * <li><code>INVALID</code></li>
 * </ul>
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @todo incorporate levels of trust
 */
public class SignatureStatus
{
    /**
     * Status that indicates the signature is valid, and from a trusted source.
     */
    public static SignatureStatus VALID_TRUSTED = new SignatureStatus( true, true );

    /**
     * Status that indicates the signature is valid, but from an unknown or untrusted source.
     */
    public static SignatureStatus VALID_UNTRUSTED = new SignatureStatus( true, false );

    /**
     * Status that indicates the signature is invalid.
     */
    public static SignatureStatus INVALID = new SignatureStatus( false, false );

    /**
     * Whether the signature is valid.
     */
    private final boolean valid;

    /**
     * Whether the signature is trusted.
     */
    private final boolean trusted;

    private SignatureStatus( boolean valid, boolean trusted )
    {
        this.valid = valid;
        this.trusted = trusted;
    }

    public boolean isValid()
    {
        return valid;
    }

    public boolean isTrusted()
    {
        return trusted;
    }
}
