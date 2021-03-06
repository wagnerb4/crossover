package crossover;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;

import view.View;
import view.ViewSetOperationException;

/**
 * A Class for the simplified usage of the {@link Crossover Crossover-Class}.
 * @author Benjamin Wagner
 */
public class SimpleCrossover implements Runnable {
	
	/**
	 * Files with these extensions are recognices as resources.
	 */
	private final List<String> SUPPORTED_RESOURCE_EXTENSIONS = List.of("xmi", "ecore");
	
	private final HenshinResourceSet inputResourceSet;
	
	private final HenshinResourceSet outputResourceSet;
	
	/**
	 * The {@link Map} that holds the loaded resources.
	 * The keys (e.g resource names) are the respective filenames without the filename extension.
	 */
	private final Map<String, Resource> loadedResources;
	
	/**
	 * A map that contains the resource names from earch non-metamodel resource to thier
	 * respective metamodels.
	 */
	private final Map<String, String> resourceMetamodels;
	
	/**
	 * A map saving the {@link EClass eClasses} of the problem part in the meta-model with the respective resource name.
	 */
	private final Map<String, List<EClass>> metamodelProblemPartEClasses;
	
	/**
	 * A map saving the {@link EReference eReferences} of the problem part in the meta-model with the respective resource name.
	 */
	private final Map<String, List<EReference>> metamodelProblemPartEReferences;
	
	private final List<Crossover> crossovers;
	private final List<String> crossoverNames;
	private final File outputDirectory;
	
	private BiConsumer<Pair<Resource, Resource>, Pair<String, Integer>> whenDone;
	
	/**
	 * Creates a new instance of the {@link SimpleCrossover} class.
	 * @param resourceDirectoryPath the path to the directory to non-recursively scan for resources to use in the crossovers 
	 * and write the computed crossover pairs to (if no different behaviour is specified by the {@link SimpleCrossover#setWhenDone(BiConsumer) whenDone-function})
	 */
	public SimpleCrossover(String resourceDirectoryPath) {
		this(resourceDirectoryPath, resourceDirectoryPath);
	}
	
	/**
	 * Creates a new instance of the {@link SimpleCrossover} class.
	 * @param resourceInputDirectoryPath the path to the directory to non-recursively scan for resources to use in the crossovers
	 * @param resourceOutputDirectoryPath the path to the directory to write the computed crossover pairs to
	 * (if no different behaviour is specified by the {@link SimpleCrossover#setWhenDone(BiConsumer) whenDone-function})
	 */
	public SimpleCrossover (String resourceInputDirectoryPath, String resourceOutputDirectoryPath) {
		
		File inputDirectory = new File(resourceInputDirectoryPath);
		if (!inputDirectory.isDirectory()) throw new IllegalArgumentException("The given resourceInputDirectoryPath does not exist or is not a directory.");
		
		this.outputDirectory = new File(resourceOutputDirectoryPath);
		if (!this.outputDirectory.isDirectory()) throw new IllegalArgumentException("The given resourceInputDirectoryPath does not exist or is not a directory.");
		
		this.inputResourceSet =  new HenshinResourceSet(resourceInputDirectoryPath);
		this.outputResourceSet = new HenshinResourceSet(resourceOutputDirectoryPath);
		
		
		this.whenDone = (pairOfResources, identifierPair) -> {
			String name = identifierPair.getFirst();
			int crossoverNumber = identifierPair.getSecond();
			outputResourceSet.saveEObject(pairOfResources.getFirst().getContents().get(0), name + "No" + crossoverNumber + "First.xmi");
			outputResourceSet.saveEObject(pairOfResources.getSecond().getContents().get(0), name + "No" + crossoverNumber + "Second.xmi");
		};
		
		this.loadedResources = new HashMap<>();
		this.crossovers = new ArrayList<>();
		this.crossoverNames =  new ArrayList<>();
		this.resourceMetamodels = new HashMap<>();
		this.metamodelProblemPartEClasses = new HashMap<>();
		this.metamodelProblemPartEReferences = new HashMap<>();
		
		File[] contentsOfDirectory = inputDirectory.listFiles(file -> {
			if (!file.isFile()) return false;
			String extension = FilenameUtils.getExtension(file.getPath()).toLowerCase();
			return SUPPORTED_RESOURCE_EXTENSIONS.contains(extension);
		});
		
		for (File resourceFile : contentsOfDirectory) {
			String filename = resourceFile.getName();
			String basename = FilenameUtils.getBaseName(filename);
			this.loadedResources.put(basename, inputResourceSet.getResource(filename));
		}
		
	}

