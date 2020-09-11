package view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.Graph;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.impl.GraphImpl;

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
	
	final Resource resource;
	Graph graph;
	Map<EObject, Node> graphMap;
	Map<Node, EObject> objectMap;
	
	/**
	 * Creates a new empty {@link View}.
	 * @param resource the {@code resource} to be used
	 */
	public View(Resource resource) {
		super();
		this.resource = resource;
		graphMap = new HashMap<EObject, Node>();
		objectMap = new HashMap<Node, EObject>();
		graph = new GraphImpl();
	}

	/**
	 * @return Returns the {@code resource}.
	 */
	Resource getResource() {
		return resource;
	}
	
	/**
	 * @param eObject an {@link EObject} form the {@code resource}
	 * @return Returns the {@link Node} mapped to the given {@link EObject} or {@code null} if the {@link EObject} is not part of the {@link View}.
	 */
	public Node getNode(EObject eObject) {
		return graphMap.get(eObject);
	}
	
	/**
	 * @return Returns a random {@link Node} form the {@code graph} or {@code null} if the {@code graph} is empty.
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
	 * @param node an {@link Node} from the {@code graph}
	 * @return Returns the {@link EObject} mapped to the given {@link Node} or {@code null} if the {@link Node} is not part of the {@link View}s {@code graph}.
	 */
	public EObject getObject(Node node) {
		return objectMap.get(node);
	}
	
	/**
	 * @return Returns {@code ture} if the {@link View} represents no elements, {@code false} otherwise.
	 */
	public boolean isEmpty() {
		return graph.getNodes().isEmpty() && graph.getEdges().isEmpty();
	}
	
	public boolean extend(EClass eClass) {
		return false;	
	}
	
	public boolean extend(EReference eReference) {
		return false;
	}
	
	public boolean extend(EObject eObject) {
		return false;
	}
	
	public boolean extend(EObject eObject, EReference eReference) {
		return false;
	}
	
	public boolean reduce(EClass eClass) {
		return false;
	}
	
	public boolean reduce(EReference eReference) {
		return false;
	}
	
	public boolean reduce(EObject eObject) {
		return false;
	}
	
	public boolean reduce(EObject eObject, EReference eReference) {
		return false;
	}
	
	/**
	 * @return Returns a copy of this view containing separate Maps and a 
	 * separate {@link Graph} but the same {@link Node} and {@link Edge} objects.
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
	 * Reduces the view by any elements not part of the given {@code metamodel} and extends it by all that are.
	 * @param metamodel the meta-model or sub-meta-model of the {@code resource}
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
