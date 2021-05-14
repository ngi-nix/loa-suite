package de.naturzukunft.rdf4j.loarepository;

import java.util.Set;

import org.eclipse.rdf4j.model.IRI;

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
@Iri(SCHEMA_ORG.NAMESPACE + "Organisation" )
public class OrgansationLoa extends AsObject{
	
	@Iri(SCHEMA_ORG.NAMESPACE + "legalName" )
	private String legalName;	
	
	@Iri(SCHEMA_ORG.NAMESPACE + "location" )
	private PlaceLoa placeLocation;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "name" )
	private String name;

	@Iri(SCHEMA_ORG.NAMESPACE + "url" )
	private Set<IRI> url;

	@Iri(SCHEMA_ORG.NAMESPACE + "contactPoint" )
	private ContactPointLoa contactPoint;
}
