package de.naturzukunft.rdf4j.loarepository;

import java.io.StringWriter;
import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import de.naturzukunft.rdf4j.ommapper.BaseObject;
import de.naturzukunft.rdf4j.ommapper.Converter;
import de.naturzukunft.rdf4j.ommapper.ModelCreator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseObjectRepository <T extends BaseObject> {
	
	private Repository repository;
	private Converter<T> converter;
	private Namespace[] namespace;

	public BaseObjectRepository(Repository repository , Converter<T> converter, Namespace... namespace) {
		this.repository = repository;
		this.converter = converter;
		this.namespace = namespace;
	}

	public void save(T object) {
		try(RepositoryConnection con = repository.getConnection()) {
			log.info("-> "+getCaller()+"save " + object.getSubject() + "("+object.getClass().getSimpleName()+")");
			ModelCreator<T> mc = new ModelCreator<T>(object);
			Model model = mc.toModel(this.namespace);
			log.trace("saving: \n"+ toString(RDFFormat.TURTLE, model));
			con.add(model);			
		}
		log.debug("<- save " + object.getSubject());
	}

	private String getCaller() {
		String caller = "";
		if(log.isDebugEnabled()) {
			String className = new Throwable().getStackTrace()[2].getClassName();
			className = className.substring(className.lastIndexOf(".")+1);
			caller = className + "->";
		}
		return caller;
	}
	
	public static void main(String[] args) {
		String rdf4jServer = "https://rdf.dev.osalliance.com/rdf4j-server/";
		String repositoryID = "kvm";
		Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
		BaseObjectRepository<PostalAddressLoa> p = new BaseObjectRepository<PostalAddressLoa>(repo, new Converter<PostalAddressLoa>(PostalAddressLoa.class));
		
		p.save(PostalAddressLoa.builder()
		.subject(Values.iri("http://example.com"))
			.addressCountry("addressCountry")
			.build());
		p.remove(Values.iri("http://example.com"));
	}
	
	public void remove(IRI subject) {
		Optional<T> pa = read(subject);
		if(pa.isPresent()) {
			ModelCreator<T> mc = new ModelCreator<T>(pa.get());
			try(RepositoryConnection con = repository.getConnection()) {
			Model model = mc.toModel();
			log.trace(getCaller()+"removing: \n", toString(RDFFormat.TURTLE, model));
			con.remove(model);
			}
		}
	}
	
	public Optional<T> read(IRI subject) {
		// TODO		hier muss das complette model aufgebaut werden !!
		Model resultModel = readBySubject(subject);
		Optional<T> pa = converter.fromModel(subject, subRead(subject, resultModel));
		return pa;
	}

	protected Model readBySubject(IRI subject) {
		Model resultModel;		
		try(RepositoryConnection con = repository.getConnection()) {
			String query = "CONSTRUCT { ?s ?p ?o }\n"
					+ "WHERE {\n"
					+ "<"+subject.stringValue()+"> ?p ?o .\n"
					+ "BIND (<"+subject.stringValue()+"> as ?s) \n"
					+ "}\n"
					+ "";
			GraphQueryResult result = con.prepareGraphQuery(query).evaluate();
			resultModel = QueryResults.asModel(result);
		}
		return resultModel;
	}
	
	protected Model subRead(IRI subject ,Model mainResultModel) {
		return mainResultModel;
	}
	
	private static String toString(RDFFormat rdfFormat, Model model) {
		StringWriter sw = new StringWriter();
		Rio.write(model, sw, rdfFormat);		
		return sw.toString();
	}
	
	protected Repository getRepository() {
		return repository;
	}
}
