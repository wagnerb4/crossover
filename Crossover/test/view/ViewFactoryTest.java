/**
 * 
 */
package view;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Benjamin Wagner
 *
 */
class ViewFactoryTest extends ViewPackageTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	// BuildViewMapping
	
	/**
	 * Test method for {@link view.ViewFactory#buildViewMapping(view.View, view.View, java.util.List, java.util.List)}.
	 */
	@Test
	final void testBuildViewMapping () {
		
		List<EClass> problemPartEClasses = List.of(getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan", "Backlog", "Stakeholder", "WorkItem"));
		List<EReference> problemPartEReferences = new ArrayList<>();
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(0), "backlog"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(0), "stakeholders"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(1), "workitems"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(2), "workitems"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(3), "stakeholder"));
		
		View viewOne = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		Set<Mapping> viewMappings = ViewFactory.buildViewMapping(viewOne, viewTwo, problemPartEClasses, problemPartEReferences);
		assertNotNull(viewMappings);
		assertViewContainsElements(viewOne, problemPartEClasses, problemPartEReferences);
		assertViewContainsElements(viewTwo, problemPartEClasses, problemPartEReferences);
		assertMappingIsTotal(viewMappings, viewOne, problemPartEClasses);
		assertMappingIsGraphIsomorphism(viewMappings, viewOne, viewTwo, problemPartEReferences);
		
		viewOne = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		viewTwo = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		viewMappings = ViewFactory.buildViewMapping(viewOne, viewTwo, problemPartEClasses, problemPartEReferences);
		assertNotNull(viewMappings);
		assertViewContainsElements(viewOne, problemPartEClasses, problemPartEReferences);
		assertViewContainsElements(viewTwo, problemPartEClasses, problemPartEReferences);
		assertMappingIsTotal(viewMappings, viewOne, problemPartEClasses);
		assertMappingIsGraphIsomorphism(viewMappings, viewOne, viewTwo, problemPartEReferences);
		
		viewOne = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		viewMappings = ViewFactory.buildViewMapping(viewOne, viewTwo, problemPartEClasses, problemPartEReferences);
		assertNotNull(viewMappings);
		assertViewContainsElements(viewOne, problemPartEClasses, problemPartEReferences);
		assertViewContainsElements(viewTwo, problemPartEClasses, problemPartEReferences);
		assertMappingIsTotal(viewMappings, viewOne, problemPartEClasses);
		assertMappingIsGraphIsomorphism(viewMappings, viewOne, viewTwo, problemPartEReferences);
		
		viewOne = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		viewTwo = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		viewMappings = ViewFactory.buildViewMapping(viewOne, viewTwo, problemPartEClasses, problemPartEReferences);
		assertNotNull(viewMappings);
		assertViewContainsElements(viewOne, problemPartEClasses, problemPartEReferences);
		assertViewContainsElements(viewTwo, problemPartEClasses, problemPartEReferences);
		assertMappingIsTotal(viewMappings, viewOne, problemPartEClasses);
		assertMappingIsGraphIsomorphism(viewMappings, viewOne, viewTwo, problemPartEReferences);
		
		viewOne = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		viewTwo = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		viewMappings = ViewFactory.buildViewMapping(viewOne, viewTwo, problemPartEClasses, problemPartEReferences);
		assertNotNull(viewMappings);
		assertViewContainsElements(viewOne, problemPartEClasses, problemPartEReferences);
		assertViewContainsElements(viewTwo, problemPartEClasses, problemPartEReferences);
		assertMappingIsTotal(viewMappings, viewOne, problemPartEClasses);
		assertMappingIsGraphIsomorphism(viewMappings, viewOne, viewTwo, problemPartEReferences);
		
	}
	
	/**
	 * Makes assertions that are true if and only if the {@link View view} contains all of the
	 * given {@link EClass eClasses} and {@link EReference eReferences}.
	 * @param view the {@link View view} to check
	 * @param eClasses the {@link EClass eClass}-elements to check containment of
	 * @param eReferences the {@link EReference eReference}-elements to check containment of
	 */
	private void assertViewContainsElements (View view, List<EClass> eClasses, List<EReference> eReferences) {
		
		View containingView = new View(view.resource);
		eClasses.forEach(containingView::extend);
		eReferences.forEach(containingView::extend);
		assertTrue(containingView.equals(view));
		
	}
	
	/**
	 * Asserts that the given {@link Mapping viewMappings} are total.
	 * More exact it asserts that the set of all {@link EObject eObjects} of the given {@link EClass eClasses} 
	 * in the {@link View#resource resource} of the given {@link View view} is equal to those covered by the 
	 * {@link Mapping viewMappings'} {@link Mapping#getOrigin() origins}.
	 * @param viewMapping a {@link Set set} of {@link Mapping mappings} between {@link Node nodes}.
	 * @param view the {@link View view} whose {@link View#graph graph} contains the {@link Mapping#getOrigin() origin-nodes}
	 * @param eClasses the {@link EClass eClasses} used in the {@link ViewFactory#buildViewMapping(View, View, List, List)} call
	 */
	private void assertMappingIsTotal (Set<Mapping> viewMappings, View view, List<EClass> eClasses) {
		
		View containingView = new View(view.resource);
		eClasses.forEach(containingView::extend);
		Set<EObject> expectedEObjects = containingView.graphMap.keySet();
		Set<EObject> actualEObjects = viewMappings.stream().
				map(mapping -> view.getObject(mapping.getOrigin())).
				collect(Collectors.toSet());
		assertEquals(expectedEObjects, actualEObjects);
		
	}
	
	/**
	 * Asserts that the given {@link Mapping viewMappings} is an graph isomorphism between
	 * the {@link View#graph graph} of {@link View viewOne} and its image in the {@link View#graph graph} of {@link View viewTwo}.
	 * @param viewMappings a {@link Set set} of {@link Mapping mappings} between {@link Node nodes}.
	 * @param viewOne the {@link View view} whose {@link View#graph graph} contains the {@link Mapping#getOrigin() origin-nodes}
	 * @param viewTwo the {@link View view} whose {@link View#graph graph} contains the {@link Mapping#getImage() image-nodes}
	 * @param eReferences the {@link EReference eReferences} used in the {@link ViewFactory#buildViewMapping(View, View, List, List)} call
	 */
	private void assertMappingIsGraphIsomorphism (Set<Mapping> viewMappings, View viewOne, View viewTwo, List<EReference> eReferences) {
		
		Set<Node> imageNodes = viewMappings.stream().map(Mapping::getImage).collect(Collectors.toSet());
		
		Set<List<EObject>> viewTwoEdges = viewTwo.graph.getEdges().stream().
				filter(edge -> eReferences.contains(edge.getType()) && imageNodes.contains(edge.getSource()) && imageNodes.contains(edge.getTarget())).
				map(edge -> List.of(viewTwo.getObject(edge.getSource()), viewTwo.getObject(edge.getTarget()))).
				collect(Collectors.toSet());
		
		for (Edge edge : viewOne.graph.getEdges()) {
			
			Node viewOneSourceNode = edge.getSource();
			Node viewOneTargetNode = edge.getTarget();
			
			Node mappedViewTwoSourceNode = null;
			Node mappedViewTwoTargetNode = null;
			
			for (Mapping mapping : viewMappings) {
				if(mapping.getOrigin().equals(viewOneSourceNode))
					mappedViewTwoSourceNode = mapping.getImage();
				if(mapping.getOrigin().equals(viewOneTargetNode))
					mappedViewTwoTargetNode = mapping.getImage();
			}
			
			EObject viewTwoSourceEObject = viewTwo.getObject(mappedViewTwoSourceNode);
			EObject viewTwoTargetEObject = viewTwo.getObject(mappedViewTwoTargetNode);
			List<EObject> mappedEdge = List.of(viewTwoSourceEObject, viewTwoTargetEObject);
			
			assertTrue(viewTwoEdges.contains(mappedEdge));
			viewTwoEdges.remove(mappedEdge);
			
		}
		
		assertTrue(viewTwoEdges.isEmpty());
		
	}

	// IntersectByMapping
	
	/**
	 * Test method for {@link view.ViewFactory#intersectByMapping(view.View, view.View, java.util.Set)}.
	 */
	@Test
	final void testIntersectByMapping () {
		
		View viewOnScrumPlanningInstanceThree = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		View viewOnScrumPlanningInstanceTwo = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan", "Backlog", "Stakeholder", "WorkItem");
		
		EClass plan = eClasses[0];
		EClass backlog = eClasses[1];
		EClass stakeholder = eClasses[2];
		EClass workitem = eClasses[3];
		
		EReference planBacklog = getEReferenceFromEClass(plan, "backlog");
		EReference planStakeholders = getEReferenceFromEClass(plan, "stakeholders");
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		EReference workitemStakeholder = getEReferenceFromEClass(workitem, "stakeholder");
		
		List<EClass> eClassesList = List.of(eClasses);
		List<EReference> eReferencesList = List.of(planBacklog, planStakeholders, backlogWorkitems, stakeholderWorkitems, workitemStakeholder);
		
		Set<Mapping> mappings = ViewFactory.buildViewMapping(viewOnScrumPlanningInstanceTwo, viewOnScrumPlanningInstanceThree, eClassesList, eReferencesList);
		
		View copyOfViewOnScrumPlanningInstanceTwo = viewOnScrumPlanningInstanceTwo.copy();
		Set<Mapping> mappingFromCopy = mappings.stream().map(mapping -> {
			mapping.setOrigin(copyOfViewOnScrumPlanningInstanceTwo.getNode(viewOnScrumPlanningInstanceTwo.getObject(mapping.getOrigin())));
			return mapping;
		}).collect(Collectors.toSet());
		
		copyOfViewOnScrumPlanningInstanceTwo.reduce(plan);
		copyOfViewOnScrumPlanningInstanceTwo.removeDangling();
		
		View actualView = ViewFactory.intersectByMapping(copyOfViewOnScrumPlanningInstanceTwo, viewOnScrumPlanningInstanceThree, mappingFromCopy);
		
		View expectedView = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		expectedView.extend(backlog);
		expectedView.extend(stakeholder);
		expectedView.extend(workitem);
		expectedView.extend(backlogWorkitems);
		expectedView.extend(stakeholderWorkitems);
		expectedView.extend(workitemStakeholder);
		
		assertTrue(expectedView.equals(actualView));
		
	}

	// IsSubgraph
	
	/**
	 * Test method for {@link view.ViewFactory#isSubgraph(view.View, view.View)}.
	 */
	@Test
	final void testIsSubgraph() {
		fail("Not yet implemented"); // TODO
	}

	// DoDFS
	
	/**
	 * Test method for {@link view.ViewFactory#doDFS(view.View, org.eclipse.emf.henshin.model.Node)}.
	 */
	@Test
	final void testDoDFS() {
		fail("Not yet implemented"); // TODO
	}

	// GetSubGraphIterator
	
	/**
	 * Test method for {@link view.ViewFactory#getSubGraphIterator(view.View)}.
	 */
	@Test
	final void testGetSubGraphIterator() {
		fail("Not yet implemented"); // TODO
	}

}
