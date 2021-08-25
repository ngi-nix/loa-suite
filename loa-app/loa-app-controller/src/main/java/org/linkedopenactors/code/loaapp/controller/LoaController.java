package org.linkedopenactors.code.loaapp.controller;

import java.time.format.DateTimeFormatter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.linkedopenactors.code.loaapp.controller.ui.model.LastSyncModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "LoaController", description = "Controller for UI, no web service !")
public class LoaController {
	
	private LastSyncDateStore lastSyncDateStore;
	private PublicationRepo publicationRepoWeCahnge;
	
	public LoaController(@Qualifier("KvmLastSyncDateStore") LastSyncDateStore lastSyncDateStore,
			@Qualifier("WeChangePublicationRepo") PublicationRepo publicationRepoWeCahnge,
			@Value("${app.repositoryIdKvm}") String kvmRepositoryID) {
		this.lastSyncDateStore = lastSyncDateStore;
		this.publicationRepoWeCahnge = publicationRepoWeCahnge;
	}
		
	@GetMapping(path = "/lastSync", produces = { "text/html" })
	public String getLastSync(Model model) {
		IRI kvm = Values.iri("http://linkedopenactors.org/adapters/kvm");
		IRI wechange = Values.iri("http://linkedopenactors.org/adapters/wechange");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E dd.MM.yyyy HH:mm");
		String kvmLastSync = lastSyncDateStore.lastSyncDate(kvm).map(dt->dt.format(formatter)).orElse("n/a");
		String weChangeLastSync = ((LastSyncDateStore)publicationRepoWeCahnge).lastSyncDate(wechange).map(dt->dt.format(formatter)).orElse("n/a");
		model.addAttribute("model", new LastSyncModel(kvmLastSync, weChangeLastSync));
		return "lastSync";
	}

	@GetMapping(path = "/", produces = { "text/html" })
	public String home() {
		return "home";
	}
	
	@Operation(summary = "Calling the page to upload a csv.")
	@GetMapping("/tempRepoFromCsv")
	public String tempRepoFromCsv(Model model) {
		return "tempRepoFromCsv";
	}
}
