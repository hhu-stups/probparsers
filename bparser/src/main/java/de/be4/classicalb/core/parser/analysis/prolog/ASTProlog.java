package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.analysis.AnalysisAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.prolog.output.IPrologTermOutput;

/**
 * This class defines the output of a B machine as a prolog term.
 */
public class ASTProlog extends AnalysisAdapter {
	// The tables SUM_TYPE and SIMPLE_NAME are used to translate the Java class
	// name to
	// the Prolog functor name.
	// These tables MUST be in sync with BParser.scc.
	// SUM_TYPE must list all sum-types in BPaser.scc.
	// The name of the sum-type is not part of the Prolog functor.

	// SIMPLE_NAME must list all AST Classes that are not part of a sum-type
	// If a class is not a token , not in ATOMIC_TYPE and not in SUM_TYPE we
	// throw an exception.
	private static final List<String> SUM_TYPE = Collections.unmodifiableList(Arrays.asList("expression", "predicate",
			"machine_clause", "substitution", "parse_unit", "model_clause", "context_clause", "eventstatus",
			"argpattern", "set", "machine_variant", "definition", "freetype_constructor"));

	private static final List<String> ATOMIC_TYPE = Collections.unmodifiableList(Arrays.asList(
			"description_event", // for ADescriptionEvent
			"description_machine_clause", // for ADescriptionMachineClause
			"description_operation",
			"description_pragma",
			"event",
			"freetype",
			"machine_header", "machine_reference", "operation",
			"refined_operation", "rec_entry", "values_entry", "witness", "unit"));

	// the simpleFormats are mappings from node classes to prolog functor representing them
	private final Map<Class<? extends Node>, String> simpleFormats = new HashMap<>();

	// to look up the identifier of each node
	private final PositionPrinter positionPrinter;

	// helper object to print the prolog terms
	private final IPrologTermOutput pout;

	/**
	 * @param start
	 *            the AST node which should contain an
	 *            {@link AExpressionParseUnit}, an {@link APredicateParseUnit}
	 *            or an {@link ASubstitutionParseUnit}. The {@code start} node
	 *            should have been created by
	 *            {@link de.be4.classicalb.core.parser.BParser#parseFormula(String input)
	 *            parseFormula}.
	 * @param pout
	 *            the IPrologTermOutput to which the formula is printed
	 * 
	 */
	public static void printFormula(Start start, final IPrologTermOutput pout) {
		ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(new NodeFileNumbers());
		pprinter.setPrintSourcePositions(true);
		ASTProlog printer = new ASTProlog(pout, pprinter);
		start.apply(printer);
	}

	public ASTProlog(final IPrologTermOutput pout, final PositionPrinter positionPrinter) {
		this.positionPrinter = positionPrinter;
		this.pout = pout;
		if (positionPrinter != null) {
			positionPrinter.setPrologTermOutput(pout);
		}
	}

	/**
	 * This prints the functor of a prolog term together with the opening
	 * parenthesis. The first argument of the term is the identifier of the
	 * syntax tree element.
	 * 
	 * @param node
	 *            the node of the syntax tree, never <code>null</code>. It is
	 *            assumed that <code>node</code> is an abstract syntax tree
	 *            element, which class name is A* .
	 */
	private void open(final Node node) {
		pout.openTerm(simpleFormat(node));
		printPosition(node);
	}

	private void printPosition(final Node node) {
		if (positionPrinter != null) {
			positionPrinter.printPosition(node);
		} else {
			pout.printAtom("none");
		}
	}

	private void printPositionRange(Node startNode, Node endNode) {
		if (positionPrinter != null) {
			positionPrinter.printPositionRange(startNode, endNode);
		} else {
			pout.printAtom("none");
		}
	}

	private void printPositionRange(List<? extends Node> nodes, Node fallback) {
		if (!nodes.isEmpty()) {
			printPositionRange(nodes.get(0), nodes.get(nodes.size() - 1));
		} else if (fallback != null) {
			printPosition(fallback);
		} else {
			pout.printAtom("none");
		}
	}

	/**
	 * The counterpart to {@link #open(Node)}, prints the closing parenthesis of
	 * the term.
	 */
	private void close(final Node node) {
		pout.closeTerm();
	}

	private void printAtomic(Node node) {
		open(node);
		close(node);
	}

	private void printUnary(Node node, Node child) {
		open(node);
		child.apply(this);
		close(node);
	}

	private void printBinary(Node node, Node left, Node right) {
		open(node);
		left.apply(this);
		right.apply(this);
		close(node);
	}

	/**
	 * Print a list of syntax tree elements as a Prolog list (
	 * <code>[term1, ..., termN]</code>)
	 * 
	 * @param nodes
	 *            A list of nodes, never <code>null</code>. The list may be empty.
	 */
	private void printAsList(final List<? extends Node> nodes) {
		pout.openList();
		for (Node elem : nodes) {
			elem.apply(this);
		}
		pout.closeList();
	}

	/**
	 * This method combines {@link #open(Node)}, {@link #printAsList(List)} and
	 * {@link #close(Node)}.
	 * 
	 * @param node
	 *            Like in {@link #open(Node)}
	 * @param list
	 *            Like in {@link #printAsList(List)}
	 */
	private void printOCAsList(final Node node, final List<? extends Node> list) {
		open(node);
		printAsList(list);
		close(node);
	}

	@Override
	public void defaultCase(final Node node) {
		if (node instanceof Token && node.parent() != null) {
			throw new IllegalArgumentException("Encountered unexpected token '" + node.getClass().getSimpleName() + "' while generating Prolog AST - this probably indicates an issue in the translation for the parent node type '" + node.parent().getClass().getSimpleName() + "'");
		} else {
			throw new IllegalArgumentException("Translation of node type '" + node.getClass().getSimpleName() + "' to Prolog AST not implemented");
		}
	}

