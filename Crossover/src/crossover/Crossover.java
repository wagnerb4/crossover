package crossover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
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
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.eclipse.emf.henshin.model.impl.RuleImpl;

import view.View;
import view.ViewFactory;
import view.ViewSetOperationException;

public class Crossover implements Iterable<Pair<Resource, Resource>> {
	
	/**
	 * The default strategy used in splitting up the search space elements. 
	 * It uses a depth first search in order to find all of the elements from 
	 * the solution part which are in connected components with the first part of the problem split.
	 */
	public static final SearchSpaceElementSplitStrategy DEFAULT_STRATEGY = new SearchSpaceElementSplitStrategy () {
		@Override
		public void apply (View searchSpaceElementOne) throws ViewSetOperationException {
			
			searchSpaceElementOne.removeDangling();
			
			View tempProblemSplitOne = super.problemSplitPart.copy();
			View tempSearchSpaceElementOne = new View(searchSpaceElementOne.getResource());
			
			while (!tempProblemSplitOne.isEmpty()) {
				EObject randomNode = tempProblemSplitOne.getObject(tempProblemSplitOne.getRandomNode());
				View connectedComponentOfSearchSpaceElementOne = ViewFactory.doDFS(searchSpaceElementOne, searchSpaceElementOne.getNode(randomNode));
				tempProblemSplitOne.subtract(connectedComponentOfSearchSpaceElementOne);
				tempSearchSpaceElementOne.union(connectedComponentOfSearchSpaceElementOne);
			}
			
			searchSpaceElementOne.clear();
			searchSpaceElementOne.union(tempSearchSpaceElementOne);
			
		}
	};
	
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
	 * A set of {@link Mapping mappings}  from {@link View problemPartSSEOne} to {@link problemPartSSETwo}.
	 * This mapping represents a graph isomorphism between the problem parts of each search space element.
	 */
	private Set<Mapping> problemPartMappings;
	
	/**
	 * A {@link Pair pair} of {@link View views} over the {@link View#resource resource} that represents
	 * the first search space element, each containing parts according to
	 * {@link Crossover#splitSearchSpaceElement(View, Pair, SearchSpaceElementSplitStrategy, View)}.
	 */
	private Pair<View, View> splitOfSSEOne;
	
	/**
	 * A {@link Pair pair} of {@link View views} over the {@link View#resource resource} that represents
	 * the second search space element, each containing parts according to
	 * {@link Crossover#splitSearchSpaceElement(View, Pair, SearchSpaceElementSplitStrategy, View)}.
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
	 * Default constructor for testing purposes.
	 */
	@SuppressWarnings("unused")
	private Crossover () {}
	
	/**
	 * Creates a new Crossover between the given {@link ResourceSet searchSpaceElements}.
	 * @param metamodel the meta-model for the {@link ResourceSet searchSpaceElements}.
	 * @param searchSpaceElements the search space elements to do the crossover on
	 * @param problemPartSplitStrategy the strategy used to split up the problem part
	 * @param problemPartEClasses a {@link List list} of all non-abstract {@link EClass eClasses} describing the problem part
	 * @param problemPartEReferences a {@link List list} of {@link EReference eReferences} describing the problem part
	 * @param searchSpaceElementSplitStrategy the strategy usedto split up the search space elements
	 * @throws CrossoverUsageException if one of the parameters is null or the metamodel or searchSpaceElements are empty.
	 * @throws ViewSetOperationException on a set operation of a view
	 */
	public Crossover (Resource metamodel, Pair<Resource, Resource> searchSpaceElements, Strategy problemPartSplitStrategy,
			List<EClass> problemPartEClasses, List<EReference> problemPartEReferences,
			SearchSpaceElementSplitStrategy searchSpaceElementSplitStrategy) throws CrossoverUsageException, ViewSetOperationException {
		init(metamodel, searchSpaceElements, problemPartSplitStrategy, problemPartEClasses, problemPartEReferences, searchSpaceElementSplitStrategy, new View(metamodel));
	}
	
