package org.apache.maven.artifact.resolver.metadata;

public enum MetadataResolutionRequestTypeEnum
{
      tree(1)
    , graph(2)
    , noConflictGraph(3)
    , subGraph(4)
    ;

	private int id;

	// Constructor 
	MetadataResolutionRequestTypeEnum( int id )
	{
	  this.id = id;
	}

	int getId()
	{
	  return id;
	}
}
