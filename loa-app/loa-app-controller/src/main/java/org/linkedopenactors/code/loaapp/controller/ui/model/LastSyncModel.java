package org.linkedopenactors.code.loaapp.controller.ui.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LastSyncModel {
	private String kvmLastSync;
	private String wechangeLastSync;
}
