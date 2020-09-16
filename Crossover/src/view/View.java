package view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	 * All {@link Node nodes} and {@link Edge edges} in this graph are considered to be part of the {@link View}.
	 * Reflected elements may be {@link EObject EObjects} and references between them. The mapping between this graph and
	 * the {@link View#resource resource} is recorded in the {@link View#graphMap graphMap} and the {@link View#objectMap objectMap}.
	 */
	Graph graph;
	
	/**
	 * The map used to record the association from a given {@link EObject} to one specific {@link Node}.
	 * The {@link View#objectMap objectMap} should always be the exact inverse mapping of this map. </br>
	 * All {@link EObject EObjects} from the {@link View#resource resource} that are not part of this map's keyset are not part of this {@link View},
	 * however not all {@link EObject EObjects} from this map's keyset are necessarily part of this view (e.g are part of the {@link View#graph graph})
	 * as they may be part of <b>dangling</b> {@link Edge edges}.
	 */
	Map<EObject, Node> graphMap;
	
	/**
	 * The map used to record the association from a given {@link Node} to one specific {@link EObject}.
	 * The {@link View#graphMap graphMap} should always be the exact inverse mapping of this map.
	 */
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
		return contains(eObject) ? graphMap.get(eObject) : null;
	}
	
	
	/**
	 * @return Returns a random {@link Node} form the {@link View#graph graph} or {@code null} if the {@link View#graph graph} is empty.
	 */
	public Node getRandomNode() {
		if(graph.getNodes().isEmpty()) {
			return null;
		} else {
			List<Node> nodes = graph.getNodes();
			int randomIndex = (int) (Math.random() * nodes.size());
			return nodes.get(randomIndex);
		}
	}
	
	
	/**
	 * @param node an {@link Node} from the {@link View#graph graph}
	 * @return Returns the {@link EObject} mapped to the given {@link Node} or {@code null} if the {@link Node} is not part of the {@link View Views} {@link View#graph graph}.
	 */
	public EObject getObject(Node node) {
		return contains(node) ? objectMap.get(node) : null;
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
	 * @return Returns {@code true} if at least one matched {@link EObject} has been found and all matched {@link EObjects} have been added successfully and {@code false} otherwise.
	 */
	public boolean extend(EClass eClass) {
		
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		boolean addedSuccessfull = true;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if (eObject.eClass().equals(eClass)) {
				foundMatch = true;
				addedSuccessfull &= extend(eObject);
			}
		}
		
		return foundMatch & addedSuccessfull;	
		
	}
	
	
	/**
	 * Extends the {@link View} by all instances of the {@code eReference} in the {@link View#resource resource} using the {@link View#extend(EObject, EObject, EReference)} method.
	 * @param eReference the {@link EReference} to extend by
	 * @return Returns {@code true} if at least one instance of the {@code eReference} has been added successfully and {@code false} otherwise.
	 */
	public boolean extend(EReference eReference) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			
			if(eObject.eClass().getEAllReferences().contains(eReference)) {
				
				Object object = eObject.eGet(eReference);
				
				if(object instanceof EObject) {
					foundMatch |= extend(eObject, ((EObject) object), eReference);
				} else if (object != null) {
					@SuppressWarnings("unchecked") // see EObject#eGet(EStructuralFeature)
					EList<EObject> eListOfEObjects = (EList<EObject>) object;
					
					for (EObject referencedEObject : eListOfEObjects) {
						foundMatch |= extend(eObject, referencedEObject, eReference);
					}
				}
				
			}
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
		if (contains(eObject)) return false;
		
		Node node = new NodeImpl();
		node.setGraph(graph);
		node.setType(eObject.eClass());
		
		graphMap.put(eObject, node);
		objectMap.put(node, eObject);
		graph.getNodes().add(node);
		
		return true;
	}
	
	
	/**
	 * Extends the {@link View} by the {@link Edge edge} specified by the given parameters. 
	 * This operation may result in an {@link Graph graph} with dangling {@link Edge edges}.
	 * @param eObject the {@link EObject} {@link EObject#eGet(org.eclipse.emf.ecore.EStructuralFeature) associated with} the {@code eReference}
	 * @param referencedEObject the by the given {@link EObject eObject} and {@link EReference eReference} referenced {@link EObject}.
	 * @param eReference the {@link Edge#getType() type} of the {@link Edge} to extend the {@link View} by
	 * @return Returns {@code false} if the input parameters don't match the required conditions or the specified {@link Edge edge} is already {@link View#contains(EObject, EObject, EReference, boolean) contained} in the {@link View}.
	 * Returns {@code true} if a new {@link Edge} has been added to the {@link View#graph graph}.
	 */
	public boolean extend(EObject eObject, EObject referencedEObject, EReference eReference) {
		
		// check if the eObject is contained in the resource
		String uriFragment = resource.getURIFragment(eObject);
		if (uriFragment == null || uriFragment.isEmpty() || uriFragment.equals("/-1")) return false;
		
		// check if the referencedEObject is contained in the resource
		uriFragment = resource.getURIFragment(referencedEObject);
		if (uriFragment == null || uriFragment.isEmpty() || uriFragment.equals("/-1")) return false;
		
		// check whether the eReference references an EObject at all 
		if (!(eReference.getEType() instanceof EObject)) return false;
		
		// check whether the given referencedEObject really is referenced by eObject
		try {
			Object object = eObject.eGet(eReference);
			
			if(object instanceof EObject) {
				if (((EObject) object) != referencedEObject) return false;
			} else {
				@SuppressWarnings("unchecked") // see EObject#eGet(EStructuralFeature)
				EList<EObject> eListOfEObjects = (EList<EObject>) object;
				if (!eListOfEObjects.contains(referencedEObject)) {
					return false;
				}
			}
			
		} catch (IllegalArgumentException e1) {
			// the eReference is not part of the eObject's eClass
			return false;
		}
		
		// check if the edge is already contained in the view
		if (contains(eObject, referencedEObject, eReference, true)) return false;
		
		// add a new edge
		
		Node sourceNode, targetNode;
		
		if (graphMap.containsKey(eObject)) {
			// the edge might or might not be dangling
			sourceNode = graphMap.get(eObject);
		} else {
			// create a new node but don't add it to the graph as we want to create a dangling edge
			sourceNode = new NodeImpl();
			sourceNode.setGraph(graph);
			sourceNode.setType(eObject.eClass());
			graphMap.put(eObject, sourceNode);
			objectMap.put(sourceNode, eObject);
		}
		
		if(graphMap.containsKey(referencedEObject)) {
			// the edge might or might not be dangling
			targetNode = graphMap.get(referencedEObject);
		} else {
			// create a new node but don't add it to the graph as we want to create a dangling edge
			targetNode = new NodeImpl();
			targetNode.setGraph(graph);
			targetNode.setType(referencedEObject.eClass());
			graphMap.put(referencedEObject, targetNode);
			objectMap.put(targetNode, referencedEObject);
		}
		
		Edge edge = new EdgeImpl(sourceNode, targetNode, eReference);
		edge.setGraph(graph);
		graph.getEdges().add(edge);
		
		return true;

	}
	
	
	/**
	 * Removes all {@link EObject EObjects} with the given {@link EObject#eClass() type} from the {@link View} using the {@link View#reduce(EObject)} method.
	 * @param eClass the {@link EObject#eClass() type} of {@link EObject EObjects} to remove
	 * @return Returns {@code true} if at least one match has been found and all found matcher have been removed successfully, {@code false} otherwise.
	 */
	public boolean reduce(EClass eClass) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		boolean removedSuccessfully = true;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if (eObject.eClass().equals(eClass)) {
				foundMatch = true;
				removedSuccessfully &= reduce(eObject);
			}
		}
		
		return foundMatch & removedSuccessfully;	
	}
	
	
	/**
	 * Removes all instances of the {@code eReference} in the {@link View#resource resource} from the {@link View} using the {@link View#reduce(EObject, EObject, EReference)} method.
	 * @param eReference the {@link EReference} to remove
	 * @return Returns {@code true} if at least one instance of the {@code eReference} has been removed successfully and {@code false} otherwise.
	 */
	public boolean reduce(EReference eReference) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			
			if(eObject.eClass().getEAllReferences().contains(eReference)) {
				
				Object object = eObject.eGet(eReference);
				
				if(object instanceof EObject) {
					foundMatch |= reduce(eObject, ((EObject) object), eReference);
				} else if (object != null) {
					@SuppressWarnings("unchecked") // see EObject#eGet(EStructuralFeature)
					EList<EObject> eListOfEObjects = (EList<EObject>) object;
					
					for (EObject referencedEObject : eListOfEObjects) {
						foundMatch |= reduce(eObject, referencedEObject, eReference);
					}
				}
				
			}
		}
		
		return foundMatch;
	}
	
	
	/**
	 * Removes the given {@link EObject} from the {@link View}.
	 * This operation may result in an {@link Graph graph} with dangling {@link Edge edges}.
	 * @param eObject the {@link EObject} to remove
	 * @return Returns {@code true} if the {@link EObject} has been removed successfully and 
	 * {@code false} if the {@link EObject} is not part of the {@link View}.
	 */
	public boolean reduce(EObject eObject) {
		// check if the eObject is part of the view
		if(!contains(eObject)) return false;
		
		// I don't use the graph.removeNode method, as it automatically removes all of the attached edges
		graph.getNodes().remove(graphMap.get(eObject));
		
		objectMap.remove(graphMap.get(eObject));
		graphMap.remove(eObject);
		return true;
	}
	
	
	/**
	 * Removes an instance of the given {@link EReference} from the {@link View}.
	 * @param eObject the {@link EObject} the {@link EReference} is {@link EObject#eGet(org.eclipse.emf.ecore.EStructuralFeature) associated with}
	 * @param referencedEObject {@link EObject} refered by the given {@link EObject eObject} with the given {@link EReference eReference}
	 * @param eReference the {@link Edge#getType() type} of {@link Edge} to be removed
	 * @return Returns {@code true} if the specified {@link Edges edge} has been found and removed successfully.
	 * Returns {@code false} if the input parameters don't match the required conditions or the specified {@link Edge edge} is not {@link View#contains(EObject, EObject, EReference, boolean) contained} in the {@link View}.
	 */
	public boolean reduce(EObject eObject, EObject referencedEObject, EReference eReference) {
		// check if the eObject is contained in the resource
		String uriFragment = resource.getURIFragment(eObject);
		if (uriFragment == null || uriFragment.isEmpty() || uriFragment.equals("/-1")) return false;
		
		// check if the referencedEObject is contained in the resource
		uriFragment = resource.getURIFragment(referencedEObject);
		if (uriFragment == null || uriFragment.isEmpty() || uriFragment.equals("/-1")) return false;
		
		// check whether the eReference references an EObject at all 
		if (!(eReference.getEType() instanceof EObject)) return false;
		
		// check whether the given referencedEObject really is referenced by eObject
		try {
			Object object = eObject.eGet(eReference);
			
			if(object instanceof EObject) {
				if (((EObject) object) != referencedEObject) return false;
			} else {
				@SuppressWarnings("unchecked") // see EObject#eGet(EStructuralFeature)
				EList<EObject> eListOfEObjects = (EList<EObject>) object;
				if (!eListOfEObjects.contains(referencedEObject)) {
					return false;
				}
			}
			
		} catch (IllegalArgumentException e1) {
			// the eReference is not part of the eObject's eClass
			return false;
		}
		
		if(!contains(eObject, referencedEObject, eReference, true)) return false;
		
		List<Edge> edges = graphMap.get(eObject).getAllEdges();
		if(edges.isEmpty()) return false;
		
		boolean foundEdge = false;
		boolean removedEdge = true;
		
		for (Edge edge : edges ) {
			if (edge.getType().equals(eReference) && 
					(edge.getSource() == graphMap.get(referencedEObject) || 
					edge.getTarget() == graphMap.get(referencedEObject))
				) {
				foundEdge = true;
				removedEdge &= graph.removeEdge(edge);
			}
		}
		
		return foundEdge & removedEdge;
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
	
	
	/**
	 * Calculates the union of this and the given {@link View}, altering this {@link View} in the process.
	 * This operation may result in an {@link Graph graph} with dangling {@link Edge edges}.
	 * @param view the {@link View} to unite with (it is required to have the same {@link View#resource resource} as this {@link View})
	 * @throws ViewSetOperationException If the union has not been successful and the current {@link View} is in an uncertain state.
	 */
	public void union(View view) throws ViewSetOperationException {
		View savedState = copy();
		
		if (resource != view.resource) throw new ViewSetOperationException("The resources are not identical.", savedState);
		
		// add all contained nodes from the given view to this one
		Iterator<EObject> containedNodesIterator = view.graph.getNodes().stream().map(view.objectMap::get).iterator();
		boolean addingSuccessfull = true;
		
		while (containedNodesIterator.hasNext()) {
			EObject eObject = (EObject) containedNodesIterator.next();
			if (!contains(eObject)) {
				addingSuccessfull &= extend(eObject);
			}
		}
		
		if (!addingSuccessfull) throw new ViewSetOperationException("Some nodes cannot be added.", savedState);
		
		// add all contained edges from the given view to this one
		List<Edge> edges = view.graph.getEdges();
		
		for (Edge edge : edges) {
			EObject sourceEObject = view.objectMap.get(edge.getSource());
			EObject targetEObject = view.objectMap.get(edge.getTarget());
			
			boolean viewContainsEdge = contains(sourceEObject, targetEObject, edge.getType(), true);
			 
			if (!viewContainsEdge) {
				if(sourceEObject.eClass().getEAllReferences().contains(edge.getType()) && 
						sourceEObject.eGet(edge.getType()).equals(targetEObject) && 
						!extend(sourceEObject, targetEObject, edge.getType())) {
					throw new ViewSetOperationException("Cannot add an edge.", savedState);
				} else if (targetEObject.eClass().getEAllReferences().contains(edge.getType()) && 
						targetEObject.eGet(edge.getType()).equals(sourceEObject) && 
						!extend(targetEObject, sourceEObject, edge.getType())) {
					throw new ViewSetOperationException("Cannot add an edge.", savedState);
				} else {
					throw new ViewSetOperationException("Cannot add an edge.", savedState);
				}
			}
			
		}
	}
	
	/**
	 * Calculates the intersection of this and the given {@link View}, altering this {@link View} in the process.
	 * This operation may result in an {@link Graph graph} with dangling {@link Edge edges}.
	 * @param view the {@link View} to intersect with (it is required to have the same {@link View#resource resource} as this {@link View})
	 * @throws ViewSetOperationException If the intersection has not been successful and the current {@link View} is in an uncertain state.
	 */
	public void intersect(View view) throws ViewSetOperationException {
		View savedState = copy();
		
		if (resource != view.resource) throw new ViewSetOperationException("The resources aren't identical.", savedState);
		
		// remove all edges contained in this view that are not part of the given view
		List<Edge> edges = graph.getEdges();
		
		for (Edge edge : edges) {
			
			// find out if the given view contains the edge
			
			EObject sourceEObject = objectMap.get(edge.getSource());
			EObject targetEObject = objectMap.get(edge.getTarget());
			boolean graphContainsEdge = view.contains(sourceEObject, targetEObject, edge.getType(), true);

			if (!graphContainsEdge) {
				// remove the edge form this view
				boolean removedAny = false;
				
				if(sourceEObject.eClass().getEAllReferences().contains(edge.getType()) && 
						sourceEObject.eGet(edge.getType()).equals(targetEObject)) {
					removedAny = true;
					if(!reduce(sourceEObject, targetEObject, edge.getType())) throw new ViewSetOperationException("Cannot remove an edge.", savedState);
				}
				
				if (targetEObject.eClass().getEAllReferences().contains(edge.getType()) && 
						targetEObject.eGet(edge.getType()).equals(sourceEObject)) {
					removedAny = true;
					if(!reduce(targetEObject, sourceEObject, edge.getType())) throw new ViewSetOperationException("Cannot remove an edge.", savedState);
				}
				
				if(!removedAny) throw new ViewSetOperationException("Cannot remove an edge.", savedState);
			}
		}
		
		// remove all nodes contained in this view that are not part of the given view
		List<EObject> eObjects = graph.getNodes().stream().map(objectMap::get).collect(Collectors.toList());
		
		for (EObject eObject : eObjects) {
			if(!view.contains(eObject)) {
				if(!reduce(eObject)) throw new ViewSetOperationException("Cannot remove a node.", savedState);
			}
		}
	}
	
	/**
	 * Calculates the difference of this and the given {@link View}, altering this {@link View} in the process.
	 * This operation may result in an {@link Graph graph} with dangling {@link Edge edges}.
	 * @param view the {@link View} to subtract (it is required to have the same {@link View#resource resource} as this {@link View})
	 * @throws ViewSetOperationException If the subtraction has not been successful and the current {@link View} is in an uncertain state.
	 */
	public void subtract(View view) throws ViewSetOperationException {
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
	
	/**
	 * Checks if the given {@link Node node} is part of the {@link View}.
	 * @param node the {@link Node} to check the containment of
	 * @return Returns whether the given {@link Node node} is part of the {@link View}.
	 */
	private boolean contains(Node node) {
		return graph.getNodes().contains(node);
	}
	
	/**
	 * Checks if the given {@link Edge edge} is part of the {@link View}.
	 * @param edge the {@link Edge} to check the containment of
	 * @param isDangling whether the given {@link Edge edge} is considered to be dangling
	 * @return Returns whether the given {@link Edge edge} is part of the {@link View}.
	 */
	private boolean contains(Edge edge, boolean isDangling) {
		return isDangling ? graph.getEdges().contains(edge) : graph.getEdges().contains(edge) && contains(edge.getSource()) && contains(edge.getTarget());
	}
	
	/**
	 * Checks if the given {@link EObject eObject} is part of the {@link View}.
	 * @param eObject an {@link EObject} form the {@link View#resource resource} to check the containment of
	 * @return Returns whether the given {@link EObject eObject} is part of the {@link View}.
	 */
	private boolean contains(EObject eObject) {
		return graphMap.containsKey(eObject) && contains(graphMap.get(eObject));
	}
	
	/**
	 * Checks if the specified {@link Edge} is part of the {@link View}.
	 * @param sourceEObject an {@link EObject} from the {@link View#resource resource} 
	 * that is different from the targetEObject and considered to be one {@link Node node} of the {@link Edge edge}. 
	 * @param targetEObject an {@link EObject} from the {@link View#resource resource}
	 * that is different form the sourceEObject and considered to be one {@link Node node} of the {@link Edge edge}.
	 * @param eReference the {@link Edge#getType() type} of the {@link Edge edge}.
	 * @param isDangling whether the given {@link Edge edge} is considered to be dangling
	 * @return Returns whether the specified {@link Edge} is part of the {@link View}.
	 */
	private boolean contains(EObject sourceEObject, EObject targetEObject, EReference eReference, boolean isDangling) {
		if (sourceEObject == targetEObject) return false;
		if(!graphMap.containsKey(sourceEObject) || !graphMap.containsKey(targetEObject)) return false;
		Node sourceNode = graphMap.get(sourceEObject);
		Node targetNode = graphMap.get(targetEObject);
		Edge edge = sourceNode.getOutgoing(eReference, targetNode);
		if(edge != null) return contains(edge, isDangling);
		edge = sourceNode.getIncoming(eReference, targetNode);
		if (edge == null) return false;
		return contains(edge, isDangling);
	}
}
