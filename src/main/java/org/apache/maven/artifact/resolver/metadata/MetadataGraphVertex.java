package org.apache.maven.artifact.resolver.metadata;


/**
 * metadata graph vertice - just a wrapper around artifact's metadata
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 */

public class MetadataGraphVertex
{
    ArtifactMetadata md;

    public MetadataGraphVertex( ArtifactMetadata md )
    {
        super();
        this.md = md;
    }

    public ArtifactMetadata getMd()
    {
        return md;
    }

    public void setMd( ArtifactMetadata md )
    {
        this.md = md;
    }

	@Override
	public String toString()
	{
		return "["+ (md == null ? "no metadata" : md.toString()) + "]";
	}
}
