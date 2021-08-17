package org.linkedopenactors.code.loaapp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.linkedopenactors.code.comparator.ComparatorModel;
import org.linkedopenactors.code.kvmadapter.KvmSync;
import org.linkedopenactors.code.kvmadapter.initial.KvmCsv2RdfRepoInitialLoad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(name = "Karte von Morgen - Controller", description = "Karte von Morgen specific stuff")
public class KvmController {

	private static final String KVM_LOA_REPO_ID = "kvm_loa";
	private LoaRepositoryManager loaRepositoryManager;
	private KvmCsv2RdfRepoInitialLoad csv2RdfRepoInitialLoad;
	private KvmSync kvmSync;

	public KvmController(KvmSync kvmSync, LoaRepositoryManager loaRepositoryManager, KvmCsv2RdfRepoInitialLoad csv2RdfRepoInitialLoad) {
		this.kvmSync = kvmSync;
		this.loaRepositoryManager = loaRepositoryManager;
		this.csv2RdfRepoInitialLoad = csv2RdfRepoInitialLoad;
	}

	@RequestMapping(value = "/kvm/initialLoad", method = RequestMethod.GET)
	public Mono<ResponseEntity<String>> initialLoad() {
		Optional<Repository> repositoryOptional = loaRepositoryManager.getRepository(KVM_LOA_REPO_ID); 
		Repository repository = null;
		if (repositoryOptional.isPresent()) {
			repository = repositoryOptional.get();
			try(RepositoryConnection con = repository.getConnection()) {
				con.begin();
				con.remove(con.getStatements(null, null, null));
				con.commit();
				log.info("CLEANED existing repository " + KVM_LOA_REPO_ID);				
			}
		} else {
			repository = loaRepositoryManager.createRepo(KVM_LOA_REPO_ID);
		}
		log.info("getting the maximum of available changes provided by openFairDb for the case that the csv used is already older" + KVM_LOA_REPO_ID);
		long start = System.currentTimeMillis();
		long count = csv2RdfRepoInitialLoad.run(repository);
		long duration = (System.currentTimeMillis() - start) / 1000;
		try {
			start = System.currentTimeMillis();
			Mono<List<ComparatorModel>> cms = kvmSync.sync(LocalDateTime.now().minusDays(100));
			long duration2 = (System.currentTimeMillis() - start) / 1000;
			System.gc();
			return cms.map(models->new ResponseEntity<String>(count + " publications imported ("+models.size()+" updated in "+duration2+" seconds) in " + duration + " seconds", HttpStatus.OK));
		} catch (Exception e) {
			System.gc();
			return Mono.error(e);
		}
	}
}
