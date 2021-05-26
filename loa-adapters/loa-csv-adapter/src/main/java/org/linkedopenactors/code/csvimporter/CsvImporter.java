package org.linkedopenactors.code.csvimporter;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMappingException;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import de.naturzukunft.rdf4j.loarepository.ContactPointLoa;
import de.naturzukunft.rdf4j.loarepository.OrgansationLoa;
import de.naturzukunft.rdf4j.loarepository.PlaceLoa;
import de.naturzukunft.rdf4j.loarepository.PostalAddressLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.SystemRepository;
import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CsvImporter {

	private static final String RDF4J_SERVER = "https://rdf.dev.osalliance.com/rdf4j-server";
	private String namespace = "http://loa.xy/";
	private RemoteRepositoryManager manager; 
	private SystemRepository systemrepository;
	
	public CsvImporter(SystemRepository systemrepository) {
		this.systemrepository = systemrepository;
		manager = new RemoteRepositoryManager(RDF4J_SERVER);
		manager.init();		
	}
	
	public Mono<ServerResponse> importCsv(ServerRequest request) {
		 Mono<FileData> path = request.multipartData()
				 .map(it -> it.get("file"))
				 .flatMapMany(Flux::fromIterable)
				 .cast(FilePart.class)
				 .flatMap(it -> {
					 Flux<DataBuffer> db = it.content();
					 try {
						 return Flux.just(new FileData(it.filename(), getInputStreamFromFluxDataBuffer(db)));
						 } catch (IOException e) {
							throw new RuntimeException(e);
						}
					}).next();
		 
			Mono<String> repositoryUrl = path.map(it->importCsvInternal(it)).map(repositoryId->String.format("The reopository with the id '%s' was created. \n"
					+ "Access it via rdf4j-workbench: https://rdf.dev.osalliance.com/rdf4j-workbench/repositories/%s/summary \n"
		 			+ "or as SPARQL endpoint: %s/%s/query", repositoryId, repositoryId, RDF4J_SERVER, repositoryId));
			return ServerResponse.ok().body(repositoryUrl, String.class);
	}
	
	public String importCsvInternal(FileData fileData) {
		return importCsvInternal(fileData.filename, fileData.inputStream);
	}
	
	public String importCsvInternal(String filename, InputStream inputStream) {		
		MappingIterator<Map<String, String>> mappingIterator = parseCsv(inputStream);
		Model all = new ModelBuilder().build();
		while (mappingIterator.hasNext()) {
			PublicationLoa publicationLoa = convert(mappingIterator.next(), UUID.randomUUID().toString());
			ModelCreator<PublicationLoa> mc = new ModelCreator<PublicationLoa>(publicationLoa);
			all.addAll(mc.toModel());
			}
		int lastIndexOf = filename.lastIndexOf(".") == -1 ? filename.length() : filename.lastIndexOf(".");
		return save(filename.substring(0, lastIndexOf), all);
	}
	
	InputStream getInputStreamFromFluxDataBuffer(Flux<DataBuffer> data) throws IOException {
	    PipedOutputStream osPipe = new PipedOutputStream();
	    PipedInputStream isPipe = new PipedInputStream(osPipe);

	    DataBufferUtils.write(data, osPipe)
	            .doOnComplete(() -> {
	                try {
	                    osPipe.close();
	                } catch (IOException ignored) {
	                }
	            })
	            .subscribe(DataBufferUtils.releaseConsumer());
	    return isPipe;
	}
	
	private MappingIterator<Map<String, String>> parseCsv(InputStream is) {
		MappingIterator<Map<String, String>> mappingIterator;
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = CsvSchema.emptySchema().withHeader(); // use first row as header; otherwise defaults are fine
		try {
			mappingIterator = mapper.readerFor(Map.class)
			   .with(schema)
			   .readValues(is);
		} catch(CsvMappingException csvMappingException) {
			throw WebClientResponseException.create(400, HttpStatus.BAD_REQUEST.name() + " " + csvMappingException.getMessage(), null, csvMappingException.getMessage().getBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException("error reading csv file", e);
		}
		return mappingIterator;
	}

	private PublicationLoa convert(Map<String, String> rowAsMap, String uuid) {
		ContactPointLoa contactPointLoa = ContactPointLoa.builder()
				  .subject(iri(namespace + "contact_" + uuid))
				  .type(Set.of(SCHEMA_ORG.ContactPoint))
				  .email(rowAsMap.get("ContactPointLoa.email"))
				  .name(rowAsMap.get("ContactPointLoa.name"))
				  .telephone(rowAsMap.get("ContactPointLoa.telephone"))
				  .build();

		  PostalAddressLoa postalAddressLoa = PostalAddressLoa.builder()
				  .subject(iri(namespace + "postalAddress_" + uuid))
				  .type(Set.of(SCHEMA_ORG.PostalAddress))
				  .postalCode(rowAsMap.get("PostalAddressLoa.postalCode"))
				  .addressLocality(rowAsMap.get("PostalAddressLoa.addressLocality"))
				  .addressRegion(rowAsMap.get("PostalAddressLoa.addressRegion"))
				  .addressCountry(rowAsMap.get("PostalAddressLoa.addressCountry"))
				  .streetAddress(rowAsMap.get("PostalAddressLoa.streetAddress"))
				  .build();
			
		  PlaceLoa placeLoa = PlaceLoa.builder()
				  .subject(iri(namespace + "place_" + uuid))
				  .type(Set.of(SCHEMA_ORG.Place))
				  .latitude(Double.valueOf(rowAsMap.get("PlaceLoa.latitude")))
				  .longitude(Double.valueOf(rowAsMap.get("PlaceLoa.longitude")))
				  .postalAddress(postalAddressLoa)
				  .build();
		  
		  OrgansationLoa organsationLoa = OrgansationLoa.builder()
				  .subject(iri(namespace + "organisation_" + uuid))
				  .legalName(rowAsMap.get("OrgansationLoa.legalName"))
				  .type(Set.of(SCHEMA_ORG.Organization))
				  .name(rowAsMap.get("OrgansationLoa.name"))
				  .url(Arrays.stream(rowAsMap.get("OrgansationLoa.url").split(",")).map(Values::iri).collect(Collectors.toSet()))
				  .placeLocation(placeLoa)
				  .contactPoint(contactPointLoa)
				  .build();
		  
		  PublicationLoa publicationLoa = PublicationLoa.builder()
				  .subject(iri(namespace + "publication_" + uuid))
				  .type(Set.of(SCHEMA_ORG.CreativeWork))
				  .version(rowAsMap.get("PublicationLoa.version"))
				  .copyrightNotice(rowAsMap.get("PublicationLoa.copyrightNotice"))
				  .creativeWorkStatus(rowAsMap.get("PublicationLoa.creativeWorkStatus"))
				  .license(rowAsMap.get("PublicationLoa.license"))
				  .keywords(rowAsMap.get("PublicationLoa.keywords"))
				  .identifier(rowAsMap.get("PublicationLoa.identifier"))
				  .description(rowAsMap.get("PublicationLoa.description"))
				  .about(organsationLoa)
				.build();
		  
		  // sollte ein Set sein !?
//		  if(StringUtils.hasText(rowAsMap.get("PublicationLoa.keywords"))) {
//			  List.of(rowAsMap.get("PublicationLoa.keywords").split(",")).forEach(tag->)
//		  }
		  if(StringUtils.hasText(rowAsMap.get("PublicationLoa.dateCreated"))) {
			  publicationLoa.setDateCreated(toDate(rowAsMap.get("PublicationLoa.dateCreated")));
		  }
		  if(StringUtils.hasText(rowAsMap.get("PublicationLoa.dateModified"))) {
			  publicationLoa.setDateCreated(toDate(rowAsMap.get("PublicationLoa.dateModified")));
		  }
		  
		return publicationLoa;
	}

	private String toDate(String dateCreated) {
		return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(dateCreated));
	}

	private String save(String repositoryId, Model model) {
		String addTempRepositoryId = systemrepository.addTempRepository(repositoryId);
		Repository repository = systemrepository.getRepository(addTempRepositoryId);
		try( RepositoryConnection con = repository.getConnection()) {
			con.add(model);
		}
		return addTempRepositoryId;
	}
	

	private class FileData  {
		
		public FileData(String filename, InputStream inputStream) {
			this.filename = filename;
			this.inputStream = inputStream;
		}
		String filename;
		InputStream inputStream;
	}
}
