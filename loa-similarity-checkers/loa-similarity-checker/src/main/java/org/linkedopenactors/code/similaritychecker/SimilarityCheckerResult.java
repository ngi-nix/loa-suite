package org.linkedopenactors.code.similaritychecker;

import java.util.Set;

import org.linkedopenactors.code.comparator.ComparatorModel;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimilarityCheckerResult {
	private ComparatorModel comparatorModelOne;
	private ComparatorModel comparatorModelTwo;
	private String idComparatorModelOne;
	private String idComparatorModelTwo;
	private Set<LoaComparatorResultInterpretation> loaComparatorResultInterpretations;
}