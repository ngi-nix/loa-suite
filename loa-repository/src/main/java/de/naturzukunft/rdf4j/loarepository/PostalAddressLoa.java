package de.naturzukunft.rdf4j.loarepository;

import activitystreamsde.naturzukunft.rdf4j.activitystreams.model.AsObject;
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
@Iri(SCHEMA_ORG.NAMESPACE + "PostalAddress" )
public class PostalAddressLoa extends AsObject{


	public PostalAddressLoa(AsObjectBuilder<?, ?> b) {
		super(b);
		getType().add(SCHEMA_ORG.PostalAddress);
	}

	/**
	 * e.g. 82333 
	 */
	@Iri("http://schema.org/postalCode")
	private String postalCode;
	
	/**
	 * e.g. Munich
	 */
	@Iri("http://schema.org/addressLocality")
	private String addressLocality;
	
	/**
	 * e.g. Bavaria 
	 */
	@Iri("http://schema.org/addressRegion")
	private String addressRegion;
	
	/**
	 * e.g. Germany
	 */
	@Iri("http://schema.org/addressCountry")
	private String addressCountry;
	
	/**
	 * e.g. Wasserburger Landstrasse 263
	 */
	@Iri("http://schema.org/streetAddress")
	private String streetAddress;
}
