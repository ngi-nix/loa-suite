package org.linkedopenactors.code.kvmadapter.config;

import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import de.naturzukunft.rdf4j.ommapper.Converter;

@Configuration
public class KvmRDF4JRepositoryManager {
	
	@Value("${app.repositoryIdKvm}")
	private String repositoryID;

	private LoaRepositoryManager loaRepositoryManager;

	public KvmRDF4JRepositoryManager(LoaRepositoryManager loaRepositoryManager) {
		this.loaRepositoryManager = loaRepositoryManager;
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
		return new PublicationRepo(
				loaRepositoryManager.getRepository(repositoryID).orElse(loaRepositoryManager.createRepo(repositoryID)),
				new Converter<>(PublicationLoa.class), getNamespaces());
	}
}
