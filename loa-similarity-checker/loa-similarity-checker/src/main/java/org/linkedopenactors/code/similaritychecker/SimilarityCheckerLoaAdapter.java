package org.linkedopenactors.code.similaritychecker;

import org.linkedopenactors.code.comparator.ComparatorModel;

import reactor.core.publisher.Flux;

public interface SimilarityCheckerLoaAdapter {
	/**
	 * Finds all Loa Publications in the specified bounding box.
	 * @param boundingBox https://wiki.openstreetmap.org/wiki/Bounding_Box
	 * @return All Publications found in that bounding box.
	 */
	public Flux<ComparatorModel> findByBoundingBox(BoundingBox boundingBox);

	/**
	 * @return The id of that adapter. E.g. 'osm', 'kvm', 'weChange'.
	 */
	public String getAdapterId();
}