	/**
	 * @return Corresponging Prolog functor Name.
	 */
	private String simpleFormat(final Node node) {
		Class<? extends Node> clazz = node.getClass();
		String formatted = simpleFormats.get(clazz);
		if (formatted == null) {
			formatted = toFunctorName(clazz.getSimpleName());
			simpleFormats.put(clazz, formatted);
		}
		return formatted;
	}

	/**
	 * The translation from the names in the SableCC grammar to prolog functors
	 * must be systematic. Otherwise it will not be possible to reuse the
	 * grammar for non-Java front-ends. Please DO NOT add any magic special cases here!
	 * 
	 * @return Prolog functor name
	 */
	private String toFunctorName(final String className) {
		String camelName = formatCamel(className.substring(1)).substring(1);
		if (className.startsWith("A")) {
			if (ATOMIC_TYPE.contains(camelName)) {
				return camelName;
			}
			for (String checkend : SUM_TYPE) {
				if (camelName.endsWith(checkend)) {
					return camelName.substring(0, camelName.length() - checkend.length() - 1);
				}
			}
		}
		// There is no rule to translate the class name to a prolog functor.
		// Probably the class name is missing in table SUM_TYPE or in table
		// ATOMIC_TYPE.
		throw new AssertionError("cannot determine functor name: " + className);
	}

	/**
	 * 
	 * @param input
	 *            A string with an identifier in camel style (e.g.
	 *            ClassDoingSomeStuff), never <code>null</code>.
	 * @return The input string in lower case and seperated by _ (e.g.
	 *         class_doing_some_stuff).
	 */
	private String formatCamel(final String input) {
		StringWriter out = new StringWriter();
		char[] chars = input.toCharArray();
		for (char current : chars) {
			if (Character.isUpperCase(current)) {
				out.append('_');
				out.append(Character.toLowerCase(current));
			} else {
				out.append(current);
			}
		}
		return out.toString();
	}

	private void printIdentifier(List<TIdentifierLiteral> list) {
		pout.printAtom(Utils.getTIdentifierListAsString(list));
	}

	/**
	 * Print a {@link TIdentifierLiteral} list exactly as if it was an {@link AIdentifierExpression}.
	 * 
	 * @param identifierParts the identifier to print (a list of identifier tokens that will be joined using dots)
	 */
	private void printPositionedIdentifier(List<TIdentifierLiteral> identifierParts) {
		if (identifierParts.isEmpty()) {
			throw new IllegalArgumentException("There must be at least one token in a dotted identifier list");
		}
		pout.openTerm("identifier");
		printPositionRange(identifierParts, null);
		printIdentifier(identifierParts);
		pout.closeTerm();
	}

	/**
	 * Print a {@link TIdentifierLiteral} exactly as if it was an {@link AIdentifierExpression}.
	 * 
	 * @param identifier the identifier token to print
	 */
	private void printPositionedIdentifier(TIdentifierLiteral identifier) {
		pout.openTerm("identifier");
		printPosition(identifier);
		pout.printAtom(identifier.getText());
		pout.closeTerm();
	}

	private void printNullSafeSubstitution(final Node subst) {
		if (subst == null) {
			pout.openTerm("skip");
			pout.printAtom("none");
			pout.closeTerm();
		} else {
			subst.apply(this);
		}
	}

	@Override
	public void caseStart(Start node) {
		// Don't print the Start and EOF nodes.
		node.getPParseUnit().apply(this);
	}

	@Override
	public void caseAIdentifierExpression(final AIdentifierExpression node) {
		open(node);
		printIdentifier(node.getIdentifier());
		close(node);
	}

	@Override
	public void caseAPrimedIdentifierExpression(final APrimedIdentifierExpression node) {
		open(node);
		printIdentifier(node.getIdentifier());
		// The parser now only supports $0
		pout.printNumber(0);
		close(node);
	}

	// Parse Units

	@Override
	public void caseAGeneratedParseUnit(AGeneratedParseUnit node) {
		printUnary(node, node.getParseUnit());
	}

	@Override
	public void caseAAbstractMachineParseUnit(final AAbstractMachineParseUnit node) {
		open(node);
		node.getVariant().apply(this);
		node.getHeader().apply(this);
		printAsList(node.getMachineClauses());
		close(node);
	}

	@Override
	public void caseARefinementMachineParseUnit(final ARefinementMachineParseUnit node) {
		open(node);
		node.getHeader().apply(this);
		pout.printAtom(node.getRefMachine().getText());
		printAsList(node.getMachineClauses());
		close(node);
	}

	@Override
	public void caseAImplementationMachineParseUnit(final AImplementationMachineParseUnit node) {
		open(node);
		node.getHeader().apply(this);
		pout.printAtom(node.getRefMachine().getText());
		printAsList(node.getMachineClauses());
		close(node);
	}

	@Override
	public void caseADefinitionFileParseUnit(ADefinitionFileParseUnit node) {
		printUnary(node, node.getDefinitionsClauses());
	}

	@Override
	public void caseAUndefArgpattern(AUndefArgpattern node) {
		printAtomic(node);
	}

