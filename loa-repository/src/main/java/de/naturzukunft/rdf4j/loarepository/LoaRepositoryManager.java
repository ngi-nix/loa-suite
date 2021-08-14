package de.naturzukunft.rdf4j.loarepository;

import java.util.Optional;

import org.eclipse.rdf4j.repository.Repository;

public interface LoaRepositoryManager {
	Repository createRepo(String repositoryId);

	Optional<Repository> getRepository(String repositoryId);

	boolean removeRepository(String repositoryId);
}
