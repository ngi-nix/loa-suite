package de.naturzukunft.rdf4j.loarepository;

import java.io.OutputStream;

import org.eclipse.rdf4j.repository.Repository;

public interface SparqlQueryEvaluator {
	void evaluate(Repository repository, String query, String acceptHeader, String defaultGraphUri,
			String namedGraphUri, OutputStream outputStream );
}