	/**
	 * Creates a new Crossover between the given {@link ResourceSet searchSpaceElements}.
	 * @param metamodel the meta-model for the {@link ResourceSet searchSpaceElements}.
	 * @param searchSpaceElements the search space elements to do the crossover on
	 * @param problemPartSplitStrategy the strategy used to split up the problem part
	 * @param problemPartEClasses a {@link List list} of all non-abstract {@link EClass eClasses} describing the problem part
	 * @param problemPartEReferences a {@link List list} of {@link EReference eReferences} describing the problem part
	 * @param searchSpaceElementSplitStrategy the strategy usedto split up the search space elements
	 * @param subMetaModelOfIntersection a {@link View view} on the {@literal metamodel} used to create the splits </br>
	 * All instances of the elements contained by this view are part of the intersections of the problem split and search space
	 * element split respectively. 
	 * @throws CrossoverUsageException if one of the parameters is null or the metamodel or searchSpaceElements are empty.
	 * @throws ViewSetOperationException on a set operation of a view
	 */
	public Crossover (Resource metamodel, Pair<Resource, Resource> searchSpaceElements, Strategy problemPartSplitStrategy,
			List<EClass> problemPartEClasses, List<EReference> problemPartEReferences,
			SearchSpaceElementSplitStrategy searchSpaceElementSplitStrategy,
			View subMetaModelOfIntersection) throws CrossoverUsageException, ViewSetOperationException {
		init(metamodel, searchSpaceElements, problemPartSplitStrategy, problemPartEClasses, problemPartEReferences, searchSpaceElementSplitStrategy, subMetaModelOfIntersection);
	}
	
	private void init (Resource metamodel, Pair<Resource, Resource> searchSpaceElements, Strategy problemPartSplitStrategy,
			List<EClass> problemPartEClasses, List<EReference> problemPartEReferences,
			SearchSpaceElementSplitStrategy searchSpaceElementSplitStrategy,
			View subMetaModelOfIntersection) throws CrossoverUsageException, ViewSetOperationException {
		
		// checking parameters
		
		if(metamodel == null || metamodel.getContents().isEmpty())
			throw new CrossoverUsageException("The metamodel must not be null or empty.");
		if(searchSpaceElements == null || searchSpaceElements.getFirst() == null || searchSpaceElements.getSecond() == null)
			throw new CrossoverUsageException("The searchSpaceElements must not be null.");
		if(problemPartSplitStrategy == null)
			throw new CrossoverUsageException("The strategy must not be null.");
		if(problemPartEClasses == null)
			throw new CrossoverUsageException("The problemPartEClasses must not be null.");
		if(problemPartEReferences == null)
			throw new CrossoverUsageException("The problemPartEReferences must not be null.");
		
		// find the problem part of each search space element
		
		this.problemPartSSEOne = new View(searchSpaceElements.getFirst());
		this.problemPartSSETwo = new View(searchSpaceElements.getSecond());
		
		/**
		 * A set of {@link Mapping mappings}  from {@link View problemPartSSEOne} to {@link problemPartSSETwo}.
		 * This mapping represents a graph isomorphism between the problem parts of each search space element.
		 */
		problemPartMappings = ViewFactory.buildViewMapping(problemPartSSEOne, problemPartSSETwo, problemPartEClasses, problemPartEReferences);
		
		// split the problem part
		
		View problemBorder = findBorder(metamodel, problemPartEClasses, problemPartEReferences);
		
		this.problemSplitSSEOne = splitProblemPart(problemPartSSEOne, problemBorder, problemPartSplitStrategy, subMetaModelOfIntersection);
		this.problemSplitSSETwo = new Pair<View, View> (
				ViewFactory.intersectByMapping(this.problemSplitSSEOne.getFirst(), this.problemPartSSETwo, 
						MappingUtil.mapByOrigin(problemPartMappings, origin -> this.problemSplitSSEOne.getFirst().
																				getNode(problemPartSSEOne.getObject(origin)))
						),
				ViewFactory.intersectByMapping(this.problemSplitSSEOne.getSecond(), this.problemPartSSETwo, 
						MappingUtil.mapByOrigin(problemPartMappings, origin -> this.problemSplitSSEOne.getSecond().
																				getNode(problemPartSSEOne.getObject(origin)))
					)
		);
		
		// split the search space elements according to the problem split
		
		this.splitOfSSEOne = splitSearchSpaceElement(problemPartSSEOne, problemSplitSSEOne, searchSpaceElementSplitStrategy, subMetaModelOfIntersection);
		this.splitOfSSETwo = splitSearchSpaceElement(problemPartSSETwo, problemSplitSSETwo, searchSpaceElementSplitStrategy, subMetaModelOfIntersection);
		
		// compute the intersections
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
			
			Set<Mapping> tmpMappings = new HashSet<Mapping>();
			problemPartMappings.forEach(mapping -> {
				Mapping inverseMapping = new MappingImpl();
				inverseMapping.setOrigin(mapping.getImage());
				inverseMapping.setImage(mapping.getOrigin());
				tmpMappings.add(inverseMapping);
			});
			this.problemPartMappings = tmpMappings;
			
		}
		
