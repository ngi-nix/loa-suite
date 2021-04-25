package org.linkedopenactors.code.loaapp.controller.comperator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.linkedopenactors.code.loaAlgorithm.AlgorithmName;
import org.linkedopenactors.code.loaAlgorithm.AlgorithmRepository;
import org.linkedopenactors.code.loaAlgorithm.LoaAlgorithm;
import org.linkedopenactors.code.loaapp.controller.comperator.algorithms.GeoLocationAlgorithm;
import org.linkedopenactors.code.loaapp.controller.infrastructure.config.LoaRDF4JRepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.loarepository.ContactPointLoa;
import de.naturzukunft.rdf4j.loarepository.OrgansationLoa;
import de.naturzukunft.rdf4j.loarepository.PlaceLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@Qualifier("wolmanComperator")
@Component
@Slf4j
public class WolmanComperator implements LoaComparator {

//	private LoaRDF4JRepositoryManager rdf4jRepositoryManager;
	private AlgorithmRepository algorithmRepository;
//	private PublicationRepo getKvmPublicationRepo;
	
	@Autowired
	@Qualifier("KvmPublicationRepo")
	private PublicationRepo getKvmPublicationRepo;


	public WolmanComperator(//LoaRDF4JRepositoryManager rdf4jRepositoryManager, 
			AlgorithmRepository algorithmRepository
//			, @Qualifier(value = "KvmPublicationRepo") PublicationRepo getKvmPublicationRepo
			) {
//		this.rdf4jRepositoryManager = rdf4jRepositoryManager;
		this.algorithmRepository = algorithmRepository;
//		this.getKvmPublicationRepo = getKvmPublicationRepo;
	}
	
	@Override
	public String getDescription() {
		return "See: https://gitlab.com/naturzukunft.de/public/loa/loa-app/-/issues/4";
	}

	@Override
	public List<ComparatorOutput> compare(ComparatorInput comparatorInput) {

		List<ComparatorOutput> result = new ArrayList<>();
				
		Integer webSiteMatch=null;

		PublicationLoa pubA = getPublication(comparatorInput.getSubjectA());
		PublicationLoa pubB = getPublication(comparatorInput.getSubjectB());

		if(pubA.getAbout()==null) {
			new RuntimeException("publicationA does not have an organisation");
		}
		if(pubB.getAbout()==null) {
			new RuntimeException("publicationB does not have an organisation");
		}
		
		PlaceLoa placeA = pubA.getAbout().getLocation();
		PlaceLoa placeB = pubB.getAbout().getLocation();

		ContactPointLoa contactPointA = pubA.getAbout().getContactPoint();
		ContactPointLoa contactPointB = pubB.getAbout().getContactPoint();

		// A. Distance: If they closer than 10 m -> 100 duplicate; if larger than 500 m -> 0% duplicate
		result.add(compareDistance(placeA, placeB));
		
		// B. Webseite: If they have the same URL-basis or,
		// TODO
		
		// Ca. Matching Mailadress or phone-number is a 100% duplicate.
		if(contactPointA!=null && contactPointB!=null) {
			result.add(compareEmail(placeA, placeB, contactPointA, contactPointB));			
			result.add(compareTelefone(placeA, placeB, contactPointA, contactPointB));
		}
		
		// Cb. If they have the same Mailing-Adress-Domain which is not @gmail.com, @posteo.de or one of those big mailingsystems 
		// -> its a hint, like 80% duplicate...
		// TODO
		
		//D. Title: What percentage of words match?
		result.add(compareLegalname(pubA.getAbout(), pubB.getAbout()));
		result.add(compareName(pubA.getAbout(), pubB.getAbout()));
		
		return result;
	}

	private ComparatorOutput compareLegalname(OrgansationLoa orgaA, OrgansationLoa orgaB) {
		int result = -1;
		String algoName = "null";
		if(orgaA.getLegalName()!=null&&orgaB.getLegalName()!=null) {
			LoaAlgorithm<String> algorithm = (LoaAlgorithm<String>)algorithmRepository.get(AlgorithmName.fuzzySearchRatioAlgorithm);
			result = algorithm.compare(orgaA.getLegalName(), orgaB.getLegalName());
			algoName = algorithm.getName();
		}
		return ComparatorOutput.builder()
				.subject(Values.iri("urn:todo_" + UUID.randomUUID()))
				.type(Set.of(Values.iri("http://linkedopenactors.org/ns/comparator/ComparatorOutput")))
				.subjectA(orgaA.getSubject())
				.subjectB(orgaB.getSubject())
				.usedAlgorithm(algoName)
				.properties(Set.of(ComparatorOutputProperty.builder()
						.property(SCHEMA_ORG.legalName)
						.propertyValueA(orgaA.getLegalName())
						.propertyValueB(orgaB.getLegalName())
						.build()))
				.result(result)
				.build();
	}

