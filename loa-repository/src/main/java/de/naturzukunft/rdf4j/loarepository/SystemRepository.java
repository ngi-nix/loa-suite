package de.naturzukunft.rdf4j.loarepository;

import org.eclipse.rdf4j.repository.Repository;

public interface SystemRepository {
	String addTempRepository(String repositoryId);
	Repository getRepository(String repositoryId);
}