package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
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
	    
	    List<Function<?, ?>> toUnset = new ArrayList<Function<?, ?>>();
	    
	    for (EObject eObject : eGraph) {
	    	List<EReference> eReferences = eObject.eClass().getEAllReferences();
	    	for (EReference eReference : eReferences) {
	    		EStructuralFeature.Setting setting = ((InternalEObject)eObject).eSetting(eReference);
				Object object = eObject.eGet(eReference);
				if (object instanceof EObject) {
					if (!view.contains(copiesReversed.get(eObject), copiesReversed.get((EObject) object), eReference, false)) {
						toUnset.add((o) -> {
							setting.unset();
							return null;
						});
					}
				} else if (object != null) {
					@SuppressWarnings("unchecked")
					EList<EObject> eObjects = (EList<EObject>) object;
					for (EObject referencedEObject : eObjects) {
						if (!view.contains(copiesReversed.get(eObject), copiesReversed.get(referencedEObject), eReference, false)) {
							toUnset.add((o) -> {
								setting.unset();
								return null;
							});
						}
					}
				}
			}
		}
	    
	    toUnset.forEach(function -> {
	    	function.apply(null);
	    });
	    
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
		return null;
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
		
		return new Iterator<View>() {

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public View next() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
	}
	
}
