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

	@Override
	public String toString() {
		return "Pair [" + (first != null ? "first=" + first + ", " : "") + (second != null ? "second=" + second : "")
				+ "]";
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
		
	}
		
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Pair<?, ?> other = (Pair<?, ?>) obj;
		
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		
		return true;
		
	}

}
