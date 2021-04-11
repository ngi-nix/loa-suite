package org.linkedopenactors.code.loaapp.controller.comperator;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.jupiter.api.Test;
import org.linkedopenactors.code.loaAlgorithm.AlgorithmName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ComperatorTest {

	@Autowired
	private LoaComparator comparator;
	
//	@Test
	public void test() {
		
		Model ma = new ModelBuilder()
			.subject(iri("http://example.com/subjectA"))
			.add("http://example.com/propertyP1", "testA1")
			.add("http://example.com/propertyP2", "testA2")
			.add("http://example.com/propertyP3", "adsfdsf")
			.build();
		
		Model mb = new ModelBuilder()
				.subject(iri("http://example.com/subjectB"))
				.add("http://example.com/propertyP1", "testB1")
				.add("http://example.com/propertyP2", "testB2")
				.add("http://example.com/propertyP3", "trezuerz")
				.build();
		
		ComparatorInput ci = ComparatorInput.builder()
			.subjectA(ma)
			.subjectB(mb)
			.algorithmNames(Set.of(AlgorithmName.fuzzySearchRatioAlgorithm))
			.build();
				
		List<ComparatorOutput> co = comparator.compare(ci);
		co.forEach(System.out::println);
	}
}
