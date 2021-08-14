package org.linkedopenactors.code.distancecalculator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "AlgorithmDistanceCalculator", description = "Calcute the distance between two geo coordinates.")
public class DistancecalculatorController {
	
	private DistanceCalculator distanceCalculator;

	public DistancecalculatorController(DistanceCalculator distanceCalculator) {
		this.distanceCalculator = distanceCalculator;
	}
	
	@GetMapping(path = "/distanceCalculator/compare", produces = { "text/html" })
	public ResponseEntity<String> getComparators(Model model, @RequestParam Double latA, @RequestParam Double lonA, @RequestParam Double latB, @RequestParam Double lonB) {
		GeoCoordinates geoA = GeoCoordinates.builder()
				.latitude(latA)
				.longitude(lonA)
				.build();

		GeoCoordinates geoB = GeoCoordinates.builder()
				.latitude(latB)
				.longitude(lonB)
				.build();
		
		Integer result = distanceCalculator.compare(geoA, geoB);
		return new ResponseEntity<String>(result.toString(), HttpStatus.OK);
	}
}
