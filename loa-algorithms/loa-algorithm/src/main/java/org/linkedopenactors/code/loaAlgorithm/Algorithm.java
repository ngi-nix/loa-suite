package org.linkedopenactors.code.loaAlgorithm;

import java.util.Comparator;

/**
 * 
 * @author fredy
 *
 */
public interface Algorithm extends Comparator<Object> {
	String getDescription();
	String getShortDescription();
	int compare(Object a, Object b);
	AlgorithmName getAlgorithmName();
	String externalDocuLink();
}
