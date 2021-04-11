package de.naturzukunft.rdf4j.loarepository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;

public interface LastSyncDateStore {
	void lastSync(IRI subject, LocalDateTime lastSyncDate);
	Optional<LocalDateTime> lastSyncDate(IRI subject);
}
