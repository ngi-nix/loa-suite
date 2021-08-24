package org.linkedopenactors.code.kvmadapter.initial;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.linkedopenactors.code.csvimporter.SubjectModelPair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import de.naturzukunft.rdf4j.vocabulary.AS;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;

/**
 * A converter that knows how to convert a csv file with columns like {@link KvmCsvNames} into a LOA model.
 */
@Component
public class KvmCsvRecord2PublicationLoaModel {

	public SubjectModelPair convert(CSVRecord record) {
		String version = Optional.ofNullable(record.get(KvmCsvNames.version)).orElse("0");
		String baseSubjectWitVersion = "kvm:V" + version + "_" + record.get(KvmCsvNames.id);
		return extractPublication(baseSubjectWitVersion, record, version);
	}
	
	private SubjectModelPair extractPostalAddress(String baseSubjectWitVersion, CSVRecord record) {
		
		IRI postalAddressIri = iri(baseSubjectWitVersion + "/postalAddress");
		Model model = new ModelBuilder()
				.subject(postalAddressIri)
				.add(RDF.TYPE, SCHEMA_ORG.PostalAddress)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.addressCountry, getText(record.get(KvmCsvNames.country)))
				.add(SCHEMA_ORG.addressLocality, getText(record.get(KvmCsvNames.city)))
				.add(SCHEMA_ORG.streetAddress, getText(record.get(KvmCsvNames.street)))
				.add(SCHEMA_ORG.postalCode, getText(record.get(KvmCsvNames.zip)))
				.build();
		return new SubjectModelPair(postalAddressIri, model);
	}
	
	private SubjectModelPair extractPlace(String baseSubjectWitVersion, CSVRecord record) {
		String lat = record.get(KvmCsvNames.lat);
		Double latAsDouble = Double.parseDouble(lat);
		String lng = record.get(KvmCsvNames.lng);
		Double lngAsDouble = Double.parseDouble(lng);
		
		SubjectModelPair postalAddress = extractPostalAddress(baseSubjectWitVersion, record);
		
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
		return new SubjectModelPair(subject, model);
	}
	
	private SubjectModelPair extractContactPoint(String baseSubjectWitVersion, CSVRecord record) {
		IRI subject = iri(baseSubjectWitVersion + "/contactPoint");
		return new SubjectModelPair(subject, new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.ContactPoint)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.email, record.get(KvmCsvNames.contact_email))
				.add(SCHEMA_ORG.name, record.get(KvmCsvNames.contact_name))
				.add(SCHEMA_ORG.telephone, record.get(KvmCsvNames.contact_phone))
				.build());
	}
	
	private SubjectModelPair extractOrgansation(String baseSubjectWitVersion, CSVRecord record) {
		SubjectModelPair place = extractPlace(baseSubjectWitVersion, record);
		SubjectModelPair contactPoint = extractContactPoint(baseSubjectWitVersion, record);
		IRI subject = iri(baseSubjectWitVersion + "/organisation");
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.Organization)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.name, record.get(KvmCsvNames.title))
				.add(SCHEMA_ORG.name, record.get(KvmCsvNames.title))
				.add(SCHEMA_ORG.contactPoint, contactPoint.getSubject())
				.add(SCHEMA_ORG.location, place.getSubject())
				.build();
		model.addAll(place.getModel());
		model.addAll(contactPoint.getModel());
		return new SubjectModelPair( subject, model);
	}
	
	private SubjectModelPair extractPublication(String baseSubjectWitVersion, CSVRecord record, String version) {
		SubjectModelPair organisation = extractOrgansation(baseSubjectWitVersion, record);
		IRI subject = iri(baseSubjectWitVersion);
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.CreativeWork)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.name, record.get(KvmCsvNames.title))
				.add(SCHEMA_ORG.creativeWorkStatus, record.get(KvmCsvNames.state))
				.add(AS.name, record.get(KvmCsvNames.title))
				.add(SCHEMA_ORG.description, record.get(KvmCsvNames.description))
				.add(SCHEMA_ORG.identifier, record.get(KvmCsvNames.id))
				.add(SCHEMA_ORG.license, record.get(KvmCsvNames.license))
				.add(SCHEMA_ORG.version, literal(version))
				.add(SCHEMA_ORG.about, organisation.getSubject())
				.build();
		
		model.addAll(organisation.getModel());
			
		if(record.get(KvmCsvNames.tags)!=null) {
			String tags = record.get(KvmCsvNames.tags);
			if(StringUtils.hasText(tags)) {
				Arrays.asList(tags.split(",")).forEach(tag -> model.add(subject, SCHEMA_ORG.keywords, literal(tag)));
			}
		}
		
		if( record.get(KvmCsvNames.created_at) != null ) {
			long created = Long.parseLong(record.get(KvmCsvNames.created_at));
			LocalDateTime triggerTime =
			        LocalDateTime.ofInstant(Instant.ofEpochMilli(created), 
			                                TimeZone.getDefault().toZoneId());			
			model.add(subject, SCHEMA_ORG.dateCreated, literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(triggerTime)));
		}		
		model.add(subject, SCHEMA_ORG.dateModified, literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())));
		
		return new SubjectModelPair(subject, model);
	}
	
	private String getText(String text) {
		if(StringUtils.hasText(text)) {
			return text;
		}
		return "";
	}
}
