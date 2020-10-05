package crossover;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.henshin.interpreter.Change;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.Match;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.MatchImpl;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.impl.RuleImpl;

import view.View;
import view.ViewFactory;
import view.ViewSetOperationException;

public class Crossover implements Iterable<Pair<Resource, Resource>> {
	
	/**
	 * The {@link View view} over the {@link View#resource resource} that represents
	 * the first search space element and contains its the problem part.
	 */
	private View problemPartSSEOne;
	
	/**
	 * The {@link View view} over the {@link View#resource resource} that represents
	 * the second search space element and contains its the problem part.
	 */
	private View problemPartSSETwo;
	
	/**
	 * A {@link Pair pair} of {@link View views} over the {@link View#resource resource} that represents
	 * the first search space element, each containing parts according to
	 * {@link Crossover#splitProblemPart(View, View)}.
	 */
	private Pair<View, View> problemSplitSSEOne;
	
	/**
	 * A {@link Pair pair} of {@link View views} over the {@link View#resource resource} that represents
	 * the second search space element, each containing parts according to
	 * {@link Crossover#splitProblemPart(View, View)}.
	 */
	private Pair<View, View> problemSplitSSETwo;
	
	/**
	 * A {@link Pair pair} of {@link View views} over the {@link View#resource resource} that represents
	 * the first search space element, each containing parts according to
	 * {@link Crossover#splitSearchSpaceElement(View, Pair)}.
	 */
	private Pair<View, View> splitOfSSEOne;
	
	/**
	 * A {@link Pair pair} of {@link View views} over the {@link View#resource resource} that represents
	 * the second search space element, each containing parts according to
	 * {@link Crossover#splitSearchSpaceElement(View, Pair)}.
	 */
	private Pair<View, View> splitOfSSETwo;
	
	/**
	 * The intersection of both {@link Crossover#problemSplitSSEOne} {@link View views}.
	 */
	private View problemPartIntersection;
	
	/**
	 * The intersection of both {@link Crossover#splitOfSSEOne} {@link View views}.
	 */
	private View intersectionOfSSEOne;
	
	/**
	 * The intersection of both {@link Crossover#splitOfSSETwo} {@link View views}.
	 */
	private View intersectionOfSSETwo;

