package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.Definitions;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.analysis.prolog.NodeFileNumbers;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PDefinition;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.prolog.output.IPrologTermOutput;

import static de.be4.classicalb.core.parser.util.ASTBuilder.addBooleanPreferenceDefinition;
import static de.be4.classicalb.core.parser.util.ASTBuilder.addChooseDefinition;
import static de.be4.classicalb.core.parser.util.ASTBuilder.addFormatToStringDefinition;
import static de.be4.classicalb.core.parser.util.ASTBuilder.addSortDefinition;
import static de.be4.classicalb.core.parser.util.ASTBuilder.addToStringDefinition;

public class RulesProject {
	public static final String CTAGS_FILE_NAME = ".rules-tags1";
	static final String MAIN_MACHINE_NAME = "__RULES_MACHINE_Main";
	static final String COMPOSITION_MACHINE_NAME = "__RULES_MACHINE_Composition";
	private ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
	private final List<BException> bExceptionList = new ArrayList<>();
	private final HashMap<String, AbstractOperation> allOperations = new LinkedHashMap<>();
	protected final List<IModel> bModels = new ArrayList<>();
	protected final INodeIds nodeIdAssignment = new NodeFileNumbers();
	private final List<File> filesLoaded = new ArrayList<>(); // rules machine files and definitions files
	private final Map<String, String> constantStringValues = new HashMap<>();
	private RulesMachineRunConfiguration rulesMachineRunConfiguration;
	private final Map<String, String> operationReplacementMap = new HashMap<>();

	public RulesProject() {
		// use the provided setter methods to parameterize the rules project
	}

	public void parseRulesMachines(String mainMachineAsString, String... referencedMachines) {
		addModel(RulesParseUnit.parse(mainMachineAsString));
		for (String referencedMachine : referencedMachines) {
			addModel(RulesParseUnit.parse(referencedMachine));
		}
	}

	public void parseProject(File mainFile) {
		final RulesParseUnit mainModel = RulesParseUnit.parse(mainFile, parsingBehaviour);
		addModel(mainModel);
		Deque<MachineReference> fifo = new LinkedList<>(mainModel.getMachineReferences());
		while (!fifo.isEmpty()) {
			final MachineReference modelReference = fifo.pollFirst();
			if (isANewModel(modelReference)) {
				final IModel bModel = RulesParseUnit.parse(new File(modelReference.getPath()), parsingBehaviour);
				addModel(bModel);
				fifo.addAll(bModel.getMachineReferences());
			}
		}
	}

	private void addModel(IModel model) {
		bModels.add(model);
		if (model.hasError()) {
			this.bExceptionList.addAll(model.getCompoundException().getBExceptions());
		}
	}

	public void checkAndTranslateProject() {
		this.checkProject();
		this.flattenProject();
	}

	public List<BException> getBExceptionList() {
		return bExceptionList;
	}

	public void setParsingBehaviour(final ParsingBehaviour parsingBehaviour) {
		this.parsingBehaviour = parsingBehaviour;
	}

	public Map<String, AbstractOperation> getOperationsMap() {
		Map<String, AbstractOperation> result = new HashMap<>();
		final Set<String> replacedOperations = new HashSet<>(this.operationReplacementMap.values());
		for (Map.Entry<String, AbstractOperation> entry : this.allOperations.entrySet()) {
			AbstractOperation op = entry.getValue();
			if (!replacedOperations.contains(op.getOriginalName())) {
				// resolve REPLACES chain
				String name = op.getOriginalName();
				for (int i = 0; operationReplacementMap.containsKey(name) && i < operationReplacementMap.size(); i++) {
					name = operationReplacementMap.get(name);
				}
				result.put(name, op);
			}
		}
		return result;
	}

	public Set<AbstractOperation> getOperationsWithNoSuccessor() {
		final Collection<AbstractOperation> operations = getOperationsMap().values();
		Set<AbstractOperation> values = new HashSet<>(operations);
		for (AbstractOperation op : operations) {
			values.removeAll(op.getTransitiveDependencies());
		}
		return values;
	}

