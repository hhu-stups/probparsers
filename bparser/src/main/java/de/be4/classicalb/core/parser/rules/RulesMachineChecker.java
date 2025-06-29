package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;

import static de.be4.classicalb.core.parser.util.ASTBuilder.createIdentifier;

/*
 * This class checks that all extensions for the rules language are used in a correct way
 */
public class RulesMachineChecker extends DepthFirstAdapter {
	private String machineName;
	private final File file;
	private final Map<ARuleOperation, RuleOperation> rulesMap = new HashMap<>();
	private final Map<AComputationOperation, ComputationOperation> computationMap = new HashMap<>();
	private final Map<AFunctionOperation, FunctionOperation> functionMap = new HashMap<>();
	private final ArrayList<CheckException> errorList = new ArrayList<>();
	private final Set<AIdentifierExpression> referencedRuleOperations = new HashSet<>();

	// this list is used to track if certain nodes appear inside of a loop (i.e.
	// AForLoopSubstitution or AWhileSubstitution)
	private final List<Node> loopNodes = new ArrayList<>();

	private final KnownIdentifier knownIdentifier = new KnownIdentifier();
	private final LocalIdentifierScope identifierScope = new LocalIdentifierScope();
	private final Set<String> definitions = new HashSet<>();
	private final HashMap<String, HashSet<Node>> readIdentifier = new HashMap<>();
	private final List<MachineReference> machineReferences;

	private AbstractOperation currentOperation;

	// this map is used to track if all error types are implemented
	private final HashMap<RuleOperation, Set<Integer>> implementedErrorTypes = new HashMap<>();
	private final Start start;
	private TIdentifierLiteral nameLiteral;

	public RulesMachineChecker(final File file, List<MachineReference> machineReferences, Start start) {
		this.file = file;
		this.machineReferences = machineReferences;
		this.start = start;
	}

	public void runChecks() throws BCompoundException {
		start.apply(this);
		if (!errorList.isEmpty()) {
			final List<BException> bExceptionList = new ArrayList<>();
			final String filePath = file == null ? null : file.getPath();
			for (CheckException checkException : errorList) {
				final BException bException = new BException(filePath, checkException);
				bExceptionList.add(bException);
			}
			throw new BCompoundException(bExceptionList);
		}
	}

	public File getFile() {
		return this.file;
	}

	public Set<RuleOperation> getRuleOperations() {
		return new HashSet<>(this.rulesMap.values());
	}

	public TIdentifierLiteral getNameLiteral() {
		return this.nameLiteral;
	}

	public Set<AIdentifierExpression> getReferencedRuleOperations() {
		return new HashSet<>(this.referencedRuleOperations);
	}

	public List<AbstractOperation> getOperations() {
		List<AbstractOperation> list = new ArrayList<>();
		list.addAll(rulesMap.values());
		list.addAll(computationMap.values());
		list.addAll(functionMap.values());
		return list;
	}

	public RuleOperation getRuleOperation(ARuleOperation aRuleOperation) {
		return this.rulesMap.get(aRuleOperation);
	}

	public ComputationOperation getComputationOperation(AComputationOperation node) {
		return this.computationMap.get(node);
	}

	private boolean isNotInRule() {
		return currentOperation == null || !(currentOperation instanceof RuleOperation);
	}

	public FunctionOperation getFunctionOperation(AFunctionOperation funcOp) {
		return this.functionMap.get(funcOp);
	}

	public Set<String> getFunctionOperationNames() {
		Set<String> set = new HashSet<>();
		for (FunctionOperation func : this.functionMap.values()) {
			set.add(func.getName());
		}
		return set;
	}

	public Map<String, HashSet<Node>> getUnknownIdentifier() {
		HashMap<String, HashSet<Node>> result = new HashMap<>();
		for (Entry<String, HashSet<Node>> entry : readIdentifier.entrySet()) {
			String name = entry.getKey();
			HashSet<Node> nodes = entry.getValue();
			if (!this.knownIdentifier.getKnownIdentifierNames().contains(name) && !this.definitions.contains(name)
					&& !this.getFunctionOperationNames().contains(name)) {
				result.put(name, nodes);
			}
		}
		return result;
	}

	public Set<String> getGlobalIdentifierNames() {
		return this.knownIdentifier.getKnownIdentifierNames();
	}

	public Set<TIdentifierLiteral> getGlobalIdentifiers() {
		return this.knownIdentifier.getKnownIdentifiers();
	}

	public Set<String> getDefinitionNames() {
		return this.definitions;
	}

	@Override
	public void caseAMachineHeader(AMachineHeader node) {
		if (!node.getParameters().isEmpty()) {
			errorList.add(new CheckException("A RULES_MACHINE must not have any machine parameters", node));
		}
		LinkedList<TIdentifierLiteral> nameList = node.getName();
		if (nameList.size() > 1) {
			errorList.add(new CheckException("Renaming of a RULES_MACHINE name is not allowed.", node));
		}
		this.nameLiteral = nameList.get(0);
		this.machineName = nameLiteral.getText();

		// check self references; TODO: maybe we do not need this - duplicate references are ignored
		for (MachineReference machineReference : machineReferences) {
			if (machineReference.getName().equals(machineName)) {
				errorList.add(new CheckException("The reference '" + machineReference.getName() + "' has the same name as the machine in which it is contained.", machineReference.getNode()));
			}
		}
	}

