/**
 * 
 */
package view;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
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
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Benjamin Wagner
 *
 */
class ViewTest {
	
	private static final String RESOURCE_PATH = "test/resources";
	private static Resource SCRUM_PLANNIG_ECORE;
	private static Resource SCRUM_PLANNIG_INSTANCE_ONE;
	private static Resource SCRUM_PLANNIG_INSTANCE_TWO;
	private static Resource CRA_ECORE;
	private static Resource CRA_INSTANCE_ONE;
	
	private View viewOnScrumPlanningInstanceOne;

	// setup
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		HenshinResourceSet resourceSet = new HenshinResourceSet(RESOURCE_PATH);
		SCRUM_PLANNIG_ECORE = resourceSet.getResource("scrumPlanning.ecore");
		SCRUM_PLANNIG_INSTANCE_ONE = resourceSet.getResource("scrumPlanningInstanceOne.xmi");
		SCRUM_PLANNIG_INSTANCE_TWO = resourceSet.getResource("scrumPlanningInstanceTwo.xmi");
		CRA_ECORE = resourceSet.getResource("CRA.ecore");
		CRA_INSTANCE_ONE = resourceSet.getResource("CRAInstanceOne.xmi");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		viewOnScrumPlanningInstanceOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
		viewOnScrumPlanningInstanceOne = null;
	}

	// helper methods and tests of the helper methods
	
	/**
	 * A helper method to get a {@link EReference eReference} fron an {@link EClass eClass} by name.
	 * If the given {@link EClass eClass} doesn't contain the named {@link EReference eReference} the test
	 * that called this method will fail.
	 * @param eClass the {@link EClass eClass} affiliated to the {@link EReference eReference}
	 * @param name the {@link EReference#getName() name} of the {@link EReference eReference}
	 * @return Returns the {@link EReference eReference}.
	 */
	final EReference getEReferenceFromEClass (EClass eClass, String name) {
		
		List<EReference> eReferences = eClass.getEAllReferences().stream().
				filter(eReference -> eReference.getName().equals(name)).
				collect(Collectors.toList());
		assertEquals(1, eReferences.size());
		
		return eReferences.get(0);
		
	}
	
	/**
	 * Test for the internal method {@link ViewTest#getEReferenceFromEClass(EClass, String)}.
	 */
	@Test
	final void testGetEReferenceFromEClass () {
		
		EClass classEClass = getEClassFromResource(CRA_ECORE, "Class")[0];
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		assertEquals(encapsulates.getName(), "encapsulates");
		assertEquals(encapsulates.getEContainingClass(), classEClass);
		
	}
	
	/**
	 * A helper method to get a {@link EAttribute eAttribute} fron an {@link EClass eClass} by name.
	 * If the given {@link EClass eClass} doesn't contain the named {@link EAttribute eAttribute} the test
	 * that called this method will fail.
	 * @param eClass the {@link EClass eClass} affiliated to the {@link EAttribute eAttribute}
	 * @param name the {@link EAttribute#getName() name} of the {@link EAttribute eAttribute}
	 * @return Returns the {@link EAttribute eAttribute}.
	 */
	final EAttribute getEAttributeFromEClass (EClass eClass, String name) {
		
		List<EAttribute> eAttributes = eClass.getEAllAttributes().stream().
				filter(eAttribute -> eAttribute.getName().equals(name)).
				collect(Collectors.toList());
		assertEquals(1, eAttributes.size());
		
		return eAttributes.get(0);
		
	}
	
	/**
	 * Test for the internal method {@link ViewTest#getEAttributeFromEClass(EClass, String)}.
	 */
	@Test
	final void testGetEAttributeFromEClass () {
		
		EClass namedElement = getEClassFromResource(CRA_ECORE, "NamedElement")[0];
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElement, "name");
		assertEquals(nameEAttribute.getName(), "name");
		assertEquals(nameEAttribute.getEContainingClass(), namedElement);
		
	}
	
	/**
	 * @param resource the {@link Resource} of the meta-model
	 * @param names the names of the eClasses to return
	 * @return Returns an array of the {@link EClass eClasses} corresponding to the given names.
	 */
	final EClass[] getEClassFromResource (Resource resource, String... names) {
		
		TreeIterator<EObject> treeIterator = resource.getAllContents();
		EClass[] eClasses = new EClass[names.length];
		
		while (treeIterator.hasNext()) {
			
			EObject eObject = (EObject) treeIterator.next();
			
			if (eObject instanceof EClass) {
				EClass eClass = (EClass) eObject;
				
				for (int i = 0; i < names.length; i++) {
					if (eClass.getName().equals(names[i]))
						eClasses[i] = eClass;
				}
			}
			
		}
		
		return eClasses;
		
	}
	
	/**
	 * Test for the internal method {@link ViewTest#getEClassFromResource(Resource, String...)}.
	 */
	@Test
	final void testGetEClassFromResource () {
		
		// get the expected stakeholder and the backlog eClasses from the meta-model
		EClass stakeholder = null;
		EClass backlog = null;
		TreeIterator<EObject> treeIterator = SCRUM_PLANNIG_ECORE.getAllContents();
		
		while (treeIterator.hasNext()) {
			
			EObject eObject = (EObject) treeIterator.next();
			
			if (eObject instanceof EClass) {
				EClass eClass = (EClass) eObject;
				
				if(eClass.getName().equals("Stakeholder")) {
					stakeholder = eClass;
				} else if (eClass.getName().equals("Backlog")) {
					backlog = eClass;
				}
			}
			
		}
		
		assertNotNull(stakeholder);
		assertNotNull(backlog);
		
		// get the actual eClasses from the method
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog");
		
		assertEquals(stakeholder, eClasses[0]);
		assertEquals(backlog, eClasses[1]);
		
	}
	
	/**
	 * A helper method to get {@link EObject eObjects} form a {@link Resource resource}.
	 * @param resource the {@link Resource} of the model
	 * @param predicates a list of predicates for evaluating the {@link EObject eObjects} to be returned 
	 * @return Returns a list of sets containing the {@link EObject eObjects} matched by the given predicates.
	 */
	@SafeVarargs
	final Map<Integer, Set<EObject>> getEObjectsFromResource (Resource resource, Predicate<EObject>... predicates) {
	
		TreeIterator<EObject> treeIterator = resource.getAllContents();
		Map<Integer, Set<EObject>> eObjectSets = new HashMap<Integer, Set<EObject>>();
		
		while (treeIterator.hasNext()) {
			
			EObject eObject = (EObject) treeIterator.next();
			
			for (int i = 0; i < predicates.length; i++) {
				try {
					if(predicates[i].test(eObject)) {
						if(!eObjectSets.containsKey(i)) 
							eObjectSets.put(i, new HashSet<EObject>());
						eObjectSets.get(i).add(eObject);
					}
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		for (int i = 0; i < predicates.length; i++) {
			if (eObjectSets.get(i) == null)
				eObjectSets.put(i, new HashSet<EObject>());
		}
		
		return eObjectSets;
		
	}

	/**
	 * Test for the internal method {@link ViewTest#getEObjectsFromResource(Resource, Predicate...)}.
	 */
	@Test
	final void testGetEObjectsFromResource () {
		
		TreeIterator<EObject> treeIterator = SCRUM_PLANNIG_INSTANCE_ONE.getAllContents();
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		assertNotNull(stakeholder);
		assertNotNull(backlog);
		
		Set<EObject> expectedStakeholderEObject = new HashSet<>();  
		Set<EObject> expectedBacklogEObjects = new HashSet<>();
		
		while (treeIterator.hasNext()) {
			
			EObject eObject = (EObject) treeIterator.next();
			
			if (eObject.eClass() == stakeholder) {
				expectedStakeholderEObject.add(eObject);
			}
			
			if (eObject.eClass() == backlog) {
				expectedBacklogEObjects.add(eObject);
			}
			
		}
		
		assertNotNull(stakeholder);
		assertNotNull(backlog);
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass() == stakeholder, 
				eObject -> eObject.eClass() == backlog
		);
		
		assertEquals(expectedStakeholderEObject, mapOfSets.get(0));
		assertEquals(expectedBacklogEObjects, mapOfSets.get(1));
		
	}
	
	// begin test methods
	
	/**
	 * Test method for {@link view.View#getNode(org.eclipse.emf.ecore.EObject)}.
	 */
	@Test
	final void testGetNode () {
		
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
		
		EClass attributeEClass = getEClassFromResource(CRA_ECORE, "Attribute")[0];
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testExtendEClassDifferentModelEmptyView (attributeEClass);
		
		testExtendEClassDifferentModelNonEmptyView (attributeEClass, backlogWorkitemsEReference, backlogEClass);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}
	 * with an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eClass an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassDifferentModelEmptyView (EClass eClass) {
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eClass));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View} with
	 * an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eClass an {@link EClass eClass} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReferenceFromModel an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassFromModel an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassDifferentModelNonEmptyView (EClass eClass, EReference eReferenceFromModel, EClass eClassFromModel) {
		
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
		
		// get the stakeholder and the backlog eClasses from the meta-model
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog");
		EClass stakeholder = eClasses[0];
		
		testExtendEClassSameModelEmptyView (stakeholder);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEClassSameModelEmptyViewTwice (stakeholder);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEClassSameModelEmptyView (eClasses);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEClassSameModelEmptyViewTwice (eClasses);
		viewOnScrumPlanningInstanceOne.clear();
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}.
	 * @param eClasses an array of {@link EClass eClasses} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassSameModelEmptyView (EClass... eClasses) {
		
		List<EClass> listOfEClasses = List.of(eClasses);
		
		for (EClass eClass : listOfEClasses) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		}
		
		Set<EObject> expedtedEObjects = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> listOfEClasses.contains(eObject.eClass())).get(0);
		
		assertEquals(expedtedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expedtedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		assertEquals(expedtedEObjects, viewOnScrumPlanningInstanceOne.graph.getNodes().stream().map(viewOnScrumPlanningInstanceOne::getObject).collect(Collectors.toSet()));
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getEdges().isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}
	 * with the same parameter twice. The second method call should not work and nothing should change.
	 * @param eClass an {@link EClass eClass} of the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEClassSameModelEmptyViewTwice (EClass... eClasses) {
		
		testExtendEClassSameModelEmptyView (eClasses);
		
		List<EClass> listOfEClasses = List.of(eClasses);
		
		for (EClass eClass : listOfEClasses) {
			assertFalse(viewOnScrumPlanningInstanceOne.extend(eClass));
		}
		
		
		Set<EObject> expedtedEObjects = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> listOfEClasses.contains(eObject.eClass())).get(0);
		
		assertEquals(expedtedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expedtedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		assertEquals(expedtedEObjects, viewOnScrumPlanningInstanceOne.graph.getNodes().stream().map(viewOnScrumPlanningInstanceOne::getObject).collect(Collectors.toSet()));
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getEdges().isEmpty());
		
	}

	// extend(EReference)
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EReference)} with
	 * an {@link EReference eReference} form a different model.
	 */
	@Test
	final void testExtendEReferenceDifferentModel () {
		
		EClass methodEClass = getEClassFromResource(CRA_ECORE, "Method")[0];
		EReference dataDependencyEReference = getEReferenceFromEClass(methodEClass, "dataDependency");
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testExtendEReferenceDifferentModelEmptyView (dataDependencyEReference);
		
		testExtendEReferenceDifferentModelNonEmptyView (dataDependencyEReference, backlogWorkitemsEReference, backlogEClass);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method on an empty {@link View}
	 * with an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eReference an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEReferenceDifferentModelEmptyView (EReference eReference) {
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eReference));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method on an empty {@link View} with
	 * an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model.
	 * @param eReference an {@link EReference eReference} that is not from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReferenceFromModel an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassFromModel an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testExtendEReferenceDifferentModelNonEmptyView (EReference eReference, EReference eReferenceFromModel, EClass eClassFromModel) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReferenceFromModel));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClassFromModel));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EReference)} with
	 * {@link EReference eReferences} form a the same model.
	 */
	@Test
	final void testExtendEReferenceSameModel () {
		
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
		
		testExtendEReferenceSameModelEmptyView(backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodes(backlogWorkitemsEReference, backlog);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodes(backlogWorkitemsEReference, stakeholderEObjects.toArray(new EObject[] {}));
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewEmptyViewTwice(backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodesTwice(backlogWorkitemsEReference, backlog);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEReferenceSameModelViewWithNodesTwice(backlogWorkitemsEReference, stakeholderEObjects.toArray(new EObject[] {}));
		
	}

	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with some of the nodes already inserted.
	 * @param eReference an {@link EReference eReference} from the SCRUM_PLANNIG_ECORE meta-model to extend by
	 * @param eObjects an array of {@link EObject eObjects} form the SCRUM_PLANNIG_INSTANCE_ONE to fill the {@link View} with
	 */
	private void testExtendEReferenceSameModelViewWithNodes(EReference eReference, EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eObject));
		}
		
		// extend by the reference
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjects),
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet())
		);
		
		// test nodes from the maps
		Set<EObject> allExpectedEObjects = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> {
			boolean eObjectIsOfContainingEClass = eReference.getEContainingClass() == eObject.eClass();
			boolean eObjectIsOfReferencedType = eReference.getEReferenceType() == eObject.eClass();
			return eObjectIsOfContainingEClass | eObjectIsOfReferencedType;
		}).get(0);
		
		allExpectedEObjects.addAll(List.of(eObjects));
		
		Set<EObject> actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> {
			
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
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
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
	private void testExtendEReferenceSameModelViewWithNodesTwice (EReference eReference, EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eObject));
		}
		
		// extend by the reference
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjects),
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet())
		);
		
		// test nodes from the maps
		Set<EObject> allExpectedEObjects = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> {
			boolean eObjectIsOfContainingEClass = eReference.getEContainingClass() == eObject.eClass();
			boolean eObjectIsOfReferencedType = eReference.getEReferenceType() == eObject.eClass();
			return eObjectIsOfContainingEClass | eObjectIsOfReferencedType;
		}).get(0);
		
		allExpectedEObjects.addAll(List.of(eObjects));
		
		Set<EObject> actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> {
			
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
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
		// the second time
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjects),
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet())
		);
		
		// test nodes from the maps
		actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		actualSourceEObjects = new HashSet<EObject>();
		actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
	}
	
	/**
     * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with an an empty {@link View}.
     * No {@link Node nodes} should be added to the {@link view.View#graph graph} by {@link view.View#extend(org.eclipse.emf.ecore.EReference)}.
	 * @param eReference the {@link EReference eReference} from the SCRUM_PLANNIG_ECORE meta-model used to extend the {@link View}.
	 */
	private void testExtendEReferenceSameModelEmptyView (EReference eReference) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		// test nodes from the graph
		assertEquals(0, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		
		// test nodes from the maps
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> {
			
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
		
		Set<EObject> actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> actualSourceEObjects = new HashSet<EObject>();
		Set<EObject> actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
	}
	
	/**
     * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with an an empty {@link View} adding
     * the same parameter twice the second time shouldn't work and nothing should change.
	 * @param eReference the {@link EReference eReference} from the SCRUM_PLANNIG_ECORE meta-model used to extend the {@link View}.
	 */
	private void testExtendEReferenceSameModelViewEmptyViewTwice (EReference eReference) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		// test nodes from the graph
		assertEquals(0, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		
		// test nodes from the maps
		Set<EObject> expectedEObjectsWithTheEReference = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, eObject -> {
			
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
		
		Set<EObject> actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		Set<EObject> actualSourceEObjects = new HashSet<EObject>();
		Set<EObject> actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(expectedEObjectsWithTheEReference, actualSourceEObjects);
		assertEquals(expectedReferencedEObjects, actualTargetEObjects);
		
		// add  the second time
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		// test nodes from the graph
		assertEquals(0, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		
		// test nodes from the maps
		actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		actualSourceEObjects = new HashSet<EObject>();
		actualTargetEObjects = new HashSet<EObject>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(eReference, edge.getType());
			actualSourceEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
			actualTargetEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
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
		
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EClass planEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		EObject[] methodEObjects = getEObjectsFromResource (
				CRA_INSTANCE_ONE, eObject -> eObject.eClass().getName().equals("Method")
				).get(0).toArray(new EObject[] {});
		
		testExtendEObjectDifferentModelEmptyView (methodEObjects);
		
		testExtendEObjectDifferentModelNonEmptyView (backlogEClass, backlogWorkitemsEReference, methodEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectDifferentModelNonEmptyView (planEClass, backlogWorkitemsEReference, methodEObjects);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an empty {@link View}
	 * with {@link EObject eObjects} that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model.
	 * @param eObjects an array of {@link EObject eObjects} that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectDifferentModelEmptyView (EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertFalse(viewOnScrumPlanningInstanceOne.extend(eObject));
		}
		
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an {@link View} extended 
	 * by the given {@link EClass eClass} and {@link EReference eReference} with {@link EObject eObjects}
	 * that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model.
	 * @param eClass an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReference an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObjects an array of {@link EObject eObjects} that are not from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectDifferentModelNonEmptyView (EClass eClass, EReference eReference, EObject... eObjects) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		for (EObject eObject : eObjects) {
			assertFalse(viewOnScrumPlanningInstanceOne.extend(eObject));
		}
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject)} with
	 * {@link EObject eObjects} form the same model.
	 */
	@Test
	final void testExtendEObjectSameModel () {
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("Stakeholder"));
		EObject backlogEObject = mapOfSets.get(0).iterator().next();
		EObject[] stakeholderEObjects = mapOfSets.get(1).toArray(new EObject[] {});
		EObject[] stakeholderAndBacklogEObjects = new EObject[] {backlogEObject, stakeholderEObjects[0], stakeholderEObjects[1]};
		
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EClass planEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testExtendEObjectSameModelEmptyView (backlogEObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyView (stakeholderEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyView (stakeholderAndBacklogEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyViewTwice (backlogEObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelEmptyViewTwice (stakeholderEObjects[0]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (backlogEClass, backlogWorkitemsEReference, stakeholderEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (planEClass, backlogWorkitemsEReference, stakeholderEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (planEClass, backlogWorkitemsEReference, backlogEObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyView (planEClass, backlogWorkitemsEReference, stakeholderAndBacklogEObjects);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyViewTwice (backlogEClass, backlogWorkitemsEReference, stakeholderEObjects[0]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyViewTwice (planEClass, backlogWorkitemsEReference, stakeholderEObjects[0]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectSameModelNonEmptyViewTwice (planEClass, backlogWorkitemsEReference, backlogEObject);
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an empty {@link View}
	 * with {@link EObject eObjects} that are from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model.
	 * @param eObjects an array of {@link EObject eObjects} that are from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectSameModelEmptyView (EObject... eObjects) {
		
		for (EObject eObject : eObjects) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eObject));
		}
		
		// nodes from the graph
		Set<EObject> actualEObjectsFromGraph = viewOnScrumPlanningInstanceOne.graph.getNodes().
				stream().map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		Set<EObject> expectedEObjects = new HashSet<>(List.of(eObjects));
		
		assertEquals(expectedEObjects, actualEObjectsFromGraph);
		
		// nodes from the maps
		assertEquals(expectedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// edges
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getEdges().isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on an empty {@link View}
	 * with an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model twice. The second time
	 * shouldn't work and nothing should change.
	 * @param eObject an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectSameModelEmptyViewTwice (EObject eObject) {
		
		testExtendEObjectSameModelEmptyView(eObject);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		// add the same eObject again
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eObject));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
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
	private void testExtendEObjectSameModelNonEmptyView (EClass eClass, EReference eReference, EObject... eObjects) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		
		Set<EObject> expectedEObjectsFromGraph = viewOnScrumPlanningInstanceOne.graph.getNodes().
				stream().map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		List<Edge> expectedEdges = new ArrayList<Edge> (viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		List<EObject> listOfEObjects = List.of(eObjects);
		expectedEObjectsFromGraph.addAll(listOfEObjects);
		expectedGraphMapSet.addAll(listOfEObjects);
		expectedObjectMapSet.addAll(listOfEObjects);
		
		for (EObject eObject : eObjects) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eObject));
		}
		
		// nodes from the graph
		Set<EObject> actualEObjectsFromGraph = viewOnScrumPlanningInstanceOne.graph.getNodes().
				stream().map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		assertEquals(expectedEObjectsFromGraph, actualEObjectsFromGraph);
		
		// nodes from the maps
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// edges
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject)} method on a {@link View} extended 
	 * by the given {@link EClass eClass} and {@link EReference eReference} with an {@link EObject eObject} 
	 * from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model twice. The second time shouldn't work and nothing should change.
	 * @param eClass an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReference an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObject an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_TWO} model
	 */
	private void testExtendEObjectSameModelNonEmptyViewTwice (EClass eClass, EReference eReference, EObject eObject) {
		
		testExtendEObjectSameModelNonEmptyView(eClass, eReference, eObject);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		// add the same eObject again
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eObject));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	// extend(EObject, EObject, EReference)
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 */
	@Test
	final void testExtendEObjectEObjectEReferenceSameModel () {
		
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
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (backlogEObject, stakeholderTwo, stakeholderWorkitemsEReference);
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (workitemsOfStakeholderOne.get(0), stakeholderOne, stakeholderWorkitemsEReference);
		
		testExtendEObjectEObjectEReferenceSameModelEmptyView (new EObject[] {backlogEObject}, new EObject[] {workitemEObjects[0]}, 
				new EReference[] {backlogWorkitemsEReference});
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelEmptyViewTwice (backlogEObject, workitemEObjects[0], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelEmptyView (
				new EObject[] {backlogEObject, backlogEObject, backlogEObject, backlogEObject}, 
				workitemEObjects, 
				new EReference[] {backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference}
		);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyView (new EObject[] {backlogEObject}, new EObject[] {workitemEObjects[0]}, 
				new EReference[] {backlogWorkitemsEReference}, new EObject[] {backlogEObject, workitemEObjects[0]});
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyViewTwice(backlogEObject, workitemEObjects[0], 
				backlogWorkitemsEReference, new EObject[] {backlogEObject, workitemEObjects[0]});
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyView (
				new EObject[] {backlogEObject, backlogEObject, backlogEObject, backlogEObject}, 
				workitemEObjects, 
				new EReference[] {backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference, backlogWorkitemsEReference},
				new EObject[] {backlogEObject, workitemEObjects[0]}
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
	private  void testExtendEObjectEObjectEReferenceSameModelEmptyView (EObject[] eObjectsOne, EObject[] eObjectsTwo, EReference[] eReferences) {
		
		assertTrue(eObjectsOne.length == eObjectsTwo.length && eObjectsTwo.length == eReferences.length);
		
		for (int i = 0; i < eReferences.length; i++) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		// test nodes from graph
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getNodes().isEmpty());
		
		// test maps
		Set<EObject> expectedEObjects = new HashSet<EObject>(List.of(eObjectsOne));
		expectedEObjects.addAll(List.of(eObjectsTwo));
		assertEquals(expectedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// test edges from graph
		List<Set<EObject>> listOfEObjectSets = new ArrayList<Set<EObject>>();
		
		for (int i = 0; i < eObjectsOne.length; i++) {
			listOfEObjectSets.add(Set.of(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		Set<EObject> actualEdgeNodeEObjects = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			
			EObject sourceEObject = viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource());
			EObject targetEObject = viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget());
			
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
	private  void testExtendEObjectEObjectEReferenceSameModelNonEmptyView (EObject[] eObjectsOne, EObject[] eObjectsTwo, EReference[] eReferences, EObject[] eObjectsThree) {
		
		assertTrue(eObjectsOne.length == eObjectsTwo.length && eObjectsTwo.length == eReferences.length);
		
		for (int i = 0; i < eObjectsThree.length; i++) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eObjectsThree[i]));
		}
		
		
		for (int i = 0; i < eReferences.length; i++) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		// test maps
		Set<EObject> expectedEObjects = new HashSet<EObject>(List.of(eObjectsOne));
		expectedEObjects.addAll(List.of(eObjectsTwo));
		expectedEObjects.addAll(List.of(eObjectsThree));
		assertEquals(expectedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// test nodes from graph
		assertEquals (
				Set.of(eObjectsThree),
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
				map(viewOnScrumPlanningInstanceOne.objectMap::get).
				collect(Collectors.toSet())
		);
		

		// test edges from graph
		List<Set<EObject>> listOfEObjectSets = new ArrayList<Set<EObject>>();
		
		for (int i = 0; i < eObjectsOne.length; i++) {
			listOfEObjectSets.add(Set.of(eObjectsOne[i], eObjectsTwo[i], eReferences[i]));
		}
		
		Set<EObject> actualEdgeNodeEObjects = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			
			EObject sourceEObject = viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource());
			EObject targetEObject = viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget());
			
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
	private  void testExtendEObjectEObjectEReferenceSameModelNonEmptyViewTwice (EObject eObjectOne, EObject eObjectTwo, EReference eReference, EObject[] eObjectsThree) {
		
		testExtendEObjectEObjectEReferenceSameModelNonEmptyView (new EObject[] {eObjectOne}, new EObject[] {eObjectTwo}, new EReference[] {eReference}, eObjectsThree);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eObjectOne, eObjectTwo, eReference));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters from the same model that match the required conditions on an empty {@link View}.
	 * Inserting the same parameters twice shouln't work and nothing should change.
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReference an {@link EReference eReference}
	 */
	private  void testExtendEObjectEObjectEReferenceSameModelEmptyViewTwice (EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		testExtendEObjectEObjectEReferenceSameModelEmptyView (new EObject[] {eObjectOne}, new EObject[] {eObjectTwo}, new EReference[] {eReference});
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eObjectOne, eObjectTwo, eReference));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * with input parameters from a different model. 
	 */
	@Test
	final public void testExtendEObjectEObjectEReferenceDifferentModel () {
		
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
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (method3, attribute5, encapsulates);
		
		testExtendEObjectEObjectEReferenceEmptyViewWrongInput (class8, method2, encapsulates);
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (backlogEClass, backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (backlogEClass, backlogWorkitemsEReference, class8, method2, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (planEClass, backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (planEClass, backlogWorkitemsEReference, class8, method2, encapsulates);

	}
	
	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * method with input parameters that don't match the required conditions on an empty {@link View}.
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReference an {@link EReference eReference}
	 */
	private void testExtendEObjectEObjectEReferenceEmptyViewWrongInput (EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
	
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eObjectOne, eObjectTwo, eReference));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
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
	private void testExtendEObjectEObjectEReferenceNonEmptyViewWrongInput (EClass eClass, EReference eReferenceOne,
			EObject eObjectOne, EObject eObjectTwo, EReference eReferenceTwo) {
	
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReferenceOne));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(eObjectOne, eObjectTwo, eReferenceTwo));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	// reduce(EClass)
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} with
	 * an {@link EClass eClass} form a different model.
	 */
	@Test
	final void testReduceEClassDifferentModel () {
		
		EClass attributeEClass = getEClassFromResource(CRA_ECORE, "Attribute")[0];
		EClass backlogEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Backlog")[0];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		
		testReduceEClassEmptyView (attributeEClass);
		
		testReduceEClassDifferentModelNonEmptyView (attributeEClass, backlogEClass, backlogWorkitemsEReference);
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method on an empty {@link View}.
	 * @param eClass an {@link EClass eClass}
	 */
	private void testReduceEClassEmptyView (EClass eClass) {
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eClass));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an {@link View}
	 * {@link view.View#extend(EClass) extended by} the given {@link EClass eClass} with an
	 * {@link EClass eClass} from a different model.
	 * @param eClassToReduce an {@link EClass eClass} that is not from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClassToExtend an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eReferenceToExtend an {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testReduceEClassDifferentModelNonEmptyView (EClass eClassToReduce, EClass eClassToExtend, EReference eReferenceToExtend) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClassToExtend));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReferenceToExtend));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eClassToReduce));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
			
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} with
	 * {@link EClass eClasses} form the same model.
	 */
	@Test
	final void testReduceEClassSameModel () {
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		
		testReduceEClassSameModelIdempotence(workitem);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		testReduceEClassSameModelIdempotence(workitem);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEClassSameModelNodesInView(new EClass[] {stakeholder}, new EClass[] {backlog, workitem});
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEClassSameModelNodesInView(new EClass[] {stakeholder, backlog}, new EClass[] {workitem});
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEClassSameModelEdgesInView(new EClass[] {backlog}, new EReference[] {backlogWorkitems}, new EClass[] {workitem});
		
	}

	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method with edges in
	 * the view that reference {@link EObject eObjects} that are to be removed by the method.
	 * In this case the {@link EObject eObjects} should be removed from the {@link view.View#graph graph}
	 * but <b>not</b> from the maps (i.e {@link view.View#graphMap graphMap} and {@link view.View#objectMap objectMap}).
	 * @param eClassesToExtend the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param eClassesToReduce the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param eReferencesToExtend the {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} 
	 * between {@link EClass eClassesToExtend} and {@link EClass eClassesToReduce}
	 */
	private void testReduceEClassSameModelEdgesInView (EClass[] eClassesToExtend, EReference[] eReferencesToExtend, EClass[] eClassesToReduce) {
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		}
		
		for (EClass eClass : eClassesToExtend) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		}
		
		for (EReference eReference : eReferencesToExtend) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		}
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(viewOnScrumPlanningInstanceOne.reduce(eClass));
		}
		
		// test maps
		
		HashSet<EClass> eClassesToExtendHashSet = new HashSet<EClass>(List.of(eClassesToExtend));
		HashSet<EClass> eClassesToReduceHashSet = new HashSet<EClass>(List.of(eClassesToReduce));
		
		Set<EObject> expectedGraphEObjects = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eClassesToExtendHashSet.contains(eObject.eClass()) && !eClassesToReduceHashSet.contains(eObject.eClass())
		).get(0);
		
		HashSet<EClass> eClassesHashSetOfEReferences = new HashSet<EClass>();
		eClassesHashSetOfEReferences.addAll(List.of(eReferencesToExtend).stream().map(EReference::getEContainingClass).collect(Collectors.toList()));
		eClassesHashSetOfEReferences.addAll(List.of(eReferencesToExtend).stream().map(EReference::getEReferenceType).collect(Collectors.toList()));
		
		Set<EObject> eReferenceEObjects = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE,
				eObject -> eClassesHashSetOfEReferences.contains(eObject.eClass())
		).get(0);
		
		Set<EObject> expectedMapEObjects = new HashSet<>(expectedGraphEObjects);
		expectedMapEObjects.addAll(eReferenceEObjects);
		
		assertEquals(expectedMapEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedMapEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// test graph nodes
		
		assertEquals(
				expectedGraphEObjects, 
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
				map(viewOnScrumPlanningInstanceOne.objectMap::get).
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
	private void testReduceEClassSameModelIdempotence (EClass eClass) {
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(eClass));

		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method with no edges in the view.
	 * In this case the {@link EObject eObjects} that are to be removed should also be removed from the
	 * maps (i.e {@link view.View#graphMap graphMap} and {@link view.View#objectMap objectMap}).
	 * @param eClassesToExtend an array of {@link EClass eClasses} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param eClassesToReduce an array of {@link EClass eClasses} from the {@code SCRUM_PLANNIG_ECORE}
	 */
	private void testReduceEClassSameModelNodesInView (EClass[] eClassesToExtend, EClass[] eClassesToReduce) {
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		}
		
		for (EClass eClass : eClassesToExtend) {
			assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		}
		
		for (EClass eClass : eClassesToReduce) {
			assertTrue(viewOnScrumPlanningInstanceOne.reduce(eClass));
		}
		
		HashSet<EClass> eClassesToExtendHashSet = new HashSet<>(List.of(eClassesToExtend));
		Set<EObject> expectedEObjects = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eClassesToExtendHashSet.contains(eObject.eClass())).get(0);
		
		// nodes in maps
		assertEquals(expectedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// nodes in graph
		assertEquals (
				expectedEObjects, 
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
				map(viewOnScrumPlanningInstanceOne.objectMap::get).
				collect(Collectors.toSet())
		);
		
		// test edges
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getEdges().isEmpty());

	}
	
	// reduce(EReference)
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} with
	 * an {@link EReference eReference} from a different model.
	 */
	@Test
	final void testReduceEReferenceDifferentModel () {
		
		EClass workitemEClass = getEClassFromResource(SCRUM_PLANNIG_ECORE, "WorkItem")[0];
		EClass classEClass = getEClassFromResource(CRA_ECORE, "Class")[0];
		EReference encapsulatesEReference = getEReferenceFromEClass(classEClass, "encapsulates");
		
		testReduceEReferenceEmptyView (encapsulatesEReference);
		
		testReduceEReferenceDifferentModelNonEmptyView (encapsulatesEReference, workitemEClass);
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an empty {@link View}.
	 * @param eReference an {@link EReference eReference}.
	 */
	private void testReduceEReferenceEmptyView (EReference eReference) {
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eReference));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an {@link View}
	 * {@link view.View#extend(EClass) extended by} the given {@link EClass eClass} with an
	 * {@link EReference eReference} from a different model.
	 * @param eReference an {@link EReference eReference} that is not from the  {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eClass an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 */
	private void testReduceEReferenceDifferentModelNonEmptyView (EReference eReference, EClass eClass) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClass));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eReference));
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getEdges().isEmpty());
		assertFalse(viewOnScrumPlanningInstanceOne.graph.getNodes().isEmpty());
		assertFalse(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertFalse(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
			
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} with
	 * {@link EReference eReferences} from the same model.
	 */
	@Test
	final void testReduceEReferenceSameModel () {
		
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "WorkItem", "Backlog", "Stakeholder");
		EClass workitemEClass = eClasses[0];
		EClass backlogEClass = eClasses[1];
		EClass stakeholder = eClasses[2];
		EReference backlogWorkitemsEReference = getEReferenceFromEClass(backlogEClass, "workitems");
		EReference stakeholderWorkitemsEReference = getEReferenceFromEClass(stakeholder, "workitems");
		
		testReduceEReferenceEmptyView(backlogWorkitemsEReference);
		
		testReduceEReferenceSameModelNoMatchingEdges(backlogWorkitemsEReference, workitemEClass, backlogEClass, stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNoMatchingEdges(stakeholderWorkitemsEReference, workitemEClass, backlogEClass, backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelIdempotence(stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testReduceEReferenceSameModelIdempotence(backlogWorkitemsEReference);
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
	private void testReduceEReferenceSameModelNoMatchingEdges (EReference eReferenceToExtend, EClass eClassToExtendOne, EClass eClassToExtendTwo, EReference eReferenceToReduce) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReferenceToExtend));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClassToExtendOne));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClassToExtendTwo));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eReferenceToReduce));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EReference)} method on an empty {@link View}
	 * by {@link view.View#extend(EReference) extending by} the given eReference and {@link view.View#reduce(EReference) reducing} again.
	 * @param eReference the {@link EReference eReference} to use
	 */
	private void testReduceEReferenceSameModelIdempotence (EReference eReference) {
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReference));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(eReference));

		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}

	// reduce(EObject)
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} with 
	 * parameters from a different model.
	 */
	@Test
	final void testReduceEObjectDifferentModel () {
		
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
		
		testReduceEObjectEmptyView (method2EObject);
		
		testReduceEObjectEmptyView (attribute5EObject);
		
		testReduceEObjectDifferentModelNonEmptyView (workitemEClass, method2EObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectDifferentModelNonEmptyView (workitemEClass, attribute5EObject);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectDifferentModelNonEmptyView (workitemEClass, attribute5EObject);
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)}.
	 * Tries to reduce an empty {@link View} this should always fail and change nothing.
	 * @param eObject an {@link EObject eObject} to try to reduce by
	 */
	private void testReduceEObjectEmptyView (EObject eObject) {
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eObject));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)}.
	 * Extends the {@link View} by the given {@link EObject eClassToExtend} and then tries
	 * to reduce by the given {@link EObject eObjectToReduce}. The reducing should not work,
	 * as the {@link EObject eObjectToReduce} is not from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model.
	 * @param eClassToExtend an {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE} meta-model
	 * @param eObjectToReduce an {@link EObject eObject} that is not from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 */
	private void testReduceEObjectDifferentModelNonEmptyView (EClass eClassToExtend, EObject eObjectToReduce) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eClassToExtend));
		
		List<Node> expectedNodes = new ArrayList<>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eObjectToReduce));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getEdges().isEmpty());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}

	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} with 
	 * parameters from the same model.
	 */
	@Test
	final void testReduceEObjectSameModel () {
		
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
			testReduceEObjectEmptyView (eObject);
		}
		
		for (EObject eObject : stakeholderAndBacklogEObjects) {
			testReduceEReferenceSameModelIdempotence (eObject);
		}
		
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		for (EObject eObject : stakeholderAndBacklogEObjects) {
			testReduceEReferenceSameModelIdempotence (eObject);
		}
		
		viewOnScrumPlanningInstanceOne.extend(workitemClass);
		for (EObject eObject : stakeholderAndBacklogEObjects) {
			testReduceEReferenceSameModelIdempotence (eObject);
		}
		
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(backlogEObject, workitemEObjects[0], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(backlogEObject, workitemEObjects[1], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(backlogEObject, workitemEObjects[2], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyView(backlogEObject, workitemEObjects[3], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(backlogEObject, workitemEObjects[0], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(backlogEObject, workitemEObjects[1], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(backlogEObject, workitemEObjects[2], backlogWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEReferenceSameModelNonEmptyViewTwice(backlogEObject, workitemEObjects[3], backlogWorkitemsEReference);
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
	private void testReduceEReferenceSameModelNonEmptyView (EObject eObjectToReduce, EObject eObjectToExtend, EReference eReferenceToExtend) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eObjectToExtend));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eObjectToReduce));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eObjectToReduce, eObjectToExtend, eReferenceToExtend));
		
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(eObjectToReduce));
		
		// test nodes in the map
		Set<EObject> expedtedEObjects = new HashSet<>();
		expedtedEObjects.add(eObjectToReduce);
		expedtedEObjects.add(eObjectToExtend);
		
		assertEquals(expedtedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expedtedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// test nodes in the graph
		assertEquals (
				Set.of(eObjectToExtend),
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet())
		);
		
		// test edges
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		Edge edge = viewOnScrumPlanningInstanceOne.graph.getEdges().get(0);
		assertEquals(edge.getType(), eReferenceToExtend);
		
		HashSet<EObject> actualEObjects = new HashSet<>();
		actualEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
		actualEObjects.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
		
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
	private void testReduceEReferenceSameModelNonEmptyViewTwice (EObject eObjectToReduce, EObject eObjectToExtend, EReference eReferenceToExtend) {
		
		testReduceEReferenceSameModelNonEmptyView (eObjectToReduce, eObjectToExtend, eReferenceToExtend);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eObjectToReduce));
	
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EObject)} method
	 * by {@link view.View#extend(EObject) extending by} the given {@link EObject eObject} and {@link view.View#reduce(EObject) reducing} again.
	 * The method does not require an empty {@link View} as it only tests of the {@link View} remains the same.
	 * @param eObject an {@link EObject eObject} from the {@code SCRUM_PLANNIG_INSTANCE_ONE} model
	 */
	private void testReduceEReferenceSameModelIdempotence (EObject eObject) {
		
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
		
		testReduceEObjectEObjectEReferenceEmptyView (method3, attribute5, encapsulates);
		
		testReduceEObjectEObjectEReferenceEmptyView (class8, method2, encapsulates);
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (backlogWorkitemsEReference, class8, method2, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (backlogWorkitemsEReference, method3, attribute5, encapsulates);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (backlogWorkitemsEReference, class8, method2, encapsulates);
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 * Tries to reduce an empty {@link View} by the {@link Edge edge} defined by
	 * the given parameters. This should always fail and change nothing.
	 * @param eObjectOne an {@link EObject eObject}
	 * @param eObjectTwo an {@link EObject eObject}
	 * @param eReference an {@link EReference eReference}
	 */
	private void testReduceEObjectEObjectEReferenceEmptyView (EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eObjectOne, eObjectTwo, eReference));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
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
	private void testReduceEObjectEObjectEReferenceDifferentModelNonEmptyView (EReference eReferenceToExtend, EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReferenceToExtend));
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eObjectOne, eObjectTwo, eReference));
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)} with
	 * parameters from the same model.
	 */
	@Test
	final void testReduceEObjectEObjectEReferenceSameModel() {
		
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
		
		testReduceEObjectEObjectEReferenceEmptyView(stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		testReduceEObjectEObjectEReferenceEmptyView(stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		
		testReduceEObjectEObjectEReferenceSameModelIdempotence(stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		testReduceEObjectEObjectEReferenceSameModelIdempotence(stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		testReduceEObjectEObjectEReferenceSameModelIdempotence(stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(workitemsOfStakeholderOne.get(0));
		testReduceEObjectEObjectEReferenceSameModelIdempotence(stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		testReduceEObjectEObjectEReferenceSameModelIdempotence(stakeholderOne, workitemsOfStakeholderOne.get(0), stakeholderWorkitemsEReference);
		
		viewOnScrumPlanningInstanceOne.extend(workitemsOfStakeholderOne.get(1));
		testReduceEObjectEObjectEReferenceSameModelIdempotence(stakeholderOne, workitemsOfStakeholderOne.get(1), stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyView (stakeholderWorkitemsEReference, stakeholderOne, workitemsOfStakeholderOne.get(1));
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyView (backlogWorkitemsEReference, backlogEObject, workitemEObjects[3]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyViewTwice (backlogWorkitemsEReference, backlogEObject, workitemEObjects[3]);
		viewOnScrumPlanningInstanceOne.clear();
		
		testReduceEObjectEObjectEReferenceSameModelNonEmptyViewTwice (stakeholderWorkitemsEReference, stakeholderOne, workitemsOfStakeholderOne.get(1));
		
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
	private void testReduceEObjectEObjectEReferenceSameModelIdempotence (EObject eObjectOne, EObject eObjectTwo, EReference eReference) {
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eObjectOne, eObjectTwo, eReference));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(eObjectOne, eObjectTwo, eReference));

		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
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
	private void testReduceEObjectEObjectEReferenceSameModelNonEmptyView (EReference eReferenceToExtend, EObject eObjectToReduceOne, EObject eObjectToReduceTwo) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(eReferenceToExtend));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(eObjectToReduceOne, eObjectToReduceTwo, eReferenceToExtend));
		
		// test nodes from the map
		
		// get all the eObjects that contain eReferences
		Set<EObject> eObjectsFromEReferenceContainingType = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
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
		
		assertEquals(expectedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// test nodes from the graph
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getNodes().isEmpty());
		
		// test edges from the graph
		
		Set<EObject> actualEObjects = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertTrue(edge.getType() == eReferenceToExtend);
			EObject sourceEObject = viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource());
			EObject targetEObject = viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget());
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
	private void testReduceEObjectEObjectEReferenceSameModelNonEmptyViewTwice (EReference eReferenceToExtend, EObject eObjectToReduceOne, EObject eObjectToReduceTwo) {
		 
		testReduceEObjectEObjectEReferenceSameModelNonEmptyView(eReferenceToExtend, eObjectToReduceOne, eObjectToReduceTwo);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(eObjectToReduceOne, eObjectToReduceTwo, eReferenceToExtend));

		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
	}
	
	// end: reduce(EObject, EObject, EReference)
	
	/**
	 * Test method for {@link view.View#copy()} on an empty {@link View}.
	 */
	@Test
	final void testCopyEmptyView() {
		
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
	 * @param objects a list of {@link EObject eObjects}, {@link EClass eClasses} and {@link EReference eReferences} to extend the view by
	 */
	private void testCopyChangeView(Object... objects) {
		
		View copyOfView = viewOnScrumPlanningInstanceOne.copy();
		
		List<Node> expectedNodes = new ArrayList<Node>(copyOfView.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(copyOfView.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(copyOfView.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(copyOfView.objectMap.values());
		
		for (EObject eObject : expectedObjectMapSet) {
			viewOnScrumPlanningInstanceOne.extend(eObject);
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
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		testCopyChangeView (workitem, stakeholder);
		// note how the viewOnScrumPlanningInstanceOne is not cleared
		testCopyChangeView (backlogWorkitems);
		viewOnScrumPlanningInstanceOne.clear();
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		copiedViewEqualsOriginalView(viewOnScrumPlanningInstanceOne.copy());
		
		// nodes only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(workitem);
		copiedViewEqualsOriginalView(viewOnScrumPlanningInstanceOne.copy());
		
		// edges only
		viewOnScrumPlanningInstanceOne.clear();
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		copiedViewEqualsOriginalView(viewOnScrumPlanningInstanceOne.copy());
		
	}

	/**
	 * Helper method for the testing of {@link view.View#copy()}.
	 * Tests if the {@link View finalCopyOfView} is eqal to the {@link View viewOnScrumPlanningInstanceOne}.
	 * @param finalCopyOfView
	 */
	private void copiedViewEqualsOriginalView(View finalCopyOfView) {
		
		assertFalse(finalCopyOfView == viewOnScrumPlanningInstanceOne);
		assertFalse(viewOnScrumPlanningInstanceOne.graphMap == finalCopyOfView.graphMap);
		assertFalse(viewOnScrumPlanningInstanceOne.objectMap == finalCopyOfView.objectMap);
		assertFalse(viewOnScrumPlanningInstanceOne.graph == finalCopyOfView.graph);
		assertFalse(viewOnScrumPlanningInstanceOne.graph.getNodes() == finalCopyOfView.graph.getNodes());
		assertFalse(viewOnScrumPlanningInstanceOne.graph.getEdges() == finalCopyOfView.graph.getEdges());

		assertTrue(viewOnScrumPlanningInstanceOne.equals(finalCopyOfView));
		
	}

	/**
	 * Test method for {@link view.View#clear()}.
	 */
	@Test
	final void testClear() {
		
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
		
		
		testUnionException("The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testUnionException("The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		// test with empty view
		testUnionWithEmptyView();
		
		// test on view with nodes only
		viewOnScrumPlanningInstanceOne.extend(backlogEObject);
		viewOnScrumPlanningInstanceOne.extend(workitemEObjects[0]);
		testUnionWithEmptyView();
		viewOnScrumPlanningInstanceOne.clear();
		
		// test on view with edges only
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		testUnionWithEmptyView();
		viewOnScrumPlanningInstanceOne.clear();
		
		// test on view with both edges and nodes
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testUnionWithEmptyView();
		viewOnScrumPlanningInstanceOne.clear();
		
	}
	
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * First unites viewOnScrumPlanningInstanceOne with an empty view with should change nothing
	 * and then unites the empty view with viewOnScrumPlanningInstanceOne which should make them both equal.
	 */
	private void testUnionWithEmptyView() {
		
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		try {
			viewOnScrumPlanningInstanceOne.union(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		try {
			viewTwo.union(viewOnScrumPlanningInstanceOne);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(viewTwo, viewOnScrumPlanningInstanceOne);
		
	}
	
	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * @param expectedErrorMessage the expected error message
	 * @param view the view to unite with
	 */
	private void testUnionException(String expectedErrorMessage, View view) {
		
		View expectedSavedState = viewOnScrumPlanningInstanceOne.copy();
		
		try {
			viewOnScrumPlanningInstanceOne.union(view);
			fail();
		} catch (ViewSetOperationException e) {
			assertEquals(expectedErrorMessage, e.getMessage());
			assertEquals(expectedSavedState, e.getSavedState());
		}
		
		viewOnScrumPlanningInstanceOne.clear();
		
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
		
		
		testIntersectException("The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testIntersectException("The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		// test with empty view
		testIntersectWithEmptyView();
		
		// test on view with nodes only
		viewOnScrumPlanningInstanceOne.extend(backlogEObject);
		viewOnScrumPlanningInstanceOne.extend(workitemEObjects[0]);
		testIntersectWithEmptyView();
		
		// test on view with edges only
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		testIntersectWithEmptyView();
		
		// test on view with both edges and nodes
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testIntersectWithEmptyView();
		
	}

	/**
	 * Test method for {@link view.View#union(view.View)}.
	 * First intersects an empty view with viewOnScrumPlanningInstanceOne, which should not change the empty view
	 * and then intersects viewOnScrumPlanningInstanceOne with an empty view, which should make viewOnScrumPlanningInstanceOne empty .
	 */
	private void testIntersectWithEmptyView() {
		
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewTwo.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewTwo.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewTwo.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewTwo.objectMap.values());
		
		try {
			viewTwo.intersect(viewOnScrumPlanningInstanceOne);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(expectedNodes, viewTwo.graph.getNodes());
		assertEquals(expectedEdges, viewTwo.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewTwo.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewTwo.objectMap.values()));
		
		try {
			viewOnScrumPlanningInstanceOne.intersect(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#intersect(view.View)}.
	 * @param expectedErrorMessage the expected error message
	 * @param view the view to intersect with
	 */
	private void testIntersectException(String expectedErrorMessage, View view) {
		
		View expectedSavedState = viewOnScrumPlanningInstanceOne.copy();
		
		try {
			viewOnScrumPlanningInstanceOne.intersect(view);
			fail();
		} catch (ViewSetOperationException e) {
			assertEquals(expectedErrorMessage, e.getMessage());
			assertEquals(expectedSavedState, e.getSavedState());
		}
		
		viewOnScrumPlanningInstanceOne.clear();
		
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
		
		
		testSubtractException("The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testSubtractException("The resources are not identical.", new View(CRA_INSTANCE_ONE));
		
		// test with empty view
		testSubtractEmptyView();
		
		// test on view with nodes only
		viewOnScrumPlanningInstanceOne.extend(backlogEObject);
		viewOnScrumPlanningInstanceOne.extend(workitemEObjects[0]);
		testSubtractEmptyView();
		
		// test on view with edges only
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitemsEReference);
		testSubtractEmptyView();
		
		// test on view with both edges and nodes
		viewOnScrumPlanningInstanceOne.extend(stakeholderOne);
		viewOnScrumPlanningInstanceOne.extend(stakeholderTwo);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitemsEReference);
		testSubtractEmptyView();
		
	}

	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 * First subtracts an empty view from viewOnScrumPlanningInstanceOne, which should not change the viewOnScrumPlanningInstanceOne
	 * and then subtracts viewOnScrumPlanningInstanceOne from an empty view, which should not change the empty view.
	 */
	private void testSubtractEmptyView() {
		
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		List<Node> expectedNodes = new ArrayList<Node>(viewOnScrumPlanningInstanceOne.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(viewOnScrumPlanningInstanceOne.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		try {
			viewOnScrumPlanningInstanceOne.subtract(viewTwo);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertEquals(expectedNodes, viewOnScrumPlanningInstanceOne.graph.getNodes());
		assertEquals(expectedEdges, viewOnScrumPlanningInstanceOne.graph.getEdges());
		assertEquals(expectedGraphMapSet, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedObjectMapSet, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		try {
			viewTwo.subtract(viewOnScrumPlanningInstanceOne);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		assertTrue(viewTwo.isEmpty());
		assertTrue(viewTwo.graphMap.isEmpty());
		assertTrue(viewTwo.objectMap.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 * @param expectedErrorMessage the expected error message
	 * @param view the view to subtract
	 */
	private void testSubtractException(String expectedErrorMessage, View view) {
		
		View expectedSavedState = viewOnScrumPlanningInstanceOne.copy();
		
		try {
			viewOnScrumPlanningInstanceOne.subtract(view);
			fail();
		} catch (ViewSetOperationException e) {
			assertEquals(expectedErrorMessage, e.getMessage());
			assertEquals(expectedSavedState, e.getSavedState());
		}
		
		viewOnScrumPlanningInstanceOne.clear();
		
	}
	
	// removeDangling
	
	/**
	 * Test method for {@link view.View#removeDangling()}.
	 */
	@Test
	final void testRemoveDangling() {
		
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
		
		// empty view
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, CRA_ECORE);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, CRA_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_TWO);
		
		// non-empty view
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(workitem);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, CRA_ECORE);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, CRA_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnScrumPlanningInstanceOne, SCRUM_PLANNIG_INSTANCE_TWO);
		
		// empty view
		View viewOnCRAInstanceOne = new View(CRA_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, SCRUM_PLANNIG_ECORE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, CRA_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, SCRUM_PLANNIG_INSTANCE_TWO);
		
		// non-empty view
		viewOnCRAInstanceOne.extend(namedElement);
		viewOnCRAInstanceOne.extend(classEClass);
		viewOnCRAInstanceOne.extend(encapsulates);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, SCRUM_PLANNIG_ECORE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, CRA_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, SCRUM_PLANNIG_INSTANCE_ONE);
		assertMatchViewByMetamodelWrongParameters(viewOnCRAInstanceOne, SCRUM_PLANNIG_INSTANCE_TWO);
		
	}
	
	/**
	 * Calls the {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)} method
	 * on the given {@link View view} with the given {@link Resource metamodel} and expects it to return false.
	 * Also checks if the view was modified by the method.
	 * @param view the {@link View view} to call the {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)} method on
	 * @param metamodel the {@link Resource metamodel} to use as a parameter
	 */
	private void assertMatchViewByMetamodelWrongParameters (View view, Resource metamodel) {
		
		List<Node> expectedNodes = new ArrayList<Node>(view.graph.getNodes());
		List<Edge> expectedEdges = new ArrayList<Edge>(view.graph.getEdges());
		Set<EObject> expectedGraphMapSet = new HashSet<>(view.graphMap.keySet());
		Set<EObject> expectedObjectMapSet = new HashSet<>(view.objectMap.values());
		
		assertFalse(view.matchViewByMetamodel(metamodel));

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
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		// empty view
		assertMatchViewByMetamodel();
		
		// nodes only
		viewOnScrumPlanningInstanceOne.extend(stakeholder);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		assertMatchViewByMetamodel();
		
		// edges only
		viewOnScrumPlanningInstanceOne.extend(backlogWorkitems);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		assertMatchViewByMetamodel();
		
		// nodes and edges
		viewOnScrumPlanningInstanceOne.extend(workitem);
		viewOnScrumPlanningInstanceOne.extend(backlog);
		viewOnScrumPlanningInstanceOne.extend(stakeholderWorkitems);
		assertMatchViewByMetamodel();
		
	}
	
	/**
	 * Calls the {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)}
	 * method on the {@code viewOnScrumPlanningInstanceOne} with the {@code SCRUM_PLANNIG_ECORE}
	 * and asserts that all elements have been inserted. 
	 * <b>Also {@link View#clear() clears} the {@link View view} at the end.</b>
	 */
	private void assertMatchViewByMetamodel () {
		
		assertTrue(viewOnScrumPlanningInstanceOne.matchViewByMetamodel(SCRUM_PLANNIG_ECORE));
		
		// manually create a full view and assert their equal
		
		View fullView = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan", "Stakeholder", "Backlog", "WorkItem", "Sprint");
		
		for (EClass eClass : eClasses) {
			fullView.extend(eClass);
			for (EReference eReference : eClass.getEAllReferences()) {
				fullView.extend(eReference);
			}
		}
		
		assertTrue(fullView.equals(viewOnScrumPlanningInstanceOne));
		
		viewOnScrumPlanningInstanceOne.clear();
		
	}
		
	// end: matchViewByMetamodel
	
	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} with objects that are not {@link View views}.
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	final void testEqualsNotView() {
		
		assertFalse(viewOnScrumPlanningInstanceOne.equals(new GraphImpl()));
		assertFalse(viewOnScrumPlanningInstanceOne.equals(SCRUM_PLANNIG_ECORE));
		assertFalse(viewOnScrumPlanningInstanceOne.equals("View"));
		
	}
	
	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} on empty {@link View views}.
	 */
	@Test
	final void testEqualsEmptyView() {
		
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

}
