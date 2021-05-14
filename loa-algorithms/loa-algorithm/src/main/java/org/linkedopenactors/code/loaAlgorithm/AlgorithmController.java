package org.linkedopenactors.code.loaAlgorithm;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AlgorithmController {
	
	private Map<String, LoaAlgorithm<?>> availableAlgorythms;
	private String baseNamespace;
	
	public AlgorithmController(@Value("${app.baseNamespace}") String baseNamespace, Map<String, LoaAlgorithm<?>> availableAlgorythms) {
		this.baseNamespace = baseNamespace;
		this.availableAlgorythms = availableAlgorythms;
	}
	
	@GetMapping(path = "/algorithms", produces = { "text/html" })
	public String getAlgorithms(Model model) {
		List<AlgorithmsItem> algorithmsItems = new ArrayList<>();
		availableAlgorythms.forEach((k,v)->algorithmsItems.add(AlgorithmsItem.builder()
														.name(k)
														.description(v.getDescription())
														.howToLink(v.getHowTo().stringValue())
														.build()));
		model.addAttribute("algorithmsItems", algorithmsItems);
		return "algorithms";
	}

	@GetMapping(path = "/algorithms", produces = { "text/turtle" })
	public ResponseEntity<String> getAlgorithmsTurtle(Model model) {
		org.eclipse.rdf4j.model.Model rdfModel = new ModelBuilder().build();
		availableAlgorythms.forEach((k,v)->rdfModel.addAll(v.getRdfModel()));
		StringWriter sw = new StringWriter();
		Rio.write(rdfModel, sw, RDFFormat.TURTLE);
		return new ResponseEntity<String>(sw.toString(), HttpStatus.OK);
	}

	@GetMapping(path = "/algorithm/howTo{name}", produces = { "text/html" })
	public String getAlgorithmHowTo(@PathVariable String name, Model model) {
		if(availableAlgorythms.containsKey(name)) {
			LoaAlgorithm<?> algorithm = availableAlgorythms.get(name);
			
			model.addAttribute("algorithm", AlgorithmsItem.builder()
													.name(algorithm.getName())
													.description(algorithm.getDescription())
													.namespace(baseNamespace)
													.build());
			return name + "HowTo";
		} else {
			throw new RuntimeException(name + " unknown");
		}	
	}

//	@GetMapping(path = "/algorithm/{name}", produces = { "text/html" })
//	public String getAlgorithm(@PathVariable String name, Model model) {
//		if(availableAlgorythms.containsKey(name)) {
//			LoaAlgorithm<?> algorithm = availableAlgorythms.get(name);
//			
//			model.addAttribute("algorithm", AlgorithmsItem.builder()
//													.name(algorithm.getName())
//													.description(algorithm.getDescription())
//													.build());
//			return name;
//		} else {
//			throw new RuntimeException(name + " unknown");
//		}	
//	}
	
	@GetMapping(path = "/algorithm/{name}", produces = { "text/turtle" })
	public ResponseEntity<String> getAlgorithmTurtle(@PathVariable String name, Model model) {
		LoaAlgorithm<?> algorithm = availableAlgorythms.get(name);
		StringWriter sw = new StringWriter();
		org.eclipse.rdf4j.model.Model m = algorithm.getRdfModel();
		m.setNamespace("fabio", "http://purl.org/spar/fabio/");
		m.setNamespace("schema", "http://schema.org/");
		m.setNamespace("loaalgo", baseNamespace + "algorithm/");
		Rio.write(m, sw, RDFFormat.TURTLE);
		return new ResponseEntity<String>(sw.toString(), HttpStatus.OK);
	}
	
}