	@Override
	public void caseADefArgpattern(ADefArgpattern node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAMachineMachineVariant(AMachineMachineVariant node) {
		printAtomic(node);
	}

	@Override
	public void caseAModelMachineVariant(AModelMachineVariant node) {
		printAtomic(node);
	}

	@Override
	public void caseASystemMachineVariant(ASystemMachineVariant node) {
		printAtomic(node);
	}

	// machine header

	@Override
	public void caseAMachineHeader(final AMachineHeader node) {
		open(node);
		printIdentifier(node.getName());
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseAExtendedExprExpression(final AExtendedExprExpression node) {
		open(node);
		pout.printAtom(node.getIdentifier().getText());
		printAsList(node.getExpressions());
		printAsList(node.getPredicates());
		close(node);
	}

	@Override
	public void caseAExtendedPredPredicate(final AExtendedPredPredicate node) {
		open(node);
		pout.printAtom(node.getIdentifier().getText());
		printAsList(node.getExpressions());
		printAsList(node.getPredicates());
		close(node);
	}

	// machine clauses

	@Override
	public void caseADefinitionsMachineClause(final ADefinitionsMachineClause node) {
		printOCAsList(node, node.getDefinitions());
	}
	@Override
	public void caseAExpressionsMachineClause(final AExpressionsMachineClause node) {
		printOCAsList(node, node.getExpressions()); // EXPRESSIONS clause
	}
	@Override
	public void caseAPredicatesMachineClause(final APredicatesMachineClause node) {
		printOCAsList(node, node.getPredicates()); // PREDICATES clause
	}

	@Override
	public void caseASeesMachineClause(final ASeesMachineClause node) {
		printOCAsList(node, node.getMachineNames());
	}

	@Override
	public void caseAPromotesMachineClause(final APromotesMachineClause node) {
		printOCAsList(node, node.getOperationNames());
	}

	@Override
	public void caseAUsesMachineClause(final AUsesMachineClause node) {
		printOCAsList(node, node.getMachineNames());
	}

	@Override
	public void caseAIncludesMachineClause(final AIncludesMachineClause node) {
		printOCAsList(node, node.getMachineReferences());
	}

	@Override
	public void caseAExtendsMachineClause(final AExtendsMachineClause node) {
		printOCAsList(node, node.getMachineReferences());
	}

	@Override
	public void caseAImportsMachineClause(final AImportsMachineClause node) {
		printOCAsList(node, node.getMachineReferences());
	}

	@Override
	public void caseASetsMachineClause(final ASetsMachineClause node) {
		printOCAsList(node, node.getSetDefinitions());
	}

	@Override
	public void caseAVariablesMachineClause(final AVariablesMachineClause node) {
		printOCAsList(node, node.getIdentifiers());
	}

	@Override
	public void caseAConcreteVariablesMachineClause(final AConcreteVariablesMachineClause node) {
		printOCAsList(node, node.getIdentifiers());
	}

	@Override
	public void caseAAbstractConstantsMachineClause(final AAbstractConstantsMachineClause node) {
		printOCAsList(node, node.getIdentifiers());
	}

	@Override
	public void caseAConstantsMachineClause(final AConstantsMachineClause node) {
		printOCAsList(node, node.getIdentifiers());
	}

	@Override
	public void caseAPropertiesMachineClause(APropertiesMachineClause node) {
		printUnary(node, node.getPredicates());
	}

	@Override
	public void caseAConstraintsMachineClause(AConstraintsMachineClause node) {
		printUnary(node, node.getPredicates());
	}

	@Override
	public void caseAInitialisationMachineClause(AInitialisationMachineClause node) {
		printUnary(node, node.getSubstitutions());
	}

	@Override
	public void caseAInvariantMachineClause(AInvariantMachineClause node) {
		printUnary(node, node.getPredicates());
	}

	@Override
	public void caseAAssertionsMachineClause(final AAssertionsMachineClause node) {
		printOCAsList(node, node.getPredicates());
	}

	@Override
	public void caseAValuesMachineClause(final AValuesMachineClause node) {
		printOCAsList(node, node.getEntries());
	}

	@Override
	public void caseALocalOperationsMachineClause(final ALocalOperationsMachineClause node) {
		printOCAsList(node, node.getOperations());
	}

	@Override
	public void caseAOperationsMachineClause(final AOperationsMachineClause node) {
		printOCAsList(node, node.getOperations());
	}

	@Override
	public void caseADescriptionMachineClause(ADescriptionMachineClause node) {
		printBinary(node, node.getDescription(), node.getMachineClause());
	}

	@Override
	public void caseAValuesEntry(AValuesEntry node) {
		open(node);
		printIdentifier(node.getIdentifier());
		node.getValue().apply(this);
		close(node);
	}

	// machine reference

	@Override
	public void caseAMachineReferenceNoParams(final AMachineReferenceNoParams node) {
		// Keep this functor for compatibility with previous versions
		// (SEES/USES names were previously parsed as identifier expressions).
		printPositionedIdentifier(node.getMachineName());
	}

	@Override
	public void caseAMachineReference(final AMachineReference node) {
		open(node);
		printIdentifier(node.getMachineName());
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseAOperationReference(final AOperationReference node) {
		// Keep this functor for compatibility with previous versions
		// (PROMOTES names were previously parsed as identifier expressions).
		printPositionedIdentifier(node.getOperationName());
	}

	@Override
	public void caseADescriptionPragma(ADescriptionPragma node) {
		pout.openTerm("description_text");
		// If possible, print the position of the description text itself,
		// not the entire description pragma.
		printPositionRange(node.getParts(), node);
		// Print all description parts as a single atom for now, until we support parsing template parameters.
		pout.printAtom(node.getParts().stream().map(Token::getText).collect(Collectors.joining()));
		pout.closeTerm();
	}

	// definition

	@Override
	public void caseAPredicateDefinitionDefinition(final APredicateDefinitionDefinition node) {
		open(node);
		pout.printAtom(node.getName().getText());
		printAsList(node.getParameters());
		node.getRhs().apply(this);
		close(node);
	}

	@Override
	public void caseASubstitutionDefinitionDefinition(final ASubstitutionDefinitionDefinition node) {
		open(node);
		pout.printAtom(node.getName().getText());
		printAsList(node.getParameters());
		node.getRhs().apply(this);
		close(node);
	}

	@Override
	public void caseAExpressionDefinitionDefinition(final AExpressionDefinitionDefinition node) {
		open(node);
		pout.printAtom(node.getName().getText());
		printAsList(node.getParameters());
		node.getRhs().apply(this);
		close(node);
	}

	// set

	@Override
	public void caseADescriptionSet(ADescriptionSet node) {
		printBinary(node, node.getDescription(), node.getSet());
	}

	@Override
	public void caseADeferredSetSet(ADeferredSetSet node) {
		open(node);
		printIdentifier(node.getIdentifier());
		close(node);
	}

	@Override
	public void caseAEnumeratedSetSet(final AEnumeratedSetSet node) {
		open(node);
		printIdentifier(node.getIdentifier());
		printAsList(node.getElements());
		close(node);
	}

	@Override
	public void caseAEnumeratedSetViaDefSet(AEnumeratedSetViaDefSet node) {
		open(node);
		printIdentifier(node.getIdentifier());
		printIdentifier(node.getElementsDef());
		close(node);
	}

	// operation

	@Override
	public void caseAOperation(final AOperation node) {
		open(node);
		printPositionedIdentifier(node.getOpName());
		printAsList(node.getReturnValues());
		printAsList(node.getParameters());
		node.getOperationBody().apply(this);
		close(node);
	}

	@Override
	public void caseADescriptionOperation(ADescriptionOperation node) {
		printBinary(node, node.getDescription(), node.getOperation());
	}

	@Override
	public void caseARefinedOperation(final ARefinedOperation node) {
		open(node);
		printPositionedIdentifier(node.getOpName());
		printAsList(node.getReturnValues());
		printAsList(node.getParameters());
		pout.printAtom(node.getAbOpName().getText());
		node.getOperationBody().apply(this);
		close(node);
	}
	
	

	// predicate

	@Override
	public void caseADescriptionPredicate(ADescriptionPredicate node) {
		printBinary(node, node.getDescription(), node.getPredicate());
	}

	@Override
	public void caseALabelPredicate(ALabelPredicate node) {
		open(node);
		pout.printAtom(node.getName().getText());
		node.getPredicate().apply(this);
		close(node);
	}

	@Override
	public void caseASubstitutionPredicate(ASubstitutionPredicate node) {
		printBinary(node, node.getSubstitution(), node.getPredicate());
	}

	@Override
	public void caseAConjunctPredicate(final AConjunctPredicate node) {
		open(node);
		
		final Deque<PPredicate> conjunctPreds = new LinkedList<>();
		AConjunctPredicate currentNode = node;
		while (currentNode.getLeft() instanceof AConjunctPredicate) {
			conjunctPreds.addFirst(currentNode.getRight());
			currentNode = (AConjunctPredicate)currentNode.getLeft();
		}
		conjunctPreds.addFirst(currentNode.getRight());
		conjunctPreds.addFirst(currentNode.getLeft());
		
		pout.openList();
		for (final PPredicate pred : conjunctPreds) {
			pred.apply(this);
		}
		pout.closeList();
		
		close(node);
	}

	@Override
	public void caseANegationPredicate(ANegationPredicate node) {
		printUnary(node, node.getPredicate());
	}

	@Override
	public void caseADisjunctPredicate(ADisjunctPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAImplicationPredicate(AImplicationPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAEquivalencePredicate(AEquivalencePredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAForallPredicate(final AForallPredicate node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getImplication().apply(this);
		close(node);
	}

	@Override
	public void caseAExistsPredicate(final AExistsPredicate node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicate().apply(this);
		close(node);
	}

	@Override
	public void caseAEqualPredicate(AEqualPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseANotEqualPredicate(ANotEqualPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAMemberPredicate(AMemberPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseANotMemberPredicate(ANotMemberPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseASubsetPredicate(ASubsetPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseASubsetStrictPredicate(ASubsetStrictPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseANotSubsetPredicate(ANotSubsetPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseANotSubsetStrictPredicate(ANotSubsetStrictPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseALessEqualPredicate(ALessEqualPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseALessPredicate(ALessPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAGreaterEqualPredicate(AGreaterEqualPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAGreaterPredicate(AGreaterPredicate node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseATruthPredicate(ATruthPredicate node) {
		printAtomic(node);
	}

	@Override
	public void caseAFalsityPredicate(AFalsityPredicate node) {
		printAtomic(node);
	}

	@Override
	public void caseAFinitePredicate(AFinitePredicate node) {
		printUnary(node, node.getSet());
	}

	@Override
	public void caseADefinitionPredicate(final ADefinitionPredicate node) {
		open(node);
		pout.printAtom(node.getDefLiteral().getText());
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseALetPredicatePredicate(ALetPredicatePredicate node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getAssignment().apply(this);
		node.getPred().apply(this);
		close(node);
	}

	// expression

	@Override
	public void caseADescriptionExpression(ADescriptionExpression node) {
		printBinary(node, node.getDescription(), node.getExpression());
	}

	@Override
	public void caseAStringExpression(AStringExpression node) {
		open(node);
		pout.printAtom(node.getContent().getText());
		close(node);
	}

	@Override
	public void caseAMultilineTemplateExpression(AMultilineTemplateExpression node) {
		open(node);
		pout.printAtom(node.getContent().getText());
		close(node);
	}

	@Override
	public void caseABooleanFalseExpression(ABooleanFalseExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseARealExpression(ARealExpression node) {
		open(node);
		pout.printAtom(node.getLiteral().getText());
		close(node);
	}

	@Override
	public void caseAMaxIntExpression(AMaxIntExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAMinIntExpression(AMinIntExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAEmptySetExpression(AEmptySetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAIntegerSetExpression(AIntegerSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseARealSetExpression(ARealSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAFloatSetExpression(AFloatSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseANaturalSetExpression(ANaturalSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseANatural1SetExpression(ANatural1SetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseANatSetExpression(ANatSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseANat1SetExpression(ANat1SetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAIntSetExpression(AIntSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseABoolSetExpression(ABoolSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAStringSetExpression(AStringSetExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAConvertBoolExpression(AConvertBoolExpression node) {
		printUnary(node, node.getPredicate());
	}

	@Override
	public void caseAAddExpression(AAddExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAMinusExpression(AMinusExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAMinusOrSetSubtractExpression(AMinusOrSetSubtractExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAUnaryMinusExpression(AUnaryMinusExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAMultiplicationExpression(AMultiplicationExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseACartesianProductExpression(ACartesianProductExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAMultOrCartExpression(AMultOrCartExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseADivExpression(ADivExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAFlooredDivExpression(AFlooredDivExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseALetExpressionExpression(ALetExpressionExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getAssignment().apply(this);
		node.getExpr().apply(this);
		close(node);
	}

	@Override
	public void caseAIfThenElseExpression(final AIfThenElseExpression node) {
		open(node);
		node.getCondition().apply(this);
		node.getThen().apply(this);
		
		// Rewrite ELSIF clauses to nested if_then_else expressions.
		for (PExpression expr : node.getElsifs()) {
			AIfElsifExprExpression elsIf = (AIfElsifExprExpression) expr;
			pout.openTerm(simpleFormat(node));//if_then_else
			printPosition(elsIf);
			elsIf.getCondition().apply(this);
			elsIf.getThen().apply(this);
		}
		
		node.getElse().apply(this);
		
		// Close all nested if_then_else expressions that were opened for the ELSIFs.
		for (PExpression ignored : node.getElsifs()) {
			pout.closeTerm();
		}
		
		close(node);
	}

	@Override
	public void caseAModuloExpression(AModuloExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAPowerOfExpression(APowerOfExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseASuccessorExpression(ASuccessorExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAPredecessorExpression(APredecessorExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAMaxExpression(AMaxExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAMinExpression(AMinExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseACardExpression(ACardExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAConvertIntFloorExpression(AConvertIntFloorExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAConvertIntCeilingExpression(AConvertIntCeilingExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAConvertRealExpression(AConvertRealExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAGeneralSumExpression(final AGeneralSumExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		close(node);
	}

	@Override
	public void caseAGeneralProductExpression(final AGeneralProductExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		close(node);
	}

	@Override
	public void caseACoupleExpression(final ACoupleExpression node) {
		if (node.getList().size() < 2) {
			throw new IllegalArgumentException("ACoupleExpression must have at least 2 elements, but got " + node.getList().size());
		}
		printOCAsList(node, node.getList());
	}

	@Override
	public void caseAComprehensionSetExpression(final AComprehensionSetExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicates().apply(this);
		close(node);
	}

	@Override
	public void caseASymbolicComprehensionSetExpression(final ASymbolicComprehensionSetExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicates().apply(this);
		close(node);
	}

	@Override
	public void caseAEventBComprehensionSetExpression(final AEventBComprehensionSetExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getExpression().apply(this);
		node.getPredicates().apply(this);
		close(node);
	}

	@Override
	public void caseASymbolicEventBComprehensionSetExpression(final ASymbolicEventBComprehensionSetExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getExpression().apply(this);
		node.getPredicates().apply(this);
		close(node);
	}

	@Override
	public void caseAPowSubsetExpression(APowSubsetExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAPow1SubsetExpression(APow1SubsetExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAFinSubsetExpression(AFinSubsetExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAFin1SubsetExpression(AFin1SubsetExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseASetExtensionExpression(final ASetExtensionExpression node) {
		printOCAsList(node, node.getExpressions());
	}

	@Override
	public void caseAIntervalExpression(AIntervalExpression node) {
		printBinary(node, node.getLeftBorder(), node.getRightBorder());
	}

	@Override
	public void caseAUnionExpression(AUnionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAIntersectionExpression(AIntersectionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseASetSubtractionExpression(ASetSubtractionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAGeneralUnionExpression(AGeneralUnionExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAGeneralIntersectionExpression(AGeneralIntersectionExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAQuantifiedUnionExpression(final AQuantifiedUnionExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		close(node);
	}

	@Override
	public void caseASymbolicQuantifiedUnionExpression(final ASymbolicQuantifiedUnionExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		close(node);
	}

	@Override
	public void caseAQuantifiedIntersectionExpression(final AQuantifiedIntersectionExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicates().apply(this);
		node.getExpression().apply(this);
		close(node);
	}

	@Override
	public void caseARelationsExpression(ARelationsExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAIdentityExpression(AIdentityExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAEventBIdentityExpression(AEventBIdentityExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseAReverseExpression(AReverseExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAMuExpression(AMuExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAFirstProjectionExpression(AFirstProjectionExpression node) {
		printBinary(node, node.getExp1(), node.getExp2());
	}

	@Override
	public void caseAEventBFirstProjectionExpression(AEventBFirstProjectionExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAEventBFirstProjectionV2Expression(AEventBFirstProjectionV2Expression node) {
		printAtomic(node);
	}

	@Override
	public void caseASecondProjectionExpression(ASecondProjectionExpression node) {
		printBinary(node, node.getExp1(), node.getExp2());
	}

	@Override
	public void caseAEventBSecondProjectionExpression(AEventBSecondProjectionExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAEventBSecondProjectionV2Expression(AEventBSecondProjectionV2Expression node) {
		printAtomic(node);
	}

	@Override
	public void caseACompositionExpression(ACompositionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseASymbolicCompositionExpression(ASymbolicCompositionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseARingExpression(ARingExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseADirectProductExpression(ADirectProductExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAParallelProductExpression(AParallelProductExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAIterationExpression(AIterationExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAReflexiveClosureExpression(AReflexiveClosureExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAClosureExpression(AClosureExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseADomainExpression(ADomainExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseARangeExpression(ARangeExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAImageExpression(AImageExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseADomainRestrictionExpression(ADomainRestrictionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseADomainSubtractionExpression(ADomainSubtractionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseARangeRestrictionExpression(ARangeRestrictionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseARangeSubtractionExpression(ARangeSubtractionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAOverwriteExpression(AOverwriteExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAPartialFunctionExpression(APartialFunctionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseATotalFunctionExpression(ATotalFunctionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAPartialInjectionExpression(APartialInjectionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseATotalInjectionExpression(ATotalInjectionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAPartialSurjectionExpression(APartialSurjectionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseATotalSurjectionExpression(ATotalSurjectionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAPartialBijectionExpression(APartialBijectionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseATotalBijectionExpression(ATotalBijectionExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseATotalRelationExpression(ATotalRelationExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseASurjectionRelationExpression(ASurjectionRelationExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseATotalSurjectionRelationExpression(ATotalSurjectionRelationExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseALambdaExpression(final ALambdaExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicate().apply(this);
		node.getExpression().apply(this);
		close(node);
	}

	@Override
	public void caseASymbolicLambdaExpression(ASymbolicLambdaExpression node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicate().apply(this);
		node.getExpression().apply(this);
		close(node);
	}

	@Override
	public void caseATransFunctionExpression(ATransFunctionExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseATransRelationExpression(ATransRelationExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseASeqExpression(ASeqExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseASeq1Expression(ASeq1Expression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAIseqExpression(AIseqExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAIseq1Expression(AIseq1Expression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAPermExpression(APermExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAEmptySequenceExpression(AEmptySequenceExpression node) {
		printAtomic(node);
	}

	@Override
	public void caseASequenceExtensionExpression(final ASequenceExtensionExpression node) {
		printOCAsList(node, node.getExpression());
	}

	@Override
	public void caseASizeExpression(ASizeExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAFirstExpression(AFirstExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseALastExpression(ALastExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAFrontExpression(AFrontExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseATailExpression(ATailExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseARevExpression(ARevExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAConcatExpression(AConcatExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAInsertFrontExpression(AInsertFrontExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAInsertTailExpression(AInsertTailExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseARestrictFrontExpression(ARestrictFrontExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseARestrictTailExpression(ARestrictTailExpression node) {
		printBinary(node, node.getLeft(), node.getRight());
	}

	@Override
	public void caseAGeneralConcatExpression(AGeneralConcatExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAFunctionExpression(final AFunctionExpression node) {
		open(node);
		node.getIdentifier().apply(this);
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseATreeExpression(ATreeExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseABtreeExpression(ABtreeExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAConstExpression(AConstExpression node) {
		printBinary(node, node.getExpression1(), node.getExpression2());
	}

	@Override
	public void caseATopExpression(ATopExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseASonsExpression(ASonsExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAPrefixExpression(APrefixExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAPostfixExpression(APostfixExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseASizetExpression(ASizetExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAMirrorExpression(AMirrorExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseARankExpression(ARankExpression node) {
		printBinary(node, node.getExpression1(), node.getExpression2());
	}

	@Override
	public void caseAFatherExpression(AFatherExpression node) {
		printBinary(node, node.getExpression1(), node.getExpression2());
	}

	@Override
	public void caseASonExpression(ASonExpression node) {
		open(node);
		node.getExpression1().apply(this);
		node.getExpression2().apply(this);
		node.getExpression3().apply(this);
		close(node);
	}

	@Override
	public void caseASubtreeExpression(ASubtreeExpression node) {
		printBinary(node, node.getExpression1(), node.getExpression2());
	}

	@Override
	public void caseAArityExpression(AArityExpression node) {
		printBinary(node, node.getExpression1(), node.getExpression2());
	}

	@Override
	public void caseABinExpression(ABinExpression node) {
		// bin can have exactly 1 or 3 arguments.
		open(node);
		node.getExpression1().apply(this);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
			node.getExpression3().apply(this);
		}
		close(node);
	}

	@Override
	public void caseALeftExpression(ALeftExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseARightExpression(ARightExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseAInfixExpression(AInfixExpression node) {
		printUnary(node, node.getExpression());
	}

	@Override
	public void caseARecExpression(final ARecExpression node) {
		printOCAsList(node, node.getEntries());
	}

	@Override
	public void caseAStructExpression(final AStructExpression node) {
		printOCAsList(node, node.getEntries());
	}

	@Override
	public void caseARecordFieldExpression(ARecordFieldExpression node) {
		open(node);
		node.getRecord().apply(this);
		printPositionedIdentifier(node.getIdentifier());
		close(node);
	}

	@Override
	public void caseATypeofExpression(ATypeofExpression node) {
		printBinary(node, node.getExpression(), node.getType());
	}

	@Override
	public void caseAIntegerExpression(final AIntegerExpression node) {
		open(node);
		final String text = node.getLiteral().getText();
		if (text.length() <= 18) {
			pout.printNumber(Long.parseLong(text));
		} else {
			pout.printNumber(new BigInteger(text));
		}
		close(node);
	}

	@Override
	public void caseADefinitionExpression(final ADefinitionExpression node) {
		open(node);
		pout.printAtom(node.getDefLiteral().getText());
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseARecEntry(ARecEntry node) {
		open(node);
		printPositionedIdentifier(node.getIdentifier());
		node.getValue().apply(this);
		close(node);
	}

	// substitutions

	@Override
	public void caseABlockSubstitution(ABlockSubstitution node) {
		printUnary(node, node.getSubstitution());
	}

	@Override
	public void caseASkipSubstitution(ASkipSubstitution node) {
		printAtomic(node);
	}

	@Override
	public void caseAAssignSubstitution(final AAssignSubstitution node) {
		open(node);
		printAsList(node.getLhsExpression());
		printAsList(node.getRhsExpressions());
		close(node);
	}

	@Override
	public void caseAPreconditionSubstitution(APreconditionSubstitution node) {
		printBinary(node, node.getPredicate(), node.getSubstitution());
	}

	@Override
	public void caseAAssertionSubstitution(AAssertionSubstitution node) {
		printBinary(node, node.getPredicate(), node.getSubstitution());
	}

	@Override
	public void caseAWitnessThenSubstitution(AWitnessThenSubstitution node) {
		printBinary(node, node.getPredicate(), node.getSubstitution());
	}

	@Override
	public void caseAChoiceSubstitution(final AChoiceSubstitution node) {
		printOCAsList(node, node.getSubstitutions());
	}

	@Override
	public void caseAChoiceOrSubstitution(AChoiceOrSubstitution node) {
		printUnary(node, node.getSubstitution());
	}

	@Override
	public void caseAIfSubstitution(final AIfSubstitution node) {
		open(node);
		node.getCondition().apply(this);
		node.getThen().apply(this);
		printAsList(node.getElsifSubstitutions());
		printNullSafeSubstitution(node.getElse());
		close(node);
	}

	@Override
	public void caseAIfElsifSubstitution(AIfElsifSubstitution node) {
		printBinary(node, node.getCondition(), node.getThenSubstitution());
	}

	@Override
	public void caseASelectSubstitution(final ASelectSubstitution node) {
		open(node);
		node.getCondition().apply(this);
		node.getThen().apply(this);
		printAsList(node.getWhenSubstitutions());
		final Node elsenode = node.getElse();
		if (elsenode != null) {
			elsenode.apply(this);
		}
		close(node);
	}

	@Override
	public void caseASelectWhenSubstitution(ASelectWhenSubstitution node) {
		printBinary(node, node.getCondition(), node.getSubstitution());
	}

	@Override
	public void caseACaseSubstitution(final ACaseSubstitution node) {
		open(node);
		node.getExpression().apply(this);
		printAsList(node.getEitherExpr());
		node.getEitherSubst().apply(this);
		printAsList(node.getOrSubstitutions());
		printNullSafeSubstitution(node.getElse());
		close(node);
	}

	@Override
	public void caseACaseOrSubstitution(final ACaseOrSubstitution node) {
		open(node);
		printAsList(node.getExpressions());
		node.getSubstitution().apply(this);
		close(node);
	}

	@Override
	public void caseAAnySubstitution(final AAnySubstitution node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getWhere().apply(this);
		node.getThen().apply(this);
		close(node);
	}

	@Override
	public void caseALetSubstitution(final ALetSubstitution node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicate().apply(this);
		node.getSubstitution().apply(this);
		close(node);
	}

	@Override
	public void caseABecomesElementOfSubstitution(final ABecomesElementOfSubstitution node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getSet().apply(this);
		close(node);
	}

	@Override
	public void caseABecomesSuchSubstitution(final ABecomesSuchSubstitution node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getPredicate().apply(this);
		close(node);
	}

	@Override
	public void caseAVarSubstitution(final AVarSubstitution node) {
		open(node);
		printAsList(node.getIdentifiers());
		node.getSubstitution().apply(this);
		close(node);
	}

	@Override
	public void caseASequenceSubstitution(final ASequenceSubstitution node) {
		printOCAsList(node, node.getSubstitutions());
	}

	@Override
	public void caseAOperationCallSubstitution(final AOperationCallSubstitution node) {
		open(node);
		printPositionedIdentifier(node.getOperation());
		printAsList(node.getResultIdentifiers());
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseAWhileSubstitution(AWhileSubstitution node) {
		open(node);
		node.getCondition().apply(this);
		node.getDoSubst().apply(this);
		node.getInvariant().apply(this);
		node.getVariant().apply(this);
		close(node);
	}

	@Override
	public void caseAOperationCallExpression(AOperationCallExpression node) {
		open(node);
		printPositionedIdentifier(node.getOperation());
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseAParallelSubstitution(final AParallelSubstitution node) {
		printOCAsList(node, node.getSubstitutions());
	}

	@Override
	public void caseADefinitionSubstitution(final ADefinitionSubstitution node) {
		open(node);
		pout.printAtom(node.getDefLiteral().getText());
		printAsList(node.getParameters());
		close(node);
	}

	// true and false

	@Override
	public void caseABooleanTrueExpression(final ABooleanTrueExpression node) {
		pout.openTerm("boolean_true");
		printPosition(node);
		pout.closeTerm();
	}

	@Override
	public void caseAPartitionPredicate(final APartitionPredicate node) {
		open(node);
		node.getSet().apply(this);
		printAsList(node.getElements());
		close(node);
	}

	// ignore some nodes

	@Override
	public void caseAExpressionParseUnit(final AExpressionParseUnit node) {
		node.getExpression().apply(this);
	}

	@Override
	public void caseAMachineClauseParseUnit(final AMachineClauseParseUnit node) {
		node.getMachineClause().apply(this);
	}

	@Override
	public void caseAPredicateParseUnit(final APredicateParseUnit node) {
		node.getPredicate().apply(this);
	}

	@Override
	public void caseASubstitutionParseUnit(final ASubstitutionParseUnit node) {
		node.getSubstitution().apply(this);
	}

	@Override
	public void caseAEventBModelParseUnit(final AEventBModelParseUnit node) {
		open(node);
		pout.printAtom(node.getName().getText());
		printAsList(node.getModelClauses());
		close(node);
	}

	@Override
	public void caseARefinesModelClause(ARefinesModelClause node) {
		open(node);
		pout.printAtom(node.getRefines().getText());
		close(node);
	}

	@Override
	public void caseAVariablesModelClause(final AVariablesModelClause node) {
		printOCAsList(node, node.getIdentifiers());
	}

	@Override
	public void caseASeesModelClause(final ASeesModelClause node) {
		printOCAsList(node, node.getSees());
	}

	@Override
	public void caseAInvariantModelClause(final AInvariantModelClause node) {
		printOCAsList(node, node.getPredicates());
	}

	@Override
	public void caseATheoremsModelClause(final ATheoremsModelClause node) {
		printOCAsList(node, node.getPredicates());
	}

	@Override
	public void caseAVariantModelClause(AVariantModelClause node) {
		printUnary(node, node.getVariant());
	}

	@Override
	public void caseAEventsModelClause(final AEventsModelClause node) {
		printOCAsList(node, node.getEvent());
	}

	@Override
	public void caseAEvent(final AEvent node) {
		open(node);

		pout.printAtom(node.getEventName().getText());

		final PEventstatus status = node.getStatus();
		if (status != null) {
			status.apply(this);
		}

		pout.openList();
		for (TIdentifierLiteral id : node.getRefines()) {
			pout.printAtom(id.getText());
		}
		pout.closeList();

		printAsList(node.getVariables());
		printAsList(node.getGuards());
		printAsList(node.getTheorems());
		printAsList(node.getAssignments());
		printAsList(node.getWitness());

		close(node);
	}

	@Override
	public void caseADescriptionEvent(ADescriptionEvent node) {
		printBinary(node, node.getDescription(), node.getEvent());
	}

	@Override
	public void caseAOrdinaryEventstatus(AOrdinaryEventstatus node) {
		printAtomic(node);
	}

	@Override
	public void caseAAnticipatedEventstatus(AAnticipatedEventstatus node) {
		printAtomic(node);
	}

	@Override
	public void caseAConvergentEventstatus(AConvergentEventstatus node) {
		printAtomic(node);
	}

	@Override
	public void caseAWitness(final AWitness node) {
		open(node);
		printPositionedIdentifier(node.getName());
		node.getPredicate().apply(this);
		close(node);
	}

	@Override
	public void caseAEventBContextParseUnit(final AEventBContextParseUnit node) {
		open(node);
		pout.printAtom(node.getName().getText());
		printAsList(node.getContextClauses());
		close(node);
	}

	@Override
	public void caseAExtendsContextClause(final AExtendsContextClause node) {
		printOCAsList(node, node.getExtends());
	}

	@Override
	public void caseASetsContextClause(final ASetsContextClause node) {
		printOCAsList(node, node.getSet());
	}

	@Override
	public void caseAConstantsContextClause(final AConstantsContextClause node) {
		printOCAsList(node, node.getIdentifiers());
	}

	@Override
	public void caseAAbstractConstantsContextClause(final AAbstractConstantsContextClause node) {
		printOCAsList(node, node.getIdentifiers());
	}

	@Override
	public void caseAAxiomsContextClause(final AAxiomsContextClause node) {
		printOCAsList(node, node.getPredicates());
	}

	@Override
	public void caseATheoremsContextClause(final ATheoremsContextClause node) {
		printOCAsList(node, node.getPredicates());
	}

	@Override
	public void caseAOppatternParseUnit(final AOppatternParseUnit node) {
		open(node);
		printIdentifier(node.getName());
		printAsList(node.getParameters());
		close(node);
	}

	@Override
	public void caseAFreetypesMachineClause(AFreetypesMachineClause node) {
		printOCAsList(node, node.getFreetypes());
	}

	@Override
	public void caseAFreetype(AFreetype node) {
		open(node);
		pout.printAtom(node.getName().getText());
		printAsList(node.getParameters());
		printAsList(node.getConstructors());
		close(node);
	}

	@Override
	public void caseAConstructorFreetypeConstructor(AConstructorFreetypeConstructor node) {
		open(node);
		pout.printAtom(node.getName().getText());
		node.getArgument().apply(this);
		close(node);
	}

	@Override
	public void caseAElementFreetypeConstructor(AElementFreetypeConstructor node) {
		open(node);
		pout.printAtom(node.getName().getText());
		close(node);
	}

	@Override
	public void caseAFileMachineReferenceNoParams(AFileMachineReferenceNoParams node) {
		node.getReference().apply(this);
		// node.getFile().apply(this);
	}

	@Override
	public void caseAFileMachineReference(AFileMachineReference node) {
		node.getReference().apply(this);
		// node.getFile().apply(this);
	}

}
