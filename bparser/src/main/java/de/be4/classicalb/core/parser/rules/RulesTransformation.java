package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.analysis.transforming.DefinitionInjector;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.ASTBuilder;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

import static de.be4.classicalb.core.parser.util.ASTBuilder.*;

public class RulesTransformation extends DepthFirstAdapter {

	public static final String RULE_FAIL = "FAIL";
	public static final String RULE_SUCCESS = "SUCCESS";
	public static final String RULE_NOT_CHECKED = "NOT_CHECKED";
	public static final String RULE_DISABLED = "DISABLED";

	public static final String COMPUTATION_EXECUTED = "EXECUTED";
	public static final String COMPUTATION_NOT_EXECUTED = "NOT_EXECUTED";
	public static final String COMPUTATION_DISABLED = "COMPUTATION_DISABLED";

	public static final String RULE_COUNTER_EXAMPLE_VARIABLE_SUFFIX = "_Counterexamples";
	public static final String RULE_SUCCESSFUL_VARIABLE_SUFFIX = "_Successful";
	public static final String RULE_UNCHECKED_VARIABLE_SUFFIX = "_Unchecked";

	private static final String ALL_TUPLE = "$AllTuple";
	private static final String RESULT_TUPLE = "$ResultTuple";

	private final IDefinitions iDefinitions;
	private final Start start;
	private final RulesMachineChecker rulesMachineChecker;
	private final ArrayList<CheckException> errorList = new ArrayList<>();
	private final ArrayList<String> ruleNames = new ArrayList<>();
	private final ArrayList<TIdentifierLiteral> ruleOperationLiteralList = new ArrayList<>();
	private final ArrayList<TIdentifierLiteral> computationLiteralList = new ArrayList<>();

	private final List<AIdentifierExpression> variablesList = new ArrayList<>();
	private final List<PPredicate> invariantList = new ArrayList<>();
	private final List<PSubstitution> initialisationList = new ArrayList<>();

	private RuleOperation currentRule;
	private TIdentifierLiteral currentComputationIdentifier;
	private final Map<String, AbstractOperation> allOperations;

	// used to provide unique identifiers for generated variables of FOR loops
	private int nestedForLoopCount = 0;
	private int ruleBodyCount = 0;

	// some operations may be deleted by they are replaced other operations
	private final HashSet<String> operationsToBeDeleted = new HashSet<>();

	/**
	 * @param start
	 *            The root node of the abstract syntax tree.
	 * @param bParser
	 *            The parser of the rules machine.
	 * @param rulesMachineChecker
	 *            the rules machine checker which has already analyzed the rules
	 *            machine
	 * @param allOperations
	 *            The list of all operation in the project. This parameter is
	 *            needed in order to detect invalid reference to operations
	 *            which does not exist. Note, that such checks can not be done
	 *            by the {@link RulesMachineChecker} because they need more than
	 *            the machine scope. For example, it is checked that the first
	 *            argument of the GET_RULE_COUNTEREXAMPLES operator is an
	 *            existing rule operation which may be located in another rules
	 *            machine.
	 * 
	 */
	public RulesTransformation(Start start, BParser bParser, RulesMachineChecker rulesMachineChecker,
			Map<String, AbstractOperation> allOperations) {
		this.start = start;
		this.iDefinitions = bParser.getDefinitions();
		this.rulesMachineChecker = rulesMachineChecker;
		this.allOperations = allOperations;

		for (AbstractOperation operation : allOperations.values()) {
			if (null != operation.getReplacesIdentifier()) {
				AIdentifierExpression idExpr = operation.getReplacesIdentifier();
				String opName = Utils.getAIdentifierAsString(idExpr);
				operationsToBeDeleted.add(opName);
			}
		}
	}

	public void runTransformation() throws BCompoundException {
		start.apply(this);
		DefinitionInjector.injectDefinitions(start, iDefinitions);
		MissingPositionsAdder.injectPositions(start);
		if (!this.errorList.isEmpty()) {
			File machineFile = this.rulesMachineChecker.getFile();
			String machineFilePath = machineFile == null ? null : machineFile.getPath();
			List<BException> list = new ArrayList<>();
			for (CheckException checkException : this.errorList) {
				list.add(new BException(machineFilePath, checkException));
			}
			throw new BCompoundException(list);
		}
	}

	public List<AbstractOperation> getOperations() {
		return this.rulesMachineChecker.getOperations();
	}

	public List<String> getComputations() {
		List<String> list = new ArrayList<>();
		for (TIdentifierLiteral literal : computationLiteralList) {
			list.add(literal.getText());
		}
		return list;
	}

