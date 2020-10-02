package crossover;

import view.View;

import java.util.Set;

import org.eclipse.emf.henshin.model.Mapping;

public class CustomSpan {

	private final View intersection;
	private final Set<Mapping> mappingsOne;
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

}
