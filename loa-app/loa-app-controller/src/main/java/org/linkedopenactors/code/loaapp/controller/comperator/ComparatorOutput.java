package org.linkedopenactors.code.loaapp.controller.comperator;

import java.util.Set;

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
@Iri("http://linkedopenactors.org/ns/comparator/ComparatorOutput" )
public class ComparatorOutput extends BaseObject {	
	@Iri("http://linkedopenactors.org/ns/comparator/subjectA" )
	private IRI subjectA;
	@Iri("http://linkedopenactors.org/ns/comparator/subjectB" )
	private IRI subjectB;
	@Iri("http://linkedopenactors.org/ns/comparator/usedAlgorithm" )
	private String usedAlgorithm;
	@Iri("http://linkedopenactors.org/ns/comparator/result" )
	private int result;
	@Iri("http://linkedopenactors.org/ns/comparator/properties" )
	private Set<ComparatorOutputProperty> properties;
//	
//	@Iri("http://linkedopenactors.org/ns/comparator/property" )
//	private IRI property;
//	@Iri("http://linkedopenactors.org/ns/comparator/propertyValueA" )
//	private String propertyValueA;
//	@Iri("http://linkedopenactors.org/ns/comparator/propertyValueB" )
//	private String propertyValueB;
//^muss in eine klass eund hier eine liste davon ! PropertySet z.b.
	
}
