package org.linkedopenactors.code.loaAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class AlgorithmRepository {

	private Map<AlgorithmName, LoaAlgorithm<?>> algorithmsByName = new HashMap<AlgorithmName, LoaAlgorithm<?>>();

	public AlgorithmRepository(List<LoaAlgorithm<?>> availableAlgorythms) {
		availableAlgorythms.stream().forEach(algo->algorithmsByName.put(AlgorithmName.valueOf(algo.getName()),algo));
	}

	public LoaAlgorithm<?> get(AlgorithmName algorithmName) {
		return algorithmsByName.get(algorithmName);
	}
}
