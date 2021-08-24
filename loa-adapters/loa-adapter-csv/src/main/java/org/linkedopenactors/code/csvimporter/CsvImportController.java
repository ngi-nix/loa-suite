package org.linkedopenactors.code.csvimporter;

import java.io.InputStream;

import org.eclipse.rdf4j.repository.Repository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.naturzukunft.rdf4j.loarepository.LoaRepositoryManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;

@RestController
@Tag(name = "Generic CSV import controller", description = "Gives you the possibility to import csv files.")
public class CsvImportController {

	private CsvImporter csvImporter;
	private LoaRepositoryManager loaRepositoryManager;

	public CsvImportController( LoaRepositoryManager loaRepositoryManager, @Qualifier(value = "GenericCsvImporter") CsvImporter csvImporter) {		
		this.loaRepositoryManager = loaRepositoryManager;
		this.csvImporter = csvImporter;
	}
	
	@Operation(summary = "Imports entries of a csv file into a temporary repository.",
            description = "Imports entries of a csv file into a temporary repository. This repository will automatically deleted aftrer a few days. This operation was implemented for testing purposes",
			requestBody = @RequestBody(description = "The cvs file.", 
									required = true, 
									content = @Content(mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE, 
										examples = @ExampleObject(value = "pubs.csv")))
    )
	@ApiResponse(
            content = @Content(mediaType = "text/plain",
                    schema = @Schema(type="string")),description = "The repositoryId of the temporariliy created repository. You can use it as sparql endpoint like: http://localhost:8090/INSERT_repoId_HERE/sparql"
                    
    )	
	@PostMapping(value = "/tools/createTempRepo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public Flux<String> upload(ServerHttpRequest serverHttpRequest, @RequestPart("file") Flux<FilePart> filePartFlux) {
		Flux<String> repoIdFlux = filePartFlux.flatMap(filePart ->
			filePart.content()
				.map(dataBuffer ->dataBuffer.asInputStream())
				.map(inputStream -> doImport(filePart.filename(), inputStream)));
		return repoIdFlux.map(repoId->"http://" + serverHttpRequest.getURI().getHost() + ":" + serverHttpRequest.getURI().getPort() + "/" + repoId);
		
	}

	/**
	 * Copies all entries of the passed csv file input stream to a newly created temporary repository.
	 * @param filename The name of the csv file
	 * @param inputStream the content of the csv file
	 * @return The id of the temporary created repository.
	 */
	private String doImport(String filename, InputStream inputStream) {
		String addTempRepositoryId = getTempRepositoryId(filename);
		Repository repository = loaRepositoryManager.getMandatoryRepository(addTempRepositoryId);
		csvImporter.doImport(repository, inputStream);
		return addTempRepositoryId;
	}
	
	/**
	 * Creates a temporary repository with/for the passed filename.
	 * @param filename
	 * @return The id of the temporary created repository.
	 */
	private String getTempRepositoryId(String filename) {
		int lastIndexOf = filename.lastIndexOf(".") == -1 ? filename.length() : filename.lastIndexOf(".");
		String repositoryId = filename.substring(0, lastIndexOf);
		return loaRepositoryManager.addTempRepository(repositoryId);
	}
}
