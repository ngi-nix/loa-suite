package org.linkedopenactors.code.wechangeadapter.config;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import de.naturzukunft.rdf4j.ommapper.Converter;

@Configuration
public class WeChangeAdapterConfig {

	@Value("${app.repositoryIdWeChange}")
	private String repositoryID;
	private RepositoryManager repositoryManager;

	public WeChangeAdapterConfig(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	@Bean
	@Qualifier("WeChangePublicationRepo")
	public PublicationRepo getWeChangePublicationRepo() {
		return new PublicationRepo(getRepo(), new Converter<>(PublicationLoa.class));
	}

	private Repository getRepo() {
		Repository actorsRepository;
		actorsRepository = repositoryManager.getRepository(repositoryID);
		if (actorsRepository == null) {
			RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(new NativeStoreConfig());
			RepositoryConfig repConfig = new RepositoryConfig(repositoryID, repositoryTypeSpec);
			repositoryManager.addRepositoryConfig(repConfig);
			actorsRepository = repositoryManager.getRepository(repositoryID);
			return actorsRepository;
		}
		return actorsRepository;
	}
}


