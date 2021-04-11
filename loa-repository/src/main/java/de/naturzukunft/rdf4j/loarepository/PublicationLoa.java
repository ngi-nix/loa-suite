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
@Iri(SCHEMA_ORG.NAMESPACE + "CreativeWork" )
public class PublicationLoa extends AsObject{
	
	@Iri(SCHEMA_ORG.NAMESPACE + "version" )
	private String version;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "about" )
	private OrgansationLoa about;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "copyrightNotice" )
	private String copyrightNotice;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "creativeWorkStatus" )
	private String creativeWorkStatus;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "dateCreated" )
	private String dateCreated;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "dateModified" )
	private String dateModified;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "license" )
	private String license;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "keywords" )
	private String keywords;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "identifier" )
	private String identifier;
	
	@Iri(SCHEMA_ORG.NAMESPACE + "description" )
	private String description;
}
