package org.linkedopenactors.code.wechangeadapter.config;

import java.net.MalformedURLException;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import de.naturzukunft.rdf4j.ommapper.Converter;

@Configuration
public class WeChangeAdapterConfig {

	@Value("${app.rdf4jServer}")
	private String rdf4jServer;
	
	@Value("${app.repositoryIdWeChange}")
	private String repositoryID;

//	@Bean
//	public WebClient getWebClient() {
//		return WebClient.builder().build();
//	}

	@Bean
	@Qualifier("WeChangePublicationRepo")
	public PublicationRepo getWeChangePublicationRepo() {
		return new PublicationRepo(getRepo(), new Converter<>(PublicationLoa.class));
	}

	private RepositoryManager getRepositoryManager() throws MalformedURLException {
		RepositoryManager rm = new RemoteRepositoryManager(rdf4jServer);
		rm.init();
		return rm;
	}

	private Repository getRepo() {
		Repository actorsRepository;
		try {
			actorsRepository = getRepositoryManager().getRepository(repositoryID);
		} catch (RepositoryConfigException | RepositoryException | MalformedURLException e) {
			throw new RuntimeException(e);
		}
		if(actorsRepository==null) {
			throw new RuntimeException("cannot initialize repository with id: " + repositoryID);
		}
		return actorsRepository;
	}

}


