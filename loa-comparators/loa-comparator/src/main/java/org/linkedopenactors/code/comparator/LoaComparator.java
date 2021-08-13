package org.linkedopenactors.code.comparator;

import org.linkedopenactors.code.loaAlgorithm.LoaAlgorithm;

public interface LoaComparator {
	LoaComparatorResult compare(ComparatorModel loaModelSource, ComparatorModel loaModelTarget);
	LoaAlgorithm<?> getLoaAlgorithm();
	String getDescription();
	String getShortDescription();
	String externalDocuLink();
	String getId();
}
