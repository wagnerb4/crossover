package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.Match;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.eclipse.emf.henshin.model.impl.RuleImpl;

import crossover.MappingUtil;
import crossover.Pair;

/**
 * 
 * A utility class for working with {@link View}s
 * 
 * @author Benjamin Wagner
 */
public class ViewFactory {
	
	/**
	 * Builds the graphs of the given Views based on the given {@code classes} and {@code references}.
	 * The {@link View views} should have different {@link View#resource resources} on the same meta-model.
	 * The given {@link EClass classes} and {@link EReference references} define the problem part of the {@link View views}.
	 * Thus the parts of the {@link View#resource resources} in the given {@link View views} that are defined by the given
	 * given {@link EClass classes} and {@link EReference references} should be the same.
	 * @param viewOne first empty {@link View}
	 * @param viewTwo second empty {@link View}
	 * @param classes of the meta-model to buld the views from
	 * @param references of the meta-model to buld the views from
	 * @return Returns a set of mappings between the two graphs, representing an graph isomorphism. If no such isomorphism exists null is returned.
	 */
	public static Set<Mapping> buildViewMapping (View viewOne, View viewTwo, List<EClass> classes, List<EReference> references) {
		
		// build the views
		
		for (EClass eClass: classes) {
			viewOne.extend(eClass);
			viewTwo.extend(eClass);
		}
		
		for (EReference eReference: references) {
			viewOne.extend(eReference);
			viewTwo.extend(eReference);
		}
		
		// find an isomorphism
		
		Rule rule = new RuleImpl();
		rule.setLhs(viewOne.graph);
		rule.setCheckDangling(false);
		rule.setInjectiveMatching(true);
		EGraph eGraph = new EGraphImpl(viewTwo.resource.getContents().get(0));
		Engine engine = new EngineImpl();
		
		Iterator<Match> matchIterator = engine.findMatches(rule, eGraph, null).iterator();
		
		if (!matchIterator.hasNext()) return null; 
		
		Match match = matchIterator.next();
		
		// convert the match (Node -> EObject) to a set of mappings (Node -> Node) by using the graphMap of viewTwo
		
		return viewOne.graph.getNodes().stream().map(node -> {
		    Mapping mapping = new MappingImpl();
		    mapping.setOrigin(node);
		    mapping.setImage(viewTwo.getNode(match.getNodeTarget(node)));
		    return mapping;
		}).collect(Collectors.toSet());
		
	}
	
	/**
	 * Projects the graph from {@code sourceView} to the graph of {@code targetView} using the given {@code viewMappings}.
	 * @param sourceView the {@link View} to be projected onto the {@code targetView}
	 * @param targetView the {@link View} containing the image of the by {@code viewMappings} projected {@code sourceView}
	 * @param viewMappings the mappings from the graph of {@code sourceView} to the graph of {@code targetView}
	 * @return Returns a new {@link View} containing the projected subgraph.
	 */
	public static View intersectByMapping (View sourceView, View targetView, Set<Mapping> viewMappings) {
		
		// map the nodes from sourceView to nodes from targedView
		Set<Node> mappedNodes = sourceView.graph.getNodes().stream()
				.map(origin -> MappingUtil.getImageSingle(viewMappings, origin)).
				filter(node -> node != null).
				collect(Collectors.toSet());
		
		// create the new view and reduce it to the mapped nodes
		View mappedView = targetView.copy();
		targetView.graph.getNodes().stream().
			filter(node -> !mappedNodes.contains(node)). // only keep nodes that should be removed
			map(targetView::getObject). // get the corresponding  EObjects
			forEach(mappedView::reduce); // reduce the new view by them
		
		Set<Mapping> mappingsToMappedView = MappingUtil.mapByImage(viewMappings, node -> mappedView.getNode(targetView.getObject(node)));
		
		// collect all mapped edges
		Set<Edge> mappedEdges = sourceView.graph.getEdges().stream().map(edge -> {
			
			Node mappedSource = MappingUtil.getImageSingle(mappingsToMappedView, edge.getSource());
			Node mappedTarget = MappingUtil.getImageSingle(mappingsToMappedView, edge.getTarget());
		
			if (mappedSource == null || mappedTarget == null) return null;
			
		    for (Edge outgoingEdge : mappedSource.getOutgoing()) {
				if (outgoingEdge.getTarget().equals(mappedTarget)) {
					return outgoingEdge;
				}
			}
		    
		    return null;
		    
		}).filter(node -> node != null).collect(Collectors.toSet());
		
		// remove all not-mapped edges from the graph in the mappedView 
		List<Edge> edgesToRemove = mappedView.graph.getEdges().stream().filter(edge -> !mappedEdges.contains(edge)).collect(Collectors.toList());
		edgesToRemove.forEach(mappedView.graph::removeEdge);
		
		return mappedView;
		
	}
	
