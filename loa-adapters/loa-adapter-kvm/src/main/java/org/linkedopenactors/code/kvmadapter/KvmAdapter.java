package org.linkedopenactors.code.kvmadapter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KvmAdapter {
	
	@Autowired
	private KVMRestEndpoint kvmRestEndpoint;
	
	@Autowired
	private KvmConverter kvmConverter;
	
	@Autowired
	@Qualifier("KvmPublicationRepo")
	private PublicationRepo publicationRepo;
	
	public void sync(LocalDateTime lastSyncDate) throws Exception {
		long searchForChangesInTheLastMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), lastSyncDate);
		log.debug("-> UpdateChangedKvmEntries (look for changes in the last "+searchForChangesInTheLastMinutes+" minutes.)");
		List<PublicationLoa> changedPublications = kvmRestEndpoint.getChangedEntriesSince(searchForChangesInTheLastMinutes).stream().map(entry->kvmConverter.convert(entry)).collect(Collectors.toList());
		log.debug("found "+changedPublications.size()+" changed entries.");
		changedPublications.forEach(it->{
			log.debug("processing entry "+it.getAsName() + " ("+it.getSubject()+")");
			save(it);			
		});
		log.debug("<- UpdateChangedKvmEntries");
	}

	private void save(PublicationLoa it) {
		Optional<PublicationLoa> publicationLoaOptional = publicationRepo.read(it.getSubject());
		if(publicationLoaOptional.isPresent()) {
			publicationRepo.remove(it.getSubject());
		}
		List<PublicationLoa> sameAsList = publicationRepo.getByIdentifier(it.getIdentifier());
		for (PublicationLoa sameAs : sameAsList) {
			if(it.getSameAs() != null) {
				it.getSameAs().add(sameAs.getSubject());
			}
		}
		publicationRepo.save(it);
	}
}
