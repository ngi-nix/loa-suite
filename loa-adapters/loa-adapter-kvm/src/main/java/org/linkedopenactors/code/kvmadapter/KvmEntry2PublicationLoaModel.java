package org.linkedopenactors.code.kvmadapter;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.linkedopenactors.code.comparator.ComparatorModel;
import org.linkedopenactors.code.comparator.SimpleComparatorModel;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.vocabulary.AS;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;

@Component
class KvmEntry2PublicationComparatorModel {
	public ComparatorModel convert(KvmEntry entry) {
		String version = Optional.ofNullable(entry.getVersion()).orElse(0).toString();
		String baseSubjectWitVersion = "kvm:V" + version + "_" + entry.getId();
		return extractPublication(baseSubjectWitVersion, entry, version);
	}
	
	private ComparatorModel extractPostalAddress(String baseSubjectWitVersion, KvmEntry entry) {
		
		IRI postalAddressIri = iri(baseSubjectWitVersion + "/postalAddress");
		Model model = new ModelBuilder()
				.subject(postalAddressIri)
				.add(RDF.TYPE, SCHEMA_ORG.PostalAddress)
				.add(RDF.TYPE, AS.Object)
				.build();
				if(entry.getCountry()!=null ) {model.add(postalAddressIri, SCHEMA_ORG.addressCountry, literal(entry.getCountry()));}
				if(entry.getCity()!=null ) {model.add(postalAddressIri, SCHEMA_ORG.addressLocality, literal(entry.getCity()));}
				if(entry.getStreet()!=null ) {model.add(postalAddressIri, SCHEMA_ORG.streetAddress, literal(entry.getStreet()));}
				if(entry.getZip()!=null ) {model.add(postalAddressIri, SCHEMA_ORG.postalCode, literal(entry.getZip()));}

		return new SimpleComparatorModel(postalAddressIri, model);
	}
	
	private ComparatorModel extractPlace(String baseSubjectWitVersion, KvmEntry entry) {
		Double latAsDouble = entry.getLat();
		Double lngAsDouble = entry.getLng();
		
		ComparatorModel postalAddress = extractPostalAddress(baseSubjectWitVersion, entry);
		
		IRI subject = iri(baseSubjectWitVersion + "/place");		
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.Place)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.latitude, latAsDouble)
				.add(SCHEMA_ORG.longitude, lngAsDouble)
				.add(SCHEMA_ORG.address, postalAddress.getSubject())
				.build();
		model.addAll(postalAddress.getModel());
		return new SimpleComparatorModel(subject, model);
	}
	
	private ComparatorModel extractContactPoint(String baseSubjectWitVersion, KvmEntry entry) {
		IRI subject = iri(baseSubjectWitVersion + "/contactPoint");
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.ContactPoint)
				.add(RDF.TYPE, AS.Object)
				.build();
		if(entry.getEmail()!=null ) {model.add(subject, SCHEMA_ORG.email, literal(entry.getEmail()));}
		if(entry.getContactName()!=null ) {model.add(subject, SCHEMA_ORG.name, literal(entry.getContactName()));}
		if(entry.getTelephone()!=null ) {model.add(subject, SCHEMA_ORG.telephone, literal(entry.getTelephone()));}
		return new SimpleComparatorModel(subject,model);
	}
	
	private ComparatorModel extractOrgansation(String baseSubjectWitVersion, KvmEntry entry) {
		ComparatorModel place = extractPlace(baseSubjectWitVersion, entry);
		ComparatorModel contactPoint = extractContactPoint(baseSubjectWitVersion, entry);
		IRI subject = iri(baseSubjectWitVersion + "/organisation");
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.Organization)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.name, entry.getTitle())
				.add(SCHEMA_ORG.contactPoint, contactPoint.getSubject())
				.add(SCHEMA_ORG.location, place.getSubject())
				.build();
		model.addAll(place.getModel());
		model.addAll(contactPoint.getModel());
		return new SimpleComparatorModel( subject, model);
	}
	
	private ComparatorModel extractPublication(String baseSubjectWitVersion, KvmEntry entry, String version) {
		ComparatorModel organisation = extractOrgansation(baseSubjectWitVersion, entry);
		IRI subject = iri(baseSubjectWitVersion);
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.CreativeWork)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.version, literal(version))
				.add(SCHEMA_ORG.about, organisation.getSubject())
				.build();
		if(entry.getTitle()!=null ) {model.add(subject, SCHEMA_ORG.name, literal(entry.getTitle()));}
		if(entry.getTitle()!=null ) {model.add(subject, AS.name, literal(entry.getTitle()));}
		if(entry.getState()!=null ) {model.add(subject, SCHEMA_ORG.creativeWorkStatus, literal(entry.getState()));}
		if(entry.getDescription()!=null ) {model.add(subject, SCHEMA_ORG.description, literal(entry.getDescription()));}
		if(entry.getId()!=null ) {model.add(subject, SCHEMA_ORG.identifier, literal(entry.getId()));}
		if(entry.getLicense()!=null ) {model.add(subject, SCHEMA_ORG.license, literal(entry.getLicense()));}
			
		model.addAll(organisation.getModel());
			
		entry.getTags().forEach(tag -> model.add(subject, SCHEMA_ORG.keywords, literal(tag)));
		
		if( entry.getCreated() != null ) {
			long created = entry.getCreated();
			LocalDateTime triggerTime =
			        LocalDateTime.ofInstant(Instant.ofEpochMilli(created), 
			                                TimeZone.getDefault().toZoneId());			
			model.add(subject, SCHEMA_ORG.dateCreated, literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(triggerTime)));
		}		
		model.add(subject, SCHEMA_ORG.dateModified, literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())));
		
		return new SimpleComparatorModel(subject, model);
	}
}
