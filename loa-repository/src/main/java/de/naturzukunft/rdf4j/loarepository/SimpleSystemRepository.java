package de.naturzukunft.rdf4j.loarepository;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.sail.memory.config.MemoryStoreConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SimpleSystemRepository implements SystemRepository {
		
	private String baseSubject;	
	private IRI tempRepoType;
	private static final int SECOND = 1000;
	private static final int MINUTE = SECOND * 60;
	private static final int HOUR = MINUTE * 60;
	private static final String LOA_SYSTEM_REPO = "loa_system_repo";
	private static final String RDF4J_SERVER = "https://rdf.dev.osalliance.com/rdf4j-server";	
	private RemoteRepositoryManager manager; 
			
	public SimpleSystemRepository(@Value("${app.baseSubjectSystem}") String baseSubject) {
		this.baseSubject = baseSubject;
		manager = new RemoteRepositoryManager(RDF4J_SERVER);
		manager.init();		
		tempRepoType = iri(baseSubject + "tempRepo");
	}
	
	@Override
	public String addTempRepository(String repositoryId) {
		String tempRepositoryId = "tmp_" + System.currentTimeMillis() + "_" + repositoryId;
		addRepository(tempRepositoryId );
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
	
	@Scheduled(fixedRate = HOUR*6, initialDelay = 5000)
	public void tempRepoCleaner() {
		tempRepoCleaner(LocalDateTime.now().minusDays(5));
	}
	
	public void tempRepoCleaner(LocalDateTime maxAge) {
		Model repos = new ModelBuilder().build();
		try(RepositoryConnection con = getSystemRepository().getConnection()) {
			RepositoryResult<Statement> repoTypes = con.getStatements(null, RDF.TYPE, tempRepoType);
			repoTypes.forEach(stmt->{
				RepositoryResult<Statement> repoStatements = con.getStatements(stmt.getSubject(), null, null);
				repos.addAll(Iterations.asSet(repoStatements));
			});
		Model createdDateStmts = repos.filter(null, SCHEMA_ORG.dateCreated, null);
		createdDateStmts.forEach(repo->{			
			boolean shouldBeDeleted = LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(repo.getObject().stringValue())).isBefore(maxAge);
			if(shouldBeDeleted) {
				Literal repoId = Models.getPropertyLiteral(repos, repo.getSubject(), SCHEMA_ORG.name).stream().findFirst().orElseThrow(()->new RuntimeException("unable to delete repo " + repo.getSubject()));
				con.remove(repo.getSubject(), null, null);				
				manager.removeRepository(repoId.stringValue());
				log.info("temp repo " + repoId.stringValue() + " deleted");
				}
			});
		}
	}
	
	private Repository addRepository(String repositoryId) {
		RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(new MemoryStoreConfig(true));
		manager.addRepositoryConfig(new RepositoryConfig(repositoryId, repositoryTypeSpec));
		return manager.getRepository(repositoryId);
	}

	private Repository getSystemRepository() {
		Repository repo = manager.getRepository(LOA_SYSTEM_REPO);
		if(repo==null) {
			addRepository(LOA_SYSTEM_REPO);
			repo = manager.getRepository(LOA_SYSTEM_REPO);
		}
		return repo;
	}

	@Override
	public Repository getRepository(String repositoryId) {
		return manager.getRepository(repositoryId);
	}
}
