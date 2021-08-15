package org.linkedopenactors.code.kvmadapter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.linkedopenactors.code.comparator.ComparatorModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KvmSync {
	
	private KVMRestEndpoint kvmRestEndpoint;
	private KvmEntry2PublicationComparatorModel kvmEntry2PublicationComparatorModel;
	private Repository repository;

	public KvmSync(LoaRepositoryManager loaRepositoryManager, @Value("${app.repositoryIdKvm}") String repositoryID,
			KVMRestEndpoint kvmRestEndpoint, KvmEntry2PublicationComparatorModel kvmEntry2PublicationComparatorModel) {
		this.kvmRestEndpoint = kvmRestEndpoint;
		this.kvmEntry2PublicationComparatorModel = kvmEntry2PublicationComparatorModel;
		this.repository = loaRepositoryManager.getRepository(repositoryID).orElseThrow(()->new RuntimeException("kvm repository '"+repositoryID+"' is unknown!"));
	}
	
	public void sync(LocalDateTime lastSyncDate) throws Exception {
		long searchForChangesInTheLastMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), lastSyncDate);
		log.debug("-> UpdateChangedKvmEntries (look for changes in the last "+searchForChangesInTheLastMinutes+" minutes.)");
		
		List<ComparatorModel> changedPublications = kvmRestEndpoint.getChangedEntriesSince(lastSyncDate.minusSeconds(10)).stream().map(entry->kvmEntry2PublicationComparatorModel.convert(entry)).collect(Collectors.toList());
		log.debug("found "+changedPublications.size()+" changed entries.");
		changedPublications.forEach(extractedObject->{
			log.debug("processing entry ("+extractedObject.getSubject()+")");
			save(extractedObject);			
		});
		log.debug("<- UpdateChangedKvmEntries");
	}

	private void save(ComparatorModel comparatorModel) {
		try(RepositoryConnection con = repository.getConnection()) {
			con.add(comparatorModel.getModel(), comparatorModel.getSubject());
		}		
	}
}