		this.problemPartIntersection = problemSplitSSEOne.getFirst().copy();
		this.problemPartIntersection.intersect(problemSplitSSEOne.getSecond());
		
	}
	
	/**
	 * Splits the given {@link View problemPartView} according to 
	 * the {@link Crossover#strategy} and the {@link Crossover#metamodel}.
	 * @param problemPartView a {@link View} of the problem part in a search space element
	 * @param problemBorder a {@link View} of the meta-model of the {@link View#resouce resouce} 
	 * in the {@link View problemPartView} containing the border of the problem part that is
	 * adjacent to the solution part
	 * @param strategy a {@link Strategy} that is applied to the problem border of the {@link View problemPartView}
	 * in order to customize the split
	 * @param subMetaModelOfIntersection a {@link View view} on the meta-model, instances (i.e nodes and non-dangling edges) 
	 * of the contained meta-model elements are part of both returned problem part split elements dangling instance-edges are only
	 * added to the second problem part split
	 * @return Returns a {@link Pair pair} of {@link View views} over the same
	 * {@link View#resource} as the given {@link View problemPartView}, each representing
	 * one part of the split. The first split element view will contain the, by the given {@link Strategy strategy}
	 * modified {@link View problemBorder} and will never contain dangling edges. The second view consists of
	 * all elements such that two conditions are satisfied. Firstly, their union yields a view equal to the 
	 * given {@link View problemPartView} and secondly the second split element contains a
	 * minimal number of dangling edges. The second split element contains dangling edges if and only if the {@link View problemPartView}
	 * contains border edges (i.e edges that are adjacent to solution part nodes).
	 * @throws ViewSetOperationException  if a set-operation on a view was not successfull
	 * @throws IllegalStateException if general operation on a view was not successfull
	 */
	private Pair<View, View> splitProblemPart (View problemPartView, View problemBorder, Strategy strategy, View subMetaModelOfIntersection) throws ViewSetOperationException, IllegalStateException {
		
		View matchedBorder = problemPartView.copy();
		boolean matchedSuccessfully = matchedBorder.matchViewByMetamodel(problemBorder);
		if(!matchedSuccessfully) throw new IllegalStateException("The border counldn't be matched.");
		
		View matchedIntersection = problemPartView.copy();
		
		if (subMetaModelOfIntersection.isEmpty()) {
			matchedIntersection.clear();
		} else {
			matchedSuccessfully = matchedIntersection.matchViewByMetamodel(subMetaModelOfIntersection);
			if(!matchedSuccessfully) throw new IllegalStateException("The subMetaModelOfIntersection counldn't be matched.");
			matchedIntersection.intersect(problemPartView);
		}
		
		View remainderView = problemPartView.copy();
		remainderView.subtract(matchedBorder);
		remainderView.removeDangling();
		
		View problemPartOne = remainderView.copy();
		problemPartOne.union(matchedIntersection);
		problemPartOne.removeDangling();
		
		View problemPartTwo = remainderView.copy();
		problemPartTwo.union(matchedIntersection);
		problemPartTwo.removeDangling();
		
		View partOfMatchedBorder = matchedBorder.copy();
		strategy.apply(partOfMatchedBorder);
		partOfMatchedBorder.removeDangling();
		
		problemPartOne.union(partOfMatchedBorder);
		
		// can't use the extendByMissingEdges method because we don't want edges from the solution part
		
		for (Edge edge : problemPartView.getGraph().getEdges()) {
			EObject sourceEObject = problemPartView.getObject(edge.getSource());
			EObject targetEObject = problemPartView.getObject(edge.getTarget());
			if(problemPartOne.contains(sourceEObject) && problemPartOne.contains(targetEObject)) {
				if (!problemPartOne.contains(sourceEObject, targetEObject, edge.getType(), false)) {
					boolean addedSuccessfully = problemPartOne.extend(sourceEObject, targetEObject, edge.getType());
					if(!addedSuccessfully) addedSuccessfully = problemPartOne.extend(targetEObject, sourceEObject, edge.getType());
					if(!addedSuccessfully) throw new IllegalStateException("Couldn't add an edge to problemPartOne.");
				}
			}
		}
		
		View notProblemPartOne = problemPartView.copy();
		notProblemPartOne.subtract(problemPartOne);
		problemPartTwo.union(notProblemPartOne);
		// problemPartTwo.union(problemPartOne) results in a view equal to problemPartView
		
		problemPartTwo.completeDangling();
		// problemPartTwo doesn't contain dangling edges anymore but it may contain solution part nodes
		
		List<EObject> toRemove = new ArrayList<>();
		for (Node node : problemPartTwo.getGraph().getNodes()) {
			EObject eObject = problemPartTwo.getObject(node);
			if (!problemPartView.contains(eObject)) {
				toRemove.add(eObject);
			}
		}
		
		toRemove.forEach(problemPartTwo::reduce);
		
		// now we need to make sure that all problem part nodes of the split except for the root are properly contained
		problemPartOne.extendByContainers();
		problemPartTwo.extendByContainers();
		
		// check whether there are solution part nodes in the views
		View copyOfView = problemPartOne.copy();
		copyOfView.subtract(problemPartView);
		if (!copyOfView.isEmpty()) throw new IllegalStateException("All container types of problem part objects and references between them need to belong to the problem part too.");
		copyOfView = problemPartTwo.copy();
		copyOfView.subtract(problemPartView);
		if (!copyOfView.isEmpty()) throw new IllegalStateException("All container types of problem part objects and references between them need to belong to the problem part too.");
		
		return new Pair<View, View>(problemPartOne, problemPartTwo);
		
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
		View borderView = new View(metamodel);
		
		TreeIterator<EObject> treeIterator = metamodel.getAllContents();
		
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			
			if(eObject instanceof EClass) {	
				
				EClass eClass = (EClass) eObject;
			
				List<EClass> eClassestoExtend = new ArrayList<>();
				List<EReference> eReferences = eClass.getEAllReferences();
				
				for (EReference eReference : eReferences) {
					if(!problemPartEReferences.contains(eReference)) {
						
						/*
						if(problemPartEClasses.contains(eReference.getEContainingClass())) {
							eClassestoExtend.add(eReference.getEContainingClass());
						}*/
						
						if(problemPartEClasses.contains(eClass)) {
							eClassestoExtend.add(eClass);
						}
						
						if(problemPartEClasses.contains(eReference.getEReferenceType())) {
							eClassestoExtend.add(eReference.getEReferenceType());
						}
						
					} else {
						
						if(!problemPartEClasses.contains(eReference.getEContainingClass())) {
							borderView.extend(((EObject) eReference));
							if(problemPartEClasses.contains(eReference.getEReferenceType())) {
								eClassestoExtend.add(eReference.getEReferenceType());
							}
						}
						
						if(!problemPartEClasses.contains(eReference.getEReferenceType())) {
							borderView.extend(((EObject) eReference));
							
							/*if(problemPartEClasses.contains(eReference.getEContainingClass())) {
								eClassestoExtend.add(eReference.getEContainingClass());
							}*/
							
							if(problemPartEClasses.contains(eClass)) {
								eClassestoExtend.add(eClass);
							}
						}
						
					}
				}
				
				for (EClass eClassToExtend : eClassestoExtend) {
					if(!eClassToExtend.isAbstract()) {
						borderView.extend(((EObject) eClassToExtend));
					}
				}
				
			}
			
		}
		
		Set<EClass> possibleSuperTypes = new HashSet<EClass>();
		borderView.getContainedEObjects().forEach(eObject -> {
			if (eObject instanceof EClass) {
				possibleSuperTypes.add((EClass) eObject);
			}
		});
		
		treeIterator = metamodel.getAllContents();
		
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			if(eObject instanceof EClass) {
				EClass eClass = (EClass) eObject;
				for (EClass superType : eClass.getEAllSuperTypes()) {
					if (possibleSuperTypes.contains(superType)) {
						borderView.extend(eObject);
					}
				}
			}
		}
		
		return borderView;
	}
	
	/**
	 * Splits the search space element (given indirectly as the
	 * {@link View#resource resource} of the {@link View views}) according
	 * to the given {@code problemSplit}. Both {@link View views} are over the
	 * same resource.
	 * @param problemPart the {@link View view} respresenting the complete problem part
	 * @param problemSplit the split of the {@link View problemPart}
	 * @param strategy the {@link SearchSpaceElementSplitStrategy} used for extending the first split
	 * element by solution part elements. This results in the final first split view. The second split element
	 * is computed based on the first one such that the intersection of the two parts is minimal but their union
	 * yields the complete search space element.
	 * @param subMetaModelOfIntersection a {@link View view} on the meta-model, instances (i.e nodes and non-dangling edges) of
	 * the contained meta-model elements are part of both retuned split elements
	 * @return Returns a {@link Pair pair} of two new {@link View views} on the same 
	 * {@link View#resource resource} as the given {@link View views} representing the split of
	 * the search space element. The first element of the split contains the first element of the given
	 * {@link Pair problemSplit} and the second element of the split contains the second.
	 * @throws ViewSetOperationException if a set-operation on a view was not successful
	 */
	private Pair<View, View> splitSearchSpaceElement (View problemPart, Pair<View, View> problemSplit, SearchSpaceElementSplitStrategy strategy, View subMetaModelOfIntersection) throws ViewSetOperationException {
		
		View searchSpaceElement = problemPart.copy();
		searchSpaceElement.extendByAllNodes();
		searchSpaceElement.extendByMissingEdges();
		
		View searchSpaceElementOne = searchSpaceElement.copy();
		searchSpaceElementOne.subtract(problemPart);
		searchSpaceElementOne.union(problemSplit.getFirst());
		
		strategy.setProblemSplitPart(problemSplit.getFirst());
		strategy.apply(searchSpaceElementOne);
		
		View searchSpaceElementTwo = searchSpaceElement.copy();
		searchSpaceElementTwo.subtract(problemPart);
		searchSpaceElementTwo.subtract(searchSpaceElementOne);
		searchSpaceElementTwo.union(problemSplit.getSecond());
		
		/**
		 * This is necessary because we need all edges in the view for the union of searchSpaceElementOne
		 * and searchSpaceElementTwo to contain all elements of the searchSpaceElement.
		 */
		searchSpaceElementTwo.completeDangling();
		
		// Combine the views with the matched subMetaModelOfIntersection.
		
		View matchedIntersection = problemPart.copy();
		
		if (subMetaModelOfIntersection.isEmpty()) {
			matchedIntersection.clear();
		} else {
			boolean successfullyMatched = matchedIntersection.matchViewByMetamodel(subMetaModelOfIntersection);
			if (!successfullyMatched) throw new IllegalStateException("Couldn't match the subMetaModelOfIntersection.");
		}
		
		View copyOfSearchSpaceElementOne = searchSpaceElementOne.copy();
		copyOfSearchSpaceElementOne.union(matchedIntersection);
		copyOfSearchSpaceElementOne.removeDangling();
		searchSpaceElementOne.union(copyOfSearchSpaceElementOne);
		
		View copyOfSearchSpaceElementTwo = searchSpaceElementTwo.copy();
		copyOfSearchSpaceElementTwo.union(matchedIntersection);
		copyOfSearchSpaceElementTwo.removeDangling();
		searchSpaceElementTwo.union(copyOfSearchSpaceElementTwo);
		
		// now we need to make sure that all problem part nodes of the split except for the root are properly contained
		searchSpaceElementOne.extendByContainers();
		searchSpaceElementTwo.extendByContainers();
		
		return new Pair<View, View>(searchSpaceElementOne, searchSpaceElementTwo);
		
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
			Iterator<CustomSpan> computedSpans = null;
			Iterator<View> subgraphIterator = null;
			
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
				
				// initialize
				if (subgraphIterator == null) {
					subgraphIterator = ViewFactory.getSubGraphIterator(intersectionOfSSEOne, problemPartIntersection);
				}
				
				if(computedSpans != null && computedSpans.hasNext()) {
					nextSpan = computedSpans.next();
					return;
				}
				
				Set<CustomSpan> spans = Collections.emptySet();
				
				while (spans.isEmpty() && subgraphIterator.hasNext()) {
					
					View subgraphOfIntersectionOfSSEOne = subgraphIterator.next();
					
					// get mapping from subgraphOfIntersectionOfSSEOne to intersectionOfSSEOne
					Set<Mapping> mappingsOne = new HashSet<Mapping>();
					subgraphOfIntersectionOfSSEOne.getGraph().getNodes().forEach(node -> {
						Node mappedNode = intersectionOfSSEOne.getNode(subgraphOfIntersectionOfSSEOne.getObject(node));
						Mapping mapping = new MappingImpl();
						mapping.setOrigin(node);
						mapping.setImage(mappedNode);
						mappingsOne.add(mapping);
					});
					
					Map<EObject, EObject> mapFromProblemPartIntersectionSSEOneToIntersectionOfSSETwo = new HashMap<>();
					problemPartIntersection.getGraph().getNodes().forEach(node -> {
						EObject originEObject = problemPartIntersection.getObject(node);
						EObject targetEObject = problemPartSSETwo.getObject(MappingUtil.getImageSingle(problemPartMappings, problemPartSSEOne.getNode(originEObject)));
						mapFromProblemPartIntersectionSSEOneToIntersectionOfSSETwo.put(originEObject, targetEObject);
					});
					
					Iterator<Set<Mapping>> mappingSetIterator = MappingUtil.getMappingSetIterator (
							subgraphOfIntersectionOfSSEOne, // from View
							intersectionOfSSETwo, // to View
							mapFromProblemPartIntersectionSSEOneToIntersectionOfSSETwo // mapping from the itentity part in "fromView" to "toView"
					);
					
					spans = new HashSet<CustomSpan>();
					
					while (mappingSetIterator.hasNext()) {
						Set<Mapping> mappingsTwo = (Set<Mapping>) mappingSetIterator.next();
						spans.add(new CustomSpan(subgraphOfIntersectionOfSSEOne, mappingsOne, mappingsTwo));
					}
					
				}
				
				if(!spans.isEmpty()) {
					computedSpans = spans.iterator();
					nextSpan = computedSpans.next();
					return;
				}
				
				// subgraphIterator.hasNext() == false --> there are no more spans
				nextSpan = null;
				
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
					
					Set<Mapping> mappingsFromIntersectionToSSEOneFirstSplitElement = MappingUtil.
							mapByImage(span.getMappingsOne(), image -> splitOfSSEOne.getFirst().
									getNode(intersectionOfSSEOne.getObject(image))
							);
					intersectionToSSEOneFirstSplitElement.getMappings().addAll(mappingsFromIntersectionToSSEOneFirstSplitElement);
					
					Set<Mapping> mappingsFromIntersectionToSSEOneSecondSplitElement = MappingUtil.
							mapByImage(span.getMappingsOne(), image -> splitOfSSEOne.getSecond().
									getNode(intersectionOfSSEOne.getObject(image))
							);
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
					
					// E1F2
					Change chageOne = engine.createChange(intersectionToSSEOneFirstSplitElement, eGraphOfSSETwoSplitSecond.getFirst(), matchForSSETwoSplitSecond, null);
					
					// F2E1
					Change chageTwo = engine.createChange(intersectionToSSEOneSecondSplitElement, eGraphOfSSETwoSplitFirst.getFirst(), matchForSSETwoSplitFirst, null);
					
					chageOne.applyAndReverse();
					chageTwo.applyAndReverse();
					
					// create the resource pair
					
					Resource resourceOne = new ResourceImpl();
					Resource resourceTwo = new ResourceImpl();	
					
					resourceOne.getContents().addAll(chageOne.getEGraph().getRoots());
					resourceTwo.getContents().addAll(chageTwo.getEGraph().getRoots());
					
					nextCrossoverPair = new Pair<Resource, Resource>(resourceOne, resourceTwo);
					
				} else {
					
					nextCrossoverPair = null;
					
				}
				
			}
			
		};
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((intersectionOfSSEOne == null) ? 0 : intersectionOfSSEOne.hashCode());
		result = prime * result + ((intersectionOfSSETwo == null) ? 0 : intersectionOfSSETwo.hashCode());
		result = prime * result + ((problemPartIntersection == null) ? 0 : problemPartIntersection.hashCode());
		result = prime * result + ((problemPartSSEOne == null) ? 0 : problemPartSSEOne.hashCode());
		result = prime * result + ((problemPartSSETwo == null) ? 0 : problemPartSSETwo.hashCode());
		result = prime * result + ((problemSplitSSEOne == null) ? 0 : problemSplitSSEOne.hashCode());
		result = prime * result + ((problemSplitSSETwo == null) ? 0 : problemSplitSSETwo.hashCode());
		result = prime * result + ((splitOfSSEOne == null) ? 0 : splitOfSSEOne.hashCode());
		result = prime * result + ((splitOfSSETwo == null) ? 0 : splitOfSSETwo.hashCode());
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Crossover))
			return false;
		Crossover other = (Crossover) obj;
		if (intersectionOfSSEOne == null) {
			if (other.intersectionOfSSEOne != null)
				return false;
		} else if (!intersectionOfSSEOne.equals(other.intersectionOfSSEOne))
			return false;
		if (intersectionOfSSETwo == null) {
			if (other.intersectionOfSSETwo != null)
				return false;
		} else if (!intersectionOfSSETwo.equals(other.intersectionOfSSETwo))
			return false;
		if (problemPartIntersection == null) {
			if (other.problemPartIntersection != null)
				return false;
		} else if (!problemPartIntersection.equals(other.problemPartIntersection))
			return false;
		if (problemPartSSEOne == null) {
			if (other.problemPartSSEOne != null)
				return false;
		} else if (!problemPartSSEOne.equals(other.problemPartSSEOne))
			return false;
		if (problemPartSSETwo == null) {
			if (other.problemPartSSETwo != null)
				return false;
		} else if (!problemPartSSETwo.equals(other.problemPartSSETwo))
			return false;
		if (problemSplitSSEOne == null) {
			if (other.problemSplitSSEOne != null)
				return false;
		} else if (!problemSplitSSEOne.equals(other.problemSplitSSEOne))
			return false;
		if (problemSplitSSETwo == null) {
			if (other.problemSplitSSETwo != null)
				return false;
		} else if (!problemSplitSSETwo.equals(other.problemSplitSSETwo))
			return false;
		if (splitOfSSEOne == null) {
			if (other.splitOfSSEOne != null)
				return false;
		} else if (!splitOfSSEOne.equals(other.splitOfSSEOne))
			return false;
		if (splitOfSSETwo == null) {
			if (other.splitOfSSETwo != null)
				return false;
		} else if (!splitOfSSETwo.equals(other.splitOfSSETwo))
			return false;
		return true;
	}
	
}
