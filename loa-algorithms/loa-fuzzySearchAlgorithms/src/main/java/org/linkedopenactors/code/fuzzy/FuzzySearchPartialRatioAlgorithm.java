package org.linkedopenactors.code.fuzzy;


import org.eclipse.rdf4j.model.Model;
import org.linkedopenactors.code.loaAlgorithm.AbstractLoaAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import me.xdrop.fuzzywuzzy.FuzzySearch;

@Component
class FuzzySearchPartialRatioAlgorithm extends AbstractLoaAlgorithm<String> {
	
	public FuzzySearchPartialRatioAlgorithm(@Value("${app.baseNamespace}") String baseNamespace) {
		super(FuzzySearchPartialRatioAlgorithm.class, baseNamespace, "Levenshtein - Partial Ratio");
	}
	
	@Override
    public int compare(String a, String b) {
       return FuzzySearch.partialRatio(a, b);
    }
	
	@Override
	public Model getRdfModel() {		
		ModelCreator<FuzzySearchPartialRatioAlgorithm> mc = new ModelCreator<FuzzySearchPartialRatioAlgorithm>(this);
		return mc.toModel();
	}
}
