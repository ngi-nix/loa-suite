package org.linkedopenactors.code.csvimporter;

import java.util.Set;

import org.apache.commons.csv.CSVRecord;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * An implmentation of the {@link CsvImporter} for importing loa specific csv files.
 * The columns of the csv file has to be like {@link GenericCsvNames}.
 */
@Component
@Qualifier(value = "GenericCsvImporter")
class GenericCsvImporter extends AbstractCsvImporter {
	
	private GenericCsvRecord2PublicationLoaModel genericCsvRecord2PublicationLoaModel;

	public GenericCsvImporter(GenericCsvRecord2PublicationLoaModel genericCsvRecord2PublicationLoaModel) {
		this.genericCsvRecord2PublicationLoaModel = genericCsvRecord2PublicationLoaModel;
	}
	
	@Override
	protected SubjectModelPair convert(CSVRecord record) {
		return this.genericCsvRecord2PublicationLoaModel.convert(record);
	}

	@Override
	protected Class<? extends Enum<?>> getHeaderEnum() {
		return GenericCsvNames.class;
	}

	@Override
	protected Set<Namespace> getAdditionalNamespaces() {
		return Set.of(
				new SimpleNamespace("kvm", "https://linkedopenactors.org/ns#")
				);
	}

	public enum GenericCsvNames {
		PublicationLoa_version, 
		PublicationLoa_copyrightNotice, 
		PublicationLoa_creativeWorkStatus,
		PublicationLoa_dateCreated, 
		PublicationLoa_dateModified, 
		PublicationLoa_license, 
		PublicationLoa_keywords,
		PublicationLoa_identifier, 
		PublicationLoa_description, 
		OrgansationLoa_legalName, 
		OrgansationLoa_name,
		OrgansationLoa_url, 
		PlaceLoa_latitude, 
		PlaceLoa_longitude, 
		PostalAddressLoa_postalCode,
		PostalAddressLoa_addressLocality, 
		PostalAddressLoa_addressRegion, 
		PostalAddressLoa_addressCountry,
		PostalAddressLoa_streetAddress, 
		ContactPointLoa_email, 
		ContactPointLoa_name, 
		ContactPointLoa_telephone
	}
}
