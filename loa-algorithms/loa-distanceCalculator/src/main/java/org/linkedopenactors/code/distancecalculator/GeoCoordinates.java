package org.linkedopenactors.code.distancecalculator;

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
@Iri("https://schema.org/GeoCoordinates" )
public class GeoCoordinates extends BaseObject {
	@Iri("https://schema.org/latitude" )
	private double latitude;
	@Iri("https://schema.org/longitude" )
	private double longitude;
}
