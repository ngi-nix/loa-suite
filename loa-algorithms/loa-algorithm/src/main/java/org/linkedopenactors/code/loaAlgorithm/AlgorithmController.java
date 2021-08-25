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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "AlgorithmController", description = "Provide informations about the available algorithms.")
public class AlgorithmController {
	
	private Map<String, LoaAlgorithm<?>> availableAlgorythms;
	private String baseNamespace;
	
	public AlgorithmController(@Value("${app.baseNamespace}") String baseNamespace, Map<String, LoaAlgorithm<?>> availableAlgorythms) {
		this.baseNamespace = baseNamespace;
		this.availableAlgorythms = availableAlgorythms;
	}
	
	@Operation(summary = "List of available algorithms.",
            description = "Shows you a list of available algorithms with description and link to a 'mainEntityOfPage'")
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

	@Operation(summary = "List of available algorithms.",
            description = "Shows you a list of available algorithms with description and link to a 'mainEntityOfPage'")
	@GetMapping(path = "/algorithms", produces = { "text/turtle" })
	public ResponseEntity<String> getAlgorithmsTurtle(Model model) {
		org.eclipse.rdf4j.model.Model rdfModel = new ModelBuilder().build();
		availableAlgorythms.forEach((k,v)->rdfModel.addAll(v.getRdfModel()));
		StringWriter sw = new StringWriter();
		Rio.write(rdfModel, sw, RDFFormat.TURTLE);
		return new ResponseEntity<String>(sw.toString(), HttpStatus.OK);
	}

	@Operation(summary = "Shows the detailed information about a algorithm.",
            description = "Shows the detailed information about a algorithm. Also known as 'mainEntityOfPage'. Should contain a textual human readable description including a quickstart.")
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

	@Operation(summary = "Shows the information about a algorithm.",
            description = "Shows you a algorithm with description and link to a 'mainEntityOfPage'")
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
