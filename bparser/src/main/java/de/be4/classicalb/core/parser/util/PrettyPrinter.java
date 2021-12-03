package de.be4.classicalb.core.parser.util;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PrettyPrinter extends DepthFirstAdapter {
	private static final Map<Class<? extends Node>, Integer> OPERATOR_PRIORITIES;
	static {
		final Map<Class<? extends Node>, Integer> prio = new HashMap<>();
		prio.put(AParallelProductExpression.class, 20);
		prio.put(AImplicationPredicate.class, 30);
		prio.put(ADisjunctPredicate.class, 40);
		prio.put(AConjunctPredicate.class, 40);
		prio.put(AEquivalencePredicate.class, 60);
		prio.put(ARelationsExpression.class, 125);
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
		prio.put(AMultOrCartExpression.class, 190);
		prio.put(AMultiplicationExpression.class, 190);
		prio.put(ADivExpression.class, 190);
		prio.put(AModuloExpression.class, 190);
		prio.put(APowerOfExpression.class, 200); // right associative
		prio.put(AUnaryMinusExpression.class, 210);
		prio.put(AReverseExpression.class, 230);
		prio.put(AImageExpression.class, 231);
		OPERATOR_PRIORITIES = Collections.unmodifiableMap(prio);
	}

	public PrettyPrinter() {}

	private final StringBuilder sb = new StringBuilder();

	public String getPrettyPrint() {
		return sb.toString();
	}

	private void printList(final List<? extends Node> list, final String separator) {
		for (final Iterator<? extends Node> it = list.iterator(); it.hasNext();) {
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

	private void printParameterList(final List<PExpression> parameters) {
		if (!parameters.isEmpty()) {
			sb.append('(');
			printCommaList(parameters);
			sb.append(')');
		}
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
	public void caseAExpressionDefinitionDefinition(AExpressionDefinitionDefinition node) {
		sb.append(node.getName().getText());
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseAPredicateDefinitionDefinition(APredicateDefinitionDefinition node) {
		sb.append(node.getName().getText());
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseASubstitutionDefinitionDefinition(ASubstitutionDefinitionDefinition node) {
		sb.append(node.getName().getText());
		printParameterList(node.getParameters());
		sb.append(" == ");
		node.getRhs().apply(this);
	}

	@Override
	public void caseASetsMachineClause(ASetsMachineClause node) {
		sb.append("SETS ");
		printSemicolonListSingleLine(node.getSetDefinitions());
		sb.append("\n");
	}

	@Override
	public void caseAPropertiesMachineClause(APropertiesMachineClause node) {
		sb.append("PROPERTIES\n");
		node.getPredicates().apply(this);
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
	public void caseAIncludesMachineClause(AIncludesMachineClause node) {
		sb.append("INCLUDES ");
		printCommaList(node.getMachineReferences());
		sb.append("\n");
	}
	@Override
	public void caseASeesMachineClause(ASeesMachineClause node) {
		sb.append("SEES ");
		printCommaList(node.getMachineNames());
		sb.append("\n");
	}
	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node) {
		sb.append("USES ");
		printCommaList(node.getMachineNames());
		sb.append("\n");
	}
	@Override
	public void caseAImportsMachineClause(AImportsMachineClause node) {
		sb.append("IMPORTS ");
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
	public void caseAPromotesMachineClause(APromotesMachineClause node) {
		sb.append("PROMOTES ");
		printCommaList(node.getOperationNames());
		sb.append("\n");
	}

	@Override
	public void caseAAssertionsMachineClause(AAssertionsMachineClause node) {
		sb.append("ASSERTIONS\n");
		printSemicolonListSingleLine(node.getPredicates());
		sb.append("\n");
	}

	@Override
	public void caseAInvariantMachineClause(AInvariantMachineClause node) {
		sb.append("INVARIANT ");
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
	public void caseAOperationsMachineClause(AOperationsMachineClause node) {
		sb.append("OPERATIONS\n");
		printSemicolonList(node.getOperations());
		sb.append("\n");
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
	public void caseAAssignSubstitution(AAssignSubstitution node) {
		printCommaListCompact(node.getLhsExpression());
		sb.append(" := ");
		printCommaListCompact(node.getRhsExpressions());
	}

	@Override
	public void caseASkipSubstitution(ASkipSubstitution node) {
		sb.append("skip");
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
	public void caseAParallelSubstitution(AParallelSubstitution node) {
		printList(node.getSubstitutions(), " || ");
	}

	@Override
	public void caseASequenceSubstitution(ASequenceSubstitution node) {
		printList(node.getSubstitutions(), " ; ");
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
	public void caseAVarSubstitution(AVarSubstitution node) {
		sb.append("VAR ");
		printCommaListCompact(node.getIdentifiers());
		sb.append(" IN ");
		node.getSubstitution().apply(this);
		sb.append(" END ");
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
	public void caseASelectWhenSubstitution(ASelectWhenSubstitution node) {
		sb.append(" WHEN ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getSubstitution().apply(this);
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
	public void caseAIfElsifSubstitution(AIfElsifSubstitution node) {
		sb.append(" ELSIF ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThenSubstitution().apply(this);
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
	public void caseACaseOrSubstitution(ACaseOrSubstitution node) {
		sb.append(" OR ");
		printCommaListCompact(node.getExpressions());
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
	public void caseAIfThenElseExpression(AIfThenElseExpression node) {
		sb.append("IF ");
		node.getCondition().apply(this);
		sb.append(" THEN ");
		node.getThen().apply(this);
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

	public void leftParAssoc(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		// we do not insert parentheses when priority is the same
		if (priorityNode != null && priorityRight != null && priorityRight < priorityNode) {
			sb.append("(");
		}
	}

	public void rightParAssoc(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight < priorityNode) {
			sb.append(")");
		}
	}

	public void leftPar(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight <= priorityNode) {
			sb.append("(");
		}
	}

	public void rightPar(final Node node, final Node right) {
		Integer priorityNode = OPERATOR_PRIORITIES.get(node.getClass());
		Integer priorityRight = OPERATOR_PRIORITIES.get(right.getClass());
		if (priorityNode != null && priorityRight != null && priorityRight <= priorityNode) {
			sb.append(")");
		}
	}

	public void applyLeftAssociative(final Node left, final Node node, final Node right, final String operatorStr) {
		leftParAssoc(node, left);
		left.apply(this);
		rightParAssoc(node, left);

		sb.append(operatorStr);

		leftPar(node, right);
		right.apply(this);
		rightPar(node, right);
	}

	public void applyRightAssociative(final Node left, final Node node, final Node right, final String operatorStr) {
		leftPar(node, left);
		left.apply(this);
		rightPar(node, left);

		sb.append(operatorStr);

		leftParAssoc(node, right);
		right.apply(this);
		rightParAssoc(node, right);
	}

	@Override
	public void caseAPowerOfExpression(final APowerOfExpression node) {
		applyRightAssociative(node.getLeft(), node, node.getRight(), "**");
	}

	@Override
	public void caseAIntegerExpression(final AIntegerExpression node) {
		sb.append(node.getLiteral().getText());
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
	public void caseASetSubtractionExpression(final ASetSubtractionExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "\\");
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
	public void caseAModuloExpression(final AModuloExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " mod ");
	}

	@Override
	public void caseARelationsExpression(final ARelationsExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<->");
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
	public void caseAOverwriteExpression(final AOverwriteExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<+");
	}

	@Override
	public void caseADirectProductExpression(final ADirectProductExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "><");
	}

	@Override
	public void caseAConcatExpression(final AConcatExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "^");
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
	public void caseAInsertFrontExpression(final AInsertFrontExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "->");
	}

	@Override
	public void caseAInsertTailExpression(final AInsertTailExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<-");
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
	public void caseARestrictFrontExpression(final ARestrictFrontExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/|\\");
	}

	@Override
	public void caseARestrictTailExpression(final ARestrictTailExpression node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "\\|/");
	}



	@Override
	public void caseACoupleExpression(final ACoupleExpression node) {
		sb.append("(");
		node.getList().get(0).apply(this);
		sb.append(",");
		node.getList().get(1).apply(this);
		sb.append(")");
	}

	@Override
	public void caseAIdentifierExpression(final AIdentifierExpression node) {
		printDottedIdentifier(node.getIdentifier());
	}
	@Override
	public void caseAMachineReference(final AMachineReference node) {
		printDottedIdentifier(node.getMachineName());
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAIntervalExpression(final AIntervalExpression node) {
		applyLeftAssociative(node.getLeftBorder(), node, node.getRightBorder(), "..");
	}

	@Override
	public void caseAUnaryMinusExpression(final AUnaryMinusExpression node) {
		sb.append("-");
		node.getExpression().apply(this);
	}

	@Override
	public void caseAReverseExpression(final AReverseExpression node) {
		node.getExpression().apply(this);
		sb.append("~");
	}

	@Override
	public void caseAImageExpression(final AImageExpression node) {
		node.getLeft().apply(this);
		sb.append("[");
		node.getRight().apply(this);
		sb.append("]");

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
	public void caseACompositionExpression(final ACompositionExpression node) {
		sb.append("(");
		node.getLeft().apply(this);
		sb.append(";");
		node.getRight().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAConvertBoolExpression(final AConvertBoolExpression node) {
		sb.append("bool(");
		node.getPredicate().apply(this);
		sb.append(")");
	}

	@Override
	public void caseALessPredicate(final ALessPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<");
	}

	@Override
	public void caseAMaxExpression(final AMaxExpression node) {
		sb.append("max(");
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
	public void caseASymbolicCompositionExpression(ASymbolicCompositionExpression node) {
		node.getLeft().apply(this);
		sb.append(" /*@symbolic*/ ");
		sb.append(";");
		node.getRight().apply(this);
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
	public void caseAConjunctPredicate(final AConjunctPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " & ");
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
	public void caseAIdentityExpression(final AIdentityExpression node) {
		sb.append("id(");
		node.getExpression().apply(this);
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
	public void caseAFirstProjectionExpression(final AFirstProjectionExpression node) {
		sb.append("prj1(");
		node.getExp1().apply(this);
		sb.append(",");
		node.getExp2().apply(this);
		sb.append(")");
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
	public void caseAIterationExpression(final AIterationExpression node) {
		sb.append("iterate(");
		node.getLeft().apply(this);
		sb.append(",");
		node.getRight().apply(this);
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
	public void caseTIdentifierLiteral(final TIdentifierLiteral node) {
		final String identifier = node.getText();
		if (Utils.isPlainBIdentifier(identifier)) {
			sb.append(identifier);
		} else {
			sb.append('`');
			sb.append(Utils.escapeStringContents(identifier));
			sb.append('`');
		}
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
	public void caseASequenceExtensionExpression(final ASequenceExtensionExpression node) {
		sb.append("[");
		printCommaListCompact(node.getExpression());
		sb.append("]");
	}

	@Override
	public void caseAGeneralConcatExpression(final AGeneralConcatExpression node) {
		sb.append("conc(");
		node.getExpression().apply(this);
		sb.append(")");
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
	public void caseAImplicationPredicate(final AImplicationPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " => ");
	}

	@Override
	public void caseADisjunctPredicate(final ADisjunctPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " or ");
	}

	@Override
	public void caseAEquivalencePredicate(final AEquivalencePredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), " <=> ");

	}

	@Override
	public void caseAEqualPredicate(final AEqualPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "=");
	}

	@Override
	public void caseAMemberPredicate(final AMemberPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), ":");
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
	public void caseANotEqualPredicate(final ANotEqualPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/=");
	}

	@Override
	public void caseANotMemberPredicate(final ANotMemberPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "/:");

	}

	@Override
	public void caseALessEqualPredicate(final ALessEqualPredicate node) {
		applyLeftAssociative(node.getLeft(), node, node.getRight(), "<=");

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
	public void caseANegationPredicate(final ANegationPredicate node) {
		sb.append("not(");
		node.getPredicate().apply(this);
		sb.append(")");
	}

	@Override
	public void caseAStringExpression(final AStringExpression node) {
		sb.append("\"");
		sb.append(Utils.escapeStringContents(node.getContent().getText()));
		sb.append("\"");

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
	public void caseADefinitionExpression(final ADefinitionExpression node) {
		String defLiteral = node.getDefLiteral().getText();
		sb.append(defLiteral);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseADefinitionPredicate(final ADefinitionPredicate node) {
		String defLiteral = node.getDefLiteral().getText();
		sb.append(defLiteral);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseADefinitionSubstitution(ADefinitionSubstitution node) {
		String defLiteral = node.getDefLiteral().getText();
		sb.append(defLiteral);
		printParameterList(node.getParameters());
	}

	@Override
	public void caseAFunctionExpression(final AFunctionExpression node) {
		node.getIdentifier().apply(this);
		printParameterList(node.getParameters());
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
	public void caseARecEntry(final ARecEntry node) {
		node.getIdentifier().apply(this);
		sb.append(":");
		node.getValue().apply(this);
	}

	@Override
	public void caseARecordFieldExpression(final ARecordFieldExpression node) {
		node.getRecord().apply(this);
		sb.append("'");
		node.getIdentifier().apply(this);
	}

	@Override
	public void caseAEnumeratedSetSet(final AEnumeratedSetSet node) {
		printDottedIdentifier(node.getIdentifier());
		sb.append("={");
		printCommaListCompact(node.getElements());
		sb.append("}");
	}

}
