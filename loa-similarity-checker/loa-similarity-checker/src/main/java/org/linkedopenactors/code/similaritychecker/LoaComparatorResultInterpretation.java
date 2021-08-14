package org.linkedopenactors.code.similaritychecker;

import org.linkedopenactors.code.comparator.LoaComparatorResult;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoaComparatorResultInterpretation {
	private LoaComparatorResult loaComparatorResult;
	private double similarityInPercent;
}
