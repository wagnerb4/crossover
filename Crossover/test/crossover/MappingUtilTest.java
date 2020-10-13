/**
 * 
 */
package crossover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.Charset;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.eclipse.emf.henshin.model.impl.NodeImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import view.TestResources;
import view.View;

/**
 * @author Benjamin Wagner
 */
class MappingUtilTest extends TestResources {

	static final int DEFAULT_MAPPING_SIZE = 100;
	static final int DEFAULT_NODE_NAME_LENGHT = 10;
	
	private static Resource GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_FI;
	private static Resource GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_I;
	private static Resource GET_MAPPING_SET_ITERATOR_TEST_CRA_ECORE;
	
	// setup
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpGetMappingSetIteratorTestInstances() throws Exception {
		HenshinResourceSet resourceSet = new HenshinResourceSet(RESOURCE_PATH);
		GET_MAPPING_SET_ITERATOR_TEST_CRA_ECORE = resourceSet.getResource("CRA.ecore");
		GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_FI = resourceSet.getResource("GetMappingSetIteratorTest_1_CRAInstance_FI.xmi");
		GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_I = resourceSet.getResource("GetMappingSetIteratorTest_1_CRAInstance_I.xmi");
	}
	
	private Set<Mapping> initRandomMapping (int size) {
		
		Set<Mapping> mappings = new HashSet<Mapping>();
		
		long nano = Clock.systemDefaultZone().instant().getNano();
		Random random = new Random(nano);
		byte[] array;
		Charset charset = Charset.forName("UTF-8");
		
		while (mappings.size() < size) {
			
			Mapping mapping = new MappingImpl();

			array = new byte[DEFAULT_NODE_NAME_LENGHT];
			random.nextBytes(array);
			
			Node originNode = new NodeImpl(new String(array, charset), null);
			
			array = new byte[DEFAULT_NODE_NAME_LENGHT];
			random.nextBytes(array);
			
			Node imageNode = new NodeImpl(new String(array, charset), null);
			
			mapping.setOrigin(originNode);
			mapping.setImage(imageNode);
			mappings.add(mapping);
			
		}
		
		return mappings;
		
	}
	
	/**
	 * Test method for {@link crossover.MappingUtil#mapByOrigin(java.util.Set, java.util.function.Function)}.
	 */
	@Test
	final void testMapByOrigin() {
		
		Set<Mapping> mappings = initRandomMapping(DEFAULT_MAPPING_SIZE);
		
		Node newOrigin = new NodeImpl("Node", null);
		
		Set<Mapping> copyOFMappings = new HashSet<Mapping>();
		mappings.forEach(mapping -> {
			Mapping copyOfMapping = new MappingImpl();
			copyOfMapping.setImage(mapping.getImage());
			copyOfMapping.setOrigin(mapping.getOrigin());
			copyOFMappings.add(copyOfMapping);
		});
		
		Set<Mapping> changedMappings = MappingUtil.mapByOrigin(mappings, node -> newOrigin);
		
		
		String[] copyOFMappingsStrings = copyOFMappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		String[] mappingsStrings = mappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		Arrays.sort(copyOFMappingsStrings);
		Arrays.sort(mappingsStrings);
		
		assertEquals(
				Stream.of(copyOFMappingsStrings).collect(Collectors.joining()),
				Stream.of(mappingsStrings).collect(Collectors.joining())
		);
		
		Set<Mapping> expectedMappings = new HashSet<Mapping>();
		mappings.forEach(mapping -> {
			Mapping copyOfMapping = new MappingImpl();
			copyOfMapping.setImage(mapping.getImage());
			copyOfMapping.setOrigin(newOrigin);
			expectedMappings.add(copyOfMapping);
		});
		
		String[] expectedMappingsStrings = expectedMappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		String[] changedMappingsStrings = changedMappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		Arrays.sort(expectedMappingsStrings);
		Arrays.sort(changedMappingsStrings);
		assertEquals (
				Stream.of(expectedMappingsStrings).collect(Collectors.joining()),
				Stream.of(changedMappingsStrings).collect(Collectors.joining())
		);
		
	}

