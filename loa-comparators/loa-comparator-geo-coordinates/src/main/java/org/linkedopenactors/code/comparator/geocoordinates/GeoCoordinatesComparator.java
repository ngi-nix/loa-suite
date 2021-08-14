package org.linkedopenactors.code.comparator.geocoordinates;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.StringWriter;

import org.apache.commons.collections4.IterableUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.linkedopenactors.code.comparator.ComparatorModel;
import org.linkedopenactors.code.comparator.LoaComparator;
import org.linkedopenactors.code.comparator.LoaComparatorResult;
import org.linkedopenactors.code.distancecalculator.DistanceCalculator;
import org.linkedopenactors.code.distancecalculator.GeoCoordinates;
import org.linkedopenactors.code.loaAlgorithm.LoaAlgorithm;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GeoCoordinatesComparator implements LoaComparator {
	
	private DistanceCalculator distanceCalculator;

	public GeoCoordinatesComparator(DistanceCalculator distanceCalculator) {
		this.distanceCalculator = distanceCalculator;
	}
	
	@Override
	public LoaComparatorResult compare(ComparatorModel loaModelSource, ComparatorModel loaModelTarget) {
		return LoaComparatorResult.builder()
				.loaModelSourceComparedProperties(extractGeoCoordinates(loaModelSource))
				.loaModelTargetComparedProperties(extractGeoCoordinates(loaModelTarget))
				.result(check(loaModelSource, loaModelTarget))
				.build();		
	}

	private String extractGeoCoordinates(ComparatorModel loaModel) {
		Model m = new ModelBuilder().build();
		m.addAll(IterableUtils.toList(loaModel.getModel().getStatements(iri(loaModel.getSubject().stringValue() + "/place"), SCHEMA_ORG.latitude, null)));
		m.addAll(IterableUtils.toList(loaModel.getModel().getStatements(iri(loaModel.getSubject().stringValue() + "/place"), SCHEMA_ORG.longitude, null)));
		StringWriter sw = new StringWriter();
		Rio.write(m, sw, RDFFormat.TURTLE);
		return sw.toString();
	}

	private double check(ComparatorModel loaModelSource, ComparatorModel loaModelTarget) {

		GeoCoordinates geoCoordinatesOne;
		GeoCoordinates geoCoordinatesTwo;
		try {
			Double latSource = Double.parseDouble(Models.getPropertyLiteral(loaModelSource.getModel(), iri(loaModelSource.getSubject().stringValue() + "/place"), SCHEMA_ORG.latitude).orElseThrow(()->new RuntimeException("no latitude")).stringValue());
			Double lngSource = Double.parseDouble(Models.getPropertyLiteral(loaModelSource.getModel(), iri(loaModelSource.getSubject().stringValue() + "/place"), SCHEMA_ORG.longitude).orElseThrow(()->new RuntimeException("no longitude")).stringValue());

			geoCoordinatesOne = GeoCoordinates.builder()
					.latitude(latSource)
					.longitude(lngSource)
					.build();

			IRI organisation = Models.getPropertyIRI(loaModelTarget.getModel(), loaModelTarget.getSubject(), SCHEMA_ORG.about).orElseThrow(()->new RuntimeException("no organisation"));
			IRI location = Models.getPropertyIRI(loaModelTarget.getModel(), organisation, SCHEMA_ORG.location).orElseThrow(()->new RuntimeException("no location"));

			Double latTarget = Double.parseDouble(Models.getPropertyLiteral(loaModelTarget.getModel(), location, SCHEMA_ORG.latitude).orElseThrow(()->new RuntimeException("no latitude")).stringValue());
			Double lngTarget = Double.parseDouble(Models.getPropertyLiteral(loaModelTarget.getModel(), location, SCHEMA_ORG.longitude).orElseThrow(()->new RuntimeException("no longitude")).stringValue());

			geoCoordinatesTwo = GeoCoordinates.builder()
							.latitude(latTarget)
							.longitude(lngTarget)
							.build();

			return (double)distanceCalculator.compare(geoCoordinatesOne, geoCoordinatesTwo);
		} catch (Exception e) {
			log.warn("ignoring check because of exception " + e.getMessage(), e);
			return -1;
		}
	}

	@Override
	public LoaAlgorithm<?> getLoaAlgorithm() {
		return distanceCalculator;
	}

	@Override
	public String getDescription() {
		return "Compares two geo coordinates using the " + distanceCalculator.getName() + " algorithm";
	}

	@Override
	public String getShortDescription() {
		return "Compares the geo coordinates of two comparatorModels using the " + distanceCalculator.getName() + " algorithm";
	}

	@Override
	public String externalDocuLink() {
		return "n.a.";
	}

	@Override
	public String getId() {
		return this.getClass().getSimpleName();
	}
}