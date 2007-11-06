package org.apache.maven.artifact.resolver.metadata;

/** @author Oleg Gusakov */
public interface MetadataResolver
{
    String ROLE = MetadataResolver.class.getName();

    MetadataResolutionResult resolveMetadata( MetadataResolutionRequest request )
        throws MetadataResolutionException;
}
