package org.linkedopenactors.code.fuzzy;


import org.eclipse.rdf4j.model.Model;
import org.linkedopenactors.code.loaAlgorithm.AbstractLoaAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import me.xdrop.fuzzywuzzy.FuzzySearch;

@Component
class FuzzySearchWeightedRatioAlgorithm extends AbstractLoaAlgorithm<String> {
    
	public FuzzySearchWeightedRatioAlgorithm(@Value("${app.baseNamespace}") String baseNamespace) {
		super(FuzzySearchWeightedRatioAlgorithm.class, baseNamespace, "Levenshtein - Weighted Ratio");
	}

	@Override
    public int compare(String a, String b) {	    	
       return FuzzySearch.tokenSortRatio(a, b);
    }

	@Override
	public Model getRdfModel() {		
		ModelCreator<FuzzySearchWeightedRatioAlgorithm> mc = new ModelCreator<FuzzySearchWeightedRatioAlgorithm>(this);
		return mc.toModel();
	}
}
