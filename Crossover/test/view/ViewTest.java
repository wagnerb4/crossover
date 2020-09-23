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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.eclipse.emf.henshin.model.impl.NodeImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

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
	 * 
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
		
		Map<Integer, Set<EObject>> mapOfEObjectSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass() == stakeholder, 
				eObject -> eObject.eClass() == backlog
		);
		
		assertEquals(expectedStakeholderEObject, mapOfEObjectSets.get(0));
		assertEquals(expectedBacklogEObjects, mapOfEObjectSets.get(1));
		
	}
	
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

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EClass)} with 
	 * an {@link EClass eClass} of an different model.
	 */
	@Test
	final void testExtendEClassDifferentModel () {
		
		EClass someEClassNotPartOfTheScrumPlanningMetamodel = (new NodeImpl()).eClass();
		assertFalse(viewOnScrumPlanningInstanceOne.extend(someEClassNotPartOfTheScrumPlanningMetamodel));
		
		// the view should still be empty
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
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
		EClass backlog = eClasses[1];
		assertNotNull(stakeholder);
		assertNotNull(backlog);
		
		// extend the view
		assertTrue(viewOnScrumPlanningInstanceOne.extend(stakeholder));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		
		// test the correctness
		List<Node> nodes = viewOnScrumPlanningInstanceOne.graph.getNodes();
		
		assertEquals(3, nodes.size());
		
		Set<EObject> actualStakeholderEObjects = nodes.stream().
				filter(node -> node.getType().getName().equals("Stakeholder")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		Set<EObject> actualBacklogEObjects = nodes.stream().
				filter(node -> node.getType().getName().equals("Backlog")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		Map<Integer, Set<EObject>> listOfEObjectLists = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass() == stakeholder, eObject -> eObject.eClass() == backlog);
		Set<EObject> expectedStakeholderEObjects = listOfEObjectLists.getOrDefault(0, new HashSet<>());
		Set<EObject> expectedBacklogEObjects = listOfEObjectLists.getOrDefault(1, new HashSet<>());
		
		assertEquals(expectedStakeholderEObjects, actualStakeholderEObjects);
		assertEquals(expectedBacklogEObjects, actualBacklogEObjects);
		
		// try to add the same EClasses again
		assertFalse(viewOnScrumPlanningInstanceOne.extend(stakeholder));
		assertFalse(viewOnScrumPlanningInstanceOne.extend(backlog));
		
		Set<EObject> actualStakeholderEObjectsTwo = nodes.stream().
				filter(node -> node.getType().getName().equals("Stakeholder")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		Set<EObject> actualBacklogEObjectsTwo = nodes.stream().
				filter(node -> node.getType().getName().equals("Backlog")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		// nothing should have changed
		assertEquals(actualBacklogEObjectsTwo, actualBacklogEObjects);
		assertEquals(actualStakeholderEObjectsTwo, actualStakeholderEObjects);
		
	}

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EReference)} with
	 * an {@link EReference eReference} form a different model.
	 */
	@Test
	final void testExtendEReferenceDifferentModel () {
		
		EReference someEReferenceNotPartOfTheScrumPlanningMetamodel = (new NodeImpl()).eClass().getEAllReferences().get(0);
		assertFalse(viewOnScrumPlanningInstanceOne.extend(someEReferenceNotPartOfTheScrumPlanningMetamodel));
		
		// the view should still be empty
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EReference)} with
	 * {@link EReference eReferences} form a the same model.
	 */
	@Test
	final void testExtendEReferenceSameModel () {
		
		// get the expected nodes and edges
		EObject backlog = null;
		
		Map<Integer, Set<EObject>> listOfEObjectLists = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"));
		Set<EObject> backlogSet = listOfEObjectLists.getOrDefault(0, new HashSet<>());
		Set<EObject> workitemsSet = listOfEObjectLists.getOrDefault(1, new HashSet<>());
		assertEquals(1, backlogSet.size());
		backlog = backlogSet.iterator().next();
		assertEquals(4, workitemsSet.size());
		
		// get the workitems EReference from the meta-model
		// it needs to be fetched using the backlog as the name workitems is not an unique identifier
		EReference workitems = null;
		for (EReference eReference : backlog.eClass().getEAllReferences()) {
			if (eReference.getName().equals("workitems")) {
				workitems = eReference;
			}
		}
		
		testExtendEReferenceSameModelEmptyView(backlog, workitemsSet, workitems);
		
		testExtendEReferenceSameModelViewWithNodes(backlog, workitemsSet, workitems);
		
	}

	/**
	 * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with some of the nodes of the reference already inserted.
	 * @param backlog the {@link EObject eObject} form the SCRUM_PLANNIG_INSTANCE_ONE
	 * @param workitemsSet a list of all workitem {@link EObject eObjects} form the SCRUM_PLANNIG_INSTANCE_ONE
	 * @param workitems the {@link EReference eReference} from the SCRUM_PLANNIG_INSTANCE_ONE between the backlog and its workitems
	 */
	private void testExtendEReferenceSameModelViewWithNodes(EObject backlog, Set<EObject> workitemsSet,
			EReference workitems) {
		
		Set<EObject> actualEObjectsGraphMap;
		Set<EObject> actualEObjectsObjectMap;
		Set<EObject> actualWorkitems;
		Set<EObject> allExpectedEObjects = new HashSet<>(workitemsSet);
		allExpectedEObjects.add(backlog);
		
		// clear view and test with existent node
		viewOnScrumPlanningInstanceOne.clear();
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		assertNotNull(viewOnScrumPlanningInstanceOne.getNode(backlog));
		
		// extend by workitems
		assertTrue(viewOnScrumPlanningInstanceOne.extend(workitems));
		
		// test nodes
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		assertEquals(backlog, viewOnScrumPlanningInstanceOne.getObject(viewOnScrumPlanningInstanceOne.graph.getNodes().get(0)));
		
		actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		assertEquals(4, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		actualWorkitems = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(workitems, edge.getType());
			assertEquals(backlog, viewOnScrumPlanningInstanceOne.getObject(edge.getSource()));
			actualWorkitems.add(viewOnScrumPlanningInstanceOne.getObject(edge.getTarget()));
		}
		
	}

	/**
     * Tests the {@link view.View#extend(org.eclipse.emf.ecore.EReference)} method with an an empty view.
     * No {@link Node nodes} should be added to the {@link view.View#graph graph} by {@link view.View#extend(org.eclipse.emf.ecore.EReference)}.
	 * @param backlog the {@link EObject eObject} form the SCRUM_PLANNIG_INSTANCE_ONE
	 * @param workitemsSet a list of all workitem {@link EObject eObjects} form the SCRUM_PLANNIG_INSTANCE_ONE
	 * @param workitems the {@link EReference eReference} from the SCRUM_PLANNIG_INSTANCE_ONE between the backlog and its workitems
	 */
	private void testExtendEReferenceSameModelEmptyView(EObject backlog, Set<EObject> workitemsSet,
			EReference workitems) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(workitems));
		assertEquals(0, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		
		// test nodes
		
		Set<EObject> allExpectedEObjects = new HashSet<>(workitemsSet);
		allExpectedEObjects.add(backlog);
		
		Set<EObject> actualEObjectsGraphMap = viewOnScrumPlanningInstanceOne.graphMap.keySet();
		Set<EObject> actualEObjectsObjectMap = new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values());
		
		assertEquals(allExpectedEObjects, actualEObjectsGraphMap);
		assertEquals(allExpectedEObjects, actualEObjectsObjectMap);
		
		// test edges
		assertEquals(4, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		Set<EObject> actualWorkitems = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			assertEquals(workitems, edge.getType());
			assertEquals(backlog, viewOnScrumPlanningInstanceOne.objectMap.get(edge.getSource()));
			actualWorkitems.add(viewOnScrumPlanningInstanceOne.objectMap.get(edge.getTarget()));
		}
		
		assertEquals(workitemsSet, actualWorkitems);
		
	}
	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject)}  with
	 * an {@link EObject eObject} form a different model.
	 */
	@Test
	final void testExtendEObjectDifferentModel() {
		
		EObject someEObjectNotPartOfTheScrumPlanningMetamodel = SCRUM_PLANNIG_INSTANCE_TWO.getContents().get(0);
		assertFalse(viewOnScrumPlanningInstanceOne.extend(someEObjectNotPartOfTheScrumPlanningMetamodel));
		
		// the view should still be empty
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
	}

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject)} with
	 * {@link EObject eObjects} form the same model.
	 */
	@Test
	final void testExtendEObjectSameModel() {
		
		// get the Stakeholder and Backlog EObjects from the model
		
		Map<Integer, Set<EObject>> listOfEObjectLists = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("Stakeholder"));
		Set<EObject> backlogSet = listOfEObjectLists.getOrDefault(0, new HashSet<>());
		Set<EObject> stakeholder = listOfEObjectLists.getOrDefault(1, new HashSet<>());
		assertEquals(1, backlogSet.size());
		EObject backlog = backlogSet.iterator().next();
		assertEquals(2, stakeholder.size());
		
		// extend the view
		stakeholder.forEach(eObject -> assertTrue(viewOnScrumPlanningInstanceOne.extend(eObject)));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		
		// test the correctness
		List<Node> nodes = viewOnScrumPlanningInstanceOne.graph.getNodes();
		
		assertEquals(3, nodes.size());
		
		Set<EObject> actualStakeholderEObjects = nodes.stream().
				filter(node -> node.getType().getName().equals("Stakeholder")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		Set<EObject> actualBacklogEObjects = nodes.stream().
				filter(node -> node.getType().getName().equals("Backlog")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		assertEquals(stakeholder, actualStakeholderEObjects);
		assertEquals(1, actualBacklogEObjects.size());
		assertEquals(backlog, actualBacklogEObjects.iterator().next());
		
		// try to add the same EObjects again
		stakeholder.forEach(eObject -> assertFalse(viewOnScrumPlanningInstanceOne.extend(eObject)));
		assertFalse(viewOnScrumPlanningInstanceOne.extend(backlog));
		
		Set<EObject> actualStakeholderEObjectsTwo = nodes.stream().
				filter(node -> node.getType().getName().equals("Stakeholder")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		Set<EObject> actualBacklogEObjectsTwo = nodes.stream().
				filter(node -> node.getType().getName().equals("Backlog")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		// nothing should have changes
		assertEquals(actualBacklogEObjectsTwo, actualBacklogEObjects);
		assertEquals(actualStakeholderEObjectsTwo, actualStakeholderEObjects);
		
	}
	

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 */
	@Test
	final void testExtendEObjectEObjectEReferenceSameModel() {
		
		// get the expected nodes and edges
		Map<Integer, Set<EObject>> mapOfEObjectSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass().getName().equals("Backlog"),
				eObject -> eObject.eClass().getName().equals("WorkItem"),
				eObject -> eObject.eClass().getName().equals("Stakeholder")
		);
		Set<EObject> backlogSet = mapOfEObjectSets.getOrDefault(0, new HashSet<>());
		Set<EObject> workitemsSet = mapOfEObjectSets.getOrDefault(1, new HashSet<>());
		Set<EObject> stakeholderSet = mapOfEObjectSets.getOrDefault(2, new HashSet<>());
		
		assertEquals(1, backlogSet.size());
		EObject backlog = backlogSet.iterator().next();
		assertEquals(4, workitemsSet.size());
		assertEquals(2, stakeholderSet.size());
		
		Iterator<EObject> stakeholderIterator = stakeholderSet.iterator();
		assertTrue(stakeholderIterator.hasNext());
		EObject stakeholderOne = stakeholderIterator.next();
		assertTrue(stakeholderIterator.hasNext());
		EObject stakeholderTwo = stakeholderIterator.next();
		assertFalse(stakeholderIterator.hasNext());
		
		// get workitems EReference from stakeholder and from backlog
		EReference stakeholderWorkitems = null;
		EReference backlogWorkitems = null;
		
		List<EReference> workitemEReferences = backlog.eClass().getEAllReferences().stream().
			filter(eReference -> eReference.getName().equals("workitems")).
			collect(Collectors.toList());
		assertEquals(1, workitemEReferences.size());
		backlogWorkitems = workitemEReferences.get(0);
		
		workitemEReferences = stakeholderOne.eClass().getEAllReferences().stream().
			filter(eReference -> eReference.getName().equals("workitems")).
			collect(Collectors.toList());
		assertEquals(1, workitemEReferences.size());
		stakeholderWorkitems = workitemEReferences.get(0);
		
		testExtendEObjectEObjectEReferenceOnEmptyView(backlog, workitemsSet, stakeholderOne, stakeholderTwo, stakeholderWorkitems, backlogWorkitems);
		
		testExtendEObjectEObjectEReferenceOnNonEmptyView(backlog, workitemsSet, backlogWorkitems);
		
	}
	

	/**
	 * @param backlog the backlog {@link EObject eObject} from the scrumPlanningInstanceOne
	 * @param workitemsSet all workitem {@link EObject eObjects} form the scrumPlanningInstanceOne
	 * @param backlogWorkitems the {@link EReference} from the scrumPlanning metamodel between the backlog and its workitems
	 */
	private void testExtendEObjectEObjectEReferenceOnNonEmptyView (EObject backlog, Set<EObject> workitemsSet, EReference backlogWorkitems) {
		
		Edge tmpEdge;
		Set<EObject> actualWorkitems;
		List<EObject> workitemsOfBacklog = new ArrayList<EObject>(workitemsSet);
		
		// clear the view and do the same with some nodes already inserted
		viewOnScrumPlanningInstanceOne.clear();
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(workitemsOfBacklog.get(0)));
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getEdges().isEmpty());
		assertEquals(2, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(0), backlogWorkitems));
		assertEquals(2, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		assertTrue(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, false));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, false));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, true));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, true));
		tmpEdge = viewOnScrumPlanningInstanceOne.graph.getEdges().get(0);
		assertTrue(
				( viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getSource()) == backlog &&
				viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getTarget()) == workitemsOfBacklog.get(0) ) ||
				( viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getTarget()) == backlog &&
				viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getSource()) == workitemsOfBacklog.get(0)	)
		);
		assertEquals(
				new HashSet<>(List.of(backlog, workitemsOfBacklog.get(0))), 
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
					map(viewOnScrumPlanningInstanceOne::getObject).
					collect(Collectors.toSet())
		);
		
		// inserting the same edge again shouln't work and nothing should change
		assertFalse(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(0), backlogWorkitems));
		assertEquals(2, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		assertTrue(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, false));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, false));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, true));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, true));
		tmpEdge = viewOnScrumPlanningInstanceOne.graph.getEdges().get(0);
		assertTrue(
				( viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getSource()) == backlog &&
				viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getTarget()) == workitemsOfBacklog.get(0) ) ||
				( viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getTarget()) == backlog &&
				viewOnScrumPlanningInstanceOne.getObject(tmpEdge.getSource()) == workitemsOfBacklog.get(0)	)
		);
		assertEquals(
				new HashSet<>(List.of(backlog, workitemsOfBacklog.get(0))), 
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
					map(viewOnScrumPlanningInstanceOne::getObject).
					collect(Collectors.toSet())
		);
		
		// insert all the other backlog - workitem edges
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(1), backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(2), backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(3), backlogWorkitems));
		assertEquals(2, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		assertEquals(4, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		
		assertEquals(
				new HashSet<>(List.of(backlog, workitemsOfBacklog.get(0))), 
				viewOnScrumPlanningInstanceOne.graph.getNodes().stream().
					map(viewOnScrumPlanningInstanceOne::getObject).
					collect(Collectors.toSet())
		);
		
		actualWorkitems = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			Node source = edge.getSource();
			Node target = edge.getTarget();
			assertTrue(
					( viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlog && 
					viewOnScrumPlanningInstanceOne.objectMap.get(target) != backlog ) ||
					( viewOnScrumPlanningInstanceOne.objectMap.get(target) == backlog && 
					viewOnScrumPlanningInstanceOne.objectMap.get(source) != backlog )
			);
			
			Node backlogNode = viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlog ? source : target;
			Node workitemNode = viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlog ? target : source;
			assertEquals(backlog, viewOnScrumPlanningInstanceOne.objectMap.get(backlogNode));
			actualWorkitems.add(viewOnScrumPlanningInstanceOne.objectMap.get(workitemNode));
		}
		
		assertEquals(workitemsSet, actualWorkitems);
		
	}
	

	/**
	 * @param backlog the backlog {@link EObject eObject} from the scrumPlanningInstanceOne
	 * @param workitemsSet all workitem {@link EObject eObjects} form the scrumPlanningInstanceOne
	 * @param stakeholderOne one of the stakeholders form the scrumPlanningInstanceOne
	 * @param stakeholderTwo the other stakeholder form the scrumPlanningInstanceOne
	 * @param stakeholderWorkitems the {@link EReference} from the scrumPlanning meta-model between the stakeholders and its workitems
	 * @param backlogWorkitems the {@link EReference} from the scrumPlanning meta-model between the backlog and its workitems
	 */
	private void testExtendEObjectEObjectEReferenceOnEmptyView (EObject backlog, Set<EObject> workitemsSet,
			EObject stakeholderOne, EObject stakeholderTwo, EReference stakeholderWorkitems,
			EReference backlogWorkitems) {
		
		List<EObject> workitemsOfBacklog = new ArrayList<EObject>(workitemsSet);
		
		// get referenced workitems of stakeholder one
		Object object = stakeholderOne.eGet(stakeholderWorkitems);
		assertTrue(object instanceof EList<?>);
		@SuppressWarnings("unchecked")
		EList<EObject> workitemsOfStakeholderOne = (EList<EObject>) object;
		assertEquals(2, workitemsOfStakeholderOne.size());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(backlog, stakeholderTwo, stakeholderWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(workitemsOfStakeholderOne.get(0), stakeholderOne, stakeholderWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(0), backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getNodes().isEmpty());
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		assertTrue(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, true));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, true));
		assertFalse(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, false));
		assertFalse(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, false));
		Edge tmpEdge = viewOnScrumPlanningInstanceOne.graph.getEdges().get(0);
		assertTrue(
				( viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getSource()) == backlog &&
				viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getTarget()) == workitemsOfBacklog.get(0) ) ||
				( viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getTarget()) == backlog &&
				viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getSource()) == workitemsOfBacklog.get(0)	)
		);
		
		// inserting the same edge again shouln't work and nothing should change
		assertFalse(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(0), backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getNodes().isEmpty());
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		assertTrue(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, true));
		assertTrue(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, true));
		assertFalse(viewOnScrumPlanningInstanceOne.contains(backlog, workitemsOfBacklog.get(0), backlogWorkitems, false));
		assertFalse(viewOnScrumPlanningInstanceOne.contains(workitemsOfBacklog.get(0), backlog, backlogWorkitems, false));
		tmpEdge = viewOnScrumPlanningInstanceOne.graph.getEdges().get(0);
		assertTrue(
				( viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getSource()) == backlog &&
				viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getTarget()) == workitemsOfBacklog.get(0) ) ||
				( viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getTarget()) == backlog &&
				viewOnScrumPlanningInstanceOne.objectMap.get(tmpEdge.getSource()) == workitemsOfBacklog.get(0)	)
		);
		
		// insert all the other backlog - workitem edges
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(1), backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(2), backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog, workitemsOfBacklog.get(3), backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.graph.getNodes().isEmpty());
		assertEquals(4, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		
		Set<EObject> actualWorkitems = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			Node source = edge.getSource();
			Node target = edge.getTarget();
			assertTrue(
					( viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlog && 
					viewOnScrumPlanningInstanceOne.objectMap.get(target) != backlog ) ||
					( viewOnScrumPlanningInstanceOne.objectMap.get(target) == backlog && 
					viewOnScrumPlanningInstanceOne.objectMap.get(source) != backlog )
			);
			
			Node backlogNode = viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlog ? source : target;
			Node workitemNode = viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlog ? target : source;
			assertEquals(backlog, viewOnScrumPlanningInstanceOne.objectMap.get(backlogNode));
			actualWorkitems.add(viewOnScrumPlanningInstanceOne.objectMap.get(workitemNode));
		}
		
		assertEquals(workitemsSet, actualWorkitems);
		
	}

	
	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}
	 * with input parameters from a different model. 
	 */
	@Test
	final public void testExtendEObjectEObjectEReferenceDifferentModel () {
		
		// get CRA metamodel elements
		TreeIterator<EObject> iterator = CRA_ECORE.getAllContents();
		EClass namedElement = null;
		EAttribute name = null;
		EReference encapsulates = null;
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if (eObject instanceof EClass && ((EClass) eObject).getName().equals("NamedElement")) {
				namedElement = (EClass) eObject;
				List<EAttribute> attributes = namedElement.getEAttributes().stream().
						filter(eAttribute -> eAttribute.getName().equals("name")).
						collect(Collectors.toList());
				assertEquals(1, attributes.size());
				name = attributes.get(0);
			} else if (eObject instanceof EClass && ((EClass) eObject).getName().equals("Class")) {
				List<EReference> eReferences = ((EClass) eObject).getEAllReferences().stream().
						filter(eReference -> eReference.getName().equals("encapsulates")).
						collect(Collectors.toList());
				assertEquals(1, eReferences.size());
				encapsulates = eReferences.get(0);
			}
		}
		
		assertNotNull(namedElement);
		assertNotNull(name);
		assertNotNull(encapsulates);
		
		// get CRAInstanceOne elements
		iterator = CRA_INSTANCE_ONE.getAllContents();
		EObject method2 = null;
		EObject method3 = null;
		EObject attribute5 = null;
		EObject class8 = null;
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			
			if(eObject.eClass().getName().equals("ClassModel")) continue;
			
			String nameAttributeVale = (String) eObject.eGet(name);
			
			switch (nameAttributeVale) {
				case "2":
					method2 = eObject;
					break;
				case "3":
					method3 = eObject;
					break;
				case "5":
					attribute5 = eObject;
					break;
				case "8":
					class8 = eObject;
					break;
			}
			
		}
		
		assertNotNull(method2);
		assertNotNull(method3);
		assertNotNull(attribute5);
		assertNotNull(class8);
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(method3, attribute5, encapsulates));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
		assertFalse(viewOnScrumPlanningInstanceOne.extend(class8, method2, encapsulates));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
	}
	
	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} with
	 * an {@link EClass eClass} form a different model.
	 */
	@Test
	final void testReduceEClassDifferentModel () {
		
		EClass someEClassNotPartOfTheScrumPlanningMetamodel = (new NodeImpl()).eClass();
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(someEClassNotPartOfTheScrumPlanningMetamodel));
		
		// the view should still be empty
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
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
		assertNotNull(stakeholder);
		assertNotNull(backlog);
		assertNotNull(workitem);
		
		EReference backlogWorkitems = null;
		
		List<EReference> workitemEReferences = backlog.getEAllReferences().stream().
			filter(eReference -> eReference.getName().equals("workitems")).
			collect(Collectors.toList());
		assertEquals(1, workitemEReferences.size());
		backlogWorkitems = workitemEReferences.get(0);
		
		Set<EObject> expectedBacklogEObjects = getEObjectsFromResource (SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass() == backlog).getOrDefault(0, new HashSet<>());
		
		testReduceEClassSameModelNoEdgesInView(stakeholder, backlog, expectedBacklogEObjects);
		
		testReduceEClassSameModelEdgesInView(backlog, workitem, backlogWorkitems, expectedBacklogEObjects);
		
	}

	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method with edges in
	 * the view that reference {@link EObject eObjects} that are to be removed by the method.
	 * In this case the {@link EObject eObjects} should be removed from the {@link view.View#graph graph}
	 * but <b>not</b> from the maps (i.e {@link view.View#graphMap graphMap} and {@link view.View#objectMap objectMap}).
	 * @param backlog the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param workitem the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param backlogWorkitems the {@link EReference eReference} from the {@code SCRUM_PLANNIG_ECORE} between the backlog and its workitems
	 * @param expectedBacklogEObjects the set of all backlog-{@link EObject eObject} from the SCRUM_PLANNIG_INSTANCE_ONE
	 */
	private void testReduceEClassSameModelEdgesInView(EClass backlog, EClass workitem, EReference backlogWorkitems,
			Set<EObject> expectedBacklogEObjects) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(workitem));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlogWorkitems));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(workitem));
		
		// the workitem eObjects should not be in the graph anymore but in the maps as there are edges that need them
		Set<EObject> workitemsSet = getEObjectsFromResource (SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass() == workitem).getOrDefault(0, new HashSet<>());
		Set<EObject> expectedEObjects = new HashSet<>();
		assertEquals(1, expectedBacklogEObjects.size());
		EObject backlogEObject = expectedBacklogEObjects.iterator().next();
		assertEquals(4, workitemsSet.size());
		expectedEObjects.add(backlogEObject);
		expectedEObjects.addAll(workitemsSet);
		
		assertEquals(1, viewOnScrumPlanningInstanceOne.graph.getNodes().size());
		assertEquals(backlogEObject, viewOnScrumPlanningInstanceOne.getObject(viewOnScrumPlanningInstanceOne.graph.getNodes().get(0)));
		
		assertEquals(expectedEObjects, viewOnScrumPlanningInstanceOne.graphMap.keySet());
		assertEquals(expectedEObjects, new HashSet<>(viewOnScrumPlanningInstanceOne.objectMap.values()));
		
		// test the edges
		assertEquals(4, viewOnScrumPlanningInstanceOne.graph.getEdges().size());
		
		Set<EObject> actualWorkitems = new HashSet<>();
		
		for (Edge edge : viewOnScrumPlanningInstanceOne.graph.getEdges()) {
			Node source = edge.getSource();
			Node target = edge.getTarget();
			assertTrue(
					( viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlogEObject && 
					viewOnScrumPlanningInstanceOne.objectMap.get(target) != backlogEObject ) ||
					( viewOnScrumPlanningInstanceOne.objectMap.get(target) == backlogEObject && 
					viewOnScrumPlanningInstanceOne.objectMap.get(source) != backlogEObject )
			);
			
			Node backlogNode = viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlogEObject ? source : target;
			Node workitemNode = viewOnScrumPlanningInstanceOne.objectMap.get(source) == backlogEObject ? target : source;
			assertEquals(backlogEObject, viewOnScrumPlanningInstanceOne.objectMap.get(backlogNode));
			actualWorkitems.add(viewOnScrumPlanningInstanceOne.objectMap.get(workitemNode));
		}
		
		assertEquals(workitemsSet, actualWorkitems);
		
	}

	/**
	 * Tests the {@link view.View#reduce(org.eclipse.emf.ecore.EClass)} method with no edges in the view.
	 * In this case the {@link EObject eObjects} that are to be removed should also be removed from the
	 * maps (i.e {@link view.View#graphMap graphMap} and {@link view.View#objectMap objectMap}).
	 * @param stakeholder the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param backlog the {@link EClass eClass} from the {@code SCRUM_PLANNIG_ECORE}
	 * @param expectedBacklogEObjects the set of all backlog-{@link EObject eObject} from the SCRUM_PLANNIG_INSTANCE_ONE
	 */
	private void testReduceEClassSameModelNoEdgesInView(EClass stakeholder, EClass backlog, Set<EObject> expectedBacklogEObjects) {
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(backlog));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());
		
		assertTrue(viewOnScrumPlanningInstanceOne.extend(backlog));
		assertTrue(viewOnScrumPlanningInstanceOne.extend(stakeholder));
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(stakeholder));
		
		// test the correctness
		List<Node> nodes = viewOnScrumPlanningInstanceOne.graph.getNodes();
		
		assertEquals(1, nodes.size());
		
		Set<EObject> actualBacklogEObjects = nodes.stream().
				filter(node -> node.getType().getName().equals("Backlog")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		assertEquals(expectedBacklogEObjects, actualBacklogEObjects);
		
		// removing the stakeholder again should return false and change nothing
		assertFalse(viewOnScrumPlanningInstanceOne.reduce(stakeholder));
		
		actualBacklogEObjects = nodes.stream().
				filter(node -> node.getType().getName().equals("Backlog")).
				map(viewOnScrumPlanningInstanceOne::getObject).
				collect(Collectors.toSet());
		
		assertEquals(expectedBacklogEObjects, actualBacklogEObjects);
		
		assertTrue(viewOnScrumPlanningInstanceOne.reduce(backlog));
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.graphMap.isEmpty());
		assertTrue(viewOnScrumPlanningInstanceOne.objectMap.isEmpty());

	}
	

	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EReference)}.
	 */
	@Test
	final void testReduceEReference() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject)}.
	 */
	@Test
	final void testReduceEObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 */
	@Test
	final void testReduceEObjectEObjectEReference() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#copy()}.
	 */
	@Test
	final void testCopy() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#clear()}.
	 */
	@Test
	final void testClear() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#union(view.View)}.
	 */
	@Test
	final void testUnion() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#intersect(view.View)}.
	 */
	@Test
	final void testIntersect() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#subtract(view.View)}.
	 */
	@Test
	final void testSubtract() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#removeDangling()}.
	 */
	@Test
	final void testRemoveDangling() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#completeDangling()}.
	 */
	@Test
	final void testCompleteDangling() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#extendByMissingEdges()}.
	 */
	@Test
	final void testExtendByMissingEdges() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#matchViewByMetamodel(org.eclipse.emf.ecore.resource.Resource)}.
	 */
	@Test
	final void testMatchViewByMetamodel() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Test
	final void testEquals() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link java.lang.Object#toString()}.
	 */
	@Test
	final void testToString() {
		fail("Not yet implemented"); // TODO
	}

}
