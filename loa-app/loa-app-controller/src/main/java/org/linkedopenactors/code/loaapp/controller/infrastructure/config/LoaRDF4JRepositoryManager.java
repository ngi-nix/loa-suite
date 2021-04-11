package org.linkedopenactors.code.loaapp.controller.infrastructure.config;

import java.net.MalformedURLException;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.config.RepositoryConfigException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoaRDF4JRepositoryManager {
	
	@Value("${app.rdf4jServer}")
	private String rdf4jServer;
	
	@Value("${app.repositoryIdKvm}")
	private String repositoryID;
	
	private RepositoryManager getRepositoryManager() throws MalformedURLException {
		RepositoryManager rm = new RemoteRepositoryManager(rdf4jServer);
		rm.init();
		return rm;
	}
	
	public Repository getKvmRepo() {
		Repository actorsRepository;
		try {
			actorsRepository = getRepositoryManager().getRepository(repositoryID);
		} catch (RepositoryConfigException | RepositoryException | MalformedURLException e) {
			throw new RuntimeException(e);
		}
		return actorsRepository;
	}

	// kommt vom kvm-adapter!
//	@Bean
//	@Qualifier("KvmPublicationRepo")
//	public PublicationRepo getKvmPublicationRepo() {
//		return new PublicationRepo(getKvmRepo(), new Converter<>(PublicationLoa.class));
//	}
}
