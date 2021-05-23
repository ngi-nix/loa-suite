package de.naturzukunft.rdf4j.loarepository;

import static org.eclipse.rdf4j.model.util.Values.literal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;

import de.naturzukunft.rdf4j.ommapper.Converter;
import de.naturzukunft.rdf4j.vocabulary.SCHEMA_ORG;

//@Component
public class PublicationRepo extends BaseObjectRepository <PublicationLoa> implements LastSyncDateStore {

	public PublicationRepo(Repository repository, Converter<PublicationLoa> converter, Namespace... namespace) {		
		super(repository, converter, namespace);
	}

	@Override
	protected Model subRead(IRI publicationSubject, Model mainResultModel) {
		Optional<IRI> aboutSubjectOptional = Models.getPropertyIRI(mainResultModel, publicationSubject, SCHEMA_ORG.about);
		if(aboutSubjectOptional.isPresent()) { // Organisation
			IRI aboutSubject = aboutSubjectOptional.get();
			mainResultModel.addAll(readBySubject(aboutSubject));
			Optional<IRI> locationSubjectOptional = Models.getPropertyIRI(mainResultModel, aboutSubject, SCHEMA_ORG.location);
			if(locationSubjectOptional.isPresent()) { // location
				IRI locationSubject = locationSubjectOptional.get();
				mainResultModel.addAll(readBySubject(locationSubject));
				Optional<IRI> addressSubjectOptional = Models.getPropertyIRI(mainResultModel, locationSubject, SCHEMA_ORG.address);
				if(addressSubjectOptional.isPresent()) { // address
					IRI addressSubject = addressSubjectOptional.get();
					mainResultModel.addAll(readBySubject(addressSubject));
				}
			}
			Optional<IRI> contactPointSubjectOptional = Models.getPropertyIRI(mainResultModel, aboutSubject, SCHEMA_ORG.contactPoint);
			if(contactPointSubjectOptional.isPresent()) { // contactPoint
				IRI contactPointSubject = contactPointSubjectOptional.get();
				mainResultModel.addAll(readBySubject(contactPointSubject));				
			}
		}
		return mainResultModel;
	}
	
	public List<PublicationLoa> getByIdentifier(String identifier) {
		List<PublicationLoa> byIdentifier = new ArrayList<>();
		try(RepositoryConnection con = getRepository().getConnection())  {
			RepositoryResult<Statement> result = con.getStatements(null, SCHEMA_ORG.identifier, literal(identifier));
			for (Statement statement : result) {
				read((IRI)statement.getSubject()).ifPresent(it->byIdentifier.add(it));
			}
		}
		return byIdentifier;
	}
	
	public Optional<PublicationLoa> getBySubject(IRI subject) {
		List<PublicationLoa> bySubject = new ArrayList<>();
		try(RepositoryConnection con = getRepository().getConnection())  {
			RepositoryResult<Statement> result = con.getStatements(subject, RDF.TYPE, SCHEMA_ORG.CreativeWork);
			for (Statement statement : result) {
				read((IRI)statement.getSubject()).ifPresent(it->bySubject.add(it));
			}
		}
		if(bySubject.size()>1) {
			throw new RuntimeException("there are more then one statement with that subject and RDF.TYPE CreativeWork !?!?!");
		}
		return Optional.ofNullable(bySubject.get(0));
	}
	
	@Override
	public void lastSync(IRI subject, LocalDateTime lastSyncDate) {
		try(RepositoryConnection con =getRepository().getConnection()) {
			RepositoryResult<Statement> res = con.getStatements(subject, Values.iri("http://linkedopenactors.ord/lastSyncDate"), null);
			con.remove(res);			
			con.add(subject, Values.iri("http://linkedopenactors.ord/lastSyncDate"), Values.literal(lastSyncDate));
		}
	}
	
	@Override
	public Optional<LocalDateTime> lastSyncDate(IRI subject) {
		try(RepositoryConnection con =getRepository().getConnection()) {
			RepositoryResult<Statement> res = con.getStatements(subject, Values.iri("http://linkedopenactors.ord/lastSyncDate"), null);
			
			return res.stream().findFirst()
					.map(stmt->stmt.getObject())
					.map(it->((Literal)it).temporalAccessorValue())
					.map(LocalDateTime::from);
		}
	}
}
