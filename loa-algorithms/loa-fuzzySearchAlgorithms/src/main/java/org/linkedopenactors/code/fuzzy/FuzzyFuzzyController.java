package org.linkedopenactors.code.fuzzy;

import org.linkedopenactors.code.loaAlgorithm.AbstractLoaAlgorithm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FuzzyFuzzyController {
	
	private FuzzySearchRatioAlgorithm fuzzySearchRatioAlgorithm;
	private FuzzySearchPartialRatioAlgorithm fuzzySearchPartialRatioAlgorithm;
	private FuzzySearchTokenSetPartialRatioAlgorithm fuzzySearchTokenSetPartialRatioAlgorithm;
	private FuzzySearchTokenSetRatioAlgorithm fuzzySearchTokenSetRatioAlgorithm;
	private FuzzySearchTokenSortPartialRatioAlgorithm fuzzySearchTokenSortPartialRatioAlgorithm;
	private FuzzySearchTokenSortRatioAlgorithm fuzzySearchTokenSortRatioAlgorithm;
	private FuzzySearchWeightedRatioAlgorithm fuzzySearchWeightedRatioAlgorithm;

	public FuzzyFuzzyController(
			FuzzySearchRatioAlgorithm fuzzySearchRatioAlgorithm, 
			FuzzySearchPartialRatioAlgorithm fuzzySearchPartialRatioAlgorithm,
			FuzzySearchTokenSetPartialRatioAlgorithm fuzzySearchTokenSetPartialRatioAlgorithm,
		 	FuzzySearchTokenSetRatioAlgorithm fuzzySearchTokenSetRatioAlgorithm,
		 	FuzzySearchTokenSortPartialRatioAlgorithm fuzzySearchTokenSortPartialRatioAlgorithm,
		 	FuzzySearchTokenSortRatioAlgorithm fuzzySearchTokenSortRatioAlgorithm,
		 	FuzzySearchWeightedRatioAlgorithm fuzzySearchWeightedRatioAlgorithm) {
				this.fuzzySearchRatioAlgorithm = fuzzySearchRatioAlgorithm;
				this.fuzzySearchPartialRatioAlgorithm = fuzzySearchPartialRatioAlgorithm;
				this.fuzzySearchTokenSetPartialRatioAlgorithm = fuzzySearchTokenSetPartialRatioAlgorithm;
				this.fuzzySearchTokenSetRatioAlgorithm = fuzzySearchTokenSetRatioAlgorithm;
				this.fuzzySearchTokenSortPartialRatioAlgorithm = fuzzySearchTokenSortPartialRatioAlgorithm;
				this.fuzzySearchTokenSortRatioAlgorithm = fuzzySearchTokenSortRatioAlgorithm;
				this.fuzzySearchWeightedRatioAlgorithm = fuzzySearchWeightedRatioAlgorithm;
	}
	
	@GetMapping(path = "/fuzzyfuzzy/compare", produces = { "text/html" })
	public ResponseEntity<String> fuzzyfuzzy(Model model, @RequestParam String algorithmName, @RequestParam String textA, @RequestParam String textB) {
		
		AbstractLoaAlgorithm<String> algoToUse;
		switch (algorithmName) {
			case "fuzzySearchRatio":
				algoToUse = fuzzySearchRatioAlgorithm;
				break;
			case "fuzzySearchPartialRatio":
				algoToUse = fuzzySearchPartialRatioAlgorithm;
				break;
			case "fuzzySearchTokenSetPartialRatio":
				algoToUse = fuzzySearchTokenSetPartialRatioAlgorithm;
				break;
			case "fuzzySearchTokenSetRatio":
				algoToUse = fuzzySearchTokenSetRatioAlgorithm;
				break;
			case "fuzzySearchTokenSortPartialRatio":
				algoToUse = fuzzySearchTokenSortPartialRatioAlgorithm;
				break;
			case "fuzzySearchTokenSortRatio":
				algoToUse = fuzzySearchTokenSortRatioAlgorithm;
				break;
			case "fuzzySearchWeightedRatio":
				algoToUse = fuzzySearchWeightedRatioAlgorithm;
				break;
							
			default:
				throw new RuntimeException("unknown algorithm: " + algorithmName);
		}
		Integer result = algoToUse.compare(textA, textB);
		return new ResponseEntity<String>(result.toString(), HttpStatus.OK);
	}
}