	/**
	 * Creates a new Crossover between the given {@link ResourceSet searchSpaceElements}.
	 * @param metamodel the meta-model for the {@link ResourceSet searchSpaceElements}.
	 * @param searchSpaceElements the search space elements to do the crossover on
	 * @param strategy the strategy to split the problem part with
	 * @param problemPartEClasses a {@link List list} of {@link EClass eClasses} describing the problem part
	 * @param problemPartEReferences a {@link List list} of {@link EReference eReferences} describing the problem part
	 * @throws CrossoverUsageException if one of the parameters is null or the metamodel or searchSpaceElements are empty.
	 * @throws ViewSetOperationException on a set operation of a view
	 */
	public Crossover (Resource metamodel, ResourceSet searchSpaceElements, Strategy strategy,
			List<EClass> problemPartEClasses, List<EReference> problemPartEReferences
			) throws CrossoverUsageException, ViewSetOperationException {
		
		// checking parameters
		
		if(metamodel == null || metamodel.getContents().isEmpty())
			throw new CrossoverUsageException("The metamodel must not be null or empty.");
		if(searchSpaceElements == null || !searchSpaceElements.getAllContents().hasNext())
			throw new CrossoverUsageException("The searchSpaceElements must not be null or emtpy.");
		if(searchSpaceElements.getResources().size() != 2) 
			throw new CrossoverUsageException("The searchSpaceElements ResourceSet has to contain exactly two resources."
					+ "One for each search space element.");
		if(strategy == null)
			throw new CrossoverUsageException("The strategy must not be null.");
		if(problemPartEClasses == null)
			throw new CrossoverUsageException("The problemPartEClasses must not be null.");
		if(problemPartEReferences == null)
			throw new CrossoverUsageException("The problemPartEReferences must not be null.");
		
		// find the problem part of each search space element
		
		this.problemPartSSEOne = new View(searchSpaceElements.getResources().get(0));
		this.problemPartSSETwo = new View(searchSpaceElements.getResources().get(1));
		
		/**
		 * A set of {@link Mapping mappings}  from {@link View problemPartSSEOne} to {@link problemPartSSETwo}.
		 * This mapping represents a graph isomorphism between the problem parts of each search space element.
		 */
		Set<Mapping> problemPartMappings = ViewFactory.buildViewMapping(problemPartSSEOne, problemPartSSETwo, problemPartEClasses, problemPartEReferences);
		
		// split the problem part
		
		View problemBorder = findBorder(metamodel, problemPartEClasses, problemPartEReferences);
		
		this.problemSplitSSEOne = splitProblemPart(problemPartSSEOne, problemBorder);
		this.problemSplitSSETwo = new Pair<View, View> (
				ViewFactory.intersectByMapping(this.problemSplitSSEOne.getFirst(), this.problemPartSSETwo, problemPartMappings.stream().map(mapping -> {
					mapping.setOrigin(this.problemSplitSSEOne.getFirst().getNode(problemPartSSEOne.getObject(mapping.getOrigin())));
					return mapping;
				}).collect(Collectors.toSet())),
				ViewFactory.intersectByMapping(this.problemSplitSSEOne.getSecond(), this.problemPartSSETwo, problemPartMappings.stream().map(mapping -> {
					mapping.setOrigin(this.problemSplitSSEOne.getSecond().getNode(problemPartSSEOne.getObject(mapping.getOrigin())));
					return mapping;
				}).collect(Collectors.toSet()))
		);
		
		// split the search space elements according to the problem split
		
		this.splitOfSSEOne = splitSearchSpaceElement(problemPartSSEOne, problemSplitSSEOne);
		this.splitOfSSETwo = splitSearchSpaceElement(problemPartSSETwo, problemSplitSSETwo);
		
		// compute the intersections
		
		this.problemPartIntersection = problemSplitSSEOne.getFirst().copy();
		this.problemPartIntersection.intersect(problemSplitSSEOne.getSecond());
		
		this.intersectionOfSSEOne = splitOfSSEOne.getFirst().copy();
		this.intersectionOfSSEOne.intersect(splitOfSSEOne.getSecond());
		
		this.intersectionOfSSETwo = splitOfSSETwo.getFirst().copy();
		this.intersectionOfSSETwo.intersect(splitOfSSETwo.getSecond());
		
		if (this.intersectionOfSSETwo.getGraph().getNodes().size() < this.intersectionOfSSEOne.getGraph().getNodes().size()) {
			
			// switch references
			View tempView;
			Pair<View, View> tempPair;
			
			tempView = problemPartSSEOne;
			this.problemPartSSEOne = this.problemPartSSETwo;
			this.problemPartSSETwo = tempView;
			
			tempPair = this.problemSplitSSEOne;
			this.problemSplitSSEOne = this.problemSplitSSETwo;
			this.problemSplitSSETwo = tempPair;
			
			tempPair = this.splitOfSSEOne;
			this.splitOfSSEOne = this.splitOfSSETwo;
			this.splitOfSSETwo = tempPair;
			
			tempView = this.intersectionOfSSEOne;
			this.intersectionOfSSEOne = this.intersectionOfSSETwo;
			this.intersectionOfSSETwo = tempView;
			
		}
		
	}
	
	/**
	 * Splits the given {@link View problemPartView} according to 
	 * the {@link Crossover#strategy} and the {@link Crossover#metamodel}.
	 * @param problemPartView a {@link View} of the problem part in a search space element
	 * @param problemBorder a {@link View} of the meta-model of the {@link View#resouce resouce} 
	 * in the {@link View problemPartView} containing the border of the problem part that is
	 * adjacent to the solution part
	 * @return Returns a {@link Pair pair} of {@link View views} over the same
	 * {@link View#resource} as the given {@link View problemPartView}, each representing
	 * one part of the split.
	 */
	private Pair<View, View> splitProblemPart (View problemPartView, View problemBorder) {
		// TODO: implement
		return null;
	}
	
