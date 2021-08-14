package org.linkedopenactors.code.similaritychecker.controller;

import java.util.Set;

import org.linkedopenactors.code.similaritychecker.LoaComparatorResultInterpretation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimilarityCheckerControllerResult {
	private String comparatorModelOne;
	private String comparatorModelTwo;
	private String idComparatorModelOne;
	private String idComparatorModelTwo;
	private Set<LoaComparatorResultInterpretation> loaComparatorResultInterpretations;
}
