package org.linkedopenactors.code.kvmadapter;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class KvmScheduler {

	private KvmSync updateChangedKvmEntries; 	
	
	public KvmScheduler(KvmSync kvmSync ) {
		this.updateChangedKvmEntries = kvmSync;
	}
	
	@Scheduled(fixedRate = 60000, initialDelay = 30000)
	public void getKvmUpdate() {
		updateChangedKvmEntries.sync().block();
	}
}
