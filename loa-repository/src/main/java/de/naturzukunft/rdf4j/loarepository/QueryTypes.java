package de.naturzukunft.rdf4j.loarepository;

import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterFactory;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterRegistry;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;

enum QueryTypes {
	CONSTRUCT_OR_DESCRIBE(q -> q instanceof GraphQuery, RDFFormat.TURTLE, RDFFormat.NTRIPLES, RDFFormat.JSONLD,
			RDFFormat.RDFXML) {
		@Override
		protected void evaluate(Query q, String acceptHeader, OutputStream outputStream, String defaultGraphUri,
				String namedGraphUri)
				throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException {
			GraphQuery gq = (GraphQuery) q;
			RDFFormat format = (RDFFormat) bestFormat(acceptHeader);
			gq.evaluate(Rio.createWriter(format, outputStream));
		}
	},
	SELECT(q -> q instanceof TupleQuery, TupleQueryResultFormat.JSON, TupleQueryResultFormat.SPARQL,
			TupleQueryResultFormat.CSV, TupleQueryResultFormat.TSV) {
		@Override
		protected void evaluate(Query q, String acceptHeader, OutputStream outputStream, String defaultGraphUri,
				String namedGraphUri)
				throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException {
			TupleQuery tq = (TupleQuery) q;
			QueryResultFormat format = (QueryResultFormat) bestFormat(acceptHeader);
			tq.evaluate(QueryResultIO.createTupleWriter(format, outputStream));
		}
	},

	ASK(q -> q instanceof BooleanQuery, BooleanQueryResultFormat.TEXT, BooleanQueryResultFormat.JSON,
			BooleanQueryResultFormat.SPARQL) {
		@Override
		protected void evaluate(Query q, String acceptHeader, OutputStream outputStream, String defaultGraphUri,
				String namedGraphUri)
				throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException {
			BooleanQuery bq = (BooleanQuery) q;
			QueryResultFormat format = (QueryResultFormat) bestFormat(acceptHeader);
			final Optional<BooleanQueryResultWriterFactory> optional = BooleanQueryResultWriterRegistry
					.getInstance().get(format);
			if (optional.isPresent()) {

				BooleanQueryResultWriter writer = optional.get().getWriter(outputStream);
				writer.handleBoolean(bq.evaluate());
			}

		}
	};

	private final FileFormat[] formats;
	private final Predicate<Query> typeChecker;

	QueryTypes(Predicate<Query> typeChecker, FileFormat... formats) {
		this.typeChecker = typeChecker;
		this.formats = formats;
	}
	
	/**
	 * Test if the query is of a type that can be answered. And that the accept
	 * headers allow for the response to be send.
	 * 
	 * @param preparedQuery
	 * @param acceptHeader
	 * @return true if the query is of the right type and acceptHeaders are
	 *         acceptable.
	 * @throws RuntimeException
	 */
	protected boolean accepts(Query preparedQuery, String acceptHeader) {
		if (accepts(preparedQuery)) {
			if (acceptHeader == null || acceptHeader.isEmpty()) {
				return true;
			} else {
				for (FileFormat format : formats) {
					for (String mimeType : format.getMIMETypes()) {
						if (acceptHeader.contains(mimeType))
							return true;
					}
				}
			}
			throw new RuntimeException("AcceptHeader mismatching!");
		}
		return false;
	}

	protected abstract void evaluate(Query q, String acceptHeader, OutputStream outputStream,
			String defaultGraphUri, String namedGraphUri)
			throws QueryEvaluationException, RDFHandlerException, UnsupportedRDFormatException;

	protected boolean accepts(Query q) {
		return typeChecker.test(q);
	};

	protected FileFormat bestFormat(String acceptHeader) {
		if (acceptHeader == null || acceptHeader.isEmpty()) {
			return formats[0];
		} else {
			for (FileFormat format : formats) {
				for (String mimeType : format.getMIMETypes()) {
					if (acceptHeader.contains(mimeType))
						return format;
				}
			}
		}
		return formats[0];
	}
}