	/**
	 * Test method for {@link crossover.MappingUtil#mapByImage(java.util.Set, java.util.function.Function)}.
	 */
	@Test
	final void testMapByImage() {
		
		Set<Mapping> mappings = initRandomMapping(DEFAULT_MAPPING_SIZE);
		
		Node newImage = new NodeImpl("Node", null);
		
		Set<Mapping> copyOFMappings = new HashSet<Mapping>();
		mappings.forEach(mapping -> {
			Mapping copyOfMapping = new MappingImpl();
			copyOfMapping.setImage(mapping.getImage());
			copyOfMapping.setOrigin(mapping.getOrigin());
			copyOFMappings.add(copyOfMapping);
		});
		
		Set<Mapping> changedMappings = MappingUtil.mapByImage(mappings, node -> newImage);
		
		
		String[] copyOFMappingsStrings = copyOFMappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		String[] mappingsStrings = mappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		Arrays.sort(copyOFMappingsStrings);
		Arrays.sort(mappingsStrings);
		
		assertEquals(
				Stream.of(copyOFMappingsStrings).collect(Collectors.joining()),
				Stream.of(mappingsStrings).collect(Collectors.joining())
		);
		
		Set<Mapping> expectedMappings = new HashSet<Mapping>();
		mappings.forEach(mapping -> {
			Mapping copyOfMapping = new MappingImpl();
			copyOfMapping.setImage(newImage);
			copyOfMapping.setOrigin(mapping.getOrigin());
			expectedMappings.add(copyOfMapping);
		});
		
		String[] expectedMappingsStrings = expectedMappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		String[] changedMappingsStrings = changedMappings.stream().map(Mapping::toString).collect(Collectors.toList()).toArray(new String[0]);
		Arrays.sort(expectedMappingsStrings);
		Arrays.sort(changedMappingsStrings);
		assertEquals (
				Stream.of(expectedMappingsStrings).collect(Collectors.joining()),
				Stream.of(changedMappingsStrings).collect(Collectors.joining())
		);
		
	}

