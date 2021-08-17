package org.linkedopenactors.code.kvmadapter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.linkedopenactors.code.csvimporter.CsvImporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@Tag(name = "Karte von Morgen - Controller", description = "Karte von Morgen specific stuff")
public class KvmController {

	private static final String KVM_LOA_REPO_ID = "kvm_loa";
	private LoaRepositoryManager loaRepositoryManager;
	private KvmSync kvmSync;
	private CsvImporter csvImporter;

	public KvmController(KvmSync kvmSync, LoaRepositoryManager loaRepositoryManager, @Qualifier(value = "KvmCsvImporter") CsvImporter csvImporter) {
		this.kvmSync = kvmSync;
		this.loaRepositoryManager = loaRepositoryManager;
		this.csvImporter = csvImporter;
	}

	@Operation(summary = "Imports entries from openFairDb.",
            description = "This version imports entries from a file that is located in the app itself. Currently there is no possibility to import a csv file. However, the import is also getting the last ~ 1000 recent changes."
            )
	@RequestMapping(value = "/kvm/initialLoad", method = RequestMethod.GET)
	public Mono<ResponseEntity<String>> initialLoad() {
		log.info("getting the maximum of available changes provided by openFairDb for the case that the csv used is already older" + KVM_LOA_REPO_ID);
		AtomicLong start = new AtomicLong(System.currentTimeMillis());
		
		String fileName = "kvm_initial_load.csv";
		ClassPathResource inputFile = new ClassPathResource(fileName);
		long count;
		try {
			count = this.csvImporter.doImport(getRepository(), inputFile.getInputStream());
			AtomicLong duration = new AtomicLong((System.currentTimeMillis() - start.get()) / 1000);
			AtomicLong durationSync = new AtomicLong(); 
			AtomicLong startSync = new AtomicLong(System.currentTimeMillis());
			return kvmSync.sync(LocalDateTime.now().minusDays(100))
					.doOnSuccess(it->{
						durationSync.set((System.currentTimeMillis() - startSync.get()) / 1000);
						System.gc();
					})
					.map(models -> new ResponseEntity<String>(count + " publications imported in " + duration.get()
						+ " seconds / " + models.size() + " updated in " + durationSync.get() + " seconds", HttpStatus.OK));
		} catch (Exception e) {
			System.gc();
			return Mono.error(e);
		}
	}

	/**
	 * Gets the kvm repository. If the repository already exists, all entries (statements) are deleted! 
	 * @return
	 */
	private Repository getRepository() {
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
		return repository;
	}
}
