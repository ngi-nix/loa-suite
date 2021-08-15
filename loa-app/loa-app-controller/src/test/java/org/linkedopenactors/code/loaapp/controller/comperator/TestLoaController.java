package org.linkedopenactors.code.loaapp.controller.comperator;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.linkedopenactors.code.loaapp.controller.infrastructure.config.LoaRDF4JRepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TestLoaController {
	
//	@Autowired
//	private LoaController loaController;
	
	@Autowired
	private LoaRDF4JRepositoryManager rdf4JRepositoryManager; 
	
	
//	@Test
//	void test() {
//		ResponseEntity<String> res = loaController.comparePublication("a91dc4751c324b03ac7f4b8111429465", "4a28e38695854059a457beb3b53c2578", AlgorithmName.FuzzySearchRatioAlgorithm.name());
//		System.out.println(res.getBody());
//	}

//	@Test
//	void testWolman() {
//		ResponseEntity<String> res = loaController.wolmanCompare("a91dc4751c324b03ac7f4b8111429465", "4a28e38695854059a457beb3b53c2578");
////		System.out.println(res.getBody());
//	}

	
//	@Test
	void testSparql() {
		Repository repo = rdf4JRepositoryManager.getKvmRepo();
		
		try (RepositoryConnection conn = repo.getConnection()) {
			   String queryString = getQuery("", "");
			   TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);
			   try (TupleQueryResult result = tupleQuery.evaluate()) {
			      while (result.hasNext()) {  // iterate over the result
			         BindingSet bindingSet = result.next();
			         Value lat = bindingSet.getValue("lat");
			         Value lon = bindingSet.getValue("lon");
			         Value subject = bindingSet.getValue("s");
			         System.out.println("subject: " + subject + " - lat: " + lat + " - lon: " + lon);
			      }
			   }
			}
	}

	private String getQuery(String lat, String lon) {
		return "PREFIX schema: <http://schema.org/> \n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				
				+ "SELECT ?s ?lat ?lon ( ( (50.9341-xsd:float(?lat))*(50.9341-xsd:float(?lat)) + (6.93549-xsd:float(?lon))*(6.93549-xsd:float(?lon))*(0.831939969105-(0.00853595*xsd:float(?lat))) ) AS ?d2brgrad)\n"
				+ "WHERE { \n"
				+ "	?s rdf:type schema:Place .\n"
				+ "    ?s schema:latitude ?lat . \n"
				+ "    ?s schema:longitude ?lon .\n"
				+ "	FILTER( (50.9341-xsd:float(?lat))*(50.9341-xsd:float(?lat)) + (6.93549-xsd:float(?lon))*(6.93549-xsd:float(?lon))*(0.831939969105-(0.00853595*xsd:float(?lat))) < 0.0020219493461806057363177376485694 ) .\n"
				+ "} LIMIT 10\n"
				+ "";
	}
	
	
	
}
