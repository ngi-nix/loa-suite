package de.naturzukunft.rdf4j.loarepository;

import java.util.Optional;

import org.eclipse.rdf4j.repository.Repository;

/**
 * This is the only place where all loa modules should get access to a rdf4j repository! 
 */
public interface LoaRepositoryManager {
	/**
	 * Create a repository
	 * @param repositoryId The id that the newly created repository should have.
	 * @return The newly created repository.
	 */
	Repository createRepo(String repositoryId);

	/**
	 * @param repositoryId
	 * @return The repository with the passed repositoryId. 
	 */
	Optional<Repository> getRepository(String repositoryId);
	
	/**
	 * @param repositoryId
	 * @return The repository with the passed repositoryId.
	 * @throws RuntimeException if there is no repository with the passed id.
	 */
	Repository getMandatoryRepository(String repositoryId);

	/**
	 * Removes the repository with the passed id.
	 * @param repositoryId
	 * @return True, if the repository is removed, otherwise false.
	 */
	boolean removeRepository(String repositoryId);
	
	/**
	 * There are a few things that has to be stored in a system repository. The system in this case is the loa-app.
	 * @return The system repository.
	 */
	Repository getSystemRepository();
	
	/**
	 * Adding a temporary repository, that is clean up by {@link TempRepositoryCleaner}.
	 * @param repositoryId
	 * @return The real id of the temporary repository, which is not the same but similar to the one passed. 
	 */
	String addTempRepository(String repositoryId);	
}
