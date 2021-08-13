package de.naturzukunft.rdf4j.loarepository;

import java.util.Optional;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class RepositoryManagerLocal implements LoaRepositoryManager  {

	private RepositoryManager repositoryManager;

	public RepositoryManagerLocal(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	@Override
	public Repository createRepo(String repositoryId) {
		Repository created = repositoryManager.getRepository(repositoryId);
		if(created != null) {
			return created;
		}
		RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(new NativeStoreConfig());
		RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
		repositoryManager.addRepositoryConfig(repConfig);
		return repositoryManager.getRepository(repositoryId);
	}

	@Override
	public Optional<Repository> getRepository(String repositoryId) {
		Optional<Repository> repository = Optional.ofNullable(repositoryManager.getRepository(repositoryId));
		log.trace("getRepository("+repositoryId+") -> " +repository);
		return repository;
	}

	@Override
	public boolean removeRepository(String repositoryId) {
		return repositoryManager.removeRepository(repositoryId);
	}
}
