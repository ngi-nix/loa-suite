package org.linkedopenactors.code.csvimporter;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.linkedopenactors.code.csvimporter.GenericCsvImporter.GenericCsvNames;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import de.naturzukunft.rdf4j.vocabulary.AS;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;

/**
 * A converter that knows how to convert a csv file with columns like {@link GenericCsvNames} into a LOA model.
 */
@Component
class GenericCsvRecord2PublicationLoaModel {

	public SubjectModelPair convert(CSVRecord record) {
		String version = Optional.ofNullable(record.get(GenericCsvNames.PublicationLoa_version)).orElse("0");
		String baseSubjectWitVersion = "kvm:V" + version + "_" + record.get(GenericCsvNames.PublicationLoa_identifier);
		return extractPublication(baseSubjectWitVersion, record, version);
	}
	
	private SubjectModelPair extractPostalAddress(String baseSubjectWitVersion, CSVRecord record) {		
		IRI postalAddressIri = iri(baseSubjectWitVersion + "/postalAddress");
		Model model = new ModelBuilder()
				.subject(postalAddressIri)
				.add(RDF.TYPE, SCHEMA_ORG.PostalAddress)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.addressCountry, getText(record.get(GenericCsvNames.PostalAddressLoa_addressCountry)))
				.add(SCHEMA_ORG.addressLocality, getText(record.get(GenericCsvNames.PostalAddressLoa_addressLocality)))
				.add(SCHEMA_ORG.streetAddress, getText(record.get(GenericCsvNames.PostalAddressLoa_streetAddress)))
				.add(SCHEMA_ORG.postalCode, getText(record.get(GenericCsvNames.PostalAddressLoa_postalCode)))
				.build();
		return new SubjectModelPair(postalAddressIri, model);
	}
	
	private SubjectModelPair extractPlace(String baseSubjectWitVersion, CSVRecord record) {
		String lat = record.get(GenericCsvNames.PlaceLoa_latitude);
		Double latAsDouble = Double.parseDouble(lat);
		String lng = record.get(GenericCsvNames.PlaceLoa_longitude);
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
				.add(SCHEMA_ORG.email, record.get(GenericCsvNames.ContactPointLoa_email))
				.add(SCHEMA_ORG.name, record.get(GenericCsvNames.ContactPointLoa_name))
				.add(SCHEMA_ORG.telephone, record.get(GenericCsvNames.ContactPointLoa_telephone))
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
				.add(SCHEMA_ORG.name, record.get(GenericCsvNames.OrgansationLoa_name))
				.add(SCHEMA_ORG.name, record.get(GenericCsvNames.OrgansationLoa_name))
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
				.add(SCHEMA_ORG.name, record.get(GenericCsvNames.OrgansationLoa_name))
				.add(SCHEMA_ORG.creativeWorkStatus, record.get(GenericCsvNames.PublicationLoa_creativeWorkStatus))
				.add(AS.name, record.get(GenericCsvNames.OrgansationLoa_name))
				.add(SCHEMA_ORG.description, record.get(GenericCsvNames.PublicationLoa_description))
				.add(SCHEMA_ORG.identifier, record.get(GenericCsvNames.PublicationLoa_identifier))
				.add(SCHEMA_ORG.license, record.get(GenericCsvNames.PublicationLoa_license))
				.add(SCHEMA_ORG.version, literal(version))
				.add(SCHEMA_ORG.about, organisation.getSubject())
				.build();
		
		model.addAll(organisation.getModel());
			
		if(record.get(GenericCsvNames.PublicationLoa_keywords)!=null) {
			String tags = record.get(GenericCsvNames.PublicationLoa_keywords);
			model.add(subject, SCHEMA_ORG.keywords, literal(tags));
		}
		
		if( StringUtils.hasText(record.get(GenericCsvNames.PublicationLoa_dateCreated))) {
			long created = Long.parseLong(record.get(GenericCsvNames.PublicationLoa_dateCreated));
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
