package org.linkedopenactors.code.similaritychecker.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.linkedopenactors.code.similaritychecker.BoundingBox;
import org.linkedopenactors.code.similaritychecker.SimilarityChecker;
import org.linkedopenactors.code.similaritychecker.SimilarityCheckerLoaAdapter;
import org.linkedopenactors.code.similaritychecker.SimilarityCheckerResult;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Component
@Slf4j
class SimilarityCheckerFeeder {
	private List<SimilarityCheckerLoaAdapter> similarityCheckerLoaAdapters;
	private List<SimilarityChecker> similarityCheckers;
	
	public SimilarityCheckerFeeder(List<SimilarityCheckerLoaAdapter> similarityCheckerLoaAdapter, List<SimilarityChecker> similarityCheckers) {
		this.similarityCheckerLoaAdapters = similarityCheckerLoaAdapter;
		this.similarityCheckers = similarityCheckers;
	}
		
	public Flux<SimilarityCheckerResult> doFeed(String similarityCheckeriId, String sourceAdapterId, String targetAdapterId, BoundingBox boundingBox) {
		
		SimilarityCheckerLoaAdapter sourceLoaAdapter = similarityCheckerLoaAdapters.stream().filter(adapter -> sourceAdapterId.equals(adapter.getAdapterId()))
				.findFirst().orElseThrow(() -> new RuntimeException("no '"+sourceAdapterId+"' adapter registered!"));
		
		SimilarityCheckerLoaAdapter targetLoaAdapter = similarityCheckerLoaAdapters.stream().filter(adapter->targetAdapterId.equals(adapter.getAdapterId()))
				.findFirst().orElseThrow(() -> new RuntimeException("no '"+targetAdapterId+"' adapter registered!"));

		SimilarityChecker similarityChecker = similarityCheckers.stream().filter(simCheck->similarityCheckeriId.equals(simCheck.getId()))
				.findFirst().orElseThrow(() -> new RuntimeException("no '"+similarityCheckeriId+"' similarityChecker registered!"));

		log.debug("now getting data of boundingBox and execute similarityChecker.check()");
		
		AtomicInteger i=new AtomicInteger(0);
		return sourceLoaAdapter.findByBoundingBox(boundingBox)
				.flatMap(sourceComparatorModel -> targetLoaAdapter.findByBoundingBox(boundingBox)
						.map(targetComparatorModel -> similarityChecker.check(sourceComparatorModel, targetComparatorModel)))
				.doOnNext(it->log.debug("next ("+i.getAndIncrement()+"): " + it.getIdComparatorModelOne() + " - " + it.getIdComparatorModelTwo()));
	}
}