	@Override
	public void outStart(Start node) {
		ClauseFinder finder = new ClauseFinder();
		node.apply(finder);
		if (!variablesList.isEmpty()) {
			AAbstractMachineParseUnit abstractMachineParseUnit = finder.abstractMachineParseUnit;
			AInitialisationMachineClause initialisationMachineClause;
			AInvariantMachineClause invariantMachineClause;
			AVariablesMachineClause variablesMachineClause;
			if (finder.variablesMachineClause == null) {
				variablesMachineClause = new AVariablesMachineClause();
				abstractMachineParseUnit.getMachineClauses().add(0, variablesMachineClause);
				invariantMachineClause = new AInvariantMachineClause();
				abstractMachineParseUnit.getMachineClauses().add(1, invariantMachineClause);
				initialisationMachineClause = new AInitialisationMachineClause();
				abstractMachineParseUnit.getMachineClauses().add(2, initialisationMachineClause);
			} else {
				variablesMachineClause = finder.variablesMachineClause;
				invariantMachineClause = finder.invariantMachineClause;
				initialisationMachineClause = finder.initialisationMachineClause;
			}

			ArrayList<PPredicate> invariantPredicateList = new ArrayList<>();
			if (invariantMachineClause.getPredicates() != null) {
				invariantPredicateList.add(invariantMachineClause.getPredicates());
			}

			ArrayList<PSubstitution> initSubstitutionList = new ArrayList<>();
			if (initialisationMachineClause.getSubstitutions() != null) {
				initSubstitutionList.add(initialisationMachineClause.getSubstitutions());
			}

			invariantPredicateList.addAll(invariantList);
			initSubstitutionList.addAll(initialisationList);

			// VARIABLES
			variablesMachineClause.getIdentifiers().addAll(variablesList);
			// INVARIANT
			final PPredicate conjunction = createConjunction(invariantPredicateList);
			invariantMachineClause.setPredicates(conjunction);

			// INITIALISATION
			if (initSubstitutionList.size() == 1) {
				initialisationMachineClause.setSubstitutions(initSubstitutionList.get(0));
			} else {
				ASequenceSubstitution seqSubstitution = new ASequenceSubstitution(initialisationList);
				initialisationMachineClause.setSubstitutions(seqSubstitution);
			}
		}
	}

	static class ClauseFinder extends DepthFirstAdapter {
		AAbstractMachineParseUnit abstractMachineParseUnit = null;
		AVariablesMachineClause variablesMachineClause = null;
		AInvariantMachineClause invariantMachineClause = null;
		AInitialisationMachineClause initialisationMachineClause = null;

		@Override
		public void inAAbstractMachineParseUnit(AAbstractMachineParseUnit node) {
			abstractMachineParseUnit = node;
		}

		@Override
		public void inAVariablesMachineClause(AVariablesMachineClause node) {
			variablesMachineClause = node;
		}

		@Override
		public void inAInvariantMachineClause(AInvariantMachineClause node) {
			invariantMachineClause = node;
		}

		@Override
		public void inAInitialisationMachineClause(AInitialisationMachineClause node) {
			initialisationMachineClause = node;
		}
	}

	@Override
	public void caseAComputationOperation(AComputationOperation node) {
		if (operationsToBeDeleted.contains(node.getName().getText())) {
			node.replaceBy(null);
		} else {
			ComputationOperation computationOperation = this.rulesMachineChecker.getComputationOperation(node);
			if (computationOperation.replacesOperation()) {
				this.currentComputationIdentifier = computationOperation.getReplacesIdentifier().getIdentifier()
						.getFirst();
			} else {
				this.currentComputationIdentifier = computationOperation.getNameLiteral();
			}
			super.caseAComputationOperation(node);
		}
	}

	public List<PPredicate> getOperationDependenciesAsPredicateList(AbstractOperation operation) {
		List<PPredicate> result = new ArrayList<>();
		for (AbstractOperation op : operation.getRequiredDependencies()) {
			if (op instanceof RuleOperation) {
				result.add(createEqualPredicate(op.getNameLiteral(), RULE_SUCCESS));
			} else if (op instanceof ComputationOperation) {
				result.add(createEqualPredicate(op.getNameLiteral(), COMPUTATION_EXECUTED));
			}
		}
		return result;
	}

	@Override
	public void outAComputationOperation(AComputationOperation node) {
		final ComputationOperation compOperation = this.rulesMachineChecker.getComputationOperation(node);
		computationLiteralList.add(node.getName());
		if (operationsToBeDeleted.contains(node.getName().getText())) {
			node.replaceBy(null);
			return;
		}

		AOperation operation = new AOperation();
		AIdentifierExpression nameIdentifier;
		{
			// defining the operation name
			// the SableCC node AOperation.class requires a list of
			// TIdentifierLiterals as name
			final List<TIdentifierLiteral> operationNameList = new ArrayList<>();
			if (null != compOperation.getReplacesIdentifier()) {

				// renaming the operation
				final TIdentifierLiteral first = compOperation.getReplacesIdentifier().getIdentifier().getFirst();
				operationNameList.add(first.clone());
				nameIdentifier = compOperation.getReplacesIdentifier().clone();
			} else {
				operationNameList.add(node.getName().clone());
				// TODO refactor
				nameIdentifier = new AIdentifierExpression(operationNameList).clone();
			}
			operation.setOpName(operationNameList);
		}

		AEqualPredicate grd1 = new AEqualPredicate(nameIdentifier.clone(), createStringExpression(COMPUTATION_NOT_EXECUTED));
		ASelectSubstitution select = new ASelectSubstitution();
		{
			// guard
			final List<PPredicate> selectConditionList = new ArrayList<>();
			selectConditionList.add(grd1);
			selectConditionList.addAll(getOperationDependenciesAsPredicateList(compOperation));
			select.setCondition(createConjunction(selectConditionList));
		}
		{
			// substitution
			final ArrayList<PExpression> varList = new ArrayList<>();
			final ArrayList<PExpression> exprList = new ArrayList<>();
			varList.add(nameIdentifier.clone());
			exprList.add(new AStringExpression(new TStringLiteral(COMPUTATION_EXECUTED)));
			AAssignSubstitution assign = new AAssignSubstitution(varList, exprList);
			select.setThen(new ASequenceSubstitution(createSubstitutionList(node.getBody(), assign)));
		}

		operation.setOperationBody(select);
		// replacing the computation by an ordinary operation
		node.replaceBy(operation);

		// create variables in VARIABLES clause
		variablesList.add(nameIdentifier.clone());

		/*-
		 * create predicate in INVARIANT
		 * Compute_foo : {"COMPUTATION_EXECUTED", "COMPUTATION_NOT_EXECUTED","COMPUTATION_DISABLED" }
		 */
		final ASetExtensionExpression set = new ASetExtensionExpression(
			Stream.of(COMPUTATION_EXECUTED, COMPUTATION_NOT_EXECUTED, COMPUTATION_DISABLED)
				.map(ASTBuilder::createStringExpression).collect(Collectors.toList()));
		final AMemberPredicate member = new AMemberPredicate(nameIdentifier.clone(), set);

		invariantList.add(member);

		PExpression value;
		if (compOperation.getActivationPredicate() != null) {
			value = new AIfThenElseExpression(compOperation.getActivationPredicate().clone(),
					createStringExpression(COMPUTATION_NOT_EXECUTED), new LinkedList<>(),
					createStringExpression(COMPUTATION_DISABLED));
		} else {
			value = createStringExpression(COMPUTATION_NOT_EXECUTED);
		}
		// create substitution in INITIALISATION clause
		final AAssignSubstitution initSub = new AAssignSubstitution(createExpressionList(nameIdentifier.clone()),
				createExpressionList(value));
		initialisationList.add(initSub);
	}

