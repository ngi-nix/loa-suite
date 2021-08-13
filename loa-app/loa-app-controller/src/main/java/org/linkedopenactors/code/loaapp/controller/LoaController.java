package org.linkedopenactors.code.loaapp.controller;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.config.RepositoryConfig;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.sail.config.SailRepositoryConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.nativerdf.config.NativeStoreConfig;
import org.linkedopenactors.code.loaapp.controller.comperator.LoaComparator;
import org.linkedopenactors.code.loaapp.controller.ui.model.Binding;
import org.linkedopenactors.code.loaapp.controller.ui.model.LastSyncModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LoaController {
	
	private Repository kvmRepository;
	private PublicationRepo publicationRepo;
	private PublicationRepo publicationRepoWeCahnge;
	
	public LoaController(RepositoryManager kvmRepositoryManager,
			List<LoaComparator> comparators,
			@Qualifier("KvmPublicationRepo") PublicationRepo publicationRepo,
			@Qualifier("WeChangePublicationRepo") PublicationRepo publicationRepoWeCahnge,
			@Value("${app.repositoryIdKvm}") String kvmRepositoryID) {
		this.publicationRepo = publicationRepo;
		this.publicationRepoWeCahnge = publicationRepoWeCahnge;
		this.kvmRepository = getKvmRepo(kvmRepositoryManager, kvmRepositoryID);
	}

	private Repository getKvmRepo(RepositoryManager repositoryManager, String kvmRepositoryID) {
		Repository actorsRepository;
		actorsRepository = repositoryManager.getRepository(kvmRepositoryID);
		if (actorsRepository == null) {
			RepositoryImplConfig repositoryTypeSpec = new SailRepositoryConfig(new NativeStoreConfig());
			RepositoryConfig repConfig = new RepositoryConfig(kvmRepositoryID, repositoryTypeSpec);
			repositoryManager.addRepositoryConfig(repConfig);
			actorsRepository = repositoryManager.getRepository(kvmRepositoryID);
			return actorsRepository;
		}
		return actorsRepository;
	}
		
	@GetMapping(path = "/comparators", produces = { "text/html" })
	public String getComparators(Model model) {
		List<Binding> bindings = new ArrayList<>();
		model.addAttribute("bindings", bindings);
		return "algorithms";
	}

	@GetMapping(path = "/lastSync", produces = { "text/html" })
	public String getLastSync(Model model) {
		IRI kvm = Values.iri("http://linkedopenactors.org/adapters/kvm");
		IRI wechange = Values.iri("http://linkedopenactors.org/adapters/wechange");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E dd.MM.yyyy HH:mm");
		String kvmLastSync = ((LastSyncDateStore)publicationRepo).lastSyncDate(kvm).map(dt->dt.format(formatter)).orElse("n/a");
		String weChangeLastSync = ((LastSyncDateStore)publicationRepoWeCahnge).lastSyncDate(wechange).map(dt->dt.format(formatter)).orElse("n/a");
		model.addAttribute("model", new LastSyncModel(kvmLastSync, weChangeLastSync));
		return "lastSync";
	}

	
	@GetMapping(path = "/", produces = { "text/html" })
	public String home() {
		return "home";
	}

	@GetMapping(path = "/kvm/{publication}", produces = { "text/turtle" })
	public ResponseEntity<String> getPublicationTurtle(@PathVariable String publication, Model model) {
		PublicationLoa pub = getPublication(publication);
		String res = toRdf(RDFFormat.TURTLE, new ModelCreator<PublicationLoa>(pub).toModel());
		return new ResponseEntity<String>(res, HttpStatus.OK);
	}
	
	@GetMapping(path = "/kvm/{publication}", produces = { "application/json+ld" })
	public ResponseEntity<String> getPublicationJsonLd(@PathVariable String publication, Model model) {
		String res = toJsonLd(			
				new ModelCreator<PublicationLoa>(getPublication(publication))
				.toModel());
		return new ResponseEntity<String>(res, HttpStatus.OK);
	}
	
	@GetMapping(path = "/kvm/{publication}", produces = { "text/html" })
	public String getPublicationHtml(@PathVariable String publication, Model model) {
		PublicationLoa pub = getPublication(publication);
		model.addAttribute("publication", pub);
		model.addAttribute("name", pub.getIdentifier());
		model.addAttribute("pageTitle", "LinkedOpenActors - Publication - " + pub.getAbout().getName());
		return "publication";
	}

//	/**
//	 * Compare as descibed here: https://github.com/kartevonmorgen/openfairdb/issues/254#issue-575057485
//	 * @param publicationA
//	 * @param publicationB
//	 * @return
//	 */
//	@GetMapping(path = "/kvm/wolmanCompare", produces = { "text/turtle" })
//	public ResponseEntity<String> wolmanCompare(@RequestParam("publicationA") String publicationA, @RequestParam("publicationB") String publicationB) {
//
//		PublicationLoa pubA = getPublication(publicationA);
//		PublicationLoa pubB = getPublication(publicationB);
//
//		org.eclipse.rdf4j.model.Model modelA = new ModelCreator<PublicationLoa>(pubA).toModel();
//		org.eclipse.rdf4j.model.Model modelB = new ModelCreator<PublicationLoa>(pubB).toModel();
//		
//		List<ComparatorOutput> cos = wolmanComperator.compare(ComparatorInput.builder()
//				.subjectA(modelA)
//				.subjectB(modelB)
//				.build());
//		
//		org.eclipse.rdf4j.model.Model allResults = new ModelBuilder().build(); 
//		cos.forEach(it->{
//			ModelCreator<ComparatorOutput> mc = new ModelCreator<>(it);
//			allResults.addAll(mc.toModel(new SimpleNamespace("kvm", "http://test.de")));
//		});
//		
//		StringWriter sw = new StringWriter();
//		Rio.write(allResults, sw, RDFFormat.TURTLE);
//		return new ResponseEntity<String>(sw.toString(), HttpStatus.OK);
//	}

//	private org.eclipse.rdf4j.model.Model filterPublication(PublicationLoa pubA) {
//		org.eclipse.rdf4j.model.Model model = new ModelCreator<PublicationLoa>(pubA).toModel();
//		Iterable<Statement> statements = model.getStatements(null, RDF.TYPE, SCHEMA_ORG.CreativeWork);
//		IRI val = (IRI)statements.iterator().next().getSubject();
//		org.eclipse.rdf4j.model.Model modelPub = model.filter(val, null, null);
//		return modelPub;
//	}

	private PublicationLoa getPublication(String publication) {
//		PublicationRepo publicationRepo = rdf4jRepositoryManager.getKvmPublicationRepo();
		log.info("reading publication: " + publication);
		List<PublicationLoa> publications = publicationRepo.getByIdentifier(publication);
		
		// TODO search the newest
		
//		String identifier = publications.stream().findFirst().map(it->it.getAbout().getName()).orElse("notFound");
		log.info("extracting the first publication from : " + publications.size());
		PublicationLoa pub = publications.stream().findFirst().orElseThrow(()->new RuntimeException("not found"));
		return pub;
	}

	@GetMapping("/publications")
	public String getAll(Model model) {
		List<Binding> bindings = new ArrayList<>();
		try(RepositoryConnection con = kvmRepository.getConnection()) {
			TupleQuery tupleQuery = con.prepareTupleQuery(query());
			   try (TupleQueryResult result = tupleQuery.evaluate()) {//				   
			      while (result.hasNext()) {  // iterate over the result
			         BindingSet bindingSet = result.next();
			         bindingSet.getBindingNames().forEach(name->{
			        	 bindings.add(new Binding(name, bindingSet.getValue(name).stringValue()));			        	 
			         });
			         
			      }
			      model.addAttribute("bindings", bindings);
			   }
		}
		model.addAttribute("bindings", bindings);
		return "publications";
	}
	
	@GetMapping("/tempRepoFromCsv")
	public String tempRepoFromCsv(Model model) {
		return "tempRepoFromCsv";
	}

	private String query() {
		return "PREFIX schema: <http://schema.org/> \n"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "SELECT DISTINCT *\n"
				+ "WHERE { \n"
				+ "	?s rdf:type schema:CreativeWork .  	\n"
				+ "  	?s schema:creativeWorkStatus ?status .\n"
				+ "	?s schema:license ?license .\n"
				+ "    ?s schema:version ?version .\n"
				+ "  	?s schema:identifier ?identifier .\n"
				+ "  	?s schema:description ?description .\n"
				+ "} LIMIT 1000";
	}
	
	private String toJsonLd(org.eclipse.rdf4j.model.Model model) {
		return toRdf(RDFFormat.JSONLD, model);
	}
	
	private String toRdf(RDFFormat rdfFormat, org.eclipse.rdf4j.model.Model model) {
		StringWriter sw = new StringWriter();
		Rio.write(model, sw, rdfFormat);		
		return sw.toString();
	}
	
}
