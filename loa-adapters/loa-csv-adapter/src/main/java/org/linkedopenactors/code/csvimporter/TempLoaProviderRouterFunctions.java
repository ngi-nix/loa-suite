package org.linkedopenactors.code.csvimporter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class TempLoaProviderRouterFunctions {

	private final CsvImporter csvImporter;

	@Bean
	public RouterFunction<?> postActivityOutbox() {
		return RouterFunctions.route(RequestPredicates.POST("/tools/createTempRepo"), serverRequest -> {			
					return csvImporter.importCsv(serverRequest);
				});
	}
}