	/**
	 * Creates a unique name for the crossover of the two given resource names
	 * and adds it to the {@link SimpleCrossover#crossoverNames}.
	 * The name consists of "resourceOneNameXresourceTwoName" if it is already unique and otherwise
	 * "resourceOneNameXresourceTwoNameVerN" where N ist the smalest number greater than or equals to one such that the name is unique.
	 * @param resourceOneName the name (as in the {@link SimpleCrossover#loadedResources} map) of the first resource used for the crossover
	 * @param resourceTwoName the name (as in the {@link SimpleCrossover#loadedResources} map) of the second resource used for the crossover
	 */
	private void createCrossoverName (String resourceOneName, String resourceTwoName) {
		
		String crossoverName = resourceOneName + "X" + resourceTwoName;
		
		if (this.crossoverNames.contains(crossoverName)) {
			crossoverName += "Ver";
			int ver = 0;
			
			do {
				ver++;
			} while (this.crossoverNames.contains(crossoverName + ver));
			
			crossoverName += ver;
		}
		
		crossoverNames.add(crossoverName);
		
	}
		
	/**
	 * Set the whenDone function that is executed for each produced crossover pair.
	 * The first parameter of the function is the pair of resources produced by the crossover and the
	 * second parameter is a String-Integer-Pair that uniquely identifies the crossover-pair. 
	 * It consists of the {@link SimpleCrossover#createCrossoverName(String, String) crossover's name} and 
	 * the counter of the produced pair.
	 * @param whenDone the whenDone to set
	 */
	public void setWhenDone(BiConsumer<Pair<Resource, Resource>, Pair<String, Integer>> whenDone) {
		this.whenDone = whenDone;
	}

	/**
	 * Defines the resource by the given name as a metamodel.
	 * @param metamodelResourceName the name of the resource as in the {@link SimpleCrossover#loadedResources} map
	 * @param modelResourceNames the names of the resources with models complying to this meta-model
	 * @param problemPartElementNames a list defining the {@link EClass eClasses} and {@link EReference eReferences}
	 * that belong to the problem part of this meta-model <br/> <hr/>
	 * Earch entry of the list contains the name of an {@link EClass eClass} and a list with the names
	 * of the {@link EReference eReferences} of that {@link EClass eClass} wich also belong to the problem part. <hr/><br/>
	 */
	public void defineMetamodel (
			String metamodelResourceName, 
			List<String> modelResourceNames,
			List<Pair<String, List<String>>> problemPartElementNames) {
		
		if (!loadedResources.containsKey(metamodelResourceName)) throw new IllegalArgumentException("Found no loaded resource by that name.");
		
		modelResourceNames.forEach(string -> resourceMetamodels.put(string, metamodelResourceName));
		
		List<EClass> problemPartEClasses = new ArrayList<>();
		List<EReference> problemPartEReferences = new ArrayList<>();
		
		TreeIterator<EObject> treeIterator = loadedResources.get(metamodelResourceName).getAllContents();
		
		while (treeIterator.hasNext()) {
			EObject eObject = (EObject) treeIterator.next();
			
			if (eObject instanceof EClass) {
				EClass eClass = (EClass) eObject;
				
				for (Pair<String, List<String>> pair : problemPartElementNames) {
					if (eClass.getName().equals(pair.getFirst())) {
						problemPartEClasses.add(eClass);
						for (String eReferenceName : pair.getSecond()) {
							List<EReference> eReferences = eClass.getEAllReferences().stream().
									filter(eReference -> eReference.getName().equals(eReferenceName)).
									collect(Collectors.toList());
							if (eReferences.size() != 1) throw new IllegalArgumentException("The eClass " + pair.getFirst() + 
									" does not have an eReference by the name of " + eReferenceName);
							problemPartEReferences.add(eReferences.get(0));
						}
					}
				}
			}
		}
		
		metamodelProblemPartEClasses.put(metamodelResourceName, problemPartEClasses);
		metamodelProblemPartEReferences.put(metamodelResourceName, problemPartEReferences);
	}
	
