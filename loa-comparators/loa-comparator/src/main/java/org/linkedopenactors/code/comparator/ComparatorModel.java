package org.linkedopenactors.code.comparator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface ComparatorModel {
	IRI getSubject();
	Model getModel();
}
