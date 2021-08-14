package org.linkedopenactors.code.wechangeadapter;

import lombok.Data;

@Data
public class SearchResult {
	private int count;
	private Publication[] results;
}