	@Override
	public void caseAReferencesMachineClause(AReferencesMachineClause node) {
		// do nothing
	}

	@Override
	public void caseAFreetypesMachineClause(AFreetypesMachineClause node) {
		LinkedList<PExpression> identifiers = new LinkedList<>();
		for (PFreetype freetype : node.getFreetypes()) {
			if (freetype instanceof AFreetype) {
				AFreetype aFreetype = (AFreetype) freetype;
				identifiers.add(createIdentifier(aFreetype.getName()));
				for (PFreetypeConstructor freetypeConstructor : aFreetype.getConstructors()) {
					if (freetypeConstructor instanceof AElementFreetypeConstructor) {
						TIdentifierLiteral identifier = ((AElementFreetypeConstructor) freetypeConstructor).getName();
						identifiers.add(createIdentifier(identifier));
					} else if (freetypeConstructor instanceof AConstructorFreetypeConstructor) {
						TIdentifierLiteral identifier = ((AConstructorFreetypeConstructor) freetypeConstructor).getName();
						identifiers.add(createIdentifier(identifier));
					}
				}
			}
		}
		this.knownIdentifier.addKnownIdentifierList(identifiers);
	}

	@Override
	public void caseAAbstractConstantsMachineClause(AAbstractConstantsMachineClause node) {
		this.knownIdentifier.addKnownIdentifierList(node.getIdentifiers());
	}

	@Override
	public void caseAConstantsMachineClause(AConstantsMachineClause node) {
		this.knownIdentifier.addKnownIdentifierList(node.getIdentifiers());
	}

	@Override
	public void caseAEnumeratedSetSet(AEnumeratedSetSet node) {
		List<TIdentifierLiteral> copy = new ArrayList<>(node.getIdentifier());
		this.knownIdentifier.addKnownIdentifier(copy.get(0));
		this.knownIdentifier.addKnownIdentifierList(new ArrayList<>(node.getElements()));
	}

	class OccurredAttributes {
		final HashMap<String, POperationAttribute> map = new HashMap<>();

		public void add(String attrName, POperationAttribute node) {
			if (map.containsKey(attrName)) {
				errorList.add(new CheckException(String.format("%s clause is used more than once in operation '%s'.",
						attrName, currentOperation.getOriginalName()), node));
			}
			map.put(attrName, node);
		}
	}

	private void visitOperationAttributes(final LinkedList<POperationAttribute> attributes) {
		OccurredAttributes occurredAttributes = new OccurredAttributes();
		// set operation attributes
		for (POperationAttribute pOperationAttribute : attributes) {
			if (pOperationAttribute instanceof APredicateAttributeOperationAttribute) {
				checkOperationPredicateAttribute(occurredAttributes, pOperationAttribute);
			} else {
				checkOperationExpressionAttribute(occurredAttributes, pOperationAttribute);
			}
		}
	}

	private void checkOperationExpressionAttribute(OccurredAttributes occurredAttributes,
			POperationAttribute pOperationAttribute) throws AssertionError {
		AOperationAttribute attribute = (AOperationAttribute) pOperationAttribute;
		LinkedList<PExpression> arguments = attribute.getArguments();
		String name = attribute.getName().getText();
		occurredAttributes.add(name, pOperationAttribute);
		switch (name) {
		case RulesGrammar.DEPENDS_ON_RULE:
			checkDependsOnRuleAttribute(pOperationAttribute, arguments);
			return;
		case RulesGrammar.DEPENDS_ON_COMPUTATION:
			checkDependsOnComputationAttribute(pOperationAttribute, arguments);
			return;
		case RulesGrammar.RULEID:
			checkRuleIdAttribute(pOperationAttribute, arguments);
			return;
		case RulesGrammar.ERROR_TYPES:
			checkErrorTypesAttribute(pOperationAttribute, arguments);
			return;
		case RulesGrammar.CLASSIFICATION:
			checkClassificationAttribute(pOperationAttribute, arguments);
			return;
		case RulesGrammar.TAGS:
			checkTagsAttribute(pOperationAttribute, arguments);
			return;
		case RulesGrammar.REPLACES:
			checkReplacesAttribute(pOperationAttribute, arguments);
			return;
		default:
			throw new AssertionError("Unexpected operation attribute: " + name);
		}
	}

	private void checkReplacesAttribute(POperationAttribute pOperationAttribute, LinkedList<PExpression> arguments) {
		if (arguments.size() != 1 || !(arguments.get(0) instanceof AIdentifierExpression)) {
			errorList.add(new CheckException("Expected exactly one identifier after REPLACES.", pOperationAttribute));
			return;
		}
		final AIdentifierExpression idExpr = (AIdentifierExpression) arguments.get(0);
		currentOperation.addReplacesIdentifier(idExpr);
	}

	private void checkTagsAttribute(POperationAttribute pOperationAttribute, LinkedList<PExpression> arguments) {
		final List<String> tags = new ArrayList<>();
		for (PExpression pExpression : arguments) {
			if (pExpression instanceof AIdentifierExpression) {
				final AIdentifierExpression ident = (AIdentifierExpression) pExpression;
				final String identifierAsString = Utils.getTIdentifierListAsString(ident.getIdentifier());
				tags.add(identifierAsString);
			} else if (pExpression instanceof AStringExpression) {
				final AStringExpression stringExpr = (AStringExpression) pExpression;
				tags.add(stringExpr.getContent().getText());
			} else {
				errorList.add(new CheckException("Expected identifier or string after the TAGS attribute.",
						pOperationAttribute));
			}
		}
		currentOperation.addTags(tags);
	}