	private void flattenProject() {
		if (!this.bExceptionList.isEmpty()) {
			return;
		}
		this.rulesMachineRunConfiguration = RulesMachineRunConfiguration
				.extractConfigurationOfMainModel(this.bModels.get(0), this.allOperations);
		final BMachine compositionMachine = new BMachine(COMPOSITION_MACHINE_NAME);
		MachineInjector injector = new MachineInjector(compositionMachine.getStart());
		for (IModel model : bModels) {
			RulesParseUnit rulesParseUnit = (RulesParseUnit) model;
			rulesParseUnit.translate(allOperations);
			if (!rulesParseUnit.hasError()) {
				Start start = rulesParseUnit.getStart();
				filesLoaded.add(new File(rulesParseUnit.getPath()));
				nodeIdAssignment.assignIdentifiers(filesLoaded.size(), start);
				rulesParseUnit.getBParser().getDefinitions().assignIdsToNodes(nodeIdAssignment, filesLoaded);
			} else {
				this.bExceptionList.addAll(rulesParseUnit.getCompoundException().getBExceptions());
			}
			Start otherStart = rulesParseUnit.getStart();
			injector.injectMachine(otherStart);
			/*for (CheckException e : injector.injectMachine(otherStart)) {
				this.bExceptionList.add(new BException(rulesParseUnit.getPath(), e));
			}*/
		}
		compositionMachine.setParsingBehaviour(this.parsingBehaviour);
		bModels.add(compositionMachine);
		final BMachine mainMachine = createMainMachine(injector.getMainMachineDefinitions());
		bModels.add(mainMachine);
	}

	private BMachine createMainMachine(List<PDefinition> mainDefinitions) {
		BMachine mainMachine = new BMachine(MAIN_MACHINE_NAME);
		mainMachine.setParsingBehaviour(this.parsingBehaviour);
		mainMachine.addIncludesClause(COMPOSITION_MACHINE_NAME);
		mainMachine.addPromotesClause(getPromotesList());
		mainMachine.addPropertiesPredicates(this.constantStringValues);
		IDefinitions iDefinitions = new Definitions();

		if (mainDefinitions != null) {
			for (PDefinition pDefinition : mainDefinitions) {
				iDefinitions.addDefinition(pDefinition);
			}
		}
		addToStringDefinition(iDefinitions);
		addSortDefinition(iDefinitions);
		addFormatToStringDefinition(iDefinitions);
		addChooseDefinition(iDefinitions);
		addBooleanPreferenceDefinition(iDefinitions, "ALLOW_LOCAL_OPERATION_CALLS", true);
		addBooleanPreferenceDefinition(iDefinitions, "ALLOW_OPERATION_CALLS_IN_EXPRESSIONS", true);
		mainMachine.replaceDefinition(iDefinitions);
		return mainMachine;
	}

	private List<String> getPromotesList() {
		final List<String> promotesList = new ArrayList<>();
		for (AbstractOperation abstractOperation : allOperations.values()) {
			// a replacing operation is promoted under the name of the replaced operation
			if ((abstractOperation instanceof ComputationOperation || abstractOperation instanceof RuleOperation)
					&& !abstractOperation.replacesOperation()) {
				promotesList.add(abstractOperation.getOriginalName());
			}
		}
		return promotesList;
	}

	private void checkProject() {
		collectAllOperations();
		checkDependencies();
		findImplicitDependenciesToComputations();
		checkIdentifiers();
		findTransitiveDependencies();
		checkReferencedRuleOperations();
		checkReplacements();
	}

