package de.naturzukunft.rdf4j.loarepository;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
class RepositoryManagerLocal implements LoaRepositoryManager  {

	private static final String LOA_SYSTEM_REPO = "loa_system_repo";
	private RepositoryManager repositoryManager;
	private String baseSubject;
	private IRI tempRepoType;
	
	public RepositoryManagerLocal(@Value("${app.baseSubjectSystem}") String baseSubject, RepositoryManager repositoryManager) {
		this.baseSubject = baseSubject;
		tempRepoType = iri(baseSubject + "tempRepo");
		this.repositoryManager = repositoryManager;
	}

	@Override
	public Repository createRepo(String repositoryId) {
		Repository created = repositoryManager.getRepository(repositoryId);
		if(created != null) {
			return created;
		}
		RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(new NativeStoreConfig());
		RepositoryConfig repConfig = new RepositoryConfig(repositoryId, repositoryTypeSpec);
		repositoryManager.addRepositoryConfig(repConfig);
		return repositoryManager.getRepository(repositoryId);
	}

	@Override
	public Optional<Repository> getRepository(String repositoryId) {
		Optional<Repository> repository = Optional.ofNullable(repositoryManager.getRepository(repositoryId));
		log.trace("getRepository("+repositoryId+") -> " +repository);
		return repository;
	}

	@Override
	public boolean removeRepository(String repositoryId) {
		return repositoryManager.removeRepository(repositoryId);
	}

	@Override
	public Repository getMandatoryRepository(String repositoryId) {
		return getRepository(repositoryId).orElseThrow(()->new RuntimeException("cannot find mandatory repository with id: " + repositoryId));
	}
	
	@Override
	public Repository getSystemRepository() {
		Optional<Repository> repoOptional = getRepository(LOA_SYSTEM_REPO);
		if(repoOptional.isEmpty()) {
			createRepo(LOA_SYSTEM_REPO);
			repoOptional = getRepository(LOA_SYSTEM_REPO);
		}
		return repoOptional.orElseThrow(()->new RuntimeException("error determining repo: " + LOA_SYSTEM_REPO));
	}	
	
	@Override
	public String addTempRepository(String repositoryId) {
		String tempRepositoryId = "tmp_" + System.currentTimeMillis() + "_" + repositoryId;
		createRepo(tempRepositoryId );
		Model m = new ModelBuilder()
				.subject(baseSubject+tempRepositoryId)
				.add(RDF.TYPE, tempRepoType)
				.add(SCHEMA_ORG.name, literal(tempRepositoryId))
				.add(SCHEMA_ORG.dateCreated, literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())))
				.build();		
		try(RepositoryConnection con = getSystemRepository().getConnection()) {
			con.add(m);
		}
		return tempRepositoryId;
	}
}
