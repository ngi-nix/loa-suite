package org.linkedopenactors.code.fuzzy;


import org.eclipse.rdf4j.model.Model;
import org.linkedopenactors.code.loaAlgorithm.AbstractLoaAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import me.xdrop.fuzzywuzzy.FuzzySearch;

@Component
class FuzzySearchTokenSetPartialRatioAlgorithm extends AbstractLoaAlgorithm<String> {

	public FuzzySearchTokenSetPartialRatioAlgorithm(@Value("${app.baseNamespace}") String baseNamespace) {
		super(FuzzySearchTokenSetPartialRatioAlgorithm.class, baseNamespace, "Levenshtein - Token Set Partial Ratio");
	}

	@Override
	public int compare(String a, String b) {	    	
       return FuzzySearch.tokenSortPartialRatio(a, b);
    }

	@Override
	public Model getRdfModel() {		
		ModelCreator<FuzzySearchTokenSetPartialRatioAlgorithm> mc = new ModelCreator<FuzzySearchTokenSetPartialRatioAlgorithm>(this);
		return mc.toModel();
	}
}