	// without subMetaModelOfIntersection
	
	/**
	 * Adds a new crossover. Uses the {@link Crossover#DEFAULT_STRATEGY default search space element split strategy} and the
	 * {@link SimpleCrossover#createRandomProblemSplitStrategy(double) default problem part split strategy} with a value of 0.8.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 */
	@SuppressWarnings("unchecked")
	public void addCrossover (String resourceOneName, String resourceTwoName) throws CrossoverUsageException, ViewSetOperationException {		
		addCrossover (resourceOneName, resourceTwoName, Collections.EMPTY_LIST);
	}
	
	/**
	 * Adds a new crossover. Uses the {@link SimpleCrossover#createRandomProblemSplitStrategy(double) default problem part split strategy} with a value of 0.8.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 * @param searchSpaceElementSplitStrategy the {@link SearchSpaceElementSplitStrategy} used for the crossover
	 */
	@SuppressWarnings("unchecked")
	public void addCrossover (String resourceOneName, String resourceTwoName, SearchSpaceElementSplitStrategy searchSpaceElementSplitStrategy) throws CrossoverUsageException, ViewSetOperationException {
		addCrossover (resourceOneName, resourceTwoName, searchSpaceElementSplitStrategy, Collections.EMPTY_LIST);
	}
	
	/**
	 * Adds a new crossover.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 * @param problemPartSplitStrategy the {@link Stragtegy} used for splitting up the problem part for the crossover
	 * @see Crossover#splitProblemPart
	 */
	@SuppressWarnings("unchecked")
	public void addCrossover (String resourceOneName, String resourceTwoName, Strategy problemPartSplitStrategy) throws CrossoverUsageException, ViewSetOperationException {
		addCrossover (resourceOneName, resourceTwoName, problemPartSplitStrategy, Collections.EMPTY_LIST);
	}
	
	/**
	 * Adds a new crossover.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 * @param problemPartSplitStrategy the {@link Stragtegy} used for splitting up the problem part for the crossover
	 * @param searchSpaceElementSplitStrategy the {@link SearchSpaceElementSplitStrategy} used for the crossover
	 * @see Crossover#splitProblemPart
	 */
	@SuppressWarnings("unchecked")
	public void addCrossover (String resourceOneName, String resourceTwoName, Strategy problemPartSplitStrategy, SearchSpaceElementSplitStrategy searchSpaceElementSplitStrategy) throws CrossoverUsageException, ViewSetOperationException {
		addCrossover(resourceOneName, resourceTwoName, problemPartSplitStrategy, searchSpaceElementSplitStrategy, Collections.EMPTY_LIST);
	}
	
	// with subMetaModelOfIntersection
	
	/**
	 * Adds a new crossover. Uses the {@link Crossover#DEFAULT_STRATEGY default search space element split strategy} and the
	 * {@link SimpleCrossover#createRandomProblemSplitStrategy(double) default problem part split strategy} with a value of 0.8.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 * @param subMetaModelOfIntersectionNames part of the meta-model of the resources to specify the intersections <br/> <hr/>
	 * Earch entry of the list contains the name of an {@link EClass eClass} and a list with the names
	 * of the {@link EReference eReferences} of that {@link EClass eClass} wich also belong to the problem part. <hr/><br/>
	 */
	public void addCrossover (String resourceOneName, String resourceTwoName, List<Pair<String, List<String>>> subMetaModelOfIntersectionNames) throws CrossoverUsageException, ViewSetOperationException {
		
		if (!loadedResources.containsKey(resourceOneName) || !loadedResources.containsKey(resourceTwoName)) throw new IllegalArgumentException("Unloaded resources given."); 
		if (!resourceMetamodels.containsKey(resourceOneName) || !resourceMetamodels.containsKey(resourceTwoName)) throw new IllegalArgumentException("Metamodel resources given."); 
		if (resourceMetamodels.get(resourceOneName) == null || resourceMetamodels.get(resourceTwoName) == null) throw new IllegalArgumentException("Resources with undefined metamodels given."); 
		if (!resourceMetamodels.get(resourceOneName).equals(resourceMetamodels.get(resourceTwoName))) throw new IllegalArgumentException("Resources with different metamodels given."); 
		
		Strategy problemPartSplitStrategy = SimpleCrossover.createRandomProblemSplitStrategy(0.8);
		
		addCrossover (resourceOneName, resourceTwoName, problemPartSplitStrategy, Crossover.DEFAULT_STRATEGY, subMetaModelOfIntersectionNames);
		
	}
	
