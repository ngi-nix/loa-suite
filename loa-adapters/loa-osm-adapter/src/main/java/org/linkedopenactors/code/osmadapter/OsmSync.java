package org.linkedopenactors.code.osmadapter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.linkedopenactors.code.comparator.ComparatorModel;
import org.linkedopenactors.code.similaritychecker.BoundingBox;
import org.linkedopenactors.code.similaritychecker.SimpleBoundingBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OsmSync {
//	@Value("${app.osmBbox}")
	String osmTestBoundingBox = "48.482707732202215,11.894416809082031,48.63109338958398,12.363395690917969";
// TODO hard coded ???
	BoundingBox bbox = osmTestBoundingBox != null ? new SimpleBoundingBox(Double.valueOf(osmTestBoundingBox.split(",")[0]),Double.valueOf(osmTestBoundingBox.split(",")[1]),Double.valueOf(osmTestBoundingBox.split(",")[2]),Double.valueOf(osmTestBoundingBox.split(",")[3])) : null;

	private OsmRestEndpoint osmRestEndpoint;
	private OsmEntry2PublicationComparatorModel osmEntry2PublicationComparatorModel;
	private Repository repository;

	public OsmSync(LoaRepositoryManager loaRepositoryManager, @Value("${app.repositoryIdOsm}") String repositoryID,
			OsmRestEndpoint osmRestEndpoint, OsmEntry2PublicationComparatorModel osmEntry2PublicationComparatorModel) {
		this.osmRestEndpoint = osmRestEndpoint;
		this.osmEntry2PublicationComparatorModel = osmEntry2PublicationComparatorModel;
		this.repository = loaRepositoryManager.getRepository(repositoryID).orElseThrow(()->new RuntimeException("osm repository '"+repositoryID+"' is unknown!"));
	}

	public void sync(LocalDateTime lastSyncDate) throws Exception {
		long searchForChangesInTheLastMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), lastSyncDate);
		log.debug("-> UpdateChangedOsmEntries (look for changes in the last "+searchForChangesInTheLastMinutes+" minutes.)");
		List<ComparatorModel> changedPublications = osmRestEndpoint.getChangedEntriesSince(bbox, lastSyncDate).collectList().block().stream().map(entry->osmEntry2PublicationComparatorModel.convert(entry)).collect(Collectors.toList());
		log.debug("found "+changedPublications.size()+" changed entries.");
		changedPublications.forEach(LoaModel->{
			log.debug("processing entry ("+LoaModel.getSubject()+")");
			save(LoaModel);
		});
		log.debug("<- UpdateChangedOsmEntries");
	}

	private void save(ComparatorModel comparatorModel) {
		try(RepositoryConnection con = repository.getConnection()) {
			con.add(comparatorModel.getModel(), comparatorModel.getSubject());
		}
	}
}
