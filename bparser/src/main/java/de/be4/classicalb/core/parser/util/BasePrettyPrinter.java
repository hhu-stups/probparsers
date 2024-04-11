package de.be4.classicalb.core.parser.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.be4.classicalb.core.parser.analysis.AnalysisAdapter;
import de.be4.classicalb.core.parser.node.*;

public class BasePrettyPrinter extends AnalysisAdapter {

	private static final String DEFAULT_INDENT = "    ";
	private static final Map<Class<? extends Node>, Integer> OPERATOR_PRIORITIES;

	static {
		final Map<Class<? extends Node>, Integer> prio = new HashMap<>();
		prio.put(AParallelProductExpression.class, 20);
		prio.put(AImplicationPredicate.class, 30);
		prio.put(ADisjunctPredicate.class, 40);
		prio.put(AConjunctPredicate.class, 40);
		prio.put(AEquivalencePredicate.class, 60);
		prio.put(ARelationsExpression.class, 125);
		prio.put(APartialFunctionExpression.class, 125);
		prio.put(ATotalFunctionExpression.class, 125);
		prio.put(APartialInjectionExpression.class, 125);
		prio.put(ATotalInjectionExpression.class, 125);
		prio.put(APartialSurjectionExpression.class, 125);
		prio.put(ATotalSurjectionExpression.class, 125);
		prio.put(APartialBijectionExpression.class, 125);
		prio.put(ATotalBijectionExpression.class, 125);
		prio.put(ATotalRelationExpression.class, 125);
		prio.put(ATotalSurjectionRelationExpression.class, 125);
		prio.put(AOverwriteExpression.class, 160);
		prio.put(ARingExpression.class, 160);
		prio.put(ADirectProductExpression.class, 160);
		prio.put(AConcatExpression.class, 160);
		prio.put(ADomainRestrictionExpression.class, 160);
		prio.put(ADomainSubtractionExpression.class, 160);
		prio.put(ARangeRestrictionExpression.class, 160);
		prio.put(ARangeSubtractionExpression.class, 160);
		prio.put(AInsertFrontExpression.class, 160);
		prio.put(AInsertTailExpression.class, 160);
		prio.put(AUnionExpression.class, 160);
		prio.put(AIntersectionExpression.class, 160);
		prio.put(ARestrictFrontExpression.class, 160);
		prio.put(ARestrictTailExpression.class, 160);
		prio.put(ACoupleExpression.class, 160);
		prio.put(AIntervalExpression.class, 170);
		prio.put(AMinusOrSetSubtractExpression.class, 180);
		prio.put(AAddExpression.class, 180);
		prio.put(ASetSubtractionExpression.class, 180);
		prio.put(ACartesianProductExpression.class, 190);
		prio.put(AMultOrCartExpression.class, 190);
		prio.put(AMultiplicationExpression.class, 190);
		prio.put(ADivExpression.class, 190);
		prio.put(AModuloExpression.class, 190);
		prio.put(APowerOfExpression.class, 200); // right associative
		prio.put(AUnaryMinusExpression.class, 210);
		prio.put(AReverseExpression.class, 230);
		prio.put(AImageExpression.class, 231);
		prio.put(ARecordFieldExpression.class, 231);
		prio.put(AFunctionExpression.class, 231);
		OPERATOR_PRIORITIES = Collections.unmodifiableMap(prio);
	}

	private final Writer writer;
	private String indent;
	private IIdentifierRenaming renaming;
	private int indentLevel;
	private boolean hasWrittenSomething;

	public BasePrettyPrinter(Writer writer) {
		this.writer = writer;
		this.indent = null;
		this.renaming = IIdentifierRenaming.QUOTE_INVALID;
		this.indentLevel = 0;
		this.hasWrittenSomething = false;
	}

	public Writer getWriter() {
		return this.writer;
	}

	public boolean isUseIndentation() {
		return this.indent != null && !this.indent.isEmpty();
	}

	public void setUseIndentation(boolean useIndentation) {
		this.setIndent(useIndentation ? DEFAULT_INDENT : null);
	}

