package crossover;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.impl.MappingImpl;

import view.View;

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
	
	/**
	 * Finds all possible matches from {@link View fromView} to the {@link View toView} that project
	 * the part in the {@link View identityView} to itself. A <i>match</i> is a set of {@link Mapping mappings}
	 * from the {@link View#getNodes() nodes} of the {@link View fromView} to those of the {@link View toView}.
	 * The {@link View fromView} must contain less elements than the {@link View toView}.
	 * 
	 * @param fromView the {@link View view} over {@link Resource} <b><i>A</i></b> containing the domain of the mapping
	 * @param toView  the {@link View}  over {@link Resource} <b><i>B</i></b> containing the codomain of the mapping
	 * @param identity a {@link Map map} of {@link EObjects eObjects} from {@link Resource} <b><i>A</i></b> to {@link Resource} <b><i>B</i></b>,
	 * it maps the {@link EObject eObjects} from the {@link View identityView} injective to those of the {@link View toView}
	 * @param identityView a {@link View view} over {@link Resource} <b><i>A</i></b> containing the {@link EObject eObjects}
	 * and references that are part of the identity, it may contain dangling {@link Edges}
	 * @return Returns an {@link Iterator} of all possible {@link Mapping matches} satisfying the condition described above.
	 * @throws IllegalArgumentException when the given parameters don't match the required conditions.
	 */
	public static Iterator<Set<Mapping>> getMappingSetIterator (View fromView, View toView, Map<EObject, EObject> identity, View identityView) throws IllegalArgumentException {
		
		if (fromView == null) throw new IllegalArgumentException("The fromView must not be null.");
		if (toView == null)  throw new IllegalArgumentException("The toView must not be null.");
		if (identity == null) throw new IllegalArgumentException("The identity map must not be null.");
		if (identityView == null) throw new IllegalArgumentException("The identity view must not be null.");
		if (fromView.getResource() == toView.getResource()) throw new IllegalArgumentException("The toView must be over a different resource than the fromView.");
		if (fromView.getResource() != identityView.getResource()) throw new IllegalArgumentException("The identityView must be over the same resource than the fromView.");
		if (fromView.getGraph().getNodes().size() > toView.getGraph().getNodes().size()) throw new IllegalArgumentException("The fromView must not contain more elements than the toView.");
		
		Set<EObject> eObjectsFromResourceA = new HashSet<>();
		Set<EObject> eObjectsFromResourceB = new HashSet<>();
		fromView.getResource().getAllContents().forEachRemaining(eObjectsFromResourceA::add);
		toView.getResource().getAllContents().forEachRemaining(eObjectsFromResourceB::add);
		if (!eObjectsFromResourceA.containsAll(identity.keySet()) || !eObjectsFromResourceB.containsAll(identity.values())) throw new IllegalArgumentException("The identity map must map from the identityView to the toView.");
		
		if (!identity.keySet().containsAll(identityView.getContainedEObjects())) throw new IllegalArgumentException("The identity map must map all elements of the identityView.");
		
		return new Iterator<Set<Mapping>>() {
			
			boolean computedNextSet = false;
			Set<Mapping> nextSet = null;

			@Override
			public boolean hasNext() {

				if(!computedNextSet) {
					computeNextSet();
					computedNextSet = true;
				}
				
				return (nextSet != null);
				
			}

			@Override
			public Set<Mapping> next() {
				
				if (hasNext()) {
					computedNextSet = false;
				}
				
				return nextSet;
				
			}
			
			private void computeNextSet() {
				// TODO: implement
			}
			
		};
		
	}

}
