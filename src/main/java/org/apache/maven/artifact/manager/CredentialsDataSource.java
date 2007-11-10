package org.apache.maven.artifact.manager;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

/**
 * A wrapper class around setting/retrieving/caching authentication
 * info by resource ID. Typical usage - accessing server authentication for
 * by it's ID
 *
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 */

public interface CredentialsDataSource
{
    public static final String ROLE = CredentialsDataSource.class.getName();

    /**
     * find, if not found, prompt and create Authentication info
     * for a given resource
     *
     * @param resourceId resource ID for which authentication is required
     * @return resource AuthenticationInfo. Should always exist.
     * @throws CredentialsDataSourceException
     */
    AuthenticationInfo get( String resourceId )
        throws CredentialsDataSourceException;

    /**
     * set, if not found, prompt and create Authentication info
     * for a given resource. This one uses the old password
     * member of AuthenticationInfo
     *
     * @param resourceId  resource ID for which authentication is required
     * @param authInfo    authentication info to set for the given resource ID
     * @param oldPassword old password if one exists or <code>null</code> if not needed
     *                    - you know for sure that it's a new auth of the existing auth
     *                    does not have a password, associated with the resource, or you want the
     *                    old password to be prompted
     * @return
     * @throws CredentialsDataSourceException
     */
    void set( CredentialsChangeRequest req )
        throws CredentialsDataSourceException;
}
