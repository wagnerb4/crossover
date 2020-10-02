package crossover;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.emf.henshin.model.Mapping;

import view.View;

public class MatchingUtil {
	
	/**
	 * Finds all possible matches from {@link View fromView} to the {@link View toView} that project
	 * the part in the {@link View identityView} to itself. A <i>match</i> is a set of {@link Mapping mappings}
	 * from the {@link View#getNodes() nodes} of the {@link View fromView} to those of the {@link View toView}.
	 * 
	 * @param fromView the {@link View view} containing the domain of the mapping
	 * @param toView  the {@link View} containing the codomain of the mapping
	 * @param identityView the {@link View} containing the identity of the mapping
	 * @return Returns an {@link Iterator} of all possible {@link Mapping matches} satisfying the condition described above.
	 */
	public static Iterator<Set<Mapping>> findMatches (View fromView, View toView, View identityView) {
		// TODO: implement
		return null;
	}

}
