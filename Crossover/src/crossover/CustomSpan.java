package crossover;

import view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;

/**
 * A Span contains a common subgraph (the intersection) of two {@link View views} A and B.
 * The {@link View views} are not required to have the same {@link View#getResource() resources}.
 * The Span also contains two {@link Set sets} of {@link Mapping mappings} mapping from the intersection
 * to the {@link View views}.
 * @author Benjamin Wagner
 */
public class CustomSpan {

	
	private final View intersection;
	
	/**
	 * Mappings from the {@link Node nodes} in the {@literal intersection} to the nodes of onother {@link View view}
	 */
	private final Set<Mapping> mappingsOne;
	
	/**
	 * Mappings from the {@link Node nodes} in the {@literal intersection} to the nodes of onother {@link View view}
	 */
	private final Set<Mapping> mappingsTwo;
	
	public CustomSpan (View intersection, Set<Mapping> mappingsOne, Set<Mapping> mappingsTwo) {
		
		this.intersection = intersection;
		this.mappingsOne = mappingsOne;
		this.mappingsTwo = mappingsTwo;
		
	}

	public View getIntersection () {
		return intersection;
	}

	public Set<Mapping> getMappingsOne () {
		return mappingsOne;
	}

	public Set<Mapping> getMappingsTwo () {
		return mappingsTwo;
	}

	@Override
	public int hashCode() {
		
		Map<EObject, Node> mapOne = new HashMap<>();
		Map<EObject, Node> mapTwo = new HashMap<>();
		mappingsOne.forEach(mapping -> mapOne.put(intersection.getObject(mapping.getOrigin()), mapping.getImage()));
		mappingsTwo.forEach(mapping -> mapTwo.put(intersection.getObject(mapping.getOrigin()), mapping.getImage()));
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((intersection == null) ? 0 : intersection.hashCode());
		result = prime * result + ((mappingsOne == null) ? 0 : mapOne.hashCode());
		result = prime * result + ((mappingsTwo == null) ? 0 : mapOne.hashCode());
		return result;
		
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (!(obj instanceof CustomSpan))
			return false;
		CustomSpan other = (CustomSpan) obj;
		if (intersection == null) {
			if (other.intersection != null)
				return false;
		} else if (!intersection.equals(other.intersection))
			return false;
		
		Map<EObject, Node> mapOne = new HashMap<>();
		Map<EObject, Node> mapTwo = new HashMap<>();
		mappingsOne.forEach(mapping -> mapOne.put(intersection.getObject(mapping.getOrigin()), mapping.getImage()));
		mappingsTwo.forEach(mapping -> mapTwo.put(intersection.getObject(mapping.getOrigin()), mapping.getImage()));
		
		Map<EObject, Node> mapOneOther = new HashMap<>();
		Map<EObject, Node> mapTwoOther = new HashMap<>();
		other.mappingsOne.forEach(mapping -> mapOneOther.put(other.intersection.getObject(mapping.getOrigin()), mapping.getImage()));
		other.mappingsTwo.forEach(mapping -> mapTwoOther.put(other.intersection.getObject(mapping.getOrigin()), mapping.getImage()));
		
		if (!mapOne.equals(mapOneOther))
			return false;
		if (!mapTwo.equals(mapTwoOther))
			return false;
		
		return true;
		
	}
	
}
