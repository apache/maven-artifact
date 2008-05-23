package org.apache.maven.artifact.resolver.metadata.conflict;

import org.apache.maven.artifact.resolver.metadata.resolver.MetadataGraphEdge;

/**
 *  MetadataGraph edge selection policy. Complements 
 *  GraphConflictResolver by being injected into it
 * 
 * @author <a href="mailto:oleg@codehaus.org">Oleg Gusakov</a>
 * 
 * @version $Id$
 */

public interface ConflictResolutionPolicy
{
    static String ROLE = ConflictResolutionPolicy.class.getName();

    public MetadataGraphEdge apply( 
			  MetadataGraphEdge e1
			, MetadataGraphEdge e2
			);
}
