package org.linkedopenactors.code.loaapp.controller.comperator;

import org.eclipse.rdf4j.model.IRI;

import de.naturzukunft.rdf4j.ommapper.BaseObject;
import de.naturzukunft.rdf4j.ommapper.Iri;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Iri("http://linkedopenactors.org/ns/comparator/ComparatorOutputProperty" )
public class ComparatorOutputProperty extends BaseObject {
	@Iri("http://linkedopenactors.org/ns/comparator/property" )
	private IRI property;
	@Iri("http://linkedopenactors.org/ns/comparator/propertyValueA" )
	private Object propertyValueA;
	@Iri("http://linkedopenactors.org/ns/comparator/propertyValueB" )
	private Object propertyValueB;
}