	private void checkClassificationAttribute(POperationAttribute pOperationAttribute,
			LinkedList<PExpression> arguments) {
		if (currentOperation instanceof RuleOperation) {
			final RuleOperation rule = (RuleOperation) currentOperation;
			if (arguments.size() == 1 && arguments.get(0) instanceof AIdentifierExpression) {
				AIdentifierExpression identifier = (AIdentifierExpression) arguments.get(0);
				String identifierString = Utils.getTIdentifierListAsString(identifier.getIdentifier());
				rule.setClassification(identifierString);
			} else {
				errorList.add(new CheckException("Expected exactly one identifier after CLASSIFICATION.",
						pOperationAttribute));
			}
		} else {
			errorList.add(new CheckException(
					"CLASSIFICATION is not an attribute of a FUNCTION or COMPUTATION operation.", pOperationAttribute));
		}
	}

	private void checkErrorTypesAttribute(POperationAttribute pOperationAttribute, LinkedList<PExpression> arguments) {
		if (currentOperation instanceof RuleOperation) {
			final RuleOperation rule = (RuleOperation) currentOperation;
			if (arguments.size() == 1 && arguments.get(0) instanceof AIntegerExpression) {
				AIntegerExpression intExpr = (AIntegerExpression) arguments.get(0);
				rule.setErrorTypes(intExpr);
			} else {
				errorList.add(
						new CheckException("Expected exactly one integer after ERROR_TYPES.", pOperationAttribute));
			}
		} else {
			errorList.add(new CheckException("ERROR_TYPES is not an attribute of a FUNCTION or COMPUTATION operation.",
					pOperationAttribute));
		}
	}

	private void checkRuleIdAttribute(POperationAttribute pOperationAttribute, LinkedList<PExpression> arguments) {
		if (currentOperation instanceof RuleOperation) {
			final RuleOperation rule = (RuleOperation) currentOperation;
			if (arguments.size() == 1 && arguments.get(0) instanceof AIdentifierExpression) {
				rule.setRuleId((AIdentifierExpression) arguments.get(0));
			} else {
				errorList
						.add(new CheckException("Expected exactly one identifier behind RULEID.", pOperationAttribute));
			}
		} else {
			errorList.add(new CheckException("RULEID is not an attribute of a FUNCTION or Computation operation.",
					pOperationAttribute));
		}
	}

	private void checkDependsOnComputationAttribute(POperationAttribute pOperationAttribute,
			LinkedList<PExpression> arguments) {
		List<AIdentifierExpression> list = new ArrayList<>();
		for (PExpression pExpression : arguments) {
			if (pExpression instanceof AIdentifierExpression) {
				list.add((AIdentifierExpression) pExpression);
			} else {
				errorList.add(new CheckException("Expected a list of identifiers after DEPENDS_ON_COMPUTATION.",
						pOperationAttribute));
			}
		}
		currentOperation.addAllComputationDependencies(list);
	}

	private void checkDependsOnRuleAttribute(POperationAttribute pOperationAttribute,
			LinkedList<PExpression> arguments) {
		final List<AIdentifierExpression> list = new ArrayList<>();
		for (final PExpression pExpression : arguments) {
			if (pExpression instanceof AIdentifierExpression) {
				list.add((AIdentifierExpression) pExpression);
			} else {
				errorList.add(new CheckException("Expected a list of identifiers after DEPENDS_ON_RULE.",
						pOperationAttribute));
			}
		}
		currentOperation.addAllRuleDependencies(list);
	}

	private void checkOperationPredicateAttribute(OccurredAttributes occurredAttributes,
			POperationAttribute pOperationAttribute) throws AssertionError {
		APredicateAttributeOperationAttribute attr = (APredicateAttributeOperationAttribute) pOperationAttribute;
		PPredicate predicate = attr.getPredicate();
		final String attrName = attr.getName().getText();
		occurredAttributes.add(attrName, pOperationAttribute);
		switch (attrName) {
		case RulesGrammar.ACTIVATION:
			if (currentOperation instanceof FunctionOperation) {
				errorList.add(new CheckException("ACTIVATION is not a valid attribute of a FUNCTION operation.",
						pOperationAttribute));
			} else {
				currentOperation.setActivationPredicate(predicate);
			}
			break;
		case RulesGrammar.PRECONDITION:
			if (currentOperation instanceof FunctionOperation) {
				FunctionOperation func = (FunctionOperation) currentOperation;
				func.setPreconditionPredicate(predicate);
			} else {
				errorList.add(
						new CheckException("PRECONDITION clause is not allowed for a RULE or COMPUTATION operation.",
								pOperationAttribute));
			}
			break;
		case RulesGrammar.POSTCONDITION:
			if (currentOperation instanceof RuleOperation) {
				errorList.add(new CheckException("POSTCONDITION attribute is not allowed for a RULE operation",
						pOperationAttribute));
			} else {
				currentOperation.setPostcondition(predicate);
			}
			break;
		default:
			throw new AssertionError("Unexpected operation attribute: " + attrName);
		}
		predicate.apply(this);
	}

