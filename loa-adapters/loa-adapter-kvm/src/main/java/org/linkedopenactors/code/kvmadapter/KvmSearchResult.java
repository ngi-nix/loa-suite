package org.linkedopenactors.code.kvmadapter;

import java.util.List;

import lombok.Data;

@Data
public class KvmSearchResult {
	private List<KvmEntry> visible;
	private List<KvmEntry> invisible;
}
