/**
 * 
 */
package crossover;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;

import view.TestResources;
import view.View;
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
	final void testGetSpanIterator() {
		fail("Not yet implemented"); // TODO
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