	@Override
	public void caseARuleOperation(ARuleOperation node) {
		if (operationsToBeDeleted.contains(this.rulesMachineChecker.getRuleOperation(node).getOriginalName())) {
			node.replaceBy(null);
		} else {
			// transform rule operation
			super.caseARuleOperation(node);
		}
	}

	@Override
	public void inARuleOperation(ARuleOperation node) {
		// setting current rule
		this.currentRule = this.rulesMachineChecker.getRuleOperation(node);
		this.ruleBodyCount = 0;
	}

	@Override
	public void outARuleOperation(ARuleOperation node) {
		final String ruleName = currentRule.getOriginalName();
		currentRule.getReplacesIdentifier();
		ruleOperationLiteralList.add(node.getRuleName());
		ruleNames.add(ruleName);

		// SELECT ruleName /= "NOT_CHECKED" THEN
		// ... END
		final List<PPredicate> selectConditionList = new ArrayList<>();
		selectConditionList.add(new AEqualPredicate(createIdentifier(ruleName),
			new AStringExpression(new TStringLiteral(RULE_NOT_CHECKED))));
		final ASelectSubstitution select = new ASelectSubstitution();
		selectConditionList.addAll(getOperationDependenciesAsPredicateList(currentRule));
		select.setCondition(createConjunction(selectConditionList));

		ArrayList<PSubstitution> subList = new ArrayList<>();
		// Try to avoid ProB warning "Operation 'event' is possibly reading or not always assigning to output parameters":
		// set rule to SUCCESS per default. If the rule fails, the value is overwritten.
		// previous solution checked for fails after the rule body and decided for success if no fail occurred
		// => should be equivalent to:
		// IF rule_Counterexamples /= {} THEN RULE_FAIL ELSE RULE_SUCCESS END
		subList.add(createRuleSuccessAssignment(currentRule.getNameLiteral()));
		subList.add(node.getRuleBody());

		final String ctName = ruleName + RULE_COUNTER_EXAMPLE_VARIABLE_SUFFIX;
		currentRule.setCounterExampleVariableName(ctName);

		final String sfName = ruleName + RULE_SUCCESSFUL_VARIABLE_SUFFIX;
		currentRule.setSuccessfulVariableName(sfName);

		final String ucName = ruleName + RULE_UNCHECKED_VARIABLE_SUFFIX;
		currentRule.setUncheckedVariableName(ucName);

		ASequenceSubstitution seq = new ASequenceSubstitution(subList);
		select.setThen(seq);

		// The following is a hack for accessing operation attributes on the Prolog side.
		// The attributes are read in btypechecker.pl and added to the info list as rules_info([tags(T),classification(C),ruleid(RId)]).
		Writer sw = new StringWriter();
		IPrologTermOutput pout = new PrologTermOutput(sw,false);
		pout.openTerm("rules_info").openList();
		if (!currentRule.getTags().isEmpty()) {
			pout.openTerm("tags").openList();
			currentRule.getTags().forEach(pout::printAtom);
			pout.closeList().closeTerm();
		}
		if (currentRule.getClassification() != null) {
			pout.openTerm("classification").printAtom(currentRule.getClassification()).closeTerm();
		}
		if (currentRule.getRuleIdString() != null) {
			pout.openTerm("rule_id").printAtom(currentRule.getRuleIdString()).closeTerm();
		}
		pout.closeList().closeTerm().fullstop();

		// replacing the rule operation by an ordinary operation, description contains operation attributes
		node.replaceBy(new ADescriptionOperation(
				new ADescriptionPragma(Collections.singletonList(new TPragmaFreeText(sw.toString()))),
				new AOperation(
						new LinkedList<>(),
						Collections.singletonList(node.getRuleName().clone()),
						new LinkedList<>(),
						select
		)));

		/* ******************************************************* */

		// VARIABLES
		variablesList.add(createIdentifier(node.getRuleName()));

		// INVARIANT
		ASetExtensionExpression set = new ASetExtensionExpression(
			Stream.of(RULE_FAIL, RULE_SUCCESS, RULE_NOT_CHECKED, RULE_DISABLED)
				.map(ASTBuilder::createStringExpression).collect(Collectors.toList()));
		AMemberPredicate member = createPositionedNode(
				new AMemberPredicate(createIdentifier(node.getRuleName()), set), node);

		invariantList.add(member);

		/*-
		 * If there are no constant dependencies: rule := RULE_NOT_CHECKED
		 * Otherwise: rule := IF dependencies THEN RULE_NOT_CHECKED ELSE RULE_DISABLED END
		 */
		PExpression value;
		if (currentRule.getActivationPredicate() != null) {
			value = new AIfThenElseExpression(currentRule.getActivationPredicate().clone(),
					createStringExpression(RULE_NOT_CHECKED), new LinkedList<>(),
					createStringExpression(RULE_DISABLED));
		} else {
			value = createStringExpression(RULE_NOT_CHECKED);
		}
		final AAssignSubstitution initSub = new AAssignSubstitution(
				createExpressionList(createIdentifier(node.getRuleName())), createExpressionList(value));
		initialisationList.add(initSub);
		// VARIABLES ...
		variablesList.add(createIdentifier(ctName, node.getRuleName().clone()));
		variablesList.add(createIdentifier(sfName, node.getRuleName().clone()));
		variablesList.add(createIdentifier(ucName, node.getRuleName().clone()));

		// INVARIANT rule1#Counterexamples : POW(INTEGER*STRING)
		final AMemberPredicate ctTypingPredicate = new AMemberPredicate(
			createIdentifier(ctName),
			new APowSubsetExpression(new AMultOrCartExpression(new ANatural1SetExpression(), new AStringSetExpression()))
		);
		invariantList.add(createPositionedNode(ctTypingPredicate, node));

		//  rule1#Successful : POW(INTEGER*STRING)
		final AMemberPredicate sfTypingPredicate = new AMemberPredicate(
			createIdentifier(sfName),
			new APowSubsetExpression(new AMultOrCartExpression(new ANatural1SetExpression(), new AStringSetExpression()))
		);
		invariantList.add(createPositionedNode(sfTypingPredicate, node));

		//  rule1#Unchecked : POW(INTEGER*STRING)
		final AMemberPredicate ucTypingPredicate = new AMemberPredicate(
				createIdentifier(ucName),
				new APowSubsetExpression(new AMultOrCartExpression(new ANatural1SetExpression(), new AStringSetExpression()))
		);
		invariantList.add(createPositionedNode(ucTypingPredicate, node));

		// INITIALISATION rule1#Counterexamples := {}
		initialisationList.add(createAssignNode(createIdentifier(ctName, node.getRuleName().clone()), new AEmptySetExpression()));

		//  rule1#Successful := {}
		initialisationList.add(createAssignNode(createIdentifier(sfName, node.getRuleName().clone()), new AEmptySetExpression()));

		//  rule1#Successful := {}
		initialisationList.add(createAssignNode(createIdentifier(ucName, node.getRuleName().clone()), new AEmptySetExpression()));
	}

