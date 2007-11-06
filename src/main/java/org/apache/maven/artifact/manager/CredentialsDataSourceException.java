package org.apache.maven.artifact.manager;

public class CredentialsDataSourceException
    extends Exception
{

    public CredentialsDataSourceException()
    {
    }

    public CredentialsDataSourceException( String message )
    {
        super( message );
    }

    public CredentialsDataSourceException( Throwable cause )
    {
        super( cause );
    }

    public CredentialsDataSourceException( String message,
                                           Throwable cause )
    {
        super( message, cause );
    }

}
