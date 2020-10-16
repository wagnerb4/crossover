package crossover;

import view.View;

/**
 * A {@link Strategy strategy} used to compute the split of the search space elements in the {@link Crossover}.
 * The {@link Strategy#apply(View)} method will be given a {@link View view} on a search space element
 * containing the complete solution part of the search space element and the part of the problem part
 * matching the given {@link SearchSpaceElementSplitStrategy#problemSplitPart problemSplitPart}. The 
 * strategy is supposed to reduce the view by elements from the solution part.
 * @author Benjamin Wagner
 * @see Strategy
 * @see Strategy#apply(View)
 * @see Crossover#DEFAULT_STRATEGY
 */
public abstract class SearchSpaceElementSplitStrategy implements Strategy {
	
	/**
	 * The part of the problem split used in computing a part of the solution split.
	 */
	protected View problemSplitPart;
	
	/**
	 * Sets the part of the problem split used in the strategy.
	 * @param problemSplitPart The problem split part to use
	 */
	void setProblemSplitPart (final View problemSplitPart) {
		this.problemSplitPart = problemSplitPart;
	}
	
}
