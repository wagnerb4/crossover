package view;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.impl.EdgeImpl;
import org.eclipse.emf.henshin.model.impl.GraphImpl;
import org.eclipse.emf.henshin.model.impl.NodeImpl;

/**
 * This class represents a part of the {@code resource} as the {@code graph}.
 * The {@code graphMap} and {@code objectMap} are used to manage the injective mapping between the {@code graph} and the {@code resource}.
 * The {@link Edge} objects don't need to be managed separately as the edges have the attribute {@code type} of the type {@link EReference} 
 * which allows to dynamically compute the edge mapping if needed.
 * 
 * @author Benjamin Wagner
 *
 */
public class View {
	
	/**
	 * The {@link Resource} this {@link View} should reflect. 
	 */
	final Resource resource;
	
	/**
	 * The {@link Graph} used to reflect the elements from the {@link View#resource resource} and their structure.
	 * Reflected elements may be {@link EObject EObjects} and references between them. The mapping between this graph and
	 * the {@link View#resource resource} is recorded in the {@link View#graphMap graphMap} and the {@link View#objectMap objectMap}.
	 */
	Graph graph;
	
	Map<EObject, Node> graphMap;
	Map<Node, EObject> objectMap;
	
	/**
	 * Creates a new empty {@link View}.
	 * @param resource the {@link View#resource resource} to be used for the new {@link View}
	 */
	public View(Resource resource) {
		super();
		this.resource = resource;
		graphMap = new HashMap<EObject, Node>();
		objectMap = new HashMap<Node, EObject>();
		graph = new GraphImpl();
	}

	
	/**
	 * @return Returns the {@link View#resource resource}.
	 */
	Resource getResource() {
		return resource;
	}
	
	
	/**
	 * @param eObject an {@link EObject} form the {@link View#resource resource}
	 * @return Returns the {@link Node} mapped to the given {@link EObject} or {@code null} if the {@link EObject} is not part of the {@link View}.
	 */
	public Node getNode(EObject eObject) {
		return graphMap.get(eObject);
	}
	
	
	/**
	 * @return Returns a random {@link Node} form the {@link View#graph graph} or {@code null} if the {@link View#graph graph} is empty.
	 */
	public Node getRandomNode() {
		if(graphMap.isEmpty()) {
			return null;
		} else {
			Collection<Node> nodes = graphMap.values();
			int randomIndex = (int) (Math.random() * nodes.size());
			return (Node) nodes.toArray()[randomIndex];
		}
	}
	
	
	/**
	 * @param node an {@link Node} from the {@link View#graph graph}
	 * @return Returns the {@link EObject} mapped to the given {@link Node} or {@code null} if the {@link Node} is not part of the {@link View Views} {@link View#graph graph}.
	 */
	public EObject getObject(Node node) {
		return objectMap.get(node);
	}
	
	
	/**
	 * @return Returns {@code true} if the {@link View} represents no elements from its {@link View#resource resource}, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return graph.getNodes().isEmpty() && graph.getEdges().isEmpty();
	}
	
	
	/**
	 * Extends the {@link View} by all {@link EObject EObjects} in the {@link View#resource resource} of the given {@link EObject#eClass() type} using the {@link View#extend(EClass)} method.
	 * @param eClass the {@link EObject#eClass() type} of {@link EObject} to extend by
	 * @return Returns {@code true} if at least one matched {@link EObject} has been added and {@code false} otherwise.
	 */
	public boolean extend(EClass eClass) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if (eObject.eClass().equals(eClass)) {
				foundMatch |= extend(eObject);
			}
		}
		
		return foundMatch;	
	}
	
	
	/**
	 * Extends the {@link View} by all instances of the {@code eReference} in the {@link View#resource resource} using the {@link View#extend(EObject, EReference)} method.
	 * @param eReference the {@link EReference} to extend by
	 * @return Returns {@code true} if at least one instance of the {@code eReference} has been added and {@code false} otherwise.
	 */
	public boolean extend(EReference eReference) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			foundMatch |= extend(eObject, eReference);
		}
		
		return foundMatch;
	}
	
	
	/**
	 * Extends the {@link View} by the given {@code eObject}.
	 * @param eObject the {@link EObject} to add to the {@link View}
	 * @return Returns {@code true} if the given {@link EObject} is part of the {@link View#resource resource} <b>but not</b> yet part of the {@link View}.
	 */
	public boolean extend(EObject eObject) {
		// check if the eObject is contained in the resource
		String uriFragment = resource.getURIFragment(eObject);
		if (uriFragment == null || uriFragment.isEmpty() || uriFragment.equals("/-1")) return false;
		
		// check if the eObject is already part of the view
		if (graphMap.containsKey(eObject)) return false;
		
		Node node = new NodeImpl();
		node.setGraph(graph);
		node.setType(eObject.eClass());
		
		graphMap.put(eObject, node);
		objectMap.put(node, eObject);
		graph.getNodes().add(node);
		
		return true;
	}
	
	
	/**
	 * Extends the {@link View} by the given {@code eReference}-feature of the {@code eObject}.
	 * @param eObject the {@link EObject} {@link EObject#eGet(org.eclipse.emf.ecore.EStructuralFeature) associated with} the {@code eReference}
	 * @param eReference the {@link EReference} to extend the {@link View} by
	 * @return Returns {@code false} if the {@code eObject} is not part of the {@link View#resource resource} <b>or</b> not already part of the {@link View}
	 * <b>or</b> the given {@code eReference} is not {@link EObject#eGet(org.eclipse.emf.ecore.EStructuralFeature) associated with} the {@code eObject} 
	 * <b>or</b> the referenced {@link EObject} is not already part of the {@link View}. Returns {@code true} if a new {@link Edge} has been added to the {@link View#graph graph}.
	 */
	public boolean extend(EObject eObject, EReference eReference) {
		// check if the eObject is contained in the resource
		String uriFragment = resource.getURIFragment(eObject);
		if (uriFragment == null || uriFragment.isEmpty() || uriFragment.equals("/-1")) return false;
		
		// check if the eObject is already part of the view
		if (!graphMap.containsKey(eObject)) return false;
		
		try {
			Object object = eObject.eGet(eReference);
			
			if(object instanceof EObject) {
				EObject referencedEObject = (EObject) object;
				
				if(!graphMap.containsKey(referencedEObject)) return false;
				
				Edge edge = new EdgeImpl(graphMap.get(eObject), graphMap.get(referencedEObject), eReference);
				edge.setGraph(graph);
				
				graph.getEdges().add(edge);
				
				return true;
			} else {
				return false;
			}
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
	
	
	/**
	 * Removes all {@link EObject EObjects} with the given {@link EObject#eClass() type} from the {@link View} using the {@link View#reduce(EObject)} method.
	 * @param eClass the {@link EObject#eClass() type} of {@link EObject EObjects} to remove
	 * @return Returns {@code true} if at least one match has been found and removed, {@code false} otherwise.
	 */
	public boolean reduce(EClass eClass) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if (eObject.eClass().equals(eClass)) {
				foundMatch |= reduce(eObject);
			}
		}
		
		return foundMatch;	
	}
	
	
	/**
	 * Removes all instances of the {@code eReference} in the {@link View#resource resource} from the {@link View} using the {@link View#reduce(EObject, EReference)} method.
	 * @param eReference the {@link EReference} to remove
	 * @return Returns {@code true} if at least one instance of the {@code eReference} has been removed and {@code false} otherwise.
	 */
	public boolean reduce(EReference eReference) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			foundMatch |= reduce(eObject, eReference);
		}
		
		return foundMatch;
	}
	
	
	/**
	 * Removes the given {@link EObject} from the {@link View}.
	 * @param eObject the {@link EObject} to remove
	 * @return Returns {@code true} if the {@link EObject} has been removed successfully and 
	 * {@code false} if the {@link EObject} is not part of the {@link View} <b>or</b> there are still {@link #Edge Edges} connected to the {@link Node}.
	 */
	public boolean reduce(EObject eObject) {
		// check if the eObject is part of the view
		if(!graphMap.containsKey(eObject)) return false;
		
		// check if there are still edges connected to this node
		if(graphMap.get(eObject).getOutgoing().size() > 0 || graphMap.get(eObject).getIncoming().size() > 0) return false;
		
		if(!graph.removeNode(graphMap.get(eObject))) return false;
		objectMap.remove(graphMap.get(eObject));
		graphMap.remove(eObject);
		return true;
	}
	
	
	/**
	 * Removes an instance of the given {@link EReference} from the {@link View}.
	 * @param eObject the {@link EObject} the {@link EReference} is {@link EObject#eGet(org.eclipse.emf.ecore.EStructuralFeature) associated with}
	 * @param eReference the {@link Edge#getType() type} of {@link Edge} to be removed
	 * @return Returns {@code true} if the specified {@link Edges} has been removed successfully.
	 * Returns {@code false} if the {@code eObject} is not part of the view <b>or</b> the specified {@link Node} doesn't have an {@link Edge} of the correct {@link Edge#getType() type}.
	 */
	public boolean reduce(EObject eObject, EReference eReference) {
		if(!graphMap.containsKey(eObject)) return false;
		List<Edge> edges = graphMap.get(eObject).getAllEdges();
		if(edges.isEmpty()) return false;
		
		boolean removedEdge = false;
		
		for (Edge edge : edges ) {
			if (edge.getType().equals(eReference)) {
				removedEdge |= graph.removeEdge(edge);
			}
		}
		
		return removedEdge;
	}
	
	
	/**
	 * @return Returns a copy of this {@link View} containing separate Maps and a 
	 * separate {@link View#graph graph} but the same {@link Node Nodes} and {@link Edge Edges}.
	 */
	public View copy() {
		
		View copyOfView = new View(resource);
		
		Graph copyOfGraph = new GraphImpl(graph.getName());
		copyOfGraph.getNodes().addAll(graph.getNodes());
		copyOfGraph.getEdges().addAll(graph.getEdges());
		copyOfView.graph = copyOfGraph;
		
		copyOfView.graphMap.putAll(graphMap);
		
		copyOfView.objectMap.putAll(objectMap);
		
		return copyOfView;
		
	}
	
	
	/**
	 * Changes this {@link View} to be empty.
	 */
	public void clear() {
		
		graph.getEdges().forEach(graph::removeEdge);
		graph.getNodes().forEach(graph::removeNode);
		graphMap.clear();
		objectMap.clear();
		
	}
	
	
	public void union(View view) {
		
	}
	
	
	public void intersect(View view) {
		
	}
	
	
	public void substract(View view) {
		
	}
	
	
	public void removeDangling() {
		
	}
	
	
	public void completeDangling() {
		
	}
	
	
	public void extendByMissingEdges() {
		
	}
	
	
	/**
	 * Reduces the {@link View} by any elements not part of the given {@code metamodel} and extends it by all that are.
	 * @param metamodel the meta-model or sub-meta-model of the {@link View#resource resource}
	 * @return Returns {@code true} if the {@link Resource} is a valid meta-model and {@code false} otherwise.
	 */
	public boolean matchViewByMetamodel(Resource metamodel) {
		
		clear();
		EList<EObject> contets = metamodel.getContents();
		
		if (!contets.isEmpty()) {
			
			if (contets.get(0) instanceof EPackage) {
				
				EPackage ePackage = (EPackage) contets.get(0);
				EList<EClassifier> classifiers = ePackage.getEClassifiers();
				
				for (EClassifier eClassifier : classifiers) {
					if (classifiers instanceof EClass) {
						extend(eClassifier);
						((EClass) eClassifier).getEReferences().forEach(this::extend);
					}
				}
				
				return true;
	
			} else {
				return false;
			}
		} else {
			return true;
		}
		
	}
	
}
