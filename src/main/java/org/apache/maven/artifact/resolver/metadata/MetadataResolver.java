package org.apache.maven.artifact.resolver.metadata;

/** @author Jason van Zyl */
public interface MetadataResolver
{
    String ROLE = MetadataResolver.class.getName();

    MetadataResolutionResult resolveMetadata( MetadataResolutionRequest request );    
}
