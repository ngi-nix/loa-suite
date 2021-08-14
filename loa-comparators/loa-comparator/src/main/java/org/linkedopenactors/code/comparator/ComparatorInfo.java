package org.linkedopenactors.code.comparator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComparatorInfo {
	private String comparatorId;
	private String nameOfTheUsedLoaAlgorithm;
	private String description;
	private String shortDescription;
	private String externalDocuLink;
}
