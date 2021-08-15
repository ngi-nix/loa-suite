package org.linkedopenactors.code.kvmadapter.config;

import org.eclipse.rdf4j.repository.Repository;
import org.linkedopenactors.code.kvmadapter.KvmLastSyncDateStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;

@Configuration
public class KvmAdapterConfig {
	
	private Repository repository;

	public KvmAdapterConfig(LoaRepositoryManager loaRepositoryManager, @Value("${app.repositoryIdKvm}") String repositoryID) {
		this.repository = loaRepositoryManager.getRepository(repositoryID).orElse(loaRepositoryManager.createRepo(repositoryID));
	}
	
	@Bean
	@Qualifier("KvmLastSyncDateStore")	
	public LastSyncDateStore getKvmLastSyncDateStore() {
		return new KvmLastSyncDateStore(repository);
	}
}
