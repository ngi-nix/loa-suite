package org.linkedopenactors.code.osmadapter;

import static org.eclipse.rdf4j.model.util.Values.iri;
import static org.eclipse.rdf4j.model.util.Values.literal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
class OsmEntry2PublicationComparatorModel {

	public ComparatorModel convert(OsmEntry entry) {
		String version = "0";
		String baseSubjectWitVersion = "osm:V" + version + "_" + entry.getId();
		return extractPublication(baseSubjectWitVersion, entry, version);
	}

	private ComparatorModel extractPostalAddress(String baseSubjectWitVersion, OsmEntry entry) {

		IRI postalAddressIri = iri(baseSubjectWitVersion + "/postalAddress");
		Model model = new ModelBuilder()
				.subject(postalAddressIri)
				.add(RDF.TYPE, SCHEMA_ORG.PostalAddress)
				.add(RDF.TYPE, AS.Object)
				.build();
		addToModelIfNotNull(model, postalAddressIri, SCHEMA_ORG.addressCountry, entry.getTags().get("addr:country"));
		addToModelIfNotNull(model, postalAddressIri, SCHEMA_ORG.addressLocality, entry.getTags().get("addr:city"));
		addToModelIfNotNull(model, postalAddressIri, SCHEMA_ORG.streetAddress, entry.getTags().get("addr:street") == null ? null : entry.getTags().get("addr:housenumber") == null ? entry.getTags().get("addr:street") : (entry.getTags().get("addr:street") + " " + entry.getTags().get("addr:housenumber")));
		addToModelIfNotNull(model, postalAddressIri, SCHEMA_ORG.postalCode, entry.getTags().get("addr:postcode"));
		return new SimpleComparatorModel(postalAddressIri, model);
	}

	private void addToModelIfNotNull(Model model, IRI subject, IRI iri, String val) {
		if (val != null) {
			model.add(subject, iri, literal(val));
		}
	}

	private void addToModelIfNotNull(Model model, IRI subject, IRI iri, Double val) {
		if (val != null) {
			model.add(subject, iri, literal(val));
		}
	}

	private ComparatorModel extractPlace(String baseSubjectWitVersion, OsmEntry entry) {
		Double latAsDouble = entry.getLat();
		Double lngAsDouble = entry.getLon();

		ComparatorModel postalAddress = extractPostalAddress(baseSubjectWitVersion, entry);

		IRI subject = iri(baseSubjectWitVersion + "/place");
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.Place)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.address, postalAddress.getSubject()).build();
		addToModelIfNotNull(model, subject, SCHEMA_ORG.latitude, latAsDouble);
		addToModelIfNotNull(model, subject, SCHEMA_ORG.longitude, lngAsDouble);
		model.addAll(postalAddress.getModel());
		return new SimpleComparatorModel(subject, model);
	}

	private ComparatorModel extractContactPoint(String baseSubjectWitVersion, OsmEntry entry) {
		IRI subject = iri(baseSubjectWitVersion + "/contactPoint");
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.ContactPoint)
				.add(RDF.TYPE, AS.Object)
				.build();
		addToModelIfNotNull(model, subject, SCHEMA_ORG.email, entry.getTags().get("addr:email"));
		addToModelIfNotNull(model, subject, SCHEMA_ORG.name, entry.getTags().get("addr:name"));
		addToModelIfNotNull(model, subject, SCHEMA_ORG.telephone, entry.getTags().get("addr:telephone"));
		return new SimpleComparatorModel(subject, model);
	}

	private ComparatorModel extractOrgansation(String baseSubjectWitVersion, OsmEntry entry) {
		ComparatorModel place = extractPlace(baseSubjectWitVersion, entry);
		ComparatorModel contactPoint = extractContactPoint(baseSubjectWitVersion, entry);
		IRI subject = iri(baseSubjectWitVersion + "/organisation");
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.Organization)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.contactPoint, contactPoint.getSubject())
				.add(SCHEMA_ORG.location, place.getSubject())
				.build();
		addToModelIfNotNull(model, subject, SCHEMA_ORG.name, entry.getTags().get("name"));
		model.addAll(place.getModel());
		model.addAll(contactPoint.getModel());
		return new SimpleComparatorModel( subject, model);
	}

	private ComparatorModel extractPublication(String baseSubjectWitVersion, OsmEntry entry, String version) {
		ComparatorModel organisation = extractOrgansation(baseSubjectWitVersion, entry);
		IRI subject = iri(baseSubjectWitVersion);
		Model model = new ModelBuilder()
				.subject(subject)
				.add(RDF.TYPE, SCHEMA_ORG.CreativeWork)
				.add(RDF.TYPE, AS.Object)
				.add(SCHEMA_ORG.identifier, entry.getId())
				.add(SCHEMA_ORG.version, literal(version))
				.add(SCHEMA_ORG.about, organisation.getSubject())
				.build();
		addToModelIfNotNull(model, subject, SCHEMA_ORG.name, entry.getTags().get("name"));
		addToModelIfNotNull(model, subject, SCHEMA_ORG.creativeWorkStatus, entry.getTags().get("creativeWorkStatus"));
		addToModelIfNotNull(model, subject, AS.name, entry.getTags().get("name"));
		addToModelIfNotNull(model, subject, SCHEMA_ORG.description, entry.getTags().get("description"));
		addToModelIfNotNull(model, subject, SCHEMA_ORG.license, entry.getTags().get("license"));

		model.addAll(organisation.getModel());

		entry.getTags().forEach((key,val) -> model.add(subject, SCHEMA_ORG.keywords, literal(key + ":" + val)));

//		if( entry.getCreated() != null ) {
//			long created = entry.getCreated();
//			LocalDateTime triggerTime =
//			        LocalDateTime.ofInstant(Instant.ofEpochMilli(created),
//			                                TimeZone.getDefault().toZoneId());
//			model.add(subject, SCHEMA_ORG.dateCreated, literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(triggerTime)));
//		}
		model.add(subject, SCHEMA_ORG.dateModified, literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now())));

		return new SimpleComparatorModel(subject, model);
	}
}
