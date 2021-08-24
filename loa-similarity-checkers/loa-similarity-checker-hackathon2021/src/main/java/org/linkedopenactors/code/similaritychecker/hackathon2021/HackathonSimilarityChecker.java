package org.linkedopenactors.code.similaritychecker.hackathon2021;

import java.util.Set;

import org.linkedopenactors.code.comparator.ComparatorModel;
import org.linkedopenactors.code.comparator.LoaComparatorResult;
import org.linkedopenactors.code.comparator.geocoordinates.GeoCoordinatesComparator;
import org.linkedopenactors.code.similaritychecker.LoaComparatorResultInterpretation;
import org.linkedopenactors.code.similaritychecker.SimilarityChecker;
import org.linkedopenactors.code.similaritychecker.SimilarityCheckerResult;
import org.springframework.stereotype.Component;

@Component
public class HackathonSimilarityChecker implements SimilarityChecker {

	private static final String LOA_SIMILARITY_CHECKER_HACKATHON2021 = "loa-similarity-checker-hackathon2021";
	private GeoCoordinatesComparator geoCoordinatesComparator;

	public HackathonSimilarityChecker(GeoCoordinatesComparator geoCoordinatesComparator) {
		this.geoCoordinatesComparator = geoCoordinatesComparator;
	}

	@Override
	public SimilarityCheckerResult check(ComparatorModel comparatorModelSource, ComparatorModel comparatorModelTarget) {

		LoaComparatorResult geoCoordinatesComparatorResult = geoCoordinatesComparator.compare(comparatorModelSource, comparatorModelTarget);
		double percent = geoCoordinatesComparatorResult.getResult();

		Set<LoaComparatorResultInterpretation> loaComparatorResultInterpretations = Set.of(LoaComparatorResultInterpretation.builder()
			.loaComparatorResult(geoCoordinatesComparatorResult)
			.similarityInPercent(toPercent(percent))
			.build());

		return SimilarityCheckerResult.builder()
			.comparatorModelOne(comparatorModelSource)
			.comparatorModelTwo(comparatorModelTarget)
			.idComparatorModelOne(comparatorModelSource.getSubject().stringValue())
			.idComparatorModelTwo(comparatorModelTarget.getSubject().stringValue())
			.loaComparatorResultInterpretations(loaComparatorResultInterpretations)
			.build();
	}

	private double toPercent(double percent) {
		if(percent>100) {
			percent = 100;
		}
		if(percent<0) {
			percent = 0;
		}
		return 100 - percent;
	}

	@Override
	public String getId() {
		return LOA_SIMILARITY_CHECKER_HACKATHON2021;
	}
}