	private void checkReplacements() {
		if (this.hasErrors()) {
			return;
		}

		for (Map.Entry<String, String> entry : this.operationReplacementMap.entrySet()) {
			final String newOpName = entry.getKey();
			final String replacedOperationName = entry.getValue();
			AbstractOperation newOp = this.allOperations.get(newOpName);
			AbstractOperation replacedOp = this.allOperations.get(replacedOperationName);
			if (replacedOp == null) {
				this.bExceptionList.add(new BException(newOp.getFileName(), new CheckException(
						"Unknown operation '" + replacedOperationName + "' in REPLACES clause.", newOp.getReplacesIdentifier())));
				continue;
			}

			String name = replacedOperationName;
			for (int i = 0; name != null && i < operationReplacementMap.size(); i++) {
				if (name.equals(newOpName)) {
					this.bExceptionList.add(new BException(newOp.getFileName(), new CheckException(
							"Cyclic replacement of operation '" + newOpName + "'.", newOp.getReplacesIdentifier())));
					break;
				}
				name = operationReplacementMap.get(name);
			}

			if (newOp.getClass() != replacedOp.getClass()) {
				this.bExceptionList.add(new BException(newOp.getFileName(),
						new CheckException(String.format("Operation '%s' is an invalid replacement for operation '%s'.",
								newOpName, replacedOperationName), newOp.getNameLiteral())));
			} else if (replacedOp instanceof ComputationOperation) {
				ComputationOperation oldComp = (ComputationOperation) replacedOp;
				ComputationOperation newComp = (ComputationOperation) newOp;
				Set<String> oldVariables = oldComp.getDefineVariables();
				Set<String> newVariables = newComp.getDefineVariables();
				if (!oldVariables.containsAll(newVariables) || !newVariables.containsAll(oldVariables)) {
					this.bExceptionList
							.add(new BException(newOp.getFileName(),
									new CheckException(String.format(
											"Operation '%s' is an invalid replacement for operation '%s'.",
											newOpName, replacedOperationName), newOp.getNameLiteral())));
				}
			}
		}

	}

	private void collectAllOperations() {
		for (IModel iModel : bModels) {
			final RulesParseUnit unit = (RulesParseUnit) iModel;
			final List<AbstractOperation> operations = unit.getOperations();
			for (AbstractOperation abstractOperation : operations) {
				final String name = abstractOperation.getOriginalName();
				if (allOperations.containsKey(name)) {
					this.bExceptionList.add(new BException(abstractOperation.getFileName(), new CheckException(
							"Duplicate operation name: '" + name + "'.", abstractOperation.getNameLiteral())));
				}
				allOperations.put(name, abstractOperation);
				if (abstractOperation.replacesOperation()) {
					String replacedOperationName = abstractOperation.getReplacedOperationName();
					if (operationReplacementMap.containsValue(replacedOperationName)) {
						this.bExceptionList.add(new BException(abstractOperation.getFileName(),
								new CheckException(
										"Operation '" + replacedOperationName + "' is replaced more than once.",
										abstractOperation.getNameLiteral())));
					} else {
						this.operationReplacementMap.put(name, replacedOperationName);
					}

				}
			}
		}
	}

	private void checkDependencies() {
		for (AbstractOperation operation : allOperations.values()) {
			checkDependsOn(operation, operation.getDependsOnComputationList(), ComputationOperation.class,
					"Identifier '%s' is not a COMPUTATION.");
			checkDependsOn(operation, operation.getDependsOnRulesList(), RuleOperation.class,
					"Operation '%s' is not a RULE operation.");
			checkFunctionCalls(operation);
		}
	}

	private void checkFunctionCalls(AbstractOperation abstractOperation) {
		boolean errorOccurred = false;
		for (TIdentifierLiteral tIdentifierLiteral : abstractOperation.getFunctionCalls()) {
			final String functionName = tIdentifierLiteral.getText();
			if (!(allOperations.get(functionName) instanceof FunctionOperation)) {
				this.bExceptionList.add(new BException(abstractOperation.getFileName(),
						new CheckException("Unknown FUNCTION name '" + functionName + "'", tIdentifierLiteral)));
				errorOccurred = true;
			}
		}
		if (!errorOccurred) {
			checkVisibilityOfTIdentifierList(abstractOperation, abstractOperation.getFunctionCalls());
		}

	}

	// DEPENDS_ON_RULE / DEPENDS_ON_COMPUTATION: the operations must exist, have the expected type and be visible
	private void checkDependsOn(AbstractOperation operation, List<AIdentifierExpression> dependencies,
			Class<? extends AbstractOperation> expectedType, String wrongTypeMessage) {
		boolean errorOccurred = false;
		for (AIdentifierExpression aIdentifierExpression : dependencies) {
			final String name = aIdentifierExpression.getIdentifier().get(0).getText();
			final AbstractOperation abstractOperation = allOperations.get(name);
			if (abstractOperation == null) {
				errorOccurred = true;
				this.bExceptionList.add(new BException(operation.getFileName(),
						new CheckException("Unknown operation: '" + name + "'.", aIdentifierExpression)));
			} else if (!expectedType.isInstance(abstractOperation)) {
				this.bExceptionList.add(new BException(operation.getFileName(),
						new CheckException(String.format(wrongTypeMessage, name), aIdentifierExpression)));
				errorOccurred = true;
			}
		}
		if (!errorOccurred) {
			checkVisibilityOfTIdentifierList(operation, getTIdentifierLiterals(dependencies));
		}
	}

