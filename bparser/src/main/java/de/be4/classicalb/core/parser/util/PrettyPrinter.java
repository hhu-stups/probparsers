package de.be4.classicalb.core.parser.util;

import java.util.*;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.analysis.AnalysisAdapter;
import de.be4.classicalb.core.parser.node.*;

public class PrettyPrinter extends AnalysisAdapter {
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

	private final StringBuilder sb = new StringBuilder();
	private IIdentifierRenaming renaming;

	public PrettyPrinter() {
		this.renaming = IIdentifierRenaming.QUOTE_INVALID;
	}

	public IIdentifierRenaming getRenaming() {
		return this.renaming;
	}

	public void setRenaming(IIdentifierRenaming renaming) {
		this.renaming = Objects.requireNonNull(renaming, "renaming");
	}

	public String getPrettyPrint() {
		return sb.toString();
	}

	private void printList(final List<? extends Node> list, final String separator) {
		for (final Iterator<? extends Node> it = list.iterator(); it.hasNext(); ) {
			final Node node = it.next();
			node.apply(this);
			if (it.hasNext()) {
				sb.append(separator);
			}
		}
	}

	private void printDottedIdentifier(final List<TIdentifierLiteral> list) {
		printList(list, ".");
	}

	private void printCommaList(final List<? extends Node> list) {
		printList(list, ", ");
	}

	private void printCommaListCompact(final List<? extends Node> list) {
		printList(list, ",");
	}

	private void printSemicolonList(final List<? extends Node> list) {
		printList(list, ";\n");
	}

	private void printSemicolonListSingleLine(final List<? extends Node> list) {
		printList(list, "; ");
	}

	private void printParameterList(final List<? extends Node> parameters) {
		if (!parameters.isEmpty()) {
			sb.append('(');
			printCommaList(parameters);
			sb.append(')');
		}
	}

	private void leftParAssoc(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		// we do not insert parentheses when priority is the same
		if (priorityNode != null && priorityRight != null && priorityRight < priorityNode) {
			sb.append("(");
		}
	}

