package org.linkedopenactors.code.comparator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoaComparatorResult {
	private String loaModelSourceComparedProperties; 
	private String loaModelTargetComparedProperties;
	private double result;
}
