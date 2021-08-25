package de.naturzukunft.rdf4j.loarepository;

public class TestUmkreis {

	public static void main(String[] args) {
		// Test https://service-wiki.hbz-nrw.de/display/SEM/SPARQL+Examples#SPARQLExamples-Gettheorganisationslocatedwithinamaximumdistancetoaspecficplace

		double lat = 50.9341;
		double refLat = 51;
		double refLatRadian = Math.toRadians(refLat);
		double distance = 100;
		
		System.out.println("lat: " + lat);
		System.out.println("refLat: " + refLat);
		System.out.println("refLatRadian: " + refLatRadian);
		System.out.println("distance: " + distance);
		
		double cos = Math.cos(refLatRadian);
		double sin = Math.sin(refLatRadian);
		double calculated1 = cos * sin * (Math.PI / 180);
		System.out.println("Math.cos(refLat)=" + cos);
		System.out.println("Math.sin(refLat)=" + sin);
		System.out.println("calculate: cos(refLat) * sin(refLat) * (" + Math.PI + "/" + 180 + ")");
		System.out.println("calculated: \t" + calculated1 + "\nexpected:\t0.00853595");
		 
		System.out.println("----");
		System.out.println("calculating calculated2:");
		double calculated2 = Math.cos(refLatRadian)*(Math.cos(refLatRadian) - Math.sin(refLatRadian)*Math.PI/180*(lat -2*refLat));
		System.out.println("cos(refLat)*(cos(refLat) - sin(refLat)*"+Math.PI+"/180*(lat - 2 * refLat));");
		System.out.println("calculated2: \t" + calculated2 + "\nexpected:\t0.831939969105");

		System.out.println("----");		
		double brgrad = 111.1949;
		double q = (distance / brgrad);
		
		
		double d2brgrad = Math.pow(q, 2);
		System.out.println("(distance / brgrad)² = ("+distance+" / "+brgrad+")² = ("+q+")² \t -> " + d2brgrad);
		System.out.println("expected:\t\t0.808779738472242");
	}

}
