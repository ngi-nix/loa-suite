package org.linkedopenactors.code.loaapp.controller.infrastructure.config;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoaRDF4JRepositoryManager {
	
	@Value("${app.repositoryIdKvm}")
	private String repositoryID;
	private RepositoryManager repositoryManager;
	
	public LoaRDF4JRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	public Repository getKvmRepo() {
		return repositoryManager.getRepository(repositoryID);
	}
}
