package org.linkedopenactors.code.comparator;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SimpleComparatorModel implements ComparatorModel {
	private IRI subject;
	private Model model;
}
