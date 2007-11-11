package org.apache.maven.artifact;

public enum ArtifactScopeEnum
{
    compile( 1 ), test( 2 ), runtime( 3 ), provided( 4 ), system( 5 );

    public static final ArtifactScopeEnum DEFAULT_SCOPE = compile;

    private int id;

    // Constructor
    ArtifactScopeEnum( int id )
    {
        this.id = id;
    }

    int getId()
    {
        return id;
    }

    public String getScope()
    {
        if ( id == 1 )
        {
            return Artifact.SCOPE_COMPILE;
        }
        else if ( id == 2 )
        {
            return Artifact.SCOPE_TEST;

        }
        else if ( id == 3 )
        {
            return Artifact.SCOPE_RUNTIME;

        }
        else if ( id == 4 )
        {
            return Artifact.SCOPE_PROVIDED;
        }
        else
        {
            return Artifact.SCOPE_SYSTEM;
        }
    }
}