	private boolean containsRule(String name) {
		for (RuleOperation rule : this.rulesMap.values()) {
			if (name.equals(rule.getOriginalName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void caseARuleOperation(ARuleOperation node) {
		currentOperation = new RuleOperation(node.getRuleName(), this.file == null ? null : this.file.getPath(), this.machineName, machineReferences);
		if (containsRule(currentOperation.getOriginalName())) {
			errorList.add(new CheckException("Duplicate operation name '" + currentOperation.getOriginalName() + "'.",
					node.getRuleName()));
		}
		RuleOperation ruleOp = (RuleOperation) currentOperation;
		rulesMap.put(node, ruleOp);
		visitOperationAttributes(node.getAttributes());
		node.getRuleBody().apply(this);
		checkAllErrorTypesImplemented(ruleOp);
		currentOperation = null;
	}

	private void checkAllErrorTypesImplemented(RuleOperation ruleOp) {
		Set<Integer> implemented = this.implementedErrorTypes.get(ruleOp);
		for (int i = 1; i <= ruleOp.getNumberOfErrorTypes(); i++) {
			if (implemented == null || !implemented.contains(i)) {
				errorList.add(new CheckException(
						String.format("Error type '%s' is not implemented in rule '%s'.", i, ruleOp.getOriginalName()),
						ruleOp.getNameLiteral()));
			}
		}
	}

	@Override
	public void caseAComputationOperation(AComputationOperation node) {
		currentOperation = new ComputationOperation(node.getName(), this.file == null ? null : this.file.getPath(), this.machineName, machineReferences);
		computationMap.put(node, (ComputationOperation) currentOperation);
		visitOperationAttributes(node.getAttributes());
		node.getBody().apply(this);
		currentOperation = null;
	}

	@Override
	public void caseAFunctionOperation(AFunctionOperation node) {
		currentOperation = new FunctionOperation(node.getName(), this.file == null ? null : this.file.getPath(), this.machineName, machineReferences);
		functionMap.put(node, (FunctionOperation) currentOperation);

		this.identifierScope.createNewScope(new ArrayList<>(node.getParameters()));
		this.identifierScope.createNewScope(new ArrayList<>(node.getReturnValues()), true);
		visitOperationAttributes(node.getAttributes());
		node.getBody().apply(this);
		currentOperation = null;
		this.identifierScope.removeScope();
	}

	@Override
	public void outADefineSubstitution(ADefineSubstitution node) {
		// the newly defined variable should not be used in the TYPE or the
		// VALUE section
		if (currentOperation != null && currentOperation instanceof ComputationOperation) {
			ComputationOperation computationOperation = (ComputationOperation) currentOperation;
			try {
				computationOperation.addDefineVariable(node.getName());
				this.knownIdentifier.addKnownIdentifier(node.getName());
			} catch (CheckException e) {
				this.errorList.add(e);
			}
		}

		// the DEFINE block should not appear within a loop substitution
		if (!this.loopNodes.isEmpty()) {
			this.errorList
					.add(new CheckException("A DEFINE substitution must not be contained in a loop substitution.", node));
		}
	}

	@Override
	public void caseAVarSubstitution(AVarSubstitution node) {
		final HashSet<String> variables = new HashSet<>();
		LinkedList<PExpression> identifiers = node.getIdentifiers();
		for (PExpression e : identifiers) {
			if (e instanceof AIdentifierExpression) {
				AIdentifierExpression id = (AIdentifierExpression) e;
				String name = id.getIdentifier().get(0).getText();
				variables.add(name);
			} else {
				errorList.add(new CheckException("There must be a list of identifiers in VAR substitution.", node));
			}
		}
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()), true);
		node.getSubstitution().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAOperatorExpression(AOperatorExpression node) {
		final String operatorName = node.getName().getText();
		final LinkedList<PExpression> parameters = node.getIdentifiers();
		switch (operatorName) {
		case RulesGrammar.STRING_FORMAT:
			checkStringFormatOperator(node, parameters);
			return;
		case RulesGrammar.GET_RULE_COUNTEREXAMPLES:
			checkGetRuleCounterExamplesOperator(node, parameters);
			return;
		default:
			throw new AssertionError("Unknown expression operator: " + operatorName);
		}
	}

	private void checkStringFormatOperator(AOperatorExpression node, final LinkedList<PExpression> parameters) {
		PExpression firstParam = parameters.get(0);
		Integer count = countPlaceHoldersInExpression(firstParam);
		if (count != null && count != parameters.size() - 1) {
			this.errorList.add(new CheckException("The number of arguments (" + (parameters.size() - 1)
					+ ") does not match the number of placeholders (" + count + ") in the string.", node));
		}
		LinkedList<PExpression> identifiers = node.getIdentifiers();
		for (PExpression pExpression : identifiers) {
			pExpression.apply(this);
		}
	}

	private Integer countPlaceHoldersInExpression(PExpression param) {
		if (param instanceof AConcatExpression) {
			AConcatExpression con = (AConcatExpression) param;
			Integer left = countPlaceHoldersInExpression(con.getLeft());
			Integer right = countPlaceHoldersInExpression(con.getRight());
			if (left == null || right == null) {
				return null;
			} else {
				return left + right;
			}
		} else if (param instanceof AStringExpression) {
			AStringExpression string = (AStringExpression) param;
			String content = string.getContent().getText();
			String subString = "~w";
			return countOccurrences(content, subString);
		} else {
			return null;
		}

	}

	private int countOccurrences(String content, String subString) {
		int subStringLength = subString.length();
		return (content.length() - content.replace(subString, "").length()) / subStringLength;
	}

	private void checkGetRuleCounterExamplesOperator(AOperatorExpression node,
			final LinkedList<PExpression> parameters) {
		// the grammar ensures at least one argument
		if (parameters.size() > 2) {
			this.errorList.add(new CheckException("Invalid number of arguments. Expected one or two arguments.", node));
		}
		PExpression pExpression = node.getIdentifiers().get(0);
		if (!(pExpression instanceof AIdentifierExpression)) {
			this.errorList.add(
					new CheckException("The first argument of GET_RULE_COUNTEREXAMPLES must be an identifier.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) pExpression);
	}

	@Override
	public void inAAssignSubstitution(AAssignSubstitution node) {
		ArrayList<PExpression> righthand = new ArrayList<>(node.getRhsExpressions());
		for (PExpression pExpression : righthand) {
			pExpression.apply(this);
		}
		List<PExpression> copy = new ArrayList<>(node.getLhsExpression());
		checkThatIdentifiersAreLocalVariables(copy);
	}

	@Override
	public void inAOperationCallSubstitution(AOperationCallSubstitution node) {
		LinkedList<TIdentifierLiteral> opNameList = node.getOperation();
		if (opNameList.size() > 1) {
			errorList.add(new CheckException("Renaming of operation names is not allowed.", node));
		}
		List<PExpression> copy = new ArrayList<>(node.getResultIdentifiers());
		checkThatIdentifiersAreLocalVariables(copy);
		if (currentOperation != null) {
			currentOperation.addFunctionCall(opNameList.get(0));
		}
	}

	private void checkThatIdentifiersAreLocalVariables(List<PExpression> identifiers) {
		for (PExpression e : identifiers) {
			if (e instanceof AIdentifierExpression) {
				AIdentifierExpression id = (AIdentifierExpression) e;
				String name = id.getIdentifier().get(0).getText();
				if (!this.identifierScope.isAssignableVariable(name)) {
					errorList.add(new CheckException("Identifier '" + name
							+ "' is not a local variable (VAR). Hence, it can not be assigned here.", id));
				}
			} else {
				errorList.add(new CheckException(
						"There must be an identifier on the left side of the assign substitution. A function assignment 'f(1) := 1' is also not permitted.",
						e));
			}
		}
	}

	@Override
	public void caseAIdentifierExpression(AIdentifierExpression node) {
		List<TIdentifierLiteral> copy = new ArrayList<>(node.getIdentifier());
		if (copy.size() > 1) {
			this.errorList.add(new CheckException("Identifier renaming is not allowed in a RULES_MACHINE.", node));
		}
		final String name = copy.get(0).getText();
		if (currentOperation != null) {
			currentOperation.addReadVariable(node);
		}
		if (!this.identifierScope.contains(name)) {
			addReadIdentifier(node);
		}
	}

	private void addReadIdentifier(AIdentifierExpression node) {
		LinkedList<TIdentifierLiteral> list = node.getIdentifier();
		String name = list.get(0).getText();
		if (this.readIdentifier.containsKey(name)) {
			HashSet<Node> hashSet = readIdentifier.get(name);
			hashSet.add(node);
		} else {
			HashSet<Node> hashSet = new HashSet<>();
			hashSet.add(node);
			readIdentifier.put(name, hashSet);
		}
	}

	@Override
	public void caseAOperatorPredicate(AOperatorPredicate node) {
		final List<PExpression> arguments = new ArrayList<>(node.getIdentifiers());
		final String operatorName = node.getName().getText();
		switch (operatorName) {
		case RulesGrammar.SUCCEEDED_RULE:
			checkSucceededRuleOperator(node, arguments);
			return;
		case RulesGrammar.SUCCEEDED_RULE_ERROR_TYPE:
			checkSucceededRuleErrorTypeOperator(node, arguments);
			return;
		case RulesGrammar.FAILED_RULE:
			checkFailedRuleOperator(node, arguments);
			return;
		case RulesGrammar.FAILED_RULE_ALL_ERROR_TYPES:
			checkFailedRuleAllErrorTypesOperator(node, arguments);
			return;
		case RulesGrammar.FAILED_RULE_ERROR_TYPE:
			checkFailedRuleErrorTypeOperator(node, arguments);
			return;
		case RulesGrammar.NOT_CHECKED_RULE:
			checkNotCheckedRuleOperator(node, arguments);
			return;
		case RulesGrammar.DISABLED_RULE:
			checkDisabledRuleOperator(node, arguments);
			return;
		default:
			throw new AssertionError("Unsupported predicate operator: " + operatorName);
		}
	}

	private void checkDisabledRuleOperator(AOperatorPredicate node, final List<PExpression> arguments) {
		if (arguments.size() != 1 && !(arguments.get(0) instanceof AIdentifierExpression)) {
			this.errorList.add(new CheckException(
					"The DISABLED_RULE predicate operator expects exactly one rule identifier.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) arguments.get(0));
	}

	private void checkNotCheckedRuleOperator(AOperatorPredicate node, final List<PExpression> arguments) {
		if (arguments.size() != 1 && !(arguments.get(0) instanceof AIdentifierExpression)) {
			this.errorList.add(new CheckException(
					"The NOT_CHECKED_RULE predicate operator expects exactly one rule identifier.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) arguments.get(0));
	}

	private void checkFailedRuleErrorTypeOperator(AOperatorPredicate node, final List<PExpression> arguments) {
		if (arguments.size() != 2) {
			this.errorList.add(new CheckException(
					"The FAILED_RULE_ERROR_TYPE predicate operator expects exactly two arguments.", node));
			return;
		}
		PExpression pExpression = node.getIdentifiers().get(0);
		if (!(pExpression instanceof AIdentifierExpression)) {
			this.errorList.add(
					new CheckException("The first argument of FAILED_RULE_ERROR_TYPE must be an identifier.", node));
			return;
		}
		PExpression secondArg = node.getIdentifiers().get(1);
		if (!(secondArg instanceof AIntegerExpression)) {
			this.errorList.add(new CheckException(
					"The second argument of FAILED_RULE_ERROR_TYPE must be an integer literal.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) arguments.get(0));
	}

	private void checkFailedRuleAllErrorTypesOperator(AOperatorPredicate node, final List<PExpression> arguments) {
		if (arguments.size() != 1 && !(arguments.get(0) instanceof AIdentifierExpression)) {
			this.errorList.add(new CheckException(
					"The FAILED_RULE_ALL_ERROR_TYPES predicate operator expects exactly one rule identifier.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) arguments.get(0));
	}

	private void checkFailedRuleOperator(AOperatorPredicate node, final List<PExpression> arguments) {
		if (arguments.size() != 1 && !(arguments.get(0) instanceof AIdentifierExpression)) {
			this.errorList.add(new CheckException(
					"The FAILED_RULE predicate operator expects exactly one rule identifier.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) arguments.get(0));
	}

	private void checkSucceededRuleOperator(AOperatorPredicate node, final List<PExpression> arguments) {
		if (arguments.size() != 1 || !(arguments.get(0) instanceof AIdentifierExpression)) {
			this.errorList.add(new CheckException(
					"The SUCCEEDED_RULE predicate operator expects exactly one rule identifier.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) arguments.get(0));
	}

	private void checkSucceededRuleErrorTypeOperator(AOperatorPredicate node, final List<PExpression> arguments) {
		if (arguments.size() != 2) {
			this.errorList.add(new CheckException(
					"The SUCCEEDED_RULE_ERROR_TYPE predicate operator expects exactly two arguments.", node));
			return;
		}
		PExpression pExpression = node.getIdentifiers().get(0);
		if (!(pExpression instanceof AIdentifierExpression)) {
			this.errorList.add(
					new CheckException("The first argument of SUCCEEDED_RULE_ERROR_TYPE must be an identifier.", node));
			return;
		}
		PExpression secondArg = node.getIdentifiers().get(1);
		if (!(secondArg instanceof AIntegerExpression)) {
			this.errorList.add(new CheckException(
					"The second argument of SUCCEEDED_RULE_ERROR_TYPE must be an integer value.", node));
			return;
		}
		this.referencedRuleOperations.add((AIdentifierExpression) arguments.get(0));
	}

	@Override
	public void caseARuleFailSubSubstitution(ARuleFailSubSubstitution node) {
		if (isNotInRule()) {
			errorList.add(new CheckException("RULE_FAIL used outside of a RULE operation.", node));
			return;
		}

		checkErrorType(node.getErrorType());
		if (!node.getIdentifiers().isEmpty() && node.getWhen() == null) {
			this.errorList.add(new CheckException(
					"The WHEN predicate must be provided if RULE_FAIL has at least one parameter.", node));
			return;
		}
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		if (node.getWhen() != null) {
			if (!node.getIdentifiers().isEmpty()) {
				// implication is not allowed as the top level predicate if
				// there are one or more parameters
				checkTopLevelPredicate(node.getWhen(), "(WHEN predicate in RULE_FAIL)");
			}
			node.getWhen().apply(this);
		}
		node.getMessage().apply(this);
		this.identifierScope.removeScope();

	}

	public void checkTopLevelPredicate(PPredicate node, String text) {
		if (node instanceof AImplicationPredicate) {
			errorList.add(
					new CheckException("Implication is not allowed as the top level predicate " + text + ".", node));
		}
	}

	@Override
	public void caseAForallSubMessageSubstitution(AForallSubMessageSubstitution node) {
		if (isNotInRule()) {
			errorList.add(new CheckException("RULE_FORALL used outside of a RULE operation.", node));
			return;
		}
		checkTopLevelPredicate(node.getWhere(), "(WHERE predicate in RULE_FORALL)");
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getWhere().apply(this);
		node.getExpect().apply(this);
		if (node.getOnSuccess() != null)
			node.getOnSuccess().apply(this);
		node.getMessage().apply(this);
		this.identifierScope.removeScope();
		checkErrorType(node.getErrorType());
	}

	private void checkErrorType(TIntegerLiteral node) {
		if (!(currentOperation instanceof RuleOperation)) {
			return;
		}
		final RuleOperation ruleOp = (RuleOperation) currentOperation;
		if (node != null) {
			int errorType = Integer.parseInt(node.getText());

			if (errorType > ruleOp.getNumberOfErrorTypes()) {
				errorList.add(new CheckException(
						"The error type exceeded the number of error types specified for this rule operation.", node));
			} else if (errorType < 1) {
				errorList.add(new CheckException("The ERROR_TYPE must be a natural number greater than zero.", node));
			} else {
				addImplementedErrorType(ruleOp, errorType);
			}
		} else {
			addImplementedErrorType(ruleOp, 1);
		}
	}

	private void addImplementedErrorType(RuleOperation ruleOp, int errorType) {
		if (implementedErrorTypes.containsKey(ruleOp)) {
			Set<Integer> set = implementedErrorTypes.get(ruleOp);
			set.add(errorType);
		} else {
			Set<Integer> set = new HashSet<>();
			set.add(errorType);
			implementedErrorTypes.put(ruleOp, set);
		}
	}

	@Override
	public void inAWhileSubstitution(AWhileSubstitution node) {
		loopNodes.add(node);
	}

	@Override
	public void outAWhileSubstitution(AWhileSubstitution node) {
		loopNodes.remove(node);
	}

	// nodes which introduces local identifiers
	@Override
	public void caseAForLoopSubstitution(AForLoopSubstitution node) {
		loopNodes.add(node);
		node.getSet().apply(this);
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getDoSubst().apply(this);
		this.identifierScope.removeScope();
		loopNodes.remove(node);
	}

	@Override
	public void caseALetSubstitution(ALetSubstitution node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicate().apply(this);
		node.getSubstitution().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseALetPredicatePredicate(ALetPredicatePredicate node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getAssignment().apply(this);
		node.getPred().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseALetExpressionExpression(ALetExpressionExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getAssignment().apply(this);
		node.getExpr().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAGeneralProductExpression(AGeneralProductExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAGeneralSumExpression(AGeneralSumExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAQuantifiedIntersectionExpression(AQuantifiedIntersectionExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseASymbolicQuantifiedUnionExpression(ASymbolicQuantifiedUnionExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAQuantifiedUnionExpression(AQuantifiedUnionExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseASymbolicComprehensionSetExpression(ASymbolicComprehensionSetExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAComprehensionSetExpression(AComprehensionSetExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseASymbolicEventBComprehensionSetExpression(ASymbolicEventBComprehensionSetExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseASymbolicLambdaExpression(ASymbolicLambdaExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicate().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseALambdaExpression(ALambdaExpression node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicate().apply(this);
		node.getExpression().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAExistsPredicate(AExistsPredicate node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getPredicate().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAForallPredicate(AForallPredicate node) {
		this.identifierScope.createNewScope(new LinkedList<>(node.getIdentifiers()));
		node.getImplication().apply(this);
		this.identifierScope.removeScope();
	}

	// definitions
	@Override
	public void caseAPredicateDefinitionDefinition(APredicateDefinitionDefinition node) {
		final String name = node.getName().getText();
		this.definitions.add(name);
		this.identifierScope.createNewScope(new LinkedList<>(node.getParameters()));
		node.getRhs().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseAExpressionDefinitionDefinition(AExpressionDefinitionDefinition node) {
		final String name = node.getName().getText();
		this.definitions.add(name);
		if ("GOAL".equals(name)) {
			errorList.add(new CheckException("The GOAL definition must be a predicate.", node));
			return;
		}
		this.identifierScope.createNewScope(new LinkedList<>(node.getParameters()));
		node.getRhs().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseASubstitutionDefinitionDefinition(ASubstitutionDefinitionDefinition node) {
		final String name = node.getName().getText();
		this.definitions.add(name);
		if ("GOAL".equals(name)) {
			errorList.add(new CheckException("The GOAL definition must be a predicate.", node));
			return;
		}
		this.identifierScope.createNewScope(new LinkedList<>(node.getParameters()));
		node.getRhs().apply(this);
		this.identifierScope.removeScope();
	}

	@Override
	public void caseADefinitionExpression(ADefinitionExpression node) {
		node.getDefLiteral().apply(this);
		final String defName = node.getDefLiteral().getText();
		if ("READ_XML_FROM_STRING".equals(defName)) {
			if (node.getParameters().size() != 1) {
				errorList.add(new CheckException(
						"The external function 'READ_XML_FROM_STRING' requires exactly one argument.", node));
				return;
			}
			PExpression pExpression = node.getParameters().get(0);
			if (pExpression instanceof AStringExpression) {
				AStringExpression aStringExpr = (AStringExpression) pExpression;
				TStringLiteral content = aStringExpr.getContent();
				String text = content.getText();
				int xmlStartIndex = text.indexOf("<?");
				if (xmlStartIndex == -1) {
					return;
				}
				String testString = text.substring(0, xmlStartIndex);
				int numberOfNewLines = testString.length() - testString.replace("\n", "").length();
				try {
					InputSource inputSource = new InputSource(new StringReader(text.trim()));
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();
					// Surprisingly, we need both of the following two lines in
					// order to obtain all error messages in English.
					Locale.setDefault(Locale.UK);
					saxParser.setProperty("http://apache.org/xml/properties/locale", Locale.UK);
					saxParser.parse(inputSource, new DefaultHandler());
				} catch (SAXParseException e) {
					final int line = content.getLine() + numberOfNewLines + e.getLineNumber() - 1;
					final int column = (numberOfNewLines == 0 && e.getLineNumber() == 1)
							? content.getPos() + e.getColumnNumber()
							: e.getColumnNumber();
					TStringLiteral dummy = new TStringLiteral("", line, column);
					String message = e.getMessage();
					errorList.add(new CheckException(message, dummy, e));
				} catch (SAXException e) {
					String message = e.getMessage();
					errorList.add(new CheckException(message, aStringExpr, e));
				} catch (ParserConfigurationException | IOException e) {
					/*
					 * We do nothing. The error is not handled by the parser but
					 * will be handled by the ProB prolog kernel.
					 */
				}

			}

		}
		super.caseADefinitionExpression(node);
	}

	/*
	 * nodes not allowed in a rules machine
	 */

	@Override
	public void caseAChoiceSubstitution(AChoiceSubstitution node) {
		errorList.add(new CheckException("A CHOICE substitution is not allowed in a RULES_MACHINE.", node));
	}

	@Override
	public void caseASeesMachineClause(ASeesMachineClause node) {
		errorList.add(new CheckException("The SEES clause is not allowed in a RULES_MACHINE.", node));
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node) {
		errorList.add(new CheckException("The USES clause is not allowed in a RULES_MACHINE.", node));
	}

	@Override
	public void caseAAnySubstitution(AAnySubstitution node) {
		errorList.add(new CheckException("The ANY substitution is not allowed in a RULES_MACHINE.", node));
	}

	@Override
	public void caseABecomesElementOfSubstitution(ABecomesElementOfSubstitution node) {
		errorList.add(new CheckException(
				"The BecomesElementOf substitution (a,b:(P)) is not allowed in a RULES_MACHINE.", node));
	}

	@Override
	public void caseADeferredSetSet(ADeferredSetSet node) {
		errorList.add(new CheckException("Deferred sets are not allowed in a RULES_MACHINE.", node));
	}

	class KnownIdentifier {
		final Map<String, TIdentifierLiteral> knownIdentifiers = new HashMap<>();

		public void addKnownIdentifierList(List<PExpression> parameters) {
			for (PExpression pExpression : parameters) {
				this.addKnownIdentifier(pExpression);
			}
		}

		public void addKnownIdentifier(TIdentifierLiteral identifier) {
			knownIdentifiers.put(identifier.getText(), identifier);
		}

		public Set<String> getKnownIdentifierNames() {
			return new HashSet<>(knownIdentifiers.keySet());
		}

		public Set<TIdentifierLiteral> getKnownIdentifiers() {
			return new HashSet<>(this.knownIdentifiers.values());
		}

		public void addKnownIdentifier(PExpression expression) {
			if (expression instanceof ADescriptionExpression) {
				// expression with description pragma
				this.addKnownIdentifier(((ADescriptionExpression) expression).getExpression());
			} else if (expression instanceof AIdentifierExpression) {
				AIdentifierExpression identifier = (AIdentifierExpression) expression;
				LinkedList<TIdentifierLiteral> list = identifier.getIdentifier();
				// the size of list is zero; this ensured by the grammar
				TIdentifierLiteral tIdentifierLiteral = list.get(0);
				String constantName = tIdentifierLiteral.getText();
				if (this.knownIdentifiers.containsKey(constantName)) {
					errorList.add(new CheckException("Identifier already exists.", expression));
					return;
				}
				knownIdentifiers.put(constantName, tIdentifierLiteral);
			} else {
				// should not occur
				errorList.add(new CheckException("Identifier expected.", expression));
			}
		}
	}

	class LocalIdentifierScope {
		private final LinkedList<Scope> localVariablesScope = new LinkedList<>();

		public void createNewScope(final List<PExpression> parameters) {
			createNewScope(parameters, false);
		}

		public void createNewScope(final List<PExpression> parameters, boolean assignable) {
			final HashSet<String> set = new HashSet<>();
			for (PExpression expression : parameters) {
				if (expression instanceof AIdentifierExpression) {
					AIdentifierExpression identifier = (AIdentifierExpression) expression;
					TIdentifierLiteral tIdentifierLiteral = identifier.getIdentifier().getFirst();
					String identifierName = tIdentifierLiteral.getText();
					set.add(identifierName);
				} else {
					errorList.add(new CheckException("Identifier expected.", expression));
				}
			}
			localVariablesScope.add(new Scope(set, assignable));
		}

		public void removeScope() {
			this.localVariablesScope.removeLast();
		}

		public boolean contains(String identifier) {
			return contains(identifier, false);
		}

		public boolean isAssignableVariable(String name) {
			return contains(name, true);
		}

		public boolean contains(String identifier, boolean checkAssignable) {
			for (int i = localVariablesScope.size() - 1; i >= 0; i--) {
				Scope scope = localVariablesScope.get(i);
				if (scope.identifiers.contains(identifier)) {
					if (checkAssignable) {
						return scope.assignable;
					} else {
						return true;
					}
				}
			}
			return false;
		}
	}

	static class Scope {
		final Set<String> identifiers;
		final boolean assignable;

		Scope(Set<String> identifiers, boolean assignable) {
			this.identifiers = identifiers;
			this.assignable = assignable;
		}
	}

}
