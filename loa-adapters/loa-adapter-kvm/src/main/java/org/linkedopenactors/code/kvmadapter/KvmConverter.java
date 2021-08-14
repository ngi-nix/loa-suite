package org.linkedopenactors.code.kvmadapter;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.loarepository.ContactPointLoa;
import de.naturzukunft.rdf4j.loarepository.OrgansationLoa;
import de.naturzukunft.rdf4j.loarepository.OrgansationLoa.OrgansationLoaBuilder;
import de.naturzukunft.rdf4j.loarepository.PlaceLoa;
import de.naturzukunft.rdf4j.loarepository.PostalAddressLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa.PublicationLoaBuilder;
import de.naturzukunft.rdf4j.vocabulary.AS;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KvmConverter {
	
	@Value("${app.baseSubjectKvm}")
	private String baseSubject;
			  
	public PublicationLoa convert(KvmEntry entry) {
		
		String version = Optional.ofNullable(entry.getVersion()).orElse(Integer.valueOf(0)).toString();
		String baseSubjectWitVersion = "kvm:V" + version + "_" + entry.getId();

		PostalAddressLoa postalAddress = PostalAddressLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/postalAddress"))
				.type(Set.of(SCHEMA_ORG.PostalAddress, AS.Object))
				.addressCountry(entry.getCountry())
				.addressLocality(entry.getCity())
//				.addAddressRegion(entry.get)
				.streetAddress(entry.getStreet())
				.postalCode(entry.getZip())
				.build();

		PlaceLoa place = PlaceLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/place"))
				.type(Set.of(SCHEMA_ORG.Place, AS.Object))
				.latitude(entry.getLat())
				.longitude(entry.getLng())
				.postalAddress(postalAddress)
				.build();

		ContactPointLoa contactPoint = ContactPointLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/contactPoint"))
				.type(Set.of(SCHEMA_ORG.ContactPoint, AS.Object))
				.email(entry.getEmail())
				.name(entry.getContactName())
				.telephone(entry.getTelephone())
				.build();

		
		OrgansationLoaBuilder organsationLoaBuilder = OrgansationLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/organisation"))
				.type(Set.of(SCHEMA_ORG.Organization, AS.Object))
				.name(entry.getTitle())
//				.addLegalName("Mannesmann AG")
				.placeLocation(place)
				.contactPoint(contactPoint);
		
		if( entry.getHomepage()!=null ) {
			try {
				organsationLoaBuilder.url(Set.of(iri(entry.getHomepage())));
			} catch (Exception e) {
				log.warn("IGNORING: " + entry.getId() + " homepage is not a validf IRI: " + entry.getHomepage());
			}
		}
		
		OrgansationLoa organisation = organsationLoaBuilder.build();
		
		PublicationLoaBuilder publicationLoaBuilder = PublicationLoa.builder()
				.subject(iri(baseSubjectWitVersion ))
		.creativeWorkStatus(entry.getState())
		.type(Set.of(SCHEMA_ORG.CreativeWork, AS.Object))
		.asName(entry.getTitle())
//		.addCopyrightNotice(entry.getc)
		.description(entry.getDescription())
		.identifier(entry.getId())
		.license(entry.getLicense())		
		.about(organisation)
		.version(version);

		if(entry.getTags()!=null) {
			// TODO hier sollte kein csv stehen, sondern mehrere einträge!
			publicationLoaBuilder.keywords(entry.getTags().stream()
                    .collect(Collectors.joining(",")));
		}
		
		if( entry.getCreated() != null ) {
			LocalDateTime triggerTime =
			        LocalDateTime.ofInstant(Instant.ofEpochMilli(entry.getCreated()), 
			                                TimeZone.getDefault().toZoneId());			
			publicationLoaBuilder.dateCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(triggerTime));
		}		

		publicationLoaBuilder.dateModified(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
		PublicationLoa publication = publicationLoaBuilder.build();  
		return publication;
	}
}
