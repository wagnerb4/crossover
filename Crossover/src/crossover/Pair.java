package crossover;

/**
 * Realises a pair of two objects.
 * @author Benjamin Wagner
 * @param <K> The type of first object in the pair.
 * @param <V> The type of the second object in the pair.
 */
public class Pair<K, V> {

	private final K first;
	private final V second;
	
	/**
	 * Create a new pair of the given objects.
	 * @param first the first of object of type {@code T}
	 * @param second the second of object of type {@code T}
	 */
	public Pair(K first, V second) {
		this.first = first;
		this.second = second;
	}

	public K getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}
	
}
