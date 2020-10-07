/**
 * 
 */
package view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.interfaces.ECKey;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.model.Edge;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.junit.jupiter.api.Test;

import crossover.MappingUtil;
import crossover.Pair;

/**
 * @author Benjamin Wagner
 *
 */
class ViewFactoryTest extends ViewPackageTest {

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
		
		View expectedView = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		expectedView.extend(backlog);
		expectedView.extend(stakeholder);
		expectedView.extend(workitem);
		expectedView.extend(backlogWorkitems);
		expectedView.extend(stakeholderWorkitems);
		expectedView.extend(workitemStakeholder);
		
		createMappingAndTestMethod (
				SCRUM_PLANNIG_INSTANCE_TWO, 
				SCRUM_PLANNIG_INSTANCE_THREE, 
				List.of(eClasses),
				List.of(planBacklog, planStakeholders, backlogWorkitems, stakeholderWorkitems, workitemStakeholder), 
				view -> {
					view.reduce(plan);
					view.removeDangling();
					return null;
				}, map -> {
					return expectedView;
				}
		);
		
		EObject[] workitemEObjectsInstanceTwo = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_TWO, 
				eObject -> eObject.eClass() == workitem).get(0).toArray(new EObject[0]);
		
		EObject backlogEObject = getEObjectsFromResource(SCRUM_PLANNIG_INSTANCE_THREE,
				eObject -> eObject.eClass() == backlog).get(0).iterator().next();
		
		expectedView.clear();
		expectedView.extend(backlog);
		
		createMappingAndTestMethod (
				SCRUM_PLANNIG_INSTANCE_TWO, 
				SCRUM_PLANNIG_INSTANCE_THREE, 
				List.of(backlog, workitem),
				List.of(backlogWorkitems), 
				view -> {
					view.reduce(workitemEObjectsInstanceTwo[1]);
					view.removeDangling();
					return null;
				}, map -> {
					expectedView.extend(map.get(workitemEObjectsInstanceTwo[0]));
					expectedView.extend(backlogEObject, map.get(workitemEObjectsInstanceTwo[0]), backlogWorkitems);
					return expectedView;
				}
		);
		
	}

	/**
	 * Creates two {@link View views}, one of earch given {@link Resource resource} and initialized them using
	 * the {@link ViewFactory#buildViewMapping(View, View, List, List) buildViewMapping} method. Then it copies and changes the
	 * {@link View firstView} ({@link View view} of {@link Resource resourceOne}) with the given {@link Function viewChanger} 
	 * and uses the {@link view.ViewFactory#intersectByMapping(view.View, view.View, java.util.Set) intersectByMapping}
	 * method to apply the changes to the {@link View secondView} ({@link View view} of {@link Resource resourceTwo}).
	 * Then it compares the result with the {@link View view} generated by the {@link Function expectedGenerater}.
	 * @param resourceOne first {@link Resource resource} to use, another than {@link Resource resourceTwo}
	 * @param resourceTwo second {@link Resource resource} to use, another than {@link Resource resourceOne}
	 * @param eClassesList the {@link EClass eClasses} to use in the {@link ViewFactory#buildViewMapping(View, View, List, List) buildViewMapping} call
	 * @param eReferencesList the {@link EReference eReferences} to use in the  {@link ViewFactory#buildViewMapping(View, View, List, List) buildViewMapping} call
	 * @param expectedView the {@link View view} to compare the result with
	 * @param viewChanger a {@link Funktion funktion} to change the filled and copied {@link View firstView} with.
	 * @param expectedGenerater a {@link Funktion funktion} that generates the expected view from a map of {@link EObject eObjects} between
	 * {@link Resource resourceOne} and {@link Resource resourceTwo}.
	 */
	private void createMappingAndTestMethod(Resource resourceOne, Resource resourceTwo, 
			List<EClass> eClassesList, List<EReference> eReferencesList,
			Function<View, ?> viewChanger, 
			Function<Map<EObject, EObject>, View> expectedGenerater) {
		
		View firstView = new View(resourceOne);
		View secondView = new View(resourceTwo);
		
		Set<Mapping> mappings = ViewFactory.buildViewMapping(firstView, secondView, eClassesList, eReferencesList);
		
		Map<EObject, EObject> eObjectMap = mappings.stream().
				collect(Collectors.toMap(
						(Mapping mapping) -> firstView.getObject(mapping.getOrigin()),
						(Mapping mapping) -> secondView.getObject(mapping.getImage()))
				);
		
		View copyOfFirstView = firstView.copy();
		Set<Mapping> mappingFromCopyOfFirstView = MappingUtil.mapByOrigin(mappings, node -> copyOfFirstView.getNode(firstView.getObject(node)));
		
		viewChanger.apply(copyOfFirstView);
		
		View expectedCopyOfFirstView = copyOfFirstView.copy();
		View expectedSecondView = secondView.copy();
		
		View actualView = ViewFactory.intersectByMapping(copyOfFirstView, secondView, mappingFromCopyOfFirstView);
		
		assertTrue(expectedCopyOfFirstView.equals(copyOfFirstView));
		assertTrue(expectedSecondView.equals(secondView));
		
		assertTrue(expectedGenerater.apply(eObjectMap).equals(actualView));
		
	}

	// IsSubgraph
	
	/**
	 * Test method for {@link view.ViewFactory#isSubgraph(view.View, view.View)}.
	 */
	@Test
	final void testIsSubgraph() {
		
		List<EClass> problemPartEClasses = List.of(getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan", "Backlog", "Stakeholder", "WorkItem"));
		List<EReference> problemPartEReferences = new ArrayList<>();
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(0), "backlog"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(0), "stakeholders"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(1), "workitems"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(2), "workitems"));
		problemPartEReferences.add(getEReferenceFromEClass(problemPartEClasses.get(3), "stakeholder"));
		
		View viewOne = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_ONE);
		
		// empty
		
		assertTrue(ViewFactory.isSubgraph(viewOne, viewTwo));
		assertTrue(ViewFactory.isSubgraph(viewTwo, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewOne, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewTwo, viewTwo));
		
		// nodes only
		
		viewOne.extend(problemPartEClasses.get(0));
		viewOne.extend(problemPartEClasses.get(3));
		
		viewTwo.extend(problemPartEClasses.get(0));
		viewTwo.extend(problemPartEClasses.get(3));
		
		assertFalse(ViewFactory.isSubgraph(viewOne, viewTwo));
		assertFalse(ViewFactory.isSubgraph(viewTwo, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewOne, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewTwo, viewTwo));
		
		View copyOfViewOne = viewOne.copy();
		assertTrue(ViewFactory.isSubgraph(viewOne, copyOfViewOne));
		assertTrue(ViewFactory.isSubgraph(copyOfViewOne, viewOne));
		copyOfViewOne.reduce(problemPartEClasses.get(3));
		assertTrue(ViewFactory.isSubgraph(copyOfViewOne, viewOne));
		assertFalse(ViewFactory.isSubgraph(viewOne, copyOfViewOne));
		
		// edges only
		
		viewOne.clear();
		viewTwo.clear();
		
		viewOne.extend(problemPartEReferences.get(0));
		viewOne.extend(problemPartEReferences.get(3));
		
		viewTwo.extend(problemPartEReferences.get(0));
		viewTwo.extend(problemPartEReferences.get(3));
		
		assertFalse(ViewFactory.isSubgraph(viewOne, viewTwo));
		assertFalse(ViewFactory.isSubgraph(viewTwo, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewOne, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewTwo, viewTwo));
		
		copyOfViewOne = viewOne.copy();
		assertTrue(ViewFactory.isSubgraph(viewOne, copyOfViewOne));
		assertTrue(ViewFactory.isSubgraph(copyOfViewOne, viewOne));
		copyOfViewOne.reduce(problemPartEReferences.get(3));
		assertTrue(ViewFactory.isSubgraph(copyOfViewOne, viewOne));
		assertFalse(ViewFactory.isSubgraph(viewOne, copyOfViewOne));
		
		// nodes and edges
		
		viewOne.extend(problemPartEClasses.get(0));
		viewOne.extend(problemPartEClasses.get(1));
		
		viewTwo.extend(problemPartEClasses.get(0));
		viewTwo.extend(problemPartEClasses.get(1));
		
		assertFalse(ViewFactory.isSubgraph(viewOne, viewTwo));
		assertFalse(ViewFactory.isSubgraph(viewTwo, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewOne, viewOne));
		assertTrue(ViewFactory.isSubgraph(viewTwo, viewTwo));
		
		copyOfViewOne = viewOne.copy();
		assertTrue(ViewFactory.isSubgraph(viewOne, copyOfViewOne));
		assertTrue(ViewFactory.isSubgraph(copyOfViewOne, viewOne));
		copyOfViewOne.reduce(problemPartEClasses.get(1));
		assertTrue(ViewFactory.isSubgraph(copyOfViewOne, viewOne));
		assertFalse(ViewFactory.isSubgraph(viewOne, copyOfViewOne));
		
	}

	// CreateEGraphFromView
	
	/**
	 * Test method for {@link view.ViewFactory#createEGraphFromView(View)}.
	 */
	@Test
	final void testCreateEGraphFromView () {
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Plan", "Backlog", "Stakeholder", "WorkItem");
		
		EClass backlog = eClasses[1];
		EClass stakeholder = eClasses[2];
		EClass workitem = eClasses[3];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		EReference workitemStakeholder = getEReferenceFromEClass(workitem, "stakeholder");
		
		View view = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		
		// empty
		runCreateEGraphFromViewOnView(view);
		
		// nodes only
		view.extend(backlog);
		view.extend(stakeholder);
		view.extend(workitem);
		runCreateEGraphFromViewOnView(view);
		
		// edges only
		view.clear();
		view.extend(backlogWorkitems);
		view.extend(stakeholderWorkitems);
		view.extend(workitemStakeholder);
		
		View copyOfView = view.copy();
		
		try {	
			ViewFactory.createEGraphFromView(view);
			fail("Expected an IllegalArgumentException but none was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The view must not contain dangling edges.", e.getMessage());
			assertTrue(copyOfView.equals(view));
		}
		
		// nodes and edges
		view.extend(stakeholder);
		view.extend(workitem);
		
		copyOfView = view.copy();
		
		try {	
			ViewFactory.createEGraphFromView(view);
			fail("Expected an IllegalArgumentException but none was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The view must not contain dangling edges.", e.getMessage());
			assertTrue(copyOfView.equals(view));
		}
		
		view.extend(backlog);
		
		runCreateEGraphFromViewOnView(view);
		
	}
	
	/**
	 * Runns all assertions on the given {@link View view} and the
	 * result of the {@link view.ViewFactory#createEGraphFromView(View) method call}.
	 * @param view the {@link View view} to use in the method call
	 */
	private void runCreateEGraphFromViewOnView (View view) {
		
		Pair<EGraph, Map<EObject, EObject>> pair = assertViewNotChangedByMethodCall(view);
		assertEGraphContainsDifferentElements(pair.getFirst(), view);
		assertMapContainsAllElements(pair.getSecond(), pair.getFirst(), view);
		assertMapIsBijection(pair.getSecond());
		assertNodeTypesArePreserved(pair.getSecond(), pair.getFirst(), view);
		assertEdgesArePreserved(pair.getSecond(), pair.getFirst(), view);
		
	}
	
	/**
	 * Uses the given {@link View view} to call the 
	 * {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method and
	 * checks whether the view was altered by the call.
	 * @param view the {@link View view} to use in the method call
	 * @return Returns the {@link Pair} as returned by {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView}.
	 */
	private Pair<EGraph, Map<EObject, EObject>> assertViewNotChangedByMethodCall (View view) {
		
		View copyOfView = view.copy();
		
		Pair<EGraph, Map<EObject, EObject>> pair = ViewFactory.createEGraphFromView(view);
		
		assertTrue(view.equals(copyOfView));
		
		return pair;
		
	}
	
	/**
	 * Checks whether the given {@link EGraph eGraph} contains none of the {@link EObject eObjects}
	 * from the {@link View view}.
	 * @param eGraph the {@link EGraph eGraph} as returned by the {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method
	 * @param view the {@link View view} used in the method call
	 */
	private void assertEGraphContainsDifferentElements (EGraph eGraph, View view) {
	
		TreeIterator<EObject> treeIterator = view.resource.getAllContents();
		
		while (treeIterator.hasNext()) {
			assertFalse(eGraph.contains((EObject) treeIterator.next()));
		}
		
		Iterator<EObject> iterator = eGraph.iterator();
		
		while (iterator.hasNext()) {
			assertFalse(view.contains((EObject) iterator.next()));
		}
				
	}
	
	/**
	 * Asserts the given {@link Map map's} set of key and value {@link EObject eObjects}
	 * to be equal to those of the {@link EGraph eGraph} and {@link View view} respectively. 
	 * @param map the {@link Map map} as returned by the {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method
	 * @param eGraph the {@link EGraph eGraph} as returned by the {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method
	 * @param view the {@link View view} used in the method call
	 */
	private void assertMapContainsAllElements (Map<EObject, EObject> map, EGraph eGraph, View view) {
		
		Set<EObject> expectedKeySet = view.graph.getNodes().stream().map(view::getObject).collect(Collectors.toSet());
		Set<EObject> actualKeySet = map.keySet();
		assertEquals(expectedKeySet, actualKeySet);
		
		Set<EObject> expectedValueSet = new HashSet<>(eGraph);
		Set<EObject> actualValueSet = new HashSet<>(map.values());
		assertEquals(expectedValueSet, actualValueSet);
		
	}
	
	/**
	 * Checks whether the given {@link Map map} is a bijection.
	 * @param map the {@link Map map} to check
	 */
	private void assertMapIsBijection (Map<EObject, EObject> map) {
		
		assertFalse(map.keySet().stream().anyMatch(entry -> entry == null));
		assertFalse(map.values().stream().anyMatch(entry -> entry == null));
		assertEquals(map.keySet().size(), map.values().size());
		
	}
	
	/**
	 * Checks whether the mapped elements have the same type.
	 * @param map the {@link Map map} as returned by the {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method
	 * @param eGraph the {@link EGraph eGraph} as returned by the {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method
	 * @param view the {@link View view} used in the method call
	 */
	private void assertNodeTypesArePreserved (Map<EObject, EObject> map, EGraph eGraph, View view) {
		
		for (Node node : view.graph.getNodes()) {
			assertEquals(map.get(view.getObject(node)).eClass(), view.getObject(node).eClass());
		}
		
		Map<EObject, EObject> inverseMap = map.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));
		
		for (EObject eObject : eGraph) {
			assertEquals(inverseMap.get(eObject).eClass(), eObject.eClass());
		}
		
	}
	
	/**
	 * Asserts that the references bewteen the {@link EObject eObjects} in the given {@link EGraph eGraph} comply to the {@link Edge edges} of the given {@link View view}. 
	 * @param map the {@link Map map} as returned by the {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method
	 * @param eGraph the {@link EGraph eGraph} as returned by the {@link view.ViewFactory#createEGraphFromView(View) createEGraphFromView} method
	 * @param view the {@link View view} used in the method call
	 */
	private void assertEdgesArePreserved (Map<EObject, EObject> map, EGraph eGraph, View view) {
		
		for (Edge edge : view.graph.getEdges()) {
			
			EObject sourceEObject = map.get(view.getObject(edge.getSource()));
			EObject targetEObject = map.get(view.getObject(edge.getTarget()));
			
			// now there should be an reference between the two eObjects
			boolean foundReference = false;
			
			if (edge.getType().getEContainingClass() == sourceEObject.eClass()) {
				Object object = sourceEObject.eGet(edge.getType());
				if (object instanceof EObject) {
					if (((EObject) object) == targetEObject) foundReference = true;
				} else {
					@SuppressWarnings("unchecked")
					EList<EObject> listOfEObjects = (EList<EObject>) object;
					for (EObject eObject : listOfEObjects) {
						if (eObject == targetEObject) {
							foundReference = true;
							break;
						}
					}
				}
			} 
			
			if (!foundReference && edge.getType().getEContainingClass() == targetEObject.eClass()) {
				Object object = targetEObject.eGet(edge.getType());
				if (object instanceof EObject) {
					if (((EObject) object) == sourceEObject) foundReference = true;
				} else {
					@SuppressWarnings("unchecked")
					EList<EObject> listOfEObjects = (EList<EObject>) object;
					for (EObject eObject : listOfEObjects) {
						if (eObject == sourceEObject) {
							foundReference = true;
							break;
						}
					}
				}
			}
			
			assertTrue(foundReference);
			
		}
		
		Map<EObject, EObject> mapReversed = new HashMap<>();
		
		map.forEach((originalEObject, copiedEObject) -> {
			mapReversed.put(copiedEObject, originalEObject);
		});
		
		Iterator<EObject> iterator = eGraph.iterator();
		
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			for (EReference eReference : eObject.eClass().getEAllReferences()) {
				Object object = eObject.eGet(eReference);
				
				if (object instanceof EObject) {
					
					EObject referencedEObject = (EObject) object;
					assertTrue(view.contains(mapReversed.get(eObject), mapReversed.get(referencedEObject), eReference, false));
					
				} else if (object != null) {
					@SuppressWarnings("unchecked")
					EList<EObject> listOfEObjects = (EList<EObject>) object;
					
					for (EObject referencedEObject : listOfEObjects) {
						assertTrue(view.contains(mapReversed.get(eObject), mapReversed.get(referencedEObject), eReference, false));
					}
				}
			}
			
		}
		
	}
	
	// DoDFS
	
	/**
	 * Test method for {@link view.ViewFactory#doDFS(view.View, org.eclipse.emf.henshin.model.Node)}.
	 */
	@Test
	final void testDoDFS() {
		
		// get CRA meta-model elements
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		EClass attributeEClass = eClasses[3];
		EClass methodEClass = eClasses[4];
		
		EAttribute name = getEAttributeFromEClass(namedElementEClass, "name");
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference featureIsEncapsulatedBy = getEReferenceFromEClass(methodEClass, "isEncapsulatedBy");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource(
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(name).equals("1"), // 0
				eObject -> eObject.eGet(name).equals("2"), // 1
				eObject -> eObject.eGet(name).equals("3"), // 2
				eObject -> eObject.eGet(name).equals("4"), // 3
				eObject -> eObject.eGet(name).equals("5"), // 4
				eObject -> eObject.eGet(name).equals("8"), // 5
				eObject -> eObject.eGet(name).equals("9") // 6
		);
		
		View view = new View(CRA_INSTANCE_ONE);
		view.extend(classModelEClass);
		view.extend(classEClass);
		view.extend(methodEClass);
		view.extend(attributeEClass);
		view.extend(classModelClasses);
		view.extend(classModelFeatures);
		view.extend(encapsulates);
		view.extend(featureIsEncapsulatedBy);
		view.extend(methodDataDependency);
		view.extend(methodFunctionalDependency);
		view.reduce(mapOfSets.get(0).iterator().next(), mapOfSets.get(4).iterator().next(), classModelFeatures);
		view.reduce(mapOfSets.get(0).iterator().next(), mapOfSets.get(6).iterator().next(), classModelClasses);
		
		View expectedComponentOne = view.copy();
		expectedComponentOne.reduce(mapOfSets.get(4).iterator().next());
		expectedComponentOne.reduce(mapOfSets.get(6).iterator().next());
		expectedComponentOne.removeDangling();
		View expectedComponentTwo = view.copy();
		try {
			expectedComponentTwo.subtract(expectedComponentOne);
		} catch (ViewSetOperationException e) {
			fail(e.getMessage());
		}
		
		View actualComponentOne = ViewFactory.doDFS(view, view.getNode(mapOfSets.get(0).iterator().next()));
		assertTrue(expectedComponentOne.equals(actualComponentOne));
		View actualComponentTwo = ViewFactory.doDFS(view, view.getNode(mapOfSets.get(4).iterator().next()));
		assertTrue(expectedComponentTwo.equals(actualComponentTwo));
		
		try {
			ViewFactory.doDFS(expectedComponentOne, view.getNode(mapOfSets.get(0).iterator().next()));
			fail("Expected IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The node is not part of the view.", e.getMessage());
		}
		
		view.reduce(mapOfSets.get(0).iterator().next());
		
		try {
			ViewFactory.doDFS(view, view.getNode(mapOfSets.get(4).iterator().next()));
			fail("Expected IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The view must not contain dangling edges.", e.getMessage());
		}
		
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
