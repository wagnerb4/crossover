package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Benjamin Wagner
 *
 */
public class TestResources {

	protected static final String RESOURCE_PATH = "test/resources";
	protected static Resource SCRUM_PLANNIG_ECORE;
	protected static Resource SCRUM_PLANNIG_INSTANCE_ONE;
	protected static Resource SCRUM_PLANNIG_INSTANCE_TWO;
	protected static Resource SCRUM_PLANNIG_INSTANCE_THREE;
	protected static Resource CRA_ECORE;
	protected static Resource CRA_INSTANCE_ONE;
	protected static Resource CRA_INSTANCE_TWO;

	// setup
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		HenshinResourceSet resourceSet = new HenshinResourceSet(RESOURCE_PATH);
		SCRUM_PLANNIG_ECORE = resourceSet.getResource("scrumPlanning.ecore");
		SCRUM_PLANNIG_INSTANCE_ONE = resourceSet.getResource("scrumPlanningInstanceOne.xmi");
		SCRUM_PLANNIG_INSTANCE_TWO = resourceSet.getResource("scrumPlanningInstanceTwo.xmi");
		SCRUM_PLANNIG_INSTANCE_THREE = resourceSet.getResource("scrumPlanningInstanceThree.xmi");
		CRA_ECORE = resourceSet.getResource("CRA.ecore");
		CRA_INSTANCE_ONE = resourceSet.getResource("CRAInstanceOne.xmi");
		CRA_INSTANCE_TWO = resourceSet.getResource("CRAInstanceTwo.xmi");
	}
	
	// helper methods and tests of the helper methods
	
	/**
	 * A helper method to get a {@link EReference eReference} from an {@link EClass eClass} by its name.
	 * If the given {@link EClass eClass} doesn't contain the named {@link EReference eReference} the test
	 * that called this method will fail.
	 * @param eClass the {@link EClass eClass} affiliated to the {@link EReference eReference}
	 * @param name the {@link EReference#getName() name} of the {@link EReference eReference}
	 * @return Returns the {@link EReference eReference}.
	 */
	final protected EReference getEReferenceFromEClass (EClass eClass, String name) {
		
		List<EReference> eReferences = eClass.getEAllReferences().stream().
				filter(eReference -> eReference.getName().equals(name)).
				collect(Collectors.toList());
		assertEquals(1, eReferences.size());
		
		return eReferences.get(0);
		
	}
	
	/**
	 * Test for the internal method {@link ViewTest#getEReferenceFromEClass(EClass, String)}.
	 */
	@Test
	final void testGetEReferenceFromEClass () {
		
		EClass classEClass = getEClassFromResource(CRA_ECORE, "Class")[0];
		EReference encapsulates = getEReferenceFromEClass(classEClass, "encapsulates");
		assertEquals(encapsulates.getName(), "encapsulates");
		assertEquals(encapsulates.getEContainingClass(), classEClass);
		
	}
	
	/**
	 * A helper method to get a {@link EAttribute eAttribute} fron an {@link EClass eClass} by name.
	 * If the given {@link EClass eClass} doesn't contain the named {@link EAttribute eAttribute} the test
	 * that called this method will fail.
	 * @param eClass the {@link EClass eClass} affiliated to the {@link EAttribute eAttribute}
	 * @param name the {@link EAttribute#getName() name} of the {@link EAttribute eAttribute}
	 * @return Returns the {@link EAttribute eAttribute}.
	 */
	final protected EAttribute getEAttributeFromEClass (EClass eClass, String name) {
		
		List<EAttribute> eAttributes = eClass.getEAllAttributes().stream().
				filter(eAttribute -> eAttribute.getName().equals(name)).
				collect(Collectors.toList());
		assertEquals(1, eAttributes.size());
		
		return eAttributes.get(0);
		
	}
	
	/**
	 * Test for the internal method {@link ViewTest#getEAttributeFromEClass(EClass, String)}.
	 */
	@Test
	final void testGetEAttributeFromEClass () {
		
		EClass namedElement = getEClassFromResource(CRA_ECORE, "NamedElement")[0];
		EAttribute nameEAttribute = getEAttributeFromEClass(namedElement, "name");
		assertEquals(nameEAttribute.getName(), "name");
		assertEquals(nameEAttribute.getEContainingClass(), namedElement);
		
	}
	
	/**
	 * @param resource the {@link Resource} of the meta-model
	 * @param names the names of the eClasses to return
	 * @return Returns an array of the {@link EClass eClasses} corresponding to the given names.
	 */
	final protected EClass[] getEClassFromResource (Resource resource, String... names) {
		
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
	 * A helper method to get {@link EObject eObjects} form a {@link Resource resource}.
	 * @param resource the {@link Resource} of the model
	 * @param predicates a list of predicates for evaluating the {@link EObject eObjects} to be returned 
	 * @return Returns a list of sets containing the {@link EObject eObjects} matched by the given predicates.
	 */
	@SafeVarargs
	final protected Map<Integer, Set<EObject>> getEObjectsFromResource (Resource resource, Predicate<EObject>... predicates) {
	
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
		
		for (int i = 0; i < predicates.length; i++) {
			if (eObjectSets.get(i) == null)
				eObjectSets.put(i, new HashSet<EObject>());
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
		
		Map<Integer, Set<EObject>> mapOfSets = getEObjectsFromResource (
				SCRUM_PLANNIG_INSTANCE_ONE, 
				eObject -> eObject.eClass() == stakeholder, 
				eObject -> eObject.eClass() == backlog
		);
		
		assertEquals(expectedStakeholderEObject, mapOfSets.get(0));
		assertEquals(expectedBacklogEObjects, mapOfSets.get(1));
		
	}

}
