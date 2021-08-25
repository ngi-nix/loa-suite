package org.linkedopenactors.code.comparator;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Controller
@Tag(name = "ComparatorController", description = "Provide informations about the available comparators.")
@Slf4j
public class ComparatorController {

	
	private List<LoaComparator> comparators;

	public ComparatorController(List<LoaComparator> comparators) {
		this.comparators = comparators;
	}
	
	@Operation(summary = "List the availabe comparators.",
            description = "List the availabe comparators."
    )
	@ApiResponse(
            content = @Content(mediaType = "application/json",
                    schema = @Schema(type="string"))
    )	
	@GetMapping(path = "/comparators", produces = { "application/json" })
	public Mono<ResponseEntity<String>> getComparators() {		
		log.debug("-> similarityCheck");
		String json = convert(comparators.stream().map(this::convert).collect(Collectors.toList()));
		return Mono.just(new ResponseEntity<String>(json, HttpStatus.OK));
	}
	
	private String convert(List<ComparatorInfo> comparatorInfos) {
		ObjectMapper om = new ObjectMapper();
		try {
			return om.writeValueAsString(comparatorInfos);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("error creating json", e);
		}
	}
	
	private ComparatorInfo convert(LoaComparator loaComparator) {
		return ComparatorInfo.builder()
				.description(loaComparator.getDescription())
				.shortDescription(loaComparator.getShortDescription())
				.externalDocuLink(loaComparator.externalDocuLink())
				.nameOfTheUsedLoaAlgorithm(loaComparator.getLoaAlgorithm().getName())
				.comparatorId(loaComparator.getId())
				.build();
	}
}
