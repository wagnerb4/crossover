/**
 * 
 */
package crossover;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.junit.jupiter.api.Test;

import view.TestResources;
import view.View;
import view.ViewFactory;
import view.ViewSetOperationException;

/**
 * @author benjamin
 *
 */
class CrossoverTest extends TestResources {

	/**
	 * Test method for {@link Crossover#findBorder(Resource,List,List)}.
	 */
	@Test
	final void testFindBorder() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
								NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		EClass featureEClass = eClasses[3];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference classEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference featureIsEncapsulatedBy = classEncapsulates.getEOpposite();
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		runFindBorder (
				List.of(namedElementEClass, classModelEClass, featureEClass, attributeEClass, methodEClass), 
				List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency), 
				List.of((EObject) classModelEClass, (EObject) attributeEClass, (EObject) methodEClass)
		);
		
		runFindBorder (
				List.of(classModelEClass),
				List.of(classModelClasses, classModelFeatures),
				List.of((EObject) classModelEClass, (EObject) classModelClasses, (EObject) classModelFeatures)
		);
		
		runFindBorder (
				List.of(namedElementEClass, classModelEClass, featureEClass, attributeEClass, methodEClass),
				List.of(classModelFeatures, classModelClasses, methodDataDependency, methodFunctionalDependency, featureIsEncapsulatedBy, classEncapsulates),
				List.of((EObject) classModelEClass, (EObject) classModelClasses, (EObject) featureIsEncapsulatedBy, (EObject) classEncapsulates, (EObject) methodEClass, (EObject) attributeEClass)
		);
		
	}
	
	/**
	 * Runs the {@link Crossover#findBorder} method with the given {@link EClass eClasses} and {@link EReference eReferences}.
	 * Asserts the View to contain all of the given {@link EObject expected eObjects}.
	 * @param problemPartEClasses the {@link EClass eClasses} of the problem part to use in the method call
	 * @param problemPartEReferences the {@link EReference eReferences} of the problem part to use in the method call
	 * @param expected the expected border {@link EObject eObjects}
	 */
	private void runFindBorder (List<EClass> problemPartEClasses, List<EReference> problemPartEReferences, List<EObject> expected) throws NoSuchMethodException, SecurityException, InstantiationException, 
																																	IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		// get an instance of the private findBorder method
		Class<Crossover> clazz = Crossover.class;
		Constructor<Crossover> crossoverConstructor = clazz.getDeclaredConstructor(new Class[0]);
		crossoverConstructor.setAccessible(true);
		Crossover crossover = crossoverConstructor.newInstance();
		Method findBorder = clazz.getDeclaredMethod("findBorder", Resource.class, List.class, List.class);
		findBorder.setAccessible(true);
		
		// invoke the method
		View view = (View) findBorder.invoke(crossover, CRA_ECORE, problemPartEClasses, problemPartEReferences);
		
		// assert
		Collection<EObject> eObjects = view.getContainedEObjects();
		assertTrue(eObjects.containsAll(expected));
		
	}
	
	/**
	 * Test method for {@link Crossover#splitProblemPart(View,View,Strategy)}.
	 */
	@Test
	final void testSplitProblemPart() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ViewSetOperationException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass featureEClass = eClasses[3];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		Map<Integer, Set<EObject>> map = getEObjectsFromResource (
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(nameEAttribute).equals("2"),
				eObject -> eObject.eGet(nameEAttribute).equals("4")
		);
		
		EObject method2 = map.get(0).iterator().next();
		EObject attribute4 = map.get(1).iterator().next();
		
		// create strategy
		
		Strategy strategy = view -> {
			view.reduce(attributeEClass);
		};
		
		// create problemView and border
		List<EClass> problemPartClasses = List.of(namedElementEClass, classModelEClass, featureEClass, attributeEClass, methodEClass);
		List<EReference> problemPartReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		View problemPartView = new View(CRA_INSTANCE_ONE);
		View problemBorder = new View(CRA_ECORE);
		
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		problemBorder.extend((EObject) attributeEClass);
		problemBorder.extend((EObject) methodEClass);
		
		// create expected split
		
		View first = problemPartView.copy();
		View second = problemPartView.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2);
		second.extend(method2, attribute4, methodDataDependency);
		Pair<View, View> expectedPair = new Pair<View, View>(first, second);
		
		runSplitProblemPart(problemPartView, problemBorder, strategy, expectedPair);
		
		// border with classModelEClass should yield the same result
		problemBorder.extend((EObject) classModelEClass);
		runSplitProblemPart(problemPartView, problemBorder, strategy, expectedPair);
		
		// border with classModelClasses and classModelclasses should not yield the same result
		problemBorder.extend((EObject) classModelClasses);
		problemPartReferences = List.of(classModelFeatures, classModelClasses, methodDataDependency, methodFunctionalDependency);
		problemPartView.clear();
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		second.extend(classModelClasses);
		expectedPair = new Pair<View, View>(first, second);
		runSplitProblemPart(problemPartView, problemBorder, strategy, expectedPair);
		
	}
	
	/**
	 * Runs the {@link Crossover#splitProblemPart} method with the given {@link View problemPartView}, {@link View problemBorder}
	 * and {@link Strategy strategy}. Asserts the returned split to be equal to the {@link Pair expectedSplit}.
	 * @param problemPartView the {@link View view} of the problem part to use in the method call
	 * @param problemBorder the {@link View view} of the problem border to use in the method call
	 * @param strategy the {@link Strategy strategy} to use in the method call
	 * @param expectedSplit the expected problem split
	 */
	private void runSplitProblemPart (View problemPartView, View problemBorder, Strategy strategy, Pair<View, View> expectedSplit) throws NoSuchMethodException, SecurityException, InstantiationException, 
																																		IllegalAccessException, IllegalArgumentException, InvocationTargetException, ViewSetOperationException {
		
		// get an instance of the private splitProblemPart method
		Class<Crossover> clazz = Crossover.class;
		Constructor<Crossover> crossoverConstructor = clazz.getDeclaredConstructor(new Class[0]);
		crossoverConstructor.setAccessible(true);
		Crossover crossover = crossoverConstructor.newInstance();
		Method splitProblemPart = clazz.getDeclaredMethod("splitProblemPart", View.class, View.class, Strategy.class);
		splitProblemPart.setAccessible(true);
		
		// invoke the method
		@SuppressWarnings("unchecked")
		Pair<View, View> actualProblemSplit = (Pair<View, View>) splitProblemPart.invoke(crossover, problemPartView, problemBorder, strategy);
		
		View actualCombinedView = actualProblemSplit.getFirst().copy();
		actualCombinedView.union(actualProblemSplit.getSecond());
		assertTrue(actualCombinedView.equals(problemPartView));
		
		assertTrue(actualProblemSplit.equals(expectedSplit));
		
	}
	
	/**
	 * Test method for {@link Crossover#splitSearchSpaceElement(View,Pair)}.
	 */
	@Test
	final void testSplitSearchSpaceElement() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException, ViewSetOperationException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		EClass featureEClass = eClasses[3];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference classEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		Map<Integer, Set<EObject>> map = getEObjectsFromResource (
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(nameEAttribute).equals("2"),
				eObject -> eObject.eGet(nameEAttribute).equals("5"),
				eObject -> eObject.eGet(nameEAttribute).equals("4"),
				eObject -> eObject.eGet(nameEAttribute).equals("8"),
				eObject -> eObject.eGet(nameEAttribute).equals("9"),
				eObject -> eObject.eGet(nameEAttribute).equals("1")
		);
		
		EObject method2 = map.get(0).iterator().next();
		EObject attribute4 = map.get(2).iterator().next();
		EObject attribute5 = map.get(1).iterator().next();
		EObject class8 = map.get(3).iterator().next();
		EObject class9 = map.get(4).iterator().next();
		EObject classModel1 = map.get(5).iterator().next();
		
		//  classModelClasses EReference in problem part
		
		// create problemView and split
		List<EClass> problemPartClasses = List.of(namedElementEClass, classModelEClass, featureEClass, attributeEClass, methodEClass);
		List<EReference> problemPartReferences = List.of(classModelFeatures, classModelClasses, methodDataDependency, methodFunctionalDependency);
		
		View problemPartView = new View(CRA_INSTANCE_ONE);
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		View first = problemPartView.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		
		View second = problemPartView.copy();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2);
		second.extend(method2, attribute4, methodDataDependency);
		second.extend(classModelClasses);
		Pair<View, View> problemSplit = new Pair<View, View>(first, second);
		
		View expectedFirst = first.copy();
		expectedFirst.extend(class8);
		expectedFirst.extendByMissingEdges();
		
		View expectedSecond = second.copy();
		expectedSecond.extend(classEClass);
		expectedSecond.reduce(classModel1, class8, classModelClasses);
		expectedSecond.extend(class8, attribute4, classEncapsulates);
		expectedSecond.extend(class9, attribute5, classEncapsulates);
		Pair<View, View> expectedSplit = new Pair<View, View>(expectedFirst, expectedSecond);
		
		runSplitSearchSpaceElement(problemPartView, problemSplit, expectedSplit, Crossover.DEFAULT_STRATEGY);
		
		//  classModelClasses EReference in solution part
		
		problemPartClasses = List.of(namedElementEClass, classModelEClass, featureEClass, attributeEClass, methodEClass);
		problemPartReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		problemPartView = new View(CRA_INSTANCE_ONE);
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		first = problemPartView.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		
		second = problemPartView.copy();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2);
		second.extend(method2, attribute4, methodDataDependency);
		
		problemSplit = new Pair<View, View>(first, second);
		
		expectedFirst = first.copy();
		expectedFirst.extend(classEClass);
		expectedFirst.extendByMissingEdges();
		
		expectedSecond = second.copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(class8, attribute4, classEncapsulates);
		expectedSecond.extend(class9, attribute5, classEncapsulates);
		expectedSecond.reduce(classModel1, class8,classModelClasses);
		expectedSecond.reduce(classModel1, class9,classModelClasses);
		
		expectedSplit = new Pair<View, View>(expectedFirst, expectedSecond);
		
		runSplitSearchSpaceElement(problemPartView, problemSplit, expectedSplit, Crossover.DEFAULT_STRATEGY);
		
	}
	
	/**
	 * Runs the {@link Crossover#splitSearchSpaceElement} method with the given {@link View problemPart} and {@link Pair problemSplit}.
	 * Asserts the returned split to be equal to the {@link Pair expectedSplit}.
	 * @param problemPart the {@link View view} of the problem part to use in the method call
	 * @param problemSplit the problemSplit-{@link Pair pair} to use in the method call
	 * @param expectedSplit the expected problem split of the search space element given indirectly by the {@link View#resource resource}
	 * in of the {@link View problemPart}
	 */
	private void runSplitSearchSpaceElement (View problemPart, Pair<View, View> problemSplit, 
												Pair<View, View> expectedSplit, SearchSpaceElementSplitStrategy strategy) 
											throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
											ViewSetOperationException, NoSuchMethodException, SecurityException, InstantiationException {
		
		// get an instance of the private splitSearchSpaceElement method
		Class<Crossover> clazz = Crossover.class;
		Constructor<Crossover> crossoverConstructor = clazz.getDeclaredConstructor(new Class[0]);
		crossoverConstructor.setAccessible(true);
		Crossover crossover = crossoverConstructor.newInstance();
		Method splitSearchSpaceElement = clazz.getDeclaredMethod("splitSearchSpaceElement", View.class, Pair.class, SearchSpaceElementSplitStrategy.class);
		splitSearchSpaceElement.setAccessible(true);
		
		// invoke the method
		@SuppressWarnings("unchecked")
		Pair<View, View> actualSearchSpaceElementSplit = (Pair<View, View>) splitSearchSpaceElement.invoke(crossover, problemPart, problemSplit, strategy);
		
		if (!actualSearchSpaceElementSplit.equals(expectedSplit)) {
			fail("Actual: " + actualSearchSpaceElementSplit.toString() + "; Expected: " + expectedSplit.toString());
		}
		
	}
	
	/**
	 * Test method for {@link Crossover#getSpanIterator()}.
	 */
	@Test
	final void testGetSpanIterator() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		EClass featureEClass = eClasses[3];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		// EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference classEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		// create problemView
		List<EClass> problemPartClasses = List.of(namedElementEClass, classModelEClass, featureEClass, attributeEClass, methodEClass);
		List<EReference> problemPartReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		View problemPartSSEOne = new View(CRA_INSTANCE_ONE);
		View problemPartSSETwo = new View(CRA_INSTANCE_TWO);
		
		Set<Mapping> problemPartMappings = ViewFactory.buildViewMapping(problemPartSSEOne, problemPartSSETwo, problemPartClasses, problemPartReferences);
		
		Map<Integer, Set<EObject>> map = getEObjectsFromResource (
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(nameEAttribute).equals("2"),
				eObject -> eObject.eGet(nameEAttribute).equals("5"),
				eObject -> eObject.eGet(nameEAttribute).equals("4"),
				eObject -> eObject.eGet(nameEAttribute).equals("8"),
				eObject -> eObject.eGet(nameEAttribute).equals("9"),
				eObject -> eObject.eGet(nameEAttribute).equals("1")
		);
		
		EObject method2SSEOne = map.get(0).iterator().next();
		// EObject attribute4SSEOne = map.get(2).iterator().next();
		// EObject attribute5SSEOne = map.get(1).iterator().next();
		EObject class8SSEOne = map.get(3).iterator().next();
		// EObject class9SSEOne = map.get(4).iterator().next();
		EObject classModel1SSEOne = map.get(5).iterator().next();
		
		map = getEObjectsFromResource (
				CRA_INSTANCE_TWO, 
				eObject -> eObject.eGet(nameEAttribute).equals("2"),
				eObject -> eObject.eGet(nameEAttribute).equals("5"),
				eObject -> eObject.eGet(nameEAttribute).equals("4"),
				eObject -> eObject.eGet(nameEAttribute).equals("6"),
				eObject -> eObject.eGet(nameEAttribute).equals("7"),
				eObject -> eObject.eGet(nameEAttribute).equals("1")
		);
		
		EObject method2SSETwo = map.get(0).iterator().next();
		// EObject attribute4SSETwo = map.get(2).iterator().next();
		// EObject attribute5SSETwo = map.get(1).iterator().next();
		EObject class6SSETwo = map.get(3).iterator().next();
		// EObject class7SSETwo = map.get(4).iterator().next();
		EObject classModel1SSETwo = map.get(5).iterator().next();
				
		View problemPartIntersection = new View(CRA_INSTANCE_ONE);
		problemPartIntersection.extend(classModel1SSEOne);
		problemPartIntersection.extend(method2SSEOne);
		problemPartIntersection.extend(classModel1SSEOne, method2SSEOne, classModelFeatures);
		
		View intersectionOfSSEOne = new View(CRA_INSTANCE_ONE);
		intersectionOfSSEOne.extend(classModel1SSEOne);
		intersectionOfSSEOne.extend(method2SSEOne);
		intersectionOfSSEOne.extend(classModel1SSEOne, method2SSEOne, classModelFeatures);
		intersectionOfSSEOne.extend(class8SSEOne);
		
		View intersectionOfSSETwo = new View(CRA_INSTANCE_TWO);
		intersectionOfSSETwo.extend(classModel1SSETwo);
		intersectionOfSSETwo.extend(method2SSETwo);
		intersectionOfSSETwo.extend(classModel1SSETwo, method2SSETwo, classModelFeatures);
		intersectionOfSSETwo.extend(class6SSETwo);
		intersectionOfSSETwo.extend(class6SSETwo, method2SSETwo, classEncapsulates);
		
		Iterator<CustomSpan> customSpanIterator = runGetSpanIterator (
				intersectionOfSSEOne, intersectionOfSSETwo, problemPartIntersection, 
				problemPartSSEOne, problemPartSSETwo, problemPartMappings
		);
		
		Set<CustomSpan> setOfSpans = new HashSet<>();
		
		// there should be two solutions
		assertTrue(customSpanIterator.hasNext());
		setOfSpans.add(customSpanIterator.next());
		assertTrue(customSpanIterator.hasNext());
		setOfSpans.add(customSpanIterator.next());
		assertFalse(customSpanIterator.hasNext());
		
		Set<CustomSpan> expectedSetOfSpans = new HashSet<>();
		
		// first solution
		
		View intersection = problemPartIntersection.copy();
		Set<Mapping> mappingsOne = new HashSet<>();
		
		Mapping mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(classModel1SSEOne));
		mapping.setImage(intersectionOfSSEOne.getNode(classModel1SSEOne));
		mappingsOne.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(method2SSEOne));
		mapping.setImage(intersectionOfSSEOne.getNode(method2SSEOne));
		mappingsOne.add(mapping);
		
		Set<Mapping> mappingsTwo = new HashSet<>();
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(classModel1SSEOne));
		mapping.setImage(intersectionOfSSETwo.getNode(classModel1SSETwo));
		mappingsTwo.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(method2SSEOne));
		mapping.setImage(intersectionOfSSETwo.getNode(method2SSETwo));
		mappingsTwo.add(mapping);
		
		expectedSetOfSpans.add(new CustomSpan(intersection, mappingsOne, mappingsTwo));

		// second solution
		
		intersection = problemPartIntersection.copy();
		intersection.extend(class8SSEOne);
		
		mappingsOne = new HashSet<>();
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(classModel1SSEOne));
		mapping.setImage(intersectionOfSSEOne.getNode(classModel1SSEOne));
		mappingsOne.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(method2SSEOne));
		mapping.setImage(intersectionOfSSEOne.getNode(method2SSEOne));
		mappingsOne.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(class8SSEOne));
		mapping.setImage(intersectionOfSSEOne.getNode(class8SSEOne));
		mappingsOne.add(mapping);
		
		mappingsTwo = new HashSet<>();
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(classModel1SSEOne));
		mapping.setImage(intersectionOfSSETwo.getNode(classModel1SSETwo));
		mappingsTwo.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(method2SSEOne));
		mapping.setImage(intersectionOfSSETwo.getNode(method2SSETwo));
		mappingsTwo.add(mapping);
		
		mapping = new MappingImpl();
		mapping.setOrigin(intersection.getNode(class8SSEOne));
		mapping.setImage(intersectionOfSSETwo.getNode(class6SSETwo));
		mappingsTwo.add(mapping);
		
		expectedSetOfSpans.add(new CustomSpan(intersection, mappingsOne, mappingsTwo));
		
		// sorting by the amount of nodes in the intersection is enough in this case
		Comparator<CustomSpan> comparator = (spanOne, spanTwo) -> {
			int sizeOne = spanOne.getIntersection().getGraph().getNodes().size();
			int sizeTwo = spanTwo.getIntersection().getGraph().getNodes().size();
			return ((Integer) sizeOne).compareTo(sizeTwo);
		};
		
		List<CustomSpan> expectedCustomSpansSorted = expectedSetOfSpans.stream().sorted(comparator).collect(Collectors.toList());
		List<CustomSpan> actualCustomSpansSorted = setOfSpans.stream().sorted(comparator).collect(Collectors.toList());
		
		for (int i = 0; i < expectedCustomSpansSorted.size(); i++) {
			assertTrue(expectedCustomSpansSorted.get(i).equals(actualCustomSpansSorted.get(i)));
		}
		
	}
	
	/**
	 * Runs the {@link Crossover#getSpanIterator} method with the given arguments.
	 * @param intersectionOfSSEOne a {@link View view} on the first search space element containing the intersection of the split elements
	 * @param intersectionOfSSETwo a {@link View view} on the second search space element containing the intersection of the split elements
	 * @param problemPartIntersection a {@link View view} on the first search space element containing the intersection of the problem split elements
	 * @param problemPartSSEOne a {@link View view} on the first search space element containing its problem part
	 * @param problemPartSSETwo a {@link View view} on the second search space element containing its problem part
	 * @param problemPartMappings a {@link Set set} of {@link Mapping mappings} between the {@link Node nodes}
	 * of {@literal problemPartSSEOne} and {@literal problemPartSSETwo}
	 * @return Returns the {@link CustomSpan} {@link Iterator} of the method call.
	 */
	private Iterator<CustomSpan> runGetSpanIterator (View intersectionOfSSEOne, View intersectionOfSSETwo,
			View problemPartIntersection, View problemPartSSEOne, View problemPartSSETwo, 
			Set<Mapping> problemPartMappings) throws NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		
		Class<Crossover> clazz = Crossover.class;
		Constructor<Crossover> crossoverConstructor = clazz.getDeclaredConstructor(new Class[0]);
		crossoverConstructor.setAccessible(true);
		Crossover crossover = crossoverConstructor.newInstance();
		
		Field intersectionOfSSEOneField = clazz.getDeclaredField("intersectionOfSSEOne");
		Field intersectionOfSSETwoField = clazz.getDeclaredField("intersectionOfSSETwo");
		Field problemPartIntersectionField = clazz.getDeclaredField("problemPartIntersection");
		Field problemPartSSEOneField = clazz.getDeclaredField("problemPartSSEOne");
		Field problemPartSSETwoField = clazz.getDeclaredField("problemPartSSETwo");
		Field problemPartMappingsField = clazz.getDeclaredField("problemPartMappings");
		
		intersectionOfSSEOneField.setAccessible(true);
		intersectionOfSSETwoField.setAccessible(true);
		problemPartIntersectionField.setAccessible(true);
		problemPartSSEOneField.setAccessible(true);
		problemPartSSETwoField.setAccessible(true);
		problemPartMappingsField.setAccessible(true);
		
		intersectionOfSSEOneField.set(crossover, intersectionOfSSEOne);
		intersectionOfSSETwoField.set(crossover, intersectionOfSSETwo);
		problemPartIntersectionField.set(crossover, problemPartIntersection);
		problemPartSSEOneField.set(crossover, problemPartSSEOne);
		problemPartSSETwoField.set(crossover, problemPartSSETwo);
		problemPartMappingsField.set(crossover, problemPartMappings);
		
		Method getSpanIterator = clazz.getDeclaredMethod("getSpanIterator");
		getSpanIterator.setAccessible(true);
		
		@SuppressWarnings("unchecked")
		Iterator<CustomSpan> customSpanIterator = (Iterator<CustomSpan>) getSpanIterator.invoke(crossover);
		return customSpanIterator;
		
	}
	
	/**
	 * Test method for {@link Crossover#iterator()}.
	 */
	@Test
	final void testIterator() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link Crossover}. Tests the
	 * complete procedure.
	 */
	@Test
	final void testCrossover() {
		fail("Not yet implemented"); // TODO
	}
	
}
