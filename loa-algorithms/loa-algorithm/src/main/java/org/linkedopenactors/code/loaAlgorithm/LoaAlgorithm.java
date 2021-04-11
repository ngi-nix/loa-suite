package org.linkedopenactors.code.loaAlgorithm;

import java.util.Comparator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface LoaAlgorithm<T> extends Comparator<T> {
	String getName();
	String getDescription();
	Model getRdfModel();
	IRI getHowTo();
}
