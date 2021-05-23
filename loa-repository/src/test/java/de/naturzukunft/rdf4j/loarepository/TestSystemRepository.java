package de.naturzukunft.rdf4j.loarepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.junit.jupiter.api.Test;

public class TestSystemRepository {

	private static final String RDF4J_SERVER = "https://rdf.dev.osalliance.com/rdf4j-server";
	
	@Test
	void test() {
		RemoteRepositoryManager manager = new RemoteRepositoryManager(RDF4J_SERVER);
		manager.init();		

		SimpleSystemRepository ssr = new SimpleSystemRepository("http:example.com");
		String testRepositoryId = "testRepo"+System.currentTimeMillis();
		String repoId = ssr.addTempRepository(testRepositoryId);
		
		Repository tmpRepo = manager.getRepository(repoId);
		assertTrue(tmpRepo.isInitialized());
		assertTrue(tmpRepo.isWritable());
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ssr.tempRepoCleaner(LocalDateTime.now().minusSeconds(1));
		tmpRepo = manager.getRepository(repoId);
		assertFalse(tmpRepo.isWritable());		
	}
}
