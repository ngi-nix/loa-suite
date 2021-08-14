package org.linkedopenactors.code.wechangeadapter;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WeChangeScheduler {

	public static final IRI SUBJECT = Values.iri("http://linkedopenactors.org/adapters/wechange");
	private WeChangeAdapter weChangeAdapter;	
	private PublicationRepo publicationRepo;
	private LastSyncDateStore lastSyncDateStore;

	public WeChangeScheduler(WeChangeAdapter weChangeAdapter, @Qualifier("WeChangePublicationRepo") PublicationRepo publicationRepo ) {
		this.weChangeAdapter = weChangeAdapter;
		this.publicationRepo = publicationRepo;
		this.lastSyncDateStore = publicationRepo;
	}
	
	@Scheduled(fixedRate = 60000, initialDelay = 5000)
	public void getWeChangeUpdate() {		
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime lastSyncDate = lastSyncDateStore.lastSyncDate(SUBJECT).orElse(now);
		log.info("scheduler: synchronize weChange - lastSyncDate: " + lastSyncDate);
		List<PublicationLoa> changedPublications = weChangeAdapter.getChangedSince(lastSyncDate.minusMinutes(5));
		changedPublications.stream().forEach(it->{
			log.info(it.toString());
			publicationRepo.save(it);	
		});
		lastSyncDateStore.lastSync(SUBJECT, now);
	}
}