	/**
	 * Calculates the border of the given problem part, specified by the 
	 * {@link EClass problemPartEClasses} and {@link EReference problemPartEReferences}
	 * in the {@link Resource metamodel} adjacent to the solution part. The solution part
	 * consits of all elements that are not part of the problem part.
	 * @param metamodel the meta-model containing problem- and solution part
	 * @param problemPartEClasses a {@link List list} of {@link EClass eClasses} describing the problem part
	 * @param problemPartEReferences a {@link List list} of {@link EReference eReferences} describing the problem part
	 * @return Returns a {@link View view} over the {@link Resource metamodel} containing the border elements.
	 */
	private View findBorder (Resource metamodel, List<EClass> problemPartEClasses, List<EReference> problemPartEReferences) {
		// TODO: implement
		return null;
	}
	
	/**
	 * Splits the search space element (given indirectly as the
	 * {@link View#resource resource} of the {@link View views}) according
	 * to the given {@code problemSplit}.
	 * @param problemPart the {@link View view} respresenting the complete problem part
	 * @param problemSplit the split of the {@link View problemPart}
	 * @return Returns a {@link Pair pair} of two new {@link View views} on the same 
	 * {@link View#resource resource} as the given {@link View views} representing the split of
	 * the search space element.
	 */
	private Pair<View, View> splitSearchSpaceElement (View problemPart, Pair<View, View> problemSplit) {
		// TODO: implement
		return null;
	}

	/**
	 * @return Returns an {@link Iterator iterator} over {@link CustomSpan spans}.
	 * The spans consist of a common subgraph of {@link Crossover#intersectionOfSSEOne}
	 * and {@link Crossover#intersectionOfSSETwo} and a set of {@link Mapping mappings} from
	 * it to both intersections.
	 */
	private Iterator<CustomSpan> getSpanIterator () {
		return new Iterator<CustomSpan> () {
			
			boolean computedNextSpan = false;
			CustomSpan nextSpan = null;
			
			@Override
			public boolean hasNext() {
				
				if (!computedNextSpan) {
					computeNextSpan();
					computedNextSpan = true;
				}
				
				return (nextSpan != null);
				
			}

			@Override
			public CustomSpan next() {
				
				if (hasNext()) {
					computedNextSpan = false;
				}
				
				return nextSpan;
				
			}
			
			private void computeNextSpan () {
				// TODO: implement
			}
			
		};
	}
	
