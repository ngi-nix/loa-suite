package org.linkedopenactors.code.distancecalculator;

import org.eclipse.rdf4j.model.Model;
import org.linkedopenactors.code.loaAlgorithm.AbstractLoaAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.ommapper.Iri;
import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

@ToString(callSuper=true)
@EqualsAndHashCode(callSuper = true)
@Iri("http://purl.org/spar/fabio/Algorithm" )
@Component
public class DistanceCalculator extends AbstractLoaAlgorithm<GeoCoordinates> {

	public DistanceCalculator(@Value("${app.baseNamespace}") String baseNamespace) {
		super(DistanceCalculator.class, baseNamespace, "Calculating the distance between to geo locations.");
	}
	
	@Override
	public int compare(@NonNull GeoCoordinates geoCoordinatesA, @NonNull GeoCoordinates geoCoordinatesB) {
		Double distance = distanceInKm(geoCoordinatesA.getLatitude(), geoCoordinatesA.getLongitude(), geoCoordinatesB.getLatitude(), geoCoordinatesB.getLongitude());
		return distance.intValue();
	}

	/**
	 * "Stolen" from here: https://www.daniel-braun.com/technik/distanz-zwischen-zwei-gps-koordinaten-in-java-berchenen
	 * @return distance in kilometers
	 */
	private double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
	    int radius = 6371;

	    double lat = Math.toRadians(lat2 - lat1);
	    double lon = Math.toRadians(lon2- lon1);

	    double a = Math.sin(lat / 2) * Math.sin(lat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lon / 2) * Math.sin(lon / 2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    double d = radius * c;

	    return Math.abs(d);
	}

	@Override
	public Model getRdfModel() {		
		ModelCreator<DistanceCalculator> mc = new ModelCreator<DistanceCalculator>(this);
		return mc.toModel();
	}
}
