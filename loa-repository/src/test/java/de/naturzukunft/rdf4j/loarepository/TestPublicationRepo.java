package de.naturzukunft.rdf4j.loarepository;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.jupiter.api.Test;

import de.naturzukunft.rdf4j.ommapper.Converter;
import de.naturzukunft.rdf4j.vocabulary.AS;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;

class TestPublicationRepo {

	@Test
	void test() {
		Converter<PublicationLoa> converter = new Converter<PublicationLoa>(PublicationLoa.class);
		SailRepository repository = new SailRepository(new MemoryStore());
		PublicationRepo repo = new PublicationRepo(repository, converter);
		
		IRI organsiation1 = iri("http://xyz.de/org1");
		OrgansationLoa orgnisation = OrgansationLoa.builder()
			.subject(organsiation1)
			.type(Set.of(SCHEMA_ORG.Organization))
			.asName("testOrganisation")
			.build();
		
		IRI publication1 = iri("http://xyz.de/publ1");
		PublicationLoa p = PublicationLoa.builder()
			.subject(publication1)
			.type(Set.of(SCHEMA_ORG.CreativeWork))
			.asName("testPublication")
			.about(orgnisation)
			.build();
		
		repo.save(p);
		
		Model organisation = null;
		Model publication = null;
		try(RepositoryConnection con = repository.getConnection()) {
			RepositoryResult<Statement> statements = con.getStatements(organsiation1, null, null);
			organisation = new ModelBuilder().build();
			organisation.addAll(Iterations.asSet(statements));
			statements = con.getStatements(publication1, null, null);
			publication = new ModelBuilder().build();
			publication.addAll(Iterations.asSet(statements));			
		}
		assertEquals(Set.of(SCHEMA_ORG.CreativeWork), Models.getPropertyIRIs(publication, publication1, RDF.TYPE));
		assertEquals("testPublication", Models.getPropertyLiteral(publication, publication1, AS.name).get().stringValue());
		
		assertEquals(Set.of(SCHEMA_ORG.Organization), Models.getPropertyIRIs(organisation, organsiation1, RDF.TYPE));
		assertEquals("testOrganisation", Models.getPropertyLiteral(organisation, organsiation1, AS.name).get().stringValue());
	}
}