	private void checkVisibilityOfTIdentifierList(AbstractOperation operation,
			List<TIdentifierLiteral> dependencyList) {
		List<String> machineReferences = operation.getMachineReferencesAsString();
		machineReferences.add(operation.getMachineName());
		for (TIdentifierLiteral tIdentifierLiteral : dependencyList) {
			String otherOpName = tIdentifierLiteral.getText();
			if (allOperations.containsKey(otherOpName)) {
				AbstractOperation abstractOperation = allOperations.get(otherOpName);
				String otherMachineName = abstractOperation.getMachineName();
				if (!machineReferences.contains(otherMachineName)) {
					this.bExceptionList
							.add(new BException(operation.getFileName(),
									new CheckException("Operation '" + otherOpName
											+ "' is not visible in RULES_MACHINE '" + operation.getMachineName() + "'.",
											tIdentifierLiteral)));
				}
			}
		}
	}

	private void findImplicitDependenciesToComputations() {
		Map<String, ComputationOperation> variableToComputation = new HashMap<>();
		for (AbstractOperation operation : allOperations.values()) {
			if (operation instanceof ComputationOperation && !operation.replacesOperation()) {
				ComputationOperation comp = (ComputationOperation) operation;
				for (String defName : comp.getDefineVariables()) {
					variableToComputation.put(defName, comp);
				}
			}
		}

		for (AbstractOperation operation : allOperations.values()) {
			final Set<String> readVariables = operation.getReadVariables();
			readVariables.retainAll(variableToComputation.keySet());

			Set<String> variablesInScope = new HashSet<>();
			// Note, RulesMachineChecker locally checks that a variable is
			// defined before read.
			if (operation instanceof ComputationOperation) {
				variablesInScope.addAll(((ComputationOperation) operation).getDefineVariables());
			}
			for (AIdentifierExpression aIdentifier : operation.getDependsOnComputationList()) {
				String opName = aIdentifier.getIdentifier().get(0).getText();
				AbstractOperation abstractOperation = allOperations.get(opName);
				if (abstractOperation instanceof ComputationOperation) {
					variablesInScope.addAll(((ComputationOperation) abstractOperation).getDefineVariables());
				}
			}
			readVariables.removeAll(variablesInScope);
			if (!readVariables.isEmpty()) {
				Set<ComputationOperation> inferredDependenciesToComputations = new LinkedHashSet<>();
				final List<String> machineReferences = operation.getMachineReferencesAsString();
				machineReferences.add(operation.getMachineName());
				final List<String> sortedReadVariables = new ArrayList<>(readVariables);
				Collections.sort(sortedReadVariables);
				for (String varName : sortedReadVariables) {
					ComputationOperation computationOperation = variableToComputation.get(varName);
					inferredDependenciesToComputations.add(computationOperation);
					if (!machineReferences.contains(computationOperation.getMachineName())) {
						this.bExceptionList.add(new BException(operation.getFileName(),
								new CheckException(
										"Missing reference to RULES_MACHINE '" + computationOperation.getMachineName()
												+ "' in order to use variable '" + varName + "'.",
										operation.getVariableReadByName(varName))));
					}
				}
				operation.setImplicitComputationDependencies(new ArrayList<>(inferredDependenciesToComputations));
			} else {
				operation.setImplicitComputationDependencies(Collections.emptyList());
			}
		}

	}