	/**
	 * @return Returns an {@link Iterator iterator} of possible crossover {@link Pair pairs}. 
	 */
	@Override
	public Iterator<Pair<Resource, Resource>> iterator () {
		return new Iterator<Pair<Resource, Resource>>() {
			
			boolean computedNextCrossoverPair = false;
			Pair<Resource, Resource> nextCrossoverPair = null;
			Iterator<CustomSpan> spanIterator = null;
			
			@Override
			public boolean hasNext() {
				
				if (!computedNextCrossoverPair) {
					computeNextCrossoverPair();
					computedNextCrossoverPair = true;
				}
				
				return (nextCrossoverPair != null);
				
			}

			@Override
			public Pair<Resource, Resource> next() {
				
				if (hasNext()) {
					computedNextCrossoverPair = false;
				}
				
				return nextCrossoverPair;
				
			}
			
			private void computeNextCrossoverPair () {
				
				if (spanIterator == null) {
					spanIterator = getSpanIterator();
				}
				
				if (spanIterator.hasNext()) {
					
					CustomSpan span = spanIterator.next();
					
					// create rules
					
					Rule intersectionToSSEOneFirstSplitElement = new RuleImpl();
					Rule intersectionToSSEOneSecondSplitElement = new RuleImpl();
					
					intersectionToSSEOneFirstSplitElement.setLhs(span.getIntersection().getGraph());
					intersectionToSSEOneSecondSplitElement.setLhs(span.getIntersection().getGraph());
					
					intersectionToSSEOneFirstSplitElement.setRhs(splitOfSSEOne.getFirst().getGraph());
					intersectionToSSEOneSecondSplitElement.setRhs(splitOfSSEOne.getSecond().getGraph());
					
					Set<Mapping> mappingsFromIntersectionToSSEOneFirstSplitElement = span.getMappingsOne().
							stream().map((Mapping mapping) -> {
								Node image = mapping.getImage();
								mapping.setImage(splitOfSSEOne.getFirst().getNode(intersectionOfSSEOne.getObject(image)));
								return mapping;
							}).collect(Collectors.toSet());
					
					intersectionToSSEOneFirstSplitElement.getMappings().addAll(mappingsFromIntersectionToSSEOneFirstSplitElement);
					
					Set<Mapping> mappingsFromIntersectionToSSEOneSecondSplitElement = span.getMappingsOne().
							stream().map((Mapping mapping) -> {
								Node image = mapping.getImage();
								mapping.setImage(splitOfSSEOne.getSecond().getNode(intersectionOfSSEOne.getObject(image)));
								return mapping;
							}).collect(Collectors.toSet());
					
					intersectionToSSEOneSecondSplitElement.getMappings().addAll(mappingsFromIntersectionToSSEOneSecondSplitElement);
					
					// apply rules
					
					Engine engine = new EngineImpl();
					
					Pair<EGraph, Map<EObject,EObject>> eGraphOfSSETwoSplitFirst = ViewFactory.createEGraphFromView(splitOfSSETwo.getFirst());
					Pair<EGraph, Map<EObject,EObject>> eGraphOfSSETwoSplitSecond = ViewFactory.createEGraphFromView(splitOfSSETwo.getSecond());
					Match matchForSSETwoSplitFirst = new MatchImpl(intersectionToSSEOneSecondSplitElement);
					Match matchForSSETwoSplitSecond = new MatchImpl(intersectionToSSEOneFirstSplitElement);
					
					span.getMappingsTwo().stream().
						map((Mapping mapping) -> {
							Node image = mapping.getImage();
							EObject originalEObject = intersectionOfSSETwo.getObject(image);
							EObject copiedEObject = eGraphOfSSETwoSplitFirst.getSecond().get(originalEObject);
							return new Pair<Node, EObject>(mapping.getOrigin(), copiedEObject);
						}).forEach((Pair<Node, EObject> pair) -> matchForSSETwoSplitFirst.setNodeTarget(pair.getFirst(), pair.getSecond()));
					
					span.getMappingsTwo().stream().
					map((Mapping mapping) -> {
						Node image = mapping.getImage();
						EObject originalEObject = intersectionOfSSETwo.getObject(image);
						EObject copiedEObject = eGraphOfSSETwoSplitSecond.getSecond().get(originalEObject);
						return new Pair<Node, EObject>(mapping.getOrigin(), copiedEObject);
					}).forEach((Pair<Node, EObject> pair) -> matchForSSETwoSplitSecond.setNodeTarget(pair.getFirst(), pair.getSecond()));
					
					Change chageOne = engine.createChange(intersectionToSSEOneFirstSplitElement, eGraphOfSSETwoSplitSecond.getFirst(), matchForSSETwoSplitSecond, null);
					Change chageTwo = engine.createChange(intersectionToSSEOneSecondSplitElement, eGraphOfSSETwoSplitFirst.getFirst(), matchForSSETwoSplitFirst, null);
					
					chageOne.applyAndReverse();
					chageTwo.applyAndReverse();
					
					// create the resource pair
					
					Resource resourceOne = new ResourceImpl();
					Resource resourceTwo = new ResourceImpl();
					
					resourceOne.getContents().addAll(chageOne.getEGraph().getRoots());
					resourceTwo.getContents().addAll(chageTwo.getEGraph().getRoots());
					
					nextCrossoverPair = new Pair<Resource, Resource>(resourceOne, resourceTwo);
					
				}
				
			}
			
		};
	}

}
