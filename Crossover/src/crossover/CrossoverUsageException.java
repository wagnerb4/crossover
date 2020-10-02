package crossover;

/**
 * This type of expection is thrown by the {@link Crossover}-Class
 * to indicate wrong usage.
 * 
 * @author Benjamin Wagner
 */
public class CrossoverUsageException extends Exception {

	private static final long serialVersionUID = 5365864471608098346L;

	public CrossoverUsageException(String message) {
		super(message);
	}

}
