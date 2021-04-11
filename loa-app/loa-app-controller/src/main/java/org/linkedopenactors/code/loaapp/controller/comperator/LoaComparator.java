package org.linkedopenactors.code.loaapp.controller.comperator;

import java.util.List;

public interface LoaComparator {
	List<ComparatorOutput> compare(ComparatorInput comparatorInput);
	
	String getDescription();

	String getName();
}