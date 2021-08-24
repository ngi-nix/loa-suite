package de.naturzukunft.rdf4j.loarepository;

import java.io.OutputStream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.stereotype.Component;

@Component
public class SparqlQueryEvaluatorRdf4j implements SparqlQueryEvaluator {
	

	@Override
	public void evaluate(Repository repository, String query, String acceptHeader, String defaultGraphUri,
			String namedGraphUri, OutputStream outputStream ) {
		try (RepositoryConnection connection = repository.getConnection()) {
			Query preparedQuery = connection.prepareQuery(QueryLanguage.SPARQL, query);
			setQueryDataSet(preparedQuery, defaultGraphUri, namedGraphUri, connection);
			for (QueryTypes qt : QueryTypes.values()) {
				if (qt.accepts(preparedQuery, acceptHeader)) {
					qt.evaluate(preparedQuery, acceptHeader, outputStream, defaultGraphUri, namedGraphUri);
				}
			}
		}
	}
	/**
	 * @see https://www.w3.org/TR/sparql11-protocol/#dataset
	 * @param q the query
	 * @param defaultGraphUri
	 * @param namedGraphUri
	 * @param connection
	 */
	private void setQueryDataSet(Query q, String defaultGraphUri, String namedGraphUri, RepositoryConnection connection) {
		if (defaultGraphUri != null || namedGraphUri != null) {
			SimpleDataset dataset = new SimpleDataset();

			if (defaultGraphUri != null) {
				IRI defaultIri = connection.getValueFactory().createIRI(defaultGraphUri);
				dataset.addDefaultGraph(defaultIri);
			}
			
			if (namedGraphUri != null) {
				IRI namedIri = connection.getValueFactory().createIRI(namedGraphUri);
				dataset.addNamedGraph(namedIri);
			}
			q.setDataset(dataset);
		}
	}

}
