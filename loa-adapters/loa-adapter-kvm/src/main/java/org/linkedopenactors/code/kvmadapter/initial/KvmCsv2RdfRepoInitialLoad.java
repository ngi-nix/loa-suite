package org.linkedopenactors.code.kvmadapter.initial;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.linkedopenactors.code.comparator.ComparatorModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KvmCsv2RdfRepoInitialLoad {

	private KvmCsvRecord2PublicationComparatorModel kvmCsvRecord2PublicationComparatorModel;

	public KvmCsv2RdfRepoInitialLoad(KvmCsvRecord2PublicationComparatorModel kvmCsvRecord2PublicationComparatorModel) {
		this.kvmCsvRecord2PublicationComparatorModel = kvmCsvRecord2PublicationComparatorModel;
	}
	
	public long run(Repository repository) {
		log.debug("->" + this.getClass().getSimpleName() + " starting initialization.");
		String fileName = "kvm_initial_load.csv";
		ClassPathResource inputFile = new ClassPathResource(fileName);
		
		try {
			Reader in = new InputStreamReader(inputFile.getInputStream());
			Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().withHeader(KvmCsvNames.class).parse(in);
			log.info("loaded: " + fileName);
			Stream<CSVRecord> stream = StreamSupport.stream(csvRecords.spliterator(), false);			
			
			Set<Statement> statements =
					stream.parallel()
					.map(kvmCsvRecord2PublicationComparatorModel::convert)					
					.map(ComparatorModel::getModel)
					.flatMap(listContainer -> listContainer.stream())
					.collect(Collectors.toSet());
			
			toRdfStore(repository, statements);
			try(RepositoryConnection conMain = repository.getConnection()) {
				int size = Iterations.asList(conMain.getStatements(null, RDF.TYPE, SCHEMA_ORG.CreativeWork)).size();
				log.debug("initial load added " + size + " creativeWorks (LOA Publications)");
				return size;
			}
		} catch (Exception e) {
			throw new RuntimeException("error while initial load", e);
		}
	}
	
	public Set<Statement> toRdfStore(Repository repository, Set<Statement> statements) {
		log.debug("now adding " + statements.size() + " statements to the repository " + repository);
		try(RepositoryConnection conMain = repository.getConnection()) {
			conMain.begin();
			try {
				conMain.setNamespace("schema", "http://schema.org/");
				conMain.setNamespace("as", "http://www.w3.org/ns/activitystreams#");
				conMain.setNamespace("kvm", "http://kvm.org/ns#");
				conMain.add(statements);				
				conMain.commit();	
				log.debug("statements added sucessfully");
			} catch (Throwable e) {
				log.error("ROLLBACK: " + e.getMessage());
				conMain.rollback();
				return Collections.emptySet();
			}
		}
		return statements;
	}
}