	private void rightParAssoc(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight < priorityNode) {
			sb.append(")");
		}
	}

	private void leftPar(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight <= priorityNode) {
			sb.append("(");
		}
	}

	private void rightPar(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight <= priorityNode) {
			sb.append(")");
		}
	}

	private void applyLeftAssociative(final Node left, final Node node, final Node right, final String operatorStr) {
		leftParAssoc(node, left);
		left.apply(this);
		rightParAssoc(node, left);

		sb.append(operatorStr);

		leftPar(node, right);
		right.apply(this);
		rightPar(node, right);
	}

	private void applyRightAssociative(final Node left, final Node node, final Node right, final String operatorStr) {
		leftPar(node, left);
		left.apply(this);
		rightPar(node, left);

		sb.append(operatorStr);

		leftParAssoc(node, right);
		right.apply(this);
		rightParAssoc(node, right);
	}

	@Override
	public void caseStart(final Start node) {
		node.getPParseUnit().apply(this);
	}

	@Override
	public void caseAGeneratedParseUnit(AGeneratedParseUnit node) {
		sb.append("/*@generated*/\n");
		node.getParseUnit().apply(this);
	}

	@Override
	public void caseAPackageParseUnit(APackageParseUnit node) {
		sb.append("/*@package ");
		node.getPackage().apply(this);
		sb.append(" */\n");
		for (PImportPackage imp : node.getImports()) {
			imp.apply(this);
		}
		node.getParseUnit().apply(this);
	}

	@Override
	public void caseAAbstractMachineParseUnit(AAbstractMachineParseUnit node) {
		node.getVariant().apply(this);
		sb.append(" ");
		node.getHeader().apply(this);
		sb.append("\n");
		for (PMachineClause e : node.getMachineClauses()) {
			e.apply(this);
		}
		sb.append("END");
	}

	@Override
	public void caseARefinementMachineParseUnit(ARefinementMachineParseUnit node) {
		sb.append("REFINEMENT ");
		node.getHeader().apply(this);
		sb.append("\nREFINES ");
		node.getRefMachine().apply(this);
		sb.append("\n");
		for (PMachineClause e : node.getMachineClauses()) {
			e.apply(this);
		}
		sb.append("END");
	}

	@Override
	public void caseAImplementationMachineParseUnit(AImplementationMachineParseUnit node) {
		sb.append("IMPLEMENTATION ");
		node.getHeader().apply(this);
		sb.append("\nREFINES ");
		node.getRefMachine().apply(this);
		sb.append("\n");
		for (PMachineClause e : node.getMachineClauses()) {
			e.apply(this);
		}
		sb.append("END");
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
		//sb.append("#MACHINECLAUSE ");
		node.getMachineClause().apply(this);
	}

	@Override
	public void caseAOppatternParseUnit(AOppatternParseUnit node) {
		//sb.append(BParser.OPERATION_PATTERN_PREFIX);
		//sb.append(' ');
		printDottedIdentifier(node.getName());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAImportPackage(AImportPackage node) {
		sb.append("/*@import-package ");
		node.getPackage().apply(this);
		sb.append(" */\n");
	}

	@Override
	public void caseAUndefArgpattern(AUndefArgpattern node) {
		sb.append("_");
	}

	@Override
	public void caseADefArgpattern(ADefArgpattern node) {
		node.getExpression().apply(this);
	}

	@Override
	public void caseAMachineMachineVariant(AMachineMachineVariant node) {
		sb.append("MACHINE");
	}

	@Override
	public void caseAModelMachineVariant(AModelMachineVariant node) {
		sb.append("MODEL");
	}

	@Override
	public void caseASystemMachineVariant(ASystemMachineVariant node) {
		sb.append("SYSTEM");
	}

	@Override
	public void caseAMachineHeader(AMachineHeader node) {
		printDottedIdentifier(node.getName());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseADefinitionsMachineClause(ADefinitionsMachineClause node) {
		sb.append("DEFINITIONS\n");
		for (final PDefinition e : node.getDefinitions()) {
			e.apply(this);
			sb.append(";\n");
		}
	}

	@Override
	public void caseASeesMachineClause(ASeesMachineClause node) {
		sb.append("SEES ");
		printCommaList(node.getMachineNames());
		sb.append("\n");
	}

	@Override
	public void caseAPromotesMachineClause(APromotesMachineClause node) {
		sb.append("PROMOTES ");
		printCommaList(node.getOperationNames());
		sb.append("\n");
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node) {
		sb.append("USES ");
		printCommaList(node.getMachineNames());
		sb.append("\n");
	}

	@Override
	public void caseAIncludesMachineClause(AIncludesMachineClause node) {
		sb.append("INCLUDES ");
		printCommaList(node.getMachineReferences());
		sb.append("\n");
	}

	@Override
	public void caseAExtendsMachineClause(AExtendsMachineClause node) {
		sb.append("EXTENDS ");
		printCommaList(node.getMachineReferences());
		sb.append("\n");
	}

	@Override
	public void caseAImportsMachineClause(AImportsMachineClause node) {
		sb.append("IMPORTS ");
		printCommaList(node.getMachineReferences());
		sb.append("\n");
	}

	@Override
	public void caseASetsMachineClause(ASetsMachineClause node) {
		sb.append("SETS ");
		printSemicolonListSingleLine(node.getSetDefinitions());
		sb.append("\n");
	}

	@Override
	public void caseAFreetypesMachineClause(AFreetypesMachineClause node) {
		sb.append("FREETYPES\n");
		printSemicolonList(node.getFreetypes());
		sb.append("\n");
	}

	@Override
	public void caseAVariablesMachineClause(AVariablesMachineClause node) {
		sb.append("VARIABLES ");
		printCommaList(node.getIdentifiers());
		sb.append("\n");
	}

	@Override
	public void caseAConcreteVariablesMachineClause(AConcreteVariablesMachineClause node) {
		sb.append("CONCRETE_VARIABLES ");
		printCommaList(node.getIdentifiers());
		sb.append("\n");
	}

	@Override
	public void caseAAbstractConstantsMachineClause(AAbstractConstantsMachineClause node) {
		sb.append("ABSTRACT_CONSTANTS ");
		printCommaList(node.getIdentifiers());
		sb.append("\n");
	}

	@Override
	public void caseAConstantsMachineClause(AConstantsMachineClause node) {
		sb.append("CONSTANTS ");
		printCommaList(node.getIdentifiers());
		sb.append("\n");
	}

	@Override
	public void caseAPropertiesMachineClause(APropertiesMachineClause node) {
		sb.append("PROPERTIES\n");
		node.getPredicates().apply(this);
		sb.append("\n");
	}

	@Override
	public void caseAConstraintsMachineClause(AConstraintsMachineClause node) {
		sb.append("CONSTRAINTS ");
		node.getPredicates().apply(this);
		sb.append("\n");
	}

	@Override
	public void caseAInitialisationMachineClause(AInitialisationMachineClause node) {
		sb.append("INITIALISATION ");
		node.getSubstitutions().apply(this);
		sb.append("\n");
	}

	@Override
	public void caseAInvariantMachineClause(AInvariantMachineClause node) {
		sb.append("INVARIANT ");
		node.getPredicates().apply(this);
		sb.append("\n");
	}

	@Override
	public void caseAAssertionsMachineClause(AAssertionsMachineClause node) {
		sb.append("ASSERTIONS\n");
		printSemicolonListSingleLine(node.getPredicates());
		sb.append("\n");
	}

	@Override
	public void caseAValuesMachineClause(AValuesMachineClause node) {
		sb.append("VALUES\n");
		printSemicolonList(node.getEntries());
		sb.append("\n");
	}

	@Override
	public void caseALocalOperationsMachineClause(ALocalOperationsMachineClause node) {
		sb.append("LOCAL_OPERATIONS\n");
		printSemicolonList(node.getOperations());
		sb.append("\n");
	}

	@Override
	public void caseAOperationsMachineClause(AOperationsMachineClause node) {
		sb.append("OPERATIONS\n");
		printSemicolonList(node.getOperations());
		sb.append("\n");
	}

	@Override
	public void caseAReferencesMachineClause(AReferencesMachineClause node) {
		sb.append("REFERENCES ");
		printCommaList(node.getMachineReferences());
		sb.append("\n");
	}

	@Override
	public void caseAExpressionsMachineClause(AExpressionsMachineClause node) {
		sb.append("EXPRESSIONS\n");
		printSemicolonList(node.getExpressions());
		sb.append("\n");
	}

	@Override
	public void caseAPredicatesMachineClause(APredicatesMachineClause node) {
		sb.append("PREDICATES\n");
		printSemicolonList(node.getPredicates());
		sb.append("\n");
	}

	@Override
	public void caseAMachineReference(final AMachineReference node) {
		printDottedIdentifier(node.getMachineName());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAFileMachineReference(AFileMachineReference node) {
		node.getReference().apply(this);
		sb.append(" /*@file ");
		node.getFile().apply(this);
		sb.append(" */");
	}

	@Override
	public void caseAMachineReferenceNoParams(AMachineReferenceNoParams node) {
		printDottedIdentifier(node.getMachineName());
	}

	@Override
	public void caseAFileMachineReferenceNoParams(AFileMachineReferenceNoParams node) {
		node.getReference().apply(this);
		sb.append(" /*@file ");
		node.getFile().apply(this);
		sb.append(" */");
	}

	@Override
	public void caseAOperationReference(AOperationReference node) {
		printDottedIdentifier(node.getOperationName());
	}

	@Override
	public void caseAExpressionDefinition(AExpressionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseAPredicateDefinition(APredicateDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseAPredicateDefinitionDefinition(APredicateDefinitionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseASubstitutionDefinitionDefinition(ASubstitutionDefinitionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseAExpressionDefinitionDefinition(AExpressionDefinitionDefinition node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseAFileDefinitionDefinition(AFileDefinitionDefinition node) {
		node.getFilename().apply(this);
	}

	@Override
	public void caseADescriptionSet(ADescriptionSet node) {
		node.getSet().apply(this);
		sb.append(" /*@desc ");
		sb.append(node.getPragmaFreeText().getText());
		sb.append(" */");
	}

	@Override
	public void caseADeferredSetSet(final ADeferredSetSet node) {
		printDottedIdentifier(node.getIdentifier());
	}

	@Override
	public void caseAEnumeratedSetSet(final AEnumeratedSetSet node) {
		printDottedIdentifier(node.getIdentifier());
		sb.append("={");
		printCommaListCompact(node.getElements());
		sb.append("}");
	}

	@Override
	public void caseAEnumeratedSetViaDefSet(AEnumeratedSetViaDefSet node) {
		printDottedIdentifier(node.getIdentifier());
		sb.append("=");
		printDottedIdentifier(node.getElementsDef());
	}

	@Override
	public void caseAFreetype(AFreetype node) {
		node.getName().apply(this);
		printParameterList(node.getParameters());
		sb.append(" = ");
		printCommaList(node.getConstructors());
	}

	@Override
	public void caseAConstructorFreetypeConstructor(AConstructorFreetypeConstructor node) {
		node.getName().apply(this);
		sb.append("(");
		node.getArgument().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAElementFreetypeConstructor(AElementFreetypeConstructor node) {
		node.getName().apply(this);
	}

	@Override
	public void caseAValuesEntry(AValuesEntry node) {
		printDottedIdentifier(node.getIdentifier());
		sb.append(" = ");
		node.getValue().apply(this);
	}

	@Override
	public void caseAOperation(AOperation node) {
		if (!node.getReturnValues().isEmpty()) {
			printCommaList(node.getReturnValues());
			sb.append(" <-- ");
		}
		printDottedIdentifier(node.getOpName());
		printParameterList(node.getParameters());
		sb.append(" = ");
		node.getOperationBody().apply(this);
	}

	@Override
	public void caseARefinedOperation(ARefinedOperation node) {
		if (!node.getReturnValues().isEmpty()) {
			printCommaList(node.getReturnValues());
			sb.append(" <-- ");
		}
		printDottedIdentifier(node.getOpName());
		printParameterList(node.getParameters());
		node.getRefKw().apply(this);
		node.getAbOpName().apply(this);
		sb.append(" = ");
		node.getOperationBody().apply(this);
	}

	@Override
	public void caseARuleOperation(ARuleOperation node) {
		sb.append("RULE ");
		node.getRuleName().apply(this);
		for (POperationAttribute attr : node.getAttributes()) {
			sb.append(" ");
			attr.apply(this);
		}
		sb.append(" BODY\n");
		node.getRuleBody().apply(this);
		sb.append("\nEND");
	}

	@Override
	public void caseAComputationOperation(AComputationOperation node) {
		sb.append("COMPUTATION ");
		node.getName().apply(this);
		for (POperationAttribute attr : node.getAttributes()) {
			sb.append(" ");
			attr.apply(this);
		}
		sb.append(" BODY\n");
		node.getBody().apply(this);
		sb.append("\nEND");
	}

	@Override
	public void caseAFunctionOperation(AFunctionOperation node) {
		sb.append("FUNCTION ");
		printCommaList(node.getReturnValues());
		sb.append(" <-- ");
		node.getName().apply(this);
		printParameterList(node.getParameters());
		for (POperationAttribute attr : node.getAttributes()) {
			sb.append(" ");
			attr.apply(this);
		}
		sb.append(" BODY\n");
		node.getBody().apply(this);
		sb.append("\nEND");
	}

	@Override
	public void caseAOperationAttribute(AOperationAttribute node) {
		node.getName().apply(this);
		sb.append(" ");
		printCommaList(node.getArguments());
	}

	@Override
	public void caseAPredicateAttributeOperationAttribute(APredicateAttributeOperationAttribute node) {
		node.getName().apply(this);
		sb.append(" ");
		node.getPredicate().apply(this);
	}

	@Override
	public void caseADescriptionPredicate(ADescriptionPredicate node) {
		node.getPredicate().apply(this);
		sb.append(" /*@desc ");
		sb.append(node.getContent().getText());
		sb.append(" */");
	}

	@Override
	public void caseALabelPredicate(ALabelPredicate node) {
		sb.append("/*@label ");
		node.getName().apply(this);
		sb.append(" */ ");
		node.getPredicate().apply(this);
	}

	@Override
	public void caseASubstitutionPredicate(ASubstitutionPredicate node) {
		sb.append("[");
		node.getSubstitution().apply(this);
		sb.append("] ");
		node.getPredicate().apply(this);
	}

	@Override
	public void caseAConjunctPredicate(final AConjunctPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " & ");
	}

	@Override
	public void caseANegationPredicate(final ANegationPredicate node) {
		sb.append("not(");
		node.getPredicate().apply(this);
		sb.append(")");
	}

	@Override
	public void caseADisjunctPredicate(final ADisjunctPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " or ");
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
		sb.append("!");
		printCommaListCompact(node.getIdentifiers());
		sb.append(".(");
		node.getImplication().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAExistsPredicate(final AExistsPredicate node) {
		sb.append("#");
		printCommaListCompact(node.getIdentifiers());
		sb.append(".(");
		node.getPredicate().apply(this);
		sb.append(")");
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
		sb.append("btrue");
	}

	@Override
	public void caseAFalsityPredicate(AFalsityPredicate node) {
		sb.append("bfalse");
	}

	@Override
	public void caseAFinitePredicate(AFinitePredicate node) {
		sb.append("@finite(");
		node.getSet().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAPartitionPredicate(APartitionPredicate node) {
		sb.append("@partition(");
		node.getSet().apply(this);
		if (!node.getElements().isEmpty()) {
			sb.append(", ");
			printCommaList(node.getElements());
		}
		sb.append(")");
	}

	@Override
	public void caseADefinitionPredicate(final ADefinitionPredicate node) {
		node.getDefLiteral().apply(this);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseALetPredicatePredicate(final ALetPredicatePredicate node) {
		sb.append("LET ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" BE ");
		node.getAssignment().apply(this);
		sb.append(" IN ");
		node.getPred().apply(this);
		sb.append(" END");
	}

	@Override
	public void caseAIfPredicatePredicate(AIfPredicatePredicate node) {
		sb.append("IF ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThen().apply(this);
		sb.append(" ELSE ");
		node.getElse().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseAOperatorPredicate(AOperatorPredicate node) {
		node.getName().apply(this);
		printParameterList(node.getIdentifiers());
	}

	@Override
	public void caseADescriptionExpression(ADescriptionExpression node) {
		node.getExpression().apply(this);
		sb.append(" /*@desc ");
		sb.append(node.getContent().getText());
		sb.append(" */");
	}

	@Override
	public void caseAIdentifierExpression(final AIdentifierExpression node) {
		printDottedIdentifier(node.getIdentifier());
	}

	@Override
	public void caseAPrimedIdentifierExpression(APrimedIdentifierExpression node) {
		printDottedIdentifier(node.getIdentifier());
		sb.append("$0");
	}

	@Override
	public void caseAStringExpression(final AStringExpression node) {
		sb.append("\"");
		sb.append(Utils.escapeStringContents(node.getContent().getText()));
		sb.append("\"");
	}

	@Override
	public void caseAMultilineStringExpression(AMultilineStringExpression node) {
		sb.append("'''");
		// we could do the same as for the AStringExpression,
		// but it looks nicer when multi-line strings are actually multi-line
		String text = Arrays.stream(node.getContent().getText().split("\n"))
			.map(Utils::escapeStringContents)
			.collect(Collectors.joining("\n"));
		sb.append(text);
		sb.append("'''");
	}

	@Override
	public void caseABooleanTrueExpression(final ABooleanTrueExpression node) {
		sb.append("TRUE");
	}

	@Override
	public void caseABooleanFalseExpression(final ABooleanFalseExpression node) {
		sb.append("FALSE");
	}

	@Override
	public void caseAIntegerExpression(final AIntegerExpression node) {
		sb.append(node.getLiteral().getText());
	}

	@Override
	public void caseARealExpression(ARealExpression node) {
		sb.append(node.getLiteral().getText());
	}

	@Override
	public void caseAHexIntegerExpression(AHexIntegerExpression node) {
		sb.append(node.getLiteral().getText());
	}

	@Override
	public void caseAMaxIntExpression(final AMaxIntExpression node) {
		sb.append("MAXINT");
	}

	@Override
	public void caseAMinIntExpression(final AMinIntExpression node) {
		sb.append("MININT");
	}

	@Override
	public void caseAEmptySetExpression(final AEmptySetExpression node) {
		sb.append("{}");
	}

	@Override
	public void caseAIntegerSetExpression(final AIntegerSetExpression node) {
		sb.append("INTEGER");
	}

	@Override
	public void caseARealSetExpression(ARealSetExpression node) {
		sb.append("REAL");
	}

	@Override
	public void caseAFloatSetExpression(AFloatSetExpression node) {
		sb.append("FLOAT");
	}

	@Override
	public void caseANaturalSetExpression(final ANaturalSetExpression node) {
		sb.append("NATURAL");
	}

	@Override
	public void caseANatural1SetExpression(final ANatural1SetExpression node) {
		sb.append("NATURAL1");
	}

	@Override
	public void caseANatSetExpression(final ANatSetExpression node) {
		sb.append("NAT");
	}

	@Override
	public void caseANat1SetExpression(final ANat1SetExpression node) {
		sb.append("NAT1");
	}

	@Override
	public void caseAIntSetExpression(final AIntSetExpression node) {
		sb.append("INT");
	}

	@Override
	public void caseABoolSetExpression(final ABoolSetExpression node) {
		sb.append("BOOL");
	}

	@Override
	public void caseAStringSetExpression(final AStringSetExpression node) {
		sb.append("STRING");
	}

	@Override
	public void caseAConvertBoolExpression(final AConvertBoolExpression node) {
		sb.append("bool(");
		node.getPredicate().apply(this);
		sb.append(")");
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
		sb.append("-");
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
		sb.append("FDIV(");
		node.getLeft().apply(this);
		sb.append(",");
		node.getRight().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAIfElsifExprExpression(AIfElsifExprExpression node) {
		sb.append(" ELSIF ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThen().apply(this);
	}

	@Override
	public void caseAIfThenElseExpression(AIfThenElseExpression node) {
		sb.append("IF ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThen().apply(this);
		for (PExpression e : node.getElsifs()) {
			e.apply(this);
		}
		sb.append(" ELSE ");
		node.getElse().apply(this);
		sb.append(" END");
	}

	@Override
	public void caseALetExpressionExpression(final ALetExpressionExpression node) {
		sb.append("LET ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" BE ");
		node.getAssignment().apply(this);
		sb.append(" IN ");
		node.getExpr().apply(this);
		sb.append(" END");
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
		sb.append("succ");
	}

	@Override
	public void caseAPredecessorExpression(final APredecessorExpression node) {
		sb.append("pred");
	}

	@Override
	public void caseAMaxExpression(final AMaxExpression node) {
		sb.append("max(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAMinExpression(final AMinExpression node) {
		sb.append("min(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseACardExpression(final ACardExpression node) {
		sb.append("card(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAConvertIntFloorExpression(AConvertIntFloorExpression node) {
		sb.append("floor(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAConvertIntCeilingExpression(AConvertIntCeilingExpression node) {
		sb.append("ceiling(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAConvertRealExpression(AConvertRealExpression node) {
		sb.append("real(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAGeneralSumExpression(final AGeneralSumExpression node) {
		sb.append("SIGMA(");
		printCommaListCompact(node.getIdentifiers());
		sb.append(").(");
		node.getPredicates().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAGeneralProductExpression(final AGeneralProductExpression node) {
		sb.append("PI(");
		printCommaListCompact(node.getIdentifiers());
		sb.append(").(");
		node.getPredicates().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseACoupleExpression(final ACoupleExpression node) {
		assert node.getList().size() >= 2;
		sb.append("(");
		printCommaListCompact(node.getList());
		sb.append(")");
	}

	@Override
	public void caseAComprehensionSetExpression(final AComprehensionSetExpression node) {
		sb.append("{");
		printCommaListCompact(node.getIdentifiers());
		sb.append("|");
		node.getPredicates().apply(this);
		sb.append("}");
	}

	@Override
	public void caseASymbolicComprehensionSetExpression(ASymbolicComprehensionSetExpression node) {
		sb.append("/*@symbolic*/ ");
		sb.append("{");
		printCommaList(node.getIdentifiers());
		sb.append("|");
		node.getPredicates().apply(this);
		sb.append("}");
	}

	@Override
	public void caseAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node) {
		sb.append("{");
		printCommaListCompact(node.getIdentifiers());
		sb.append("·"); // Currently has to be a non-ASCII dot (e. g. U+00B7 MIDDLE DOT) for our parser to recognize it.
		node.getPredicates().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append("}");
	}

	@Override
	public void caseAPowSubsetExpression(final APowSubsetExpression node) {
		sb.append("POW(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAPow1SubsetExpression(final APow1SubsetExpression node) {
		sb.append("POW1(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAFinSubsetExpression(final AFinSubsetExpression node) {
		sb.append("FIN(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAFin1SubsetExpression(final AFin1SubsetExpression node) {
		sb.append("FIN1(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASetExtensionExpression(final ASetExtensionExpression node) {
		sb.append("{");
		printCommaListCompact(node.getExpressions());
		sb.append("}");
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
		sb.append("union(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAGeneralIntersectionExpression(final AGeneralIntersectionExpression node) {
		sb.append("inter(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAQuantifiedUnionExpression(final AQuantifiedUnionExpression node) {
		sb.append("UNION(");
		printCommaListCompact(node.getIdentifiers());
		sb.append(").(");
		node.getPredicates().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASymbolicQuantifiedUnionExpression(ASymbolicQuantifiedUnionExpression node) {
		sb.append("/*@symbolic*/ ");
		sb.append("UNION");
		printCommaList(node.getIdentifiers());
		sb.append(".");
		sb.append("(");
		node.getPredicates().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAQuantifiedIntersectionExpression(final AQuantifiedIntersectionExpression node) {
		sb.append("INTER(");
		printCommaListCompact(node.getIdentifiers());
		sb.append(").(");
		node.getPredicates().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseARelationsExpression(final ARelationsExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<->");
	}

	@Override
	public void caseAIdentityExpression(final AIdentityExpression node) {
		sb.append("id(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAReverseExpression(final AReverseExpression node) {
		leftPar(node, node.getExpression());
		node.getExpression().apply(this);
		rightPar(node, node.getExpression());
		sb.append("~");
	}

	@Override
	public void caseAFirstProjectionExpression(final AFirstProjectionExpression node) {
		sb.append("prj1(");
		node.getExp1().apply(this);
		sb.append(",");
		node.getExp2().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAEventBFirstProjectionExpression(AEventBFirstProjectionExpression node) {
		sb.append("prj1(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAEventBFirstProjectionV2Expression(AEventBFirstProjectionV2Expression node) {
		sb.append("@prj1");
	}

	@Override
	public void caseASecondProjectionExpression(final ASecondProjectionExpression node) {
		sb.append("prj2(");
		node.getExp1().apply(this);
		sb.append(",");
		node.getExp2().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAEventBSecondProjectionExpression(AEventBSecondProjectionExpression node) {
		sb.append("prj2(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAEventBSecondProjectionV2Expression(AEventBSecondProjectionV2Expression node) {
		sb.append("@prj2");
	}

	@Override
	public void caseACompositionExpression(final ACompositionExpression node) {
		sb.append("(");
		node.getLeft().apply(this);
		sb.append(";");
		node.getRight().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASymbolicCompositionExpression(ASymbolicCompositionExpression node) {
		node.getLeft().apply(this);
		sb.append(" /*@symbolic*/ ");
		sb.append(";");
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
		sb.append("(");
		node.getLeft().apply(this);
		sb.append("||");
		node.getRight().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAIterationExpression(final AIterationExpression node) {
		sb.append("iterate(");
		node.getLeft().apply(this);
		sb.append(",");
		node.getRight().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAReflexiveClosureExpression(final AReflexiveClosureExpression node) {
		sb.append("closure(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAClosureExpression(final AClosureExpression node) {
		sb.append("closure1(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseADomainExpression(final ADomainExpression node) {
		sb.append("dom(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseARangeExpression(final ARangeExpression node) {
		sb.append("ran(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAImageExpression(final AImageExpression node) {
		leftParAssoc(node, node.getLeft());
		node.getLeft().apply(this);
		rightParAssoc(node, node.getLeft());
		sb.append("[");
		node.getRight().apply(this);
		sb.append("]");
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
		sb.append("%");
		printCommaListCompact(node.getIdentifiers());
		sb.append(".(");
		node.getPredicate().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASymbolicLambdaExpression(ASymbolicLambdaExpression node) {
		sb.append("/*@symbolic*/ ");
		sb.append("%");
		printCommaList(node.getIdentifiers());
		sb.append(".");
		sb.append("(");
		node.getPredicate().apply(this);
		sb.append("|");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseATransFunctionExpression(final ATransFunctionExpression node) {
		sb.append("fnc(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseATransRelationExpression(final ATransRelationExpression node) {
		sb.append("rel(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASeqExpression(final ASeqExpression node) {
		sb.append("seq(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASeq1Expression(final ASeq1Expression node) {
		sb.append("seq1(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAIseqExpression(final AIseqExpression node) {
		sb.append("iseq(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAIseq1Expression(final AIseq1Expression node) {
		sb.append("iseq1(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAPermExpression(final APermExpression node) {
		sb.append("perm(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAEmptySequenceExpression(final AEmptySequenceExpression node) {
		sb.append("[]");
	}

	@Override
	public void caseASequenceExtensionExpression(final ASequenceExtensionExpression node) {
		sb.append("[");
		printCommaListCompact(node.getExpression());
		sb.append("]");
	}

	@Override
	public void caseASizeExpression(final ASizeExpression node) {
		sb.append("size(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAFirstExpression(final AFirstExpression node) {
		sb.append("first(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseALastExpression(final ALastExpression node) {
		sb.append("last(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAFrontExpression(final AFrontExpression node) {
		sb.append("front(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseATailExpression(final ATailExpression node) {
		sb.append("tail(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseARevExpression(final ARevExpression node) {
		sb.append("rev(");
		node.getExpression().apply(this);
		sb.append(")");
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
		sb.append("conc(");
		node.getExpression().apply(this);
		sb.append(")");
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
		sb.append("tree(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseABtreeExpression(ABtreeExpression node) {
		sb.append("btree(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAConstExpression(AConstExpression node) {
		sb.append("const(");
		node.getExpression1().apply(this);
		sb.append(", ");
		node.getExpression2().apply(this);
		sb.append(")");
	}

	@Override
	public void caseATopExpression(ATopExpression node) {
		sb.append("top(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASonsExpression(ASonsExpression node) {
		sb.append("sons(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAPrefixExpression(APrefixExpression node) {
		sb.append("prefix(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAPostfixExpression(APostfixExpression node) {
		sb.append("postfix(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASizetExpression(ASizetExpression node) {
		sb.append("sizet(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAMirrorExpression(AMirrorExpression node) {
		sb.append("mirror(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseARankExpression(ARankExpression node) {
		sb.append("rank(");
		node.getExpression1().apply(this);
		sb.append(", ");
		node.getExpression2().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAFatherExpression(AFatherExpression node) {
		sb.append("father(");
		node.getExpression1().apply(this);
		sb.append(", ");
		node.getExpression2().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASonExpression(ASonExpression node) {
		sb.append("son(");
		node.getExpression1().apply(this);
		sb.append(", ");
		node.getExpression2().apply(this);
		sb.append(", ");
		node.getExpression3().apply(this);
		sb.append(")");
	}

	@Override
	public void caseASubtreeExpression(ASubtreeExpression node) {
		sb.append("subtree(");
		node.getExpression1().apply(this);
		sb.append(", ");
		node.getExpression2().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAArityExpression(AArityExpression node) {
		sb.append("arity(");
		node.getExpression1().apply(this);
		sb.append(", ");
		node.getExpression2().apply(this);
		sb.append(")");
	}

	@Override
	public void caseABinExpression(ABinExpression node) {
		sb.append("bin(");
		node.getExpression1().apply(this);
		if (node.getExpression2() != null) {
			assert node.getExpression3() != null;
			sb.append(", ");
			node.getExpression2().apply(this);
			sb.append(", ");
			node.getExpression3().apply(this);
		} else {
			assert node.getExpression3() == null;
		}
		sb.append(")");
	}

	@Override
	public void caseALeftExpression(ALeftExpression node) {
		sb.append("left(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseARightExpression(ARightExpression node) {
		sb.append("right(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAInfixExpression(AInfixExpression node) {
		sb.append("infix(");
		node.getExpression().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAStructExpression(final AStructExpression node) {
		sb.append("struct(");
		printCommaListCompact(node.getEntries());
		sb.append(")");
	}

	@Override
	public void caseARecExpression(final ARecExpression node) {
		sb.append("rec(");
		printCommaListCompact(node.getEntries());
		sb.append(")");
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
		sb.append(":");
		node.getValue().apply(this);
	}

	@Override
	public void caseABlockSubstitution(final ABlockSubstitution node) {
		sb.append("BEGIN\n");
		node.getSubstitution().apply(this);
		sb.append("\nEND");
	}

	@Override
	public void caseASkipSubstitution(ASkipSubstitution node) {
		sb.append("skip");
	}

	@Override
	public void caseAAssignSubstitution(AAssignSubstitution node) {
		printCommaListCompact(node.getLhsExpression());
		sb.append(" := ");
		printCommaListCompact(node.getRhsExpressions());
	}

	@Override
	public void caseAPreconditionSubstitution(APreconditionSubstitution node) {
		sb.append("PRE ");
		node.getPredicate().apply(this);
		sb.append(" THEN ");
		node.getSubstitution().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseAAssertionSubstitution(AAssertionSubstitution node) {
		sb.append("ASSERT ");
		node.getPredicate().apply(this);
		sb.append(" THEN ");
		node.getSubstitution().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseAChoiceSubstitution(AChoiceSubstitution node) {
		sb.append("CHOICE ");
		for (PSubstitution e : node.getSubstitutions()) {
			e.apply(this);
		}
		sb.append(" END ");
	}

	@Override
	public void caseAChoiceOrSubstitution(AChoiceOrSubstitution node) {
		sb.append(" OR ");
		node.getSubstitution().apply(this);
	}

	@Override
	public void caseAIfSubstitution(AIfSubstitution node) {
		sb.append("IF ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThen().apply(this);
		for (PSubstitution e : node.getElsifSubstitutions()) {
			e.apply(this);
		}
		if (node.getElse() != null) {
			sb.append(" ELSE ");
			node.getElse().apply(this);
		}
		sb.append(" END ");
	}

	@Override
	public void caseAIfElsifSubstitution(AIfElsifSubstitution node) {
		sb.append(" ELSIF ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThenSubstitution().apply(this);
	}

	@Override
	public void caseASelectSubstitution(ASelectSubstitution node) {
		sb.append("SELECT ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThen().apply(this);
		for (PSubstitution e : node.getWhenSubstitutions()) {
			e.apply(this);
		}
		if (node.getElse() != null) {
			sb.append(" ELSE ");
			node.getElse().apply(this);
		}
		sb.append(" END ");
	}

	@Override
	public void caseASelectWhenSubstitution(ASelectWhenSubstitution node) {
		sb.append(" WHEN ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getSubstitution().apply(this);
	}

	@Override
	public void caseACaseSubstitution(ACaseSubstitution node) {
		sb.append("CASE ");
		node.getExpression().apply(this);
		sb.append(" OF EITHER ");
		printCommaListCompact(node.getEitherExpr());
		sb.append(" THEN ");
		node.getEitherSubst().apply(this);
		for (PSubstitution e : node.getOrSubstitutions()) {
			e.apply(this);
		}
		if (node.getElse() != null) {
			sb.append(" ELSE ");
			node.getElse().apply(this);
		}
		sb.append(" END END ");
	}

	@Override
	public void caseACaseOrSubstitution(ACaseOrSubstitution node) {
		sb.append(" OR ");
		printCommaListCompact(node.getExpressions());
		sb.append(" THEN ");
		node.getSubstitution().apply(this);
	}

	@Override
	public void caseAAnySubstitution(AAnySubstitution node) {
		sb.append("ANY ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" WHERE ");
		node.getWhere().apply(this);
		sb.append(" THEN ");
		node.getThen().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseALetSubstitution(ALetSubstitution node) {
		sb.append("LET ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" BE ");
		node.getPredicate().apply(this);
		sb.append(" IN ");
		node.getSubstitution().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseABecomesElementOfSubstitution(ABecomesElementOfSubstitution node) {
		printCommaListCompact(node.getIdentifiers());
		sb.append("::");
		node.getSet().apply(this);
	}

	@Override
	public void caseABecomesSuchSubstitution(ABecomesSuchSubstitution node) {
		printCommaListCompact(node.getIdentifiers());
		sb.append(" :(");
		node.getPredicate().apply(this);
		sb.append(") ");
	}

	@Override
	public void caseAVarSubstitution(AVarSubstitution node) {
		sb.append("VAR ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" IN ");
		node.getSubstitution().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseASequenceSubstitution(ASequenceSubstitution node) {
		printList(node.getSubstitutions(), " ; ");
	}

	@Override
	public void caseAFuncOpSubstitution(AFuncOpSubstitution node) {
		throw new IllegalArgumentException("OpSubstitutions should have replaced this with AOpSubstitution or ADefinitionSubstitution");
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
			sb.append("<--");
		}
		printDottedIdentifier(node.getOperation());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAWhileSubstitution(AWhileSubstitution node) {
		sb.append("WHILE ");
		node.getCondition().apply(this);
		sb.append(" DO ");
		node.getDoSubst().apply(this);
		sb.append(" INVARIANT ");
		node.getInvariant().apply(this);
		sb.append(" VARIANT ");
		node.getVariant().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseAParallelSubstitution(AParallelSubstitution node) {
		printList(node.getSubstitutions(), " || ");
	}

	@Override
	public void caseADefinitionSubstitution(ADefinitionSubstitution node) {
		node.getDefLiteral().apply(this);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAForallSubMessageSubstitution(AForallSubMessageSubstitution node) {
		sb.append("RULE_FORALL ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" WHERE ");
		node.getWhere().apply(this);
		sb.append(" EXPECT ");
		node.getExpect().apply(this);
		if (node.getErrorType() != null) {
			sb.append(" ERROR_TYPE ");
			sb.append(node.getErrorType().getText());
		}
		sb.append(" COUNTEREXAMPLE ");
		node.getMessage().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseARuleFailSubSubstitution(ARuleFailSubSubstitution node) {
		sb.append("RULE_FAIL ");
		printCommaListCompact(node.getIdentifiers());
		if (node.getWhen() != null) {
			sb.append(" WHEN ");
			node.getWhen().apply(this);
		}
		if (node.getErrorType() != null) {
			sb.append(" ERROR_TYPE ");
			sb.append(node.getErrorType().getText());
		}
		sb.append(" COUNTEREXAMPLE ");
		node.getMessage().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseAForLoopSubstitution(AForLoopSubstitution node) {
		sb.append("FOR ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" IN ");
		node.getSet().apply(this);
		sb.append(" DO ");
		node.getDoSubst().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseAOperatorSubstitution(AOperatorSubstitution node) {
		node.getName().apply(this);
		printParameterList(node.getArguments());
	}

	@Override
	public void caseADefineSubstitution(ADefineSubstitution node) {
		sb.append("DEFINE ");
		node.getName().apply(this);
		sb.append(" TYPE ");
		node.getType().apply(this);
		if (node.getDummyValue() != null) {
			sb.append(" DUMMY_VALUE ");
			node.getDummyValue().apply(this);
		}
		sb.append(" VALUE ");
		node.getValue().apply(this);
		sb.append(" END ");
	}

	@Override
	public void caseTPragmaIdOrString(TPragmaIdOrString node) {
		// Unlike regular TStringLiteral tokens,
		// the quotes (if any) are kept in the token text,
		// so we don't have to re-add them.
		sb.append(node.getText());
	}

	@Override
	public void caseTIdentifierLiteral(final TIdentifierLiteral node) {
		sb.append(this.renaming.renameIdentifier(node.getText()));
	}

	@Override
	public void caseTDefLiteralSubstitution(final TDefLiteralSubstitution node) {
		sb.append(this.renaming.renameIdentifier(node.getText()));
	}

	@Override
	public void caseTDefLiteralPredicate(final TDefLiteralPredicate node) {
		sb.append(this.renaming.renameIdentifier(node.getText()));
	}

	// Rules DSL grammar extension keywords

	@Override
	public void caseTKwSubstitutionOperator(TKwSubstitutionOperator node) {
		sb.append(node.getText());
	}

	@Override
	public void caseTKwPredicateOperator(TKwPredicateOperator node) {
		sb.append(node.getText());
	}

	@Override
	public void caseTKwExpressionOperator(TKwExpressionOperator node) {
		sb.append(node.getText());
	}

	@Override
	public void caseTKwPredicateAttribute(TKwPredicateAttribute node) {
		sb.append(node.getText());
	}

	@Override
	public void caseTKwAttributeIdentifier(TKwAttributeIdentifier node) {
		sb.append(node.getText());
	}

	@Override
	public void defaultCase(final Node node) {
		throw new IllegalArgumentException("Node type not (yet) supported by PrettyPrinter: " + node.getClass());
	}
}