	/**
	 * Adds a new crossover. Uses the {@link SimpleCrossover#createRandomProblemSplitStrategy(double) default problem part split strategy} with a value of 0.8.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 * @param searchSpaceElementSplitStrategy the {@link SearchSpaceElementSplitStrategy} used for the crossover
	 * @param subMetaModelOfIntersectionNames part of the meta-model of the resources to specify the intersections <br/> <hr/>
	 * Earch entry of the list contains the name of an {@link EClass eClass} and a list with the names
	 * of the {@link EReference eReferences} of that {@link EClass eClass} wich also belong to the problem part. <hr/><br/>
	 */
	public void addCrossover (String resourceOneName, String resourceTwoName, SearchSpaceElementSplitStrategy searchSpaceElementSplitStrategy, List<Pair<String, List<String>>> subMetaModelOfIntersectionNames) throws CrossoverUsageException, ViewSetOperationException {
		
		if (!loadedResources.containsKey(resourceOneName) || !loadedResources.containsKey(resourceTwoName)) throw new IllegalArgumentException("Unloaded resources given."); 
		if (!resourceMetamodels.containsKey(resourceOneName) || !resourceMetamodels.containsKey(resourceTwoName)) throw new IllegalArgumentException("Metamodel resources given."); 
		if (resourceMetamodels.get(resourceOneName) == null || resourceMetamodels.get(resourceTwoName) == null) throw new IllegalArgumentException("Resources with undefined metamodels given."); 
		if (!resourceMetamodels.get(resourceOneName).equals(resourceMetamodels.get(resourceTwoName))) throw new IllegalArgumentException("Resources with different metamodels given."); 
		
		Strategy problemPartSplitStrategy = SimpleCrossover.createRandomProblemSplitStrategy(0.8);
		
		addCrossover (resourceOneName, resourceTwoName, problemPartSplitStrategy, searchSpaceElementSplitStrategy, subMetaModelOfIntersectionNames);
		
	}
	
	/**
	 * Adds a new crossover.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 * @param problemPartSplitStrategy the {@link Stragtegy} used for splitting up the problem part for the crossover
	 * @param subMetaModelOfIntersectionNames part of the meta-model of the resources to specify the intersections <br/> <hr/>
	 * Earch entry of the list contains the name of an {@link EClass eClass} and a list with the names
	 * of the {@link EReference eReferences} of that {@link EClass eClass} wich also belong to the problem part. <hr/><br/>
	 * @see Crossover#splitProblemPart
	 */
	public void addCrossover (String resourceOneName, String resourceTwoName, Strategy problemPartSplitStrategy, List<Pair<String, List<String>>> subMetaModelOfIntersectionNames) throws CrossoverUsageException, ViewSetOperationException {
		addCrossover (resourceOneName, resourceTwoName, problemPartSplitStrategy, Crossover.DEFAULT_STRATEGY, subMetaModelOfIntersectionNames);
	}
	
