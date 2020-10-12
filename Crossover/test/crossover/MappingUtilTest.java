/**
 * 
 */
package crossover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.Charset;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.eclipse.emf.henshin.model.impl.NodeImpl;
import org.junit.jupiter.api.Test;

/**
 * @author Benjamin Wagner
 */
class MappingUtilTest {

	static final int DEFAULT_MAPPING_SIZE = 100;
	static final int DEFAULT_NODE_NAME_LENGHT = 10;
	
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
	 * Test method for {@link crossover.MappingUtil#getMappingSetIterator(view.View, view.View, java.util.Map, view.View)}.
	 */
	@Test
	final void testGetMappingSetIterator() {
		fail("Not yet implemented"); // TODO
	}
	
}
