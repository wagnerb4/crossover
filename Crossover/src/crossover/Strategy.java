package crossover;

import view.View;

/**
 * The strategy used in the {@link Crossover} for splitting up the problem part.
 * A strategy is supposed to reduce the View by EObjects and References between them
 * thus resulting
 * 
 * @author Benjamin Wagner
 */
public interface Strategy {
	
	void apply (View view);

}
