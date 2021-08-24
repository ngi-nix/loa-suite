package org.linkedopenactors.code.loaapp;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ITTestPublicationController {

	@LocalServerPort
	private int port;

	@Autowired
	private WebTestClient webTestClient;
	 
	// TODO add an "initialLoad" for tests. import turtle for example or the csv of hannes.
	@Test
	public void testGetByIRI() throws Exception {
		webTestClient = webTestClient
				.mutate()
				.responseTimeout(Duration.ofMinutes(2))
				.build();
		webTestClient
			.get()
			.uri("/kvm/initialLoad")
			.exchange()
			.expectStatus()
			.is2xxSuccessful()
			.expectHeader()
			.contentType("text/plain;charset=UTF-8")
			.expectBody(String.class)
			.consumeWith(response->response.getResponseBody().startsWith("23130 publications imported in "));
		
		
			webTestClient
			.get()
			.uri("/kvm_loa/kvm:V0_abb3604e6a2b41feac901b263f08a131")
			.header("accept", "application/json-ld")
			.exchange()
			.expectStatus()
			.is2xxSuccessful()
			.expectHeader()
			.contentType("application/json-ld;charset=UTF-8")
//			.expectBody()
//			.jsonPath("$.http://schema.org/identifier").isEqualTo("abb3604e6a2b41feac901b263f08a131") 
			// Das "$.http://schema.org/identifier" mag jsonpath eher nicht
			;
//			.jsonPath("$.http://schema.org/keywords").equals("abb3604e6a2b41feac901b263f08a131")
//			                               "leitungswasser",
//			                               "refill-station",
//			                               "barrierefrei",
//			                               "trinkwasser",
//			                               "bank",
//			                               "refill"
//			                             ],
	}
	
//	curl -X 'GET' \
//	  'http://localhost:8090/kvm_loa' \
//	  -H 'accept: application/json'
}
