package org.linkedopenactors.code.loaapp;

import java.net.MalformedURLException;

import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RDF4JRepositoryManager {
	
	@Value("${app.rdfRepositoryHome}")
	private String rdfRepositoryHome;

	@Bean
	public RepositoryManager getRepositoryManager() throws MalformedURLException {
		RepositoryManager rm = RepositoryProvider.getRepositoryManager(rdfRepositoryHome);
		rm.init();
		return rm;
	}
}
