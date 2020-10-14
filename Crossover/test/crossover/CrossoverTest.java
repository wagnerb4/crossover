/**
 * 
 */
package crossover;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import view.TestResources;
import view.View;

/**
 * @author benjamin
 *
 */
class CrossoverTest extends TestResources {

	/**
	 * Test method for {@link Crossover#findBorder(Resource,List,List)}.
	 */
	@Test
	final void testFindBorder() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		
		// get ProblemPartEClasses and ProblemPartEReferences from CRA_ECORE
		EClass[] eClasses = getEClassFromResource(CRA_ECORE, "NamedElement", "ClassModel", "Class", "Feature", "Attribute", "Method");
		EClass namedElementEClass = eClasses[0];
		EClass classModelEClass = eClasses[1];
		EClass classEClass = eClasses[2];
		EClass featureEClass = eClasses[3];
		EClass attributeEClass = eClasses[4];
		EClass methodEClass = eClasses[5];
		
		EReference classModelClasses = getEReferenceFromEClass(classModelEClass, "classes");
		EReference classModelFeatures = getEReferenceFromEClass(classModelEClass, "features");
		EReference classClassEncapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		EReference featureIsEncapsulatedBy = classClassEncapsulates.getEOpposite();
		EReference methodDataDependency = getEReferenceFromEClass(methodEClass, "dataDependency");
		EReference methodFunctionalDependency = getEReferenceFromEClass(methodEClass, "functionalDependency");
		
		List<EClass> problemPartEClasses = List.of(namedElementEClass, classModelEClass, featureEClass, attributeEClass, methodEClass);
		List<EReference> problemPartEReferences = List.of(classModelFeatures, methodDataDependency, methodFunctionalDependency);
		
		Class<Crossover> clazz = Crossover.class;
		Constructor<Crossover> crossoverConstructor = clazz.getDeclaredConstructor(new Class[0]);
		crossoverConstructor.setAccessible(true);
		Crossover crossover = crossoverConstructor.newInstance();
		Method findBorder = clazz.getDeclaredMethod("findBorder", Resource.class, List.class, List.class);
		findBorder.setAccessible(true);
		View view = (View) findBorder.invoke(crossover, CRA_ECORE, problemPartEClasses, problemPartEReferences);
		
		Collection<EObject> eObjects = view.getContainedEObjects();
		assertTrue(eObjects.contains((EObject) classModelEClass));
		assertTrue(eObjects.contains((EObject) attributeEClass));
		assertTrue(eObjects.contains((EObject) methodEClass));
		
	}
	
	/**
	 * Test method for {@link Crossover#splitSearchSpaceElement(View,Pair)}.
	 */
	@Test
	final void testSplitSearchSpaceElement() {
		fail("Not yet implemented"); // TODO
	}
	
	/**
	 * Test method for {@link Crossover#splitProblemPart(View,View,Strategy)}.
	 */
	@Test
	final void testSplitProblemPart() {
		fail("Not yet implemented"); // TODO
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
