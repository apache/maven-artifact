package org.apache.maven.artifact.manager;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;

public interface UpdateCheckManager {

	String ROLE = UpdateCheckManager.class.getName();

	boolean isUpdateRequired( Artifact artifact, ArtifactRepository repository );

	void touch( Artifact artifact, ArtifactRepository repository );

    boolean isUpdateRequired( RepositoryMetadata metadata, ArtifactRepository repository, File file );

	void touch( RepositoryMetadata metadata, ArtifactRepository repository, File file );

}
