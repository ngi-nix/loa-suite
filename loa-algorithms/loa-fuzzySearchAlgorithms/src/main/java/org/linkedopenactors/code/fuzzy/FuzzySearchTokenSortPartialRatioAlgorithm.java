package org.linkedopenactors.code.fuzzy;


import org.eclipse.rdf4j.model.Model;
import org.linkedopenactors.code.loaAlgorithm.AbstractLoaAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import me.xdrop.fuzzywuzzy.FuzzySearch;

@Component
class FuzzySearchTokenSortPartialRatioAlgorithm extends AbstractLoaAlgorithm<String> {

	public FuzzySearchTokenSortPartialRatioAlgorithm(@Value("${app.baseNamespace}") String baseNamespace) {
		super(FuzzySearchTokenSortPartialRatioAlgorithm.class, baseNamespace, "Levenshtein - Token Sort Partial Ratio");
	}

	@Override
    public int compare(String a, String b) {	    	
       return FuzzySearch.tokenSortPartialRatio(a, b);
    }

	@Override
	public Model getRdfModel() {		
		ModelCreator<FuzzySearchTokenSortPartialRatioAlgorithm> mc = new ModelCreator<FuzzySearchTokenSortPartialRatioAlgorithm>(this);
		return mc.toModel();
	}
}
