package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.Definitions;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.analysis.prolog.NodeFileNumbers;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
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
	private final LinkedHashMap<String, AbstractOperation> allOperations = new LinkedHashMap<>();
	protected final List<IModel> bModels = new ArrayList<>();
	protected final INodeIds nodeIdAssignment = new NodeFileNumbers();
	private final List<File> filesLoaded = new ArrayList<>(); // rules machine files
														// and definitions files
	private final HashMap<String, String> constantStringValues = new HashMap<>();
	private RulesMachineRunConfiguration rulesMachineRunConfiguration;
	private final HashMap<String, String> operationReplacementMap = new HashMap<>();

	public RulesProject() {
		// use the provided setter methods to parameterize the rules project
	}

	public void parseRulesMachines(String mainMachineAsString, String... referencedMachines) {
		final IModel mainMachine = RulesParseUnit.parse(mainMachineAsString);
		this.bModels.add(mainMachine);
		if (mainMachine.hasError()) {
			this.bExceptionList.addAll(mainMachine.getCompoundException().getBExceptions());
		}
		for (String referencedMachine : referencedMachines) {
			final IModel machine = RulesParseUnit.parse(referencedMachine);
			this.bModels.add(machine);
			if (machine.hasError()) {
				this.bExceptionList.addAll(machine.getCompoundException().getBExceptions());
			}
		}
	}

	public void parseProject(File mainFile) {
		RulesParseUnit mainModel = RulesParseUnit.parse(mainFile, parsingBehaviour);
		if (mainModel.hasError()) {
			final BCompoundException compound = mainModel.getCompoundException();
			this.bExceptionList.addAll(compound.getBExceptions());
		}
		bModels.add(mainModel);
		final LinkedList<MachineReference> fifo = new LinkedList<>(mainModel.getMachineReferences());
		while (!fifo.isEmpty()) {
			final MachineReference modelReference = fifo.pollFirst();
			if (isANewModel(modelReference)) {
				File file = new File(modelReference.getPath());
				final IModel bModel = RulesParseUnit.parse(file, parsingBehaviour);
				if (bModel.hasError()) {
					this.bExceptionList.addAll(bModel.getCompoundException().getBExceptions());
				}
				bModels.add(bModel);
				fifo.addAll(bModel.getMachineReferences());
			}
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
		for (Entry<String, AbstractOperation> entry : this.allOperations.entrySet()) {
			AbstractOperation op = entry.getValue();
			if (!this.operationReplacementMap.containsValue(op.getOriginalName())) {
				result.put(op.getName(), op);
			}
		}
		return result;
	}

	public Set<AbstractOperation> getOperationsWithNoSuccessor() {
		Set<AbstractOperation> values = new HashSet<>(getOperationsMap().values());
		for (AbstractOperation op : getOperationsMap().values()) {
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
			if (abstractOperation instanceof ComputationOperation || abstractOperation instanceof RuleOperation) {
				if (abstractOperation.replacesOperation()) {
					// replacement operations are not added here because the
					// replaced operation will be added
					continue;
				}
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

		for (Entry<String, String> entry : this.operationReplacementMap.entrySet()) {
			final String newOpName = entry.getKey();
			final String replacedOperationName = entry.getValue();
			AbstractOperation newOp = this.allOperations.get(newOpName);
			AbstractOperation replacedOp = this.allOperations.get(replacedOperationName);

			if (newOp.getClass() != replacedOp.getClass()) {
				this.bExceptionList.add(new BException(newOp.getFileName(),
						new CheckException(String.format("Operation '%s' is an invalid replacement for operation '%s'.",
								newOpName, replacedOperationName), newOp.getNameLiteral())));
			} else {
				if (replacedOp instanceof ComputationOperation) {
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
			checkDependsOnComputations(operation);
			checkDependsOnRules(operation);
			checkFunctionCalls(operation);
		}
	}

	private void checkFunctionCalls(AbstractOperation abstractOperation) {
		boolean errorOccurred = false;
		for (TIdentifierLiteral tIdentifierLiteral : abstractOperation.getFunctionCalls()) {
			final String functionName = tIdentifierLiteral.getText();
			if (!allOperations.containsKey(functionName)
					|| !(allOperations.get(functionName) instanceof FunctionOperation)) {
				this.bExceptionList.add(new BException(abstractOperation.getFileName(),
						new CheckException("Unknown FUNCTION name '" + functionName + "'", tIdentifierLiteral)));
				errorOccurred = true;
			}
		}
		if (!errorOccurred) {
			checkVisibilityOfTIdentifierList(abstractOperation, abstractOperation.getFunctionCalls());
		}

	}

	private void checkDependsOnRules(AbstractOperation operation) {
		boolean errorOccurred = false;
		for (AIdentifierExpression aIdentifierExpression : operation.getDependsOnRulesList()) {
			final String name = aIdentifierExpression.getIdentifier().get(0).getText();
			if (allOperations.containsKey(name)) {
				AbstractOperation abstractOperation = allOperations.get(name);
				if (!(abstractOperation instanceof RuleOperation)) {
					this.bExceptionList.add(new BException(operation.getFileName(), new CheckException(
							"Operation '" + name + "' is not a RULE operation.", aIdentifierExpression)));
					errorOccurred = true;
				}
			} else {
				errorOccurred = true;
				this.bExceptionList.add(new BException(operation.getFileName(),
						new CheckException("Unknown operation: '" + name + "'.", aIdentifierExpression)));
			}
		}
		if (!errorOccurred) {
			checkVisibilityOfAIdentifierList(operation, operation.getDependsOnRulesList());
		}
	}

	private void checkDependsOnComputations(AbstractOperation operation) {
		boolean errorOccurred = false;
		for (AIdentifierExpression aIdentifierExpression : operation.getDependsOnComputationList()) {
			final String name = aIdentifierExpression.getIdentifier().get(0).getText();
			if (allOperations.containsKey(name)) {
				AbstractOperation abstractOperation = allOperations.get(name);
				if (!(abstractOperation instanceof ComputationOperation)) {
					this.bExceptionList.add(new BException(operation.getFileName(), new CheckException(
							"Identifier '" + name + "' is not a COMPUTATION.", aIdentifierExpression)));
					errorOccurred = true;
				}
			} else {
				errorOccurred = true;
				this.bExceptionList.add(new BException(operation.getFileName(),
						new CheckException("Unknown operation: '" + name + "'.", aIdentifierExpression)));
			}
		}
		if (!errorOccurred) {
			checkVisibilityOfAIdentifierList(operation, operation.getDependsOnComputationList());
		}
	}

	private void checkVisibilityOfAIdentifierList(AbstractOperation operation,
			List<AIdentifierExpression> dependencyList) {
		List<TIdentifierLiteral> tidentifierList = new ArrayList<>();
		for (AIdentifierExpression aIdentifier : dependencyList) {
			tidentifierList.add(aIdentifier.getIdentifier().get(0));
		}
		checkVisibilityOfTIdentifierList(operation, tidentifierList);
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

	public List<AbstractOperation> sortOperations(Collection<AbstractOperation> values) {
		HashMap<AbstractOperation, Set<AbstractOperation>> dependenciesMap = new HashMap<>();
		for (AbstractOperation abstractOperation : values) {
			if (!(abstractOperation instanceof FunctionOperation)) {
				dependenciesMap.put(abstractOperation, new HashSet<>(abstractOperation.getTransitiveDependencies()));
			}
		}
		List<AbstractOperation> resultList = new ArrayList<>();
		List<AbstractOperation> todoList = new ArrayList<>(dependenciesMap.keySet());
		while (!todoList.isEmpty()) {
			for (AbstractOperation abstractOperation : new ArrayList<>(todoList)) {
				final Set<AbstractOperation> deps = dependenciesMap.get(abstractOperation);
				resultList.forEach(deps::remove);
				if (deps.isEmpty()) {
					resultList.add(abstractOperation);
					todoList.remove(abstractOperation);
				}
			}
		}
		return resultList;
	}

	private void findImplicitDependenciesToComputations() {
		HashMap<String, ComputationOperation> variableToComputation = new HashMap<>();
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

			final HashSet<String> variablesInScope = new HashSet<>();
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
				List<ComputationOperation> inferredDependenciesToComputations = new ArrayList<>();
				for (String varName : readVariables) {
					ComputationOperation computationOperation = variableToComputation.get(varName);
					inferredDependenciesToComputations.add(computationOperation);
					List<String> machineReferences = operation.getMachineReferencesAsString();
					machineReferences.add(operation.getMachineName());
					if (!machineReferences.contains(computationOperation.getMachineName())) {
						this.bExceptionList.add(new BException(operation.getFileName(),
								new CheckException(
										"Missing reference to RULES_MACHINE '" + computationOperation.getMachineName()
												+ "' in order to use variable '" + varName + "'.",
										operation.getVariableReadByName(varName))));
					}
					operation.setImplicitComputationDependencies(inferredDependenciesToComputations);
				}
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
		final HashMap<String, RulesParseUnit> map = new HashMap<>();
		for (IModel model : bModels) {
			RulesParseUnit parseUnit = (RulesParseUnit) model;
			map.put(parseUnit.getMachineName(), parseUnit);
		}
		Set<String> knownIdentifiers = new HashSet<>();
		Set<String> visited = new HashSet<>();
		RulesParseUnit parseUnit = (RulesParseUnit) bModels.get(0);
		visited.add(parseUnit.getMachineName());
		for (IModel model : bModels) {
			knownIdentifiers.addAll(checkIdentifiers((RulesParseUnit) model, map, visited));
			visited.clear();
		}
		RulesMachineChecker checker = parseUnit.getRulesMachineChecker();
		Map<String, HashSet<Node>> unknownIdentifierMap = checker.getUnknownIdentifier();
		HashSet<String> unknownIdentifiers = new HashSet<>(unknownIdentifierMap.keySet());
		unknownIdentifiers.removeAll(knownIdentifiers);
		for (String name : unknownIdentifiers) {
			HashSet<Node> hashSet = unknownIdentifierMap.get(name);
			Node node = hashSet.iterator().next();
			this.bExceptionList.add(new BException(parseUnit.getPath(),
					new CheckException("Unknown identifier '" + name + "'.", node)));
		}
	}

	private Set<String> checkIdentifiers(RulesParseUnit parseUnit, Map<String, RulesParseUnit> map, Set<String> visited) {
		HashSet<String> knownIdentifiers = new HashSet<>();
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
		final HashMap<String, RulesParseUnit> map = new HashMap<>();
		for (IModel model : bModels) {
			RulesParseUnit parseUnit = (RulesParseUnit) model;
			map.put(parseUnit.getMachineName(), parseUnit);
		}
		for (IModel model : bModels) {
			if (model instanceof RulesParseUnit) {
				RulesParseUnit rulesParseUnit = (RulesParseUnit) model;
				Set<AIdentifierExpression> referencedRuleOperations = rulesParseUnit.getRulesMachineChecker()
						.getReferencedRuleOperations();
				final HashSet<String> knownRules = new HashSet<>();
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
		LinkedList<AbstractOperation> todoList = new LinkedList<>(allOperations.values());
		while (!todoList.isEmpty()) {
			AbstractOperation operation = todoList.poll();
			if (operation.getTransitiveDependencies() == null) {
				findTransitiveDependencies(operation, new ArrayList<>());
			}
		}
	}

	private Set<AbstractOperation> findTransitiveDependencies(final AbstractOperation operation,
			final List<AbstractOperation> ancestors) {
		ancestors.add(operation);
		List<TIdentifierLiteral> directDependencies = new ArrayList<>();
		directDependencies
				.addAll(convertAIdentifierListToTIdentifierLiteralList(operation.getDependsOnComputationList()));
		directDependencies.addAll(convertAIdentifierListToTIdentifierLiteralList(operation.getDependsOnRulesList()));
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

	private List<TIdentifierLiteral> convertAIdentifierListToTIdentifierLiteralList(List<AIdentifierExpression> list) {
		List<TIdentifierLiteral> result = new ArrayList<>();
		for (AIdentifierExpression aIdentifier : list) {
			result.add(aIdentifier.getIdentifier().get(0));
		}
		return result;
	}

	private boolean checkForCycles(AbstractOperation operation, List<TIdentifierLiteral> directDependencies,
			List<AbstractOperation> ancestors) {
		List<String> ancestorsNames = new ArrayList<>();
		for (AbstractOperation op : ancestors) {
			String opName = op.getOriginalName();
			ancestorsNames.add(opName);
		}
		for (TIdentifierLiteral id : directDependencies) {
			final String opName = id.getText();
			if (ancestorsNames.contains(opName)) {
				StringBuilder sb = new StringBuilder();
				for (int index = ancestorsNames.indexOf(opName); index < ancestors.size(); index++) {
					final String name = ancestors.get(index).getOriginalName();
					sb.append(name);
					sb.append(" -> ");
				}
				sb.append(opName);
				this.bExceptionList.add(new BException(operation.getFileName(),
						new CheckException("Cyclic dependencies between operations: " + sb, id)));

				return true;
			}
		}
		return false;
	}

	public RulesMachineRunConfiguration getRulesMachineRunConfiguration() {
		return this.rulesMachineRunConfiguration;
	}

	protected boolean isANewModel(MachineReference reference) {
		for (IModel iModel : bModels) {
			if (iModel.getMachineName().equals(reference.getName())) {
				return false;
			}
		}
		return true;
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