	/**
	 * Adds a new crossover.
	 * @param resourceOneName the name of the resource to be used as the first search space element for the crossover
	 * @param resourceTwoName the name of the resource to be used as the second search space element for the crossover
	 * @param problemPartSplitStrategy the {@link Stragtegy} used for splitting up the problem part for the crossover
	 * @param searchSpaceElementSplitStrategy the {@link SearchSpaceElementSplitStrategy} used for the crossover
	 * @param subMetaModelOfIntersectionNames part of the meta-model of the resources to specify the intersections <br/> <hr/>
	 * Earch entry of the list contains the name of an {@link EClass eClass} and a list with the names
	 * of the {@link EReference eReferences} of that {@link EClass eClass} wich also belong to the problem part. <hr/><br/>
	 * @see Crossover#splitProblemPart
	 */
	public void addCrossover (String resourceOneName, String resourceTwoName, Strategy problemPartSplitStrategy, SearchSpaceElementSplitStrategy searchSpaceElementSplitStrategy, List<Pair<String, List<String>>> subMetaModelOfIntersectionNames) throws CrossoverUsageException, ViewSetOperationException {
		
		if (!loadedResources.containsKey(resourceOneName) || !loadedResources.containsKey(resourceTwoName)) throw new IllegalArgumentException("Unloaded resources given."); 
		if (!resourceMetamodels.containsKey(resourceOneName) || !resourceMetamodels.containsKey(resourceTwoName)) throw new IllegalArgumentException("Metamodel resources given."); 
		if (resourceMetamodels.get(resourceOneName) == null || resourceMetamodels.get(resourceTwoName) == null) throw new IllegalArgumentException("Resources with undefined metamodels given."); 
		if (!resourceMetamodels.get(resourceOneName).equals(resourceMetamodels.get(resourceTwoName))) throw new IllegalArgumentException("Resources with different metamodels given."); 
		
		Resource resourceOne = loadedResources.get(resourceOneName);
		Resource resourceTwo = loadedResources.get(resourceTwoName);
		Pair<Resource, Resource> searchSpaceElements = new Pair<Resource, Resource>(resourceOne, resourceTwo);
		
		String resourceMetamodelName = resourceMetamodels.get(resourceOneName);
		Resource metamodel = loadedResources.get(resourceMetamodelName);
		
		View subMetaModelOfIntersection = new View(metamodel);
		
		if (!subMetaModelOfIntersectionNames.isEmpty()) {
			
			TreeIterator<EObject> treeIterator = metamodel.getAllContents();
			
			while (treeIterator.hasNext()) {
				EObject eObject = (EObject) treeIterator.next();
				
				if (eObject instanceof EClass) {
					EClass eClass = (EClass) eObject;
					
					for (Pair<String, List<String>> pair : subMetaModelOfIntersectionNames) {
						if (eClass.getName().equals(pair.getFirst())) {
							subMetaModelOfIntersection.extend(eObject);
							for (String eReferenceName : pair.getSecond()) {
								List<EReference> eReferences = eClass.getEAllReferences().stream().
										filter(eReference -> eReference.getName().equals(eReferenceName)).
										collect(Collectors.toList());
								if (eReferences.size() != 1) throw new IllegalArgumentException("The eClass " + pair.getFirst() + 
										" does not have an eReference by the name of " + eReferenceName);
								subMetaModelOfIntersection.extend(((EObject) eReferences.get(0)));
							}
						}
					}
				}
			}
			
		}
		
		List<EClass> problemPartEClasses = metamodelProblemPartEClasses.get(resourceMetamodelName);
		List<EReference> problemPartEReferences = metamodelProblemPartEReferences.get(resourceMetamodelName);
		
		Crossover crossover = new Crossover(metamodel, searchSpaceElements, problemPartSplitStrategy, problemPartEClasses, problemPartEReferences, searchSpaceElementSplitStrategy, subMetaModelOfIntersection);
		
		if (!crossovers.contains(crossover)) {
			crossovers.add(crossover);
			createCrossoverName(resourceOneName, resourceTwoName);
		}
		
	}
	
