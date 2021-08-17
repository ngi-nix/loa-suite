package org.linkedopenactors.code.kvmadapter.initial;

import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.linkedopenactors.code.csvimporter.AbstractCsvImporter;
import org.linkedopenactors.code.csvimporter.CsvImporter;
import org.linkedopenactors.code.csvimporter.SubjectModelPair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * An implmentation of the {@link CsvImporter} for importing openfairdb specific csv files (https://github.com/kartevonmorgen/openfairdb).
 * The columns of the csv file has to be like {@link KvmCsvNames}.
 */
@Component
@Qualifier(value = "KvmCsvImporter")
public class KvmCsvImporter extends AbstractCsvImporter {

	private KvmCsvRecord2PublicationLoaModel kvmCsvRecord2PublicationComparatorModel;
	
	public KvmCsvImporter( KvmCsvRecord2PublicationLoaModel kvmCsvRecord2PublicationComparatorModel) {
		this.kvmCsvRecord2PublicationComparatorModel = kvmCsvRecord2PublicationComparatorModel;
	}
	
	@Override
	protected SubjectModelPair convert(CSVRecord record) {
		return this.kvmCsvRecord2PublicationComparatorModel.convert(record);
	}

	@Override
	protected Class<? extends Enum<?>> getHeaderEnum() {
		return KvmCsvNames.class;
	}

	@Override
	protected Set<Namespace> getAdditionalNamespaces() {
		return Set.of(new SimpleNamespace("kvm", "http://kvm.org/ns#"));
	}
}
