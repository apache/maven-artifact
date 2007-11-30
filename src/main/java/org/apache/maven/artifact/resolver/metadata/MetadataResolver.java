package org.apache.maven.artifact.resolver.metadata;


/**
 * entry point into metadata resolution component
 * 
 * @author <a href="oleg@codehaus.org">Oleg Gusakov</a>
 *
 */
public interface MetadataResolver
{
    String ROLE = MetadataResolver.class.getName();

    /**
     * collect all dependency metadata into one "dirty" graph
     * 
     * @param request
     * @return
     * @throws MetadataResolutionException
     */
    MetadataResolutionResult resolveMetadata( MetadataResolutionRequest request )
        throws MetadataResolutionException;
}
