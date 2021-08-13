package org.linkedopenactors.code.kvmadapter.initial;

import static org.eclipse.rdf4j.model.util.Values.iri;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import de.naturzukunft.rdf4j.loarepository.ContactPointLoa;
import de.naturzukunft.rdf4j.loarepository.OrgansationLoa;
import de.naturzukunft.rdf4j.loarepository.OrgansationLoa.OrgansationLoaBuilder;
import de.naturzukunft.rdf4j.loarepository.PlaceLoa;
import de.naturzukunft.rdf4j.loarepository.PostalAddressLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa;
import de.naturzukunft.rdf4j.loarepository.PublicationLoa.PublicationLoaBuilder;
import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import de.naturzukunft.rdf4j.vocabulary.AS;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class InitialLoadTurtleFileCrator {

	// Start me as java app in IDE
	// The file is vcreated in target folder.
	
	private static final String rdfjRepoUrl = "https://rdf.dev.osalliance.com/rdf4j-server/repositories/";
	
	public static void main(String[] args) throws Exception {
		InitialLoadTurtleFileCrator creator = new InitialLoadTurtleFileCrator();
		creator.createFile();
	}

	private void createFile() throws Exception {

//		String repoId = "kvm22";
//		if(!repoExists(repoId)) {
//			createRepo(repoId);
//		}
		Model m = null;
		ClassPathResource inputFile = new ClassPathResource("kvm_initial_load.csv");
		Iterable<CSVRecord> csvRecords = getCsvRecords(inputFile);
//		StreamSupport.stream(csvRecords.spliterator(), true).parallel();
		int entries = 0;		
		
		for (CSVRecord csvRecord : csvRecords) {
			log.debug("processing csv entry " + entries);
			entries++;
			PublicationLoa publicationLoa = convert(csvRecord);
			ModelCreator<PublicationLoa> mc = new ModelCreator<PublicationLoa>(publicationLoa);
			if(m == null) {
				m = mc.toModel(getNamespaces());
			} else {
				m.addAll(mc.toModel(getNamespaces()));
			}			
		}		
		File f = new File("target/kvmInitialLoad.ttl");
		FileWriter fw = new FileWriter(f);
		
		m.filter(null, SCHEMA_ORG.latitude, null).stream().forEach(System.out::println);
		
		Rio.write(m, fw, RDFFormat.TURTLE);
		log.info("The input file " + inputFile.getFilename() + " was processed.");
		log.info("export kvm initial load file ("+entries+" entries) to: " + f.getAbsolutePath());
	}

	
	// TODO selbe wie config, zentralisieren !!
	private Namespace[] getNamespaces() {
		Namespace schema = new SimpleNamespace("schema", "http://schema.org/");
		Namespace as = new SimpleNamespace("as", "http://www.w3.org/ns/activitystreams#");
		Namespace kvm = new SimpleNamespace("kvm", "http://kvm.org/ns#");
		return new Namespace[] {schema, as, kvm};
	}

	
	private Iterable<CSVRecord> getCsvRecords(ClassPathResource cpr) throws Exception {
		Reader in = new InputStreamReader(cpr.getInputStream());
		return CSVFormat.DEFAULT.withFirstRecordAsHeader().withHeader(KvmCsvNames.class).parse(in);
	}
	
	public PublicationLoa convert(CSVRecord record) {
		String version = Optional.ofNullable(record.get(KvmCsvNames.version)).orElse("0");
		String baseSubjectWitVersion = "kvm:V" + version + "_" + record.get(KvmCsvNames.id);
		PostalAddressLoa postalAddress = PostalAddressLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/postalAddress"))
				.type(Set.of(SCHEMA_ORG.PostalAddress, AS.Object))
				.addressCountry(getText(record.get(KvmCsvNames.country)))
				.addressLocality(getText(record.get(KvmCsvNames.city)))
//				.addAddressRegion(entry.get)
				.streetAddress(getText(record.get(KvmCsvNames.street)))
				.postalCode(getText(record.get(KvmCsvNames.zip)))
				.build();

		String lat = record.get(KvmCsvNames.lat);
		Double latAsDouble = Double.parseDouble(lat);
		log.info("latAsDouble: " + lat + "/" +latAsDouble);
		String lng = record.get(KvmCsvNames.lng);
		Double lngAsDouble = Double.parseDouble(lng);
		log.info("lngAsDouble: " + lng + "/" +lngAsDouble);
		PlaceLoa place = PlaceLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/place"))
				.type(Set.of(SCHEMA_ORG.Place, AS.Object))				
				.latitude(latAsDouble)
				.longitude(lngAsDouble)
				.postalAddress(postalAddress)
				.build();

		ContactPointLoa contactPoint = ContactPointLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/contactPoint"))
				.type(Set.of(SCHEMA_ORG.ContactPoint, AS.Object))
				.email(record.get(KvmCsvNames.contact_email))
				.name(record.get(KvmCsvNames.contact_name))
				.telephone(record.get(KvmCsvNames.contact_phone))
				.build();

		
		
		OrgansationLoaBuilder organsationLoaBuilder = OrgansationLoa.builder()
				.subject(iri(baseSubjectWitVersion + "/organisation"))
				.type(Set.of(SCHEMA_ORG.Organization, AS.Object))
				.name(record.get(KvmCsvNames.title))
//				.addLegalName("Mannesmann AG")
				.placeLocation(place)
				.contactPoint(contactPoint);
		
		if( StringUtils.hasText(record.get(KvmCsvNames.homepage))) {
			try {
				organsationLoaBuilder.url(Set.of(iri(record.get(KvmCsvNames.homepage))));
			} catch (Exception e) {
				log.warn("IGNORING: " + record.get(KvmCsvNames.id) + " homepage is not a validf IRI: " + record.get(KvmCsvNames.homepage));
			}
		}
		
		OrgansationLoa organisation = organsationLoaBuilder.build();
		
		PublicationLoaBuilder publicationLoaBuilder = PublicationLoa.builder()
				.subject(iri(baseSubjectWitVersion ))
				.type(Set.of(SCHEMA_ORG.CreativeWork, AS.Object))
				.creativeWorkStatus(record.get(KvmCsvNames.state))
//		.addCopyrightNotice(entry.getc)
		.asName(record.get(KvmCsvNames.title))
		.description(record.get(KvmCsvNames.description))
		.identifier(record.get(KvmCsvNames.id))
		.license(record.get(KvmCsvNames.license))
		.version(version)
		.about(organisation);
		
		if(record.get(KvmCsvNames.tags)!=null) {
			publicationLoaBuilder.keywords(record.get(KvmCsvNames.tags));
		}
		
		if( record.get(KvmCsvNames.created_at) != null ) {
			long created = Long.parseLong(record.get(KvmCsvNames.created_at));
			LocalDateTime triggerTime =
			        LocalDateTime.ofInstant(Instant.ofEpochMilli(created), 
			                                TimeZone.getDefault().toZoneId());			
			publicationLoaBuilder.dateCreated(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(triggerTime));
		}		

		publicationLoaBuilder.dateModified(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()));
		PublicationLoa publication = publicationLoaBuilder.build();  
		return publication; 
	}
	
	private String getText(String text) {
		if(StringUtils.hasText(text)) {
			return text;
		}
		return null;
	}

	private boolean repoExists(String repoId) {		
		String url = rdfjRepoUrl + repoId + "?query=SELECT * WHERE { } LIMIT 10";				
		Flux<String> response = WebClient.builder().build()
		.get().uri(url)
		.retrieve()
		.bodyToFlux(String.class);		
		try {
			response.collectList().block();
			return true;
		} catch (Exception e) {
			return false;
		}		
	}
	
	private void createRepo(String repoId) {
		
		String payload = "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.\n"
				+ "@prefix rep: <http://www.openrdf.org/config/repository#>.\n"
				+ "@prefix sr: <http://www.openrdf.org/config/repository/sail#>.\n"
				+ "@prefix sail: <http://www.openrdf.org/config/sail#>.\n"
				+ "@prefix ms: <http://www.openrdf.org/config/sail/memory#>.\n"
				+ "\n"
				+ "[] a rep:Repository ;\n"
				+ "   rep:repositoryID \""+repoId+"\" ;\n"
				+ "   rdfs:label \"test memory store\" ;\n"
				+ "   rep:repositoryImpl [\n"
				+ "      rep:repositoryType \"openrdf:SailRepository\" ;\n"
				+ "      sr:sailImpl [\n"
				+ "	 sail:sailType \"openrdf:MemoryStore\" ;\n"
				+ "	 ms:persist true ;\n"
				+ "	 ms:syncDelay 120\n"
				+ "      ]\n"
				+ "   ].";
		
		String url = rdfjRepoUrl + repoId;				
		Flux<String> response = WebClient.builder()
//				.filters(exchangeFilterFunctions -> {
//				      exchangeFilterFunctions.add(logRequest());
//				      exchangeFilterFunctions.add(logResponse());
//				  })
				.build()
		.put().uri(url)
		.bodyValue(payload)
		.retrieve()
		.bodyToFlux(String.class);		
		
		log.info("response: " + response.collectList().block());
	}

	private ExchangeFilterFunction logResponse() {
		return ExchangeFilterFunction.ofResponseProcessor(clientRequest -> {
			log.info("#################");
	        if (log.isDebugEnabled()) {
	            StringBuilder sb = new StringBuilder("Request: \n");
	            //append clientRequest method and url
	            sb.append( clientRequest.bodyToMono(String.class) );
//	              .headers();
//	              .forEach((name, values) -> values.forEach(value -> /* append header key/value */));
	            
	              log.debug("########" + sb.toString());
	        }
	        return Mono.just(clientRequest);
	    });	}

	private ExchangeFilterFunction logRequest() {
	    return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
	        if (log.isDebugEnabled()) {
	            StringBuilder sb = new StringBuilder("Request: \n");
	            //append clientRequest method and url
	            sb.append( clientRequest.body() );
//	              .forEach((name, values) -> values.forEach(value -> /* append header key/value */));
	            
	              log.debug("########" + sb.toString());
	        }
	        return Mono.just(clientRequest);
	    });
	}
}