	@Override
	public void outAOperatorExpression(AOperatorExpression node) {
		final String operatorName = node.getName().getText();
		final LinkedList<PExpression> parameters = node.getIdentifiers();
		switch (operatorName) {
			case RulesGrammar.STRING_FORMAT:
				translateStringFormatOperator(node, parameters);
				return;
			case RulesGrammar.GET_RULE_COUNTEREXAMPLES:
				translateGetRuleCounterExamplesOperator(node);
				return;
			default:
				throw new AssertionError("Unsupported operator " + operatorName);
		}
	}

	private void translateGetRuleCounterExamplesOperator(AOperatorExpression node) {
		final PExpression pExpression = node.getIdentifiers().get(0);
		final AIdentifierExpression id = (AIdentifierExpression) pExpression;
		final String ruleName = id.getIdentifier().get(0).getText();
		final AbstractOperation operation = allOperations.get(ruleName);
		if (!(operation instanceof RuleOperation)) {
			errorList.add(new CheckException(
					String.format("'%s' does not match any rule visible to this machine.", ruleName), node));
			return;
		}
		final RuleOperation rule = (RuleOperation) operation;
		final String name = ruleName + RULE_COUNTER_EXAMPLE_VARIABLE_SUFFIX;
		if (node.getIdentifiers().size() == 1) {
			final AIdentifierExpression ctVariable = createIdentifier(name, pExpression);
			final ARangeExpression range = createPositionedNode(new ARangeExpression(ctVariable), node);
			node.replaceBy(range);
		} else {
			PExpression funcCall = getSetOfErrorMessagesByErrorType(name, node.getIdentifiers().get(1),
					rule.getNumberOfErrorTypes());
			node.replaceBy(funcCall);
		}
	}

	private void translateStringFormatOperator(AOperatorExpression node, final LinkedList<PExpression> parameters) {
		addFormatToStringDefinition(iDefinitions);
		addToStringDefinition(iDefinitions);
		final List<PExpression> seqList = new ArrayList<>();
		for (int i = 1; i < parameters.size(); i++) {
			PExpression param = parameters.get(i);
			seqList.add(createPositionedNode(callExternalFunction(TO_STRING, param), param));
		}
		node.replaceBy(createPositionedNode(
			callExternalFunction(FORMAT_TO_STRING, parameters.get(0), new ASequenceExtensionExpression(seqList)),
			node));
	}

	private AAssignSubstitution createRuleSuccessAssignment(final TIdentifierLiteral ruleLiteral) {
		return createRuleAssignment(ruleLiteral, RULE_SUCCESS);
	}

	private AAssignSubstitution createRuleFailAssignment(final TIdentifierLiteral ruleLiteral) {
		// rule1 := "FAIL"
		return createRuleAssignment(ruleLiteral, RULE_FAIL);
	}

	private AAssignSubstitution createRuleAssignment(TIdentifierLiteral ruleLiteral, String ruleStatus) {
		return createAssignNode(createIdentifier(ruleLiteral), createStringExpression(ruleStatus));
	}

	private PSubstitution createConditionalFailAssignment() {
		PPredicate ifCondition = new ANotEqualPredicate(createIdentifier(RESULT_TUPLE), new AEmptySetExpression());
		return new AIfSubstitution(ifCondition, createRuleFailAssignment(currentRule.getNameLiteral()),
				new ArrayList<>(), null);
	}