	private void checkIdentifiers() {
		if (this.hasErrors()) {
			/*
			 * if there is already an error such as a parse error in one
			 * machine, it makes no sense to check for invalid identifiers
			 * because all declarations of this machine are missing.
			 */
			return;
		}
		final Map<String, RulesParseUnit> map = getParseUnitsByName();
		// Every machine is checked separately: an identifier is known in a machine if it is
		// declared in the machine itself or in one of the machines it (transitively) references.
		for (IModel model : bModels) {
			final RulesParseUnit parseUnit = (RulesParseUnit) model;
			final Set<String> visited = new HashSet<>();
			visited.add(parseUnit.getMachineName());
			final Set<String> knownIdentifiers = checkIdentifiers(parseUnit, map, visited);
			final Map<String, Set<Node>> unknownIdentifierMap = parseUnit.getRulesMachineChecker().getUnknownIdentifier();
			final List<String> unknownIdentifiers = new ArrayList<>(unknownIdentifierMap.keySet());
			unknownIdentifiers.removeAll(knownIdentifiers);
			Collections.sort(unknownIdentifiers); // deterministic order of error messages
			for (String name : unknownIdentifiers) {
				final Node node = getFirstNode(unknownIdentifierMap.get(name));
				this.bExceptionList.add(new BException(parseUnit.getPath(),
						new CheckException("Unknown identifier '" + name + "'.", node)));
			}
		}
	}

	// the node with the smallest source position (nodes without position last)
	private static Node getFirstNode(Collection<Node> nodes) {
		return Collections.min(nodes, Comparator.comparing(Node::getStartPos,
				Comparator.nullsLast(Comparator.naturalOrder())));
	}

	private Map<String, RulesParseUnit> getParseUnitsByName() {
		final Map<String, RulesParseUnit> map = new HashMap<>();
		for (IModel model : bModels) {
			map.put(model.getMachineName(), (RulesParseUnit) model);
		}
		return map;
	}

	private Set<String> checkIdentifiers(RulesParseUnit parseUnit, Map<String, RulesParseUnit> map, Set<String> visited) {
		Set<String> knownIdentifiers = new HashSet<>();
		List<MachineReference> machineReferences = parseUnit.getMachineReferences();
		for (MachineReference rulesMachineReference : machineReferences) {
			RulesParseUnit rulesParseUnit = map.get(rulesMachineReference.getName());
			if (visited.contains(rulesParseUnit.getMachineName())) { // already visited
				continue;
			}
			visited.add(rulesParseUnit.getMachineName());
			knownIdentifiers.addAll(checkIdentifiers(rulesParseUnit, map, visited)); // get identifiers of all referenced machines
			RulesMachineChecker checker = rulesParseUnit.getRulesMachineChecker();
			knownIdentifiers.addAll(checker.getGlobalIdentifierNames());
			knownIdentifiers.addAll(checker.getFunctionOperationNames());
			knownIdentifiers.addAll(checker.getDefinitionNames());
		}
		return knownIdentifiers;
	}

	public void checkReferencedRuleOperations() {
		if (this.hasErrors()) {
			return;
		}
		final Map<String, RulesParseUnit> map = getParseUnitsByName();
		for (IModel model : bModels) {
			if (model instanceof RulesParseUnit) {
				RulesParseUnit rulesParseUnit = (RulesParseUnit) model;
				Set<AIdentifierExpression> referencedRuleOperations = rulesParseUnit.getRulesMachineChecker()
						.getReferencedRuleOperations();
				Set<String> knownRules = new HashSet<>();
				for (RuleOperation ruleOperation : rulesParseUnit.getRulesMachineChecker().getRuleOperations()) {
					knownRules.add(ruleOperation.getOriginalName());
				}
				for (MachineReference rulesMachineReference : rulesParseUnit.getMachineReferences()) {
					String referenceName = rulesMachineReference.getName();
					RulesParseUnit otherParseUnit = map.get(referenceName);
					for (RuleOperation ruleOperation : otherParseUnit.getRulesMachineChecker().getRuleOperations()) {
						knownRules.add(ruleOperation.getOriginalName());
					}
				}

				for (AIdentifierExpression aIdentifierExpression : referencedRuleOperations) {
					String ruleName = Utils.getAIdentifierAsString(aIdentifierExpression);
					if (!knownRules.contains(ruleName)) {
						this.bExceptionList.add(new BException(rulesParseUnit.getPath(),
								new CheckException("Unknown rule '" + ruleName + "'.", aIdentifierExpression)));
					}
				}
			}
		}
	}

	private void findTransitiveDependencies() {
		if (this.hasErrors())
			return;
		for (AbstractOperation operation : allOperations.values()) {
			if (operation.getTransitiveDependencies() == null) {
				findTransitiveDependencies(operation, new ArrayList<>());
			}
		}
	}

