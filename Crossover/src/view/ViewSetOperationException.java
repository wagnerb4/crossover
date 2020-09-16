package view;

/**
 * 
 * This exception is thrown by the {@link View#union(View)}, {@link View#subtract(View)} and 
 * {@link View#intersect(View)} methods indicating that the {@link View} is in an uncertain state.
 * @see ViewSetOperationException#savedState
 * @author Benjamin Wagner
 *
 */
public class ViewSetOperationException extends Exception {

	private static final long serialVersionUID = 1246076260391433892L;
	
	/**
	 * The state of the {@link View} the at the beginning of the set-operation. 
	 */
	private final View savedState;

	public ViewSetOperationException(String message, View savedState) {
		super(message);
		this.savedState = savedState;
	}

	/**
	 * @return Returns the by the set-operation unaltered {@link View}.
	 */
	public View getSavedState() {
		return savedState;
	}

}
