package org.linkedopenactors.code.csvimporter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Container for model and subject.
 */
@Data
@AllArgsConstructor
public class SubjectModelPair {
	/**
	 * The subject of the 'root' object. In LOA this is the seubject of the publication (https://schema.org/CreativeWork) 
	 */
	private IRI subject;
	
	/**
	 * The model, that represents the publication (https://schema.org/CreativeWork)
	 */
	private Model model;
}
