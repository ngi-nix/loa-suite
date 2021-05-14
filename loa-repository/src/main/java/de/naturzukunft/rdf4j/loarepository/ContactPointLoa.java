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
@Iri(SCHEMA_ORG.NAMESPACE + "ContactPoint" )
public class ContactPointLoa extends AsObject{

	@Iri(SCHEMA_ORG.NAMESPACE + "email" )
	private String email;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "name" )
	private String name;

	@Iri(SCHEMA_ORG.NAMESPACE + "telephone" )
	private String telephone;
}
