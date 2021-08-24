package org.linkedopenactors.code.loaapp.controller.comperator;

import java.io.File;
import java.net.MalformedURLException;

import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.eclipse.rdf4j.repository.manager.RepositoryProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"org.linkedopenactors.code"})
public class TestApplication {

//	@Bean
//	public RepositoryManager getRepositoryManager() throws MalformedURLException {
//		RepositoryManager rm = RepositoryProvider.getRepositoryManager(new File("target/repoManager"));
//		rm.init();
//		return rm;
//	}
}
