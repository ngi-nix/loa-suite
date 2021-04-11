package org.linkedopenactors.code.loaAlgorithm;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;

import de.naturzukunft.rdf4j.ommapper.BaseObject;
import de.naturzukunft.rdf4j.ommapper.Iri;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder

@EqualsAndHashCode(callSuper = true)
public abstract class AbstractLoaAlgorithm<T> extends BaseObject implements LoaAlgorithm<T> {

	public AbstractLoaAlgorithm(Class<?> cls, String baseNamespace, String description) {
		setName(StringUtils.uncapitalize(cls.getSimpleName()));
		setDescription(description);
		setType(Set.of(Values.iri("http://purl.org/spar/fabio/Algorithm")));
		setSubject(Values.iri(baseNamespace + "algorithm/" +getName()));
		setHowTo(Values.iri(getSubject().getNamespace() + "howTo" + getName()));
	}
	
	@Iri("http://schema.org/name" )
	private String name;
	
	@Iri("http://schema.org/description" )
	private String description;
	
	@Iri("http://schema.org/mainEntityOfPage" )
	private IRI howTo;
}
