package de.naturzukunft.rdf4j.loarepository;


import de.naturzukunft.rdf4j.activitystreams.model.AsObject;
import de.naturzukunft.rdf4j.ommapper.Iri;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Iri(SCHEMA_ORG.NAMESPACE + "Place" )
public class PlaceLoa extends AsObject{

	@Iri(SCHEMA_ORG.NAMESPACE + "latitude" )
	private Double latitude;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "longitude" )
	private Double longitude;

	@Iri(SCHEMA_ORG.NAMESPACE + "address" )
	private PostalAddressLoa postalAddress;
}
