package org.linkedopenactors.code.kvmadapter.initial;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KvmCsv2RdfRepoInitialLoad {

	private KvmCsvRecord2PublicationComparatorModel kvmCsvRecord2PublicationComparatorModel;

	public KvmCsv2RdfRepoInitialLoad(KvmCsvRecord2PublicationComparatorModel kvmCsvRecord2PublicationComparatorModel) {
		this.kvmCsvRecord2PublicationComparatorModel = kvmCsvRecord2PublicationComparatorModel;
	}
	
	public long run(Repository repository) {
		String fileName = "kvm_initial_load.csv";
		ClassPathResource inputFile = new ClassPathResource(fileName);
		
		try(RepositoryConnection con = repository.getConnection()) {
			con.setNamespace("schema", "http://schema.org/");
			con.setNamespace("as", "http://www.w3.org/ns/activitystreams#");
			con.setNamespace("kvm", "http://kvm.org/ns#");
		}
		
		try {
			Reader in = new InputStreamReader(inputFile.getInputStream());
			log.info("loading: " + fileName);
			Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().withHeader(KvmCsvNames.class).parse(in);
			log.info("loaded: " + fileName);
			Stream<CSVRecord> stream = StreamSupport.stream(csvRecords.spliterator(), false);			
			return stream.parallel()
			.map(kvmCsvRecord2PublicationComparatorModel::convert)
			.map(extractObject-> {
				try(RepositoryConnection con = repository.getConnection()) {					
					con.add(extractObject.getModel(), extractObject.getSubject());
				}
				String subject = extractObject.getSubject().stringValue();
				return subject;
				}).count();
		} catch (Exception e) {
			throw new RuntimeException("error while initial load", e);
		}
	}
}
