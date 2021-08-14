package org.linkedopenactors.code.osmadapter.config;

import org.eclipse.rdf4j.repository.Repository;
import org.linkedopenactors.code.osmadapter.OsmLastSyncDateStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;

@Configuration
public class OsmAdapterConfig {

	private Repository repository;

	public OsmAdapterConfig(LoaRepositoryManager loaRepositoryManager, @Value("${app.repositoryIdOsm}") String repositoryID) {
		this.repository = loaRepositoryManager.getRepository(repositoryID).orElse(loaRepositoryManager.createRepo(repositoryID));
	}

	@Bean
	@Qualifier("OsmLastSyncDateStore")
	public LastSyncDateStore getOsmLastSyncDateStore() {
		return new OsmLastSyncDateStore(repository);
	}
}
