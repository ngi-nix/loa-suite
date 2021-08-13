package org.linkedopenactors.code.kvmadapter;

import org.linkedopenactors.code.comparator.ComparatorModel;
import org.linkedopenactors.code.similaritychecker.BoundingBox;
import org.linkedopenactors.code.similaritychecker.SimilarityCheckerLoaAdapter;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class KvmLoaAdapter implements SimilarityCheckerLoaAdapter {
	
	private KVMRestEndpoint kvmRestEndpoint;
	private KvmEntry2PublicationComparatorModel kvmEntry2PublicationComparatorModel;

	public KvmLoaAdapter(KVMRestEndpoint kvmRestEndpoint, KvmEntry2PublicationComparatorModel kvmEntry2PublicationComparatorModel) {
		this.kvmRestEndpoint = kvmRestEndpoint;
		this.kvmEntry2PublicationComparatorModel = kvmEntry2PublicationComparatorModel;		
	}
	
	@Override
	public Flux<ComparatorModel> findByBoundingBox(BoundingBox boundingBox) {
		return kvmRestEndpoint
				.findByBoundingBox(boundingBox.getLatleftTop(), boundingBox.getLngleftTop(),
						boundingBox.getLatRightBottom(), boundingBox.getLngRightBottom())
				.map(kvmEntry -> kvmEntry2PublicationComparatorModel.convert(kvmEntry));// .collect(Collectors.toSet());
	}

	@Override
	public String getAdapterId() {
		return "kvm";
	}
}