	private Set<AbstractOperation> findTransitiveDependencies(final AbstractOperation operation,
			final List<AbstractOperation> ancestors) {
		ancestors.add(operation);
		List<TIdentifierLiteral> directDependencies = getTIdentifierLiterals(operation.getDependsOnComputationList());
		directDependencies.addAll(getTIdentifierLiterals(operation.getDependsOnRulesList()));
		directDependencies.addAll(operation.getImplicitDependenciesToComputations());
		directDependencies.addAll(operation.getFunctionCalls());

		// check for cycle
		Set<AbstractOperation> transitiveDependenciesFound = new HashSet<>();
		boolean cycleDetected = checkForCycles(operation, directDependencies, ancestors);
		if (cycleDetected) {
			operation.setTransitiveDependencies(transitiveDependenciesFound);
			return transitiveDependenciesFound;
		}
		for (TIdentifierLiteral tIdentifierLiteral : directDependencies) {
			String opName = tIdentifierLiteral.getText();
			AbstractOperation nextOperation = allOperations.get(opName);
			transitiveDependenciesFound.add(nextOperation);
			if (nextOperation.getTransitiveDependencies() != null) {
				transitiveDependenciesFound.addAll(nextOperation.getTransitiveDependencies());
			} else {
				transitiveDependenciesFound
						.addAll(findTransitiveDependencies(nextOperation, new ArrayList<>(ancestors)));
			}
		}

		operation.setTransitiveDependencies(transitiveDependenciesFound);
		return new HashSet<>(transitiveDependenciesFound);
	}

	private static List<TIdentifierLiteral> getTIdentifierLiterals(List<AIdentifierExpression> list) {
		return list.stream().map(id -> id.getIdentifier().get(0)).collect(Collectors.toList());
	}

	private boolean checkForCycles(AbstractOperation operation, List<TIdentifierLiteral> directDependencies,
			List<AbstractOperation> ancestors) {
		List<String> ancestorsNames = ancestors.stream().map(AbstractOperation::getOriginalName).collect(Collectors.toList());
		for (TIdentifierLiteral id : directDependencies) {
			final String opName = id.getText();
			if (ancestorsNames.contains(opName)) {
				final String cycle = String.join(" -> ", ancestorsNames.subList(ancestorsNames.indexOf(opName), ancestorsNames.size()))
						+ " -> " + opName;
				this.bExceptionList.add(new BException(operation.getFileName(),
						new CheckException("Cyclic dependencies between operations: " + cycle, id)));

				return true;
			}
		}
		return false;
	}

	public RulesMachineRunConfiguration getRulesMachineRunConfiguration() {
		return this.rulesMachineRunConfiguration;
	}

	protected boolean isANewModel(MachineReference reference) {
		return bModels.stream().noneMatch(model -> model.getMachineName().equals(reference.getName()));
	}

	public List<IModel> getBModels() {
		return bModels;
	}

	public boolean hasErrors() {
		return !this.bExceptionList.isEmpty();
	}

	public void printProjectAsPrologTerm(final IPrologTermOutput pout) {
		this.printAsPrologTermWithFullstops(pout, true);
	}

	public void printProjectAsPrologTermDirect(final IPrologTermOutput pout) {
		this.printAsPrologTermWithFullstops(pout, false);
	}

	public void printAsPrologTermWithFullstops(final IPrologTermOutput pout, final boolean withFullstops) {
		// parser version
		pout.openTerm("parser_version");
		pout.printAtom(BParser.getGitSha());
		pout.closeTerm();
		if (withFullstops) {
			pout.fullstop();
		}

		// machine metadata
		pout.openTerm("classical_b");
		pout.printAtom(MAIN_MACHINE_NAME);
		pout.openList();
		for (File file : this.filesLoaded) {
			pout.printAtom(file.getAbsolutePath());
		}
		pout.closeList();
		pout.closeTerm();
		if (withFullstops) {
			pout.fullstop();
		}

		// machines
		for (IModel iModel : bModels) {
			iModel.printAsPrologWithFullstops(pout, this.nodeIdAssignment, withFullstops);
		}
	}

	public void addConstantValue(String constant, String value) {
		this.constantStringValues.put(constant, value);
	}

}
