package org.linkedopenactors.code.kvmadapter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.linkedopenactors.code.comparator.ComparatorModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class KvmSync {
	
	private static final IRI SUBJECT = Values.iri("http://linkedopenactors.org/adapters/kvm");
	private KVMRestEndpoint kvmRestEndpoint;
	private KvmEntry2PublicationComparatorModel kvmEntry2PublicationComparatorModel;
	private Repository repository;
	private LastSyncDateStore lastSyncDateStore;

	public KvmSync(LoaRepositoryManager loaRepositoryManager, @Value("${app.repositoryIdKvm}") String repositoryID,
			KVMRestEndpoint kvmRestEndpoint, KvmEntry2PublicationComparatorModel kvmEntry2PublicationComparatorModel,
			@Qualifier("KvmLastSyncDateStore") LastSyncDateStore lastSyncDateStore) {
		this.kvmRestEndpoint = kvmRestEndpoint;
		this.kvmEntry2PublicationComparatorModel = kvmEntry2PublicationComparatorModel;
		this.lastSyncDateStore = lastSyncDateStore;
		this.repository = loaRepositoryManager.getRepository(repositoryID).orElse(loaRepositoryManager.createRepo(repositoryID)); // TODO sollte bereits existieren, wie machen wir hier den initial load ?
	}
	
	public Mono<List<ComparatorModel>> sync() {
		LocalDateTime now = LocalDateTime.now();
		try {
			LocalDateTime lastSyncDate = lastSyncDateStore.lastSyncDate(SUBJECT).orElse(now.minusDays(100));
			log.info("scheduler: synchronize kvm - lastSyncDate: " + lastSyncDate);
			return sync(lastSyncDate);			
		} catch (Exception e) {
			log.error("error while updateChangedKvmEntries.run() " + e.getMessage());
			return Mono.empty();
		}
	}
	
	public Mono<List<ComparatorModel>> sync(LocalDateTime lastSyncDate) throws Exception {
		LocalDateTime now = LocalDateTime.now();
		long searchForChangesInTheLastMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), lastSyncDate);
		log.debug("-> UpdateChangedKvmEntries (look for changes in the last "+searchForChangesInTheLastMinutes+" minutes.)");
		
		Mono<List<ComparatorModel>> changedPublicationsMono = kvmRestEndpoint
				.getChangedEntriesSince(lastSyncDate.minusSeconds(10))
				.map(kvmEntry2PublicationComparatorModel::convert)
				.collectList()
				.map(cm->save(cm))
				.doOnSuccess(it->log.debug("<- UpdateChangedKvmEntries (succeeded)"));
		lastSyncDateStore.lastSync(SUBJECT, now);
		return changedPublicationsMono;
	}

	private List<ComparatorModel> save(List<ComparatorModel> comparatorModels) {
		log.debug("save " + comparatorModels.size() + " comparatorModels.");
		Set<Statement> statements = new HashSet<>();
 		comparatorModels.forEach(cm->statements.addAll(cm.getModel()));
		try(RepositoryConnection con = repository.getConnection()) {
			con.add(statements);
			return comparatorModels;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 				
	}
}
