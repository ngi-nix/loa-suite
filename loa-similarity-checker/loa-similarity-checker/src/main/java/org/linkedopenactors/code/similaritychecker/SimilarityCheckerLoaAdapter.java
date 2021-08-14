package org.linkedopenactors.code.similaritychecker;

import org.linkedopenactors.code.comparator.ComparatorModel;

import reactor.core.publisher.Flux;

public interface SimilarityCheckerLoaAdapter {
	/**
	 * Finds all Loa Publications in the specified bounding box.
	 * @param latleftTop latitude top left
	 * @param lngleftTop longitude top left
	 * @param latRightBottom latitude bottom right
	 * @param lngRightBottom longitude bottom right
	 * @return All Publications found in that bounding box.
	 */
	public Flux<ComparatorModel> findByBoundingBox(BoundingBox bbox);

	/**
	 * @return The id of that adapter. E.g. 'osm', 'kvm', 'weChange'.
	 */
	public String getAdapterId();
}
