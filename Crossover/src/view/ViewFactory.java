package view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
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
		
		// convert the viewMappings to a Map
		
		Map<Node, Node> map = new HashMap<Node, Node>();
		viewMappings.forEach((Mapping mapping) -> map.put(mapping.getImage(), mapping.getOrigin()));
		
		// map the nodes from sourceView to nodes from targedView
		
		Set<Node> mappedNodes = sourceView.graph.getNodes().stream().map(map::get).
				filter(node -> node != null). // remove all null values to support partial mappings
				collect(Collectors.toSet());
		
		// create the new view and reduce it to the mapped nodes
		
		View mappedView = targetView.copy();
		targetView.graph.getNodes().stream().
			filter(node -> mappedNodes.contains(node)). // only keep nodes that should be removed
			map(targetView::getObject). // get the corresponding  EObjects
			forEach(mappedView::reduce); // reduce the new view by them
		
		// collect all mapped edges
		
		Set<Edge> mappedEdges = sourceView.graph.getEdges().stream().map(edge -> {
			
			Node mappedSource = map.get(edge.getSource());
			Node mappedTarget = map.get(edge.getTarget());
		
		    for (Edge outgoingEdge : mappedSource.getOutgoing()) {
				if (outgoingEdge.getTarget().equals(mappedTarget)) {
					return outgoingEdge;
				}
			}
		    
		    return null;
		    
		}).filter(node -> node != null).collect(Collectors.toSet());
		
		// remove all not-mapped edges from the graph in the mappedView 
		
		mappedView.graph.getEdges().stream().filter(edge -> !mappedEdges.contains(edge)).forEach(mappedView.graph::removeEdge);
		
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
				if (!viewTwo.graph.getNodes().contains(node)) return false;
			for (Edge edge : viewOne.graph.getEdges())
				if (!viewTwo.graph.getEdges().contains(edge)) return false;
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
	 * will comply to the {@link Edge edges} of the {@link View#graph}.
	 * @param view The {@link View view} to create an {@link EGraph eGraph} of
	 * @return Returns an {@link EGraph eGraph} as described above and a map from the original {@link EObject eObjects}
	 * to the copied ones.
	 */
	public static Pair<EGraph, Map<EObject, EObject>> createEGraphFromView (View view) {
		
		TreeIterator<EObject> iterator = view.resource.getAllContents();
		Map<EObject, EObject> copies = new HashMap<>();
		
	    Copier copier = new Copier();
	    
	    while (iterator.hasNext()) {
			Object object = (Object) iterator.next();
			
			if(object instanceof EObject) {
				EObject originalEObject = (EObject) object;
				EObject copiedEObject = copier.copy(originalEObject);
				copies.put(originalEObject, copiedEObject);
			}
			
		}
	    
	    copier.copyReferences();
	    
	    EGraph eGraph = new EGraphImpl(copies.values());
	    
	    List<EObject> toRemove = eGraph.stream().filter(eObject -> !view.contains(eObject)).collect(Collectors.toList());
	    
	    for (EObject eObject : toRemove) {
			eGraph.remove(eObject);
			copies.remove(eObject);
		}
	    
	    for (EObject eObject : eGraph) {
	    	List<EReference> eReferences = eObject.eClass().getEAllReferences();
	    	for (EReference eReference : eReferences) {
	    		EStructuralFeature.Setting setting = ((InternalEObject)eObject).eSetting(eReference);
				Object object = eObject.eGet(eReference);
				if (object instanceof EObject) {
					if (!view.contains(eObject, (EObject) object, eReference, false)) {
						setting.unset();
					}
				} else {
					@SuppressWarnings("unchecked")
					EList<EObject> eObjects = (EList<EObject>) object;
					for (EObject referencedEObject : eObjects) {
						if (!view.contains(eObject, referencedEObject, eReference, false)) {
							setting.unset();
						}
					}
				}
			}
		}
	    
		return new Pair<EGraph, Map<EObject,EObject>>(eGraph, copies);
		
	}
	
	public static View doDFS (View view, Node node) {
		return null;
	}
	
	
	public static Iterator<View> getSubGraphIterator (View view) {
		return null;
	}
	
}
