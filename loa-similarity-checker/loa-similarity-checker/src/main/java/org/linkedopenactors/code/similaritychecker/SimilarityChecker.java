package org.linkedopenactors.code.similaritychecker;

import org.linkedopenactors.code.comparator.ComparatorModel;

public interface SimilarityChecker {
	SimilarityCheckerResult check(ComparatorModel comparatorModelSource, ComparatorModel comparatorModelTarget);
	String getId();
}
