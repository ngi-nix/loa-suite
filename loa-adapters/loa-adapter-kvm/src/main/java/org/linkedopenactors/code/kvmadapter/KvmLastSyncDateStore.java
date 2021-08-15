package org.linkedopenactors.code.kvmadapter;

import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;

public class KvmLastSyncDateStore implements LastSyncDateStore {

	private Repository repository;

	public KvmLastSyncDateStore(Repository repository) {
		this.repository = repository;
	}
	
	@Override
	public void lastSync(IRI subject, LocalDateTime lastSyncDate) {
		try(RepositoryConnection con = repository.getConnection()) {
			RepositoryResult<Statement> res = con.getStatements(subject, Values.iri("http://linkedopenactors.ord/lastSyncDate"), null);
			con.remove(res);			
			con.add(subject, Values.iri("http://linkedopenactors.ord/lastSyncDate"), Values.literal(lastSyncDate));
		}
	}
	
	@Override
	public Optional<LocalDateTime> lastSyncDate(IRI subject) {
		try(RepositoryConnection con = repository.getConnection()) {
			RepositoryResult<Statement> res = con.getStatements(subject, Values.iri("http://linkedopenactors.ord/lastSyncDate"), null);
			
			return res.stream().findFirst()
					.map(stmt->stmt.getObject())
					.map(it->((Literal)it).temporalAccessorValue())
					.map(LocalDateTime::from);
		}
	}
}