	public String getIndent() {
		return indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	public IIdentifierRenaming getRenaming() {
		return this.renaming;
	}

	public void setRenaming(IIdentifierRenaming renaming) {
		this.renaming = Objects.requireNonNull(renaming, "renaming");
	}

	public int getIndentLevel() {
		return indentLevel;
	}

	public void setIndentLevel(int indentLevel) {
		this.indentLevel = indentLevel;
	}

	private void indent() {
		this.indentLevel++;
	}

	private void dedent() {
		if (this.indentLevel > 0) {
			this.indentLevel--;
		}
	}

	private void _write(String s) {
		if (s == null || s.isEmpty()) {
			return;
		}

		try {
			this.writer.write(s);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		this.hasWrittenSomething = true;
	}

	public void flush() {
		try {
			this.writer.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void print(String s) {
		if (s == null || s.isEmpty()) {
			return;
		}

		if (this.indentLevel > 0 && !this.hasWrittenSomething) {
			this.printIndent();
		}

		this._write(s);
	}

	private void printIndent() {
		if (this.isUseIndentation()) {
			for (int i = 0; i < this.indentLevel; i++) {
				this._write(this.indent);
			}
		}
	}

	private void println() {
		this.print("\n");
		this.printIndent();
	}

	private void printlnOpt() {
		if (this.isUseIndentation()) {
			this.print("\n");
		} else {
			this.print(" ");
		}
		this.printIndent();
	}

	private void println(String s) {
		this.print(s);
		this.println();
	}

	private void printlnOpt(String s) {
		this.print(s);
		this.printlnOpt();
	}

	private void printListImpl(final Iterable<? extends Node> iterable, String separator, boolean trailing, boolean opt) {
		String[] lines = separator.split("\n", -1);
		for (final Iterator<? extends Node> it = iterable.iterator(); it.hasNext(); ) {
			final Node node = it.next();
			node.apply(this);
			if (trailing || it.hasNext()) {
				for (int i = 0; i < lines.length; i++) {
					this.print(lines[i]);
					if (i < lines.length - 1) {
						if (opt) {
							this.printlnOpt();
						} else {
							this.println();
						}
					}
				}
			}
		}
	}

	private void printList(final Iterable<? extends Node> iterable, String separator) {
		this.printListImpl(iterable, separator, false, false);
	}

	private void printListTrailing(final Iterable<? extends Node> iterable, String separator) {
		this.printListImpl(iterable, separator, true, false);
	}

	private void printListOpt(final Iterable<? extends Node> iterable, String separator) {
		this.printListImpl(iterable, separator, false, true);
	}

	private void printListOptTrailing(final Iterable<? extends Node> iterable, String separator) {
		this.printListImpl(iterable, separator, true, true);
	}

	private void printList(final Iterable<? extends Node> iterable) {
		this.printListOpt(iterable, "\n");
	}

	private void printListTrailing(final Iterable<? extends Node> iterable) {
		this.printListOptTrailing(iterable, "\n");
	}

	private void printListMultiLine(final Iterable<? extends Node> iterable) {
		this.printList(iterable, "\n");
	}

	private void printListMultiLineTrailing(final Iterable<? extends Node> iterable) {
		this.printListTrailing(iterable, "\n");
	}

	private void printDottedList(final Iterable<? extends Node> iterable) {
		this.printListOpt(iterable, ".");
	}

	private void printCommaList(final Iterable<? extends Node> iterable) {
		this.printListOpt(iterable, ",\n");
	}

	private void printCommaListSingleLine(final Iterable<? extends Node> iterable) {
		this.printListOpt(iterable, ", ");
	}

	private void printCommaListCompact(final Iterable<? extends Node> iterable) {
		this.printListOpt(iterable, ",");
	}

	private void printSemicolonList(final Iterable<? extends Node> iterable) {
		this.printListOpt(iterable, ";\n");
	}

	private void printSemicolonListMultiLine(final Iterable<? extends Node> iterable) {
		this.printList(iterable, ";\n");
	}

	private void printParameterList(final Iterable<? extends Node> iterable) {
		if (iterable.iterator().hasNext()) {
			this.print("(");
			this.printCommaListSingleLine(iterable);
			this.print(")");
		}
	}

	private void leftParAssoc(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		// we do not insert parentheses when priority is the same
		if (priorityNode != null && priorityRight != null && priorityRight < priorityNode) {
			this.print("(");
		}
	}

	private void rightParAssoc(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight < priorityNode) {
			this.print(")");
		}
	}

	private void leftPar(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight <= priorityNode) {
			this.print("(");
		}
	}

	private void rightPar(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight <= priorityNode) {
			this.print(")");
		}
	}

	private void applyLeftAssociative(final Node left, final Node node, final Node right, final String operatorStr) {
		String[] lines = operatorStr.split("\n", -1);

		this.leftParAssoc(node, left);
		left.apply(this);
		this.rightParAssoc(node, left);

		for (int i = 0; i < lines.length; i++) {
			this.print(lines[i]);
			if (i < lines.length - 1) {
				this.printlnOpt();
			}
		}

		this.leftPar(node, right);
		right.apply(this);
		this.rightPar(node, right);
	}

	private void applyRightAssociative(final Node left, final Node node, final Node right, final String operatorStr) {
		String[] lines = operatorStr.split("\n", -1);

		this.leftPar(node, left);
		left.apply(this);
		this.rightPar(node, left);

		for (int i = 0; i < lines.length; i++) {
			this.print(lines[i]);
			if (i < lines.length - 1) {
				this.printlnOpt();
			}
		}

		this.leftParAssoc(node, right);
		right.apply(this);
		this.rightParAssoc(node, right);
	}

	@Override
	public void caseStart(final Start node) {
		node.getPParseUnit().apply(this);
	}

	@Override
	public void caseAGeneratedParseUnit(AGeneratedParseUnit node) {
		println("/*@generated*/");
		node.getParseUnit().apply(this);
	}

	@Override
	public void caseAPackageParseUnit(APackageParseUnit node) {
		print("/*@package ");
		node.getPackage().apply(this);
		println(" */");
		printListMultiLineTrailing(node.getImports());
		node.getParseUnit().apply(this);
	}

	@Override
	public void caseAAbstractMachineParseUnit(AAbstractMachineParseUnit node) {
		node.getVariant().apply(this);
		print(" ");
		node.getHeader().apply(this);
		if (!node.getMachineClauses().isEmpty()) {
			indent();
			println();
			printListMultiLine(node.getMachineClauses());
			dedent();
		}
		println();
		print("END");
	}

	@Override
	public void caseARefinementMachineParseUnit(ARefinementMachineParseUnit node) {
		print("REFINEMENT ");
		node.getHeader().apply(this);
		println();
		print("REFINES ");
		node.getRefMachine().apply(this);
		if (!node.getMachineClauses().isEmpty()) {
			indent();
			println();
			printListMultiLine(node.getMachineClauses());
			dedent();
		}
		println();
		print("END");
	}

	@Override
	public void caseAImplementationMachineParseUnit(AImplementationMachineParseUnit node) {
		print("IMPLEMENTATION ");
		node.getHeader().apply(this);
		println();
		print("REFINES ");
		node.getRefMachine().apply(this);
		if (!node.getMachineClauses().isEmpty()) {
			indent();
			println();
			printListMultiLine(node.getMachineClauses());
			dedent();
		}
		println();
		print("END");
	}

	@Override
	public void caseADefinitionFileParseUnit(final ADefinitionFileParseUnit node) {
		node.getDefinitionsClauses().apply(this);
	}

	@Override
	public void caseAPredicateParseUnit(final APredicateParseUnit node) {
		node.getPredicate().apply(this);
	}

	@Override
	public void caseAExpressionParseUnit(final AExpressionParseUnit node) {
		node.getExpression().apply(this);
	}

	@Override
	public void caseASubstitutionParseUnit(final ASubstitutionParseUnit node) {
		node.getSubstitution().apply(this);
	}

	@Override
	public void caseAMachineClauseParseUnit(AMachineClauseParseUnit node) {
		node.getMachineClause().apply(this);
	}

	@Override
	public void caseAOppatternParseUnit(AOppatternParseUnit node) {
		printDottedList(node.getName());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAImportPackage(AImportPackage node) {
		print("/*@import-package ");
		node.getPackage().apply(this);
		print(" */");
	}

	@Override
	public void caseAUndefArgpattern(AUndefArgpattern node) {
		print("_");
	}

	@Override
	public void caseADefArgpattern(ADefArgpattern node) {
		node.getExpression().apply(this);
	}

	@Override
	public void caseAMachineMachineVariant(AMachineMachineVariant node) {
		print("MACHINE");
	}

	@Override
	public void caseAModelMachineVariant(AModelMachineVariant node) {
		print("MODEL");
	}

	@Override
	public void caseASystemMachineVariant(ASystemMachineVariant node) {
		print("SYSTEM");
	}

	@Override
	public void caseAMachineHeader(AMachineHeader node) {
		printDottedList(node.getName());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseADefinitionsMachineClause(ADefinitionsMachineClause node) {
		indent();
		println("DEFINITIONS");
		printSemicolonListMultiLine(node.getDefinitions());
		dedent();
	}

	@Override
	public void caseASeesMachineClause(ASeesMachineClause node) {
		print("SEES ");
		printCommaListSingleLine(node.getMachineNames());
	}

	@Override
	public void caseAPromotesMachineClause(APromotesMachineClause node) {
		print("PROMOTES ");
		printCommaListSingleLine(node.getOperationNames());
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node) {
		print("USES ");
		printCommaListSingleLine(node.getMachineNames());
	}

	@Override
	public void caseAIncludesMachineClause(AIncludesMachineClause node) {
		print("INCLUDES ");
		printCommaListSingleLine(node.getMachineReferences());
	}

	@Override
	public void caseAExtendsMachineClause(AExtendsMachineClause node) {
		print("EXTENDS ");
		printCommaListSingleLine(node.getMachineReferences());
	}

	@Override
	public void caseAImportsMachineClause(AImportsMachineClause node) {
		print("IMPORTS ");
		printCommaListSingleLine(node.getMachineReferences());
	}

	@Override
	public void caseASetsMachineClause(ASetsMachineClause node) {
		indent();
		printlnOpt("SETS");
		printSemicolonList(node.getSetDefinitions());
		dedent();
	}

	@Override
	public void caseAFreetypesMachineClause(AFreetypesMachineClause node) {
		indent();
		printlnOpt("FREETYPES");
		printSemicolonList(node.getFreetypes());
		dedent();
	}

	@Override
	public void caseAVariablesMachineClause(AVariablesMachineClause node) {
		indent();
		printlnOpt("VARIABLES");
		printCommaList(node.getIdentifiers());
		dedent();
	}

	@Override
	public void caseAConcreteVariablesMachineClause(AConcreteVariablesMachineClause node) {
		indent();
		printlnOpt("CONCRETE_VARIABLES");
		printCommaList(node.getIdentifiers());
		dedent();
	}

	@Override
	public void caseAAbstractConstantsMachineClause(AAbstractConstantsMachineClause node) {
		indent();
		printlnOpt("ABSTRACT_CONSTANTS");
		printCommaList(node.getIdentifiers());
		dedent();
	}

	@Override
	public void caseAConstantsMachineClause(AConstantsMachineClause node) {
		indent();
		printlnOpt("CONSTANTS");
		printCommaList(node.getIdentifiers());
		dedent();
	}

	@Override
	public void caseAPropertiesMachineClause(APropertiesMachineClause node) {
		indent();
		printlnOpt("PROPERTIES");
		node.getPredicates().apply(this);
		dedent();
	}

	@Override
	public void caseAConstraintsMachineClause(AConstraintsMachineClause node) {
		indent();
		printlnOpt("CONSTRAINTS");
		node.getPredicates().apply(this);
		dedent();
	}

	@Override
	public void caseAInitialisationMachineClause(AInitialisationMachineClause node) {
		indent();
		printlnOpt("INITIALISATION");
		node.getSubstitutions().apply(this);
		dedent();
	}

	@Override
	public void caseAInvariantMachineClause(AInvariantMachineClause node) {
		indent();
		printlnOpt("INVARIANT");
		node.getPredicates().apply(this);
		dedent();
	}

	@Override
	public void caseAAssertionsMachineClause(AAssertionsMachineClause node) {
		indent();
		printlnOpt("ASSERTIONS");
		printSemicolonList(node.getPredicates());
		dedent();
	}

	@Override
	public void caseAValuesMachineClause(AValuesMachineClause node) {
		indent();
		printlnOpt("VALUES");
		printSemicolonList(node.getEntries());
		dedent();
	}

	@Override
	public void caseALocalOperationsMachineClause(ALocalOperationsMachineClause node) {
		indent();
		println("LOCAL_OPERATIONS");
		printSemicolonListMultiLine(node.getOperations());
		dedent();
	}

	@Override
	public void caseAOperationsMachineClause(AOperationsMachineClause node) {
		indent();
		println("OPERATIONS");
		printSemicolonListMultiLine(node.getOperations());
		dedent();
	}

	@Override
	public void caseAReferencesMachineClause(AReferencesMachineClause node) {
		print("REFERENCES ");
		printCommaListSingleLine(node.getMachineReferences());
	}

	@Override
	public void caseAExpressionsMachineClause(AExpressionsMachineClause node) {
		indent();
		printlnOpt("EXPRESSIONS");
		printSemicolonList(node.getExpressions());
		dedent();
	}

	@Override
	public void caseAPredicatesMachineClause(APredicatesMachineClause node) {
		indent();
		printlnOpt("PREDICATES");
		printSemicolonList(node.getPredicates());
		dedent();
	}

	@Override
	public void caseAMachineReference(final AMachineReference node) {
		printDottedList(node.getMachineName());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAFileMachineReference(AFileMachineReference node) {
		node.getReference().apply(this);
		print(" /*@file ");
		node.getFile().apply(this);
		print(" */");
	}

	@Override
	public void caseAMachineReferenceNoParams(AMachineReferenceNoParams node) {
		printDottedList(node.getMachineName());
	}

	@Override
	public void caseAFileMachineReferenceNoParams(AFileMachineReferenceNoParams node) {
		node.getReference().apply(this);
		print(" /*@file ");
		node.getFile().apply(this);
		print(" */");
	}

	@Override
	public void caseAOperationReference(AOperationReference node) {
		printDottedList(node.getOperationName());
	}

	@Override
	public void caseAExpressionDefinition(AExpressionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		print(" == ");
		indent();
		node.getRhs().apply(this);
		dedent();
	}

	@Override
	public void caseAPredicateDefinition(APredicateDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		print(" == ");
		indent();
		node.getRhs().apply(this);
		dedent();
	}

	@Override
	public void caseAPredicateDefinitionDefinition(APredicateDefinitionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		print(" == ");
		indent();
		node.getRhs().apply(this);
		dedent();
	}

	@Override
	public void caseASubstitutionDefinitionDefinition(ASubstitutionDefinitionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		print(" == ");
		indent();
		node.getRhs().apply(this);
		dedent();
	}

	@Override
	public void caseAExpressionDefinitionDefinition(AExpressionDefinitionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		print(" == ");
		indent();
		node.getRhs().apply(this);
		dedent();
	}

	@Override
	public void caseAFileDefinitionDefinition(AFileDefinitionDefinition node) {
		print("\"");
		print(Utils.escapeStringContents(node.getFilename().getText()));
		print("\"");
	}

	@Override
	public void caseADescriptionSet(ADescriptionSet node) {
		node.getSet().apply(this);
		print(" /*@desc ");
		print(node.getPragmaFreeText().getText());
		print(" */");
	}

	@Override
	public void caseADeferredSetSet(final ADeferredSetSet node) {
		printDottedList(node.getIdentifier());
	}

	@Override
	public void caseAEnumeratedSetSet(final AEnumeratedSetSet node) {
		printDottedList(node.getIdentifier());
		print(" = {");
		printCommaListSingleLine(node.getElements());
		print("}");
	}

	@Override
	public void caseAEnumeratedSetViaDefSet(AEnumeratedSetViaDefSet node) {
		printDottedList(node.getIdentifier());
		print(" = ");
		printDottedList(node.getElementsDef());
	}

	@Override
	public void caseAFreetype(AFreetype node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		print(" = ");
		printCommaListSingleLine(node.getConstructors());
	}

	@Override
	public void caseAConstructorFreetypeConstructor(AConstructorFreetypeConstructor node) {
		node.getName().apply(this);
		printParameterList(Collections.singletonList(node.getArgument()));
	}

	@Override
	public void caseAElementFreetypeConstructor(AElementFreetypeConstructor node) {
		node.getName().apply(this);
	}

	@Override
	public void caseAValuesEntry(AValuesEntry node) {
		printDottedList(node.getIdentifier());
		print(" = ");
		node.getValue().apply(this);
	}

	@Override
	public void caseAOperation(AOperation node) {
		if (!node.getReturnValues().isEmpty()) {
			printCommaListCompact(node.getReturnValues());
			print(" <-- ");
		}
		printDottedList(node.getOpName());
		printParameterList(node.getParameters());
		indent();
		printlnOpt(" =");
		node.getOperationBody().apply(this);
		dedent();
	}

	@Override
	public void caseARefinedOperation(ARefinedOperation node) {
		if (!node.getReturnValues().isEmpty()) {
			printCommaListCompact(node.getReturnValues());
			print(" <-- ");
		}
		printDottedList(node.getOpName());
		printParameterList(node.getParameters());
		print(" ");
		node.getRefKw().apply(this);
		print(" ");
		node.getAbOpName().apply(this);
		indent();
		printlnOpt(" =");
		node.getOperationBody().apply(this);
		dedent();
	}

	@Override
	public void caseARuleOperation(ARuleOperation node) {
		print("RULE ");
		node.getRuleName().apply(this);
		indent();
		printlnOpt();
		printListTrailing(node.getAttributes());
		indent();
		printlnOpt("BODY");
		node.getRuleBody().apply(this);
		dedent();
		dedent();
		print("END");
	}

	@Override
	public void caseAComputationOperation(AComputationOperation node) {
		print("COMPUTATION ");
		node.getName().apply(this);
		indent();
		printlnOpt();
		printListTrailing(node.getAttributes());
		indent();
		printlnOpt("BODY");
		node.getBody().apply(this);
		dedent();
		dedent();
		print("END");
	}

	@Override
	public void caseAFunctionOperation(AFunctionOperation node) {
		print("FUNCTION ");
		printCommaListCompact(node.getReturnValues());
		print(" <-- ");
		node.getName().apply(this);
		printParameterList(node.getParameters());
		indent();
		printlnOpt();
		printListTrailing(node.getAttributes());
		indent();
		printlnOpt("BODY");
		node.getBody().apply(this);
		dedent();
		dedent();
		print("END");
	}

	@Override
	public void caseAOperationAttribute(AOperationAttribute node) {
		node.getName().apply(this);
		print(" ");
		printCommaListSingleLine(node.getArguments());
	}

	@Override
	public void caseAPredicateAttributeOperationAttribute(APredicateAttributeOperationAttribute node) {
		node.getName().apply(this);
		print(" ");
		node.getPredicate().apply(this);
	}

	@Override
	public void caseADescriptionPredicate(ADescriptionPredicate node) {
		node.getPredicate().apply(this);
		print(" /*@desc ");
		print(node.getContent().getText());
		print(" */");
	}

	@Override
	public void caseALabelPredicate(ALabelPredicate node) {
		print("/*@label ");
		node.getName().apply(this);
		print(" */ ");
		node.getPredicate().apply(this);
	}

	@Override
	public void caseASubstitutionPredicate(ASubstitutionPredicate node) {
		print("[");
		node.getSubstitution().apply(this);
		print("] ");
		node.getPredicate().apply(this);
	}

	@Override
	public void caseAConjunctPredicate(final AConjunctPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " &\n");
	}

	@Override
	public void caseANegationPredicate(final ANegationPredicate node) {
		print("not");
		printParameterList(Collections.singletonList(node.getPredicate()));
	}

	@Override
	public void caseADisjunctPredicate(final ADisjunctPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " or\n");
	}

	@Override
	public void caseAImplicationPredicate(final AImplicationPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " => ");
	}

	@Override
	public void caseAEquivalencePredicate(final AEquivalencePredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " <=> ");
	}

