package org.linkedopenactors.code.csvimporter;

import java.io.InputStream;

import org.eclipse.rdf4j.repository.Repository;

/**
 * Interface for all importers, that imports csv files.
 */
public interface CsvImporter {

	/**
	 * Copies the records from the inputFile to the repository 
	 * @param repository The repository into which the records are to be imported.
	 * @param input The inputStream containing the records to import.
	 * @return The number of imported publications.
	 */
	long doImport(Repository repository, InputStream input);

}