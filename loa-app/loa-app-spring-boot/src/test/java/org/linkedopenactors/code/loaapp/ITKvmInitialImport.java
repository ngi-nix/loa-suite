package org.linkedopenactors.code.loaapp;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.repository.util.Repositories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.reactive.server.WebTestClient;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@Slf4j
public class ITKvmInitialImport {

	@Autowired
	private WebTestClient webTestClient;
	 
	@Test
	public void testGetByIRI() throws Exception {
		webTestClient
        .mutate()
        .responseTimeout(Duration.ofMinutes(2))
        .build()
			.get()			
			.uri("/kvm/initialLoad")
			.exchange()
			.expectStatus()
			.is2xxSuccessful()
			.expectBody(String.class)
			.consumeWith(response->{
				System.out.println("body: " + response.getResponseBody());
				});
		validateImport("http://localhost:8090/kvm_loa");
	}
	
	private void validateImport(String repository) {
		log.info("validateImport - " + repository);
		
		
		String identifier = "4a28e38695854059a457beb3b53c2578";
		String query = "SELECT * WHERE { ?s ?p ?o "
		+ "    FILTER( ?o = \"" + identifier + "\" ) .\n"
		+ "}";
				
		List<BindingSet> result = sparqlSelect(repository+"/sparql", query);
		assertTrue(result.size()>0); // there can be more than one version of that entry!
		String subject = result.get(0).getBinding("s").getValue().stringValue();

		query = "SELECT * WHERE { ?s ?p ?o "
		+ "    FILTER( ?s = <"+subject+"> ) .\n"
		+ "}";
		
		result = sparqlSelect(repository+"/sparql", query);
		
		ModelBuilder mb = new ModelBuilder();
		for (BindingSet bindingSet : result) {
			mb.add((Resource)bindingSet.getBinding("s").getValue(), (IRI)bindingSet.getBinding("p").getValue(), bindingSet.getBinding("o").getValue());
		}
		Model model = mb.build();

		Set<Resource> publications = model.filter(null, RDF.TYPE, SCHEMA_ORG.CreativeWork).subjects();
		assertTrue(publications.size() == 1); 
		Resource publication = publications.stream().findFirst().orElseThrow(()->new RuntimeException("could not find first publication"));
		
		Optional<Literal> idOptional = Models.objectLiteral(model.filter(publication, SCHEMA_ORG.identifier, null));
		assertTrue(idOptional.isPresent()); 
		assertEquals(identifier, idOptional.get().stringValue());
		
		Optional<Literal> nameOptional = Models.objectLiteral(model.filter(publication, SCHEMA_ORG.name, null));
		assertTrue(nameOptional.isPresent()); 
		assertEquals("Teikei Gemeinschaft München-Trudering", nameOptional.get().stringValue());
		
		query = "SELECT DISTINCT * WHERE { ?s <"+SCHEMA_ORG.identifier+"> ?o "
		+ "}";
		result = sparqlSelect(repository+"/sparql", query);
		assertTrue(result.size()>23000);
	}
	
	private List<BindingSet> sparqlSelect(String spqrqlEndpoint, String query) {
		List<Header> headers = Collections.emptyList(); // Empty collection, because in this test scenario we do not use Oauth2 
		org.apache.http.client.HttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
		SPARQLRepository repo = new SPARQLRepository(spqrqlEndpoint);
		repo.setHttpClient(client);
		return Repositories.tupleQuery(repo, query, r -> QueryResults.asList(r));
	}	
}

