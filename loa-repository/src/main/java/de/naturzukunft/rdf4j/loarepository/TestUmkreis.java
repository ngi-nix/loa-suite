package de.naturzukunft.rdf4j.loarepository;

public class TestUmkreis {

	public static void main(String[] args) {
		// Test https://service-wiki.hbz-nrw.de/display/SEM/SPARQL+Examples#SPARQLExamples-Gettheorganisationslocatedwithinamaximumdistancetoaspecficplace

		double lat = 50.9341;
		double refLat = 51;
		double distance = 100;

		System.out.println("lat: " + lat);
		System.out.println("refLat: " + refLat);
		System.out.println("distance: " + distance);
		
		double cos = Math.cos(refLat);
		double sin = Math.sin(refLat);
		System.out.println("Math.cos(refLat)=" + cos);
		System.out.println("Math.sin(refLat)=" + sin);
		double calculated1 = cos * sin * (Math.PI / 180);
		System.out.println("calculate: cos(refLat) * sin(refLat) * (" + Math.PI + "/" + 180 + ")");
		System.out.println("calculated: \t" + calculated1 + "\nexpected:\t0.00853595");
		 
		System.out.println("----");
		System.out.println("calculating brgrad:");
		double brgrad = Math.cos(refLat)*(Math.cos(refLat) - Math.sin(refLat)*Math.PI/180*(lat -2*refLat));
		System.out.println("cos(refLat)*(cos(refLat) - sin(refLat)*"+Math.PI+"/180*(lat - 2 * refLat));");
		System.out.println("brgrad: \t" + brgrad + "\nexpected:\t0.831939969105");

		System.out.println("----");		
		brgrad = 0.831939969105; // TODO remove me
		double q = (distance / brgrad);
		double s = Math.pow(q, 2);
		System.out.println("(distance / brgrad)² = \t" + s);
		System.out.println("expected:\t\t0.808779738472242");
	}

}
