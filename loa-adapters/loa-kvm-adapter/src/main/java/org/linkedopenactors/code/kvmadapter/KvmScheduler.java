package org.linkedopenactors.code.kvmadapter;

import java.time.LocalDateTime;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KvmScheduler {

	private static final IRI SUBJECT = Values.iri("http://linkedopenactors.org/adapters/kvm");
	private KvmAdapter updateChangedKvmEntries; 	
	private LastSyncDateStore lastSyncDateStore;
	
	public KvmScheduler(KvmAdapter updateChangedKvmEntries, @Qualifier("KvmPublicationRepo") PublicationRepo publicationRepo) {
		this.updateChangedKvmEntries = updateChangedKvmEntries;
		lastSyncDateStore = publicationRepo;
	}
	
	@Scheduled(fixedRate = 60000, initialDelay = 30000)
	public void getKvmUpdate() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime lastSyncDate = lastSyncDateStore.lastSyncDate(SUBJECT).orElse(now);
		log.info("scheduler: synchronize kvm - lastSyncDate: " + lastSyncDate);
		
		try {
			updateChangedKvmEntries.sync(lastSyncDate);
			lastSyncDateStore.lastSync(SUBJECT, now);
		} catch (Exception e) {
			log.error("wrror while updateChangedKvmEntries.run()" ,e);
		}
	}
}
