package view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
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
	 * @implNote Only {@link Resource resources} with one root element are supported.
	 * The use of mulit-root {@link Resource resources} may result in unforeseen consequences.
	 * @see View#graph
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
	 * @throws IllegalArgumentException if the {@link Resource resource} {@link Resource#getContents() contains} none or more that
	 * one root element(s)
	 */
	public View(Resource resource) {	
		if (resource.getContents().size() != 1) throw new IllegalArgumentException("The given resource should only contain one root element.");
		this.resource = resource;
		graphMap = new HashMap<EObject, Node>();
		objectMap = new HashMap<Node, EObject>();
		graph = new GraphImpl();
	}

	/**
	 * @return Returns the {@link View#resource resource}.
	 */
	public Resource getResource() {
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
	
	public Collection<EObject> getContainedEObjects () {
		return graph.getNodes().stream().map(this::getObject).collect(Collectors.toSet());
	}
	
	/**
	 * @return Returns the {@link View#graph}.
	 */
	public Graph getGraph () {
		return graph;
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
			if (eObject.eClass().equals(eClass) || eObject.eClass().getEAllSuperTypes().contains(eClass)) {
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
		
		Node node;
		
		if(graphMap.containsKey(eObject)) {
			node = graphMap.get(eObject);
		} else {
			node = new NodeImpl();
			node.setGraph(graph);
			node.setType(eObject.eClass());
			graphMap.put(eObject, node);
			objectMap.put(node, eObject);
		}

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
	 * @return Returns {@code true} if at least one match has been found and all found matches have been removed successfully, {@code false} otherwise.
	 */
	public boolean reduce(EClass eClass) {
		TreeIterator<EObject> iterator = resource.getAllContents();
		boolean foundMatch = false;
		boolean removedSuccessfully = true;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if (eObject.eClass().equals(eClass) || eObject.eClass().getEAllSuperTypes().contains(eClass)) {
				foundMatch = true;
				removedSuccessfully &= reduce(eObject);
			}
		}
		
		return foundMatch & removedSuccessfully;	
	}
	
	/**
	 * Removes all instances of the {@code eReference} in the {@link View#resource resource} from the {@link View} using the {@link View#reduce(EObject, EObject, EReference)} method.
	 * @param eReference the {@link EReference} to remove
	 * @return Returns {@code true} if at least one instance of the {@code eReference} has been found and all found instances have been removed successfully, {@code false} otherwise.
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
		
		boolean allReferencesRemoved = graph.getEdges().stream().allMatch(edge -> !edge.getType().equals(eReference));
		
		return foundMatch & allReferencesRemoved;
		
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
		if(!graphMap.containsKey(eObject)) return false;
		
		Node node = graphMap.get(eObject);
		
		// Only remove the eObject from the maps if no edge needs it
		boolean existsEdgeWithEObject = graph.getEdges().stream().anyMatch(edge -> objectMap.get(edge.getSource()) == eObject || objectMap.get(edge.getTarget()) == eObject);
		
		if (contains(node)) {
			// I don't use the graph.removeNode method, as it automatically removes all of the attached edges
			graph.getNodes().remove(node);
			
			if(!existsEdgeWithEObject) {
				
				objectMap.remove(node);
				graphMap.remove(eObject);
				
			}
			
			return true;
		} else {
			
			if(!existsEdgeWithEObject) {
				
				objectMap.remove(node);
				graphMap.remove(eObject);
				return true;
				
			} else {
				
				return false;
				
			}
			
		}
		
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
			if (((edge.getType().equals(eReference)) || (edge.getType().equals(eReference.getEOpposite()))) && 
					(edge.getSource() == graphMap.get(referencedEObject) || 
					edge.getTarget() == graphMap.get(referencedEObject))
				) {
				
				foundEdge = true;
				Node sourceNode = edge.getSource();
				Node targetNode = edge.getTarget();
				removedEdge &= graph.removeEdge(edge);
				// remove nodes if the edge was dangling and no other edge needs them
				
				if (!graph.getNodes().contains(sourceNode)) {
					boolean existsOtherEdgeWithSource = graph.getEdges().stream().anyMatch(edgeTwo -> edgeTwo.getSource() == sourceNode || edgeTwo.getTarget() == sourceNode);
					if(!existsOtherEdgeWithSource) {
						EObject eObjectForNode = objectMap.get(sourceNode);
						graphMap.remove(eObjectForNode, sourceNode);
						objectMap.remove(sourceNode, eObjectForNode);
					}
				}
				
				if (!graph.getNodes().contains(targetNode)) {
					boolean existsOtherEdgeWithTarget = graph.getEdges().stream().anyMatch(edgeTwo -> edgeTwo.getSource() == targetNode || edgeTwo.getTarget() == targetNode);
					if(!existsOtherEdgeWithTarget) {
						EObject eObjectForNode = objectMap.get(targetNode);
						graphMap.remove(eObjectForNode, targetNode);
						objectMap.remove(targetNode, eObjectForNode);
					}
					
				}
				
			}
		}
		
		return foundEdge & removedEdge;
		
	}
	
	/**
	 * @return Returns a copy of this {@link View} containing separate Maps and a 
	 * separate {@link View#graph graph}.
	 */
	public View copy() {
		
		View copyOfView = new View(resource);
		
		Graph copyOfGraph = new GraphImpl();
		
		Map<Node, Node> nodeMap = new HashMap<Node, Node>();
		
		for (Edge edge : graph.getEdges()) {
			
			Edge newEdge = new EdgeImpl();
			newEdge.setType(edge.getType());
			
			if(!nodeMap.containsKey(edge.getSource())) {
				
				Node newNode = new NodeImpl();
				newNode.setType(edge.getSource().getType());
				newEdge.setSource(newNode);
				
				if(graph.getNodes().contains(edge.getSource()))
					newNode.setGraph(copyOfGraph);
				
				copyOfView.graphMap.put(objectMap.get(edge.getSource()), newNode);
				copyOfView.objectMap.put(newNode, objectMap.get(edge.getSource()));
				
				nodeMap.put(edge.getSource(), newNode);
				
			} else {
				
				newEdge.setSource(nodeMap.get(edge.getSource()));
				
			}
			
			if(!nodeMap.containsKey(edge.getTarget())) {
				
				Node newNode = new NodeImpl();
				newNode.setType(edge.getTarget().getType());
				newEdge.setTarget(newNode);
				
				if(graph.getNodes().contains(edge.getTarget()))
					newNode.setGraph(copyOfGraph);
				
				copyOfView.graphMap.put(objectMap.get(edge.getTarget()), newNode);
				copyOfView.objectMap.put(newNode, objectMap.get(edge.getTarget()));
				
				nodeMap.put(edge.getTarget(), newNode);
				
			} else {
				
				newEdge.setTarget(nodeMap.get(edge.getTarget()));
				
			}
			
			newEdge.setGraph(copyOfGraph);
			
		}
		
		for (Node node : graph.getNodes()) {
			if(!nodeMap.containsKey(node)) {
				
				Node newNode = new NodeImpl();
				newNode.setType(node.getType());
				newNode.setGraph(copyOfGraph);
				
				copyOfView.graphMap.put(objectMap.get(node), newNode);
				copyOfView.objectMap.put(newNode, objectMap.get(node));
				
				nodeMap.put(node, newNode);
				
			}
		}
		
		copyOfView.graph = copyOfGraph;
		
		return copyOfView;
		
	}
	
	/**
	 * Changes this {@link View} to be empty.
	 */
	public void clear() {
		
		graph.getEdges().clear();
		graph.getNodes().clear();
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
				boolean successfullyAdded = extend(sourceEObject, targetEObject, edge.getType());
				
				if (!successfullyAdded)
					successfullyAdded = extend(targetEObject, sourceEObject, edge.getType());
				
				if(!successfullyAdded)
					throw new ViewSetOperationException("Cannot add an edge.", savedState);
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
		
		if (resource != view.resource) throw new ViewSetOperationException("The resources are not identical.", savedState);
		
		// remove all edges contained in this view that are not part of the given view
		List<Edge> edges = graph.getEdges();
		List<List<EObject>> toRemove = new ArrayList<List<EObject>>();
		
		for (Edge edge : edges) {
			// find out if the given view contains the edge
			EObject sourceEObject = objectMap.get(edge.getSource());
			EObject targetEObject = objectMap.get(edge.getTarget());
			boolean graphContainsEdge = view.contains(sourceEObject, targetEObject, edge.getType(), true);

			if (!graphContainsEdge) {
				toRemove.add(List.of(sourceEObject, targetEObject, edge.getType()));
			}
		}
		
		for (List<EObject> list : toRemove) {
			// remove the edge form this view
			boolean removedAny = false;
			removedAny |= reduce(list.get(0), list.get(1), ((EReference) list.get(2)));
			removedAny |= reduce(list.get(1), list.get(0), ((EReference) list.get(2)));
			if(!removedAny) throw new ViewSetOperationException("Cannot remove an edge.", savedState);
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
	 * @return 
	 * @throws ViewSetOperationException If the subtraction has not been successful and the current {@link View} is in an uncertain state.
	 */
	public void subtract(View view) throws ViewSetOperationException {
		
		View savedState = copy();
		
		if (resource != view.resource) throw new ViewSetOperationException("The resources are not identical.", savedState);
		
		// remove all edges contained in the given view that are part of this view from this view
		List<Edge> edges = view.graph.getEdges();
		
		for (Edge edge : edges) {
			
			// find out if this view contains the edge
			EObject sourceEObject = view.objectMap.get(edge.getSource());
			EObject targetEObject = view.objectMap.get(edge.getTarget());
			boolean graphContainsEdge = contains(sourceEObject, targetEObject, edge.getType(), true);
			
			if(graphContainsEdge) {
				// remove the edge form this view
				boolean removedAny = false;
				removedAny |= reduce(sourceEObject, targetEObject, edge.getType());
				removedAny |= reduce(targetEObject, sourceEObject, edge.getType());
				if(!removedAny) throw new ViewSetOperationException("Cannot remove an edge.", savedState);
			}
			
		}
		
		// remove all nodes contained in the given view that are part of this view from this view
		List<EObject> eObjects = view.graph.getNodes().stream().map(view.objectMap::get).collect(Collectors.toList());
		
		for (EObject eObject : eObjects) {
			if(contains(eObject)) {
				if(!reduce(eObject)) throw new ViewSetOperationException("Cannot remove a node.", savedState);
			}
		}
		
	}
	
	/**
	 * Removes all dangling {@link Edge edges}. An {@link Edge edge} is considered to be dangling if it
	 * is part of the {@link View} but either (or both) of the {@link Edge edge's} {@link Node nodes} are not.
	 */
	public void removeDangling() {
		
		List<Edge> toRemove = new ArrayList<>();
		
		for (Edge edge : graph.getEdges()) {
			boolean edgeIsDangling = !graph.getNodes().contains(edge.getSource()) || !graph.getNodes().contains(edge.getTarget());
			if (edgeIsDangling) {
				toRemove.add(edge);
			}
		}
		
		for (Edge edge : toRemove) {
			graph.getEdges().remove(edge);
			
			if (!graph.getNodes().contains(edge.getSource())) {
				EObject eObject = objectMap.remove(edge.getSource());
				graphMap.remove(eObject);
			}
			
			if (!graph.getNodes().contains(edge.getTarget())) {
				EObject eObject = objectMap.remove(edge.getTarget());
				graphMap.remove(eObject);
			}
		}
		
	}
	
	/**
	 * Makes all dangling {@link Edge edges} '<i>real</i>' by adding their {@link Node nodes} to the {@link View#graph graph}.
	 * An {@link Edge edge} is considered to be dangling if it is part of the {@link View} but either (or both) of the 
	 * {@link Edge edge's} {@link Node nodes} are not.
	 */
	public void completeDangling() {
		
		for (Edge edge : graph.getEdges()) {
			boolean edgeIsDangling = !graph.getNodes().contains(edge.getSource()) || !graph.getNodes().contains(edge.getTarget());
			if (edgeIsDangling) {
				if(!graph.getNodes().contains(edge.getSource()))
					graph.getNodes().add(edge.getSource());
				if(!graph.getNodes().contains(edge.getTarget()))
					graph.getNodes().add(edge.getTarget());
			}
		}
		
	}
		
	/**
	 * Adds all {@link Edge edges} to the {@link View} that exist the {@link View#resource resource} and do not result in dangling {@link Edge edges}.
	 */
	public void extendByMissingEdges() {
		
		TreeIterator<EObject> treeIterator = resource.getAllContents();
		
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			List<EReference> eReferences = eObject.eClass().getEAllReferences();
			
			for (EReference eReference : eReferences) {
				Object object = eObject.eGet(eReference);
				
				if(object instanceof EObject) {
					if(contains(eObject) && contains((EObject) object))
						extend(eObject, ((EObject) object), eReference);
				} else if (object != null) {
					@SuppressWarnings("unchecked") // see EObject#eGet(EStructuralFeature)
					EList<EObject> eListOfEObjects = (EList<EObject>) object;
					
					for (EObject referencedEObject : eListOfEObjects) {
						if(contains(eObject) && contains(referencedEObject))
							extend(eObject, referencedEObject, eReference);
					}
				}
			}
			
		}
		
	}
	
	/**
	 * Adds all {@link EObject eObjects} to the {@link View} that exist the {@link View#resource resource}.
	 */
	public void extendByAllNodes() {
		
		TreeIterator<EObject> treeIterator = resource.getAllContents();
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			extend(eObject);
		}
		
	}
	
	/**
	 * Adds all {@link EObject eObjects} and references to the {@link View} that
	 * are {@link EObject#eContainer() containers} of {@link EObject eObjects} in the {@link View}.
	 */
	public void extendByContainers() {
		
		EObject rootEObject = resource.getContents().get(0);
		List<EObject> toExtendNodes = new ArrayList<>();
		List<List<EObject>> toExtendEdges = new ArrayList<>();
		
		for (Node node : graph.getNodes()) {
			
			EObject eObject = getObject(node);
			
			if (eObject == rootEObject) continue;
			
			EObject container = eObject.eContainer();
			EReference containingEReference = eObject.eContainmentFeature();
			
			if (!contains(container)) {
				toExtendNodes.add(container);
			}
			
			if (!contains(container, eObject, containingEReference, false)) {
				toExtendEdges.add(List.of(container, eObject, containingEReference));
			}
			
		}
		
		for (EObject eObject : toExtendNodes) {
			if (!contains(eObject)) {
				boolean successfullyExtended = extend(eObject);
				if (!successfullyExtended) throw new IllegalStateException("Couldn't extend the searchSpaceElementTwo view by an eObject.");
			}
		}
		
		for (List<EObject> list : toExtendEdges) {
			if (!contains(list.get(0), list.get(1), (EReference) list.get(2), false)) {
				boolean successfullyExtended = extend(list.get(0), list.get(1), (EReference) list.get(2));
				if (!successfullyExtended) throw new IllegalStateException("Couldn't extend the searchSpaceElementTwo view by an eObject.");
			}
		}
		
	}
	
	/**
	 * Reduces the {@link View} by any elements not part of the given {@link View viewOnMetamodel} and extends it by all that are.
	 * @param viewOnMetamodel a {@link View view} on the meta-model of the {@link View#resource resource}
	 * @return Returns {@code true} if the given {@link View viewOnMetamodel} and its {@link Resource resource} represent
	 * a valid meta-model and {@code false} otherwise. To be valid the {@link View viewOnMetamodel's} {@link Resource resource} 
	 * has to contain all the {@link EClass eClasses} of this {@link View#resource view's resource}. The given {@link View view}
	 * itself must not contain any dangling edges.
	 */
	public boolean matchViewByMetamodel(View viewOnMetamodel) {
		
		Set<EClass> eClasses = new HashSet<>();
		TreeIterator<EObject> treeIterator = viewOnMetamodel.resource.getAllContents();
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			if (eObject instanceof EClass) eClasses.add((EClass) eObject);
		}
		
		if (eClasses.isEmpty()) return false;
			
		treeIterator = this.resource.getAllContents();
		
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			if(!eClasses.contains(eObject.eClass()))
				return false; // the meta-model does not match the resource 
		}
		
		clear();
		
		for (Node node : viewOnMetamodel.graph.getNodes()) {
			EObject eObject = viewOnMetamodel.getObject(node);
			if (eObject instanceof EClass)
				extend(((EClass) eObject));
			if (eObject instanceof EReference)
				extend(((EReference) eObject));
		}
		
		return true;
		
	}
	
	/**
	 * Checks if the given {@link Node node} is part of the {@link View}.
	 * @param node the {@link Node} to check the containment of
	 * @return Returns whether the given {@link Node node} is part of the {@link View}.
	 */
	public boolean contains(Node node) {
		return graph.getNodes().contains(node);
	}
	
	/**
	 * Checks if the given {@link Edge edge} is part of the {@link View}.
	 * @param edge the {@link Edge} to check the containment of
	 * @param isDangling whether the given {@link Edge edge} is considered to be dangling
	 * @return Returns whether the given {@link Edge edge} is part of the {@link View}.
	 */
	public boolean contains(Edge edge, boolean isDangling) {
		return isDangling ? graph.getEdges().contains(edge) : graph.getEdges().contains(edge) && contains(edge.getSource()) && contains(edge.getTarget());
	}
	
	/**
	 * Checks if the given {@link EObject eObject} is part of the {@link View}.
	 * @param eObject an {@link EObject} form the {@link View#resource resource} to check the containment of
	 * @return Returns whether the given {@link EObject eObject} is part of the {@link View}.
	 */
	public boolean contains(EObject eObject) {
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
	public boolean contains(EObject sourceEObject, EObject targetEObject, EReference eReference, boolean isDangling) {
		if (sourceEObject == targetEObject) return false;
		if(!graphMap.containsKey(sourceEObject) || !graphMap.containsKey(targetEObject)) return false;
		Node sourceNode = graphMap.get(sourceEObject);
		Node targetNode = graphMap.get(targetEObject);
		Edge foundEdge = null;
		
		for (Edge edge : graph.getEdges()) {
			if(edge.getSource() == sourceNode && edge.getTarget() == targetNode) {
				if (edge.getType() == eReference || edge.getType().getEOpposite() == eReference) {
					foundEdge = edge;
					break;
				}
			}
			
			if(edge.getSource() == targetNode && edge.getTarget() == sourceNode) {
				if (edge.getType() == eReference || edge.getType().getEOpposite() == eReference) {
					foundEdge = edge;
					break;
				}
			}
		}
		
		if (foundEdge == null) return false;
		
		return contains(foundEdge, isDangling);
	}

	@Override
	public String toString() {
		return "View [" + (resource != null ? "resource=" + resource.hashCode() + ", " : "")
				+ (graph != null ? "graph=" + graph.getNodes().toString() + graph.getEdges().toString() + ", " : "") + "]";
	}
	
	@Override
	public int hashCode () {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((graph == null) ? 0 : graph.hashCode());
		result = prime * result + ((graphMap == null) ? 0 : graphMap.hashCode());
		result = prime * result + ((objectMap == null) ? 0 : objectMap.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		return result;
		
	}

	@Override
	public boolean equals (Object obj) {
		
		if (this == obj)
			return true;
		if (!(obj instanceof View))
			return false;
		
		View view = (View) obj;
		if (!view.resource.equals(resource)) return false;
		
		if(graph.getNodes().size() != view.graph.getNodes().size()) return false;
		boolean nodesAreEqual = graph.getNodes().stream().
				map(this::getObject).
				collect(Collectors.toSet())
			.equals(
				view.graph.getNodes().stream().
				map(view::getObject).
				collect(Collectors.toSet())
			);
		
		if(!nodesAreEqual) return false;
		
		if(graph.getEdges().size() != view.graph.getEdges().size()) return false;
		
		Function<? super Edge, ? extends HashSet<EObject>> edgeToEObjectSetThisView = edge -> {
			HashSet<EObject> hashSet = new HashSet<EObject>();
			hashSet.add(objectMap.get(edge.getSource()));
			hashSet.add(objectMap.get(edge.getTarget()));
			return hashSet;
		};
		
		Function<? super Edge, ? extends HashSet<EObject>> edgeToEObjectSetThatView = edge -> {
			HashSet<EObject> hashSet = new HashSet<EObject>();
			hashSet.add(view.objectMap.get(edge.getSource()));
			hashSet.add(view.objectMap.get(edge.getTarget()));
			return hashSet;
		};
		
		boolean edgesAreEqual = graph.getEdges().stream().
					map(edgeToEObjectSetThisView).collect(Collectors.toSet()).
				equals(
					view.graph.getEdges().stream().
					map(edgeToEObjectSetThatView).collect(Collectors.toSet())
				);
		
		if(!edgesAreEqual) return false;
		
		List<Node> nodesOfThisView = graph.getNodes();
		List<Node> nodesOfThatView = view.graph.getNodes();
		
		for (Edge edge : graph.getEdges()) {
			
			EObject sourceEObject = objectMap.get(edge.getSource());
			EObject targetEObject = objectMap.get(edge.getTarget());
			
			if(nodesOfThisView.contains(edge.getSource()) != nodesOfThatView.contains(view.graphMap.get(sourceEObject))) return false;
			if(nodesOfThisView.contains(edge.getTarget()) != nodesOfThatView.contains(view.graphMap.get(targetEObject))) return false;
			
		}
		
		return nodesAreEqual & edgesAreEqual;
		
	}

}