	@Override
	public void outADefineSubstitution(ADefineSubstitution node) {
		variablesList.add(createIdentifier(node.getName().getText(), node.getName()));

		if (node.getDummyValue() != null) {
			initialisationList.add(createAssignNode(createIdentifier(node.getName()), node.getDummyValue()));
		} else {
			initialisationList.add(createAssignNode(createIdentifier(node.getName()), new AEmptySetExpression()));
		}

		PExpression value;
		if (node.getValue() instanceof ASymbolicLambdaExpression
				|| node.getValue() instanceof ASymbolicComprehensionSetExpression
				|| node.getValue() instanceof ASymbolicEventBComprehensionSetExpression
				|| node.getValue() instanceof ASymbolicQuantifiedUnionExpression) {
			value = node.getValue();
		} else {
			addForceDefinition(iDefinitions);
			value = createPositionedNode(callExternalFunction(FORCE, node.getValue()), node.getValue());
		}

		if (node.getType() != null) { // use TYPE if provided; compname=COMPUTATION_EXECUTED => name:type
			final TIdentifierLiteral computationIdentifierLiteral = this.currentComputationIdentifier.clone();
			PPredicate compExecuted = new AEqualPredicate(createIdentifier(computationIdentifierLiteral),
					createStringExpression(COMPUTATION_EXECUTED));
			AMemberPredicate member = new AMemberPredicate(createIdentifier(node.getName()), node.getType());
			invariantList.add(new AImplicationPredicate(compExecuted, member));
		} else {
			// else: (btrue or name=value) for typing
			invariantList.add(new ADisjunctPredicate(
					new ATruthPredicate(),
					new AEqualPredicate(createIdentifier(node.getName()), value.clone())
			));
		}

		node.replaceBy(createAssignNode(createIdentifier(node.getName()), value));
	}

	private PSubstitution createCounterExampleSubstitution(int errorIndex, PExpression setOfCounterexamples, boolean conditionalFail) {
		final String ctName = currentRule.getOriginalName() + RULE_COUNTER_EXAMPLE_VARIABLE_SUFFIX;

		final AUnionExpression union = new AUnionExpression(createIdentifier(ctName),
				createPositionedNode(new AMultOrCartExpression(
						new ASetExtensionExpression(createExpressionList(
								new AIntegerExpression(new TIntegerLiteral(Integer.toString(errorIndex))))),
						setOfCounterexamples.clone()), setOfCounterexamples));
		AAssignSubstitution assign = new AAssignSubstitution(createExpressionList(createIdentifier(ctName)),
				createExpressionList(union));
		if (conditionalFail) {
			return createSequenceSubstitution(assign, createConditionalFailAssignment());
		} else {
			return createSequenceSubstitution(assign, createRuleFailAssignment(currentRule.getNameLiteral()));
		}

	}

	private PSubstitution createSuccessfulSubstitution(PExpression setOfSuccessMessages) {
		final String sfName = currentRule.getOriginalName() + RULE_SUCCESSFUL_VARIABLE_SUFFIX;

		final AUnionExpression union = new AUnionExpression(createIdentifier(sfName),
			createPositionedNode(new AMultOrCartExpression(
					new ASetExtensionExpression(createExpressionList(createIntegerExpression(ruleBodyCount))),
					setOfSuccessMessages.clone()
				),
				setOfSuccessMessages
			));

		AAssignSubstitution assign = new AAssignSubstitution(createExpressionList(createIdentifier(sfName)),
			createExpressionList(union));
		return new ASequenceSubstitution(Collections.singletonList(assign));
	}

	private PSubstitution createUncheckedSubstitution(PExpression uncheckedMessage) {
		final String ucName = currentRule.getOriginalName() + RULE_UNCHECKED_VARIABLE_SUFFIX;
		final AUnionExpression union = new AUnionExpression(createIdentifier(ucName),
				createPositionedNode(new ASetExtensionExpression(Collections.singletonList(new ACoupleExpression(
						Arrays.asList(createIntegerExpression(ruleBodyCount), uncheckedMessage.clone())))), uncheckedMessage));

		AAssignSubstitution assign = new AAssignSubstitution(createExpressionList(createIdentifier(ucName)),
				createExpressionList(union));
		return new ASequenceSubstitution(Collections.singletonList(assign));
	}

	@Override
	public void outAFunctionOperation(AFunctionOperation node) {
		FunctionOperation func = rulesMachineChecker.getFunctionOperation(node);

		final List<PPredicate> preConditionList = new ArrayList<>();
		if (func.getPreconditionPredicate() != null) {
			preConditionList.add(func.getPreconditionPredicate());
		}

		preConditionList.addAll(getOperationDependenciesAsPredicateList(func));

		PSubstitution body = node.getBody();
		if (null != func.getPostconditionPredicate()) {
			body = createSequenceSubstitution(body, new AAssertionSubstitution(func.getPostconditionPredicate(),
				new ASkipSubstitution()));
		}

		if (!preConditionList.isEmpty()) {
			body = new APreconditionSubstitution(createConjunction(preConditionList), body);
		}
		node.replaceBy(new AOperation(new LinkedList<>(node.getReturnValues()), Collections.singletonList(node.getName()),
				new LinkedList<>(node.getParameters()), body));
	}

