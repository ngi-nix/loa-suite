package org.linkedopenactors.code.kvmadapter.config;

import java.net.MalformedURLException;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
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
public class RDF4JRepositoryManager {
	
	@Value("${app.rdf4jServer}")
	private String rdf4jServer;
	
	@Value("${app.repositoryIdKvm}")
	private String repositoryID;
	
	private RepositoryManager getRepositoryManager() throws MalformedURLException {
		RepositoryManager rm = new RemoteRepositoryManager(rdf4jServer);
		rm.init();
		return rm;
	}
	
	private Repository getKvmRepo() {
		Repository actorsRepository;
		try {
			actorsRepository = getRepositoryManager().getRepository(repositoryID);
		} catch (RepositoryConfigException | RepositoryException | MalformedURLException e) {
			throw new RuntimeException("rdf4jServer: " + rdf4jServer + " - repositoryID: " + repositoryID ,e);
		}
		return actorsRepository;
	}
	
	@Bean
	Namespace[] getNamespaces() {
		Namespace schema = new SimpleNamespace("schema", "http://schema.org/");
		Namespace as = new SimpleNamespace("as", "http://www.w3.org/ns/activitystreams#");
		Namespace kvm = new SimpleNamespace("kvm", "http://kvm.org/ns#");
		return new Namespace[] {schema, as, kvm};
	}
	
	@Bean
	@Qualifier("KvmPublicationRepo")
	public PublicationRepo getKvmPublicationRepo() {
//		Namespace schema = new SimpleNamespace("schema", "http://schema.org/");
//		Namespace as = new SimpleNamespace("as", "http://www.w3.org/ns/activitystreams#");
//		Namespace kvm = new SimpleNamespace("kvm", "http://kvm.org/ns#");
		return new PublicationRepo(getKvmRepo(), new Converter<>(PublicationLoa.class), getNamespaces());
	}
}
