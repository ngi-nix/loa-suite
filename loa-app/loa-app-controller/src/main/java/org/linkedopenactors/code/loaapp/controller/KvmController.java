package org.linkedopenactors.code.loaapp.controller;

import org.eclipse.rdf4j.repository.Repository;
import org.linkedopenactors.code.kvmadapter.initial.KvmCsv2RdfRepoInitialLoad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Tag(name = "Karte von Morgen - Controller", description = "Karte von Morgen specific stuff")
public class KvmController {

	private static final String KVM_LOA_REPO_ID = "kvm_loa";
	private LoaRepositoryManager loaRepositoryManager;
	private KvmCsv2RdfRepoInitialLoad csv2RdfRepoInitialLoad;

	public KvmController(LoaRepositoryManager loaRepositoryManager, KvmCsv2RdfRepoInitialLoad csv2RdfRepoInitialLoad) {
		this.loaRepositoryManager = loaRepositoryManager;
		this.csv2RdfRepoInitialLoad = csv2RdfRepoInitialLoad;
	}

	@RequestMapping(value = "/kvm/initialLoad", method = RequestMethod.GET)
	public ResponseEntity<String> initialLoad() {
		if (loaRepositoryManager.getRepository(KVM_LOA_REPO_ID).isPresent()) {
			log.info("deleting existing repository " + KVM_LOA_REPO_ID);
			loaRepositoryManager.removeRepository(KVM_LOA_REPO_ID);
		}
		Repository repository = loaRepositoryManager.createRepo(KVM_LOA_REPO_ID);

		long start = System.currentTimeMillis();
		long count = csv2RdfRepoInitialLoad.run(repository);
		long duration = (System.currentTimeMillis() - start) / 1000;
		return new ResponseEntity<String>(count + " publications imported in " + duration + " seconds", HttpStatus.OK);
	}
}