	private PExpression getSetOfErrorMessagesByErrorType(String name, PExpression errorTypeNode,
			int numberOfErrorTypes) {
		final String LAMBDA_IDENTIFIER = "$x";
		final ALambdaExpression lambda = new ALambdaExpression(
			createExpressionList(createIdentifier(LAMBDA_IDENTIFIER)),
			new AMemberPredicate(createIdentifier(LAMBDA_IDENTIFIER),
				new AIntervalExpression(createIntegerExpression(1), createIntegerExpression(numberOfErrorTypes))),
			new AImageExpression(createIdentifier(name), createSetOfPExpression(createIdentifier(LAMBDA_IDENTIFIER)))
		);
		return new AFunctionExpression(lambda, createExpressionList(errorTypeNode));
	}

	@Override
	public void outAOperatorPredicate(AOperatorPredicate node) {
		// currently all operator handle rule names
		final List<PExpression> arguments = new ArrayList<>(node.getIdentifiers());
		final String operatorName = node.getName().getText();
		final AIdentifierExpression ruleIdentifier = (AIdentifierExpression) arguments.get(0);
		final String ruleName = ruleIdentifier.getIdentifier().get(0).getText();
		AbstractOperation operation = allOperations.get(ruleName);
		if (!(operation instanceof RuleOperation)) {
			errorList.add(new CheckException(
					String.format("'%s' does not match any rule visible to this machine.", ruleName), node));
			return;
		}
		final RuleOperation rule = (RuleOperation) operation;
		switch (operatorName) {
			case RulesGrammar.SUCCEEDED_RULE:
				replacePredicateOperator(node, arguments, RULE_SUCCESS);
				return;
			case RulesGrammar.SUCCEEDED_RULE_ERROR_TYPE:
				replaceSucceededRuleErrorTypeOperator(node, ruleName, rule);
				return;
			case RulesGrammar.FAILED_RULE:
				replacePredicateOperator(node, arguments, RULE_FAIL);
				return;
			case RulesGrammar.FAILED_RULE_ALL_ERROR_TYPES:
				replaceFailedRuleAllErrorTypesOperator(node, rule);
				return;
			case RulesGrammar.FAILED_RULE_ERROR_TYPE:
				replaceFailedRuleErrorTypeOperator(node, rule);
				return;
			case RulesGrammar.NOT_CHECKED_RULE:
				replacePredicateOperator(node, arguments, RULE_NOT_CHECKED);
				return;
			case RulesGrammar.DISABLED_RULE:
				replacePredicateOperator(node, arguments, RULE_DISABLED);
				return;
			default:
				throw new AssertionError("should not happen: " + operatorName);
		}
	}

	private void replaceSucceededRuleErrorTypeOperator(AOperatorPredicate node, final String ruleName,
			final RuleOperation rule) {
		String name = ruleName + RULE_COUNTER_EXAMPLE_VARIABLE_SUFFIX;
		PExpression funcCall = getSetOfErrorMessagesByErrorType(name, node.getIdentifiers().get(1),
				rule.getNumberOfErrorTypes());
		node.replaceBy(new AEqualPredicate(funcCall, new AEmptySetExpression()));
	}

	private void replaceFailedRuleAllErrorTypesOperator(AOperatorPredicate node, final RuleOperation rule) {
		// dom(rule_cts) = 1..n
		String name = rule.getOriginalName() + RULE_COUNTER_EXAMPLE_VARIABLE_SUFFIX;
		AEqualPredicate equal = new AEqualPredicate(new ADomainExpression(createIdentifier(name)),
				new AIntervalExpression(createIntegerExpression(1),
						createIntegerExpression(rule.getNumberOfErrorTypes())));
		node.replaceBy(equal);
	}

	private void replaceFailedRuleErrorTypeOperator(AOperatorPredicate node, final RuleOperation rule) {
		PExpression pExpression = node.getIdentifiers().get(0);
		AIdentifierExpression id = (AIdentifierExpression) pExpression;
		String name = id.getIdentifier().get(0).getText() + RULE_COUNTER_EXAMPLE_VARIABLE_SUFFIX;
		PExpression funcCall = getSetOfErrorMessagesByErrorType(name, node.getIdentifiers().get(1),
				rule.getNumberOfErrorTypes());
		node.replaceBy(new ANotEqualPredicate(funcCall, new AEmptySetExpression()));
	}

	private void replacePredicateOperator(final AOperatorPredicate node, List<PExpression> copy,
			final String stringValue) {
		final List<PPredicate> predList = new ArrayList<>();
		for (PExpression e : copy) {
			predList.add(createPositionedNode(new AEqualPredicate(e, createStringExpression(stringValue)), e));
		}
		node.replaceBy(createConjunction(predList));
	}

	@Override
	public void inAForLoopSubstitution(AForLoopSubstitution node) {
		nestedForLoopCount++;
	}

