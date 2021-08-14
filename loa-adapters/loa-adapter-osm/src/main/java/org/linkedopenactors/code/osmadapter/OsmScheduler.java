package org.linkedopenactors.code.osmadapter;

import java.time.LocalDateTime;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.naturzukunft.rdf4j.loarepository.LastSyncDateStore;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OsmScheduler {

	private static final IRI SUBJECT = Values.iri("http://linkedopenactors.org/adapters/osm");
	private OsmSync osmSync;
	private LastSyncDateStore lastSyncDateStore;

	public OsmScheduler( @Qualifier("OsmLastSyncDateStore") LastSyncDateStore lastSyncDateStore, OsmSync osmSync) {
		this.lastSyncDateStore = lastSyncDateStore;
		this.osmSync = osmSync;
	}

//	@Scheduled(fixedRate = 60000, initialDelay = 30000)
	public void getOsmUpdate() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime lastSyncDate = lastSyncDateStore.lastSyncDate(SUBJECT).orElse(now);
		log.info("scheduler: synchronize osm - lastSyncDate: " + lastSyncDate);

		try {
			osmSync.sync(lastSyncDate);
			lastSyncDateStore.lastSync(SUBJECT, now);
		} catch (Exception e) {
			log.error("wrror while updateChangedOsmEntries.run()" ,e);
		}
	}
}