	/**
	 * Checks whether the graph in {@code viewOne} is a subgraph of the graph in {@code viewTwo}.
	 * More exact it checks if all of the {@link Node} and {@link Edge} Objects are contained in the graph of {@code viewTwo}.
	 * @param viewOne the {@code View} whose graph is checked for being a subgraph
	 * @param viewTwo the {@code View} whose graph is checked for containung a subgraph
	 * @return Returns {@code true} if {@code viewOne} is a subgraph of {@code viewTwo} and {@code false} otherwise.
	 */
	public static boolean isSubgraph (View viewOne, View viewTwo) {
		
		if (viewOne.graph.getNodes().size() <= viewTwo.graph.getNodes().size() && 
				viewOne.graph.getEdges().size() <= viewTwo.graph.getEdges().size()) {
			for (Node node : viewOne.graph.getNodes())
				if (!viewTwo.contains(viewOne.getObject(node))) return false;
			for (Edge edge : viewOne.graph.getEdges())
				if (!viewTwo.contains(viewOne.objectMap.get(edge.getSource()), viewOne.objectMap.get(edge.getTarget()), edge.getType(), true)) return false;
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * Create an {@link EGraph eGraph} from the given {@link View view}.
	 * The {@link EGraph eGraph} will contain copies of the {@link EObject eObjects} of
	 * the {@link View view's} {@link View#resource resource}. The {@link EGraph eGraph}
	 * will only contain such {@link EObject eObjects} that are contained by the given {@link View view}.
	 * Also the {@link EObject eObjects} referenced by the  {@link EObject eObjects} in the {@link EGraph eGraph}
	 * will comply to the {@link Edge edges} of the {@link View#graph}. The given {@link View view} must not contain dangling edges.
	 * @param view the {@link View view} to create an {@link EGraph eGraph} of
	 * @return Returns an {@link EGraph eGraph} as described above and a map from the original {@link EObject eObjects}
	 * to the copied ones.
	 * @throws IllegalArgumentException if the given {@link View view} contains dangling edges
	 */
	public static Pair<EGraph, Map<EObject, EObject>> createEGraphFromView (View view) {
		
		if(view.isEmpty()) return new Pair<EGraph, Map<EObject,EObject>>(new EGraphImpl(), new HashMap<>());
		
		boolean viewContainsDanglingEdge = view.graph.getEdges().stream().anyMatch(edge -> !view.contains(edge.getSource()) || !view.contains(edge.getTarget()));
		
		if (viewContainsDanglingEdge) throw new IllegalArgumentException("The view must not contain dangling edges.");
		
	    Copier copier = new Copier(true, false);
	    
	    EObject rootEObject = view.resource.getContents().get(0);
	    EObject copyOfRotEObject = copier.copy(rootEObject);
	    
	    copier.copyReferences();
	    
	    EGraph eGraph = new EGraphImpl(copyOfRotEObject);
	    
		Map<EObject, EObject> copiesReversed = new HashMap<>();
		
		copier.forEach((originalEObject, copiedEObject) -> {
			copiesReversed.put(copiedEObject, originalEObject);
		});
	    
		// remove all nodes not contained in the view
		
	    List<EObject> toRemove = new ArrayList<EObject>();
	    
	    for (EObject eObject : eGraph) {
			EObject originalEObject = copiesReversed.get(eObject);
			if(!view.graphMap.containsKey(originalEObject)) {
				copier.remove(originalEObject);
				toRemove.add(eObject);
			}
		}
	    
	    for (EObject eObject : toRemove) {
	    	eGraph.remove(eObject);
		}
	    
	    // remove edges
	    
	    List<List<EObject>> toUnset = new ArrayList<List<EObject>>();
	    
	    for (EObject eObject : eGraph) {
	    	List<EReference> eReferences = eObject.eClass().getEAllReferences();
	    	for (EReference eReference : eReferences) {
				Object object = eObject.eGet(eReference);
				if (object instanceof EObject) {
					if (!view.contains(copiesReversed.get(eObject), copiesReversed.get((EObject) object), eReference, false)) {
						toUnset.add(List.of(eObject, (EObject) object, (EObject) eReference));
					}
				} else if (object != null) {
					@SuppressWarnings("unchecked")
					EList<EObject> eObjects = (EList<EObject>) object;
					for (EObject referencedEObject : eObjects) {
						if (!view.contains(copiesReversed.get(eObject), copiesReversed.get(referencedEObject), eReference, false)) {
							toUnset.add(List.of(eObject, referencedEObject, (EObject) eReference));
						}
					}
				}
			}
		}
	    
	    for (List<EObject> list : toUnset) {
	    	EcoreUtil.remove(list.get(0), ((EStructuralFeature) list.get(2)), list.get(1));
	    }
	    
		return new Pair<EGraph, Map<EObject,EObject>>(eGraph, copier);
		
	}
	
	/**
	 * Does a depth first search in the given {@link View view} starting
	 * at the given {@link Node node}. The given {@link View view} must not
	 * contain dangling edges.
	 * @param view the {@link View view} to do the depth first search on, it will not be altered
	 * @param node a {@link Node node} from the given {@link View view} to start the search at
	 * @return Returns a new {@link View view}, that is a subgraph of the given {@link View view} and contains
	 * all {@link Node nodes} and {@link Edge edges} reachable by the depth first search.
	 * @throws IllegalArgumentException  if the given {@link View view} contains dangling edges or the {@link Node node}
	 * is not part of the {@link View view}
	 */
	public static View doDFS (View view, Node node) throws IllegalArgumentException {
		
		if(!view.contains(node)) throw new IllegalArgumentException("The node is not part of the view.");
		boolean viewContainsDanglingEdge = view.graph.getEdges().stream().anyMatch(edge -> !view.contains(edge.getSource()) || !view.contains(edge.getTarget()));
		if(viewContainsDanglingEdge) throw new IllegalArgumentException("The view must not contain dangling edges.");
		
		Set<Node> visitedNodes = new HashSet<>();
		internalDoDFS(view, node, visitedNodes);
		
		View dfsView = new View(view.resource);
		
		for (Node visitedNode : visitedNodes) {
			dfsView.extend(view.getObject(visitedNode));
		}
		
		dfsView.extendByMissingEdges();
		
		return dfsView;
		
	}
	
	private static void internalDoDFS (View view, Node node, Set<Node> visitedNodes) {
		
		visitedNodes.add(node);
		
		for (Edge edge : view.graph.getEdges()) {
			if (edge.getSource() == node) {
				if(!visitedNodes.contains(edge.getTarget()))
					internalDoDFS(view, edge.getTarget(), visitedNodes);
			} else if (edge.getTarget() == node) {
				if(!visitedNodes.contains(edge.getSource()))
					internalDoDFS(view, edge.getSource(), visitedNodes);
			}
		}
		
	}
	
	/**
	 * @param view the {@link View view} to get subgraph-views of
	 * @param subgraphView the {@link View view} to be contained in all subgraph returned by the iterator
	 * @return Returns an {@link Iterator} of {@link View views} containing all possible subgraphs
	 * of the given {@link View view} that contain the {@link View subgraphView}.
	 * @throws IllegalArgumentException if the given {@link View subgraphView} is not a subgraph
	 * of the {@link View view} or one of the {@link View views} contain dangling edges.
	 */
 	public static Iterator<View> getSubGraphIterator (View view, View subgraphView) throws IllegalArgumentException {
		
		if(!ViewFactory.isSubgraph(subgraphView, view)) throw new IllegalArgumentException("The subgraphView must be a subgraph of the view.");
		boolean viewContainsDanglingEdge = view.graph.getEdges().stream().anyMatch(edge -> !view.contains(edge.getSource()) || !view.contains(edge.getTarget()));
		if(viewContainsDanglingEdge) throw new IllegalArgumentException("The view must not contain dangling edges.");
		viewContainsDanglingEdge = subgraphView.graph.getEdges().stream().anyMatch(edge -> !subgraphView.contains(edge.getSource()) || !subgraphView.contains(edge.getTarget()));
		if(viewContainsDanglingEdge) throw new IllegalArgumentException("The subgraphView must not contain dangling edges.");
		
		Set<Node> subgraphNodes = subgraphView.graph.getNodes().stream().map(node -> view.getNode(subgraphView.getObject(node))).collect(Collectors.toSet());
		Set<Edge> subgraphEdges = subgraphView.graph.getEdges().stream().map(edge -> {
			EObject sourceEObject = subgraphView.getObject(edge.getSource());
			EObject targetEObject = subgraphView.getObject(edge.getTarget());
			Node nodeOne = view.getNode(sourceEObject);
			Node nodeTwo = view.getNode(targetEObject);
			
			Edge foundEdge = nodeOne.getOutgoing(edge.getType(), nodeTwo);
			if(foundEdge != null) return foundEdge;
			foundEdge = nodeOne.getIncoming(edge.getType(), nodeTwo);
			return foundEdge;
		}).collect(Collectors.toSet());
		
		return new Iterator<View>() {

			Iterator<Set<Node>> powersetIterator = null;
			boolean computedNextSubgraph = false;
			View nextSubgraph = null;
			Queue<View> computedViews = null;
			
			@Override
			public boolean hasNext() {
				
				if (!computedNextSubgraph) {
					computeNextSubgraph();
					computedNextSubgraph = true;
				}
				
				return (nextSubgraph != null);
				
			}

			@Override
			public View next() {
				
				if (hasNext()) {
					computedNextSubgraph = false;
				}
				
				return nextSubgraph;
				
			}
			
			private void computeNextSubgraph() {
				
				if (powersetIterator == null) {
					powersetIterator = getPowerSetIterator(new HashSet<>(view.graph.getNodes()));
				}
				
				if (computedViews == null) {
					computedViews = new LinkedList<View>();
				}

				if (computedViews.isEmpty()) {
					
					if(!powersetIterator.hasNext()) {
						nextSubgraph = null;
					}
					
					while (powersetIterator.hasNext()) {
						
						Set<Node> set = (Set<Node>) powersetIterator.next();
						if (!set.containsAll(subgraphNodes)) continue;
						
						// get all edges that can be used with this set of nodes
						Set<Edge> edges = view.graph.getEdges().stream().
								filter(edge -> set.contains(edge.getSource()) && set.contains(edge.getTarget())).
								collect(Collectors.toSet());
						
						Iterator<Set<Edge>> edgePowerSetIterator = getPowerSetIterator(edges);
						
						while (edgePowerSetIterator.hasNext()) {
							
							Set<Edge> subsetOfEdges = (Set<Edge>) edgePowerSetIterator.next();
							if (!subsetOfEdges.containsAll(subgraphEdges)) continue;
							
							View possibleSubgraph = new View(view.resource);
							
							set.forEach(node -> possibleSubgraph.extend(view.getObject(node)));
							subsetOfEdges.forEach(edge -> {
								EObject sourceEObject = view.getObject(edge.getSource());
								EObject targetEObject = view.getObject(edge.getTarget());
								
								if (sourceEObject.eClass().getEAllReferences().contains(edge.getType())) {
									possibleSubgraph.extend(sourceEObject, targetEObject, edge.getType());
								}
								
								if (targetEObject.eClass().getEAllReferences().contains(edge.getType())) {
									possibleSubgraph.extend(targetEObject, sourceEObject, edge.getType());
								}
							});
							
							computedViews.add(possibleSubgraph);
							
						}
						
						break;
						
					}
					
					if(computedViews.isEmpty()) {
						nextSubgraph = null;
					} else {
						nextSubgraph = computedViews.poll();
					}
					
				} else {
					
					nextSubgraph = computedViews.poll();
					
				}
				
			}
			
		};
		
	}
	
 	static <T> Iterator<Set<T>> getPowerSetIterator (Set<T> setOfTs) {
		return new Iterator<Set<T>>() {

			boolean computedNextSet = false;
			Set<T> nextSet = null;
			Set<Set<T>> partialPowerSet = null;
			Iterator<Set<T>> partialPowerSetIterator = null;
			
			@Override
			public boolean hasNext() {
				
				if(!computedNextSet) {
					computeNextSet();
					computedNextSet = true;
				}
				
				return (nextSet != null);
				
			}

			@Override
			public Set<T> next() {
				
				if (hasNext()) {
					computedNextSet = false;
				}
				
				return nextSet;
				
			}
			
			private void computeNextSet() {
				
				// initialize
				if (partialPowerSet == null) {
					partialPowerSet = new HashSet<Set<T>>();
				}
					
				
				// case: empty set
				if (nextSet == null) {
					nextSet = new HashSet<T>();
					return;
				}
				
				// last case
				if (nextSet.equals(setOfTs)) {
					nextSet = null;
					return;
				}
				
				// as long as not all sets of the partialPowerSet have been shown use those
				if(partialPowerSetIterator != null && partialPowerSetIterator.hasNext()) {
					nextSet = partialPowerSetIterator.next();
					return;
				}
				
				// case: single element
				if (nextSet.isEmpty()) {
					for (T t : setOfTs) {
						partialPowerSet.add(Set.of(t));
					}
					
					partialPowerSetIterator = partialPowerSet.iterator();
					nextSet = partialPowerSetIterator.next();
					return;
				}
				
				// case: general
				
				Set<Set<T>> newPartialPowerSet = new HashSet<Set<T>>();
				
				for (Set<T> setInPartialPowerSet : partialPowerSet) {
					for (T t : setOfTs) {
						Set<T> tmpSet = new HashSet<>();
						tmpSet.addAll(setInPartialPowerSet);
						tmpSet.add(t);
						newPartialPowerSet.add(tmpSet);
					}
				}
				
				newPartialPowerSet.removeAll(partialPowerSet);
				partialPowerSet = newPartialPowerSet;
				partialPowerSetIterator = partialPowerSet.iterator();
				nextSet = partialPowerSetIterator.next();
				
			}
			
		};
 	}
 	
}