	/**
	 * Creates all specified crossovers and writes them to the directory.
	 */
	@Override
	public void run() {
		
		Iterator<Crossover> crossoverIterator = crossovers.iterator();
		Iterator<String> crossoverNamesIterator = crossoverNames.iterator();
		
		while (crossoverIterator.hasNext()) {
			Crossover crossover = (Crossover) crossoverIterator.next();
			String name = (String) crossoverNamesIterator.next();
			Iterator<Pair<Resource, Resource>> crossoverResourcePairIterator = crossover.iterator();
			int crossoverNumber = 0;
			
			while (crossoverResourcePairIterator.hasNext()) {
				crossoverNumber++;
				
				Pair<Resource, Resource> pair = (Pair<Resource, Resource>) crossoverResourcePairIterator.next();
				if (pair.getFirst().getContents().size() != 1) throw new IllegalStateException("There is more or less then one root element in: " + name + "No" + crossoverNumber + "First");
				if (pair.getSecond().getContents().size() != 1) throw new IllegalStateException("There is more or less then one root element in: " + name + "No" + crossoverNumber + "Second");
				whenDone.accept(pair, Pair.of(name, crossoverNumber));

			}
		}
		
	}
	
	/**
	 * @param name the filename of a resource in the resource input directory
	 * @return the resource with the given filename as used by the crossover
	 */
	public Resource getLoadedResource(String name) {
		return loadedResources.get(name);
	}
	
	/**
	 * @param chance the chance to stop removing nodes from the view (each time) after half of the nodes have been removed
	 * @return Returns a {@link Strategy} that removes random nodes from the view and all dangling edges.
	 */
	public static Strategy createRandomProblemSplitStrategy (double chance) {
		return (view) -> {
			
			int amountOfNodes = view.getGraph().getNodes().size();
			int half = amountOfNodes / 2;
			
			for (int i = 0; i < amountOfNodes; i++) {
				
				view.reduce(view.getObject(view.getRandomNode()));
				
				if (i >= half) {
					if (Math.random() <= chance) {
						break;
					}
				}
			}
			
			view.removeDangling();
			
		};
	}
	
	/**
	 * @param percent the percent of nodes to remove, 0 < percent < 1
	 * @return the created strategy
	 */
	public static Strategy createNonRandomProblemSplitStrategy (double percent) {
		
		if (percent <= 0 || percent >= 1) throw new IllegalArgumentException("percent was not a number in the interval of (0, 1)");
		
		return (view) -> {
			
			Collection<EObject> eObjects = view.getContainedEObjects();
			
			
			int amountOfNodes = eObjects.size();
			int toRemove = (int) percent * amountOfNodes;
			
			EObject[] arrayOfEObjects = eObjects.toArray(new EObject[0]);
			
			for (int i = 0; i < amountOfNodes; i++) {
				
				view.reduce(arrayOfEObjects[i]);
				if (i > toRemove) break;
				
			}
			
			view.removeDangling();
			
		};
	}
	
	/**
	 * Runner of the Class with a simple example.
	 * @param args the first argument of this array will be used as the output directory for the crossover pairs
	 */
	public static void main(String[] args) {
		expl_one();
	}
	
	public static void expl_two() {
		SimpleCrossover sc = new SimpleCrossover("test/resources/");
		ExecutorService es = Executors.newSingleThreadExecutor();
		
		sc.defineMetamodel(
				"CRA", 
				List.of("CRA_Pres_One", "CRA_Pres_Two"), 
				List.of(
						Pair.of("ClassModel", List.of("features")),
						Pair.of("Feature", List.of()),
						Pair.of("Attribute", List.of()), 
						Pair.of("Method", List.of("dataDependency", "functionalDependency"))
				)
		);
		
		// get meta-model elements
		
		Resource cra = sc.loadedResources.get("CRA");
		
		EClass namedElementEClass = null;
		EAttribute name = null;
		EClass classModelEClass = null;
		EReference classesEReference = null;
		
		Iterator<EObject> iterator = cra.getAllContents();
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if(eObject instanceof EClass) {
				EClass eClass = (EClass) eObject;
				if(eClass.getName().equals("NamedElement")) {
					namedElementEClass = eClass;
				} else if (eClass.getName().equals("ClassModel")) {
					classModelEClass = eClass;
				}
			}	
		}
		
		for (EAttribute eAttribute : namedElementEClass.getEAttributes()) {
			if (eAttribute.getName().equals("name")) {
				name = eAttribute;
			}
		}
		
		
		for (EReference eReference : classModelEClass.getEAllReferences()) {
			if (eReference.getName().equals("classes")) {
				classesEReference = eReference;
			}
		}
		
