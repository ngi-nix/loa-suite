package de.naturzukunft.rdf4j.loarepository;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.core.io.ClassPathResource;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;

import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

public class JsonLdFormatter {
	
	public static String compact(Model model) {
		try {
			return toString(JsonLd
					.compact(toDocument(model), context())
					.compactToRelative(false).get());
		} catch (JsonLdError | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String frame(Model model) {
		try {
			return toString(JsonLd.frame(toDocument(model),context()).get());
		} catch (JsonLdError | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String flatten(Model model) {
		try {
			return toString(JsonLd.flatten(toDocument(model)).get());
		} catch (JsonLdError e) {
			throw new RuntimeException(e);
		}
	}

	private static Document toDocument(Model model) throws JsonLdError {
		StringWriter sw = new StringWriter();
		Rio.write(model, sw, RDFFormat.JSONLD);
		Document document = JsonDocument.of(new StringReader(sw.toString()));
		return document;
	}

	private static Document context() throws JsonLdError, IOException {
//		ClassPathResource cpr = new ClassPathResource("as.jsonld");
		ClassPathResource cpr = new ClassPathResource("context.jsonld");
		Document context = JsonDocument.of(cpr.getInputStream());
		return context;
	}

//	private static String toString(JsonObject o) {
//		StringWriter sw = new StringWriter();
//		JsonWriter jsonWriter = createJsonWriter(sw);
//		jsonWriter.write(o);
//		jsonWriter.close();
//		return sw.toString();
//	}

	private static JsonWriter createJsonWriter(StringWriter sw) {
		Map<String, Object> properties = new HashMap<>(1);
		properties.put(JsonGenerator.PRETTY_PRINTING, true);
		JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
		return writerFactory.createWriter(sw);
	}
	
	private static String toString(JsonStructure  o) {
		StringWriter sw = new StringWriter();
		JsonWriter jsonWriter = createJsonWriter(sw);
		jsonWriter.write(o);
		jsonWriter.close();
		return sw.toString();
	}
}
