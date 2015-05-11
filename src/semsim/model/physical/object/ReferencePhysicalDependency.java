package semsim.model.physical.object;

import java.net.URI;

import semsim.SemSimConstants;

public class ReferencePhysicalDependency extends PhysicalDependency{

	public ReferencePhysicalDependency(URI uri, String description){
		addReferenceOntologyAnnotation(SemSimConstants.REFERS_TO_RELATION, uri, description);
	}
	
	@Override
	protected boolean isEquivalent(Object obj) {
		return ((ReferencePhysicalDependency)obj).getReferstoURI().compareTo(referenceuri)==0;
	}
}
