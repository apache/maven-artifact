package org.apache.maven.artifact.resolver.metadata;

/** @author Oleg Gusakov */
public class MetadataGraphVertice
{
	ArtifactMetadata md;

	public MetadataGraphVertice(ArtifactMetadata md)
	{
		super();
		this.md = md;
	}

	public ArtifactMetadata getMd()
	{
		return md;
	}

	public void setMd(ArtifactMetadata md)
	{
		this.md = md;
	}

}
