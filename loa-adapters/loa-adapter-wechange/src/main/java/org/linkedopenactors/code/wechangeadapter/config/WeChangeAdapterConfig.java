package org.linkedopenactors.code.wechangeadapter.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import de.naturzukunft.rdf4j.ommapper.Converter;

@Configuration
public class WeChangeAdapterConfig {

	@Value("${app.repositoryIdWeChange}")
	private String repositoryID;
	
	private LoaRepositoryManager loaRepositoryManager;

	public WeChangeAdapterConfig(LoaRepositoryManager loaRepositoryManager) {
		this.loaRepositoryManager = loaRepositoryManager;		
	}
	
	@Bean
	@Qualifier("WeChangePublicationRepo")
	public PublicationRepo getWeChangePublicationRepo() {
		return new PublicationRepo(
				loaRepositoryManager.getRepository(repositoryID).orElse(loaRepositoryManager.createRepo(repositoryID)),
				new Converter<>(PublicationLoa.class));
	}
}


