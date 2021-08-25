package de.naturzukunft.rdf4j.loarepository;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Map;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(name = "PublicationController", description = "CRUD functions for Publications.")
public class PublicationController {

	private LoaRepositoryManager loaRepositoryManager;
	private SparqlQueryEvaluator sparqlQueryEvaluator;

	public PublicationController(LoaRepositoryManager loaRepositoryManager, SparqlQueryEvaluator sparqlQueryEvaluator) {
		this.loaRepositoryManager = loaRepositoryManager;
		this.sparqlQueryEvaluator = sparqlQueryEvaluator;
	}

	@RequestMapping(value = "/{repositoryId}", method = RequestMethod.GET, produces = { "application/json" })
	public Mono<ResponseEntity<String>> findAllSubjects(@PathVariable("repositoryId") String repositoryId, @RequestParam(required = false) String identifier, @RequestHeader Map<String, String> headers) {
		if(identifier != null) {
			log.trace("findAllSubjects with identifier: " + identifier);
			String query = getByIdentifierQuery(identifier);
			return Mono.just(sparql(repositoryId, query,null,headers,null));
		} else {		
			return loaRepositoryManager.getRepository(repositoryId).map(repository -> {
				return findAllSubjectsInternal(repository).map(result->new ResponseEntity<String>(result, HttpStatus.OK));
			}).orElse(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Reason", "unkonw repository '" + repositoryId + "'").build()));
		}
	}

	@GetMapping(path = "/{repositoryId}/{id}", produces = { "application/json-ld" })
	public ResponseEntity<Mono<String>> findByIdJsonLd(@PathVariable("repositoryId") String repositoryId, @PathVariable("id") String id) {
		return loaRepositoryManager.getRepository(repositoryId).map(repository -> {
			try( RepositoryConnection con = repository.getConnection()) {
				String model = JsonLdFormatter.compact(toModel(con.getStatements(iri(id), null, null)));
//				model = JsonLdFormatter.flatten(toModel(con.getStatements(iri(id), null, null)));
//				model = JsonLdFormatter.frame(toModel(con.getStatements(iri(id), null, null)));
				return new ResponseEntity<Mono<String>>(Mono.just(model), HttpStatus.OK);
			}
		}).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Reason", "unkonw repository").build());
	}

	@GetMapping(path = "/{repositoryId}/{id}", produces = { "text/turtle" })
	public ResponseEntity<Mono<String>> findById(@PathVariable("repositoryId") String repositoryId, @PathVariable("id") String id) {
		return loaRepositoryManager.getRepository(repositoryId).map(repository -> {
			return new ResponseEntity<Mono<String>>(findByIdInternal(RDFFormat.TURTLE, repository, id), HttpStatus.OK);
		}).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).header("X-Reason", "unkonw repository").build());
	}

	private Mono<String> findByIdInternal(RDFFormat rdfFormat, Repository repository, String id) {
		try( RepositoryConnection con = repository.getConnection()) {
			return Mono.just(toRdf(rdfFormat, toModel(con.getStatements(iri(id), null, null))));
		}
	}

	private Mono<String> findAllSubjectsInternal(Repository repository) {
		try(RepositoryConnection con = repository.getConnection()) {
			RepositoryResult<Statement> statements = con.getStatements(null, RDF.TYPE, SCHEMA_ORG.CreativeWork);
			return Mono.just(toRdf(RDFFormat.TURTLE, toModel(statements)));
		}
	}

	private Model toModel(RepositoryResult<Statement> statements) {
		Model model = new ModelBuilder()
				.setNamespace("schema", "http://schema.org/")
				.setNamespace("as", "http://www.w3.org/ns/activitystreams#")
				.setNamespace("kvm", "http://kvm.org/ns#")
				.build();
		model.addAll(Iterations.asSet(statements));
		return model;
	}

	@GetMapping(path = "/{repositoryId}/sparql", produces = { "application/json" })
	public ResponseEntity<String> sparql(@PathVariable("repositoryId") String repositoryId, @RequestParam String query,
			 @RequestParam(required = false) String defaultGraphUri,
			 @RequestHeader Map<String, String> headers,
			@RequestParam(required = false) String namedGraphUri) {

		log.debug("->"+repositoryId+"/sparql");
		String acceptHeader = headers.get("accept");

		return
    	loaRepositoryManager.getRepository(repositoryId).map(repository -> {
    		String result;
    		try {
    			result = execute(repository, query, acceptHeader, defaultGraphUri,namedGraphUri);    			
    		} catch (Exception e) {
    			log.debug("<-sparql executed");
    			String msg = "Error executing SPQARL query: " + e.getMessage();
				log.error(msg , e);
        		return new ResponseEntity<String>(msg
        				, HttpStatus.INTERNAL_SERVER_ERROR);
    		}
    		log.debug("<-sparql executed");
//    		log.debug("result: " + result);
    		return new ResponseEntity<String>(result
    				, HttpStatus.OK);

    	}).orElse(new ResponseEntity<String>("No repository with id " + repositoryId, HttpStatus.NOT_FOUND));
    }

	@Operation(summary = "Do a surrounding area search. !!! UNDER CONSTRUCTION !!!",
            description = "Do a surrounding area search. Limited to 1000 result entries.",
            		parameters = {			
            				@Parameter(in = ParameterIn.PATH,
    								name = "repositoryId", 
    								description = "The id of the repository to search for. E.g. kvm_loa, wechange_loa",
    								content = @Content(
    										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
    										examples = @ExampleObject (value = "kvm_loa")
    										)),
							@Parameter(in = ParameterIn.QUERY,
								name = "longitude", 
								description = "The longitude of the place to search.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "9.742469787597658")
										)),
							@Parameter(in = ParameterIn.QUERY, 
								name = "latitude", 
								description = "The latitude of the place to search.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "51.30722333912494")
										)),
							@Parameter(in = ParameterIn.QUERY, 
								name = "distance", 
								description = "the distance of the surrounding area.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "100 (hardcoded!)")										
										))
							}
    )
	@ApiResponse(
            content = @Content(mediaType = "application/json",
                    schema = @Schema(type="string"))
    )	
	@GetMapping(path = "/{repositoryId}/surroundingAreaSearch", produces = { "application/json" })
	public ResponseEntity<String> surroundingAreaSearch(@PathVariable("repositoryId") String repositoryId,
			@RequestParam(required = true) String longitude, @RequestParam(required = true) String latitude,
			@RequestParam(required = true) String distance, @RequestHeader Map<String, String> headers) {
		// based on https://service-wiki.hbz-nrw.de/display/SEM/SPARQL+Examples#SPARQLExamples-Gettheorganisationslocatedwithinamaximumdistancetoaspecficplace
		
		String query = "PREFIX schema: <http://schema.org/> \n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE { \n"
				+ "	?s rdf:type schema:CreativeWork .  	\n"
				+ "  	?s schema:identifier ?identifier .\n"
				+ "    OPTIONAL {?s schema:about/schema:name ?name .}\n"
				+ "    OPTIONAL {?s schema:about/schema:url ?url .}\n"
				+ "  	?s schema:description ?description .\n"
				+ "    OPTIONAL {?s schema:about/schema:location/schema:latitude ?lat . }\n"
				+ "    OPTIONAL {?s schema:about/schema:location/schema:longitude ?lon . }\n"
				+ "    FILTER( (${lat}-xsd:float(?lat))*(${lat}-xsd:float(?lat)) + (${lon}-xsd:float(?lon))*(${lon}-xsd:float(?lon))*(0.831939969105-(${calculated1}*xsd:float(?lat))) < 0.808779738472242 ) .\n"
				+ "} LIMIT 1000";
		query = query.replace("${lat}", latitude);
		query = query.replace("${lon}", longitude);
		query = query.replace("${calculated1}", Double.toString(0.00853595));
		
//		System.out.println("query: " + query);
		return sparql(repositoryId, query, null, headers, null);

	}
	
	private String execute(Repository repository, String query, String acceptHeader, String defaultGraphUri,
			String namedGraphUri) {
		log.trace("query" + query);
		log.trace("defaultGraphUri: " + defaultGraphUri);
		log.trace("namedGraphUri: " + namedGraphUri);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		sparqlQueryEvaluator.evaluate(repository, query, acceptHeader, defaultGraphUri, namedGraphUri, bos);
		String result = bos.toString();
		return result;
	}

	private String toRdf(RDFFormat rdfFormat, Iterable<Statement> model) {
		StringWriter sw = new StringWriter();
		Rio.write(model, sw, rdfFormat);
		return sw.toString();
	}
	
	private String getByIdentifierQuery(String identifier) {
		return "PREFIX schema: <http://schema.org/> \n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE { \n"
				+ "	?s rdf:type schema:CreativeWork .  	\n"
				+ "  	?s schema:identifier ?identifier .\n"
				+ "    OPTIONAL {?s schema:about/schema:name ?name .}\n"
				+ "    OPTIONAL {?s schema:about/schema:url ?url .}\n"
				+ "    OPTIONAL {?s schema:description ?description .}\n"
				+ "    OPTIONAL {?s schema:about/schema:location/schema:latitude ?latitude . }\n"
				+ "    OPTIONAL {?s schema:about/schema:location/schema:longitude ?longitude . }\n"
				+ "    FILTER( ?identifier = \""+identifier+"\" ) .\n"
				+ "} LIMIT 10";
	}
}
