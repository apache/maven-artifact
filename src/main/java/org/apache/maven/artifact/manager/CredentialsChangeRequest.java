package org.apache.maven.artifact.manager;

import org.apache.maven.wagon.authentication.AuthenticationInfo;

public class CredentialsChangeRequest
{
    private String resourceId;
    private AuthenticationInfo authInfo;
    private String oldPassword;

    public CredentialsChangeRequest()
    {
    }

    public CredentialsChangeRequest( String resourceId,
                                     AuthenticationInfo authInfo,
                                     String oldPassword )
    {
        super();
        this.resourceId = resourceId;
        this.authInfo = authInfo;
        this.oldPassword = oldPassword;
    }

    public String getResourceId()
    {
        return resourceId;
    }

    public void setResourceId( String resourceId )
    {
        this.resourceId = resourceId;
    }

    public AuthenticationInfo getAuthInfo()
    {
        return authInfo;
    }

    public void setAuthInfo( AuthenticationInfo authInfo )
    {
        this.authInfo = authInfo;
    }

    public String getOldPassword()
    {
        return oldPassword;
    }

    public void setOldPassword( String oldPassword )
    {
        this.oldPassword = oldPassword;
    }


}
