package org.linkedopenactors.code.loaapp.controller.comperator.algorithms;

import org.linkedopenactors.code.loaAlgorithm.Algorithm;
import org.linkedopenactors.code.loaAlgorithm.AlgorithmName;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.loarepository.PlaceLoa;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor
public class GeoLocationAlgorithm implements Algorithm {
    
//	private String description = "Calculating the distance between to geo locations.";
//	
//	public GeoLocationAlgorithm(String description) {
//		this.description = description;
//	}
	
	public AlgorithmName getAlgorithmName() {
		return AlgorithmName.valueOf(getClass().getSimpleName());
	}

	public String getDescription() {
		return "Calculating the distance between to geo locations."
				+ "\n"
				+ "";
	}

	@Override
	public int compare(Object a, Object b) {
		if(!(a instanceof PlaceLoa) || !(b instanceof PlaceLoa)) {
			throw new IllegalArgumentException(String.format("only %s is allowed!", PlaceLoa.class.getName()));
		}
		PlaceLoa placeLoaA = (PlaceLoa)a;
		PlaceLoa placeLoaB = (PlaceLoa)b;
		Double distance = distanceInKm(placeLoaA.getLatitude(), placeLoaA.getLongitude(), placeLoaB.getLatitude(), placeLoaB.getLongitude());
		return distance.intValue();
	}
	
	/**
	 * "Geklaut" von hier: https://www.daniel-braun.com/technik/distanz-zwischen-zwei-gps-koordinaten-in-java-berchenen
	 * @return distanceInKm
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
	public String externalDocuLink() {
		return "";
	}

	@Override
	public String getShortDescription() {
		return "Calculating the distance between to geo locations.";
	}
}

