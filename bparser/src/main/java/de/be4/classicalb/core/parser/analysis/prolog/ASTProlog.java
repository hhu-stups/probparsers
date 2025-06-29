package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.prolog.output.IPrologTermOutput;

/**
 * This class defines the output of a B machine as a prolog term.
 */
public class ASTProlog extends DepthFirstAdapter {
	// The tables SUM_TYPE and SIMPLE_NAME are used to translate the Java class
	// name to
	// the Prolog functor name.
	// These tables MUST be in sync with BParser.scc.
	// SUM_TYPE must list all sum-types in BPaser.scc.
	// The name of the sum-type is not part of the Prolog functor.

	// SIMPLE_NAME must list all AST Classes that are not part of a sum-type
	// If a class is not a token , not in ATOMIC_TYPE and not in SUM_TYPE we
	// throw an exception.
	private static final List<String> SUM_TYPE = new LinkedList<>(Arrays.asList("expression", "predicate",
			"machine_clause", "substitution", "parse_unit", "model_clause", "context_clause", "eventstatus",
			"argpattern", "set", "machine_variant", "definition", "freetype_constructor"));

	private static final List<String> ATOMIC_TYPE = new LinkedList<>(Arrays.asList(
			"description_event", // for ADescriptionEvent
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
		pprinter.setPrintSourcePositions(true, false); // TODO Any reason not to enable compact positions?
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

	@Override
	public void inStart(final Start node) {
		// intentionally left blank: don't write the start node.
	}

	@Override
	public void outStart(final Start node) {
		// intentionally left blank: don't write the start node.
	}

	/**
	 * If the node is not handled otherwise, we just open it (see
	 * {@link #open(Node)}), print the sub-nodes, and close it later in
	 * {@link #defaultOut(Node)}
	 */
	@Override
	public void defaultIn(final Node node) {
		open(node);
	}

	/**
	 * This is the counterpart to {@link #defaultIn(Node)}
	 */
	@Override
	public void defaultOut(final Node node) {
		close(node);
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

	/**
	 * @param node
	 *            Never <code>null</code>, node is assumed to be a terminal
	 *            symbol that can be printed as a simple string
	 */
	@Override
	public void defaultCase(final Node node) {
		// All non-terminal cases have default implementations in DepthFirstAdapter.
		// Their default handling happens in defaultIn/defaultOut.
		assert node instanceof Token;

		pout.printAtom(((Token) node).getText());
	}

	@Override
	public void caseEOF(final EOF node) {
		// do nothing
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
		if (className.startsWith("T")) {
			// A SableCC Token
			return camelName;
		}

		if (className.startsWith("A")) {
			if (ATOMIC_TYPE.contains(camelName))
				return camelName;
			for (String checkend : SUM_TYPE)
				if (camelName.endsWith(checkend)) {
					return camelName.substring(0, camelName.length() - checkend.length() - 1);
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

	/***************************************************************************
	 * special cases with lists
	 */

	// Parse Units
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
		node.getRefMachine().apply(this);
		printAsList(node.getMachineClauses());
		close(node);
	}

	@Override
	public void caseAImplementationMachineParseUnit(final AImplementationMachineParseUnit node) {
		open(node);
		node.getHeader().apply(this);
		node.getRefMachine().apply(this);
		printAsList(node.getMachineClauses());
		close(node);
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
		node.getName().apply(this);
		printAsList(node.getParameters());
		node.getRhs().apply(this);
		close(node);
	}

	@Override
	public void caseASubstitutionDefinitionDefinition(final ASubstitutionDefinitionDefinition node) {
		open(node);
		node.getName().apply(this);
		printAsList(node.getParameters());
		node.getRhs().apply(this);
		close(node);
	}

	@Override
	public void caseAExpressionDefinitionDefinition(final AExpressionDefinitionDefinition node) {
		open(node);
		node.getName().apply(this);
		printAsList(node.getParameters());
		node.getRhs().apply(this);
		close(node);
	}

	// set

	@Override
	public void caseAEnumeratedSetSet(final AEnumeratedSetSet node) {
		open(node);
		printIdentifier(node.getIdentifier());
		printAsList(node.getElements());
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
	public void caseARefinedOperation(final ARefinedOperation node) {
		open(node);
		printPositionedIdentifier(node.getOpName());
		printAsList(node.getReturnValues());
		printAsList(node.getParameters());
		node.getAbOpName().apply(this);
		node.getOperationBody().apply(this);
		close(node);
	}
	
	

	// predicate

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
	public void caseADefinitionPredicate(final ADefinitionPredicate node) {
		open(node);
		node.getDefLiteral().apply(this);
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
	public void caseASetExtensionExpression(final ASetExtensionExpression node) {
		printOCAsList(node, node.getExpressions());
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
	public void caseASequenceExtensionExpression(final ASequenceExtensionExpression node) {
		printOCAsList(node, node.getExpression());
	}

	@Override
	public void caseAFunctionExpression(final AFunctionExpression node) {
		open(node);
		node.getIdentifier().apply(this);
		printAsList(node.getParameters());
		close(node);
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
		node.getDefLiteral().apply(this);
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
	public void caseAAssignSubstitution(final AAssignSubstitution node) {
		open(node);
		printAsList(node.getLhsExpression());
		printAsList(node.getRhsExpressions());
		close(node);
	}

	@Override
	public void caseAChoiceSubstitution(final AChoiceSubstitution node) {
		printOCAsList(node, node.getSubstitutions());
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
		node.getDefLiteral().apply(this);
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
		node.getName().apply(this);
		printAsList(node.getModelClauses());
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
	public void caseAEventsModelClause(final AEventsModelClause node) {
		printOCAsList(node, node.getEvent());
	}

	@Override
	public void caseAEvent(final AEvent node) {
		open(node);
		node.getEventName().apply(this);
		final PEventstatus status = node.getStatus();
		if (status != null) {
			status.apply(this);
		}
		printAsList(node.getRefines());
		printAsList(node.getVariables());
		printAsList(node.getGuards());
		printAsList(node.getTheorems());
		printAsList(node.getAssignments());
		printAsList(node.getWitness());
		close(node);
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
		node.getName().apply(this);
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
