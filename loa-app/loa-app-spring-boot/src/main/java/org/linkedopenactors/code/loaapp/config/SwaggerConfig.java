package org.linkedopenactors.code.loaapp.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Value("${app.version:unknown}")
    private String version;

    @Value("${app.baseNamespace}") 
    private String baseNamespace;
    
    @Value("${springdoc.swagger-ui.oauth.token-url}")
    private String tokenUrl;

    final String OAUTH2_SCHEMA_NAME = "Keycloak";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().servers(servers())
                .info(info())
                .components(securityComponents())
                .addSecurityItem(securityRequirement());
    }

    private List<Server> servers() {
		Server server = new Server();
		server.setUrl(baseNamespace);
		server.setDescription("LOA Environment");
		return List.of(server, server);
	}

	private Info info() {
        return new Info()
                .title("Linked Open Actors API")
                .description(
                        "")
                .version(version)
                .license(new License()
                        .name("European Union Public Licence 1.2")
                        .url("https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12"))
                .contact(new Contact()
                        .name("LinkedOpenActors")
                        .url("https://linkedopenactors.org"));
    }

    private Components securityComponents() {
        Scopes scopes = new Scopes();
        scopes.addString("*_read", "");
        scopes.addString("*_create_update", "");

        return new Components()
                .addSecuritySchemes(OAUTH2_SCHEMA_NAME, new io.swagger.v3.oas.models.security.SecurityScheme()
                        .name(OAUTH2_SCHEMA_NAME)
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows()
                                .password(new OAuthFlow()
                                        .tokenUrl(tokenUrl)
                                        .scopes(scopes))));
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList(OAUTH2_SCHEMA_NAME);
    }
}

