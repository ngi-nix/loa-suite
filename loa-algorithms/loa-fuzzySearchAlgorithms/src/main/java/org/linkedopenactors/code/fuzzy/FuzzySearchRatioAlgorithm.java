package org.linkedopenactors.code.fuzzy;


import org.eclipse.rdf4j.model.Model;
import org.linkedopenactors.code.loaAlgorithm.AbstractLoaAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import me.xdrop.fuzzywuzzy.FuzzySearch;

@Component
class FuzzySearchRatioAlgorithm extends AbstractLoaAlgorithm<String> {

	public FuzzySearchRatioAlgorithm(@Value("${app.baseNamespace}") String baseNamespace) {
		super(FuzzySearchRatioAlgorithm.class, baseNamespace, "Levenshtein - Simple Ratio");
	}

	@Override
	public int compare(String a, String b) {	    	
       return FuzzySearch.ratio(a, b);
    }
	
	@Override
	public Model getRdfModel() {		
		ModelCreator<FuzzySearchRatioAlgorithm> mc = new ModelCreator<FuzzySearchRatioAlgorithm>(this);
		return mc.toModel();
	}
}