	@Override
	public void caseAForallPredicate(final AForallPredicate node) {
		print("!(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getImplication().apply(this);
		print(")");
	}

	@Override
	public void caseAExistsPredicate(final AExistsPredicate node) {
		print("#(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicate().apply(this);
		print(")");
	}

	@Override
	public void caseAEqualPredicate(final AEqualPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "=");
	}

	@Override
	public void caseANotEqualPredicate(final ANotEqualPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/=");
	}

	@Override
	public void caseAMemberPredicate(final AMemberPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ":");
	}

	@Override
	public void caseANotMemberPredicate(final ANotMemberPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/:");
	}

	@Override
	public void caseASubsetPredicate(final ASubsetPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<:");
	}

	@Override
	public void caseASubsetStrictPredicate(final ASubsetStrictPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<<:");
	}

	@Override
	public void caseANotSubsetPredicate(final ANotSubsetPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/<:");
	}

	@Override
	public void caseANotSubsetStrictPredicate(final ANotSubsetStrictPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/<<:");
	}

	@Override
	public void caseALessEqualPredicate(final ALessEqualPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<=");
	}

	@Override
	public void caseALessPredicate(final ALessPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<");
	}

	@Override
	public void caseAGreaterEqualPredicate(final AGreaterEqualPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ">=");
	}

	@Override
	public void caseAGreaterPredicate(final AGreaterPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ">");
	}

	@Override
	public void caseATruthPredicate(ATruthPredicate node) {
		print("btrue");
	}

	@Override
	public void caseAFalsityPredicate(AFalsityPredicate node) {
		print("bfalse");
	}

	@Override
	public void caseAFinitePredicate(AFinitePredicate node) {
		print("@finite");
		printParameterList(Collections.singletonList(node.getSet()));
	}

	@Override
	public void caseAPartitionPredicate(APartitionPredicate node) {
		print("@partition");
		printParameterList(Stream.concat(Stream.of(node.getSet()), node.getElements().stream()).collect(Collectors.toList()));
	}

	@Override
	public void caseADefinitionPredicate(final ADefinitionPredicate node) {
		node.getDefLiteral().apply(this);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseALetPredicatePredicate(final ALetPredicatePredicate node) {
		print("LET ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt(" BE");
		node.getAssignment().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("IN");
		node.getPred().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAIfPredicatePredicate(AIfPredicatePredicate node) {
		print("IF ");
		indent();
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getThen().apply(this);
		dedent();
		printlnOpt();
		printListTrailing(node.getElsifs());
		indent();
		printlnOpt("ELSE");
		node.getElse().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAIfElsifPredicatePredicate(AIfElsifPredicatePredicate node) {
		print("ELSIF ");
		indent();
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getThen().apply(this);
		dedent();
	}

	@Override
	public void caseAOperatorPredicate(AOperatorPredicate node) {
		node.getName().apply(this);
		printParameterList(node.getIdentifiers());
	}

	@Override
	public void caseADescriptionExpression(ADescriptionExpression node) {
		node.getExpression().apply(this);
		print(" /*@desc ");
		print(node.getContent().getText());
		print(" */");
	}

	@Override
	public void caseAIdentifierExpression(final AIdentifierExpression node) {
		printDottedList(node.getIdentifier());
	}

	@Override
	public void caseAPrimedIdentifierExpression(APrimedIdentifierExpression node) {
		printDottedList(node.getIdentifier());
		print("$0");
	}

	@Override
	public void caseAStringExpression(final AStringExpression node) {
		print("\"");
		print(Utils.escapeStringContents(node.getContent().getText()));
		print("\"");
	}

	@Override
	public void caseAMultilineStringExpression(AMultilineStringExpression node) {
		print("'''");
		// we could do the same as for the AStringExpression,
		// but it looks nicer when multi-line strings are actually multi-line
		String text = Arrays.stream(node.getContent().getText().split("\n")).map(Utils::escapeStringContents).collect(Collectors.joining("\n"));
		print(text);
		print("'''");
	}

	@Override
	public void caseABooleanTrueExpression(final ABooleanTrueExpression node) {
		print("TRUE");
	}

	@Override
	public void caseABooleanFalseExpression(final ABooleanFalseExpression node) {
		print("FALSE");
	}

	@Override
	public void caseAIntegerExpression(final AIntegerExpression node) {
		print(node.getLiteral().getText());
	}

	@Override
	public void caseARealExpression(ARealExpression node) {
		print(node.getLiteral().getText());
	}

	@Override
	public void caseAHexIntegerExpression(AHexIntegerExpression node) {
		print(node.getLiteral().getText());
	}

	@Override
	public void caseAMaxIntExpression(final AMaxIntExpression node) {
		print("MAXINT");
	}

	@Override
	public void caseAMinIntExpression(final AMinIntExpression node) {
		print("MININT");
	}

	@Override
	public void caseAEmptySetExpression(final AEmptySetExpression node) {
		print("{}");
	}

	@Override
	public void caseAIntegerSetExpression(final AIntegerSetExpression node) {
		print("INTEGER");
	}

	@Override
	public void caseARealSetExpression(ARealSetExpression node) {
		print("REAL");
	}

	@Override
	public void caseAFloatSetExpression(AFloatSetExpression node) {
		print("FLOAT");
	}

	@Override
	public void caseANaturalSetExpression(final ANaturalSetExpression node) {
		print("NATURAL");
	}

	@Override
	public void caseANatural1SetExpression(final ANatural1SetExpression node) {
		print("NATURAL1");
	}

	@Override
	public void caseANatSetExpression(final ANatSetExpression node) {
		print("NAT");
	}

	@Override
	public void caseANat1SetExpression(final ANat1SetExpression node) {
		print("NAT1");
	}

	@Override
	public void caseAIntSetExpression(final AIntSetExpression node) {
		print("INT");
	}

	@Override
	public void caseABoolSetExpression(final ABoolSetExpression node) {
		print("BOOL");
	}

	@Override
	public void caseAStringSetExpression(final AStringSetExpression node) {
		print("STRING");
	}

	@Override
	public void caseAConvertBoolExpression(final AConvertBoolExpression node) {
		print("bool");
		printParameterList(Collections.singletonList(node.getPredicate()));
	}

	@Override
	public void caseAAddExpression(final AAddExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "+");
	}

	@Override
	public void caseAMinusOrSetSubtractExpression(final AMinusOrSetSubtractExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "-");
	}

	@Override
	public void caseAUnaryMinusExpression(final AUnaryMinusExpression node) {
		print("-");
		leftParAssoc(node, node.getExpression());
		node.getExpression().apply(this);
		rightParAssoc(node, node.getExpression());
	}

	@Override
	public void caseACartesianProductExpression(ACartesianProductExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "×");
	}

	@Override
	public void caseAMultOrCartExpression(final AMultOrCartExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "*");
	}

	@Override
	public void caseADivExpression(final ADivExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/");
	}

	@Override
	public void caseAFlooredDivExpression(AFlooredDivExpression node) {
		// Floored division doesn't exist in B syntax -
		// it's only produced by translation from TLA+ or Z.
		// ProB's LibraryMath.def provides an external function FDIV that implements floored division.
		print("FDIV");
		printParameterList(Arrays.asList(node.getLeft(), node.getRight()));
	}

	@Override
	public void caseAIfThenElseExpression(AIfThenElseExpression node) {
		print("IF ");
		indent();
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getThen().apply(this);
		dedent();
		printlnOpt();
		printListTrailing(node.getElsifs());
		indent();
		printlnOpt("ELSE");
		node.getElse().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAIfElsifExprExpression(AIfElsifExprExpression node) {
		print("ELSIF ");
		indent();
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getThen().apply(this);
		dedent();
	}

	@Override
	public void caseALetExpressionExpression(final ALetExpressionExpression node) {
		print("LET ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt(" BE");
		node.getAssignment().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("IN");
		node.getExpr().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAModuloExpression(final AModuloExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " mod ");
	}

	@Override
	public void caseAPowerOfExpression(final APowerOfExpression node) {
		applyRightAssociative(node.getLeft(), node, node.getRight(), "**");
	}

	@Override
	public void caseASuccessorExpression(final ASuccessorExpression node) {
		print("succ");
	}

	@Override
	public void caseAPredecessorExpression(final APredecessorExpression node) {
		print("pred");
	}

	@Override
	public void caseAMaxExpression(final AMaxExpression node) {
		print("max");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAMinExpression(final AMinExpression node) {
		print("min");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseACardExpression(final ACardExpression node) {
		print("card");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAConvertIntFloorExpression(AConvertIntFloorExpression node) {
		print("floor");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAConvertIntCeilingExpression(AConvertIntCeilingExpression node) {
		print("ceiling");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAConvertRealExpression(AConvertRealExpression node) {
		print("real");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAGeneralSumExpression(final AGeneralSumExpression node) {
		print("SIGMA(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicates().apply(this);
		print("|");
		node.getExpression().apply(this);
		print(")");
	}

	@Override
	public void caseAGeneralProductExpression(final AGeneralProductExpression node) {
		print("PI(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicates().apply(this);
		print("|");
		node.getExpression().apply(this);
		print(")");
	}

	@Override
	public void caseACoupleExpression(final ACoupleExpression node) {
		printParameterList(node.getList());
	}

	@Override
	public void caseAComprehensionSetExpression(final AComprehensionSetExpression node) {
		print("{");
		printCommaListCompact(node.getIdentifiers());
		print("|");
		node.getPredicates().apply(this);
		print("}");
	}

	@Override
	public void caseASymbolicComprehensionSetExpression(ASymbolicComprehensionSetExpression node) {
		print("/*@symbolic*/ ");
		print("{");
		printCommaListCompact(node.getIdentifiers());
		print("|");
		node.getPredicates().apply(this);
		print("}");
	}

	@Override
	public void caseAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node) {
		print("{");
		printCommaListCompact(node.getIdentifiers());
		print("·"); // Currently has to be a non-ASCII dot (e. g. U+00B7 MIDDLE DOT) for our parser to recognize it.
		node.getPredicates().apply(this);
		print("|");
		node.getExpression().apply(this);
		print("}");
	}

	@Override
	public void caseAPowSubsetExpression(final APowSubsetExpression node) {
		print("POW");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAPow1SubsetExpression(final APow1SubsetExpression node) {
		print("POW1");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAFinSubsetExpression(final AFinSubsetExpression node) {
		print("FIN");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAFin1SubsetExpression(final AFin1SubsetExpression node) {
		print("FIN1");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseASetExtensionExpression(final ASetExtensionExpression node) {
		print("{");
		printCommaListSingleLine(node.getExpressions());
		print("}");
	}

	@Override
	public void caseAIntervalExpression(final AIntervalExpression node) {
		applyLeftAssociative(node.getLeftBorder(), node, node.getRightBorder(), "..");
	}

	@Override
	public void caseAUnionExpression(final AUnionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "\\/");
	}

	@Override
	public void caseAIntersectionExpression(final AIntersectionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/\\");
	}

	@Override
	public void caseASetSubtractionExpression(final ASetSubtractionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "\\");
	}

	@Override
	public void caseAGeneralUnionExpression(final AGeneralUnionExpression node) {
		print("union");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAGeneralIntersectionExpression(final AGeneralIntersectionExpression node) {
		print("inter");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAQuantifiedUnionExpression(final AQuantifiedUnionExpression node) {
		print("UNION(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicates().apply(this);
		print("|");
		node.getExpression().apply(this);
		print(")");
	}

	@Override
	public void caseASymbolicQuantifiedUnionExpression(ASymbolicQuantifiedUnionExpression node) {
		print("/*@symbolic*/ ");
		print("UNION(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicates().apply(this);
		print("|");
		node.getExpression().apply(this);
		print(")");
	}

	@Override
	public void caseAQuantifiedIntersectionExpression(final AQuantifiedIntersectionExpression node) {
		print("INTER(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicates().apply(this);
		print("|");
		node.getExpression().apply(this);
		print(")");
	}

	@Override
	public void caseARelationsExpression(final ARelationsExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<->");
	}

	@Override
	public void caseAIdentityExpression(final AIdentityExpression node) {
		print("id");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAReverseExpression(final AReverseExpression node) {
		leftPar(node, node.getExpression());
		node.getExpression().apply(this);
		rightPar(node, node.getExpression());
		print("~");
	}

	@Override
	public void caseAFirstProjectionExpression(final AFirstProjectionExpression node) {
		print("prj1");
		printParameterList(Arrays.asList(node.getExp1(), node.getExp2()));
	}

	@Override
	public void caseAEventBFirstProjectionExpression(AEventBFirstProjectionExpression node) {
		print("prj1");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAEventBFirstProjectionV2Expression(AEventBFirstProjectionV2Expression node) {
		print("@prj1");
	}

	@Override
	public void caseASecondProjectionExpression(final ASecondProjectionExpression node) {
		print("prj2");
		printParameterList(Arrays.asList(node.getExp1(), node.getExp2()));
	}

	@Override
	public void caseAEventBSecondProjectionExpression(AEventBSecondProjectionExpression node) {
		print("prj2");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAEventBSecondProjectionV2Expression(AEventBSecondProjectionV2Expression node) {
		print("@prj2");
	}

	@Override
	public void caseACompositionExpression(final ACompositionExpression node) {
		print("(");
		node.getLeft().apply(this);
		print(";");
		node.getRight().apply(this);
		print(")");
	}

	@Override
	public void caseASymbolicCompositionExpression(ASymbolicCompositionExpression node) {
		node.getLeft().apply(this);
		print(" /*@symbolic*/ ");
		print(";");
		node.getRight().apply(this);
	}

	@Override
	public void caseARingExpression(ARingExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "∘");
	}

	@Override
	public void caseADirectProductExpression(final ADirectProductExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "><");
	}

	@Override
	public void caseAParallelProductExpression(final AParallelProductExpression node) {
		print("(");
		node.getLeft().apply(this);
		print("||");
		node.getRight().apply(this);
		print(")");
	}

	@Override
	public void caseAIterationExpression(final AIterationExpression node) {
		print("iterate");
		printParameterList(Arrays.asList(node.getLeft(), node.getRight()));
	}

	@Override
	public void caseAReflexiveClosureExpression(final AReflexiveClosureExpression node) {
		print("closure");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAClosureExpression(final AClosureExpression node) {
		print("closure1");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseADomainExpression(final ADomainExpression node) {
		print("dom");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseARangeExpression(final ARangeExpression node) {
		print("ran");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAImageExpression(final AImageExpression node) {
		leftParAssoc(node, node.getLeft());
		node.getLeft().apply(this);
		rightParAssoc(node, node.getLeft());
		print("[");
		node.getRight().apply(this);
		print("]");
	}

	@Override
	public void caseADomainRestrictionExpression(final ADomainRestrictionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<|");
	}

	@Override
	public void caseADomainSubtractionExpression(final ADomainSubtractionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<<|");
	}

	@Override
	public void caseARangeRestrictionExpression(final ARangeRestrictionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "|>");
	}

	@Override
	public void caseARangeSubtractionExpression(final ARangeSubtractionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "|>>");
	}

	@Override
	public void caseAOverwriteExpression(final AOverwriteExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<+");
	}

	@Override
	public void caseAPartialFunctionExpression(final APartialFunctionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "+->");
	}

	@Override
	public void caseATotalFunctionExpression(final ATotalFunctionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "-->");
	}

	@Override
	public void caseAPartialInjectionExpression(final APartialInjectionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ">+>");
	}

	@Override
	public void caseATotalInjectionExpression(final ATotalInjectionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ">->");
	}

	@Override
	public void caseAPartialSurjectionExpression(final APartialSurjectionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "+->>");
	}

	@Override
	public void caseATotalSurjectionExpression(final ATotalSurjectionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "-->>");
	}

	@Override
	public void caseAPartialBijectionExpression(final APartialBijectionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ">+>>");
	}

	@Override
	public void caseATotalBijectionExpression(final ATotalBijectionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ">->>");
	}

	@Override
	public void caseATotalRelationExpression(final ATotalRelationExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<<->");
	}

	@Override
	public void caseASurjectionRelationExpression(final ASurjectionRelationExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<->>");
	}

	@Override
	public void caseATotalSurjectionRelationExpression(final ATotalSurjectionRelationExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<<->>");
	}

	@Override
	public void caseALambdaExpression(final ALambdaExpression node) {
		print("%(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicate().apply(this);
		print("|");
		node.getExpression().apply(this);
		print(")");
	}

	@Override
	public void caseASymbolicLambdaExpression(ASymbolicLambdaExpression node) {
		print("/*@symbolic*/ %(");
		printCommaListCompact(node.getIdentifiers());
		print(").(");
		node.getPredicate().apply(this);
		print("|");
		node.getExpression().apply(this);
		print(")");
	}

	@Override
	public void caseATransFunctionExpression(final ATransFunctionExpression node) {
		print("fnc");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseATransRelationExpression(final ATransRelationExpression node) {
		print("rel");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseASeqExpression(final ASeqExpression node) {
		print("seq");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseASeq1Expression(final ASeq1Expression node) {
		print("seq1");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAIseqExpression(final AIseqExpression node) {
		print("iseq");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAIseq1Expression(final AIseq1Expression node) {
		print("iseq1");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAPermExpression(final APermExpression node) {
		print("perm");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAEmptySequenceExpression(final AEmptySequenceExpression node) {
		print("[]");
	}

	@Override
	public void caseASequenceExtensionExpression(final ASequenceExtensionExpression node) {
		print("[");
		printCommaListSingleLine(node.getExpression());
		print("]");
	}

	@Override
	public void caseASizeExpression(final ASizeExpression node) {
		print("size");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAFirstExpression(final AFirstExpression node) {
		print("first");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseALastExpression(final ALastExpression node) {
		print("last");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAFrontExpression(final AFrontExpression node) {
		print("front");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseATailExpression(final ATailExpression node) {
		print("tail");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseARevExpression(final ARevExpression node) {
		print("rev");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAConcatExpression(final AConcatExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "^");
	}

	@Override
	public void caseAInsertFrontExpression(final AInsertFrontExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "->");
	}

	@Override
	public void caseAInsertTailExpression(final AInsertTailExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<-");
	}

	@Override
	public void caseARestrictFrontExpression(final ARestrictFrontExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/|\\");
	}

	@Override
	public void caseARestrictTailExpression(final ARestrictTailExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "\\|/");
	}

	@Override
	public void caseAGeneralConcatExpression(final AGeneralConcatExpression node) {
		print("conc");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseADefinitionExpression(final ADefinitionExpression node) {
		node.getDefLiteral().apply(this);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAFunctionExpression(final AFunctionExpression node) {
		leftParAssoc(node, node.getIdentifier());
		node.getIdentifier().apply(this);
		rightParAssoc(node, node.getIdentifier());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseATreeExpression(ATreeExpression node) {
		print("tree");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseABtreeExpression(ABtreeExpression node) {
		print("btree");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAConstExpression(AConstExpression node) {
		print("const");
		printParameterList(Arrays.asList(node.getExpression1(), node.getExpression2()));
	}

	@Override
	public void caseATopExpression(ATopExpression node) {
		print("top");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseASonsExpression(ASonsExpression node) {
		print("sons");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAPrefixExpression(APrefixExpression node) {
		print("prefix");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAPostfixExpression(APostfixExpression node) {
		print("postfix");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseASizetExpression(ASizetExpression node) {
		print("sizet");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAMirrorExpression(AMirrorExpression node) {
		print("mirror");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseARankExpression(ARankExpression node) {
		print("rank");
		printParameterList(Arrays.asList(node.getExpression1(), node.getExpression2()));
	}

	@Override
	public void caseAFatherExpression(AFatherExpression node) {
		print("father");
		printParameterList(Arrays.asList(node.getExpression1(), node.getExpression2()));
	}

	@Override
	public void caseASonExpression(ASonExpression node) {
		print("son");
		printParameterList(Arrays.asList(node.getExpression1(), node.getExpression2(), node.getExpression3()));
	}

	@Override
	public void caseASubtreeExpression(ASubtreeExpression node) {
		print("subtree");
		printParameterList(Arrays.asList(node.getExpression1(), node.getExpression2()));
	}

	@Override
	public void caseAArityExpression(AArityExpression node) {
		print("arity");
		printParameterList(Arrays.asList(node.getExpression1(), node.getExpression2()));
	}

	@Override
	public void caseABinExpression(ABinExpression node) {
		print("bin");
		printParameterList(Stream.of(node.getExpression1(), node.getExpression2(), node.getExpression3()).filter(Objects::nonNull).collect(Collectors.toList()));
	}

	@Override
	public void caseALeftExpression(ALeftExpression node) {
		print("left");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseARightExpression(ARightExpression node) {
		print("right");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAInfixExpression(AInfixExpression node) {
		print("infix");
		printParameterList(Collections.singletonList(node.getExpression()));
	}

	@Override
	public void caseAStructExpression(final AStructExpression node) {
		print("struct");
		printParameterList(node.getEntries());
	}

	@Override
	public void caseARecExpression(final ARecExpression node) {
		print("rec");
		printParameterList(node.getEntries());
	}

	@Override
	public void caseARecordFieldExpression(final ARecordFieldExpression node) {
		applyLeftAssociative(node.getRecord(), node, node.getIdentifier(), "'");
	}

	@Override
	public void caseAOperatorExpression(AOperatorExpression node) {
		node.getName().apply(this);
		printParameterList(node.getIdentifiers());
	}

	@Override
	public void caseARecEntry(final ARecEntry node) {
		node.getIdentifier().apply(this);
		print(": ");
		node.getValue().apply(this);
	}

	@Override
	public void caseABlockSubstitution(final ABlockSubstitution node) {
		indent();
		printlnOpt("BEGIN");
		node.getSubstitution().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseASkipSubstitution(ASkipSubstitution node) {
		print("skip");
	}

	@Override
	public void caseAAssignSubstitution(AAssignSubstitution node) {
		printCommaListCompact(node.getLhsExpression());
		print(" := ");
		printCommaListCompact(node.getRhsExpressions());
	}

	@Override
	public void caseAPreconditionSubstitution(APreconditionSubstitution node) {
		indent();
		printlnOpt("PRE");
		node.getPredicate().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("THEN");
		node.getSubstitution().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAAssertionSubstitution(AAssertionSubstitution node) {
		indent();
		printlnOpt("ASSERT");
		node.getPredicate().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("THEN");
		node.getSubstitution().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAChoiceSubstitution(AChoiceSubstitution node) {
		indent();
		printlnOpt("CHOICE");
		printList(node.getSubstitutions());
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAChoiceOrSubstitution(AChoiceOrSubstitution node) {
		print("OR ");
		node.getSubstitution().apply(this);
	}

	@Override
	public void caseAIfSubstitution(AIfSubstitution node) {
		print("IF ");
		indent();
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getThen().apply(this);
		dedent();
		printlnOpt();
		printListTrailing(node.getElsifSubstitutions());
		if (node.getElse() != null) {
			indent();
			printlnOpt("ELSE");
			node.getElse().apply(this);
			dedent();
			printlnOpt();
		}
		print("END");
	}

	@Override
	public void caseAIfElsifSubstitution(AIfElsifSubstitution node) {
		print("ELSIF ");
		indent();
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getThenSubstitution().apply(this);
		dedent();
	}

	@Override
	public void caseASelectSubstitution(ASelectSubstitution node) {
		print("SELECT ");
		indent();
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getThen().apply(this);
		dedent();
		printlnOpt();
		printListTrailing(node.getWhenSubstitutions());
		if (node.getElse() != null) {
			indent();
			printlnOpt("ELSE");
			node.getElse().apply(this);
			dedent();
			printlnOpt();
		}
		print("END");
	}

	@Override
	public void caseASelectWhenSubstitution(ASelectWhenSubstitution node) {
		indent();
		print("WHEN ");
		node.getCondition().apply(this);
		printlnOpt(" THEN");
		node.getSubstitution().apply(this);
		dedent();
	}

	@Override
	public void caseACaseSubstitution(ACaseSubstitution node) {
		print("CASE ");
		indent();
		node.getExpression().apply(this);
		printlnOpt(" OF");
		print("EITHER ");
		indent();
		printCommaListSingleLine(node.getEitherExpr());
		printlnOpt(" THEN");
		node.getEitherSubst().apply(this);
		dedent();
		printlnOpt();
		printListTrailing(node.getOrSubstitutions());
		if (node.getElse() != null) {
			indent();
			printlnOpt("ELSE");
			node.getElse().apply(this);
			dedent();
			printlnOpt();
		}
		dedent();
		printlnOpt("END");
		print("END");
	}

	@Override
	public void caseACaseOrSubstitution(ACaseOrSubstitution node) {
		print("OR ");
		indent();
		printCommaListSingleLine(node.getExpressions());
		printlnOpt(" THEN");
		node.getSubstitution().apply(this);
		dedent();
	}

	@Override
	public void caseAAnySubstitution(AAnySubstitution node) {
		print("ANY ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt(" WHERE");
		node.getWhere().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("THEN");
		node.getThen().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseALetSubstitution(ALetSubstitution node) {
		print("LET ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt(" BE");
		node.getPredicate().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("IN");
		node.getSubstitution().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseABecomesElementOfSubstitution(ABecomesElementOfSubstitution node) {
		printCommaListCompact(node.getIdentifiers());
		print(" :: ");
		node.getSet().apply(this);
	}

	@Override
	public void caseABecomesSuchSubstitution(ABecomesSuchSubstitution node) {
		printCommaListCompact(node.getIdentifiers());
		print(" : (");
		node.getPredicate().apply(this);
		print(")");
	}

	@Override
	public void caseAVarSubstitution(AVarSubstitution node) {
		print("VAR ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt(" IN");
		node.getSubstitution().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseASequenceSubstitution(ASequenceSubstitution node) {
		printListOpt(node.getSubstitutions(), " ;\n");
	}

	@Override
	public void caseAFuncOpSubstitution(AFuncOpSubstitution node) {
		node.getFunction().apply(this);
	}

	@Override
	public void caseAOpSubstitution(AOpSubstitution node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAOperationCallSubstitution(AOperationCallSubstitution node) {
		if (!node.getResultIdentifiers().isEmpty()) {
			printCommaListCompact(node.getResultIdentifiers());
			print(" <-- ");
		}
		printDottedList(node.getOperation());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAWhileSubstitution(AWhileSubstitution node) {
		indent();
		printlnOpt("WHILE");
		node.getCondition().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("DO");
		node.getDoSubst().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("INVARIANT");
		node.getInvariant().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("VARIANT");
		node.getVariant().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAParallelSubstitution(AParallelSubstitution node) {
		printListOpt(node.getSubstitutions(), " ||\n");
	}

	@Override
	public void caseADefinitionSubstitution(ADefinitionSubstitution node) {
		node.getDefLiteral().apply(this);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAForallSubMessageSubstitution(AForallSubMessageSubstitution node) {
		print("RULE_FORALL ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt();
		indent();
		printlnOpt("WHERE");
		node.getWhere().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("EXPECT");
		node.getExpect().apply(this);
		dedent();
		printlnOpt();
		if (node.getErrorType() != null) {
			print("ERROR_TYPE ");
			node.getErrorType().apply(this);
			printlnOpt();
		}
		indent();
		printlnOpt("COUNTEREXAMPLE");
		node.getMessage().apply(this);
		dedent();
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseARuleFailSubSubstitution(ARuleFailSubSubstitution node) {
		print("RULE_FAIL ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt();
		if (node.getWhen() != null) {
			indent();
			printlnOpt("WHEN");
			node.getWhen().apply(this);
			dedent();
			printlnOpt();
		}
		if (node.getErrorType() != null) {
			print("ERROR_TYPE ");
			node.getErrorType().apply(this);
			printlnOpt();
		}
		indent();
		printlnOpt("COUNTEREXAMPLE");
		node.getMessage().apply(this);
		dedent();
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAForLoopSubstitution(AForLoopSubstitution node) {
		print("FOR ");
		printCommaListCompact(node.getIdentifiers());
		indent();
		printlnOpt(" IN");
		node.getSet().apply(this);
		dedent();
		printlnOpt();
		indent();
		printlnOpt("DO");
		node.getDoSubst().apply(this);
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseAOperatorSubstitution(AOperatorSubstitution node) {
		node.getName().apply(this);
		printParameterList(node.getArguments());
	}

	@Override
	public void caseADefineSubstitution(ADefineSubstitution node) {
		print("DEFINE ");
		node.getName().apply(this);
		indent();
		printlnOpt();
		indent();
		print("TYPE ");
		node.getType().apply(this);
		dedent();
		printlnOpt();
		if (node.getDummyValue() != null) {
			indent();
			print("DUMMY_VALUE ");
			node.getDummyValue().apply(this);
			dedent();
			printlnOpt();
		}
		indent();
		print("VALUE ");
		node.getValue().apply(this);
		dedent();
		dedent();
		printlnOpt();
		print("END");
	}

	@Override
	public void caseTIdentifierLiteral(final TIdentifierLiteral node) {
		print(this.renaming.renameIdentifier(node.getText()));
	}

	@Override
	public void caseTDefLiteralSubstitution(final TDefLiteralSubstitution node) {
		print(this.renaming.renameIdentifier(node.getText()));
	}

	@Override
	public void caseTDefLiteralPredicate(final TDefLiteralPredicate node) {
		print(this.renaming.renameIdentifier(node.getText()));
	}

	@Override
	public void defaultCase(final Node node) {
		if (node instanceof Token) {
			print(((Token) node).getText());
		} else {
			throw new IllegalArgumentException("Node type '" + node.getClass().getSimpleName() + "' not (yet) supported by PrettyPrinter: " + node);
		}
	}
}