	/**
	 * Test method for {@link crossover.MappingUtil#getImageSet(java.util.Set, org.eclipse.emf.henshin.model.Node)}.
	 */
	@Test
	final void testGetImageSet() {
		
		Set<Mapping> mappings = new HashSet<>();
		
		Node nodeOne = new NodeImpl("1", null);
		Node nodeTwo = new NodeImpl("2", null);
		Node nodeThree = new NodeImpl("3", null);
		Node nodeFour = new NodeImpl("4", null);
		Node nodeFife = new NodeImpl("5", null);
		Node nodeSix = new NodeImpl("6", null);
		
		Mapping mapping = new MappingImpl();
		mapping.setOrigin(nodeOne);
		mapping.setImage(nodeTwo);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeOne);
		mapping.setImage(nodeThree);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeFour);
		mapping.setImage(nodeFife);
		mappings.add(mapping);
		
		List<Node> actualNodes = new ArrayList<Node>(MappingUtil.getImageSet(mappings, nodeOne));
		Collections.sort(actualNodes, (Node firstNode, Node secondNode) -> {
			int one = Integer.parseInt(firstNode.getName());
			int two = Integer.parseInt(secondNode.getName());
			return ((Integer) one).compareTo(two);
		});
		
		assertEquals(2, actualNodes.size());
		assertTrue(actualNodes.get(0) == nodeTwo);
		assertTrue(actualNodes.get(1) == nodeThree);
		
		assertTrue(MappingUtil.getImageSet(mappings, nodeSix).isEmpty());
		
	}

	/**
	 * Test method for {@link crossover.MappingUtil#getImageSingle(java.util.Set, org.eclipse.emf.henshin.model.Node)}.
	 */
	@Test
	final void testGetImageSingle() {
		
		Set<Mapping> mappings = new HashSet<>();
		
		Node nodeOne = new NodeImpl("1", null);
		Node nodeTwo = new NodeImpl("2", null);
		Node nodeThree = new NodeImpl("3", null);
		Node nodeFour = new NodeImpl("4", null);
		Node nodeFife = new NodeImpl("5", null);
		Node nodeSix = new NodeImpl("6", null);
		
		Mapping mapping = new MappingImpl();
		mapping.setOrigin(nodeOne);
		mapping.setImage(nodeTwo);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeOne);
		mapping.setImage(nodeThree);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeFour);
		mapping.setImage(nodeFife);
		mappings.add(mapping);
		
		assertNull(MappingUtil.getImageSingle(mappings, nodeOne));
		assertNull(MappingUtil.getImageSingle(mappings, nodeSix));
		assertTrue(MappingUtil.getImageSingle(mappings, nodeFour) == nodeFife);
		
	}

	/**
	 * Test method for {@link crossover.MappingUtil#getInverseImageSet(java.util.Set, org.eclipse.emf.henshin.model.Node)}.
	 */
	@Test
	final void testGetInverseImageSet() {
		
		Set<Mapping> mappings = new HashSet<>();
		
		Node nodeOne = new NodeImpl("1", null);
		Node nodeTwo = new NodeImpl("2", null);
		Node nodeThree = new NodeImpl("3", null);
		Node nodeFour = new NodeImpl("4", null);
		Node nodeFife = new NodeImpl("5", null);
		Node nodeSix = new NodeImpl("6", null);
		
		Mapping mapping = new MappingImpl();
		mapping.setOrigin(nodeTwo);
		mapping.setImage(nodeOne);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeThree);
		mapping.setImage(nodeOne);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeFife);
		mapping.setImage(nodeFour);
		mappings.add(mapping);
		
		List<Node> actualNodes = new ArrayList<Node>(MappingUtil.getInverseImageSet(mappings, nodeOne));
		Collections.sort(actualNodes, (Node firstNode, Node secondNode) -> {
			int one = Integer.parseInt(firstNode.getName());
			int two = Integer.parseInt(secondNode.getName());
			return ((Integer) one).compareTo(two);
		});
		
		assertEquals(2, actualNodes.size());
		assertTrue(actualNodes.get(0) == nodeTwo);
		assertTrue(actualNodes.get(1) == nodeThree);
		
		assertTrue(MappingUtil.getImageSet(mappings, nodeSix).isEmpty());
		
	}

	/**
	 * Test method for {@link crossover.MappingUtil#getInverseImageSingle(java.util.Set, org.eclipse.emf.henshin.model.Node)}.
	 */
	@Test
	final void testGetInverseImageSingle() {
		
		Set<Mapping> mappings = new HashSet<>();
		
		Node nodeOne = new NodeImpl("1", null);
		Node nodeTwo = new NodeImpl("2", null);
		Node nodeThree = new NodeImpl("3", null);
		Node nodeFour = new NodeImpl("4", null);
		Node nodeFife = new NodeImpl("5", null);
		Node nodeSix = new NodeImpl("6", null);
		
		Mapping mapping = new MappingImpl();
		mapping.setOrigin(nodeTwo);
		mapping.setImage(nodeOne);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeThree);
		mapping.setImage(nodeOne);
		mappings.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(nodeFife);
		mapping.setImage(nodeFour);
		mappings.add(mapping);
		
		assertNull(MappingUtil.getInverseImageSingle(mappings, nodeOne));
		assertNull(MappingUtil.getInverseImageSingle(mappings, nodeSix));
		assertTrue(MappingUtil.getInverseImageSingle(mappings, nodeFour) == nodeFife);
		
	}

	/**
	 * Test method for {@link crossover.MappingUtil#getMappingSetIterator(view.View, view.View, java.util.Map, view.View)}
	 * with wrong parameters.
	 */
	@Test
	final void testGetMappingSetIteratorWrongParameters() {
		
		View viewOne = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		View viewTwo = new View(SCRUM_PLANNIG_INSTANCE_TWO);
		View viewThree = new View(SCRUM_PLANNIG_INSTANCE_THREE);
		
		// null values
		
		try {
			MappingUtil.getMappingSetIterator(viewOne, viewTwo, null, viewThree);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The identity map must not be null.", e.getMessage());
		}
		
		try {
			MappingUtil.getMappingSetIterator(viewOne, viewTwo, new HashMap<EObject, EObject>(), null);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The identity view must not be null.", e.getMessage());
		}
		
		try {
			MappingUtil.getMappingSetIterator(viewOne, null, new HashMap<EObject, EObject>(), viewThree);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The toView must not be null.", e.getMessage());
		}
		
		try {
			MappingUtil.getMappingSetIterator(null, viewTwo, new HashMap<EObject, EObject>(), viewThree);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The fromView must not be null.", e.getMessage());
		}
		
		// wrong resources in views
		
		try {
			MappingUtil.getMappingSetIterator(viewOne, viewOne, new HashMap<EObject, EObject>(), viewThree);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The toView must be over a different resource than the fromView.", e.getMessage());
		}
		
		try {
			MappingUtil.getMappingSetIterator(viewOne, viewTwo, new HashMap<EObject, EObject>(), viewTwo);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The identityView must be over the same resource than the fromView.", e.getMessage());
		}
		
		// get the Stakeholder and Backlog EClass from the metamodel
		EClass[] eClasses = getEClassFromResource(SCRUM_PLANNIG_ECORE, "Stakeholder", "Backlog", "WorkItem", "Sprint");
		EClass stakeholder = eClasses[0];
		EClass backlog = eClasses[1];
		EClass workitem = eClasses[2];
		EClass sprint = eClasses[3];
		
		EReference backlogWorkitems = getEReferenceFromEClass(backlog, "workitems");
		EReference stakeholderWorkitems = getEReferenceFromEClass(stakeholder, "workitems");
		
		viewOne.extend(stakeholder);
		viewOne.extend(backlog);
		viewOne.extend(workitem);
		viewOne.extend(sprint);
		viewOne.extend(backlogWorkitems);
		viewThree = viewOne.copy();
		viewOne.extend(stakeholderWorkitems);
		
		viewTwo.extend(stakeholder);
		viewTwo.extend(backlog);
		viewTwo.extend(workitem);
		viewTwo.extend(backlogWorkitems);
		
		// fromView contains more elements than the toView
		try {
			MappingUtil.getMappingSetIterator(viewOne, viewTwo, new HashMap<EObject, EObject>(), viewOne);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The fromView must not contain more elements than the toView.", e.getMessage());
		}
		
		// wrong resources in the map
		
		viewThree = viewTwo.copy();
		
		// from B to A instead of A to B
	 	Map<EObject, EObject> map = findRandomInjection(viewTwo, viewThree);
		
		try {
			MappingUtil.getMappingSetIterator(viewTwo, viewOne, map, viewThree);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The identity map must map from the identityView to the toView.", e.getMessage());
		}
		
		// map does not contain all of the identity view
		map = findRandomInjection(viewThree, viewOne);
		
		List<EObject> eObjects = new ArrayList<>(map.keySet());
		Collections.shuffle(eObjects);
		eObjects = eObjects.subList(0, eObjects.size() / 2);
		eObjects.forEach(map::remove);
		
		try {
			MappingUtil.getMappingSetIterator(viewTwo, viewOne, map, viewThree);
			fail("Expected an IllegalArgumentException but no was thrown.");
		} catch (IllegalArgumentException e) {
			assertEquals("The identity map must map all elements of the identityView.", e.getMessage());
		}
		
	}
	
	/**
	 * @param fromView the domain of the map
	 * @param toView containing the codomain of the map
	 * @return Returns a random injective {@link Map map} of {@link EObject eObjects} from the {@link View fromView} to the {@link View toView}.
	 */
	private Map<EObject, EObject> findRandomInjection(View fromView, View toView) {
		
		HashMap<EObject, EObject> map = new HashMap<>();
		Set<EObject> usedEObjects = new HashSet<>();
		final int capacity = toView.getGraph().getNodes().size();
		
		for (EObject eObject : fromView.getGraph().getNodes().stream().map(fromView::getObject).collect(Collectors.toList())) {
			boolean foundUnusedEObject = false;
			
			while (!foundUnusedEObject) {
				EObject mappedEObject = toView.getObject(toView.getRandomNode());
				if(!usedEObjects.contains(mappedEObject)) {
					usedEObjects.add(mappedEObject);
					foundUnusedEObject = true;
					map.put(eObject, mappedEObject);
				} else {
					if (usedEObjects.size() == capacity) throw new IllegalStateException("There is no injective mapping.");
				}
			}
			
		}
		
		return map;
		
	}
	
	/**
	 * Test method for {@link crossover.MappingUtil#getMappingSetIterator(view.View, view.View, java.util.Map, view.View)}
	 * with the identityView being empty.
	 */
	@Test
	final void testGetMappingSetIteratorOnePosibility() {
		
		EClass[] eClasses = getEClassFromResource(GET_MAPPING_SET_ITERATOR_TEST_CRA_ECORE, "NamedElement", "Class");
		EClass namedElement = eClasses[0];
		EClass classEClass = eClasses[1];
		
		EAttribute name = getEAttributeFromEClass(namedElement, "name");
		
		// get CRAInstanceOne elements
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_I, 
				eObject -> eObject.eGet(name).equals("1"),
				eObject -> eObject.eGet(name).equals("2"),
				eObject -> eObject.eGet(name).equals("3")
		);
		
		EObject classModel1 = mapOfSets.get(0).iterator().next();
		EObject method2 = mapOfSets.get(1).iterator().next();
		EObject method3 = mapOfSets.get(2).iterator().next();
		
		mapOfSets = getEObjectsFromResource (
				GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_FI,
				eObject -> eObject.eGet(name).equals("5"),
				eObject -> eObject.eGet(name).equals("6"),
				eObject -> eObject.eGet(name).equals("7")
		);
		
		EObject classModel5 = mapOfSets.get(0).iterator().next();
		EObject method6 = mapOfSets.get(1).iterator().next();
		EObject method7 = mapOfSets.get(2).iterator().next();
		
		View fIntersection = new View(GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_FI);
		View intersection = new View(GET_MAPPING_SET_ITERATOR_TEST_1_CRA_INSTANCE_I);
		
		fIntersection.extendByAllNodes();
		fIntersection.extendByMissingEdges();
		intersection.extendByAllNodes();
		intersection.extendByMissingEdges();
		
		View pIntersection = intersection.copy();
		pIntersection.reduce(classEClass);
		pIntersection.removeDangling();
		
		Map<EObject, EObject> map = new HashMap<>();
		map.put(classModel1, classModel5);
		map.put(method2, method6);
		map.put(method3, method7);
		
		Iterator<Set<Mapping>> mappingSetIterator = MappingUtil.getMappingSetIterator(intersection, fIntersection, map, pIntersection);
		
		assertTrue(mappingSetIterator.hasNext());
		Set<Mapping> mappings = mappingSetIterator.next();
		assertFalse(mappingSetIterator.hasNext());
		
		for (Mapping mapping : mappings) {
			EObject originEObject = intersection.getObject(mapping.getOrigin());
			EObject imageEObject = fIntersection.getObject(mapping.getImage());
			String originName = (String) originEObject.eGet(name);
			String imageName = (String) imageEObject.eGet(name);
			
			switch (originName) {
				case "1":
					assertEquals("5", imageName);
					break;
				case "2":
					assertEquals("6", imageName);
					break;
				case "3":
					assertEquals("7", imageName);
					break;
				case "4":
					assertEquals("8", imageName);
					break;
				default:
					fail("Unexpected name: " + originName);
			}
			
		}
		
		
	}
	
}
