package org.linkedopenactors.code.wechangeadapter;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.ommapper.Converter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WeChangeAdapter {

	private static final String WECHANGE_SEARCH_URL = "https://organisations.staging.wechange.de/api/v2/organizations/?last_modified__gt=";
	private WebClient webClient;
	private ObjectMapper objectMapper;
	private Converter<PublicationLoa> converter;
	 
	public WeChangeAdapter() {
		this.webClient = WebClient.builder().build();
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		converter = new Converter<>(PublicationLoa.class);
	}
	
	public static void main (String[] args) {
		WeChangeAdapter w = new WeChangeAdapter();
		w.getChangedSince(LocalDateTime.now().minusMonths(2)).forEach(it->log.info(it.toString()));
	}
	
	public List<PublicationLoa> getChangedSince(LocalDateTime since) {		
		List<IRI> pubs = getPublicationsSince(since);
		List<PublicationLoa> result = pubs.stream().map(this::getPublication).map(Optional::get).collect(Collectors.toList());
		return result;
	} 
	
	private Optional<PublicationLoa> getPublication(IRI id) {
		String body = webClient.get().uri(id.stringValue()).header("Accept","text/turtle").retrieve().bodyToMono(String.class).block();

		StringReader sr = new StringReader(body);
		try {
			Model model = Rio.parse(sr, RDFFormat.TURTLE);
			
			Optional<PublicationLoa> publicationLoa = converter.fromModel(id,  model);

			// check Dateformat (exception if wrong)
			String dateCreated = publicationLoa.get().getDateCreated();
			LocalDateTime.parse(dateCreated, DateTimeFormatter.ISO_DATE_TIME);

			return publicationLoa;
		} catch (RDFParseException | UnsupportedRDFormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<IRI> getPublicationsSince(LocalDateTime dateTime) {
		String url = WECHANGE_SEARCH_URL + DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
		String body = webClient.get().uri(url)
				.retrieve().bodyToMono(String.class).block();
		try {
			SearchResult sr = objectMapper.readValue(body, SearchResult.class);
			return Arrays.stream(sr.getResults())
					.map(pub->Values.iri(pub.getId()))
					.collect(Collectors.toList());
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
}
