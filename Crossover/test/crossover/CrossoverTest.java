/**
 * 
 */
package crossover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.Match;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.model.Mapping;
import org.eclipse.emf.henshin.model.Node;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.impl.MappingImpl;
import org.eclipse.emf.henshin.model.impl.RuleImpl;
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
		// EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		// EClass featureEClass = eClasses[3];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference classEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference featureIsEncapsulatedBy = classEncapsulates.getEOpposite();
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		runFindBorder (
				List.of(classModelEClass, attributeEClass, methodEClass), 
				List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency), 
				List.of((EObject) classModelEClass, (EObject) attributeEClass, (EObject) methodEClass)
		);
		
		runFindBorder (
				List.of(classModelEClass),
				List.of(classModelClasses, classModelFeatures),
				List.of((EObject) classModelEClass, (EObject) classModelClasses, (EObject) classModelFeatures)
		);
		
		runFindBorder (
				List.of(classModelEClass, attributeEClass, methodEClass),
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
	final void testSplitProblemPartEmptySubMetamodel() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ViewSetOperationException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		Map<Integer, Set<EObject>> map = getEObjectsFromResource (
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(nameEAttribute).equals("1"),
				eObject -> eObject.eGet(nameEAttribute).equals("2"),
				eObject -> eObject.eGet(nameEAttribute).equals("4")
		);
		
		EObject classModel1 = map.get(0).iterator().next();
		EObject method2 = map.get(1).iterator().next();
		EObject attribute4 = map.get(2).iterator().next();
		
		// create strategy
		
		Strategy strategy = view -> {
			view.reduce(attributeEClass);
		};
		
		// create problemView and border
		List<EClass> problemPartClasses = List.of(classModelEClass, attributeEClass, methodEClass);
		List<EReference> problemPartReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		View problemPartView = new View(CRA_INSTANCE_ONE);
		View problemBorder = new View(CRA_ECORE);
		
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		problemBorder.extend((EObject) attributeEClass);
		problemBorder.extend((EObject) methodEClass);
		
		// create empty sub metamodel
		View subMetamodel = new View(CRA_ECORE);
		
		// create expected split
		
		View first = problemPartView.copy();
		View second = problemPartView.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2);
		second.extend(method2, attribute4, methodDataDependency);
		second.extend(classModel1, method2, classModelFeatures);
		Pair<View, View> expectedPair = new Pair<View, View>(first, second);
		
		runSplitProblemPart(problemPartView, problemBorder, strategy, subMetamodel, expectedPair);
		
		// border with classModelEClass should yield the same result
		problemBorder.extend((EObject) classModelEClass);
		runSplitProblemPart(problemPartView, problemBorder, strategy, subMetamodel, expectedPair);
		
		// border with classModelClasses and classModelclasses should not yield the same result
		problemBorder.extend((EObject) classModelClasses);
		problemPartReferences = List.of(classModelFeatures, classModelClasses, methodDataDependency, methodFunctionalDependency);
		problemPartView.clear();
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		second.extend(classModelClasses);
		expectedPair = new Pair<View, View>(first, second);
		runSplitProblemPart(problemPartView, problemBorder, strategy, subMetamodel, expectedPair);
		
	}
	
	/**
	 * Test method for {@link Crossover#splitProblemPart(View,View,Strategy)}.
	 */
	@Test
	final void testSplitProblemPartNonEmptySubMetamodel() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ViewSetOperationException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		Map<Integer, Set<EObject>> map = getEObjectsFromResource (
				CRA_INSTANCE_ONE, 
				eObject -> eObject.eGet(nameEAttribute).equals("1"),
				eObject -> eObject.eGet(nameEAttribute).equals("2"),
				eObject -> eObject.eGet(nameEAttribute).equals("4")
		);
		
		EObject classModel1 = map.get(0).iterator().next();
		EObject method2 = map.get(1).iterator().next();
		EObject attribute4 = map.get(2).iterator().next();
		
		// create strategy
		
		Strategy strategy = view -> {
			view.reduce(attributeEClass);
		};
		
		// create problemView and border
		List<EClass> problemPartClasses = List.of(classModelEClass, attributeEClass, methodEClass);
		List<EReference> problemPartReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		View problemPartView = new View(CRA_INSTANCE_ONE);
		View problemBorder = new View(CRA_ECORE);
		
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		problemBorder.extend((EObject) attributeEClass);
		problemBorder.extend((EObject) methodEClass);
		
		// create empty sub metamodel
		View subMetamodel = new View(CRA_ECORE);
		subMetamodel.extend((EObject) classModelEClass);
		subMetamodel.extend((EObject) classModelClasses);
		
		// create expected split
		
		View first = problemPartView.copy();
		View second = problemPartView.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2);
		second.extend(method2, attribute4, methodDataDependency);
		second.extend(classModel1, method2, classModelFeatures);
		Pair<View, View> expectedPair = new Pair<View, View>(first, second);
		
		runSplitProblemPart(problemPartView, problemBorder, strategy, subMetamodel, expectedPair);
		
		// border with classModelEClass should yield the same result
		problemBorder.extend((EObject) classModelEClass);
		runSplitProblemPart(problemPartView, problemBorder, strategy, subMetamodel, expectedPair);
		
		// border with classModelClasses and classModelclasses should not yield the same result
		problemBorder.extend((EObject) classModelClasses);
		problemPartReferences = List.of(classModelFeatures, classModelClasses, methodDataDependency, methodFunctionalDependency);
		problemPartView.clear();
		problemPartClasses.forEach(problemPartView::extend);
		problemPartReferences.forEach(problemPartView::extend);
		
		second.extend(classModelClasses);
		expectedPair = new Pair<View, View>(first, second);
		runSplitProblemPart(problemPartView, problemBorder, strategy, subMetamodel, expectedPair);
		
	}
	
	/**
	 * Runs the {@link Crossover#splitProblemPart} method with the given {@link View problemPartView}, {@link View problemBorder}
	 * and {@link Strategy strategy}. Asserts the returned split to be equal to the {@link Pair expectedSplit}.
	 * @param problemPartView the {@link View view} of the problem part to use in the method call
	 * @param problemBorder the {@link View view} of the problem border to use in the method call
	 * @param strategy the {@link Strategy strategy} to use in the method call
	 * @param subMetaModelOfIntersection the {@link View view} on the metamodel containing the sub-meta-model of the problem part intersection
	 * @param expectedSplit the expected problem split
	 */
	private void runSplitProblemPart (View problemPartView, View problemBorder, Strategy strategy, View subMetaModelOfIntersection, Pair<View, View> expectedSplit) throws NoSuchMethodException, SecurityException, InstantiationException, 
																																		IllegalAccessException, IllegalArgumentException, InvocationTargetException, ViewSetOperationException {
		
		// get an instance of the private splitProblemPart method
		Class<Crossover> clazz = Crossover.class;
		Constructor<Crossover> crossoverConstructor = clazz.getDeclaredConstructor(new Class[0]);
		crossoverConstructor.setAccessible(true);
		Crossover crossover = crossoverConstructor.newInstance();
		Method splitProblemPart = clazz.getDeclaredMethod("splitProblemPart", View.class, View.class, Strategy.class, View.class);
		splitProblemPart.setAccessible(true);
		
		// invoke the method
		@SuppressWarnings("unchecked")
		Pair<View, View> actualProblemSplit = (Pair<View, View>) splitProblemPart.invoke(crossover, problemPartView, problemBorder, strategy, subMetaModelOfIntersection);
		
		View actualCombinedView = actualProblemSplit.getFirst().copy();
		actualCombinedView.union(actualProblemSplit.getSecond());
		assertTrue(actualCombinedView.equals(problemPartView));
		
		assertTrue(actualProblemSplit.equals(expectedSplit));
		
	}
	
	/**
	 * Test method for {@link Crossover#splitSearchSpaceElement(View,Pair)}.
	 */
	@Test
	final void testSplitSearchSpaceElementNonEmptySubMetamodel () throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException, ViewSetOperationException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
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
		
		// create empty sub metamodel
		View subMetamodel = new View(CRA_ECORE);
		subMetamodel.extend((EObject) classModelEClass);
		subMetamodel.extend((EObject) classModelClasses);
		
		// create problemView and split
		List<EClass> problemPartClasses = List.of(classModelEClass, attributeEClass, methodEClass);
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
		second.extend(classModel1, method2, classModelFeatures);
		second.extend(classModelClasses);
		Pair<View, View> problemSplit = new Pair<View, View>(first, second);
		
		View expectedFirst = first.copy();
		expectedFirst.extend(class8);
		expectedFirst.extendByMissingEdges();
		
		View expectedSecond = second.copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(classModelClasses);
		expectedSecond.extend(class8, attribute4, classEncapsulates);
		expectedSecond.extend(class9, attribute5, classEncapsulates);
		Pair<View, View> expectedSplit = new Pair<View, View>(expectedFirst, expectedSecond);
		
		runSplitSearchSpaceElement(problemPartView, problemSplit, Crossover.DEFAULT_STRATEGY, subMetamodel, expectedSplit);
		
		//  classModelClasses EReference in solution part
		
		problemPartClasses = List.of(classModelEClass, attributeEClass, methodEClass);
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
		second.extend(classModel1, method2, classModelFeatures);
		second.extend(method2, attribute4, methodDataDependency);
		
		problemSplit = new Pair<View, View>(first, second);
		
		expectedFirst = first.copy();
		expectedFirst.extend(classEClass);
		expectedFirst.extendByMissingEdges();
		
		expectedSecond = second.copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(classModelClasses);
		expectedSecond.extend(class8, attribute4, classEncapsulates);
		expectedSecond.extend(class9, attribute5, classEncapsulates);
		
		expectedSplit = new Pair<View, View>(expectedFirst, expectedSecond);
		
		runSplitSearchSpaceElement(problemPartView, problemSplit, Crossover.DEFAULT_STRATEGY, subMetamodel, expectedSplit);
		
	}
	
	/**
	 * Test method for {@link Crossover#splitSearchSpaceElement(View,Pair)}.
	 */
	@Test
	final void testSplitSearchSpaceElementEmptySubMetamodel() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException, ViewSetOperationException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
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
		
		// create empty sub metamodel
		View subMetamodel = new View(CRA_ECORE);
		
		// create problemView and split
		List<EClass> problemPartClasses = List.of(classModelEClass, attributeEClass, methodEClass);
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
		second.extend(classModel1, method2, classModelFeatures);
		second.extend(classModelClasses);
		Pair<View, View> problemSplit = new Pair<View, View>(first, second);
		
		View expectedFirst = first.copy();
		expectedFirst.extend(class8);
		expectedFirst.extendByMissingEdges();
		expectedFirst.reduce(classModel1, class8, classModelClasses);
		
		View expectedSecond = second.copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(class8, attribute4, classEncapsulates);
		expectedSecond.extend(class9, attribute5, classEncapsulates);
		Pair<View, View> expectedSplit = new Pair<View, View>(expectedFirst, expectedSecond);
		
		runSplitSearchSpaceElement(problemPartView, problemSplit, Crossover.DEFAULT_STRATEGY, subMetamodel, expectedSplit);
		
		//  classModelClasses EReference in problem part different problem split
		first = problemPartView.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		first.extend(classModel1, class8, classModelClasses);
		
		second = problemPartView.copy();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2);
		second.extend(method2, attribute4, methodDataDependency);
		second.extend(classModel1, method2, classModelFeatures);
		second.extend(classModel1, class9, classModelClasses);
		problemSplit = new Pair<View, View>(first, second);
		
		expectedFirst = first.copy();
		expectedFirst.extend(class8);
		expectedFirst.extendByMissingEdges();
		
		expectedSecond = second.copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(classModelClasses);
		expectedSecond.extend(class8, attribute4, classEncapsulates);
		expectedSecond.extend(class9, attribute5, classEncapsulates);
		expectedSplit = new Pair<View, View>(expectedFirst, expectedSecond);
		
		runSplitSearchSpaceElement(problemPartView, problemSplit, Crossover.DEFAULT_STRATEGY, subMetamodel, expectedSplit);
		
		//  classModelClasses EReference in solution part
		
		problemPartClasses = List.of(classModelEClass, attributeEClass, methodEClass);
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
		second.extend(classModel1, method2, classModelFeatures);
		
		problemSplit = new Pair<View, View>(first, second);
		
		expectedFirst = first.copy();
		expectedFirst.extend(classEClass);
		expectedFirst.extendByMissingEdges();
		
		expectedSecond = second.copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(classModelClasses);
		expectedSecond.extend(class8, attribute4, classEncapsulates);
		expectedSecond.extend(class9, attribute5, classEncapsulates);
		
		expectedSplit = new Pair<View, View>(expectedFirst, expectedSecond);
		
		runSplitSearchSpaceElement(problemPartView, problemSplit, Crossover.DEFAULT_STRATEGY, subMetamodel, expectedSplit);
		
	}
	
	/**
	 * Runs the {@link Crossover#splitSearchSpaceElement} method with the given {@link View problemPart} and {@link Pair problemSplit}.
	 * Asserts the returned split to be equal to the {@link Pair expectedSplit}.
	 * @param problemPart the {@link View view} of the problem part to use in the method call
	 * @param problemSplit the problemSplit-{@link Pair pair} to use in the method call
	 * @param subMetaModelOfIntersection the {@link View view} on the metamodel containing the sub-meta-model of the problem part intersection
	 * @param expectedSplit the expected problem split of the search space element given indirectly by the {@link View#resource resource}
	 * in of the {@link View problemPart}
	 */
	private void runSplitSearchSpaceElement (View problemPart, Pair<View, View> problemSplit, 
												SearchSpaceElementSplitStrategy strategy, View subMetaModelOfIntersection, Pair<View, View> expectedSplit) 
											throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
											ViewSetOperationException, NoSuchMethodException, SecurityException, InstantiationException {
		
		// get an instance of the private splitSearchSpaceElement method
		Class<Crossover> clazz = Crossover.class;
		Constructor<Crossover> crossoverConstructor = clazz.getDeclaredConstructor(new Class[0]);
		crossoverConstructor.setAccessible(true);
		Crossover crossover = crossoverConstructor.newInstance();
		Method splitSearchSpaceElement = clazz.getDeclaredMethod("splitSearchSpaceElement", View.class, Pair.class, SearchSpaceElementSplitStrategy.class, View.class);
		splitSearchSpaceElement.setAccessible(true);
		
		// invoke the method
		@SuppressWarnings("unchecked")
		Pair<View, View> actualSearchSpaceElementSplit = (Pair<View, View>) splitSearchSpaceElement.invoke(crossover, problemPart, problemSplit, strategy, subMetaModelOfIntersection);
		
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
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		// EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference classEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		// create problemView
		List<EClass> problemPartClasses = List.of(classModelEClass, attributeEClass, methodEClass);
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
	 * Test method for {@link Crossover}. Tests the
	 * complete procedure.
	 */
	@Test
	final void testCrossover() throws CrossoverUsageException, ViewSetOperationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		EReference classEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		
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
		EObject attribute4SSEOne = map.get(2).iterator().next();
		EObject attribute5SSEOne = map.get(1).iterator().next();
		EObject class8SSEOne = map.get(3).iterator().next();
		EObject class9SSEOne = map.get(4).iterator().next();
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
		EObject attribute4SSETwo = map.get(2).iterator().next();
		EObject attribute5SSETwo = map.get(1).iterator().next();
		// EObject class6SSETwo = map.get(3).iterator().next();
		EObject class7SSETwo = map.get(4).iterator().next();
		EObject classModel1SSETwo = map.get(5).iterator().next();
		
		// create problemView and border
		List<EClass> problemPartEClasses = List.of(classModelEClass, attributeEClass, methodEClass);
		List<EReference> problemPartEReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		Pair<Resource, Resource> searchSpaceElements = new Pair<Resource, Resource>(CRA_INSTANCE_ONE, CRA_INSTANCE_TWO);
		
		Strategy problemPartSplitStrategy = (View view) -> {
			view.reduce(attributeEClass);
		};
		
		Crossover crossover = new Crossover(CRA_ECORE, searchSpaceElements, problemPartSplitStrategy, problemPartEClasses, problemPartEReferences, Crossover.DEFAULT_STRATEGY);
		
		View problemPartSSETwo = new View(CRA_INSTANCE_TWO);
		problemPartEClasses.forEach(eClass -> problemPartSSETwo.extend(eClass));
		problemPartEReferences.forEach(eReference -> problemPartSSETwo.extend(eReference));
		
		assertFieldIsCorrect (crossover, "problemPartSSEOne", (actualValue) -> {
			assertTrue(problemPartSSETwo.equals(actualValue));	
		});
		
		View problemPartSSEOne = new View(CRA_INSTANCE_ONE);
		problemPartEClasses.forEach(eClass -> problemPartSSEOne.extend(eClass));
		problemPartEReferences.forEach(eReference -> problemPartSSEOne.extend(eReference));
		
		assertFieldIsCorrect (crossover, "problemPartSSETwo", (actualValue) -> {
			assertTrue(problemPartSSEOne.equals(actualValue));
		});
		
		View first = problemPartSSETwo.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		
		View second = problemPartSSETwo.copy();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2SSETwo);
		second.extend(method2SSETwo, attribute4SSETwo, methodDataDependency);
		second.extend(classModel1SSETwo, method2SSETwo, classModelFeatures);
		
		Pair<View, View> problemSplitSSETwo = new Pair<View, View>(first, second);
		
		assertFieldIsCorrect (crossover, "problemSplitSSEOne", (actualValue) -> {
			assertTrue(problemSplitSSETwo.equals(actualValue));
		});
		
		first = problemPartSSEOne.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		
		second = problemPartSSEOne.copy();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2SSEOne);
		second.extend(method2SSEOne, attribute4SSEOne, methodDataDependency);
		second.extend(classModel1SSEOne, method2SSEOne, classModelFeatures);
		
		Pair<View, View> problemSplitSSEOne = new Pair<View, View>(first, second);
		
		assertFieldIsCorrect (crossover, "problemSplitSSETwo", (actualValue) -> {
			assertTrue(problemSplitSSEOne.equals(actualValue));
		});
		
		View expectedFirst = problemSplitSSETwo.getFirst().copy();
		expectedFirst.extend(classEClass);
		expectedFirst.extendByMissingEdges();
		
		View expectedSecond = problemSplitSSETwo.getSecond().copy();
		expectedSecond.extend(class7SSETwo);
		expectedSecond.extend(class7SSETwo, attribute4SSETwo, classEncapsulates);
		expectedSecond.extend(class7SSETwo, attribute5SSETwo, classEncapsulates);
		expectedSecond.extend(classModel1SSETwo, class7SSETwo, classModelClasses);
		
		Pair<View, View> splitOfSSETwo = new Pair<View, View>(expectedFirst, expectedSecond);
		
		assertFieldIsCorrect (crossover, "splitOfSSEOne", (actualValue) -> {
			assertTrue(splitOfSSETwo.equals(actualValue));
		});
		
		expectedFirst = problemSplitSSEOne.getFirst().copy();
		expectedFirst.extend(classEClass);
		expectedFirst.extendByMissingEdges();
		
		expectedSecond = problemSplitSSEOne.getSecond().copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(class8SSEOne, attribute4SSEOne, classEncapsulates);
		expectedSecond.extend(class9SSEOne, attribute5SSEOne, classEncapsulates);
		expectedSecond.extend(classModelClasses);
		
		Pair<View, View> splitOfSSEOne = new Pair<View, View>(expectedFirst, expectedSecond);
		
		assertFieldIsCorrect (crossover, "splitOfSSETwo", (actualValue) -> {
			assertTrue(splitOfSSEOne.equals(actualValue));
		});
		
		assertFieldIsCorrect (crossover, "problemPartIntersection", (actualValue) -> {
			
			View problemPartIntersection = new View(CRA_INSTANCE_TWO);
			problemPartIntersection.extend(classModel1SSETwo);
			problemPartIntersection.extend(method2SSETwo);
			problemPartIntersection.extend(classModel1SSETwo, method2SSETwo, classModelFeatures);
			
			assertTrue(problemPartIntersection.equals(actualValue));
			
		});
		
		assertFieldIsCorrect (crossover, "intersectionOfSSEOne", (actualValue) -> {
			
			View intersectionOfSSETwo = new View(CRA_INSTANCE_TWO);
			intersectionOfSSETwo.extend(classModel1SSETwo);
			intersectionOfSSETwo.extend(method2SSETwo);
			intersectionOfSSETwo.extend(class7SSETwo);
			intersectionOfSSETwo.extend(classModel1SSETwo, class7SSETwo, classModelClasses);
			intersectionOfSSETwo.extend(classModel1SSETwo, method2SSETwo, classModelFeatures);
			
			assertTrue(intersectionOfSSETwo.equals(actualValue));
			
		});
		
		assertFieldIsCorrect (crossover, "intersectionOfSSETwo", (actualValue) -> {
			
			View intersectionOfSSEOne = new View(CRA_INSTANCE_ONE);
			intersectionOfSSEOne.extend(classModel1SSEOne);
			intersectionOfSSEOne.extend(method2SSEOne);
			intersectionOfSSEOne.extend(classEClass);
			intersectionOfSSEOne.extend(classModelClasses);
			intersectionOfSSEOne.extend(classModel1SSEOne, method2SSEOne, classModelFeatures);
			
			assertTrue(intersectionOfSSEOne.equals(actualValue));
			
		});
		
		Iterator<Pair<Resource, Resource>> iterator = crossover.iterator();
		List<Pair<Resource, Resource>> foundCrossoverPairs = new ArrayList<Pair<Resource, Resource>>();
		
		while (iterator.hasNext()) {
			Pair<Resource, Resource> pair = (Pair<Resource, Resource>) iterator.next();
			assertNotNull(pair);
			assertNotNull(pair.getFirst());
			assertNotNull(pair.getSecond());
			assertEquals(1, pair.getFirst().getContents().size());
			assertEquals(1, pair.getSecond().getContents().size());
			
			foundCrossoverPairs.add(
					new Pair<Resource, Resource>(
							pair.getFirst(),
							pair.getSecond()
						)
			);
		}
		
		assertEquals(5, foundCrossoverPairs.size());
		
		/**
		 * CRACrossoverTest3 comes first because the intersection is build upon the smallest subset.
		 * After CRACrossoverTest3 comes CRACrossoverTest1 and CRACrossoverTest2 their order is determined
		 * on whether the class of the intersection is first matched with class8 or class9 so I just tried it out.
		 * The last two found crossovers pairs are exactly the same as the ones before them as the inclution of the
		 * classModelClasses EReference in the intersection does not change the crossovers.
		 */
		
		for (int i = 0; i < 5; i++) {
			
			View firstView = new View(foundCrossoverPairs.get(i).getFirst());
			firstView.extendByAllNodes();
			firstView.extendByMissingEdges();
			View secondView = new View(foundCrossoverPairs.get(i).getSecond());
			secondView.extendByAllNodes();
			secondView.extendByMissingEdges();
			
			switch (i + 1) {
				case 1:
					assertExistsOnlyOneBijection(CRACrossoverTest3_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest3_2, secondView);
				break;
				
				case 2:
					assertExistsOnlyOneBijection(CRACrossoverTest2_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest2_2, secondView);
				break;
				
				case 3:
					assertExistsOnlyOneBijection(CRACrossoverTest1_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest1_2, secondView);
				break;
				
				case 4:
					assertExistsOnlyOneBijection(CRACrossoverTest2_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest2_2, secondView);
				break;
				
				case 5:
					assertExistsOnlyOneBijection(CRACrossoverTest1_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest1_2, secondView);
				break;
			}
			
		}
		
	}
	
	/**
	 * Test method for {@link Crossover}. Tests the
	 * complete procedure with a subMetaModelOfIntersection parameter.
	 */
	@Test
	final void testCrossoverWithSubMetaModelOfIntersection() throws CrossoverUsageException, ViewSetOperationException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElementEClass, "name");
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		EReference classEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		
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
		EObject attribute4SSEOne = map.get(2).iterator().next();
		EObject attribute5SSEOne = map.get(1).iterator().next();
		EObject class8SSEOne = map.get(3).iterator().next();
		EObject class9SSEOne = map.get(4).iterator().next();
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
		EObject attribute4SSETwo = map.get(2).iterator().next();
		EObject attribute5SSETwo = map.get(1).iterator().next();
		// EObject class6SSETwo = map.get(3).iterator().next();
		EObject class7SSETwo = map.get(4).iterator().next();
		EObject classModel1SSETwo = map.get(5).iterator().next();
		
		// create problemView and border
		List<EClass> problemPartEClasses = List.of(classModelEClass, attributeEClass, methodEClass);
		List<EReference> problemPartEReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		Pair<Resource, Resource> searchSpaceElements = new Pair<Resource, Resource>(CRA_INSTANCE_ONE, CRA_INSTANCE_TWO);
		
		Strategy problemPartSplitStrategy = (View view) -> {
			view.reduce(attributeEClass);
		};
		
		// create empty sub metamodel
		View subMetamodel = new View(CRA_ECORE);
		subMetamodel.extend((EObject) classModelEClass);
		subMetamodel.extend((EObject) classModelClasses);
		
		Crossover crossover = new Crossover(CRA_ECORE, searchSpaceElements, problemPartSplitStrategy, problemPartEClasses, problemPartEReferences, Crossover.DEFAULT_STRATEGY, subMetamodel);
		
		View problemPartSSETwo = new View(CRA_INSTANCE_TWO);
		problemPartEClasses.forEach(eClass -> problemPartSSETwo.extend(eClass));
		problemPartEReferences.forEach(eReference -> problemPartSSETwo.extend(eReference));
		
		assertFieldIsCorrect (crossover, "problemPartSSEOne", (actualValue) -> {
			assertTrue(problemPartSSETwo.equals(actualValue));	
		});
		
		View problemPartSSEOne = new View(CRA_INSTANCE_ONE);
		problemPartEClasses.forEach(eClass -> problemPartSSEOne.extend(eClass));
		problemPartEReferences.forEach(eReference -> problemPartSSEOne.extend(eReference));
		
		assertFieldIsCorrect (crossover, "problemPartSSETwo", (actualValue) -> {
			assertTrue(problemPartSSEOne.equals(actualValue));
		});
		
		View first = problemPartSSETwo.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		
		View second = problemPartSSETwo.copy();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2SSETwo);
		second.extend(method2SSETwo, attribute4SSETwo, methodDataDependency);
		second.extend(classModel1SSETwo, method2SSETwo, classModelFeatures);
		
		Pair<View, View> problemSplitSSETwo = new Pair<View, View>(first, second);
		
		assertFieldIsCorrect (crossover, "problemSplitSSEOne", (actualValue) -> {
			assertTrue(problemSplitSSETwo.equals(actualValue));
		});
		
		first = problemPartSSEOne.copy();
		first.reduce(attributeEClass);
		first.removeDangling();
		
		second = problemPartSSEOne.copy();
		second.reduce(methodEClass);
		second.removeDangling();
		second.extend(method2SSEOne);
		second.extend(method2SSEOne, attribute4SSEOne, methodDataDependency);
		second.extend(classModel1SSEOne, method2SSEOne, classModelFeatures);
		
		Pair<View, View> problemSplitSSEOne = new Pair<View, View>(first, second);
		
		assertFieldIsCorrect (crossover, "problemSplitSSETwo", (actualValue) -> {
			assertTrue(problemSplitSSEOne.equals(actualValue));
		});
		
		View expectedFirst = problemSplitSSETwo.getFirst().copy();
		expectedFirst.extend(classEClass);
		expectedFirst.extendByMissingEdges();
		
		View expectedSecond = problemSplitSSETwo.getSecond().copy();
		expectedSecond.extend(class7SSETwo);
		expectedSecond.extend(class7SSETwo, attribute4SSETwo, classEncapsulates);
		expectedSecond.extend(class7SSETwo, attribute5SSETwo, classEncapsulates);
		expectedSecond.extend(classModel1SSETwo, class7SSETwo, classModelClasses);
		
		Pair<View, View> splitOfSSETwo = new Pair<View, View>(expectedFirst, expectedSecond);
		
		assertFieldIsCorrect (crossover, "splitOfSSEOne", (actualValue) -> {
			assertTrue(splitOfSSETwo.equals(actualValue));
		});
		
		expectedFirst = problemSplitSSEOne.getFirst().copy();
		expectedFirst.extend(classEClass);
		expectedFirst.extendByMissingEdges();
		
		expectedSecond = problemSplitSSEOne.getSecond().copy();
		expectedSecond.extend(classEClass);
		expectedSecond.extend(class8SSEOne, attribute4SSEOne, classEncapsulates);
		expectedSecond.extend(class9SSEOne, attribute5SSEOne, classEncapsulates);
		expectedSecond.extend(classModelClasses);
		
		Pair<View, View> splitOfSSEOne = new Pair<View, View>(expectedFirst, expectedSecond);
		
		assertFieldIsCorrect (crossover, "splitOfSSETwo", (actualValue) -> {
			assertTrue(splitOfSSEOne.equals(actualValue));
		});
		
		assertFieldIsCorrect (crossover, "problemPartIntersection", (actualValue) -> {
			
			View problemPartIntersection = new View(CRA_INSTANCE_TWO);
			problemPartIntersection.extend(classModel1SSETwo);
			problemPartIntersection.extend(method2SSETwo);
			problemPartIntersection.extend(classModel1SSETwo, method2SSETwo, classModelFeatures);
			
			assertTrue(problemPartIntersection.equals(actualValue));
			
		});
		
		assertFieldIsCorrect (crossover, "intersectionOfSSEOne", (actualValue) -> {
			
			View intersectionOfSSETwo = new View(CRA_INSTANCE_TWO);
			intersectionOfSSETwo.extend(classModel1SSETwo);
			intersectionOfSSETwo.extend(method2SSETwo);
			intersectionOfSSETwo.extend(class7SSETwo);
			intersectionOfSSETwo.extend(classModel1SSETwo, class7SSETwo, classModelClasses);
			intersectionOfSSETwo.extend(classModel1SSETwo, method2SSETwo, classModelFeatures);
			
			assertTrue(intersectionOfSSETwo.equals(actualValue));
			
		});
		
		assertFieldIsCorrect (crossover, "intersectionOfSSETwo", (actualValue) -> {
			
			View intersectionOfSSEOne = new View(CRA_INSTANCE_ONE);
			intersectionOfSSEOne.extend(classModel1SSEOne);
			intersectionOfSSEOne.extend(method2SSEOne);
			intersectionOfSSEOne.extend(classEClass);
			intersectionOfSSEOne.extend(classModelClasses);
			intersectionOfSSEOne.extend(classModel1SSEOne, method2SSEOne, classModelFeatures);
			
			assertTrue(intersectionOfSSEOne.equals(actualValue));
			
		});
		
		Iterator<Pair<Resource, Resource>> iterator = crossover.iterator();
		List<Pair<Resource, Resource>> foundCrossoverPairs = new ArrayList<Pair<Resource, Resource>>();
		
		while (iterator.hasNext()) {
			Pair<Resource, Resource> pair = (Pair<Resource, Resource>) iterator.next();
			assertNotNull(pair);
			assertNotNull(pair.getFirst());
			assertNotNull(pair.getSecond());
			assertEquals(1, pair.getFirst().getContents().size());
			assertEquals(1, pair.getSecond().getContents().size());
			
			foundCrossoverPairs.add(
					new Pair<Resource, Resource>(
							pair.getFirst(),
							pair.getSecond()
						)
			);
		}
		
		assertEquals(5, foundCrossoverPairs.size());
		
		/**
		 * CRACrossoverTest3 comes first because the intersection is build upon the smallest subset.
		 * After CRACrossoverTest3 comes CRACrossoverTest1 and CRACrossoverTest2 their order is determined
		 * on whether the class of the intersection is first matched with class8 or class9 so I just tried it out.
		 * The last two found crossovers pairs are exactly the same as the ones before them as the inclution of the
		 * classModelClasses EReference in the intersection does not change the crossovers.
		 */
		
		for (int i = 0; i < 5; i++) {
			
			View firstView = new View(foundCrossoverPairs.get(i).getFirst());
			firstView.extendByAllNodes();
			firstView.extendByMissingEdges();
			View secondView = new View(foundCrossoverPairs.get(i).getSecond());
			secondView.extendByAllNodes();
			secondView.extendByMissingEdges();
			
			switch (i + 1) {
				case 1:
					assertExistsOnlyOneBijection(CRACrossoverTest3_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest3_2, secondView);
				break;
				
				case 2:
					assertExistsOnlyOneBijection(CRACrossoverTest2_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest2_2, secondView);
				break;
				
				case 3:
					assertExistsOnlyOneBijection(CRACrossoverTest1_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest1_2, secondView);
				break;
				
				case 4:
					assertExistsOnlyOneBijection(CRACrossoverTest2_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest2_2, secondView);
				break;
				
				case 5:
					assertExistsOnlyOneBijection(CRACrossoverTest1_1, firstView);
					assertExistsOnlyOneBijection(CRACrossoverTest1_2, secondView);
				break;
			}
			
		}
		
	}
	
	/**
	 * Use the {@link Engine} to find a bijective {@link Match} between the 
	 * {@link Resource expected} model and the {@link View actual} model.
	 * @param expected a {@link Resource} containing the expected crossover part
	 * @param actual a complete {@link View} over the actual crossover part
	 */
	private Match assertExistsOnlyOneBijection (Resource expected, View actual) {
		
		// find an isomorphism from actual to expected
		Rule rule = new RuleImpl();
		rule.setLhs(actual.getGraph());
		rule.setInjectiveMatching(true);
		EGraph eGraph = new EGraphImpl(expected.getContents());
		
		Engine engine = new EngineImpl();
		
		Iterator<Match> matchIterator = engine.findMatches(rule, eGraph, null).iterator();
		
		assertTrue(matchIterator.hasNext());
		Match match = matchIterator.next();
		assertFalse(matchIterator.hasNext());
		assertEquals(eGraph.size(), match.getNodeTargets().size());
		
		return match;
		
	}
	
	/**
	 * Get a private field from the given {@link Crossover crossover} by its {@literal fieldName} and check
	 * the value for correctness.
	 * @param crossover the {@link Crossover} to get the value from
	 * @param fieldName the name of the field
	 * @param assertCorrectness the correctness criterion
	 */
	private void assertFieldIsCorrect (Crossover crossover, String fieldName, Consumer<Object> assertCorrectness) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		
		Class<Crossover> clazz = Crossover.class;
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		Object actualValue = field.get(crossover);
		assertCorrectness.accept(actualValue);
		
	}
	
}
