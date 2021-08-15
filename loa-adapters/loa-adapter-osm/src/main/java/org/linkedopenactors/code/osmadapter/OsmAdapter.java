package org.linkedopenactors.code.osmadapter;


import org.linkedopenactors.code.comparator.ComparatorModel;
import org.linkedopenactors.code.similaritychecker.BoundingBox;
import org.linkedopenactors.code.similaritychecker.SimilarityCheckerLoaAdapter;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class OsmAdapter implements SimilarityCheckerLoaAdapter {

	private OsmRestEndpoint osmRestEndpoint;
	private OsmEntry2PublicationComparatorModel osmEntry2PublicationLoaModel;	        

	public OsmAdapter(OsmRestEndpoint osmRestEndpoint, OsmEntry2PublicationComparatorModel osmEntry2PublicationLoaModel) {
		this.osmRestEndpoint = osmRestEndpoint;
		this.osmEntry2PublicationLoaModel = osmEntry2PublicationLoaModel;
	}


	@Override
	public Flux<ComparatorModel> findByBoundingBox(BoundingBox bbox) {
		return osmRestEndpoint.getChangedEntriesSince(bbox, null).map(entry->osmEntry2PublicationLoaModel.convert(entry));
	}

	@Override
	public String getAdapterId() {
		return "osm";
	}

}