	@Override
	public void outAForLoopSubstitution(AForLoopSubstitution node) {
		/*-
		 * FOR x IN set DO sub END
		 * or
		 * FOR x,y IN set DO sub END
		 * 
		 */
		nestedForLoopCount--;
		final String localSetVariableName = "$SET" + nestedForLoopCount;
		final String localLoopCounter = "$c" + nestedForLoopCount;

		// G_Set := set
		final PSubstitution assignSetVariable = new AAssignSubstitution(
				createExpressionList(createIdentifier(localSetVariableName, node.getSet())),
				createExpressionList(node.getSet().clone()));
		final PSubstitution assignCVariable = new AAssignSubstitution(
				createExpressionList(createIdentifier(localLoopCounter)),
				createExpressionList(new ACardExpression(createIdentifier(localSetVariableName, node.getSet()))));

		final AWhileSubstitution whileSub = createPositionedNode(new AWhileSubstitution(), node);
		final List<PSubstitution> subList = new ArrayList<>();
		subList.add(assignSetVariable);
		subList.add(assignCVariable);
		subList.add(whileSub);
		final AVarSubstitution varSub = createPositionedNode(
				new AVarSubstitution(createExpressionList(createIdentifier(localSetVariableName, node.getSet()),
						createIdentifier(localLoopCounter)), new ASequenceSubstitution(subList)),
				node);

		// WHILE set /= {}
		final ANotEqualPredicate whileCon = new ANotEqualPredicate(
				createIdentifier(localSetVariableName, node.getSet()), new AEmptySetExpression());
		whileSub.setCondition(whileCon);
		// INVARIANT btrue
		whileSub.setInvariant(new ATruthPredicate());

		// VARIANT card(set)
		whileSub.setVariant(createIdentifier(localLoopCounter));

		// VAR x IN ...
		final AVarSubstitution varSub2 = new AVarSubstitution();
		whileSub.setDoSubst(varSub2);
		List<PExpression> varIdList = new ArrayList<>();
		for (PExpression pExpression : node.getIdentifiers()) {
			varIdList.add(pExpression.clone());
		}
		varSub2.setIdentifiers(varIdList);

		addChooseDefinition(iDefinitions);
		PExpression chooseCall = callExternalFunction(CHOOSE, createIdentifier(localSetVariableName, node.getSet()));
		PSubstitution assignSub;
		if (varIdList.size() >= 2) {
			// <code> x,y :: {CHOOSE(set)}; </code>
			List<PExpression> assignIdList = new ArrayList<>();
			for (PExpression pExpression : node.getIdentifiers()) {
				assignIdList.add(pExpression.clone());
			}
			assignSub = new ABecomesElementOfSubstitution(assignIdList,
					new ASetExtensionExpression(createExpressionList(chooseCall)));
		} else {
			// <code> x := CHOOSE(set); </code>
			assignSub = new AAssignSubstitution(createExpressionList(node.getIdentifiers().get(0).clone()),
					createExpressionList(chooseCall));
		}

		// <code> G_Set := G_Set \ {x} </code>
		PExpression element;
		if (varIdList.size() >= 2) {
			List<PExpression> ids = new ArrayList<>();
			for (PExpression pExpression : node.getIdentifiers()) {
				ids.add(pExpression.clone());
			}
			element = createNestedCouple(ids);
		} else {
			element = node.getIdentifiers().get(0).clone();
		}

		// <code> G_Set \ {CHOOSE(G_Set)} </code>
		PExpression rhs = new AMinusOrSetSubtractExpression(createIdentifier(localSetVariableName, node.getSet()),
				new ASetExtensionExpression(createExpressionList(element)));

		PSubstitution assignSetVariable2 = new AAssignSubstitution(
				createExpressionList(createIdentifier(localSetVariableName, node.getSet()),
						createIdentifier(localLoopCounter)),
				createExpressionList(rhs, new AMinusOrSetSubtractExpression(createIdentifier(localLoopCounter),
						createIntegerExpression(1))));

		varSub2.setSubstitution(new ASequenceSubstitution(Arrays.asList(assignSub, node.getDoSubst(), assignSetVariable2)));
		node.replaceBy(varSub);
	}

	@Override
	public void outARuleFailSubSubstitution(ARuleFailSubSubstitution node) {
		this.ruleBodyCount++;
		addForceDefinition(iDefinitions);
		Node newNode;
		if (!node.getIdentifiers().isEmpty()) {
			newNode = createPositionedNode(createCounterExampleSubstitutions(node.getIdentifiers(), node.getWhen(),
				null, null, node.getMessage(), null, node.getErrorType()), node);
		} else {
			// default value is 1 if no value is provided
			int errorType = node.getErrorType() != null ? Integer.parseInt(node.getErrorType().getText()) : 1;
			PSubstitution sub = createCounterExampleSubstitution(errorType,
					createSetOfPExpression(node.getMessage(), node.getMessage()), false);
			// 1st case: there is a when predicate but no parameters
			// 2nd case: no parameters and no when predicate
			newNode = node.getWhen() != null ? new AIfSubstitution(node.getWhen(), sub, new ArrayList<>(), null) : sub;
		}
		node.replaceBy(newNode);
	}

	// @Override
	// public void caseAMemberPredicate(AMemberPredicate node) {
	// node.getLeft().apply(this);
	// node.getRight().apply(this);
	// if (node.getLeft() instanceof ARecordFieldExpression) {
	// // rewrite r'a : S to {r'a} /\ S /= {} in order to prevent an
	// // enumeration point
	// AIntersectionExpression inter = new
	// AIntersectionExpression(createSetOfPExpression(node.getLeft()),
	// node.getRight());
	// node.replaceBy(new ANotEqualPredicate(inter, new AEmptySetExpression()));
	// }
	// }

