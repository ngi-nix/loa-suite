package org.linkedopenactors.code.loaapp.controller.comperator;

import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.linkedopenactors.code.loaAlgorithm.AlgorithmName;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComparatorInput {
	private Model subjectA;
	private Model subjectB;
	
	// Das muss der Comperator wissen !! Das begründet seine existence
	@Deprecated
	private Set<AlgorithmName> algorithmNames;
}
