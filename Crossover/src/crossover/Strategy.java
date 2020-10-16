package crossover;

import view.View;
import view.ViewSetOperationException;

/**
 * A strategy is used in the {@link Crossover} Class for splitting up views.
 * 
 * @author Benjamin Wagner
 * @see Strategy#apply(View)
 */
public interface Strategy {
	
	/**
	 * Applies the stategy on the given {@link View} changing it in the process.
	 * @param view the {@link View} to apply the strategy on
	 * @throws ViewSetOperationException in case set operations an a {@link View} used in the strategy fail
	 * @throws IllegalStateException in case a gerneral operation on a {@link view} failes
	 */
	void apply (View view) throws ViewSetOperationException, IllegalStateException;

}
