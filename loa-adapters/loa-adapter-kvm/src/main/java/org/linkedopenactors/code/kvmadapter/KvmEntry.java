package org.linkedopenactors.code.kvmadapter;


import java.time.LocalDate;
import java.util.List;

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
public class KvmEntry extends AsObject {

	  private String title;

	  private String description;

	  private Double lat;

	  private Double lng;

	  private String street;

	  private String zip;

	  private String city;

	  private String country;

	  private String state;

	  private String contactName;

	  private String email;

	  private String telephone;

	  private String homepage;

	  private String openingHours;

	  private LocalDate foundedOn;

	  private List<String> categories;

	  private List<String> tags;

//	  private ImageUrl imageUrl = null;

//	  private ImageLink imageLinkUrl = null;

//	  private List<CustomLink> links = null;

	  private String license;

	  private String id;

	  private Integer version;

	  private Integer created;

//	  private List<String> ratings = null;

	
	
}