	public PSubstitution createCounterExampleSubstitutions(final List<PExpression> identifiers,
			final PPredicate wherePredicate, final PPredicate expectPredicate, final PExpression onSuccessMessage,
			final PExpression counterExampleMessage, final PExpression uncheckedMessage, final TIntegerLiteral errorTypeNode) {

		final String ON_SUCCESS_STRINGS = "$OnSuccessStrings";
		final String COUNTEREXAMPLE_STRINGS = "$CounterexampleStrings";
		final String UNCHECKED_STRING = "$UncheckedString"; // just a single string

		final AComprehensionSetExpression setWithoutExpect = new AComprehensionSetExpression();
		{
			final List<PExpression> list = new ArrayList<>();
			for (PExpression id : identifiers) {
				list.add(id.clone());
			}
			setWithoutExpect.setIdentifiers(list);
			setWithoutExpect.setPredicates(wherePredicate.clone());
		}
		addToStringDefinition(this.iDefinitions);
		// default error type: 1
		int errorType = errorTypeNode != null ? Integer.parseInt(errorTypeNode.getText()) : 1;
		AVarSubstitution var = new AVarSubstitution();

		List<PExpression> varIdentifiers = createExpressionList(createIdentifier(RESULT_TUPLE), createIdentifier(COUNTEREXAMPLE_STRINGS));
		if (expectPredicate != null) {
			varIdentifiers.add(createIdentifier(ALL_TUPLE));
		}
		if (onSuccessMessage != null) {
			varIdentifiers.add(createIdentifier(ON_SUCCESS_STRINGS));
		}
		if (uncheckedMessage != null) {
			varIdentifiers.add(createIdentifier(UNCHECKED_STRING));
		}
		var.setIdentifiers(varIdentifiers);
		List<PSubstitution> subList = new ArrayList<>();
		// if ON_SUCCESS clause exists (only RULE_FORALL): first assign allTuples without EXPECT clause
		// else: directly assign RESULT_TUPLE (only RULE_FAIL)
		{
			AAssignSubstitution assign = new AAssignSubstitution();
			assign.setLhsExpression(createExpressionList(createIdentifier(expectPredicate != null ? ALL_TUPLE : RESULT_TUPLE)));
			assign.setRhsExpressions(createExpressionList(setWithoutExpect));
			subList.add(assign);
		}
		if (expectPredicate != null) {
			final AComprehensionSetExpression setWithExpect = new AComprehensionSetExpression();
			final List<PExpression> list = new ArrayList<>();
			final List<PExpression> list2 = new ArrayList<>();
			for (PExpression id : identifiers) {
				list.add(id.clone());
				list2.add(id.clone());
			}
			setWithExpect.setIdentifiers(list);
			setWithExpect.setPredicates(new AConjunctPredicate(
				new AMemberPredicate(
					list2.size() > 1 ? new ACoupleExpression(list2) : list2.get(0),
					createIdentifier(ALL_TUPLE)
				),
				new ANegationPredicate(expectPredicate.clone())));

			AAssignSubstitution assign = new AAssignSubstitution();
			assign.setLhsExpression(createExpressionList(createIdentifier(RESULT_TUPLE)));
			// don't use FORCE here to allow infinitely many counter examples
			// enumeration warnings are then handled by RuleResult in prob_java
			assign.setRhsExpressions(createExpressionList(setWithExpect));
			subList.add(assign);
		}
		if (onSuccessMessage != null) {
			final List<PExpression> list = new ArrayList<>();
			final List<PExpression> list2 = new ArrayList<>();
			for (PExpression id : identifiers) {
				list.add(id.clone());
				list2.add(id.clone());
			}
			AMemberPredicate member = new AMemberPredicate(
				list.size() > 1 ? new ACoupleExpression(list) : list.get(0),
				new AMinusOrSetSubtractExpression(
					createIdentifier(ALL_TUPLE),
					createIdentifier(RESULT_TUPLE)
				)
			);

			// don't use FORCE(.); successful applications can be infinitely many!
			subList.add(new AAssignSubstitution(
				createExpressionList(createIdentifier(ON_SUCCESS_STRINGS)),
				createExpressionList(new AEventBComprehensionSetExpression(list2, onSuccessMessage, member))
			));
		}
		if (uncheckedMessage != null) {
			// just a single string; identifier is not in the all_tuple set
			subList.add(new AAssignSubstitution(
					createExpressionList(createIdentifier(UNCHECKED_STRING)),
					createExpressionList(uncheckedMessage)
			));
		}
		{
			final List<PExpression> list = new ArrayList<>();
			final List<PExpression> list2 = new ArrayList<>();
			for (PExpression id : identifiers) {
				list.add(id.clone());
				list2.add(id.clone());
			}
			PExpression couple = list.size() > 1 ? new ACoupleExpression(list) : list.get(0);
			AMemberPredicate member = new AMemberPredicate(couple, createIdentifier(RESULT_TUPLE));

			// don't use FORCE here to allow infinitely many counter examples
			// enumeration warnings are then handled by RuleResult in prob_java
			subList.add(new AAssignSubstitution(
				createExpressionList(createIdentifier(COUNTEREXAMPLE_STRINGS)),
				createExpressionList(new AEventBComprehensionSetExpression(list2, counterExampleMessage, member))
			));
		}

		PSubstitution counterExampleSubstitution = createCounterExampleSubstitution(errorType,
				createIdentifier(COUNTEREXAMPLE_STRINGS), true);
		subList.add(counterExampleSubstitution);

		if (onSuccessMessage != null) {
			PSubstitution successfulSubstitution = createSuccessfulSubstitution(createIdentifier(ON_SUCCESS_STRINGS));
			subList.add(successfulSubstitution);
		}
		if (uncheckedMessage != null) {
			PPredicate ifUnchecked = new AEqualPredicate(createIdentifier(ALL_TUPLE), new AEmptySetExpression());
			AIfSubstitution uncheckedSubstitution = new AIfSubstitution(ifUnchecked,
					createUncheckedSubstitution(uncheckedMessage),
					new ArrayList<>(),
					null);
			subList.add(uncheckedSubstitution);
		}

		ASequenceSubstitution seqSub = new ASequenceSubstitution(subList);
		var.setSubstitution(seqSub);
		return var;
	}

	@Override
	public void outAForallSubMessageSubstitution(AForallSubMessageSubstitution node) {
		this.ruleBodyCount++;
		addForceDefinition(iDefinitions);
		PSubstitution newNode = createPositionedNode(createCounterExampleSubstitutions(node.getIdentifiers(),
				node.getWhere(), node.getExpect(), node.getOnSuccess(), node.getMessage(), node.getUnchecked(), node.getErrorType()), node);
		node.replaceBy(newNode);
	}

}
