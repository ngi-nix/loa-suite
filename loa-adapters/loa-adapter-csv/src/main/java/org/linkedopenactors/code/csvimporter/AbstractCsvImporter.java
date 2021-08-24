package org.linkedopenactors.code.csvimporter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base implementation of {@link CsvImporter}.
 */
@Slf4j
public abstract class AbstractCsvImporter implements CsvImporter {

	/**
	 * Copies the records from the inputFile to the repository 
	 * @param repository The repository into which the records are to be imported.
	 * @param input The inputStream containing the records to import.
	 * @return The number of imported publications.
	 */
	@Override
	public long doImport(Repository repository, InputStream input) {
		log.debug("->" + this.getClass().getSimpleName() + " starting initialization.");
		
		try {
			Reader in = new InputStreamReader(input);
			Iterable<CSVRecord> csvRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().withHeader(getHeaderEnum()).parse(in);
			Stream<CSVRecord> stream = StreamSupport.stream(csvRecords.spliterator(), false);			
			
			Set<Statement> statements =
					stream.parallel()
					.map(this::convert)
					.map(SubjectModelPair::getModel)
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

	/**
	 * Converts a csv record into a set of statements (Model).
	 * @param record The csv record to convert
	 * @return A {@link SubjectModelPair} containing the data of the csv record.
	 */
	protected abstract SubjectModelPair convert(CSVRecord record);
	
	/**
	 * See {@link CSVFormat#withHeader(Class)}
	 * @return The enum containing the headers of the csv.
	 */
	protected abstract Class<? extends Enum<?>> getHeaderEnum();
	
	/**
	 * Adding the statements to the repository
	 * @param repository
	 * @param statements
	 * @return added statements
	 */
	private Set<Statement> toRdfStore(Repository repository, Set<Statement> statements) {
		log.debug("now adding " + statements.size() + " statements to the repository " + repository);
		try(RepositoryConnection conMain = repository.getConnection()) {
			conMain.begin();
			try {
				getNamespaces().forEach(ns->conMain.setNamespace(ns.getPrefix(), ns.getName()));
				conMain.add(statements);				
				conMain.commit();	
				log.debug("statements added sucessfully");
			} catch (Throwable e) {
				log.error("ROLLBACK: " + e.getMessage(), e);
				conMain.rollback();
				return Collections.emptySet();
			}
		}
		return statements;
	}
	
	/**
	 * @return The default namesüpaces, that we always expect/use.
	 */
	protected Set<Namespace> getNamespaces() {
		Set<Namespace> namespaces = new HashSet<>();
		namespaces.add(new SimpleNamespace("schema", "http://schema.org/"));
		namespaces.add(new SimpleNamespace("as", "http://www.w3.org/ns/activitystreams#"));
		namespaces.addAll(getAdditionalNamespaces());
		return namespaces;
	}
	
	/**
	 * Gives the implementer the possibility to add specific namespaces to the generated model.
	 * @return additional namespaces
	 */
	protected abstract Set<Namespace> getAdditionalNamespaces();
}
