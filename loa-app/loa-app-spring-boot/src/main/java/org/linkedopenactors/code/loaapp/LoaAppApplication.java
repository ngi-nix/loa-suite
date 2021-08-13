package org.linkedopenactors.code.loaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.solr.SolrAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude={SolrAutoConfiguration.class})
@EnableScheduling
@ComponentScan(basePackages = {
		"org.linkedopenactors.code.wechangeadapter",
		"org.linkedopenactors.code.kvmadapter",
		"org.linkedopenactors.code", 
		"org.linkedopenactors.code.distancecalculator", 
		"org.linkedopenactors.code.loaAlgorithm",
		"de.naturzukunft.rdf4j.loarepository",
		"org.linkedopenactors.code.loaapp.config"})
public class LoaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoaAppApplication.class, args);
	}

}
