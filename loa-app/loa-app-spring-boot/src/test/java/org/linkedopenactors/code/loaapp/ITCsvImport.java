package org.linkedopenactors.code.loaapp;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.IOUtils;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Slf4j
public class ITCsvImport {

	@Autowired
	private WebTestClient webTestClient;
	 
	@Test
	public void testGetByIRI() throws Exception {
		
		String fileName = "ITCsvImportSample1.csv";
		MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
		byte[] byteArray = IOUtils.toByteArray(new ClassPathResource(fileName).getInputStream());
		bodyBuilder.part("file", byteArray)
			.header("Content-Disposition", "form-data; name=file; filename=" + fileName);
		
		webTestClient
			.post()			
			.uri("/tools/createTempRepo")
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.body(BodyInserters.fromMultipartData(bodyBuilder.build()))
			.exchange()
			.expectStatus()
			.is2xxSuccessful()
			.expectBody(String.class)
			.consumeWith(response->{
				System.out.println("body: " + response.getResponseBody());
				validateImport(response.getResponseBody());
				});
	}
	
	private void validateImport(String repository) {
		log.info("validateImport - " + repository);
		String query = "SELECT * WHERE { ?s ?p ?o }";
		String url = repository;
		List<BindingSet> result = sparqlSelect(url+"/sparql", query);
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
		assertEquals("222434", idOptional.get().stringValue());
		
		Optional<Literal> tagsOptional = Models.objectLiteral(model.filter(publication, SCHEMA_ORG.keywords, null));
		assertTrue(tagsOptional.isPresent()); 
		assertEquals("Sdg1,bio,unverpackt", tagsOptional.get().stringValue());
	}
	
	private List<BindingSet> sparqlSelect(String spqrqlEndpoint, String query) {
		List<Header> headers = Collections.emptyList(); // Empty collection, because in this test scenario we do not use Oauth2 
		org.apache.http.client.HttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
		SPARQLRepository repo = new SPARQLRepository(spqrqlEndpoint);
		repo.setHttpClient(client);
		return Repositories.tupleQuery(repo, query, r -> QueryResults.asList(r));
	}	
}

