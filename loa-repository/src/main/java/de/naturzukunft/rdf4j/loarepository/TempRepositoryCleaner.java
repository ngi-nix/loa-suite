package de.naturzukunft.rdf4j.loarepository;

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
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link LoaRepositoryManager#addTempRepository(String)} gives you the possibility to add a temporary repository.
 * {@link TempRepositoryCleaner} hast the responsibility to cleanup this temporary repositories.
 */
@Component
@Slf4j
public class TempRepositoryCleaner {
		
	private IRI tempRepoType;
	private static final int SECOND = 1000;
	private static final int MINUTE = SECOND * 60;
	private static final int HOUR = MINUTE * 60;
	private LoaRepositoryManager loaRepositoryManager;
			
	public TempRepositoryCleaner(LoaRepositoryManager loaRepositoryManager) {
		this.loaRepositoryManager = loaRepositoryManager;
	}
	
	@Scheduled(fixedRate = HOUR*6, initialDelay = 5000)
	public void tempRepoCleaner() {
		tempRepoCleaner(LocalDateTime.now().minusDays(5));
	}
	
	public void tempRepoCleaner(LocalDateTime maxAge) {
		Model repos = new ModelBuilder().build();
		try(RepositoryConnection con = loaRepositoryManager.getSystemRepository().getConnection()) {
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
				loaRepositoryManager.removeRepository(repoId.stringValue());
				log.info("temp repo " + repoId.stringValue() + " deleted");
				}
			});
		}
	}
}