		final EAttribute finalName = name;
		
		Strategy pps = (View view) -> {
			
			List<EObject> toRemove = new ArrayList<>();
			
			for (EObject eObject : view.getContainedEObjects()) {
				
				String nameString = (String) eObject.eGet(finalName);
				
				switch (nameString) {
					case "4":
					case "6":
						toRemove.add(eObject);
						break;
	
					default:
						break;
				}
				
			}
			
			toRemove.forEach(view::reduce);

		};
		
		final EReference finalClassesEReference = classesEReference;
		
		SearchSpaceElementSplitStrategy sss = new SearchSpaceElementSplitStrategy() {
			@Override
			public void apply(View sseOne) throws ViewSetOperationException, IllegalStateException {
				
				View changedSSEOne = sseOne.copy();
				
				changedSSEOne.reduce(finalClassesEReference);
				
				Crossover.DEFAULT_STRATEGY.setProblemSplitPart(super.problemSplitPart);
				Crossover.DEFAULT_STRATEGY.apply(changedSSEOne);
				
				sseOne.clear();
				sseOne.union(changedSSEOne);
				sseOne.extend(finalClassesEReference);
				sseOne.removeDangling();
				
			}
		};
		
		try {
			sc.addCrossover("CRA_Pres_One", "CRA_Pres_Two", pps, sss);
		} catch (CrossoverUsageException | ViewSetOperationException e) {
			e.printStackTrace();
		}
		
		es.execute(sc);
	}
	
	public static void expl_one() {
		SimpleCrossover sc = new SimpleCrossover("test/resources/");
		ExecutorService es = Executors.newSingleThreadExecutor();
		
		sc.defineMetamodel(
				"CRA", 
				List.of("CRAInstanceOne", "CRAInstanceTwo"), 
				List.of(
						Pair.of("ClassModel", List.of("features")),
						Pair.of("Feature", List.of()),
						Pair.of("Attribute", List.of()), 
						Pair.of("Method", List.of("dataDependency", "functionalDependency"))
				)
		);
		
		// get meta-model elements
		
		Resource cra = sc.loadedResources.get("CRA");
		
		EClass attributeEClass = null;
		EClass classModelEClass = null;
		EReference classesEReference = null;
		
		Iterator<EObject> iterator = cra.getAllContents();
		while (iterator.hasNext()) {
			EObject eObject = (EObject) iterator.next();
			if(eObject instanceof EClass) {
				EClass eClass = (EClass) eObject;
				if(eClass.getName().equals("Attribute")) {
					attributeEClass = eClass;
				} else if (eClass.getName().equals("ClassModel")) {
					classModelEClass = eClass;
				}
			}
			
		}
		
		for (EReference eReference : classModelEClass.getEAllReferences()) {
			if (eReference.getName().equals("classes")) {
				classesEReference = eReference;
			}
		}
		
		final EClass finalAttributeEClass = attributeEClass;
		final EReference finalClassesEReference = classesEReference;
		
		
		Strategy pps = (View view) -> {
			view.reduce(finalAttributeEClass);
		};
		
		// pps = SimpleCrossover.createRandomProblemSplitStrategy(0.8);
		
		SearchSpaceElementSplitStrategy sss = new SearchSpaceElementSplitStrategy() {
			@Override
			public void apply(View sseOne) throws ViewSetOperationException, IllegalStateException {
				
				View changedSSEOne = sseOne.copy();
				
				changedSSEOne.reduce(finalClassesEReference);
				
				Crossover.DEFAULT_STRATEGY.setProblemSplitPart(super.problemSplitPart);
				Crossover.DEFAULT_STRATEGY.apply(changedSSEOne);
				
				sseOne.clear();
				sseOne.union(changedSSEOne);
				sseOne.extend(finalClassesEReference);
				sseOne.removeDangling();
				
			}
		};
		
		try {
			sc.addCrossover("CRAInstanceOne", "CRAInstanceTwo", pps, sss, List.of(Pair.of("Class", List.of())));
		} catch (CrossoverUsageException | ViewSetOperationException e) {
			e.printStackTrace();
		}
		
		es.execute(sc);
	}

}
