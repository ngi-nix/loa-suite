package org.linkedopenactors.code.loaapp.controller.comperator.algorithms;

import org.eclipse.rdf4j.model.Literal;
import org.linkedopenactors.code.loaAlgorithm.Algorithm;
import org.linkedopenactors.code.loaAlgorithm.AlgorithmName;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class AbstractLiteralOrStringAlgorithm implements Algorithm {
    
	private String description = "Fuzzy string matching like a boss. It uses Levenshtein Distance to calculate the differences between sequences.";
	
	public AbstractLiteralOrStringAlgorithm(String description) {
		this.description = description;
	}
	
	public abstract int compareString(String a, String b);

	public AlgorithmName getAlgorithmName() {
		return AlgorithmName.valueOf(getClass().getSimpleName());
	}

	public String getDescription() {
		return description;
	}

	@Override
	public int compare(Object a, Object b) {
		if(a==null || b==null) {
			throw new RuntimeException("null not allowed!");
		}		
		if(!(a instanceof Literal) || !(b instanceof Literal)) {
			if(!(a instanceof String) || !(b instanceof String)) {
				throw new IllegalArgumentException("only org.eclipse.rdf4j.model.Literal or java.lang.String is allowed! But is (a/b): " + a.getClass().getName() + " / " + b.getClass().getName());
			}
			return compareString((String)a, (String)b);
		}
		return compareString(a.toString(), b.toString());
	}

	@Override
	public String externalDocuLink() {
		return "https://github.com/seatgeek/fuzzywuzzy";
	}

	@Override
	public String getShortDescription() {
		return description;
	}
}
