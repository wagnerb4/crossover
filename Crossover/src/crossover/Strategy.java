package crossover;

import view.View;

/**
 * The strategy used in the {@link Crossover} for splitting up the problem part.
 * 
 * @author Benjamin Wagner
 */
public interface Strategy {
	
	void apply (View view);

}