	private ComparatorOutput compareName(OrgansationLoa orgaA, OrgansationLoa orgaB) {
		LoaAlgorithm<String> algorithm = (LoaAlgorithm<String>)algorithmRepository.get(AlgorithmName.fuzzySearchRatioAlgorithm);
		return ComparatorOutput.builder()
				.subject(Values.iri("urn:todo_" + UUID.randomUUID()))
				.type(Set.of(Values.iri("http://linkedopenactors.org/ns/comparator/ComparatorOutput")))
				.subjectA(orgaA.getSubject())
				.subjectB(orgaB.getSubject())
				.usedAlgorithm(algorithm.getName())
				.properties(Set.of(ComparatorOutputProperty.builder()
						.property(SCHEMA_ORG.name)
						.propertyValueA(orgaA.getName())
						.propertyValueB(orgaB.getName())
						.build()))
				.result(algorithm.compare(orgaA.getName(), orgaB.getName()))
				.build();
	}

	private ComparatorOutput compareTelefone(PlaceLoa placeA, PlaceLoa placeB, ContactPointLoa contactPointA,
			ContactPointLoa contactPointB) {
		LoaAlgorithm<String> algorithm = (LoaAlgorithm<String>)algorithmRepository.get(AlgorithmName.fuzzySearchRatioAlgorithm);
		return ComparatorOutput.builder()
				.subject(Values.iri("urn:todo_" + UUID.randomUUID()))
				.type(Set.of(Values.iri("http://linkedopenactors.org/ns/comparator/ComparatorOutput")))
				.subjectA(placeA.getSubject())
				.subjectB(placeB.getSubject())
				.usedAlgorithm(algorithm.getName())
				.properties(Set.of(ComparatorOutputProperty.builder()
						.property(SCHEMA_ORG.telephone)
						.propertyValueA(contactPointA.getTelephone())
						.propertyValueB(contactPointB.getTelephone())
						.build()))
				.result(algorithm.compare(contactPointA.getTelephone(), contactPointB.getTelephone()))
				.build();
	}

	private ComparatorOutput compareEmail(PlaceLoa placeA, PlaceLoa placeB, ContactPointLoa contactPointA,
			ContactPointLoa contactPointB) {
		LoaAlgorithm<String> algorithm = (LoaAlgorithm<String>)algorithmRepository.get(AlgorithmName.fuzzySearchRatioAlgorithm);
		return ComparatorOutput.builder()
				.subject(Values.iri("urn:todo_" + UUID.randomUUID()))
				.type(Set.of(Values.iri("http://linkedopenactors.org/ns/comparator/ComparatorOutput")))
				.subjectA(placeA.getSubject())
				.subjectB(placeB.getSubject())
				.usedAlgorithm(algorithm.getName())
				.properties(Set.of(ComparatorOutputProperty.builder()
						.property(SCHEMA_ORG.email)
						.propertyValueA(contactPointA.getEmail())
						.propertyValueB(contactPointB.getEmail())
						.build()))
				.result(algorithm.compare(contactPointA.getEmail(), contactPointB.getEmail()))
				.build();
	}

	private ComparatorOutput compareDistance(PlaceLoa placeA, PlaceLoa placeB) {
		Integer distance=null;
		if(placeA!=null && placeB!=null) {
			//A. Distance: If they closer than 10 m -> 100 duplicate; if larger than 500 m -> 0% duplicate
			GeoLocationAlgorithm gla = new GeoLocationAlgorithm(); 
			distance = gla.compare(placeA, placeB);
		}
		ComparatorOutputProperty lat = ComparatorOutputProperty.builder()
			.property(SCHEMA_ORG.latitude)
			.propertyValueA(placeA.getLatitude())
			.propertyValueB(placeB.getLatitude())
			.build();
		
		ComparatorOutputProperty lng = ComparatorOutputProperty.builder()
			.property(SCHEMA_ORG.longitude)
			.propertyValueA(placeA.getLongitude())
			.propertyValueB(placeB.getLongitude())
			.build();

		ComparatorOutput comparatorOutputDistance = ComparatorOutput.builder()
				.subject(Values.iri("urn:todo_" + UUID.randomUUID()))
				.type(Set.of(Values.iri("http://linkedopenactors.org/ns/comparator/ComparatorOutput")))
				.subjectA(placeA.getSubject())
				.subjectB(placeB.getSubject())
				.usedAlgorithm(AlgorithmName.distanceCalculator.name())
				.properties(Set.of(lat,lng))
				.result(distance)
				.build();
		return comparatorOutputDistance;
	}

	private PublicationLoa getPublication(Model model) {
		
		Model creativeQWorkStmts = model.filter(null, RDF.TYPE, SCHEMA_ORG.CreativeWork);
		if(creativeQWorkStmts.size()!=1) {
			throw new RuntimeException("exact one of " + SCHEMA_ORG.CreativeWork + " is expected.");
		}
		IRI subject = (IRI)creativeQWorkStmts.stream().findFirst().get().getSubject();
		
		
//		PublicationRepo publicationRepo = rdf4jRepositoryManager.getKvmPublicationRepo();
		log.info("reading publication: " + subject);
		Optional<PublicationLoa> publicationOptional = getKvmPublicationRepo.getBySubject(subject);
		PublicationLoa pub = publicationOptional.orElseThrow(()->new RuntimeException("not found"));
		return pub;
	}

	@Override
	public String getName() {
		return "WolmanComperator";
	}

}
