package de.naturzukunft.rdf4j.loarepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.LocalDateTime;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.junit.jupiter.api.Test;

public class TestSystemRepository {

	@Test
	void test() {

		LoaRepositoryManager loaRepositoryManager = new RepositoryManagerLocal("https://loa.test.opensourceecology.de/system/", new LocalRepositoryManager(new File("target/repoManager")));
				
		String testRepositoryId = "testRepo"+System.currentTimeMillis();
		String repoId = loaRepositoryManager.addTempRepository(testRepositoryId);
		
		Repository tmpRepo = loaRepositoryManager.getRepository(repoId).orElseThrow(()->new RuntimeException("repo error"));
		
		assertTrue(tmpRepo.isInitialized());
		assertTrue(tmpRepo.isWritable());
		try {
			Thread.sleep(1200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		TempRepositoryCleaner tempRepositoryCleaner = new TempRepositoryCleaner(loaRepositoryManager);		
		tempRepositoryCleaner.tempRepoCleaner(LocalDateTime.now().minusSeconds(1));
		assertFalse(loaRepositoryManager.getRepository(repoId).isPresent());		
	}
}
