package org.linkedopenactors.code.similaritychecker.controller;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.linkedopenactors.code.similaritychecker.SimilarityCheckerResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@Tag(name = "SimilarityCheck", description = "SimilarityCheck - todo")
public class SimilarityCheckController {

	private SimilarityCheckerFeeder similarityCheckerFeeder;

	public SimilarityCheckController(SimilarityCheckerFeeder feeder) {
		this.similarityCheckerFeeder = feeder;
		}

	@Operation(summary = "Do a similarityCheck",
            description = "Do a similarityCheck",
            		parameters = {			
            				@Parameter(in = ParameterIn.QUERY,
    								name = "similarityCheckerId", 
    								description = "The id of the similarityCheckerId. E.g. loa-similarity-checker-hackathon2021",
    								content = @Content(
    										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
    										examples = @ExampleObject (value = "loa-similarity-checker-hackathon2021")
    										)),
							@Parameter(in = ParameterIn.QUERY,
								name = "sourceId", 
								description = "The id of the source adapter. E.g. 	kvm, osm, weChange. Where the data comes from.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "osm")
										)),
							@Parameter(in = ParameterIn.QUERY, 
								name = "targetId", 
								description = "The id of the target adapter. E.g. kvm, osm, weChange. Where the data should go to.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "kvm")
										)),
							@Parameter(in = ParameterIn.QUERY, 
								name = "latleftTop", 
								description = "bounding box parameter.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "51.30722333912494")										
										)),
							@Parameter(in = ParameterIn.QUERY, 
								name = "lngleftTop", 
								description = "bounding box parameter.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "9.742469787597658")
										)),
							@Parameter(in = ParameterIn.QUERY, 
								name = "latRightBottom", 
								description = "bounding box parameter.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "51.37531658842644")
										)),
							@Parameter(in = ParameterIn.QUERY, 
								name = "lngRightBottom", 
								description = "bounding box parameter.",
								content = @Content(
										mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
										examples = @ExampleObject (value = "9.974040985107424")
										))
							}
    )
	@ApiResponse(
            content = @Content(mediaType = "application/json",
                    schema = @Schema(type="string"))
    )	
	@GetMapping(path = "/similarityChecker", produces = { "application/json" })
	public Mono<ResponseEntity<String>> similarityCheck(@RequestParam String similarityCheckerId, @RequestParam String sourceId, @RequestParam String targetId,
			@RequestParam Double latleftTop, @RequestParam Double lngleftTop, @RequestParam Double latRightBottom,
			@RequestParam Double lngRightBottom) {		
		log.debug("-> similarityCheck");
		
		return similarityCheckerFeeder
				.doFeed(similarityCheckerId, sourceId, targetId,
						new BoundingBox(latleftTop, lngleftTop, latRightBottom, lngRightBottom))
				.collectList().map(similarityCheckerResult -> convert(similarityCheckerResult))
				.doOnNext(it->log.debug("it: " + it));
	}
	
	private ResponseEntity<String> convert(List<SimilarityCheckerResult> similarityCheckerResults) {
		ObjectMapper om = new ObjectMapper();
		try {
			List<SimilarityCheckerControllerResult> similarityCheckerControllerResultList = toSimilarityCheckerControllerResultList(similarityCheckerResults);
			String re = om.writeValueAsString(similarityCheckerControllerResultList);
			return new ResponseEntity<String>(re, HttpStatus.OK);
		} catch (JsonProcessingException e) {				
			e.printStackTrace();
			throw new RuntimeException("error creating json", e);
		}
	}
	
	private List<SimilarityCheckerControllerResult> toSimilarityCheckerControllerResultList(
			List<SimilarityCheckerResult> similarityCheckerResults) {
		return similarityCheckerResults.stream().map(result->{
			return SimilarityCheckerControllerResult.builder()
			.comparatorModelOne(toTurtle(result.getComparatorModelOne().getModel()))
			.comparatorModelTwo(toTurtle(result.getComparatorModelTwo().getModel()))
			.idComparatorModelOne(result.getIdComparatorModelOne())
			.idComparatorModelTwo(result.getIdComparatorModelTwo())
			.loaComparatorResultInterpretations(result.getLoaComparatorResultInterpretations())
			.build();
		}).collect(Collectors.toList());
	}

	private String toTurtle(Model model) {
		StringWriter sw = new StringWriter();
		Rio.write(model, sw, RDFFormat.TURTLE);
		return sw.toString();
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@EqualsAndHashCode()
	public class BoundingBox implements org.linkedopenactors.code.similaritychecker.BoundingBox {

		Double latleftTop;
		Double lngleftTop;
		Double latRightBottom;
		Double lngRightBottom;

		public String toString() {
			return latleftTop + ", " + lngleftTop + "," + latRightBottom + "," + lngRightBottom;
		}
	}
}
