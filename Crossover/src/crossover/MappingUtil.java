package crossover;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.impl.MappingImpl;

/**
 * A utility class for working with {@link Set sets} of {@link Mapping mappings}.
 * @author Benjamin Wagner
 */
public class MappingUtil {

	/**
	 * Apply the mapper function to all origin nodes of given mapping.
	 * The original mapping won't be alterated.
	 * @param mappings The {@link Set set} of {@link Mapping mappings} to change
	 * @param mapper The function to apply to the {@link Mapping#getOrigin() origin-nodes}.
	 * @return Returns an alterated mapping.
	 */
	public static Set<Mapping> mapByOrigin (Set<Mapping> mappings, Function<Node, Node> mapper) {
		return mappings.stream().map(mapping -> {
			Mapping newMapping = new MappingImpl();
			newMapping.setImage(mapping.getImage());
			newMapping.setOrigin(mapper.apply(mapping.getOrigin()));
			return newMapping;
		}).collect(Collectors.toSet());
	}
	
	/**
	 * Apply the mapper function to all image nodes of given mapping.
	 * The original mapping won't be alterated.
	 * @param mappings The {@link Set set} of {@link Mapping mappings} to change
	 * @param mapper The function to apply to the {@link Mapping#getImage() image-nodes}.
	 * @return Returns an alterated mapping.
	 */
	public static Set<Mapping> mapByImage (Set<Mapping> mappings, Function<Node, Node> mapper) {
		return mappings.stream().map(mapping -> {
			Mapping newMapping = new MappingImpl();
			newMapping.setImage(mapper.apply(mapping.getImage()));
			newMapping.setOrigin(mapping.getOrigin());
			return newMapping;
		}).collect(Collectors.toSet());
	}
	
	/**
	 * Get the image of the the {@link Node origin} in the given {@link Set set} of {@link Mapping mappings}.
	 * @param mappings the {@link Set set} of {@link Mapping mappings} containging the {@link Node origin}
	 * @param origin the {@link Node node} to find the image of
	 * @return Returns a {@link Set set} of {@link Node nodes} consiting of all found {@link Mapping#getImage() image-nodes}
	 * or an empty {@link Set set}.
	 */
	public static Set<Node> getImageSet (Set<Mapping> mappings, Node origin) {
		return mappings.stream().
				filter(mapping -> mapping.getOrigin() == origin).
				map(Mapping::getImage).
				collect(Collectors.toSet());
	}
	
	/**
	 * Get the image of the the {@link Node origin} in the given {@link Set set} of {@link Mapping mappings}.
	 * @param mappings the {@link Set set} of {@link Mapping mappings} containging the {@link Node origin}
	 * @param origin the {@link Node node} to find the image of
	 * @return Returns the found {@link Mapping#getImage() image-node} or null if multiple or none were found.
	 */
	public static Node getImageSingle (Set<Mapping> mappings, Node origin) {
		Set<Node> imageSet = getImageSet(mappings, origin);
		if(imageSet.size() == 1) return imageSet.iterator().next();
		return null;
	}
	
	/**
	 * Get the inverse image of the the {@link Node image} in the given {@link Set set} of {@link Mapping mappings}.
	 * @param mappings the {@link Set set} of {@link Mapping mappings} containging the {@link Node image}
	 * @param image the {@link Node node} to find the inverse image of
	 * @return Returns a {@link Set set} of {@link Node nodes} consiting of all found {@link Mapping#getOrigin() origin-nodes}
	 * or an empty {@link Set set}.
	 */
	public static Set<Node> getInverseImageSet (Set<Mapping> mappings, Node image) {
		return mappings.stream().
				filter(mapping -> mapping.getImage() == image).
				map(Mapping::getOrigin).
				collect(Collectors.toSet());
	}
	
	/**
	 * Get the inverse image of the the {@link Node image} in the given {@link Set set} of {@link Mapping mappings}.
	 * @param mappings the {@link Set set} of {@link Mapping mappings} containging the {@link Node image}
	 * @param image the {@link Node node} to find the inverse image of
	 * @return Returns the found {@link Mapping#getOrigin() origin-node} or null if multiple or none were found.
	 */
	public static Node getInverseImageSingle (Set<Mapping> mappings, Node image) {
		Set<Node> inverseImageSet = getInverseImageSet(mappings, image);
		if(inverseImageSet.size() == 1) return inverseImageSet.iterator().next();
		return null;
	}
	
}
