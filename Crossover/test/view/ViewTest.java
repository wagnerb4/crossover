/**
 * 
 */
package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.impl.GraphImpl;
import org.eclipse.emf.henshin.model.impl.NodeImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Benjamin Wagner
 *
 */
class ViewTest extends TestResources {
	
	// begin test methods
	
	/**
	 * Test method for {@link view.View#getNode(org.eclipse.emf.ecore.EObject)}.
	 */
	@Test
	final void testGetNode () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		TreeIterator<EObject> treeIterator = SCRUM_PLANNIG_INSTANCE_ONE.getAllContents();
		
		EObject eObjectOne = treeIterator.next();
		viewOnScrumPlanningInstanceOne.extend(eObjectOne);
		List<Node> nodes = viewOnScrumPlanningInstanceOne.graph.getNodes();
		
		assertEquals(1, nodes.size());
		assertEquals(nodes.get(0), viewOnScrumPlanningInstanceOne.getNode(eObjectOne));
		
		EObject eObjectTwo = treeIterator.next();
		assertNull(viewOnScrumPlanningInstanceOne.getNode(eObjectTwo));
		
	}

	/**
	 * Test method for {@link view.View#getRandomNode()}.
	 */
	@Test
	final void testGetRandomNode () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// should be null as the view is empty
		assertNull(viewOnScrumPlanningInstanceOne.getRandomNode());
		
		// add dangling Edge and see if it is still null
		EObject plan = SCRUM_PLANNIG_INSTANCE_ONE.getContents().get(0);
		List<EReference> references = plan.eClass().getEAllReferences().stream().
				filter(reference -> reference.getName().equals("backlog")).
				collect(Collectors.toList());
		
		assertEquals(1, references.size());
		
		EObject backlog = (EObject) plan.eGet(references.get(0));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(plan, backlog, references.get(0)));
		
		assertNull(viewOnScrumPlanningInstanceOne.getRandomNode());
		
		// complete dangling edges and execute enough times so that all two nodes have a good probability of being returned
		viewOnScrumPlanningInstanceOne.completeDangling();
		
		Set<Node> nodeSetActual = new HashSet<>();
		nodeSetActual.addAll(viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(2, nodeSetActual.size());
		
		Set<Node> nodeSetReturned = new HashSet<>();
		
		for (int i = 0; i < 10; i++) {
			nodeSetReturned.add(viewOnScrumPlanningInstanceOne.getRandomNode());
		}
		
		// is only true with a high probability
		assertEquals(nodeSetReturned, nodeSetActual);
		
	}

	/**
	 * Test method for {@link view.View#getObject(org.eclipse.emf.henshin.model.Node)}.
	 */
	@Test
	final void testGetObject () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		assertNull(viewOnScrumPlanningInstanceOne.getObject(new NodeImpl()));
		
		// add plan object and test
		EObject plan = SCRUM_PLANNIG_INSTANCE_ONE.getContents().get(0);
		viewOnScrumPlanningInstanceOne.extend(plan);
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		Node nodeForPlan = viewOnScrumPlanningInstanceOne.graph.getNodes().get(0);
		assertEquals(plan, viewOnScrumPlanningInstanceOne.getObject(nodeForPlan));
		
		// add backlog and edge between backlog and plan
		List<EReference> references = plan.eClass().getEAllReferences().stream().
				filter(reference -> reference.getName().equals("backlog")).
				collect(Collectors.toList());
		
		assertEquals(1, references.size());
		
		EObject backlog = (EObject) plan.eGet(references.get(0));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(plan, backlog, references.get(0)));
		
		// get the node for the backlog and test again
		assertEquals(1, nodeForPlan.getOutgoing().size());
		Node nodeForBacklog = nodeForPlan.getOutgoing().get(0).getTarget();
		
		assertEquals(backlog, viewOnScrumPlanningInstanceOne.getObject(nodeForBacklog));
		
	}

	/**
	 * Test method for {@link view.View#isEmpty()}.
	 */
	@Test
	final void testIsEmpty () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		viewOnScrumPlanningInstanceOne.extend(SCRUM_PLANNIG_INSTANCE_ONE.getContents().get(0));
		assertFalse(viewOnScrumPlanningInstanceOne.isEmpty());
		
	}

	// extend(EClass)
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EClass)} with 
	 * an {@link EClass eClass} of an different model.
	 */
	@Test
	final void testExtendEClassDifferentModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass attributeEClass = getEClassFromResource(CRA_ECORE, "Attribute")[0];
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testExtendEClassDifferentModelEmptyView (viewOnScrumPlanningInstanceOne, attributeEClass);
		
		testExtendEClassDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, attributeEClass, backlogWorkitemsEReference, backlogEClass);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}
	 * with an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eClass an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassDifferentModelEmptyView (View view, EClass eClass) {
		
		assertFalse(view.extend(eClass));
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View} with
	 * an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eClass an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReferenceFromModel an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassFromModel an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassDifferentModelNonEmptyView (View viewOnScrumPlanningInstanceOne, EClass eClass, EReference eReferenceFromModel, EClass eClassFromModel) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReferenceFromModel));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClassFromModel));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eClass));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EClass)} with
	 * {@link EClass eClasses} from the same model.
	 */
	@Test
	final void testExtendEClassSameModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the stakeholder and the backlog eClasses from the meta-model
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog");
		EClass stakeholder = eClasses[0];
		
		testExtendEClassSameModelEmptyView (viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, stakeholder);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEClassSameModelEmptyViewTwice (viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, stakeholder);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEClassSameModelEmptyView (viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, eClasses);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEClassSameModelEmptyViewTwice (viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, eClasses);
		viewOnScrumPlanningInstanceOne.clear();
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}.
	 * @param eClasses an array of {@link EClass eClasses} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassSameModelEmptyView (View view, Resource model, EClass... eClasses) {
		
		List<EClass> listOfEClasses = List.of(eClasses);
		
		for (EClass eClass : listOfEClasses) {
			assertTrue(view.extend(eClass));
		}
		
		Set<EObject> expedtedEObjects = getEObjectsFromResource(model, eObject -> listOfEClasses.contains(eObject.eClass())).get(0);
		
		assertEquals(expedtedEObjects, view.graphMap.keySet());
		assertEquals(expedtedEObjects, new HashSet<>(view.objectMap.values()));
		assertEquals(expedtedEObjects, view.graph.getNodes().stream().map(view::getObject).collect(Collectors.toSet()));
		assertTrue(view.graph.getEdges().isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}
	 * with the same parameter twice. The second method call should not work and nothing should change.
	 * @param eClass an {@link EClass eClass} of the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassSameModelEmptyViewTwice (View view, Resource model, EClass... eClasses) {
		
		testExtendEClassSameModelEmptyView (view, SCRUM_PLANNIG_INSTANCE_ONE, eClasses);
		
		List<EClass> listOfEClasses = List.of(eClasses);
		
		for (EClass eClass : listOfEClasses) {
			assertFalse(view.extend(eClass));
		}
		
		
		Set<EObject> expedtedEObjects = getEObjectsFromResource(model, eObject -> listOfEClasses.contains(eObject.eClass())).get(0);
		
		assertEquals(expedtedEObjects, view.graphMap.keySet());
		assertEquals(expedtedEObjects, new HashSet<>(view.objectMap.values()));
		assertEquals(expedtedEObjects, view.graph.getNodes().stream().map(view::getObject).collect(Collectors.toSet()));
		assertTrue(view.graph.getEdges().isEmpty());
		
	}

	// extend(EReference)
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EReference)} with
	 * an {@link EReference eReference} form a different model.
	 */
	@Test
	final void testExtendEReferenceDifferentModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass methodEClass = getEClassFromResource(CRA_ECORE, "Method")[0];
		EReference dataDependencyEReference = getEReferenceFromEClass(methodEClass, "dataDependency");
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testExtendEReferenceDifferentModelEmptyView (viewOnScrumPlanningInstanceOne, dataDependencyEReference);
		
		testExtendEReferenceDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, dataDependencyEReference, backlogWorkitemsEReference, backlogEClass);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method on an empty {@link View}
	 * with an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eReference an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEReferenceDifferentModelEmptyView (View view, EReference eReference) {
		
		assertFalse(view.extend(eReference));
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method on an empty {@link View} with
	 * an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eReference an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReferenceFromModel an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassFromModel an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEReferenceDifferentModelNonEmptyView (View view, EReference eReference, EReference eReferenceFromModel, EClass eClassFromModel) {
		
		assertTrue(view.extend(eReferenceFromModel));
		assertTrue(view.extend(eClassFromModel));
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.extend(eReference));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EReference)} with
	 * {@link EReference eReferences} form a the same model.
	 */
	@Test
	final void testExtendEReferenceSameModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the expected nodes and edges
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("Stakeholder"));
		EObject backlog = mapOfSets.get(0).iterator().next();
		Set<EObject> stakeholderEObjects = mapOfSets.get(1);
		assertEquals(2, stakeholderEObjects.size());
		
		// get the workitems EReference from the meta-model
		// it needs to be fetched using the backlog as the name workitems is not an unique identifier
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlog.eClass(), "workitems");
		
		testExtendEReferenceSameModelEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodes(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, backlogWorkitemsEReference, backlog);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodes(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, backlogWorkitemsEReference, stakeholderEObjects.toArray(new EObject[] {}));
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewEmptyViewTwice(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodesTwice(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, backlogWorkitemsEReference, backlog);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodesTwice(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, backlogWorkitemsEReference, stakeholderEObjects.toArray(new EObject[] {}));
		
	}

	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with some of the nodes already inserted.
	 * @param eReference an {@link EReference eReference} from the SCRUM_PLANNIG_ECORE meta-model to extend by
	 * @param eObjects an array of {@link EObject eObjects} form the SCRUM_PLANNIG_INSTANCE_ONE to fill the {@link View} with
	 */
	private void testExtendEReferenceSameModelViewWithNodes(View view, Resource model, EReference eReference, EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertTrue(view.extend(eObject));
		}
		
		// extend by the reference
		assertTrue(view.extend(eReference));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjects),
				view.graph.getNodes().stream().
				map(view::getObject).
				collect(Collectors.toSet())
		);
		
		// test nodes from the maps
		Set<EObject> allExpectedEObjects = getEObjectsFromResource(model, eObject -> {
			boolean eObjectIsOfContainingEClass = eReference.getEContainingClass() == eObject.eClass();
			boolean eObjectIsOfReferencedType = eReference.getEReferenceType() == eObject.eClass();
			return eObjectIsOfContainingEClass | eObjectIsOfReferencedType;
		}).get(0);
		
		allExpectedEObjects.addAll(List.of(eObjects));
		
		Set<EObject> actualEObjectsGraphMap = view.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(view.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(model, eObject -> {
			
			boolean eClassOfeObjectContainsTheReference = eObject.eClass().getEAllReferences().contains(eReference);
			
			if (eClassOfeObjectContainsTheReference) {
				return eObject.eGet(eReference) != null;
			} else {
				return false;
			}
			
		}).get(0);
		
		Set<EObject> expectedReferencedEObjects = new HashSet<EObject>();
		
		for (EObject eObject : expectedEObjectsWithTheEReference) {
			Object referenced = eObject.eGet(eReference);
			if (referenced instanceof EObject) {
				expectedReferencedEObjects.add((EObject) referenced);
			} else if (referenced instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<EObject> listOfEObjects = (List<EObject>) referenced;
				expectedReferencedEObjects.addAll(listOfEObjects);
			}
		}
		
		Set<EObject> actualSourceEObjects = new HashSet<EObject>();
		Set<EObject> actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : view.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(view.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(view.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with some of the nodes already inserted
	 * appying the same parameter twice. The second time shoulnd't work and nothing should change.
	 * @param eReference an {@link EReference eReference} from the SCRUM_PLANNIG_ECORE meta-model to extend by
	 * @param eObjects an array of {@link EObject eObjects} form the SCRUM_PLANNIG_INSTANCE_ONE to fill the {@link View} with
	 */
	private void testExtendEReferenceSameModelViewWithNodesTwice (View view, Resource model, EReference eReference, EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertTrue(view.extend(eObject));
		}
		
		// extend by the reference
		assertTrue(view.extend(eReference));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjects),
				view.graph.getNodes().stream().
				map(view::getObject).
				collect(Collectors.toSet())
		);
		
		// test nodes from the maps
		Set<EObject> allExpectedEObjects = getEObjectsFromResource(model, eObject -> {
			boolean eObjectIsOfContainingEClass = eReference.getEContainingClass() == eObject.eClass();
			boolean eObjectIsOfReferencedType = eReference.getEReferenceType() == eObject.eClass();
			return eObjectIsOfContainingEClass | eObjectIsOfReferencedType;
		}).get(0);
		
		allExpectedEObjects.addAll(List.of(eObjects));
		
		Set<EObject> actualEObjectsGraphMap = view.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(view.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(model, eObject -> {
			
			boolean eClassOfeObjectContainsTheReference = eObject.eClass().getEAllReferences().contains(eReference);
			
			if (eClassOfeObjectContainsTheReference) {
				return eObject.eGet(eReference) != null;
			} else {
				return false;
			}
			
		}).get(0);
		
		Set<EObject> expectedReferencedEObjects = new HashSet<EObject>();
		
		for (EObject eObject : expectedEObjectsWithTheEReference) {
			Object referenced = eObject.eGet(eReference);
			if (referenced instanceof EObject) {
				expectedReferencedEObjects.add((EObject) referenced);
			} else if (referenced instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<EObject> listOfEObjects = (List<EObject>) referenced;
				expectedReferencedEObjects.addAll(listOfEObjects);
			}
		}
		
		Set<EObject> actualSourceEObjects = new HashSet<EObject>();
		Set<EObject> actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : view.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(view.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(view.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
		// the second time
		assertFalse(view.extend(eReference));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjects),
				view.graph.getNodes().stream().
				map(view::getObject).
				collect(Collectors.toSet())
		);
		
		// test nodes from the maps
		actualEObjectsGraphMap = view.graphMap.keySet();
		actualEObjectsObjectMap = new HashSet<>(view.objectMap.values());
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		actualSourceEObjects = new HashSet<EObject>();
		actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : view.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(view.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(view.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
	}
	
	/**
     * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with an an empty {@link View}.
     * No {@link Node nodes} should be added to the {@link view.View#graph graph} by {@link view.View#extend(org.eclipse.emf.ecore.EReference)}.
	 * @param eReference the {@link EReference eReference} from the SCRUM_PLANNIG_ECORE meta-model used to extend the {@link View}.
	 */
	private void testExtendEReferenceSameModelEmptyView (View view, Resource model, EReference eReference) {
		
		assertTrue(view.extend(eReference));
		
		// test nodes from the graph
		assertEquals(0, view.graph.getNodes().size());
		
		// test nodes from the maps
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(model, eObject -> {
			
			boolean eClassOfeObjectContainsTheReference = eObject.eClass().getEAllReferences().contains(eReference);
			
			if (eClassOfeObjectContainsTheReference) {
				return eObject.eGet(eReference) != null;
			} else {
				return false;
			}
			
		}).get(0);
		
		Set<EObject> expectedReferencedEObjects = new HashSet<EObject>();
		
		for (EObject eObject : expectedEObjectsWithTheEReference) {
			Object referenced = eObject.eGet(eReference);
			if (referenced instanceof EObject) {
				expectedReferencedEObjects.add((EObject) referenced);
			} else if (referenced instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<EObject> listOfEObjects = (List<EObject>) referenced;
				expectedReferencedEObjects.addAll(listOfEObjects);
			}
		}
		
		Set<EObject> allExpectedEObjects = new HashSet<>(expectedEObjectsWithTheEReference);
		allExpectedEObjects.addAll(expectedReferencedEObjects);
		
		Set<EObject> actualEObjectsGraphMap = view.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(view.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> actualSourceEObjects = new HashSet<EObject>();
		Set<EObject> actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : view.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(view.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(view.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
	}
	
	/**
     * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with an an empty {@link View} adding
     * the same parameter twice the second time shouldn't work and nothing should change.
	 * @param eReference the {@link EReference eReference} from the SCRUM_PLANNIG_ECORE meta-model used to extend the {@link View}.
	 */
	private void testExtendEReferenceSameModelViewEmptyViewTwice (View view, Resource model, EReference eReference) {
		
		assertTrue(view.extend(eReference));
		
		// test nodes from the graph
		assertEquals(0, view.graph.getNodes().size());
		
		// test nodes from the maps
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(model, eObject -> {
			
			boolean eClassOfeObjectContainsTheReference = eObject.eClass().getEAllReferences().contains(eReference);
			
			if (eClassOfeObjectContainsTheReference) {
				return eObject.eGet(eReference) != null;
			} else {
				return false;
			}
			
		}).get(0);
		
		Set<EObject> expectedReferencedEObjects = new HashSet<EObject>();
		
		for (EObject eObject : expectedEObjectsWithTheEReference) {
			Object referenced = eObject.eGet(eReference);
			if (referenced instanceof EObject) {
				expectedReferencedEObjects.add((EObject) referenced);
			} else if (referenced instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<EObject> listOfEObjects = (List<EObject>) referenced;
				expectedReferencedEObjects.addAll(listOfEObjects);
			}
		}
		
		Set<EObject> allExpectedEObjects = new HashSet<>(expectedEObjectsWithTheEReference);
		allExpectedEObjects.addAll(expectedReferencedEObjects);
		
		Set<EObject> actualEObjectsGraphMap = view.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(view.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> actualSourceEObjects = new HashSet<EObject>();
		Set<EObject> actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : view.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(view.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(view.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
		// add  the second time
		assertFalse(view.extend(eReference));
		
		// test nodes from the graph
		assertEquals(0, view.graph.getNodes().size());
		
		// test nodes from the maps
		actualEObjectsGraphMap = view.graphMap.keySet();
		actualEObjectsObjectMap = new HashSet<>(view.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		actualSourceEObjects = new HashSet<EObject>();
		actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : view.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(view.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(view.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
	}
	
	// extend(EObject)
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject)}  with
	 * an {@link EObject eObject} form a different model.
	 */
	@Test
	final void testExtendEObjectDifferentModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EClass planEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		EObject[] methodEObjects = getEObjectsFromResource (
				CRA_INSTANCE_ONE, eObject -> eObject.eClass().getName().equals("Method")
				).get(0).toArray(new EObject[] {});
		
		testExtendEObjectDifferentModelEmptyView (viewOnScrumPlanningInstanceOne, methodEObjects);
		
		testExtendEObjectDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, backlogEClass, backlogWorkitemsEReference, methodEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, methodEObjects);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an empty {@link View}
	 * with {@link EObject eObjects} that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model.
	 * @param eObjects an array of {@link EObject eObjects} that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectDifferentModelEmptyView (View view, EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertFalse(view.extend(eObject));
		}
		
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an {@link View} extended 
	 * by the given {@link EClass eClass} and {@link EReference eReference} with {@link EObject eObjects}
	 * that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model.
	 * @param eClass an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReference an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObjects an array of {@link EObject eObjects} that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectDifferentModelNonEmptyView (View view, EClass eClass, EReference eReference, EObject... eObjects) {
		
		assertTrue(view.extend(eClass));
		assertTrue(view.extend(eReference));
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		for (EObject eObject : eObjects) {
			assertFalse(view.extend(eObject));
		}
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject)} with
	 * {@link EObject eObjects} form the same model.
	 */
	@Test
	final void testExtendEObjectSameModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("Stakeholder"));
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		EObject[] stakeholderEObjects = mapOfSets.get(1).toArray(new EObject[] {});
		EObject[] stakeholderAndBacklogEObjects = new EObject[] {backlogEObject, stakeholderEObjects[0], stakeholderEObjects[1]};
		
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EClass planEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testExtendEObjectSameModelEmptyView (viewOnScrumPlanningInstanceOne, backlogEObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyView (viewOnScrumPlanningInstanceOne, stakeholderEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyView (viewOnScrumPlanningInstanceOne, stakeholderAndBacklogEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyViewTwice (viewOnScrumPlanningInstanceOne, backlogEObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyViewTwice (viewOnScrumPlanningInstanceOne, stakeholderEObjects[0]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (viewOnScrumPlanningInstanceOne, backlogEClass, backlogWorkitemsEReference, stakeholderEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, stakeholderEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, backlogEObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, stakeholderAndBacklogEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyViewTwice (viewOnScrumPlanningInstanceOne, backlogEClass, backlogWorkitemsEReference, stakeholderEObjects[0]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyViewTwice (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, stakeholderEObjects[0]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyViewTwice (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, backlogEObject);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an empty {@link View}
	 * with {@link EObject eObjects} that are from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model.
	 * @param eObjects an array of {@link EObject eObjects} that are from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectSameModelEmptyView (View view, EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertTrue(view.extend(eObject));
		}
		
		// nodes from the graph
		Set<EObject> actualEObjectsFromGraph = view.graph.getNodes().
				stream().map(view::getObject).
				collect(Collectors.toSet());
		
		Set<EObject> expectedEObjects = new HashSet<>(List.of(eObjects));
		
		assertEquals(expectedEObjects, actualEObjectsFromGraph);
		
		// nodes from the maps
		assertEquals(expectedEObjects, view.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(view.objectMap.values()));
		
		// edges
		assertTrue(view.graph.getEdges().isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an empty {@link View}
	 * with an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model twice. The second time
	 * shouldn't work and nothing should change.
	 * @param eObject an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectSameModelEmptyViewTwice (View view, EObject eObject) {
		
		testExtendEObjectSameModelEmptyView(view, eObject);
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		// add the same eObject again
		assertFalse(view.extend(eObject));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on a {@link View} extended 
	 * by the given {@link EClass eClass} and {@link EReference eReference} with {@link EObject eObjects}
	 * that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model with {@link EObject eObjects} that are also 
	 * from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model.
	 * @param eClass an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReference an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObjects an array of {@link EObject eObjects} that are from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectSameModelNonEmptyView (View view, EClass eClass, EReference eReference, EObject... eObjects) {
		
		assertTrue(view.extend(eClass));
		assertTrue(view.extend(eReference));
		
		Set<EObject> expectedEObjectsFromGraph = view.graph.getNodes().
				stream().map(view::getObject).
				collect(Collectors.toSet());
		List<Edge> expectedEdges = new ArrayList<Edge> (view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		List<EObject> listOfEObjects = List.of(eObjects);
		expectedEObjectsFromGraph.addAll(listOfEObjects);
		expectedGraphMapSet.addAll(listOfEObjects);
		expectedObjectMapSet.addAll(listOfEObjects);
		
		for (EObject eObject : eObjects) {
			assertTrue(view.extend(eObject));
		}
		
		// nodes from the graph
		Set<EObject> actualEObjectsFromGraph = view.graph.getNodes().
				stream().map(view::getObject).
				collect(Collectors.toSet());
		
		assertEquals(expectedEObjectsFromGraph, actualEObjectsFromGraph);
		
		// nodes from the maps
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
		// edges
		assertEquals(expectedEdges, view.graph.getEdges());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on a {@link View} extended 
	 * by the given {@link EClass eClass} and {@link EReference eReference} with an {@link EObject eObject} 
	 * twice. The second time shouldn't work and nothing should change.
	 * @param eClass an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReference an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObject an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectSameModelNonEmptyViewTwice (View view, EClass eClass, EReference eReference, EObject eObject) {
		
		testExtendEObjectSameModelNonEmptyView(view, eClass, eReference, eObject);
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		// add the same eObject again
		assertFalse(view.extend(eObject));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	// extend(EObject, EObject, EReference)
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 */
	@Test
	final void testExtendEObjectEObjectEReferenceSameModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		
		EObject[] workitemEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		
		EObject[] stakeholderEObjects = mapOfSets.get(2).toArray(new EObject[0]);
		EObject stakeholderOne = stakeholderEObjects[0];
		EObject stakeholderTwo = stakeholderEObjects[1];
		
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholderOne.eClass(), "workitems");
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEObject.eClass(), "workitems");
		
		// get referenced workitems of stakeholder one
		Object object = stakeholderOne.eGet(stakeholderWorkitemsEReference);
		assertTrue(object instanceof EList<?>);
		@SuppressWarnings("unchecked")
		EList<EObject> workitemsOfStakeholderOne = (EList<EObject>) object;
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, backlogEObject, stakeholderTwo, stakeholderWorkitemsEReference);
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, workitemsOfStakeholderOne.get(0), stakeholderOne, stakeholderWorkitemsEReference);
		
		testExtendEObjectEObjectEReferenceSameModelEmptyView (viewOnScrumPlanningInstanceOne, new EObject[] {backlogEObject}, 
				new EObject[] {workitemEObjects[0]}, new EReference[] {backlogWorkitemsEReference});
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelEmptyViewTwice (viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[0], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelEmptyView (
				viewOnScrumPlanningInstanceOne, 
				new EObject[] {backlogEObject, backlogEObject, backlogEObject, backlogEObject}, 
				workitemEObjects, new EReference[] {backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference}
		);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyView (viewOnScrumPlanningInstanceOne, new EObject[] {backlogEObject}, 
				new EObject[] {workitemEObjects[0]}, new EReference[] {backlogWorkitemsEReference}, new EObject[] {backlogEObject, workitemEObjects[0]});
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyViewTwice(viewOnScrumPlanningInstanceOne, backlogEObject, 
				workitemEObjects[0], backlogWorkitemsEReference, new EObject[] {backlogEObject, workitemEObjects[0]});
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyView (
				viewOnScrumPlanningInstanceOne, 
				new EObject[] {backlogEObject, backlogEObject, backlogEObject, backlogEObject}, 
				workitemEObjects,
				new EReference[] {backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference}, new EObject[] {backlogEObject, workitemEObjects[0]}
		);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters from the same model that match the required conditions on an empty {@link View}. The size of all arrays
	 * are expected to be equal.
	 * @param eObjectsOne an array of {@link EObject eObjects}
	 * @param eObjectsTwo an array of {@link EObject eObjects}
	 * @param eReferences an array of {@link EReference eReferences}
	 */
	private  void testExtendEObjectEObjectEReferenceSameModelEmptyView (View view, EObject[] eObjectsOne, EObject[] eObjectsTwo, EReference[] eReferences) {
		
		assertTrue(eObjectsOne.length == eObjectsTwo.length && eObjectsTwo.length == eReferences.length);
		
		for (int i = 0; i < eReferences.length; i++) {
			assertTrue(view.extend(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		// test nodes from graph
		assertTrue(view.graph.getNodes().isEmpty());
		
		// test maps
		Set<EObject> expectedEObjects = new HashSet<EObject>(List.of(eObjectsOne));
		expectedEObjects.addAll(List.of(eObjectsTwo));
		assertEquals(expectedEObjects, view.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(view.objectMap.values()));
		
		// test edges from graph
		List<Set<EObject>> listOfEObjectSets = new ArrayList<Set<EObject>>();
		
		for (int i = 0; i < eObjectsOne.length; i++) {
			listOfEObjectSets.add(Set.of(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		Set<EObject> actualEdgeNodeEObjects = new HashSet<>();
		
		for (Edge edge : view.graph.getEdges()) {
			
			EObject sourceEObject = view.objectMap.get(edge.getSource());
			EObject targetEObject = view.objectMap.get(edge.getTarget());
			
			assertTrue(listOfEObjectSets.contains(Set.of(sourceEObject, targetEObject, edge.getType())));
			
			actualEdgeNodeEObjects.add(sourceEObject);
			actualEdgeNodeEObjects.add(targetEObject);
			
		}
		
		assertEquals(expectedEObjects, actualEdgeNodeEObjects);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters from the same model that match the required conditions on an {@link View} extended by the given {@link EObject eObjectsThree}.
	 * The size of all arrays are expected to be equal.
	 * @param eObjectsOne an array of {@link EObject eObjects}
	 * @param eObjectsTwo an array of {@link EObject eObjects}
	 * @param eReferences an array of {@link EReference eReferences}
	 * @param eObjectsThree an array of {@link EObject eObjects}
	 */
	private  void testExtendEObjectEObjectEReferenceSameModelNonEmptyView (View view, EObject[] eObjectsOne, EObject[] eObjectsTwo, EReference[] eReferences, EObject[] eObjectsThree) {
		
		assertTrue(eObjectsOne.length == eObjectsTwo.length && eObjectsTwo.length == eReferences.length);
		
		for (int i = 0; i < eObjectsThree.length; i++) {
			assertTrue(view.extend(eObjectsThree[i]));
		}
		
		
		for (int i = 0; i < eReferences.length; i++) {
			assertTrue(view.extend(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		// test maps
		Set<EObject> expectedEObjects = new HashSet<EObject>(List.of(eObjectsOne));
		expectedEObjects.addAll(List.of(eObjectsTwo));
		expectedEObjects.addAll(List.of(eObjectsThree));
		assertEquals(expectedEObjects, view.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(view.objectMap.values()));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjectsThree),
				view.graph.getNodes().stream().
				map(view.objectMap::get).
				collect(Collectors.toSet())
		);
		

		// test edges from graph
		List<Set<EObject>> listOfEObjectSets = new ArrayList<Set<EObject>>();
		
		for (int i = 0; i < eObjectsOne.length; i++) {
			listOfEObjectSets.add(Set.of(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		Set<EObject> actualEdgeNodeEObjects = new HashSet<>();
		
		for (Edge edge : view.graph.getEdges()) {
			
			EObject sourceEObject = view.objectMap.get(edge.getSource());
			EObject targetEObject = view.objectMap.get(edge.getTarget());
			
			assertTrue(listOfEObjectSets.contains(Set.of(sourceEObject, targetEObject, edge.getType())));
			
			actualEdgeNodeEObjects.add(sourceEObject);
			actualEdgeNodeEObjects.add(targetEObject);
			
		}
		
		assertEquals(expectedEObjects, actualEdgeNodeEObjects);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters from the same model that match the required conditions on an {@link View} extended by the given {@link EObject eObjectsThree}.
	 * Inserting the same parameters twice shouln't work and nothing should change.
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReference an {@link EReference eReference}
	 * @param eObjectsThree an array of {@link EObject eObjects}
	 */
	private  void testExtendEObjectEObjectEReferenceSameModelNonEmptyViewTwice (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference, EObject[] eObjectsThree) {
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyView (view, new EObject[] {eObjectOne}, new EObject[] {eObjectTwo}, new EReference[] {eReference}, eObjectsThree);
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.extend(eObjectOne, eObjectTwo, eReference));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters from the same model that match the required conditions on an empty {@link View}.
	 * Inserting the same parameters twice shouln't work and nothing should change.
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReference an {@link EReference eReference}
	 */
	private  void testExtendEObjectEObjectEReferenceSameModelEmptyViewTwice (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		testExtendEObjectEObjectEReferenceSameModelEmptyView (view, new EObject[] {eObjectOne}, new EObject[] {eObjectTwo}, new EReference[] {eReference});
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.extend(eObjectOne, eObjectTwo, eReference));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * with input parameters from a different model. 
	 */
	@Test
	final public void testExtendEObjectEObjectEReferenceDifferentModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("2"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("3"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("5"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("8")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject method3 = mapOfSets.get(1).iterator().next();
		EObject attribute5 = mapOfSets.get(2).iterator().next();
		EObject class8 = mapOfSets.get(3).iterator().next();
		
		// get SCRUM PLANNING meta-model elements
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EClass planEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, method3, attribute5, encapsulates);
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, class8, method2, encapsulates);
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, backlogEClass, backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, backlogEClass, backlogWorkitemsEReference, class8, method2, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (viewOnScrumPlanningInstanceOne, planEClass, backlogWorkitemsEReference, class8, method2, encapsulates);

	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters that don't match the required conditions on an empty {@link View}.
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReference an {@link EReference eReference}
	 */
	private void testExtendEObjectEObjectEReferenceEmptyViewWrongInput (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
	
		assertFalse(view.extend(eObjectOne, eObjectTwo, eReference));
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters that don't match the required conditions on an {@link View}
	 * that is extended by the given {@link EClass eClass} and {@link EReference eReferenceOne}.
	 * @param eClass an {@link EClass eClass} that is from the {@code SCRUM_PLANNING_ECORE} meta-model
	 * @param eReferenceOne an {@link EReference eReference} that is from the {@code SCRUM_PLANNING_ECORE} meta-model
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReferenceTwo an {@link EReference eReference}
	 */
	private void testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (View view, EClass eClass,
			EReference eReferenceOne, EObject eObjectOne, EObject eObjectTwo, EReference eReferenceTwo) {
	
		assertTrue(view.extend(eClass));
		assertTrue(view.extend(eReferenceOne));
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.extend(eObjectOne, eObjectTwo, eReferenceTwo));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	// reduce(EClass)
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} with
	 * an {@link EClass eClass} form a different model.
	 */
	@Test
	final void testReduceEClassDifferentModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass attributeEClass = getEClassFromResource(CRA_ECORE, "Attribute")[0];
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testReduceEClassEmptyView (viewOnScrumPlanningInstanceOne, attributeEClass);
		
		testReduceEClassDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, attributeEClass, backlogEClass, backlogWorkitemsEReference);
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}.
	 * @param eClass an {@link EClass eClass}
	 */
	private void testReduceEClassEmptyView (View view, EClass eClass) {
		
		assertFalse(view.reduce(eClass));
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an {@link View}
	 * {@link view.View#extend(EClass) extended by} the given {@link EClass eClass} with an
	 * {@link EClass eClass} from a different model.
	 * @param eClassToReduce an {@link EClass eClass} that is not from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassToExtend an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReferenceToExtend an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testReduceEClassDifferentModelNonEmptyView (View view, EClass eClassToReduce, EClass eClassToExtend, EReference eReferenceToExtend) {
		
		assertTrue(view.extend(eClassToExtend));
		assertTrue(view.extend(eReferenceToExtend));
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.reduce(eClassToReduce));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
			
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} with
	 * {@link EClass eClasses} form the same model.
	 */
	@Test
	final void testReduceEClassSameModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		
		testReduceEClassSameModelIdempotence(viewOnScrumPlanningInstanceOne, workitem);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		testReduceEClassSameModelIdempotence(viewOnScrumPlanningInstanceOne, workitem);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEClassSameModelNodesInView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, new EClass[] {stakeholder}, new EClass[] {backlog, workitem});
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEClassSameModelNodesInView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, new EClass[] {stakeholder, backlog}, new EClass[] {workitem});
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEClassSameModelEdgesInView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, new EClass[] {backlog}, new EReference[] {backlogWorkitems}, new EClass[] {workitem});
		
	}

	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method with edges in
	 * the view that reference {@link EObject eObjects} that are to be removed by the method.
	 * In this case the {@link EObject eObjects} should be removed from the {@link view.View#graph graph}
	 * but <b>not</b> from the maps (i.e {@link view.View#graphMap graphMap} and {@link view.View#objectMap objectMap}).
	 * @param eClassesToExtend the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param eReferencesToExtend the {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} 
	 * between {@link EClass eClassesToExtend} and {@link EClass eClassesToReduce}
	 * @param eClassesToReduce the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 */
	private void testReduceEClassSameModelEdgesInView (View view, Resource model, EClass[] eClassesToExtend, EReference[] eReferencesToExtend, EClass[] eClassesToReduce) {
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(view.extend(eClass));
		}
		
		for (EClass eClass : eClassesToExtend) {
			assertTrue(view.extend(eClass));
		}
		
		for (EReference eReference : eReferencesToExtend) {
			assertTrue(view.extend(eReference));
		}
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(view.reduce(eClass));
		}
		
		// test maps
		
		HashSet<EClass> eClassesToExtendHashSet = new HashSet<EClass>(List.of(eClassesToExtend));
		HashSet<EClass> eClassesToReduceHashSet = new HashSet<EClass>(List.of(eClassesToReduce));
		
		Set<EObject> expectedGraphEObjects = getEObjectsFromResource (
				model, 
				eObject -> eClassesToExtendHashSet.contains(eObject.eClass()) && !eClassesToReduceHashSet.contains(eObject.eClass())
		).get(0);
		
		HashSet<EClass> eClassesHashSetOfEReferences = new HashSet<EClass>();
		eClassesHashSetOfEReferences.addAll(List.of(eReferencesToExtend).stream().map(EReference::getEContainingClass).collect(Collectors.toList()));
		eClassesHashSetOfEReferences.addAll(List.of(eReferencesToExtend).stream().map(EReference::getEReferenceType).collect(Collectors.toList()));
		
		Set<EObject> eReferenceEObjects = getEObjectsFromResource (
				model,
				eObject -> eClassesHashSetOfEReferences.contains(eObject.eClass())
		).get(0);
		
		Set<EObject> expectedMapEObjects = new HashSet<>(expectedGraphEObjects);
		expectedMapEObjects.addAll(eReferenceEObjects);
		
		assertEquals(expectedMapEObjects, view.graphMap.keySet());
		assertEquals(expectedMapEObjects, new HashSet<>(view.objectMap.values()));
		
		// test graph nodes
		
		assertEquals(
				expectedGraphEObjects, 
				view.graph.getNodes().stream().
				map(view.objectMap::get).
				collect(Collectors.toSet())
		);
		
	}

	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} by 
	 * {@link view.View#extend(EClass) extending by} the given {@link EClass eClass} 
	 * and {@link view.View#reduce(EClass) reducing} again. The {@link View} is not required
	 * to be empty.
	 * @param eClass the {@link EClass eClass} to use
	 */
	private void testReduceEClassSameModelIdempotence (View view, EClass eClass) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertTrue(view.extend(eClass));
		assertTrue(view.reduce(eClass));

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method with no edges in the view.
	 * In this case the {@link EObject eObjects} that are to be removed should also be removed from the
	 * maps (i.e {@link view.View#graphMap graphMap} and {@link view.View#objectMap objectMap}).
	 * @param eClassesToExtend an array of {@link EClass eClasses} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param eClassesToReduce an array of {@link EClass eClasses} from the {@code SCRUM_PLANNIG_ECORE}
	 */
	private void testReduceEClassSameModelNodesInView (View view, Resource model, EClass[] eClassesToExtend, EClass[] eClassesToReduce) {
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(view.extend(eClass));
		}
		
		for (EClass eClass : eClassesToExtend) {
			assertTrue(view.extend(eClass));
		}
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(view.reduce(eClass));
		}
		
		HashSet<EClass> eClassesToExtendHashSet = new HashSet<>(List.of(eClassesToExtend));
		Set<EObject> expectedEObjects = getEObjectsFromResource(model, 
				eObject -> eClassesToExtendHashSet.contains(eObject.eClass())).get(0);
		
		// nodes in maps
		assertEquals(expectedEObjects, view.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(view.objectMap.values()));
		
		// nodes in graph
		assertEquals (
				expectedEObjects, 
				view.graph.getNodes().stream().
				map(view.objectMap::get).
				collect(Collectors.toSet())
		);
		
		// test edges
		assertTrue(view.graph.getEdges().isEmpty());

	}
	
	// reduce(EReference)
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} with
	 * an {@link EReference eReference} from a different model.
	 */
	@Test
	final void testReduceEReferenceDifferentModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass workitemEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "WorkItem")[0];
		EClass classEClass = getEClassFromResource(CRA_ECORE, "Class")[0];
		EReference encapsulatesEReference = getEReferenceFromEClass(classEClass, "encapsulates");
		
		testReduceEReferenceEmptyView (viewOnScrumPlanningInstanceOne, encapsulatesEReference);
		
		testReduceEReferenceDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, encapsulatesEReference, workitemEClass);
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an empty {@link View}.
	 * @param eReference an {@link EReference eReference}.
	 */
	private void testReduceEReferenceEmptyView (View view, EReference eReference) {
		
		assertFalse(view.reduce(eReference));
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an {@link View}
	 * {@link view.View#extend(EClass) extended by} the given {@link EClass eClass} with an
	 * {@link EReference eReference} from a different model.
	 * @param eReference an {@link EReference eReference} that is not from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClass an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testReduceEReferenceDifferentModelNonEmptyView (View view, EReference eReference, EClass eClass) {
		
		assertTrue(view.extend(eClass));
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.reduce(eReference));
		assertTrue(view.graph.getEdges().isEmpty());
		assertFalse(view.graph.getNodes().isEmpty());
		assertFalse(view.graphMap.isEmpty());
		assertFalse(view.objectMap.isEmpty());
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
			
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} with
	 * {@link EReference eReferences} from the same model.
	 */
	@Test
	final void testReduceEReferenceSameModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "WorkItem", "Backlog", "Stakeholder");
		EClass workitemEClass = eClasses[0];
		EClass backlogEClass = eClasses[1];
		EClass stakeholder = eClasses[2];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholder, "workitems");
		
		testReduceEReferenceEmptyView(viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference);
		
		testReduceEReferenceSameModelNoMatchingEdges(viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference, workitemEClass, backlogEClass, stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNoMatchingEdges(viewOnScrumPlanningInstanceOne, stakeholderWorkitemsEReference, workitemEClass, backlogEClass, backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testReduceEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
			
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an {@link View}
	 * {@link view.View#extend(EClass) extended by} the given {@link EClass eClasses} and {@link view.View#extend(EReference) extended by}
	 * the given {@link EReference eReferenceToExtend} with an {@link EReference eReferenceToReduce} that is not part of the view.
	 * @param eReferenceToExtend an {@link EReference eReference} from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassToExtendOne an {@link EClass eClass} from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassToExtendTwo another {@link EClass eClass} from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReferenceToReduce another {@link EReference eReference} from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testReduceEReferenceSameModelNoMatchingEdges (View view, EReference eReferenceToExtend, EClass eClassToExtendOne, EClass eClassToExtendTwo, EReference eReferenceToReduce) {
		
		assertTrue(view.extend(eReferenceToExtend));
		assertTrue(view.extend(eClassToExtendOne));
		assertTrue(view.extend(eClassToExtendTwo));
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.reduce(eReferenceToReduce));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an empty {@link View}
	 * by {@link view.View#extend(EReference) extending by} the given eReference and {@link view.View#reduce(EReference) reducing} again.
	 * @param eReference the {@link EReference eReference} to use
	 */
	private void testReduceEReferenceSameModelIdempotence (View view, EReference eReference) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertTrue(view.extend(eReference));
		assertTrue(view.reduce(eReference));

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}

	// reduce(EObject)
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} with 
	 * parameters from a different model.
	 */
	@Test
	final void testReduceEObjectDifferentModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass workitemEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "WorkItem")[0];
		EClass namedElement = getEClassFromResource(CRA_ECORE, "NamedElement")[0];
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("2"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("5")
		);
		
		EObject method2EObject = mapOfSets.get(0).iterator().next();
		EObject attribute5EObject = mapOfSets.get(1).iterator().next();
		
		testReduceEObjectEmptyView (viewOnScrumPlanningInstanceOne, method2EObject);
		
		testReduceEObjectEmptyView (viewOnScrumPlanningInstanceOne, attribute5EObject);
		
		testReduceEObjectDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, workitemEClass, method2EObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, workitemEClass, attribute5EObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, workitemEClass, attribute5EObject);
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)}.
	 * Tries to reduce an empty {@link View} this should always fail and change nothing.
	 * @param eObject an {@link EObject eObject} to try to reduce by
	 */
	private void testReduceEObjectEmptyView (View view, EObject eObject) {
		
		assertFalse(view.reduce(eObject));
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)}.
	 * Extends the {@link View} by the given {@link EObject eClassToExtend} and then tries
	 * to reduce by the given {@link EObject eObjectToReduce}. The reducing should not work,
	 * as the {@link EObject eObjectToReduce} is not from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model.
	 * @param eClassToExtend an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObjectToReduce an {@link EObject eObject} that is not from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 */
	private void testReduceEObjectDifferentModelNonEmptyView (View view, EClass eClassToExtend, EObject eObjectToReduce) {
		
		assertTrue(view.extend(eClassToExtend));
		
		List<Node> expectedNodes = new ArrayList<>(view.graph.getNodes());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.reduce(eObjectToReduce));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertTrue(view.graph.getEdges().isEmpty());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}

	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} with 
	 * parameters from the same model.
	 */
	@Test
	final void testReduceEObjectSameModel () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("Stakeholder"),
				eObject -> eObject.eClass().getName().equals("WorkItem"));
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		EObject[] stakeholderEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		EObject[] workitemEObjects = mapOfSets.get(2).toArray(new EObject[0]);
		EObject[] stakeholderAndBacklogEObjects = new EObject[] {backlogEObject, stakeholderEObjects[0], stakeholderEObjects[1]};
		
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EClass workitemClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "WorkItem")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		for (EObject eObject : stakeholderAndBacklogEObjects) {
			testReduceEObjectEmptyView (viewOnScrumPlanningInstanceOne, eObject);
		}
		
		for (EObject eObject : stakeholderAndBacklogEObjects) {
			testReduceEReferenceSameModelIdempotence (viewOnScrumPlanningInstanceOne, eObject);
		}
		
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		for (EObject eObject : stakeholderAndBacklogEObjects) {
			testReduceEReferenceSameModelIdempotence (viewOnScrumPlanningInstanceOne, eObject);
		}
		
		viewOnScrumPlanningInstanceOne.extend(workitemClass);
		for (EObject eObject : stakeholderAndBacklogEObjects) {
			testReduceEReferenceSameModelIdempotence (viewOnScrumPlanningInstanceOne, eObject);
		}
		
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[0], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[1], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[2], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[3], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[0], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[1], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[2], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(viewOnScrumPlanningInstanceOne, backlogEObject, workitemEObjects[3], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} on an non empty {@link View}.
	 * The {@link View} is extended by the given {@link EObject eObjects} and the {@link Edge edge} defined
	 * by the {@link EObject eObjects} and the {@link EReference eReferenceToExtend}.
	 * @param eObjectToReduce an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and a container of the {@linkplain EReference eReferenceToExtend}
	 * @param eObjectToExtend an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and the by {@link EObject eObjectToReduce} and {@linkplain EReference eReferenceToExtend} referenced {@link EObject eObject}
	 * @param eReferenceToExtend an {@linkplain EReference eReference} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} between
	 * {@link EObject eObjectToReduce} and {@link EObject eObjectToExtend}
	 */
	private void testReduceEReferenceSameModelNonEmptyView (View view, EObject eObjectToReduce, EObject eObjectToExtend, EReference eReferenceToExtend) {
		
		assertTrue(view.extend(eObjectToExtend));
		assertTrue(view.extend(eObjectToReduce));
		assertTrue(view.extend(eObjectToReduce, eObjectToExtend, eReferenceToExtend));
		
		assertTrue(view.reduce(eObjectToReduce));
		
		// test nodes in the map
		Set<EObject> expedtedEObjects = new HashSet<>();
		expedtedEObjects.add(eObjectToReduce);
		expedtedEObjects.add(eObjectToExtend);
		
		assertEquals(expedtedEObjects, view.graphMap.keySet());
		assertEquals(expedtedEObjects, new HashSet<>(view.objectMap.values()));
		
		// test nodes in the graph
		assertEquals (
				Set.of(eObjectToExtend),
				view.graph.getNodes().stream().
				map(view::getObject).
				collect(Collectors.toSet())
		);
		
		// test edges
		assertEquals(1, view.graph.getEdges().size());
		Edge edge = view.graph.getEdges().get(0);
		assertEquals(edge.getType(), eReferenceToExtend);
		
		HashSet<EObject> actualEObjects = new HashSet<>();
		actualEObjects.add(view.objectMap.get(edge.getSource()));
		actualEObjects.add(view.objectMap.get(edge.getTarget()));
		
		assertEquals(expedtedEObjects, actualEObjects);
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} on an non empty {@link View}.
	 * The {@link View} is extended by the given {@link EObject eObjects} and the {@link Edge edge} defined
	 * by the {@link EObject eObjects} and the {@link EReference eReferenceToExtend}. It reduces the 
	 * {@link View} by the {@link EObject eObjectToReduce} a second time. This shouldn't work and nothing
	 * should change.
	 * @param eObjectToReduce an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and a container of the {@linkplain EReference eReferenceToExtend}
	 * @param eObjectToExtend an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and the by {@link EObject eObjectToReduce} and {@linkplain EReference eReferenceToExtend} referenced {@link EObject eObject}
	 * @param eReferenceToExtend an {@linkplain EReference eReference} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} between
	 * {@link EObject eObjectToReduce} and {@link EObject eObjectToExtend}
	 */
	private void testReduceEReferenceSameModelNonEmptyViewTwice (View view, EObject eObjectToReduce, EObject eObjectToExtend, EReference eReferenceToExtend) {
		
		testReduceEReferenceSameModelNonEmptyView (view, eObjectToReduce, eObjectToExtend, eReferenceToExtend);
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.reduce(eObjectToReduce));
	
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} method
	 * by {@link view.View#extend(EObject) extending by} the given {@link EObject eObject} and {@link view.View#reduce(EObject) reducing} again.
	 * The method does not require an empty {@link View} as it only tests of the {@link View} remains the same.
	 * @param eObject an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 */
	private void testReduceEReferenceSameModelIdempotence (View viewOnScrumPlanningInstanceOne, EObject eObject) {
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eObject));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(eObject));

		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	// reduce(EObject, EObject, EReference)
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)} with
	 * parameters from a different model.
	 */
	@Test
	final void testReduceEObjectEObjectEReferenceDifferentModel() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("2"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("3"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("5"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("8")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject method3 = mapOfSets.get(1).iterator().next();
		EObject attribute5 = mapOfSets.get(2).iterator().next();
		EObject class8 = mapOfSets.get(3).iterator().next();
		
		// get SCRUM PLANNING meta-model elements
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testReduceEObjectEObjectEReferenceEmptyView (viewOnScrumPlanningInstanceOne, method3, attribute5, encapsulates);
		
		testReduceEObjectEObjectEReferenceEmptyView (viewOnScrumPlanningInstanceOne, class8, method2, encapsulates);
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference, class8, method2, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference, class8, method2, encapsulates);
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 * Tries to reduce an empty {@link View} by the {@link Edge edge} defined by
	 * the given parameters. This should always fail and change nothing.
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReference an {@link EReference eReference}
	 */
	private void testReduceEObjectEObjectEReferenceEmptyView (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		assertFalse(view.reduce(eObjectOne, eObjectTwo, eReference));
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
		
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 * Tries to reduced an {@link View} extended by the given {@link EReference eReferenceToExtend},
	 * by the {@link Edge edge} that is defined by the parameters 
	 * (e.g {@link EObject eObjectOne}, {@link EObject eObjectTwo}, {@link EReference eReference}).
	 * @param eReferenceToExtend an {@link EReference eReference} that is from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObjectOne an {@link EObject eObject} that is not from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 * @param eObjectTwo an {@link EObject eObject} that is not from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 * @param eReference an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (View view, EReference eReferenceToExtend, EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		assertTrue(view.extend(eReferenceToExtend));
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.reduce(eObjectOne, eObjectTwo, eReference));
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)} with
	 * parameters from the same model.
	 */
	@Test
	final void testReduceEObjectEObjectEReferenceSameModel() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		
		EObject[] workitemEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		
		EObject[] stakeholderEObjects = mapOfSets.get(2).toArray(new EObject[0]);
		EObject stakeholderOne = stakeholderEObjects[0];
		EObject stakeholderTwo = stakeholderEObjects[1];
		
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholderOne.eClass(), "workitems");
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEObject.eClass(), "workitems");
		
		// get referenced workitems of stakeholder one
		Object object = stakeholderOne.eGet(stakeholderWorkitemsEReference);
		assertTrue(object instanceof EList<?>);
		@SuppressWarnings("unchecked")
		EList<EObject> workitemsOfStakeholderOne = (EList<EObject>) object;
		
		testReduceEObjectEObjectEReferenceEmptyView(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		testReduceEObjectEObjectEReferenceEmptyView(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		
		testReduceEObjectEObjectEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		testReduceEObjectEObjectEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		testReduceEObjectEObjectEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(workitemsOfStakeholderOne.get(0));
		testReduceEObjectEObjectEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		testReduceEObjectEObjectEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(workitemsOfStakeholderOne.get(1));
		testReduceEObjectEObjectEReferenceSameModelIdempotence(viewOnScrumPlanningInstanceOne, stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyView (viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, stakeholderWorkitemsEReference, stakeholderOne, workitemsOfStakeholderOne.get(1));
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyView (viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE, backlogWorkitemsEReference, backlogEObject, workitemEObjects[3]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyViewTwice (viewOnScrumPlanningInstanceOne, backlogWorkitemsEReference, backlogEObject, workitemEObjects[3]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyViewTwice (viewOnScrumPlanningInstanceOne, stakeholderWorkitemsEReference, stakeholderOne, workitemsOfStakeholderOne.get(1));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)} method
	 * by {@link view.View#extend(EObject, EObject, EReference) extending by} the {@link Edge edge} that is defined by the parameters 
	 * (e.g {@link EObject eObjectOne}, {@link EObject eObjectTwo}, {@link EReference eReference}) and {@link view.View#reduce(EObject, EObject, EReference) reducing} again.
	 * The method does not require an empty {@link View} as it only tests of the {@link View} remains the same.
	 * @param eObjectOne an {@link EObject eObject} that is from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 * @param eObjectTwo an {@link EObject eObject} that is from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 * @param eReference an {@link EReference eReference} that is from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testReduceEObjectEObjectEReferenceSameModelIdempotence (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertTrue(view.extend(eObjectOne, eObjectTwo, eReference));
		assertTrue(view.reduce(eObjectOne, eObjectTwo, eReference));

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)} on an non empty {@link View}.
	 * The {@link View} is extended by the given {@link EReference eReferenceToExtend} and then reduced by {@link Edge edge} defined by the parameters 
	 * (e.g {@link EObject eObjectOne}, {@link EObject eObjectTwo}, {@link EReference eReferenceToExtend}).
	 * @param eReferenceToExtend an {@linkplain EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * between the {@link EObject eObjectToReduceOne} and the {@link EObject eObjectToReduceTwo}.
	 * @param eObjectToReduceOne an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and a container of the {@linkplain EReference eReferenceToExtend}
	 * @param eObjectToReduceTwo an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and the by {@link EObject eObjectToReduceOne} and {@linkplain EReference eReferenceToExtend} referenced {@link EObject eObject}
	 */
	private void testReduceEObjectEObjectEReferenceSameModelNonEmptyView (View view, Resource model, EReference eReferenceToExtend, EObject eObjectToReduceOne, EObject eObjectToReduceTwo) {
		
		assertTrue(view.extend(eReferenceToExtend));
		assertTrue(view.reduce(eObjectToReduceOne, eObjectToReduceTwo, eReferenceToExtend));
		
		// test nodes from the map
		
		// get all the eObjects that contain eReferences
		Set<EObject> eObjectsFromEReferenceContainingType = getEObjectsFromResource (
				model, 
				eObject -> eObject.eClass() == eReferenceToExtend.getEContainingClass()
		).get(0);
		
		// collect all possible edges ignoring the direction
		List<Set<EObject>> listOfEdges = new ArrayList<Set<EObject>>();
		Iterator<EObject> eObjectIterator = eObjectsFromEReferenceContainingType.iterator();
		
		while (eObjectIterator.hasNext()) {
			EObject eObject = (EObject) eObjectIterator.next();
			Object referenced = eObject.eGet(eReferenceToExtend);
			if (referenced instanceof EObject) {
				listOfEdges.add(new HashSet<>(List.of(eObject, (EObject) referenced)));
			} else if (referenced instanceof EList<?>) {
				for (Object object : (EList<?>) referenced) {
					listOfEdges.add(new HashSet<>(List.of(eObject, (EObject) object)));
				}
			}
			
		}
		
		listOfEdges.remove(new HashSet<>(List.of(eObjectToReduceOne, eObjectToReduceTwo)));
		
		/**
		 * expected eObjects are only those that are part of edges after the (eObjectToReduceOne, eObjectToReduceTwo, eReferenceToExtend) edge has been removed
		 * so there shouldn't be unnecessary edges in the maps
		 */
		Set<EObject> expectedEObjects = new HashSet<EObject>();
		
		for (Set<EObject> eObjectSet : listOfEdges) {
			expectedEObjects.addAll(eObjectSet);
		}
		
		assertEquals(expectedEObjects, view.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(view.objectMap.values()));
		
		// test nodes from the graph
		assertTrue(view.graph.getNodes().isEmpty());
		
		// test edges from the graph
		
		Set<EObject> actualEObjects = new HashSet<>();
		
		for (Edge edge : view.graph.getEdges()) {
			assertTrue(edge.getType() == eReferenceToExtend);
			EObject sourceEObject = view.objectMap.get(edge.getSource());
			EObject targetEObject = view.objectMap.get(edge.getTarget());
			assertTrue(listOfEdges.contains(new HashSet<>(List.of (sourceEObject, targetEObject))));
			actualEObjects.add(sourceEObject);
			actualEObjects.add(targetEObject);
		}
		
		assertEquals(expectedEObjects, actualEObjects);
		
	}
	
	/* Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)} on an non empty {@link View}.
	 * The {@link View} is extended by the given {@link EReference eReferenceToExtend} and then reduced twice by {@link Edge edge} defined by the parameters 
	 * (e.g {@link EObject eObjectOne}, {@link EObject eObjectTwo}, {@link EReference eReferenceToExtend}). The second reduction shouldn't work and nothing should change. 
	 * @param eReferenceToExtend an {@linkplain EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * between the {@link EObject eObjectToReduceOne} and the {@link EObject eObjectToReduceTwo}.
	 * @param eObjectToReduceOne an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and a container of the {@linkplain EReference eReferenceToExtend}
	 * @param eObjectToReduceTwo an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model 
	 * and the by {@link EObject eObjectToReduceOne} and {@linkplain EReference eReferenceToExtend} referenced {@link EObject eObject}
	 */
	private void testReduceEObjectEObjectEReferenceSameModelNonEmptyViewTwice (View view, EReference eReferenceToExtend, EObject eObjectToReduceOne, EObject eObjectToReduceTwo) {
		 
		testReduceEObjectEObjectEReferenceSameModelNonEmptyView(view, SCRUM_PLANNIG_INSTANCE_ONE, eReferenceToExtend, eObjectToReduceOne, eObjectToReduceTwo);
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.reduce(eObjectToReduceOne, eObjectToReduceTwo, eReferenceToExtend));

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	// end: reduce(EObject, EObject, EReference)
	
	/**
	 * Test method for {@link view.View#copy()} on an empty {@link View}.
	 */
	@Test
	final void testCopyEmptyView() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test copy of empty view
		View copyOfView = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(copyOfView == viewOnScrumPlanningInstanceOne);
		assertTrue(copyOfView.graphMap.isEmpty());
		assertFalse(viewOnScrumPlanningInstanceOne.graphMap == copyOfView.graphMap);
		assertTrue(copyOfView.objectMap.isEmpty());
		assertFalse(viewOnScrumPlanningInstanceOne.objectMap == copyOfView.objectMap);
		assertFalse(viewOnScrumPlanningInstanceOne.graph == copyOfView.graph);
		assertTrue(copyOfView.graph.getNodes().isEmpty());
		assertFalse(viewOnScrumPlanningInstanceOne.graph.getNodes() == copyOfView.graph.getNodes());
		assertTrue(copyOfView.graph.getEdges().isEmpty());
		assertFalse(viewOnScrumPlanningInstanceOne.graph.getEdges() == copyOfView.graph.getEdges());
		
	}
	
	/**
	 * Test method for {@link view.View#copy()} on an empty {@link View}.
	 * Changing the copied view shouldn't change the original one.
	 * @param viewToCopy the view to copy
	 * @param objects a list of {@link EObject eObjects}, {@link EClass eClasses} and {@link EReference eReferences} to extend the view by
	 */
	private void testCopyChangeView(View viewToCopy, Object... objects) {
		
		View copyOfView = viewToCopy.copy();
		
		List<Node> expectedNodes = new ArrayList<Node>(copyOfView.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(copyOfView.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(copyOfView.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(copyOfView.objectMap.values());
		
		for (EObject eObject : expectedObjectMapSet) {
			viewToCopy.extend(eObject);
		}
		
		assertEquals(expectedNodes, copyOfView.graph.getNodes());
		assertEquals(expectedEdges, copyOfView.graph.getEdges());
		assertEquals(expectedGraphMapSet, copyOfView.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(copyOfView.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#copy()} on a non-empty {@link View}.
	 */
	@Test
	final void testCopyNonEmpty() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		testCopyChangeView (viewOnScrumPlanningInstanceOne, workitem, stakeholder);
		// note how the viewOnScrumPlanningInstanceOne is not cleared
		testCopyChangeView (viewOnScrumPlanningInstanceOne, backlogWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		copiedViewEqualsOriginalView(viewOnScrumPlanningInstanceOne.copy(), viewOnScrumPlanningInstanceOne);
		
		// nodes only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(workitem);
		copiedViewEqualsOriginalView(viewOnScrumPlanningInstanceOne.copy(), viewOnScrumPlanningInstanceOne);
		
		// edges only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		copiedViewEqualsOriginalView(viewOnScrumPlanningInstanceOne.copy(), viewOnScrumPlanningInstanceOne);
		
	}

	/**
	 * Helper method for the testing of {@link view.View#copy()}.
	 * Tests if the {@link View finalCopyOfView} is eqal to the {@link View viewOnScrumPlanningInstanceOne}.
	 */
	private void copiedViewEqualsOriginalView(View copyOfView, View originalView) {
		
		assertFalse(copyOfView == originalView);
		assertFalse(originalView.graphMap == copyOfView.graphMap);
		assertFalse(originalView.objectMap == copyOfView.objectMap);
		assertFalse(originalView.graph == copyOfView.graph);
		assertFalse(originalView.graph.getNodes() == copyOfView.graph.getNodes());
		assertFalse(originalView.graph.getEdges() == copyOfView.graph.getEdges());

		assertTrue(originalView.equals(copyOfView));
		
	}

	/**
	 * Test method for {@link view.View#clear()}.
	 */
	@Test
	final void testClear() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		
		EObject[] workitemEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		
		EObject[] stakeholderEObjects = mapOfSets.get(2).toArray(new EObject[0]);
		EObject stakeholderOne = stakeholderEObjects[0];
		EObject stakeholderTwo = stakeholderEObjects[1];
		
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholderOne.eClass(), "workitems");
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEObject.eClass(), "workitems");
		
		// test on empty view
		viewOnScrumPlanningInstanceOne.clear();
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
		// test on view with nodes only
		viewOnScrumPlanningInstanceOne.extend(backlogEObject);
		viewOnScrumPlanningInstanceOne.extend(workitemEObjects[0]);
		viewOnScrumPlanningInstanceOne.clear();
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
		// test on view with edges only
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
		// test on view with both edges and nodes
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
	}

	// union
	
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * This methods tests the union method on a non-empty view.
	 */
	@Test
	final void testUnionWithNonEmptyView () {
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		View viewOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// nodes only
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		
		View copyOfViewOne = viewOne.copy();
		
		testUnionWithNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testUnionWithNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// edges only
		
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		copyOfViewOne = viewOne.copy();
		
		testUnionWithNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testUnionWithNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// nodes and edges
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		copyOfViewOne = viewOne.copy();
		
		testUnionWithNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testUnionWithNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// nodes only
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		
		testUnionCommutativity (viewOne, viewTwo);
		
		viewOne.clear();
		viewTwo.clear();
		
		// edges only
		
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		testUnionCommutativity (viewOne, viewTwo);
		
		viewOne.clear();
		viewTwo.clear();
		
		// nodes and edges
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		testUnionCommutativity (viewOne, viewTwo);
		
	}
	
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * This method tests if the commutativity of the union operation holds for the given {@link View views}.
	 * @param viewOne a {@link View view} with the same {@link View#resource resource} as {@link View viewTwo}
	 * @param viewTwo a {@link View view} with the same {@link View#resource resource} as {@link View viewOne}
	 */
	private void testUnionCommutativity (View viewOne, View viewTwo) {
		
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewTwoEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewTwo.objectMap.get(edge.getSource()), viewTwo.objectMap.get(edge.getTarget()));
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewOneEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewOne.objectMap.get(edge.getSource()), viewOne.objectMap.get(edge.getTarget()));
		
		View copyOfViewOne = viewOne.copy();
		
		try {
			viewOne.union(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		Set<EObject> nodeEObjectsViewOne = viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> edgeEObjectsViewOne = viewOne.graph.getEdges().stream().map(viewOneEdgeMapper).collect(Collectors.toSet());
		Set<EObject> graphMapSetViewOne = new HashSet<>(viewOne.graphMap.keySet());
		Set<EObject> objectMapSetViewOne = new HashSet<>(viewOne.objectMap.values());
		
		try {
			viewTwo.union(copyOfViewOne);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		Set<EObject> nodeEObjectsViewTwo = viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> edgeEObjectsViewTwo = viewTwo.graph.getEdges().stream().map(viewTwoEdgeMapper).collect(Collectors.toSet());
		Set<EObject> graphMapSetViewTwo = new HashSet<>(viewTwo.graphMap.keySet());
		Set<EObject> objectMapSetViewTwo = new HashSet<>(viewTwo.objectMap.values());
		
		assertEquals(nodeEObjectsViewOne, nodeEObjectsViewTwo);
		assertEquals(edgeEObjectsViewOne, edgeEObjectsViewTwo);
		assertEquals(graphMapSetViewOne, graphMapSetViewTwo);
		assertEquals(objectMapSetViewOne, objectMapSetViewTwo);
		
	}
	
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * This methods tests the {@link view.View#union(view.View)} method the given {@link View views}.
	 * @param viewOne a {@link View view} to call the {@link view.View#union(view.View)} method on
	 * @param viewTwo the {@link View view} to unite with
	 */
	private void testUnionWithNonEmptyView (View viewOne, View viewTwo) {
		
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewTwoEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewTwo.objectMap.get(edge.getSource()), viewTwo.objectMap.get(edge.getTarget()));
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewOneEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewOne.objectMap.get(edge.getSource()), viewOne.objectMap.get(edge.getTarget()));
		
		Set<EObject> expectedNodeEObjectsViewTwo = viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> expectedEdgeEObjectsViewTwo = viewTwo.graph.getEdges().stream().
				map(viewTwoEdgeMapper).
				collect(Collectors.toSet());
		Set<EObject> expectedGraphMapSetViewTwo = new HashSet<>(viewTwo.graphMap.keySet());
		Set<EObject> expectedObjectMapSetViewTwo = new HashSet<>(viewTwo.objectMap.values());
		
		Set<EObject> expectedNodeEObjectsViewOne = viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> expectedEdgeEObjectsViewOne = viewOne.graph.getEdges().stream().
				map(viewOneEdgeMapper).
				collect(Collectors.toSet());
		
		try {
			viewOne.union(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		// test if viewTwo has not been altered
		assertEquals(expectedNodeEObjectsViewTwo, viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet()));
		assertEquals(expectedEdgeEObjectsViewTwo, viewTwo.graph.getEdges().stream().map(viewTwoEdgeMapper).collect(Collectors.toSet()));
		assertEquals(expectedGraphMapSetViewTwo, viewTwo.graphMap.keySet());
		assertEquals(expectedObjectMapSetViewTwo, new HashSet<>(viewTwo.objectMap.values()));
		
		expectedNodeEObjectsViewOne.addAll(expectedNodeEObjectsViewTwo);
		expectedEdgeEObjectsViewOne.addAll(expectedEdgeEObjectsViewTwo);
		
		// test if viewOne contains the correct nodes and edges
		assertEquals(expectedNodeEObjectsViewOne, viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet()));
		assertEquals(expectedEdgeEObjectsViewOne, viewOne.graph.getEdges().stream().map(viewOneEdgeMapper).collect(Collectors.toSet()));
		
		// test if the maps contain all needed but no useless elements
		Set<EObject> expectedMapEObjectsViewOne = expectedNodeEObjectsViewOne;
		expectedEdgeEObjectsViewOne.forEach(entry -> {
			expectedMapEObjectsViewOne.add(entry.getKey());
			expectedMapEObjectsViewOne.add(entry.getValue());
		});
		
		assertEquals(expectedMapEObjectsViewOne, viewOne.graphMap.keySet());
		assertEquals(expectedMapEObjectsViewOne, new HashSet<>(viewOne.objectMap.values()));
		
	}
		
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 */
	@Test
	final void testUnionEmptyViewAndExceptionRunner() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		
		EObject[] workitemEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		
		EObject[] stakeholderEObjects = mapOfSets.get(2).toArray(new EObject[0]);
		EObject stakeholderOne = stakeholderEObjects[0];
		EObject stakeholderTwo = stakeholderEObjects[1];
		
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholderOne.eClass(), "workitems");
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEObject.eClass(), "workitems");
		
		
		testUnionException(viewOnScrumPlanningInstanceOne, "The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testUnionException(viewOnScrumPlanningInstanceOne, "The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		// test with empty view
		testUnionWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test on view with nodes only
		viewOnScrumPlanningInstanceOne.extend(backlogEObject);
		viewOnScrumPlanningInstanceOne.extend(workitemEObjects[0]);
		testUnionWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		viewOnScrumPlanningInstanceOne.clear();
		
		// test on view with edges only
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		testUnionWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		viewOnScrumPlanningInstanceOne.clear();
		
		// test on view with both edges and nodes
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testUnionWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		viewOnScrumPlanningInstanceOne.clear();
		
	}
	
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * First unites viewOnScrumPlanningInstanceOne with an empty view with should change nothing
	 * and then unites the empty view with viewOnScrumPlanningInstanceOne which should make them both equal.
	 * @param view view to call union on
	 * @param model the model to use
	 */
	private void testUnionWithEmptyView(View view, Resource model) {
		
		View viewTwo = new View(model);
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		try {
			view.union(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
		try {
			viewTwo.union(view);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(viewTwo, view);
		
	}
	
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * @param view to call union on
	 * @param expectedErrorMessage the expected error message
	 * @param viewToUniteWith the view to unite with
	 */
	private void testUnionException(View view, String expectedErrorMessage, View viewToUniteWith) {
		
		View expectedSavedState = view.copy();
		
		try {
			view.union(viewToUniteWith);
			fail();
		} catch (ViewSetOperationException e) {
			assertEquals(expectedErrorMessage, e.getMessage());
			assertEquals(expectedSavedState, e.getSavedState());
		}
		
		view.clear();
		
	}

	// intersect
	
	/**
	 * Test method for {@link view.View#intersect(view.View)}.
	 * This methods tests the intersect method on a non-empty view.
	 */
	@Test
	final void testIntersectWithNonEmptyView () {
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		View viewOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// nodes only
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		
		View copyOfViewOne = viewOne.copy();
		
		testIntersectWithNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testIntersectWithNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// edges only
		
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		copyOfViewOne = viewOne.copy();
		
		testIntersectWithNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testIntersectWithNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// nodes and edges
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		copyOfViewOne = viewOne.copy();
		
		testIntersectWithNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testIntersectWithNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		viewOne.extend(backlogWorkitems);
		viewOne.extend(stakeholderWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		copyOfViewOne = viewOne.copy();
		
		testIntersectWithNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testIntersectWithNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// nodes only
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		
		testIntersectCommutativity (viewOne, viewTwo);
		
		viewOne.clear();
		viewTwo.clear();
		
		// edges only
		
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		testIntersectCommutativity (viewOne, viewTwo);
		
		viewOne.clear();
		viewTwo.clear();
		
		// nodes and edges
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		testIntersectCommutativity (viewOne, viewTwo);
		
	}
	
	/**
	 * Test method for {@link view.View#intersect(view.View)}.
	 * This method tests if the commutativity of the intersect operation holds for the given {@link View views}.
	 * @param viewOne a {@link View view} with the same {@link View#resource resource} as {@link View viewTwo}
	 * @param viewTwo a {@link View view} with the same {@link View#resource resource} as {@link View viewOne}
	 */
	private void testIntersectCommutativity (View viewOne, View viewTwo) {
		
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewTwoEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewTwo.objectMap.get(edge.getSource()), viewTwo.objectMap.get(edge.getTarget()));
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewOneEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewOne.objectMap.get(edge.getSource()), viewOne.objectMap.get(edge.getTarget()));
		
		View copyOfViewOne = viewOne.copy();
		
		try {
			viewOne.intersect(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		Set<EObject> nodeEObjectsViewOne = viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> edgeEObjectsViewOne = viewOne.graph.getEdges().stream().map(viewOneEdgeMapper).collect(Collectors.toSet());
		Set<EObject> graphMapSetViewOne = new HashSet<>(viewOne.graphMap.keySet());
		Set<EObject> objectMapSetViewOne = new HashSet<>(viewOne.objectMap.values());
		
		try {
			viewTwo.intersect(copyOfViewOne);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		Set<EObject> nodeEObjectsViewTwo = viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> edgeEObjectsViewTwo = viewTwo.graph.getEdges().stream().map(viewTwoEdgeMapper).collect(Collectors.toSet());
		Set<EObject> graphMapSetViewTwo = new HashSet<>(viewTwo.graphMap.keySet());
		Set<EObject> objectMapSetViewTwo = new HashSet<>(viewTwo.objectMap.values());
		
		assertEquals(nodeEObjectsViewOne, nodeEObjectsViewTwo);
		assertEquals(edgeEObjectsViewOne, edgeEObjectsViewTwo);
		assertEquals(graphMapSetViewOne, graphMapSetViewTwo);
		assertEquals(objectMapSetViewOne, objectMapSetViewTwo);
		
	}
	
	/**
	 * Test method for {@link view.View#intersect(view.View)}.
	 * This methods tests the {@link view.View#intersect(view.View)} method the given {@link View views}.
	 * @param viewOne a {@link View view} to call the {@link view.View#intersect(view.View)} method on
	 * @param viewTwo the {@link View view} to intersect with
	 */
	private void testIntersectWithNonEmptyView (View viewOne, View viewTwo) {
		
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewTwoEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewTwo.objectMap.get(edge.getSource()), viewTwo.objectMap.get(edge.getTarget()));
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewOneEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewOne.objectMap.get(edge.getSource()), viewOne.objectMap.get(edge.getTarget()));
		
		Set<EObject> expectedNodeEObjectsViewTwo = viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> expectedEdgeEObjectsViewTwo = viewTwo.graph.getEdges().stream().
				map(viewTwoEdgeMapper).
				collect(Collectors.toSet());
		Set<EObject> expectedGraphMapSetViewTwo = new HashSet<>(viewTwo.graphMap.keySet());
		Set<EObject> expectedObjectMapSetViewTwo = new HashSet<>(viewTwo.objectMap.values());
		
		Set<EObject> expectedNodeEObjectsViewOne = viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> expectedEdgeEObjectsViewOne = viewOne.graph.getEdges().stream().
				map(viewOneEdgeMapper).
				collect(Collectors.toSet());
		
		try {
			viewOne.intersect(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		// test if viewTwo has not been altered
		
		assertEquals(expectedNodeEObjectsViewTwo, viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet()));
		assertEquals(expectedEdgeEObjectsViewTwo, viewTwo.graph.getEdges().stream().map(viewTwoEdgeMapper).collect(Collectors.toSet()));
		assertEquals(expectedGraphMapSetViewTwo, viewTwo.graphMap.keySet());
		assertEquals(expectedObjectMapSetViewTwo, new HashSet<>(viewTwo.objectMap.values()));
		
		expectedNodeEObjectsViewOne = expectedNodeEObjectsViewOne.stream().filter(expectedNodeEObjectsViewTwo::contains).collect(Collectors.toSet());
		expectedEdgeEObjectsViewOne = expectedEdgeEObjectsViewOne.stream().filter(expectedEdgeEObjectsViewTwo::contains).collect(Collectors.toSet());
		
		// test if the nodes and edges of view one are correct
		
		assertEquals(expectedNodeEObjectsViewOne, viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet()));
		assertEquals(expectedEdgeEObjectsViewOne, viewOne.graph.getEdges().stream().map(viewOneEdgeMapper).collect(Collectors.toSet()));
		
		// test if the maps contain all needed but no useless elements
		Set<EObject> expectedMapEObjectsViewOne = expectedNodeEObjectsViewOne;
		expectedEdgeEObjectsViewOne.forEach(entry -> {
			expectedMapEObjectsViewOne.add(entry.getKey());
			expectedMapEObjectsViewOne.add(entry.getValue());
		});
		
		assertEquals(expectedMapEObjectsViewOne, viewOne.graphMap.keySet());
		assertEquals(expectedMapEObjectsViewOne, new HashSet<>(viewOne.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#intersect(view.View)}.
	 */
	@Test
	final void testIntersectEmptyViewAndExceptionRunner() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		
		EObject[] workitemEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		
		EObject[] stakeholderEObjects = mapOfSets.get(2).toArray(new EObject[0]);
		EObject stakeholderOne = stakeholderEObjects[0];
		EObject stakeholderTwo = stakeholderEObjects[1];
		
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholderOne.eClass(), "workitems");
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEObject.eClass(), "workitems");
		
		
		testIntersectException(viewOnScrumPlanningInstanceOne, "The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testIntersectException(viewOnScrumPlanningInstanceOne, "The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		// test with empty view
		testIntersectWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test on view with nodes only
		viewOnScrumPlanningInstanceOne.extend(backlogEObject);
		viewOnScrumPlanningInstanceOne.extend(workitemEObjects[0]);
		testIntersectWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test on view with edges only
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		testIntersectWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test on view with both edges and nodes
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testIntersectWithEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
	}

	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * First intersects an empty view with viewOnScrumPlanningInstanceOne, which should not change the empty view
	 * and then intersects viewOnScrumPlanningInstanceOne with an empty view, which should make viewOnScrumPlanningInstanceOne empty .
	 * @param view to call the method on
	 * @param model the model to use
	 */
	private void testIntersectWithEmptyView(View view, Resource model) {
		
		View viewTwo = new View(model);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewTwo.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewTwo.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewTwo.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewTwo.objectMap.values());
		
		try {
			viewTwo.intersect(view);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(expectedNodes, viewTwo.graph.getNodes());
		assertEquals(expectedEdges, viewTwo.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewTwo.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewTwo.objectMap.values()));
		
		try {
			view.intersect(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertTrue(view.isEmpty());
		assertTrue(view.graphMap.isEmpty());
		assertTrue(view.objectMap.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#intersect(view.View)}.
	 * @param view the {@link View view} to call the intersect-method on
	 * @param expectedErrorMessage the expected error message
	 * @param viewToIntersect the view to intersect with
	 */
	private void testIntersectException(View view, String expectedErrorMessage, View viewToIntersect) {
		
		View expectedSavedState = view.copy();
		
		try {
			view.intersect(viewToIntersect);
			fail();
		} catch (ViewSetOperationException e) {
			assertEquals(expectedErrorMessage, e.getMessage());
			assertEquals(expectedSavedState, e.getSavedState());
		}
		
		view.clear();
		
	}
	
	// subtract
	
	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 * This methods tests the subtract method on a non-empty view.
	 */
	@Test
	final void testSubtractNonEmptyView () {
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		View viewOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// nodes only
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		
		View copyOfViewOne = viewOne.copy();
		
		testSubtractNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testSubtractNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// edges only
		
		viewOne.extend(backlogWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		copyOfViewOne = viewOne.copy();
		
		testSubtractNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testSubtractNonEmptyView(viewTwo, copyOfViewOne);
		
		viewOne.clear();
		viewTwo.clear();
		
		// nodes and edges
		
		viewOne.extend(backlog);
		viewTwo.extend(workitem);
		viewOne.extend(backlogWorkitems);
		viewOne.extend(stakeholderWorkitems);
		viewTwo.extend(stakeholderWorkitems);
		
		copyOfViewOne = viewOne.copy();
		
		testSubtractNonEmptyView(viewOne, viewTwo);
		
		viewOne = copyOfViewOne;
		
		testSubtractNonEmptyView(viewTwo, copyOfViewOne);
		
	}
	
	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 * This methods tests the {@link view.View#subtract(view.View)} method for the given {@link View views}.
	 * @param viewOne a {@link View view} to call the {@link view.View#subtract(view.View)} method on
	 * @param viewTwo the {@link View view} to subtract
	 */
	private void testSubtractNonEmptyView (View viewOne, View viewTwo) {
		
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewTwoEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewTwo.objectMap.get(edge.getSource()), viewTwo.objectMap.get(edge.getTarget()));
		Function<? super Edge, ? extends SimpleEntry<EObject, EObject>> viewOneEdgeMapper = edge -> new SimpleEntry<EObject, EObject>(viewOne.objectMap.get(edge.getSource()), viewOne.objectMap.get(edge.getTarget()));
		
		Set<EObject> expectedNodeEObjectsViewTwo = viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> expectedEdgeEObjectsViewTwo = viewTwo.graph.getEdges().stream().
				map(viewTwoEdgeMapper).
				collect(Collectors.toSet());
		Set<EObject> expectedGraphMapSetViewTwo = new HashSet<>(viewTwo.graphMap.keySet());
		Set<EObject> expectedObjectMapSetViewTwo = new HashSet<>(viewTwo.objectMap.values());
		
		Set<EObject> expectedNodeEObjectsViewOne = viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet());
		Set<Entry<EObject, EObject>> expectedEdgeEObjectsViewOne = viewOne.graph.getEdges().stream().
				map(viewOneEdgeMapper).
				collect(Collectors.toSet());
		
		try {
			viewOne.subtract(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		// test if viewTwo has not been altered
		
		assertEquals(expectedNodeEObjectsViewTwo, viewTwo.graph.getNodes().stream().map(viewTwo.objectMap::get).collect(Collectors.toSet()));
		assertEquals(expectedEdgeEObjectsViewTwo, viewTwo.graph.getEdges().stream().map(viewTwoEdgeMapper).collect(Collectors.toSet()));
		assertEquals(expectedGraphMapSetViewTwo, viewTwo.graphMap.keySet());
		assertEquals(expectedObjectMapSetViewTwo, new HashSet<>(viewTwo.objectMap.values()));
		
		expectedNodeEObjectsViewOne = expectedNodeEObjectsViewOne.stream().filter(eObject -> !expectedNodeEObjectsViewTwo.contains(eObject)).collect(Collectors.toSet());
		expectedEdgeEObjectsViewOne = expectedEdgeEObjectsViewOne.stream().filter(entry -> !expectedEdgeEObjectsViewTwo.contains(entry)).collect(Collectors.toSet());
		
		// test if the nodes and edges of view one are correct
		
		assertEquals(expectedNodeEObjectsViewOne, viewOne.graph.getNodes().stream().map(viewOne.objectMap::get).collect(Collectors.toSet()));
		assertEquals(expectedEdgeEObjectsViewOne, viewOne.graph.getEdges().stream().map(viewOneEdgeMapper).collect(Collectors.toSet()));
		
		// test if the maps contain all needed but no useless elements
		Set<EObject> expectedMapEObjectsViewOne = expectedNodeEObjectsViewOne;
		expectedEdgeEObjectsViewOne.forEach(entry -> {
			expectedMapEObjectsViewOne.add(entry.getKey());
			expectedMapEObjectsViewOne.add(entry.getValue());
		});
		
		assertEquals(expectedMapEObjectsViewOne, viewOne.graphMap.keySet());
		assertEquals(expectedMapEObjectsViewOne, new HashSet<>(viewOne.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 */
	@Test
	final void testSubtractEmptyViewAndExceptionRunner() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		
		EObject[] workitemEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		
		EObject[] stakeholderEObjects = mapOfSets.get(2).toArray(new EObject[0]);
		EObject stakeholderOne = stakeholderEObjects[0];
		EObject stakeholderTwo = stakeholderEObjects[1];
		
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholderOne.eClass(), "workitems");
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEObject.eClass(), "workitems");
		
		
		testSubtractException(viewOnScrumPlanningInstanceOne, "The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testSubtractException(viewOnScrumPlanningInstanceOne, "The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		// test with empty view
		testSubtractEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test on view with nodes only
		viewOnScrumPlanningInstanceOne.extend(backlogEObject);
		viewOnScrumPlanningInstanceOne.extend(workitemEObjects[0]);
		testSubtractEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test on view with edges only
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		testSubtractEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
		// test on view with both edges and nodes
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testSubtractEmptyView(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		
	}

	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 * First subtracts an empty view from viewOnScrumPlanningInstanceOne, which should not change the viewOnScrumPlanningInstanceOne
	 * and then subtracts viewOnScrumPlanningInstanceOne from an empty view, which should not change the empty view.
	 * @param view the {@link View view} to subtract from
	 * @param model the model to use
	 */
	private void testSubtractEmptyView(View view, Resource model) {
		
		View viewTwo = new View(model);
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		try {
			view.subtract(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
		try {
			viewTwo.subtract(view);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertTrue(viewTwo.isEmpty());
		assertTrue(viewTwo.graphMap.isEmpty());
		assertTrue(viewTwo.objectMap.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 * @param view the view to subtract from
	 * @param expectedErrorMessage the expected error message
	 * @param viewToSubtract the view to subtract
	 */
	private void testSubtractException(View view, String expectedErrorMessage, View viewToSubtract) {
		
		View expectedSavedState = view.copy();
		
		try {
			view.subtract(viewToSubtract);
			fail();
		} catch (ViewSetOperationException e) {
			assertEquals(expectedErrorMessage, e.getMessage());
			assertEquals(expectedSavedState, e.getSavedState());
		}
		
		view.clear();
		
	}
	
	// removeDangling
	
	/**
	 * Test method for {@link view.View#removeDangling()}.
	 */
	@Test
	final void testRemoveDangling() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		// empty view
		checkRemoveDangling(viewOnScrumPlanningInstanceOne);
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		checkRemoveDangling(viewOnScrumPlanningInstanceOne);
		
		// edges only
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		checkRemoveDangling(viewOnScrumPlanningInstanceOne);
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		checkRemoveDangling(viewOnScrumPlanningInstanceOne);
		
	}

	/**
	 * This methods calls the {@link view.View#removeDangling()} method on the
	 * given {@link View view}, checks if the nodes have not been altered the maps are correct
	 * and all dangling edges have been removed. <b> Also clears the view afterwards. </b>
	 * @param view the {@link View view} to check
	 */
	private void checkRemoveDangling (View view) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		Set<Edge> expectedEdges = view.graph.getEdges().stream().filter(edge -> view.contains(edge, false)).collect(Collectors.toSet());
		
		view.removeDangling();

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(new HashSet<>(expectedNodes), new HashSet<>(view.graphMap.values()));
		assertEquals(new HashSet<>(expectedNodes), view.objectMap.keySet());
		assertEquals(expectedEdges, new HashSet<Edge>(view.graph.getEdges()));
		
		view.clear();
		
	}

	// completeDangling
	
	/**
	 * Test method for {@link view.View#completeDangling()}.
	 */
	@Test
	final void testCompleteDangling() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		// empty view
		checkCompleteDangling(viewOnScrumPlanningInstanceOne);
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		checkCompleteDangling(viewOnScrumPlanningInstanceOne);
		
		// edges only
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		checkCompleteDangling(viewOnScrumPlanningInstanceOne);
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		checkCompleteDangling(viewOnScrumPlanningInstanceOne);
		
	}
	
	/**
	 * This methods calls the {@link view.View#completeDangling()} method on the
	 * given {@link View view}, checks if the edges and maps have not been altered
	 * and all edges' nodes are contained in the graph. <b> Also clears the view afterwards. </b>
	 * @param view the {@link View view} to check
	 */
	private void checkCompleteDangling (View view) {
		
		List<Edge> expectedEdges = view.graph.getEdges();
		Set<Node> expectedGraphMapNodes = new HashSet<>(view.graphMap.values());
		Set<Node> expectedObjectMapNodes = view.objectMap.keySet();
		
		view.completeDangling();

		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapNodes, new HashSet<>(view.graphMap.values()));
		assertEquals(expectedObjectMapNodes, view.objectMap.keySet());
		
		Set<Edge> actualEdges = view.graph.getEdges().stream().filter(edge -> view.contains(edge, false)).collect(Collectors.toSet());
		
		assertEquals(new HashSet<>(expectedEdges), actualEdges);
		
		view.clear();
		
	}
	
	// extendByMissingEdges
	
	/**
	 * Test method for {@link view.View#extendByMissingEdges()}.
	 */
	@Test
	final void testExtendByMissingEdges() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		// empty view
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(workitem);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		// edges only
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		testExtendByMissingEdgesCompleteEdges(viewOnScrumPlanningInstanceOne);
		viewOnScrumPlanningInstanceOne.clear();
		
		// incomplete
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("Stakeholder"));
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		EObject[] stakeholderEObjects = mapOfSets.get(1).toArray(new EObject[] {});
		EObject stakeholderOneEObject = stakeholderEObjects[0];
		EObject stakeholderTwoEObject = stakeholderEObjects[1];
		
		@SuppressWarnings("unchecked")
		EList<EObject> referencedBacklogWorkitemEObjects = (EList<EObject>) backlogEObject.eGet(backlogWorkitems);
		@SuppressWarnings("unchecked")
		EList<EObject> referencedStakeholderOneWorkitemEObjects = (EList<EObject>) stakeholderOneEObject.eGet(stakeholderWorkitems);
		@SuppressWarnings("unchecked")
		EList<EObject> referencedStakeholderTwoWorkitemEObjects = (EList<EObject>) stakeholderTwoEObject.eGet(stakeholderWorkitems);
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, stakeholderWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOneEObject);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, stakeholderWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, backlogWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(referencedBacklogWorkitemEObjects.get(0));
		viewOnScrumPlanningInstanceOne.extend(referencedBacklogWorkitemEObjects.get(3));
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, backlogWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, backlogWorkitems, stakeholderWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwoEObject);
		viewOnScrumPlanningInstanceOne.extend(referencedStakeholderTwoWorkitemEObjects.get(0));
		viewOnScrumPlanningInstanceOne.extend(referencedStakeholderOneWorkitemEObjects.get(1));
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, backlogWorkitems, stakeholderWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, backlogWorkitems, stakeholderWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(referencedBacklogWorkitemEObjects.get(2));
		viewOnScrumPlanningInstanceOne.extend(referencedBacklogWorkitemEObjects.get(1));
		viewOnScrumPlanningInstanceOne.extend(backlog, referencedBacklogWorkitemEObjects.get(2), backlogWorkitems);
		testExtendByMissingEdgesIncompleteEdges(viewOnScrumPlanningInstanceOne, backlogWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
	}
	
	/**
	 * Tests if calling the {@link view.View#extendByMissingEdges()} method
	 * on the given {@link View view} changes it. 
	 * @param view a {@link View view} with all possible edges inserted
	 */
	public void testExtendByMissingEdgesCompleteEdges (View view) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		view.extendByMissingEdges();

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}
	
	/**
	 * Tests if calling the {@link view.View#extendByMissingEdges()} method alters the
	 * {@link Node nodes} or the maps. Furthermore checks if the given {@link View view}
	 * contains all required edges with the given {@link Edge#getType() edge-types}.
	 * 
	 * @param view a {@link View view} with missing edges
	 * @param eReferences an array of all {@link Edge#getType() edge-types} the view
	 * should contain after calling the method 
	 */
	public void testExtendByMissingEdgesIncompleteEdges (View view, EReference... eReferences) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		view.extendByMissingEdges();

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
		List<EReference> edgeTypes = List.of(eReferences);
		List<Edge> actualEdges = view.graph.getEdges().stream().filter(edge -> edgeTypes.contains(edge.getType())).collect(Collectors.toList());
		
		// get all possible edges with the given types from the meta-model
		List<List<EObject>> allPossibleEdges = new ArrayList<List<EObject>>();
		
		for (EReference eReference : edgeTypes) {
			EObject[] containingEObjects = getEObjectsFromResource(view.resource, eReference.getEContainingClass()::equals).get(0).toArray(new EObject[0]);
			for (EObject eObject : containingEObjects) {
				Object object = eObject.eGet(eReference);
				if(object instanceof EObject) {
					allPossibleEdges.add(List.of(eObject, ((EObject) object), eReference));
				} else {
					for (Object referencedEObject : ((List<?>) object)) {
						allPossibleEdges.add(List.of(eObject, ((EObject) referencedEObject), eReference));
					}
				}
			}
		}
		
		// remove the actualEdges from allPossibleEdges
		List<List<EObject>> inResourceRemainingEdges = allPossibleEdges.stream().filter(list -> {
			boolean existsEdgeInActualEdges = false;
			for (Edge edge : actualEdges) {
				EObject sourceEObject = view.objectMap.get(edge.getSource());
				EObject targetEObject = view.objectMap.get(edge.getTarget());
				if (edge.getType().equals(list.get(2)) && ( 
						(sourceEObject.equals(list.get(0)) && targetEObject.equals(list.get(1))) || 
						(sourceEObject.equals(list.get(1)) && targetEObject.equals(list.get(0))) 
					)
				) {
					existsEdgeInActualEdges = true;
					break;
				}	
			}
			return !existsEdgeInActualEdges;
		}).collect(Collectors.toList());
		
		// all of the inResourceRemainingEdges must have at least one end that is not in the view
		for (List<EObject> list : inResourceRemainingEdges) {
			assertTrue(!view.contains(list.get(0)) || !view.contains(list.get(1)));
		}
		
	}
	
	// matchViewByMetamodel

	/**
	 * Test method for {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)}
	 * with a wrong meta-model as the input parameter.
	 */
	@Test
	final void testMatchViewByMetamodelWrongParameters() {
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass workitem = eClasses[2];
		
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");

		View viewOnCRAEcore = new View(CRA_ECORE);
		viewOnCRAEcore.extendByAllNodes();
		viewOnCRAEcore.extendByMissingEdges();
		
		View viewOnCRAInstanceOne = new View(CRA_INSTANCE_ONE);
		viewOnCRAInstanceOne.extendByAllNodes();
		viewOnCRAInstanceOne.extendByMissingEdges();
		
		View viewOnScrumPlanningEcore = new View(SCRUM_PLANNIG_ECORE);
		viewOnScrumPlanningEcore.extendByAllNodes();
		viewOnScrumPlanningEcore.extendByMissingEdges();
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		viewOnScrumPlanningInstanceOne.extendByAllNodes();
		viewOnScrumPlanningInstanceOne.extendByMissingEdges();
		
		View viewOnScrumPlanningInstanceTwo = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		viewOnScrumPlanningInstanceTwo.extendByAllNodes();
		viewOnScrumPlanningInstanceTwo.extendByMissingEdges();
		
		// empty view
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnCRAEcore);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnCRAInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnScrumPlanningInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnScrumPlanningInstanceTwo);
		
		// non-empty view
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnCRAEcore);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnCRAInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnScrumPlanningInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, viewOnScrumPlanningInstanceTwo);
		
		// empty view
		View viewOnCRAInstanceOneFirstParam = new View(CRA_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnScrumPlanningEcore);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnCRAInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnScrumPlanningInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnScrumPlanningInstanceTwo);
		
		// non-empty view
		viewOnCRAInstanceOneFirstParam.extend(namedElement);
		viewOnCRAInstanceOneFirstParam.extend(classEClass);
		viewOnCRAInstanceOneFirstParam.extend(encapsulates);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnScrumPlanningEcore);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnCRAInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnScrumPlanningInstanceOne);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOneFirstParam, viewOnScrumPlanningInstanceTwo);
		
	}
	
	/**
	 * Calls the {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)} method
	 * on the given {@link View view} with the given {@link Resource metamodel} and expects it to return false.
	 * Also checks if the view was modified by the method.
	 * @param view the {@link View view} to call the {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)} method on
	 * @param metamodel the {@link Resource metamodel} to use as a parameter
	 */
	private void assertMatchViewByMetamodelWrongParameters (View view, View metamodelView) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.matchViewByMetamodel(metamodelView));

		assertEquals(expectedNodes, view.graph.getNodes());
		assertEquals(expectedEdges, view.graph.getEdges());
		assertEquals(expectedGraphMapSet, view.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(view.objectMap.values()));
		
	}

	/**
	 * Test method for {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)}
	 * with the complete meta-model as the input parameter.
	 */
	@Test
	final void testMatchViewByMetamodelFullMetamodel() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		View fullView = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		for (EClass eClass : getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan", "Stakeholder", "Backlog", "WorkItem", "Sprint")) {
			fullView.extend(eClass);
			for (EReference eReference : eClass.getEAllReferences()) {
				fullView.extend(eReference);
			}
		}
		
		// empty view
		assertMatchViewByMetamodel(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_ECORE, fullView);
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		assertMatchViewByMetamodel(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_ECORE, fullView);
		
		// edges only
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		assertMatchViewByMetamodel(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_ECORE, fullView);
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(workitem);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		assertMatchViewByMetamodel(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_ECORE, fullView);
		
	}
	
	/**
	 * Calls the {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)}
	 * method on the given {@link View view} with a full {@link View view} on the {@code SCRUM_PLANNIG_ECORE}
	 * and asserts that all elements have been inserted. 
	 * <b>Also {@link View#clear() clears} the {@link View view} at the end.</b>
	 * @param view the view to call the method on
	 * @param metaModel the meta-model to use
	 * @param fullView a full view on the model
	 */
	private void assertMatchViewByMetamodel (View view, Resource metaModel, View fullView) {
		
		// create a view on the meta-model and fill it.
		View metaModelView = new View(metaModel);
		metaModelView.extendByAllNodes();
		metaModelView.extendByMissingEdges();
		
		assertTrue(view.matchViewByMetamodel(metaModelView));
		
		assertTrue(fullView.equals(view));
		
		view.clear();
		
	}

	/**
	 * Test method for {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)}
	 * with a partial meta-model as the input parameter.
	 */
	@Test
	final void testMatchViewByMetamodelPartialMetamodel () {
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem", "Plan");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		EClass plan = eClasses[3];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		EReference planBacklog = getEReferenceFromEClass(plan, "backlog");
		EReference planStakeholders = getEReferenceFromEClass(plan, "stakeholders");
		
		View viewOnScrumPlanningEcore = new View(SCRUM_PLANNIG_ECORE);
		viewOnScrumPlanningEcore.extend(((EObject) stakeholder));
		viewOnScrumPlanningEcore.extend(((EObject) backlog));
		viewOnScrumPlanningEcore.extend(((EObject) workitem));
		viewOnScrumPlanningEcore.extend(((EObject) backlogWorkitems));
		viewOnScrumPlanningEcore.extend(((EObject) stakeholderWorkitems));
		
		View viewToCompare = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		viewToCompare.extend(stakeholder);
		viewToCompare.extend(backlog);
		viewToCompare.extend(workitem);
		viewToCompare.extend(backlogWorkitems);
		viewToCompare.extend(stakeholderWorkitems);
		
		View viewToMatch = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// empty view
		assertViewMatchesGiven(viewToMatch, viewOnScrumPlanningEcore, viewToCompare);
		
		// nodes only
		viewToMatch.extend(backlog);
		viewToMatch.extend(plan);
		assertViewMatchesGiven(viewToMatch, viewOnScrumPlanningEcore, viewToCompare);
		
		// edges only
		viewToMatch.extend(planBacklog);
		viewToMatch.extend(planStakeholders);
		viewToMatch.extend(stakeholderWorkitems);
		assertViewMatchesGiven(viewToMatch, viewOnScrumPlanningEcore, viewToCompare);
		
		// nodes and edges
		viewToMatch.extend(backlog);
		viewToMatch.extend(plan);
		viewToMatch.extend(planBacklog);
		viewToMatch.extend(planStakeholders);
		viewToMatch.extend(stakeholderWorkitems);
		assertViewMatchesGiven(viewToMatch, viewOnScrumPlanningEcore, viewToCompare);
		
	}
	
	/**
	 * Calls the {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)}
	 * method on the {@link View viewToMatch} with the given {@link View metaModelView} and expects
	 * it to be equal to the {@link View viewToCompare} afterwards.
	 * <b>Also {@link View#clear() clears} the {@link View viewToMatch} at the end.</b>
	 * @param viewToMatch the {@link View view} to call the method on
	 * @param metaModelView the {@link View view} to use as the parameter for the method call
	 * @param viewToCompare the {@link View view} to assert equality to afterwards
	 */
	private void assertViewMatchesGiven (View viewToMatch, View metaModelView, View viewToCompare) {
		
		assertTrue(viewToMatch.matchViewByMetamodel(metaModelView));
		assertEquals(viewToCompare, viewToMatch);
		viewToMatch.clear();
		
	}
	
	// end: matchViewByMetamodel
	
	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} with objects that are not {@link View views}.
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	final void testEqualsNotView() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		assertFalse(viewOnScrumPlanningInstanceOne.equals(new GraphImpl()));
		assertFalse(viewOnScrumPlanningInstanceOne.equals(SCRUM_PLANNIG_ECORE));
		assertFalse(viewOnScrumPlanningInstanceOne.equals("View"));
		
	}
	
	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} on empty {@link View views}.
	 */
	@Test
	final void testEqualsEmptyView() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		assertTrue(viewOnScrumPlanningInstanceOne.equals(new View(viewOnScrumPlanningInstanceOne.getResource())));
		assertTrue(new View(viewOnScrumPlanningInstanceOne.getResource()).equals(viewOnScrumPlanningInstanceOne));
		assertFalse(viewOnScrumPlanningInstanceOne.equals(new View(SCRUM_PLANNIG_INSTANCE_TWO)));
		assertFalse(new View(SCRUM_PLANNIG_INSTANCE_TWO).equals(viewOnScrumPlanningInstanceOne));
		
	}
	
	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} on non-empty {@link View views}.
	 */
	@Test
	final void testEqualsNonEmptyView() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get the Stakeholder and Backlog EClass from the meta-model
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog", "WorkItem");
		EClass backlog = eClasses[0];
		EClass workitem = eClasses[1];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		View copyOfView = viewOnScrumPlanningInstanceOne.copy();
		
		assertTrue(viewOnScrumPlanningInstanceOne.equals(copyOfView));
		assertTrue(copyOfView.equals(viewOnScrumPlanningInstanceOne));
		
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		assertFalse(viewOnScrumPlanningInstanceOne.equals(copyOfView));
		assertFalse(copyOfView.equals(viewOnScrumPlanningInstanceOne));
		
		copyOfView.extend(workitem);
		
		assertTrue(viewOnScrumPlanningInstanceOne.equals(copyOfView));
		assertTrue(copyOfView.equals(viewOnScrumPlanningInstanceOne));
		
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		copyOfView = viewOnScrumPlanningInstanceOne.copy();
		
		assertTrue(viewOnScrumPlanningInstanceOne.equals(copyOfView));
		assertTrue(copyOfView.equals(viewOnScrumPlanningInstanceOne));
		
	}
	
	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} on {@link View views} with dangling edges.
	 */
	@Test
	final void testEqualsDangling() {
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		
		EObject[] workitemEObjects = mapOfSets.get(1).toArray(new EObject[0]);
		
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEObject.eClass(), "workitems");
		
		View viewOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		viewOne.extend(backlogEObject, workitemEObjects[0], backlogWorkitemsEReference);
		viewOne.extend(backlogEObject);
		
		View viewTwo = viewOne.copy();
		
		viewOne.extend(workitemEObjects[0]);
		
		assertFalse(viewOne.equals(viewTwo));
		assertFalse(viewTwo.equals(viewOne));
		
	}

	/**
	 * Test method for {@link View#extendByAllNodes()}.
	 */
	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3})
	final void testExtendByAllNodes (int scrumPlanningInstanceNumber) {
		
		Resource scrumPlanningInstanceReference = switch (scrumPlanningInstanceNumber) {
			case 1 -> SCRUM_PLANNIG_INSTANCE_ONE;
			case 2 -> SCRUM_PLANNIG_INSTANCE_TWO;
			case 3 -> SCRUM_PLANNIG_INSTANCE_THREE;
			default -> null;
		};
		
		if(scrumPlanningInstanceReference == null) 
			throw new IllegalArgumentException("Unexpected value: " + scrumPlanningInstanceNumber);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem", "Sprint", "Plan");
		
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		EClass plan = eClasses[4];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference planStakeholders = getEReferenceFromEClass(plan, "stakeholders");
		
		View fullView = new View(scrumPlanningInstanceReference);
		
		for (EClass eClass : eClasses) {
			fullView.extend(eClass);
		}
		
		View view = new View(scrumPlanningInstanceReference);

		// empty view
		view.extendByAllNodes();
		assertTrue(fullView.equals(view));
		
		// nodes only
		view.clear();
		view.extend(workitem);
		view.extend(stakeholder);
		
		view.extendByAllNodes();
		assertTrue(fullView.equals(view));
		
		// edges only
		
		fullView.extend(planStakeholders);
		fullView.extend(backlogWorkitems);
		
		view.clear();
		view.extend(planStakeholders);
		view.extend(backlogWorkitems);
		
		view.extendByAllNodes();
		assertTrue(fullView.equals(view));
		
		// nodes and edges
		view.clear();
		view.extend(workitem);
		view.extend(stakeholder);
		view.extend(planStakeholders);
		view.extend(backlogWorkitems);
		
		view.extendByAllNodes();
		assertTrue(fullView.equals(view));
		
	}
	
	/**
	 * Test method for {@link View#contains(Node)} with wrong input parameters.
	 */
	@Test
	final void testContainsNodeWrongInput() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class");
		EClass namedElement = eClasses[0];
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(name).equals("2"),
				eObject -> eObject.eGet(name).equals("5")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject attribute5 = mapOfSets.get(1).iterator().next();
		
		// get the Stakeholder and Backlog EClass from the metamodel
		eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem", "Sprint", "Plan");
		
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		EClass plan = eClasses[4];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference planStakeholders = getEReferenceFromEClass(plan, "stakeholders");
		
		View view = new View(CRA_INSTANCE_ONE);
		view.extend(method2);
		view.extend(attribute5);
		
		// empty view
		View copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(method2)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(attribute5)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(method2)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(attribute5)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// edges only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(planStakeholders);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(method2)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(attribute5)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(method2)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(view.getNode(attribute5)));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
	}
	
	/**
	 * Test method for {@link View#contains(Node)} with correct input parameters.
	 */
	@Test
	final void testContainsNodeCorrectInput() {
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class", "ClassModel");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		EClass classModelEClass = eClasses[2];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("2"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("3"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("5"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("8")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject method3 = mapOfSets.get(1).iterator().next();
		EObject attribute5 = mapOfSets.get(2).iterator().next();
		EObject class8 = mapOfSets.get(3).iterator().next();
		
		View view = new View(CRA_INSTANCE_ONE);
		
		// nodes only
		view.extend(method2);
		view.extend(method3);
		view.extend(attribute5);
		view.extend(class8);
		
		View copyOfView = view.copy();
		
		assertTrue(view.contains(view.getNode(method2)));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(view.getNode(method3)));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(view.getNode(attribute5)));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(view.getNode(class8)));
		assertTrue(copyOfView.equals(view));
		
		// edges only
		view.clear();
		view.extend(encapsulates);
		view.extend(classModelClasses);
		
		copyOfView = view.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			assertFalse(view.contains(edge.getSource()));
			assertTrue(copyOfView.equals(view));
			
			assertFalse(view.contains(edge.getTarget()));
			assertTrue(copyOfView.equals(view));
		}
		
		// nodes and edges
		view.clear();
		view.extend(encapsulates);
		view.extend(method2);
		view.extend(method3);
		view.extend(attribute5);
		copyOfView = view.copy();
		
		assertFalse(view.contains(view.graphMap.get(class8)));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(view.getNode(method2)));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(view.getNode(method3)));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(view.getNode(attribute5)));
		assertTrue(copyOfView.equals(view));

	}
	
	/**
	 * Test method for {@link View#contains(EObject)} with wrong input parameters.
	 */
	@Test
	final void testContainsEObjectWrongInput() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class");
		EClass namedElement = eClasses[0];
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(name).equals("2"),
				eObject -> eObject.eGet(name).equals("5")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject attribute5 = mapOfSets.get(1).iterator().next();
		
		// get the Stakeholder and Backlog EClass from the metamodel
		eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem", "Sprint", "Plan");
		
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		EClass plan = eClasses[4];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference planStakeholders = getEReferenceFromEClass(plan, "stakeholders");
		
		// empty view
		View copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		assertFalse(viewOnScrumPlanningInstanceOne.contains(method2));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(attribute5));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(method2));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(attribute5));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// edges only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(planStakeholders);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(method2));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(attribute5));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(method2));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(attribute5));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
	}
	
	/**
	 * Test method for {@link View#contains(EObject)} with correct input parameters.
	 */
	@Test
	final void testContainsEObjectCorrectInput() {
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class", "ClassModel");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		EClass classModelEClass = eClasses[2];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("2"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("3"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("5"),
				eObject -> !eObject.eClass().getName().equals("ClassModel") && eObject.eGet(name).equals("8")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject method3 = mapOfSets.get(1).iterator().next();
		EObject attribute5 = mapOfSets.get(2).iterator().next();
		EObject class8 = mapOfSets.get(3).iterator().next();
		
		View view = new View(CRA_INSTANCE_ONE);
		
		// nodes only
		view.extend(method2);
		view.extend(method3);
		view.extend(attribute5);
		view.extend(class8);
		
		View copyOfView = view.copy();
		
		assertTrue(view.contains(method2));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(method3));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(attribute5));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(class8));
		assertTrue(copyOfView.equals(view));
		
		// edges only
		view.clear();
		view.extend(encapsulates);
		view.extend(classModelClasses);
		
		copyOfView = view.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			assertFalse(view.contains(view.objectMap.get(edge.getSource())));
			assertTrue(copyOfView.equals(view));
			
			assertFalse(view.contains(view.objectMap.get(edge.getTarget())));
			assertTrue(copyOfView.equals(view));
		}
		
		// nodes and edges
		view.clear();
		view.extend(encapsulates);
		view.extend(method2);
		view.extend(method3);
		view.extend(attribute5);
		copyOfView = view.copy();
		
		assertFalse(view.contains(class8));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(method2));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(method3));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(attribute5));
		assertTrue(copyOfView.equals(view));
		
	}
	
	/**
	 * Test method for {@link View#contains(Edge, boolean)} with wrong input parameters.
	 */
	@Test
	final void testContainsEdgeBooleanWrongInput() {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class", "ClassModel");
		EClass classEClass = eClasses[1];
		EClass classModelEClass = eClasses[2];
		
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		
		// get the Stakeholder and Backlog EClass from the metamodel
		eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem", "Sprint", "Plan");
		
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		EClass plan = eClasses[4];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference planStakeholders = getEReferenceFromEClass(plan, "stakeholders");
		
		View view = new View(CRA_INSTANCE_ONE);
		view.extend(encapsulates);
		view.extend(classModelClasses);
		
		// empty view
		View copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, false));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
			
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, true));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		}
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, false));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
			
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, true));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		}
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// edges only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(planStakeholders);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, false));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
			
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, true));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		}
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, false));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
			
			assertFalse(viewOnScrumPlanningInstanceOne.contains(edge, true));
			assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		}

		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
	}
	
	/**
	 * Test method for {@link View#contains(Edge, boolean)} with correct input parameters.
	 */
	@Test
	final void testContainsEdgeBooleanCorrectInput() {
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class", "ClassModel");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		EClass classModelEClass = eClasses[2];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(name).equals("2"),
				eObject -> eObject.eGet(name).equals("3"),
				eObject -> eObject.eGet(name).equals("5"),
				eObject -> eObject.eGet(name).equals("8"),
				eObject -> eObject.eGet(name).equals("9")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject method3 = mapOfSets.get(1).iterator().next();
		EObject attribute5 = mapOfSets.get(2).iterator().next();
		EObject class9 = mapOfSets.get(4).iterator().next();
		
		View view = new View(CRA_INSTANCE_ONE);
		
		// edges only
		view.clear();
		view.extend(encapsulates);
		view.extend(classModelClasses);
		
		View copyOfView = view.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			assertFalse(view.contains(edge, false));
			assertTrue(copyOfView.equals(view));
			
			assertTrue(view.contains(edge, true));
			assertTrue(copyOfView.equals(view));
		}
		
		// nodes and edges
		view.clear();
		view.extend(encapsulates);
		view.extend(classModelClasses);
		view.extend(classModelEClass);
		view.extend(method2);
		view.extend(method3);
		view.extend(attribute5);
		view.extend(class9);
		copyOfView = view.copy();
		
		for (Edge edge : view.graph.getEdges()) {
			
			assertTrue(view.contains(edge, true));
			assertTrue(copyOfView.equals(view));
			
			if (view.objectMap.get(edge.getSource()) == class9 || 
					view.objectMap.get(edge.getTarget()) == class9) {
				assertTrue(view.contains(edge, false));
			} else {
				assertFalse(view.contains(edge, false));
			}
			
		}
		
	}
		
	/**
	 * Test method for {@link View#contains(EObject, EObject, EReference, boolean)} with wrong input parameters.
	 */
	@Test
	final void testContainsEObjectEObjectEReferenceBooleanWrongInputParemeters () {
		
		View viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class", "ClassModel");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		EClass classModelEClass = eClasses[2];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(name).equals("2"),
				eObject -> eObject.eGet(name).equals("3"),
				eObject -> eObject.eGet(name).equals("5"),
				eObject -> eObject.eGet(name).equals("8"),
				eObject -> eObject.eGet(name).equals("9"),
				eObject -> eObject.eGet(name).equals("1")
		);
		
		EObject attribute5 = mapOfSets.get(2).iterator().next();
		EObject class9 = mapOfSets.get(4).iterator().next();
		EObject classModel1 = mapOfSets.get(5).iterator().next();
		
		// get the Stakeholder and Backlog EClass from the metamodel
		eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem", "Sprint", "Plan");
		
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		EClass plan = eClasses[4];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference planStakeholders = getEReferenceFromEClass(plan, "stakeholders");
		
		// empty view
		View copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// edges only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(planStakeholders);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		
		copyOfViewOnScrumPlanningInstanceOne = viewOnScrumPlanningInstanceOne.copy();
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(class9, attribute5, encapsulates, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(classModel1, class9, classModelClasses, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, false));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
		assertFalse(viewOnScrumPlanningInstanceOne.contains(null, null, null, true));
		assertTrue(copyOfViewOnScrumPlanningInstanceOne.equals(viewOnScrumPlanningInstanceOne));
		
	}
	
	/**
	 * Test method for {@link View#contains(EObject, EObject, EReference, boolean)} with correct input parameters.
	 */
	@Test
	final void testContainsEObjectEObjectEReferenceBooleanCorrectInputParemeters () {
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "Class", "ClassModel");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		EClass classModelEClass = eClasses[2];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(name).equals("2"),
				eObject -> eObject.eGet(name).equals("3"),
				eObject -> eObject.eGet(name).equals("5"),
				eObject -> eObject.eGet(name).equals("8"),
				eObject -> eObject.eGet(name).equals("9"),
				eObject -> eObject.eGet(name).equals("1")
		);
		
		EObject method2 = mapOfSets.get(0).iterator().next();
		EObject method3 = mapOfSets.get(1).iterator().next();
		EObject attribute5 = mapOfSets.get(2).iterator().next();
		EObject class8 = mapOfSets.get(3).iterator().next();
		EObject class9 = mapOfSets.get(4).iterator().next();
		EObject classModel1 = mapOfSets.get(5).iterator().next();
		
		View view = new View(CRA_INSTANCE_ONE);
		
		// edges only
		view.clear();
		view.extend(encapsulates);
		view.extend(classModelClasses);
		
		View copyOfView = view.copy();
		
		assertFalse(view.contains(class9, attribute5, encapsulates, false));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(class9, attribute5, encapsulates, true));
		assertTrue(copyOfView.equals(view));
		
		assertFalse(view.contains(classModel1, class9, classModelClasses, false));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(classModel1, class9, classModelClasses, true));
		assertTrue(copyOfView.equals(view));
		
		// nodes and edges
		view.clear();
		view.extend(encapsulates);
		view.extend(classModelClasses);
		view.extend(classModelEClass);
		view.extend(method2);
		view.extend(method3);
		view.extend(attribute5);
		view.extend(class9);
		copyOfView = view.copy();
		
		assertTrue(view.contains(class9, attribute5, encapsulates, false));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(class9, attribute5, encapsulates, true));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(classModel1, class9, classModelClasses, false));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(classModel1, class9, classModelClasses, true));
		assertTrue(copyOfView.equals(view));
		
		assertFalse(view.contains(classModel1, class8, classModelClasses, false));
		assertTrue(copyOfView.equals(view));
		
		assertTrue(view.contains(classModel1, class8, classModelClasses, true));
		assertTrue(copyOfView.equals(view));
		
	}
	
	// multiple edges of different types between the same objects
	
	/**
	 * Tests the handling of multiple {@link Edge edges} of different {@link Edge#getType() types} between the
	 * same Objects.
	 */
	@Test
	final void testMultipleEdgesOfDifferentTypesBetweenTheSameObjects () {
		
		// get eClasses eObjects, eReferences and eAttributes
		
		EClass[] eClasses = getEClassFromResource(MULTI_REF_MODEL_EORE, "person", "item", "container");
		EClass personEClass = eClasses[0];
		EClass itemEClass = eClasses[1];
		EClass containerEClass = eClasses[2];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(personEClass, "name");
		EReference personOwns = getEReferenceFromEClass(personEClass, "owns");
		EReference personLeases = getEReferenceFromEClass(personEClass, "leases");
		EReference itemIsOwned = getEReferenceFromEClass(itemEClass, "isOwned");
		EReference itemIsLeased = getEReferenceFromEClass(itemEClass, "isLeased");
		
		Map<Integer, Set<EObject>> map = getEObjectsFromResource(MULTI_REF_MODEL_INSTANCE_ONE,
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("Steve"),
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("John"),
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("Car"),
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("House")
		);
		
		EObject stevePerson = map.get(0).iterator().next();
		EObject carItem = map.get(2).iterator().next();
		
		// init a View with two nodes
		
		View multiRefView = new View(MULTI_REF_MODEL_INSTANCE_ONE);
		multiRefView.extend(stevePerson);
		multiRefView.extend(carItem);
		
		// test adding edges
		
		assertTrue(multiRefView.extend(stevePerson, carItem, personOwns));
		
		View copyOfView = multiRefView.copy();
		assertFalse(multiRefView.extend(stevePerson, carItem, itemIsOwned));
		assertTrue(copyOfView.equals(multiRefView));
		
		assertContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		copyOfView = multiRefView.copy();
		assertFalse(multiRefView.extend(stevePerson, carItem, personOwns));
		assertTrue(copyOfView.equals(multiRefView));
		
		assertTrue(multiRefView.extend(stevePerson, carItem, personLeases));
		
		assertEquals(2, multiRefView.graph.getNodes().size());
		assertEquals(2, multiRefView.graph.getEdges().size());
		
		Set<EReference> actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		Set<EReference> expectedEdgeTypes = new HashSet<>(List.of(personOwns, personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		// test removing the edges by different methods
		
		View multiViewWithTwoEdges = multiRefView.copy();
		
		multiRefView.reduce(personOwns);
		
		assertEquals(2, multiRefView.graph.getNodes().size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		multiRefView.reduce(itemIsOwned);
		
		assertEquals(2, multiRefView.graph.getNodes().size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		assertTrue(multiRefView.reduce(stevePerson, carItem, personOwns));
		
		assertEquals(2, multiRefView.graph.getNodes().size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		assertTrue(multiRefView.reduce(carItem, stevePerson, itemIsOwned));
		
		assertEquals(2, multiRefView.graph.getNodes().size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		copyOfView = multiRefView.copy();
		assertFalse(multiRefView.reduce(stevePerson, carItem, itemIsOwned));
		assertTrue(copyOfView.equals(multiRefView));
		
		assertFalse(multiRefView.reduce(carItem, stevePerson, personOwns));
		assertTrue(copyOfView.equals(multiRefView));
		
	}
	
	private void assertContainsEdge (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference, EReference eOposite) {
		assertTrue(view.contains(eObjectOne, eObjectTwo, eReference, false));
		assertTrue(view.contains(eObjectOne, eObjectTwo, eReference, true));
		assertTrue(view.contains(eObjectTwo, eObjectOne, eReference, false));
		assertTrue(view.contains(eObjectTwo, eObjectOne, eReference, true));
		assertTrue(view.contains(eObjectOne, eObjectTwo, eOposite, false));
		assertTrue(view.contains(eObjectOne, eObjectTwo, eOposite, true));
		assertTrue(view.contains(eObjectTwo, eObjectOne, eOposite, false));
		assertTrue(view.contains(eObjectTwo, eObjectOne, eOposite, true));
	}
	
	private void assertNotContainsEdge (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference, EReference eOposite) {
		assertFalse(view.contains(eObjectOne, eObjectTwo, eReference, false));
		assertFalse(view.contains(eObjectOne, eObjectTwo, eReference, true));
		assertFalse(view.contains(eObjectTwo, eObjectOne, eReference, false));
		assertFalse(view.contains(eObjectTwo, eObjectOne, eReference, true));
		assertFalse(view.contains(eObjectOne, eObjectTwo, eOposite, false));
		assertFalse(view.contains(eObjectOne, eObjectTwo, eOposite, true));
		assertFalse(view.contains(eObjectTwo, eObjectOne, eOposite, false));
		assertFalse(view.contains(eObjectTwo, eObjectOne, eOposite, true));
	}
	
	/**
	 * Tests the handling of multiple dangling {@link Edge edges} of different {@link Edge#getType() types} between the
	 * same Objects.
	 */
	@Test
	final void testMultipleDanglingEdgesOfDifferentTypesBetweenTheSameObjects () {
		
		// get eClasses eObjects, eReferences and eAttributes
		
		EClass[] eClasses = getEClassFromResource(MULTI_REF_MODEL_EORE, "person", "item", "container");
		EClass personEClass = eClasses[0];
		EClass itemEClass = eClasses[1];
		EClass containerEClass = eClasses[2];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(personEClass, "name");
		EReference personOwns = getEReferenceFromEClass(personEClass, "owns");
		EReference personLeases = getEReferenceFromEClass(personEClass, "leases");
		EReference itemIsOwned = getEReferenceFromEClass(itemEClass, "isOwned");
		EReference itemIsLeased = getEReferenceFromEClass(itemEClass, "isLeased");
		
		Map<Integer, Set<EObject>> map = getEObjectsFromResource(MULTI_REF_MODEL_INSTANCE_ONE,
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("Steve"),
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("John"),
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("Car"),
				(eObject) -> eObject.eClass() != containerEClass && eObject.eGet(nameEAttribute).equals("House")
		);
		
		EObject stevePerson = map.get(0).iterator().next();
		EObject carItem = map.get(2).iterator().next();
		
		// init a View with one node
		
		View multiRefView = new View(MULTI_REF_MODEL_INSTANCE_ONE);
		multiRefView.extend(stevePerson);
		
		// test adding edges
		
		assertTrue(multiRefView.extend(stevePerson, carItem, personOwns));
		
		View copyOfView = multiRefView.copy();
		assertFalse(multiRefView.extend(stevePerson, carItem, itemIsOwned));
		assertTrue(copyOfView.equals(multiRefView));
		
		assertContainsDanglingEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		copyOfView = multiRefView.copy();
		assertFalse(multiRefView.extend(stevePerson, carItem, personOwns));
		assertTrue(copyOfView.equals(multiRefView));
		
		assertTrue(multiRefView.extend(stevePerson, carItem, personLeases));
		
		assertEquals(1, multiRefView.graph.getNodes().size());
		assertEquals(2, multiRefView.graphMap.size());
		assertEquals(2, multiRefView.objectMap.size());
		assertEquals(2, multiRefView.graph.getEdges().size());
		
		Set<EReference> actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		Set<EReference> expectedEdgeTypes = new HashSet<>(List.of(personOwns, personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertContainsDanglingEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsDanglingEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		// test removing the edges by different methods
		
		View multiViewWithTwoEdges = multiRefView.copy();
		
		multiRefView.reduce(personOwns);
		
		assertEquals(1, multiRefView.graph.getNodes().size());
		assertEquals(2, multiRefView.graphMap.size());
		assertEquals(2, multiRefView.objectMap.size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsDanglingEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		multiRefView.reduce(itemIsOwned);
		
		assertEquals(1, multiRefView.graph.getNodes().size());
		assertEquals(2, multiRefView.graphMap.size());
		assertEquals(2, multiRefView.objectMap.size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsDanglingEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		assertTrue(multiRefView.reduce(stevePerson, carItem, personOwns));
		
		assertEquals(1, multiRefView.graph.getNodes().size());
		assertEquals(2, multiRefView.graphMap.size());
		assertEquals(2, multiRefView.objectMap.size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsDanglingEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		assertTrue(multiRefView.reduce(carItem, stevePerson, itemIsOwned));
		
		assertEquals(1, multiRefView.graph.getNodes().size());
		assertEquals(2, multiRefView.graphMap.size());
		assertEquals(2, multiRefView.objectMap.size());
		assertEquals(1, multiRefView.graph.getEdges().size());
		
		actualEdgeTypes = multiRefView.graph.getEdges().stream().map(Edge::getType).collect(Collectors.toSet());
		expectedEdgeTypes = new HashSet<>(List.of(personLeases));
		assertEquals(expectedEdgeTypes, actualEdgeTypes);
		
		assertNotContainsEdge(multiRefView, stevePerson, carItem, personOwns, itemIsOwned);
		assertContainsDanglingEdge(multiRefView, stevePerson, carItem, personLeases, itemIsLeased);
		
		multiRefView = multiViewWithTwoEdges.copy();
		
		copyOfView = multiRefView.copy();
		assertFalse(multiRefView.reduce(stevePerson, carItem, itemIsOwned));
		assertTrue(copyOfView.equals(multiRefView));
		
		assertFalse(multiRefView.reduce(carItem, stevePerson, personOwns));
		assertTrue(copyOfView.equals(multiRefView));
		
	}
	
	private void assertContainsDanglingEdge (View view, EObject eObjectOne, EObject eObjectTwo, EReference eReference, EReference eOposite) {
		assertTrue(view.contains(eObjectOne, eObjectTwo, eReference, true));
		assertTrue(view.contains(eObjectTwo, eObjectOne, eReference, true));
		assertTrue(view.contains(eObjectOne, eObjectTwo, eOposite, true));
		assertTrue(view.contains(eObjectTwo, eObjectOne, eOposite, true));
		assertFalse(view.contains(eObjectOne, eObjectTwo, eReference, false));
		assertFalse(view.contains(eObjectTwo, eObjectOne, eReference, false));
		assertFalse(view.contains(eObjectOne, eObjectTwo, eOposite, false));
		assertFalse(view.contains(eObjectTwo, eObjectOne, eOposite, false));
	}
	
	// test handling of super classes
	
	/**
	 * Tests the {@link View#extend(EClass)} and {@link View#reduce(EClass)} methods on abstract supertypes.
	 */
	@Test
	final void testSuperTypes () {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		// EClass classModelEClass = eClasses[1];
		// EClass classEClass = eClasses[2];
		EClass featureEClass = eClasses[3];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		View craView = new View(CRA_INSTANCE_ONE);
		View fullCRAView = new View(CRA_INSTANCE_ONE);
		fullCRAView.extendByAllNodes();
		
		assertTrue(craView.extend(namedElementEClass));
		assertTrue(craView.equals(fullCRAView));
		
		View expectedReducedView = craView.copy();
		expectedReducedView.reduce(attributeEClass);
		expectedReducedView.reduce(methodEClass);
		
		assertTrue(craView.reduce(featureEClass));
		assertTrue(craView.equals(expectedReducedView));
		
	}
	
}
