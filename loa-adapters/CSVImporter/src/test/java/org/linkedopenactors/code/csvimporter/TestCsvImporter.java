package org.linkedopenactors.code.csvimporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;

import de.naturzukunft.rdf4j.loarepository.SystemRepository;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;

class TestCsvImporter {

	@Test
	void test() throws IOException {
		Repository repo = new SailRepository(new MemoryStore());
		SystemRepository systemRepositoryMock = Mockito.mock(SystemRepository.class);
		when(systemRepositoryMock.addTempRepository(Mockito.anyString())).thenReturn("sampleName");
		when(systemRepositoryMock.getRepository("sampleName")).thenReturn(repo);
		
		CsvImporter toTest = new CsvImporter(systemRepositoryMock);
		ClassPathResource cpr = new ClassPathResource("pubs.csv");
		String repoId = toTest.importCsvInternal("name", cpr.getInputStream());
		assertNotNull(repoId);
		
		Model m = new ModelBuilder().build();
		try(RepositoryConnection con = repo.getConnection()) {
			RepositoryResult<Statement> stmts = con.getStatements(null, null, null);			
			m.addAll(Iterations.asSet(stmts));
		}
		m.getStatements(null, null, null).forEach(System.out::println);
		Model orgaFiltered = m.filter(null, RDF.TYPE, SCHEMA_ORG.Organization);
		IRI subject = (IRI)orgaFiltered.stream().findFirst().get().getSubject();
		String orgName = Models.getPropertyLiteral(m, subject, SCHEMA_ORG.name).get().stringValue();
		assertEquals("basic Trudering", orgName);
	}
}
