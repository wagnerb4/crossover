/**
 * 
 */
package view;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
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
	 * Test method for {@link view.View#getNode(org.eclipse.emf.ecore.EObject)}.
	 */
	@Test
	final void testGetNode() {
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
	final void testGetRandomNode() {
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
	final void testGetObject() {
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
	final void testIsEmpty() {
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		viewOnScrumPlanningInstanceOne.extend(SCRUM_PLANNIG_INSTANCE_ONE.getContents().get(0));
		assertFalse(viewOnScrumPlanningInstanceOne.isEmpty());
	}

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EClass)}.
	 */
	@Test
	final void testExtendEClass() {
		
		EClass someEClassNotPartOfTheScrumPlanningMetamodel = (new NodeImpl()).eClass();
		assertFalse(viewOnScrumPlanningInstanceOne.extend(someEClassNotPartOfTheScrumPlanningMetamodel));
		
		// the view should still be empty
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
		
		// get the Stakeholder and Backlog EClass from the metamodel
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
		
		Set<EObject> expectedStakeholderEObject = new HashSet<>();  
		Set<EObject> expectedBacklogEObjects = new HashSet<>();
		
		treeIterator = SCRUM_PLANNIG_INSTANCE_ONE.getAllContents();
		
		while (treeIterator.hasNext()) {
			
			EObject eObject = (EObject) treeIterator.next();
			
			if (eObject.eClass() == stakeholder) {
				expectedStakeholderEObject.add(eObject);
			}
			
			if (eObject.eClass() == backlog) {
				expectedBacklogEObjects.add(eObject);
			}
			
		}
		
		assertEquals(expectedStakeholderEObject, actualStakeholderEObjects);
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
		
		// nothing should have changes
		assertEquals(actualBacklogEObjectsTwo, actualBacklogEObjects);
		assertEquals(actualStakeholderEObjectsTwo, actualStakeholderEObjects);
		
	}

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EReference)}.
	 */
	@Test
	final void testExtendEReference() {
		
		EReference someEReferenceNotPartOfTheScrumPlanningMetamodel = (new NodeImpl()).eClass().getEAllReferences().get(0);
		assertFalse(viewOnScrumPlanningInstanceOne.extend(someEReferenceNotPartOfTheScrumPlanningMetamodel));
		
		// the view should still be empty
		assertTrue(viewOnScrumPlanningInstanceOne.isEmpty());
	
		
		// get the expected nodes and edges
		TreeIterator<EObject> treeIterator = SCRUM_PLANNIG_ECORE.getAllContents();
		EObject backlog = null;
		Set<EObject> workitemsSet = new HashSet<>();
		treeIterator = SCRUM_PLANNIG_INSTANCE_ONE.getAllContents();
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			switch (eObject.eClass().getName()) {
				case "Backlog":
					backlog = eObject;
					break;
				case "WorkItem":
					workitemsSet.add(eObject);
					break;
			}
			
		}
		
		assertNotNull(backlog);
		assertNotEquals(0, workitemsSet.size());
		
		// get the workitems EReference from the metamodel
		// it needs to be fetched using the backlog as the name workitems is not an unique identifier
		EReference workitems = null;
		for (EReference eReference : backlog.eClass().getEAllReferences()) {
			if (eReference.getName().equals("workitems")) {
				workitems = eReference;
			}
		}
		
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
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject)}.
	 */
	@Test
	final void testExtendEObject() {
		assertFalse(viewOnScrumPlanningInstanceOne.extend(SCRUM_PLANNIG_INSTANCE_TWO.getContents().get(0)));
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#extend(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EReference)}.
	 */
	@Test
	final void testExtendEObjectEObjectEReference() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link view.View#reduce(org.eclipse.emf.ecore.EClass)}.
	 */
	@Test
	final void testReduceEClass() {
		fail("Not yet implemented"); // TODO
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
