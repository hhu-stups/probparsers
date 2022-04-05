package util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;

/**
 * An extended version of the default {@link DepthFirstAdapter} of SableCC.
 * <p>
 * This version supports depth first traversals on the AST which are aware of
 * lists and children within a node. It offers methods which are called before,
 * between and after visiting elements in lists. Additionally there's a method
 * that is called between visiting each child of a node.
 * <p>
 * See the following methods for details:
 * <ul>
 * <li>{@link #beginList(Node)}</li>
 * <li>{@link #betweenListElements(Node)}</li>
 * <li>{@link #endList(Node)}</li>
 * <li>{@link #betweenChildren(Node)}</li>
 * </ul>
 * <p>
 * An example of usage is the class <code>Ast2String</code> in test project.
 */
@Deprecated
public class ExtendedDFAdapter extends DepthFirstAdapter {

	/**
	 * Called before the first element of a list is visited.
	 * 
	 * @param parent
	 *            The parent {@link Node} of the list.
	 */
	public void beginList(final Node parent) {
		// Do nothing
	}

	/**
	 * Called between each element of a list. This method is only called if more
	 * elements are to be visited, i.e., it's a real "between".
	 * 
	 * @param parent
	 *            The parent {@link Node}.
	 */
	public void betweenListElements(final Node parent) {
		// Do nothing
	}

	/**
	 * Called after a list has completely been visited.
	 * 
	 * @param parent
	 *            The parent {@link Node} of the list.
	 */
	public void endList(final Node parent) {
		// Do nothing
	}

	/**
	 * <p>
	 * If a visited node has more than one child, this method is called between
	 * visiting each child. Although children that are <code>null</code> are not
	 * visited in the traversal, this method is called.
	 * </p>
	 * <p>
	 * Example: Node A is supposed to have two children B and C. Even if C is
	 * <code>null</code> this method will be called after visiting child B.
	 * </p>
	 * <p>
	 * If another behaviour is needed, please reimplement the relevant
	 * <code>caseX</code> methods.
	 * </p>
	 * 
	 * @param parent
	 *            The parent {@link Node}.
	 */
	public void betweenChildren(final Node parent) {
		// Do nothing
	}

	@Override
	public void caseStart(final Start node) {
		inStart(node);
		node.getPParseUnit().apply(this);
		betweenChildren(node);
		node.getEOF().apply(this);
		outStart(node);
	}

	@Override
	public void caseAAbstractMachineParseUnit(final AAbstractMachineParseUnit node) {
		inAAbstractMachineParseUnit(node);
		if (node.getHeader() != null) {
			node.getHeader().apply(this);
		}
		betweenChildren(node);
		{
			final List<PMachineClause> copy = new ArrayList<PMachineClause>(node.getMachineClauses());
			beginList(node);
			for (final Iterator< PMachineClause>iterator = copy.iterator(); iterator.hasNext();) {
				final PMachineClause e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAAbstractMachineParseUnit(node);
	}

	@Override
	public void caseARefinementMachineParseUnit(final ARefinementMachineParseUnit node) {
		inARefinementMachineParseUnit(node);
		if (node.getHeader() != null) {
			node.getHeader().apply(this);
		}
		betweenChildren(node);
		if (node.getRefMachine() != null) {
			node.getRefMachine().apply(this);
		}
		betweenChildren(node);
		{
			final List<PMachineClause> copy = new ArrayList<PMachineClause>(node.getMachineClauses());
			beginList(node);
			for (final Iterator< PMachineClause>iterator = copy.iterator(); iterator.hasNext();) {
				final PMachineClause e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outARefinementMachineParseUnit(node);
	}

	@Override
	public void caseAImplementationMachineParseUnit(final AImplementationMachineParseUnit node) {
		inAImplementationMachineParseUnit(node);
		if (node.getHeader() != null) {
			node.getHeader().apply(this);
		}
		betweenChildren(node);
		if (node.getRefMachine() != null) {
			node.getRefMachine().apply(this);
		}
		betweenChildren(node);
		{
			final List<PMachineClause> copy = new ArrayList<PMachineClause>(node.getMachineClauses());
			beginList(node);
			for (final Iterator< PMachineClause>iterator = copy.iterator(); iterator.hasNext();) {
				final PMachineClause e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAImplementationMachineParseUnit(node);
	}

	@Override
	public void caseAPredicateParseUnit(final APredicateParseUnit node) {
		inAPredicateParseUnit(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		outAPredicateParseUnit(node);
	}

	@Override
	public void caseAExpressionParseUnit(final AExpressionParseUnit node) {
		inAExpressionParseUnit(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAExpressionParseUnit(node);
	}

	@Override
	public void caseASubstitutionParseUnit(final ASubstitutionParseUnit node) {
		inASubstitutionParseUnit(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outASubstitutionParseUnit(node);
	}

	@Override
	public void caseAMachineClauseParseUnit(final AMachineClauseParseUnit node) {
		inAMachineClauseParseUnit(node);
		if (node.getMachineClause() != null) {
			node.getMachineClause().apply(this);
		}
		outAMachineClauseParseUnit(node);
	}

	@Override
	public void caseAMachineHeader(final AMachineHeader node) {
		inAMachineHeader(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getName());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAMachineHeader(node);
	}

	@Override
	public void caseADefinitionsMachineClause(final ADefinitionsMachineClause node) {
		inADefinitionsMachineClause(node);
		{
			final List<PDefinition> copy = new ArrayList<PDefinition>(node.getDefinitions());
			beginList(node);
			for (final Iterator< PDefinition>iterator = copy.iterator(); iterator.hasNext();) {
				final PDefinition e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outADefinitionsMachineClause(node);
	}

	@Override
	public void caseASeesMachineClause(final ASeesMachineClause node) {
		inASeesMachineClause(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getMachineNames());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outASeesMachineClause(node);
	}

	@Override
	public void caseAPromotesMachineClause(final APromotesMachineClause node) {
		inAPromotesMachineClause(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getOperationNames());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAPromotesMachineClause(node);
	}

	@Override
	public void caseAUsesMachineClause(final AUsesMachineClause node) {
		inAUsesMachineClause(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getMachineNames());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAUsesMachineClause(node);
	}

	@Override
	public void caseAIncludesMachineClause(final AIncludesMachineClause node) {
		inAIncludesMachineClause(node);
		{
			final List<PMachineReference> copy = new ArrayList<PMachineReference>(node.getMachineReferences());
			beginList(node);
			for (final Iterator< PMachineReference>iterator = copy.iterator(); iterator.hasNext();) {
				final PMachineReference e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAIncludesMachineClause(node);
	}

	@Override
	public void caseAExtendsMachineClause(final AExtendsMachineClause node) {
		inAExtendsMachineClause(node);
		{
			final List<PMachineReference> copy = new ArrayList<PMachineReference>(node.getMachineReferences());
			beginList(node);
			for (final Iterator< PMachineReference>iterator = copy.iterator(); iterator.hasNext();) {
				final PMachineReference e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAExtendsMachineClause(node);
	}

	@Override
	public void caseAImportsMachineClause(final AImportsMachineClause node) {
		inAImportsMachineClause(node);
		{
			final List<PMachineReference> copy = new ArrayList<PMachineReference>(node.getMachineReferences());
			beginList(node);
			for (final Iterator< PMachineReference>iterator = copy.iterator(); iterator.hasNext();) {
				final PMachineReference e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAImportsMachineClause(node);
	}

	@Override
	public void caseASetsMachineClause(final ASetsMachineClause node) {
		inASetsMachineClause(node);
		{
			final List<PSet> copy = new ArrayList<PSet>(node.getSetDefinitions());
			beginList(node);
			for (final Iterator< PSet>iterator = copy.iterator(); iterator.hasNext();) {
				final PSet e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outASetsMachineClause(node);
	}

	@Override
	public void caseAVariablesMachineClause(final AVariablesMachineClause node) {
		inAVariablesMachineClause(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAVariablesMachineClause(node);
	}

	@Override
	public void caseAConcreteVariablesMachineClause(final AConcreteVariablesMachineClause node) {
		inAConcreteVariablesMachineClause(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAConcreteVariablesMachineClause(node);
	}

	@Override
	public void caseAAbstractConstantsMachineClause(final AAbstractConstantsMachineClause node) {
		inAAbstractConstantsMachineClause(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAAbstractConstantsMachineClause(node);
	}

	@Override
	public void caseAConstantsMachineClause(final AConstantsMachineClause node) {
		inAConstantsMachineClause(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAConstantsMachineClause(node);
	}

	@Override
	public void caseAPropertiesMachineClause(final APropertiesMachineClause node) {
		inAPropertiesMachineClause(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		outAPropertiesMachineClause(node);
	}

	@Override
	public void caseAConstraintsMachineClause(final AConstraintsMachineClause node) {
		inAConstraintsMachineClause(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		outAConstraintsMachineClause(node);
	}

	@Override
	public void caseAInitialisationMachineClause(final AInitialisationMachineClause node) {
		inAInitialisationMachineClause(node);
		if (node.getSubstitutions() != null) {
			node.getSubstitutions().apply(this);
		}
		outAInitialisationMachineClause(node);
	}

	@Override
	public void caseAInvariantMachineClause(final AInvariantMachineClause node) {
		inAInvariantMachineClause(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		outAInvariantMachineClause(node);
	}

	@Override
	public void caseAAssertionsMachineClause(final AAssertionsMachineClause node) {
		inAAssertionsMachineClause(node);
		{
			final List<PPredicate> copy = new ArrayList<PPredicate>(node.getPredicates());
			beginList(node);
			for (final Iterator< PPredicate>iterator = copy.iterator(); iterator.hasNext();) {
				final PPredicate e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAAssertionsMachineClause(node);
	}

	@Override
	public void caseAValuesMachineClause(final AValuesMachineClause node) {
		inAValuesMachineClause(node);
		{
			final List<PValuesEntry> copy = new ArrayList<PValuesEntry>(node.getEntries());
			beginList(node);
			for (final Iterator< PValuesEntry>iterator = copy.iterator(); iterator.hasNext();) {
				final PValuesEntry e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAValuesMachineClause(node);
	}

	@Override
	public void caseALocalOperationsMachineClause(final ALocalOperationsMachineClause node) {
		inALocalOperationsMachineClause(node);
		{
			final List<POperation> copy = new ArrayList<POperation>(node.getOperations());
			beginList(node);
			for (final Iterator< POperation>iterator = copy.iterator(); iterator.hasNext();) {
				final POperation e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outALocalOperationsMachineClause(node);
	}

	@Override
	public void caseAOperationsMachineClause(final AOperationsMachineClause node) {
		inAOperationsMachineClause(node);
		{
			final List<POperation> copy = new ArrayList<POperation>(node.getOperations());
			beginList(node);
			for (final Iterator< POperation>iterator = copy.iterator(); iterator.hasNext();) {
				final POperation e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAOperationsMachineClause(node);
	}

	@Override
	public void caseAMachineReference(final AMachineReference node) {
		inAMachineReference(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getMachineName());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAMachineReference(node);
	}

	@Override
	public void caseAPredicateDefinitionDefinition(final APredicateDefinitionDefinition node) {
		inAPredicateDefinitionDefinition(node);
		if (node.getName() != null) {
			node.getName().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getRhs() != null) {
			node.getRhs().apply(this);
		}
		outAPredicateDefinitionDefinition(node);
	}

	@Override
	public void caseASubstitutionDefinitionDefinition(final ASubstitutionDefinitionDefinition node) {
		inASubstitutionDefinitionDefinition(node);
		if (node.getName() != null) {
			node.getName().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getRhs() != null) {
			node.getRhs().apply(this);
		}
		outASubstitutionDefinitionDefinition(node);
	}

	@Override
	public void caseAExpressionDefinitionDefinition(final AExpressionDefinitionDefinition node) {
		inAExpressionDefinitionDefinition(node);
		if (node.getName() != null) {
			node.getName().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getRhs() != null) {
			node.getRhs().apply(this);
		}
		outAExpressionDefinitionDefinition(node);
	}

	@Override
	public void caseAFileDefinitionDefinition(final AFileDefinitionDefinition node) {
		inAFileDefinitionDefinition(node);
		if (node.getFilename() != null) {
			node.getFilename().apply(this);
		}
		outAFileDefinitionDefinition(node);
	}

	@Override
	public void caseADeferredSetSet(final ADeferredSetSet node) {
		inADeferredSetSet(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getIdentifier());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outADeferredSetSet(node);
	}

	@Override
	public void caseAEnumeratedSetSet(final AEnumeratedSetSet node) {
		inAEnumeratedSetSet(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getIdentifier());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getElements());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAEnumeratedSetSet(node);
	}

	@Override
	public void caseAValuesEntry(final AValuesEntry node) {
		inAValuesEntry(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getIdentifier());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getValue() != null) {
			node.getValue().apply(this);
		}
		outAValuesEntry(node);
	}

	@Override
	public void caseAOperation(final AOperation node) {
		inAOperation(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getReturnValues());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getOpName());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getOperationBody() != null) {
			node.getOperationBody().apply(this);
		}
		outAOperation(node);
	}

	@Override
	public void caseAConjunctPredicate(final AConjunctPredicate node) {
		inAConjunctPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAConjunctPredicate(node);
	}

	@Override
	public void caseANegationPredicate(final ANegationPredicate node) {
		inANegationPredicate(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		outANegationPredicate(node);
	}

	@Override
	public void caseADisjunctPredicate(final ADisjunctPredicate node) {
		inADisjunctPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outADisjunctPredicate(node);
	}

	@Override
	public void caseAImplicationPredicate(final AImplicationPredicate node) {
		inAImplicationPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAImplicationPredicate(node);
	}

	@Override
	public void caseAEquivalencePredicate(final AEquivalencePredicate node) {
		inAEquivalencePredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAEquivalencePredicate(node);
	}

	@Override
	public void caseAForallPredicate(final AForallPredicate node) {
		inAForallPredicate(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getImplication() != null) {
			node.getImplication().apply(this);
		}
		outAForallPredicate(node);
	}

	@Override
	public void caseAExistsPredicate(final AExistsPredicate node) {
		inAExistsPredicate(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		outAExistsPredicate(node);
	}

	@Override
	public void caseAEqualPredicate(final AEqualPredicate node) {
		inAEqualPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAEqualPredicate(node);
	}

	@Override
	public void caseANotEqualPredicate(final ANotEqualPredicate node) {
		inANotEqualPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outANotEqualPredicate(node);
	}

	@Override
	public void caseAMemberPredicate(final AMemberPredicate node) {
		inAMemberPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAMemberPredicate(node);
	}

	@Override
	public void caseANotMemberPredicate(final ANotMemberPredicate node) {
		inANotMemberPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outANotMemberPredicate(node);
	}

	@Override
	public void caseASubsetPredicate(final ASubsetPredicate node) {
		inASubsetPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outASubsetPredicate(node);
	}

	@Override
	public void caseASubsetStrictPredicate(final ASubsetStrictPredicate node) {
		inASubsetStrictPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outASubsetStrictPredicate(node);
	}

	@Override
	public void caseANotSubsetPredicate(final ANotSubsetPredicate node) {
		inANotSubsetPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outANotSubsetPredicate(node);
	}

	@Override
	public void caseANotSubsetStrictPredicate(final ANotSubsetStrictPredicate node) {
		inANotSubsetStrictPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outANotSubsetStrictPredicate(node);
	}

	@Override
	public void caseALessEqualPredicate(final ALessEqualPredicate node) {
		inALessEqualPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outALessEqualPredicate(node);
	}

	@Override
	public void caseALessPredicate(final ALessPredicate node) {
		inALessPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outALessPredicate(node);
	}

	@Override
	public void caseAGreaterEqualPredicate(final AGreaterEqualPredicate node) {
		inAGreaterEqualPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAGreaterEqualPredicate(node);
	}

	@Override
	public void caseAGreaterPredicate(final AGreaterPredicate node) {
		inAGreaterPredicate(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAGreaterPredicate(node);
	}

	@Override
	public void caseADefinitionPredicate(final ADefinitionPredicate node) {
		inADefinitionPredicate(node);
		if (node.getDefLiteral() != null) {
			node.getDefLiteral().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outADefinitionPredicate(node);
	}

	@Override
	public void caseAIdentifierExpression(final AIdentifierExpression node) {
		inAIdentifierExpression(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getIdentifier());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAIdentifierExpression(node);
	}

	@Override
	public void caseAPrimedIdentifierExpression(final APrimedIdentifierExpression node) {
		inAPrimedIdentifierExpression(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getIdentifier());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
			node.getGrade().apply(this);
		}
		outAPrimedIdentifierExpression(node);
	}

	@Override
	public void caseAStringExpression(final AStringExpression node) {
		inAStringExpression(node);
		if (node.getContent() != null) {
			node.getContent().apply(this);
		}
		outAStringExpression(node);
	}

	@Override
	public void caseABooleanTrueExpression(final ABooleanTrueExpression node) {
		inABooleanTrueExpression(node);
		outABooleanTrueExpression(node);
	}

	@Override
	public void caseABooleanFalseExpression(final ABooleanFalseExpression node) {
		inABooleanFalseExpression(node);
		outABooleanFalseExpression(node);
	}

	@Override
	public void caseAIntegerExpression(final AIntegerExpression node) {
		inAIntegerExpression(node);
		if (node.getLiteral() != null) {
			node.getLiteral().apply(this);
		}
		outAIntegerExpression(node);
	}

	@Override
	public void caseAMaxIntExpression(final AMaxIntExpression node) {
		inAMaxIntExpression(node);
		outAMaxIntExpression(node);
	}

	@Override
	public void caseAMinIntExpression(final AMinIntExpression node) {
		inAMinIntExpression(node);
		outAMinIntExpression(node);
	}

	@Override
	public void caseAEmptySetExpression(final AEmptySetExpression node) {
		inAEmptySetExpression(node);
		outAEmptySetExpression(node);
	}

	@Override
	public void caseAIntegerSetExpression(final AIntegerSetExpression node) {
		inAIntegerSetExpression(node);
		outAIntegerSetExpression(node);
	}

	@Override
	public void caseANaturalSetExpression(final ANaturalSetExpression node) {
		inANaturalSetExpression(node);
		outANaturalSetExpression(node);
	}

	@Override
	public void caseANatural1SetExpression(final ANatural1SetExpression node) {
		inANatural1SetExpression(node);
		outANatural1SetExpression(node);
	}

	@Override
	public void caseANatSetExpression(final ANatSetExpression node) {
		inANatSetExpression(node);
		outANatSetExpression(node);
	}

	@Override
	public void caseANat1SetExpression(final ANat1SetExpression node) {
		inANat1SetExpression(node);
		outANat1SetExpression(node);
	}

	@Override
	public void caseAIntSetExpression(final AIntSetExpression node) {
		inAIntSetExpression(node);
		outAIntSetExpression(node);
	}

	@Override
	public void caseABoolSetExpression(final ABoolSetExpression node) {
		inABoolSetExpression(node);
		outABoolSetExpression(node);
	}

	@Override
	public void caseAStringSetExpression(final AStringSetExpression node) {
		inAStringSetExpression(node);
		outAStringSetExpression(node);
	}

	@Override
	public void caseAConvertBoolExpression(final AConvertBoolExpression node) {
		inAConvertBoolExpression(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		outAConvertBoolExpression(node);
	}

	@Override
	public void caseAAddExpression(final AAddExpression node) {
		inAAddExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAAddExpression(node);
	}

	@Override
	public void caseAMinusOrSetSubtractExpression(final AMinusOrSetSubtractExpression node) {
		inAMinusOrSetSubtractExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAMinusOrSetSubtractExpression(node);
	}

	@Override
	public void caseAMinusExpression(final AMinusExpression node) {
		inAMinusExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAMinusExpression(node);
	}

	@Override
	public void caseAUnaryMinusExpression(final AUnaryMinusExpression node) {
		inAUnaryMinusExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAUnaryMinusExpression(node);
	}

	@Override
	public void caseAMultOrCartExpression(final AMultOrCartExpression node) {
		inAMultOrCartExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAMultOrCartExpression(node);
	}

	@Override
	public void caseAMultiplicationExpression(final AMultiplicationExpression node) {
		inAMultiplicationExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAMultiplicationExpression(node);
	}

	@Override
	public void caseACartesianProductExpression(final ACartesianProductExpression node) {
		inACartesianProductExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outACartesianProductExpression(node);
	}

	@Override
	public void caseADivExpression(final ADivExpression node) {
		inADivExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outADivExpression(node);
	}

	@Override
	public void caseAModuloExpression(final AModuloExpression node) {
		inAModuloExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAModuloExpression(node);
	}

	@Override
	public void caseAPowerOfExpression(final APowerOfExpression node) {
		inAPowerOfExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAPowerOfExpression(node);
	}

	@Override
	public void caseASuccessorExpression(final ASuccessorExpression node) {
		inASuccessorExpression(node);
		outASuccessorExpression(node);
	}

	@Override
	public void caseAPredecessorExpression(final APredecessorExpression node) {
		inAPredecessorExpression(node);
		outAPredecessorExpression(node);
	}

	@Override
	public void caseAMaxExpression(final AMaxExpression node) {
		inAMaxExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAMaxExpression(node);
	}

	@Override
	public void caseAMinExpression(final AMinExpression node) {
		inAMinExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAMinExpression(node);
	}

	@Override
	public void caseACardExpression(final ACardExpression node) {
		inACardExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outACardExpression(node);
	}

	@Override
	public void caseAGeneralSumExpression(final AGeneralSumExpression node) {
		inAGeneralSumExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAGeneralSumExpression(node);
	}

	@Override
	public void caseAGeneralProductExpression(final AGeneralProductExpression node) {
		inAGeneralProductExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAGeneralProductExpression(node);
	}

	@Override
	public void caseACoupleExpression(final ACoupleExpression node) {
		inACoupleExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getList());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outACoupleExpression(node);
	}

	@Override
	public void caseAComprehensionSetExpression(final AComprehensionSetExpression node) {
		inAComprehensionSetExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		outAComprehensionSetExpression(node);
	}

	/* todo: check this special case */
	@Override
	public void caseAProverComprehensionSetExpression(final AProverComprehensionSetExpression node) {
		inAProverComprehensionSetExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		outAProverComprehensionSetExpression(node);
	}

	@Override
	public void caseAPowSubsetExpression(final APowSubsetExpression node) {
		inAPowSubsetExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAPowSubsetExpression(node);
	}

	@Override
	public void caseAPow1SubsetExpression(final APow1SubsetExpression node) {
		inAPow1SubsetExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAPow1SubsetExpression(node);
	}

	@Override
	public void caseAFinSubsetExpression(final AFinSubsetExpression node) {
		inAFinSubsetExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAFinSubsetExpression(node);
	}

	@Override
	public void caseAFin1SubsetExpression(final AFin1SubsetExpression node) {
		inAFin1SubsetExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAFin1SubsetExpression(node);
	}

	@Override
	public void caseASetExtensionExpression(final ASetExtensionExpression node) {
		inASetExtensionExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getExpressions());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outASetExtensionExpression(node);
	}

	@Override
	public void caseAIntervalExpression(final AIntervalExpression node) {
		inAIntervalExpression(node);
		if (node.getLeftBorder() != null) {
			node.getLeftBorder().apply(this);
		}
		betweenChildren(node);
		if (node.getRightBorder() != null) {
			node.getRightBorder().apply(this);
		}
		outAIntervalExpression(node);
	}

	@Override
	public void caseAUnionExpression(final AUnionExpression node) {
		inAUnionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAUnionExpression(node);
	}

	@Override
	public void caseAIntersectionExpression(final AIntersectionExpression node) {
		inAIntersectionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAIntersectionExpression(node);
	}

	@Override
	public void caseASetSubtractionExpression(final ASetSubtractionExpression node) {
		inASetSubtractionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outASetSubtractionExpression(node);
	}

	@Override
	public void caseAGeneralUnionExpression(final AGeneralUnionExpression node) {
		inAGeneralUnionExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAGeneralUnionExpression(node);
	}

	@Override
	public void caseAGeneralIntersectionExpression(final AGeneralIntersectionExpression node) {
		inAGeneralIntersectionExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAGeneralIntersectionExpression(node);
	}

	@Override
	public void caseAQuantifiedUnionExpression(final AQuantifiedUnionExpression node) {
		inAQuantifiedUnionExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAQuantifiedUnionExpression(node);
	}

	@Override
	public void caseAQuantifiedIntersectionExpression(final AQuantifiedIntersectionExpression node) {
		inAQuantifiedIntersectionExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicates() != null) {
			node.getPredicates().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAQuantifiedIntersectionExpression(node);
	}

	@Override
	public void caseARelationsExpression(final ARelationsExpression node) {
		inARelationsExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outARelationsExpression(node);
	}

	@Override
	public void caseAIdentityExpression(final AIdentityExpression node) {
		inAIdentityExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAIdentityExpression(node);
	}

	@Override
	public void caseAReverseExpression(final AReverseExpression node) {
		inAReverseExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAReverseExpression(node);
	}

	@Override
	public void caseAFirstProjectionExpression(final AFirstProjectionExpression node) {
		inAFirstProjectionExpression(node);
		if (node.getExp1() != null) {
			node.getExp1().apply(this);
		}
		betweenChildren(node);
		if (node.getExp2() != null) {
			node.getExp2().apply(this);
		}
		outAFirstProjectionExpression(node);
	}

	@Override
	public void caseASecondProjectionExpression(final ASecondProjectionExpression node) {
		inASecondProjectionExpression(node);
		if (node.getExp1() != null) {
			node.getExp1().apply(this);
		}
		betweenChildren(node);
		if (node.getExp2() != null) {
			node.getExp2().apply(this);
		}
		outASecondProjectionExpression(node);
	}

	@Override
	public void caseACompositionExpression(final ACompositionExpression node) {
		inACompositionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outACompositionExpression(node);
	}

	@Override
	public void caseADirectProductExpression(final ADirectProductExpression node) {
		inADirectProductExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outADirectProductExpression(node);
	}

	@Override
	public void caseAParallelProductExpression(final AParallelProductExpression node) {
		inAParallelProductExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAParallelProductExpression(node);
	}

	@Override
	public void caseAIterationExpression(final AIterationExpression node) {
		inAIterationExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAIterationExpression(node);
	}

	@Override
	public void caseAReflexiveClosureExpression(final AReflexiveClosureExpression node) {
		inAReflexiveClosureExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAReflexiveClosureExpression(node);
	}

	@Override
	public void caseAClosureExpression(final AClosureExpression node) {
		inAClosureExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAClosureExpression(node);
	}

	@Override
	public void caseADomainExpression(final ADomainExpression node) {
		inADomainExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outADomainExpression(node);
	}

	@Override
	public void caseARangeExpression(final ARangeExpression node) {
		inARangeExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outARangeExpression(node);
	}

	@Override
	public void caseAImageExpression(final AImageExpression node) {
		inAImageExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAImageExpression(node);
	}

	@Override
	public void caseADomainRestrictionExpression(final ADomainRestrictionExpression node) {
		inADomainRestrictionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outADomainRestrictionExpression(node);
	}

	@Override
	public void caseADomainSubtractionExpression(final ADomainSubtractionExpression node) {
		inADomainSubtractionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outADomainSubtractionExpression(node);
	}

	@Override
	public void caseARangeRestrictionExpression(final ARangeRestrictionExpression node) {
		inARangeRestrictionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outARangeRestrictionExpression(node);
	}

	@Override
	public void caseARangeSubtractionExpression(final ARangeSubtractionExpression node) {
		inARangeSubtractionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outARangeSubtractionExpression(node);
	}

	@Override
	public void caseAOverwriteExpression(final AOverwriteExpression node) {
		inAOverwriteExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAOverwriteExpression(node);
	}

	@Override
	public void caseAPartialFunctionExpression(final APartialFunctionExpression node) {
		inAPartialFunctionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAPartialFunctionExpression(node);
	}

	@Override
	public void caseATotalFunctionExpression(final ATotalFunctionExpression node) {
		inATotalFunctionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outATotalFunctionExpression(node);
	}

	@Override
	public void caseAPartialInjectionExpression(final APartialInjectionExpression node) {
		inAPartialInjectionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAPartialInjectionExpression(node);
	}

	@Override
	public void caseATotalInjectionExpression(final ATotalInjectionExpression node) {
		inATotalInjectionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outATotalInjectionExpression(node);
	}

	@Override
	public void caseAPartialSurjectionExpression(final APartialSurjectionExpression node) {
		inAPartialSurjectionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAPartialSurjectionExpression(node);
	}

	@Override
	public void caseATotalSurjectionExpression(final ATotalSurjectionExpression node) {
		inATotalSurjectionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outATotalSurjectionExpression(node);
	}

	@Override
	public void caseAPartialBijectionExpression(final APartialBijectionExpression node) {
		inAPartialBijectionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAPartialBijectionExpression(node);
	}

	@Override
	public void caseATotalBijectionExpression(final ATotalBijectionExpression node) {
		inATotalBijectionExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outATotalBijectionExpression(node);
	}

	@Override
	public void caseATotalRelationExpression(final ATotalRelationExpression node) {
		inATotalRelationExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outATotalRelationExpression(node);
	}

	@Override
	public void caseASurjectionRelationExpression(final ASurjectionRelationExpression node) {
		inASurjectionRelationExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outASurjectionRelationExpression(node);
	}

	@Override
	public void caseATotalSurjectionRelationExpression(final ATotalSurjectionRelationExpression node) {
		inATotalSurjectionRelationExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outATotalSurjectionRelationExpression(node);
	}

	@Override
	public void caseALambdaExpression(final ALambdaExpression node) {
		inALambdaExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outALambdaExpression(node);
	}

	@Override
	public void caseATransFunctionExpression(final ATransFunctionExpression node) {
		inATransFunctionExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outATransFunctionExpression(node);
	}

	@Override
	public void caseATransRelationExpression(final ATransRelationExpression node) {
		inATransRelationExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outATransRelationExpression(node);
	}

	@Override
	public void caseASeqExpression(final ASeqExpression node) {
		inASeqExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outASeqExpression(node);
	}

	@Override
	public void caseASeq1Expression(final ASeq1Expression node) {
		inASeq1Expression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outASeq1Expression(node);
	}

	@Override
	public void caseAIseqExpression(final AIseqExpression node) {
		inAIseqExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAIseqExpression(node);
	}

	@Override
	public void caseAIseq1Expression(final AIseq1Expression node) {
		inAIseq1Expression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAIseq1Expression(node);
	}

	@Override
	public void caseAPermExpression(final APermExpression node) {
		inAPermExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAPermExpression(node);
	}

	@Override
	public void caseAEmptySequenceExpression(final AEmptySequenceExpression node) {
		inAEmptySequenceExpression(node);
		outAEmptySequenceExpression(node);
	}

	@Override
	public void caseASequenceExtensionExpression(final ASequenceExtensionExpression node) {
		inASequenceExtensionExpression(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getExpression());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outASequenceExtensionExpression(node);
	}

	@Override
	public void caseASizeExpression(final ASizeExpression node) {
		inASizeExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outASizeExpression(node);
	}

	@Override
	public void caseAFirstExpression(final AFirstExpression node) {
		inAFirstExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAFirstExpression(node);
	}

	@Override
	public void caseALastExpression(final ALastExpression node) {
		inALastExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outALastExpression(node);
	}

	@Override
	public void caseAFrontExpression(final AFrontExpression node) {
		inAFrontExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAFrontExpression(node);
	}

	@Override
	public void caseATailExpression(final ATailExpression node) {
		inATailExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outATailExpression(node);
	}

	@Override
	public void caseARevExpression(final ARevExpression node) {
		inARevExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outARevExpression(node);
	}

	@Override
	public void caseAConcatExpression(final AConcatExpression node) {
		inAConcatExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAConcatExpression(node);
	}

	@Override
	public void caseAInsertFrontExpression(final AInsertFrontExpression node) {
		inAInsertFrontExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAInsertFrontExpression(node);
	}

	@Override
	public void caseAInsertTailExpression(final AInsertTailExpression node) {
		inAInsertTailExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outAInsertTailExpression(node);
	}

	@Override
	public void caseARestrictFrontExpression(final ARestrictFrontExpression node) {
		inARestrictFrontExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outARestrictFrontExpression(node);
	}

	@Override
	public void caseARestrictTailExpression(final ARestrictTailExpression node) {
		inARestrictTailExpression(node);
		if (node.getLeft() != null) {
			node.getLeft().apply(this);
		}
		betweenChildren(node);
		if (node.getRight() != null) {
			node.getRight().apply(this);
		}
		outARestrictTailExpression(node);
	}

	@Override
	public void caseAGeneralConcatExpression(final AGeneralConcatExpression node) {
		inAGeneralConcatExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAGeneralConcatExpression(node);
	}

	@Override
	public void caseADefinitionExpression(final ADefinitionExpression node) {
		inADefinitionExpression(node);
		if (node.getDefLiteral() != null) {
			node.getDefLiteral().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outADefinitionExpression(node);
	}

	@Override
	public void caseAFunctionExpression(final AFunctionExpression node) {
		inAFunctionExpression(node);
		if (node.getIdentifier() != null) {
			node.getIdentifier().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAFunctionExpression(node);
	}

	@Override
	public void caseATreeExpression(final ATreeExpression node) {
		inATreeExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outATreeExpression(node);
	}

	@Override
	public void caseABtreeExpression(final ABtreeExpression node) {
		inABtreeExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outABtreeExpression(node);
	}

	@Override
	public void caseAConstExpression(final AConstExpression node) {
		inAConstExpression(node);
		if (node.getExpression1() != null) {
			node.getExpression1().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
		}
		outAConstExpression(node);
	}

	@Override
	public void caseATopExpression(final ATopExpression node) {
		inATopExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outATopExpression(node);
	}

	@Override
	public void caseASonsExpression(final ASonsExpression node) {
		inASonsExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outASonsExpression(node);
	}

	@Override
	public void caseAPrefixExpression(final APrefixExpression node) {
		inAPrefixExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAPrefixExpression(node);
	}

	@Override
	public void caseAPostfixExpression(final APostfixExpression node) {
		inAPostfixExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAPostfixExpression(node);
	}

	@Override
	public void caseASizetExpression(final ASizetExpression node) {
		inASizetExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outASizetExpression(node);
	}

	@Override
	public void caseAMirrorExpression(final AMirrorExpression node) {
		inAMirrorExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAMirrorExpression(node);
	}

	@Override
	public void caseARankExpression(final ARankExpression node) {
		inARankExpression(node);
		if (node.getExpression1() != null) {
			node.getExpression1().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
		}
		outARankExpression(node);
	}

	@Override
	public void caseAFatherExpression(final AFatherExpression node) {
		inAFatherExpression(node);
		if (node.getExpression1() != null) {
			node.getExpression1().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
		}
		outAFatherExpression(node);
	}

	@Override
	public void caseASonExpression(final ASonExpression node) {
		inASonExpression(node);
		if (node.getExpression1() != null) {
			node.getExpression1().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression3() != null) {
			node.getExpression3().apply(this);
		}
		outASonExpression(node);
	}

	@Override
	public void caseASubtreeExpression(final ASubtreeExpression node) {
		inASubtreeExpression(node);
		if (node.getExpression1() != null) {
			node.getExpression1().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
		}
		outASubtreeExpression(node);
	}

	@Override
	public void caseAArityExpression(final AArityExpression node) {
		inAArityExpression(node);
		if (node.getExpression1() != null) {
			node.getExpression1().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
		}
		outAArityExpression(node);
	}

	@Override
	public void caseABinExpression(final ABinExpression node) {
		inABinExpression(node);
		if (node.getExpression1() != null) {
			node.getExpression1().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression2() != null) {
			node.getExpression2().apply(this);
		}
		betweenChildren(node);
		if (node.getExpression3() != null) {
			node.getExpression3().apply(this);
		}
		outABinExpression(node);
	}

	@Override
	public void caseALeftExpression(final ALeftExpression node) {
		inALeftExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outALeftExpression(node);
	}

	@Override
	public void caseARightExpression(final ARightExpression node) {
		inARightExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outARightExpression(node);
	}

	@Override
	public void caseAInfixExpression(final AInfixExpression node) {
		inAInfixExpression(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		outAInfixExpression(node);
	}

	@Override
	public void caseAStructExpression(final AStructExpression node) {
		inAStructExpression(node);
		{
			final List<PRecEntry> copy = new ArrayList<PRecEntry>(node.getEntries());
			beginList(node);
			for (final Iterator< PRecEntry>iterator = copy.iterator(); iterator.hasNext();) {
				final PRecEntry e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAStructExpression(node);
	}

	@Override
	public void caseARecExpression(final ARecExpression node) {
		inARecExpression(node);
		{
			final List<PRecEntry> copy = new ArrayList<PRecEntry>(node.getEntries());
			beginList(node);
			for (final Iterator< PRecEntry>iterator = copy.iterator(); iterator.hasNext();) {
				final PRecEntry e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outARecExpression(node);
	}

	@Override
	public void caseARecordFieldExpression(final ARecordFieldExpression node) {
		inARecordFieldExpression(node);
		if (node.getRecord() != null) {
			node.getRecord().apply(this);
		}
		betweenChildren(node);
		if (node.getIdentifier() != null) {
			node.getIdentifier().apply(this);
		}
		outARecordFieldExpression(node);
	}

	@Override
	public void caseARecEntry(final ARecEntry node) {
		inARecEntry(node);
		if (node.getIdentifier() != null) {
			node.getIdentifier().apply(this);
		}
		betweenChildren(node);
		if (node.getValue() != null) {
			node.getValue().apply(this);
		}
		outARecEntry(node);
	}

	@Override
	public void caseABlockSubstitution(final ABlockSubstitution node) {
		inABlockSubstitution(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outABlockSubstitution(node);
	}

	@Override
	public void caseASkipSubstitution(final ASkipSubstitution node) {
		inASkipSubstitution(node);
		outASkipSubstitution(node);
	}

	@Override
	public void caseAAssignSubstitution(final AAssignSubstitution node) {
		inAAssignSubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getLhsExpression());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getRhsExpressions());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAAssignSubstitution(node);
	}

	@Override
	public void caseAPreconditionSubstitution(final APreconditionSubstitution node) {
		inAPreconditionSubstitution(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		betweenChildren(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outAPreconditionSubstitution(node);
	}

	@Override
	public void caseAAssertionSubstitution(final AAssertionSubstitution node) {
		inAAssertionSubstitution(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		betweenChildren(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outAAssertionSubstitution(node);
	}

	@Override
	public void caseAChoiceSubstitution(final AChoiceSubstitution node) {
		inAChoiceSubstitution(node);
		{
			final List<PSubstitution> copy = new ArrayList<PSubstitution>(node.getSubstitutions());
			beginList(node);
			for (final Iterator< PSubstitution>iterator = copy.iterator(); iterator.hasNext();) {
				final PSubstitution e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAChoiceSubstitution(node);
	}

	@Override
	public void caseAChoiceOrSubstitution(final AChoiceOrSubstitution node) {
		inAChoiceOrSubstitution(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outAChoiceOrSubstitution(node);
	}

	@Override
	public void caseAIfSubstitution(final AIfSubstitution node) {
		inAIfSubstitution(node);
		if (node.getCondition() != null) {
			node.getCondition().apply(this);
		}
		betweenChildren(node);
		if (node.getThen() != null) {
			node.getThen().apply(this);
		}
		betweenChildren(node);
		{
			final List<PSubstitution> copy = new ArrayList<PSubstitution>(node.getElsifSubstitutions());
			beginList(node);
			for (final Iterator< PSubstitution>iterator = copy.iterator(); iterator.hasNext();) {
				final PSubstitution e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getElse() != null) {
			node.getElse().apply(this);
		}
		outAIfSubstitution(node);
	}

	@Override
	public void caseAIfElsifSubstitution(final AIfElsifSubstitution node) {
		inAIfElsifSubstitution(node);
		if (node.getCondition() != null) {
			node.getCondition().apply(this);
		}
		betweenChildren(node);
		if (node.getThenSubstitution() != null) {
			node.getThenSubstitution().apply(this);
		}
		outAIfElsifSubstitution(node);
	}

	@Override
	public void caseASelectSubstitution(final ASelectSubstitution node) {
		inASelectSubstitution(node);
		if (node.getCondition() != null) {
			node.getCondition().apply(this);
		}
		betweenChildren(node);
		if (node.getThen() != null) {
			node.getThen().apply(this);
		}
		betweenChildren(node);
		{
			final List<PSubstitution> copy = new ArrayList<PSubstitution>(node.getWhenSubstitutions());
			beginList(node);
			for (final Iterator< PSubstitution>iterator = copy.iterator(); iterator.hasNext();) {
				final PSubstitution e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getElse() != null) {
			node.getElse().apply(this);
		}
		outASelectSubstitution(node);
	}

	@Override
	public void caseASelectWhenSubstitution(final ASelectWhenSubstitution node) {
		inASelectWhenSubstitution(node);
		if (node.getCondition() != null) {
			node.getCondition().apply(this);
		}
		betweenChildren(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outASelectWhenSubstitution(node);
	}

	@Override
	public void caseACaseSubstitution(final ACaseSubstitution node) {
		inACaseSubstitution(node);
		if (node.getExpression() != null) {
			node.getExpression().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getEitherExpr());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getEitherSubst() != null) {
			node.getEitherSubst().apply(this);
		}
		betweenChildren(node);
		{
			final List<PSubstitution> copy = new ArrayList<PSubstitution>(node.getOrSubstitutions());
			beginList(node);
			for (final Iterator< PSubstitution>iterator = copy.iterator(); iterator.hasNext();) {
				final PSubstitution e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getElse() != null) {
			node.getElse().apply(this);
		}
		outACaseSubstitution(node);
	}

	@Override
	public void caseACaseOrSubstitution(final ACaseOrSubstitution node) {
		inACaseOrSubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getExpressions());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outACaseOrSubstitution(node);
	}

	@Override
	public void caseAAnySubstitution(final AAnySubstitution node) {
		inAAnySubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getWhere() != null) {
			node.getWhere().apply(this);
		}
		betweenChildren(node);
		if (node.getThen() != null) {
			node.getThen().apply(this);
		}
		outAAnySubstitution(node);
	}

	@Override
	public void caseALetSubstitution(final ALetSubstitution node) {
		inALetSubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		betweenChildren(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outALetSubstitution(node);
	}

	@Override
	public void caseABecomesElementOfSubstitution(final ABecomesElementOfSubstitution node) {
		inABecomesElementOfSubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getSet() != null) {
			node.getSet().apply(this);
		}
		outABecomesElementOfSubstitution(node);
	}

	@Override
	public void caseABecomesSuchSubstitution(final ABecomesSuchSubstitution node) {
		inABecomesSuchSubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		outABecomesSuchSubstitution(node);
	}

	@Override
	public void caseAVarSubstitution(final AVarSubstitution node) {
		inAVarSubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		if (node.getSubstitution() != null) {
			node.getSubstitution().apply(this);
		}
		outAVarSubstitution(node);
	}

	@Override
	public void caseASequenceSubstitution(final ASequenceSubstitution node) {
		inASequenceSubstitution(node);
		{
			final List<PSubstitution> copy = new ArrayList<PSubstitution>(node.getSubstitutions());
			beginList(node);
			for (final Iterator< PSubstitution>iterator = copy.iterator(); iterator.hasNext();) {
				final PSubstitution e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outASequenceSubstitution(node);
	}

	@Override
	public void caseAFuncOpSubstitution(final AFuncOpSubstitution node) {
		inAFuncOpSubstitution(node);
		if (node.getFunction() != null) {
			node.getFunction().apply(this);
		}
		outAFuncOpSubstitution(node);
	}

	/* todo : investigate: why this special case */
	@Override
	public void caseAOpSubstitution(final AOpSubstitution node) {
		inAOpSubstitution(node);
		if (node.getName() != null) {
			node.getName().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAOpSubstitution(node);
	}

	@Override
	public void caseAOperationCallSubstitution(final AOperationCallSubstitution node) {
		inAOperationCallSubstitution(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getResultIdentifiers());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(node.getOperation());
			beginList(node);
			for (final Iterator< TIdentifierLiteral>iterator = copy.iterator(); iterator.hasNext();) {
				final TIdentifierLiteral e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAOperationCallSubstitution(node);
	}

	@Override
	public void caseAWhileSubstitution(final AWhileSubstitution node) {
		inAWhileSubstitution(node);
		if (node.getCondition() != null) {
			node.getCondition().apply(this);
		}
		betweenChildren(node);
		if (node.getDoSubst() != null) {
			node.getDoSubst().apply(this);
		}
		betweenChildren(node);
		if (node.getInvariant() != null) {
			node.getInvariant().apply(this);
		}
		betweenChildren(node);
		if (node.getVariant() != null) {
			node.getVariant().apply(this);
		}
		outAWhileSubstitution(node);
	}

	@Override
	public void caseAParallelSubstitution(final AParallelSubstitution node) {
		inAParallelSubstitution(node);
		{
			final List<PSubstitution> copy = new ArrayList<PSubstitution>(node.getSubstitutions());
			beginList(node);
			for (final Iterator< PSubstitution>iterator = copy.iterator(); iterator.hasNext();) {
				final PSubstitution e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outAParallelSubstitution(node);
	}

	@Override
	public void caseADefinitionSubstitution(final ADefinitionSubstitution node) {
		inADefinitionSubstitution(node);
		if (node.getDefLiteral() != null) {
			node.getDefLiteral().apply(this);
		}
		betweenChildren(node);
		{
			final List<PExpression> copy = new ArrayList<PExpression>(node.getParameters());
			beginList(node);
			for (final Iterator< PExpression>iterator = copy.iterator(); iterator.hasNext();) {
				final PExpression e = iterator.next();
				e.apply(this);

				if (iterator.hasNext()) {
					betweenListElements(node);
				}
			}
			endList(node);
		}
		outADefinitionSubstitution(node);
	}

	@Override
	public void caseALabelPredicate(ALabelPredicate node) {
		inALabelPredicate(node);
		if (node.getName() != null) {
			node.getName().apply(this);
		}
		betweenChildren(node);
		if (node.getPredicate() != null) {
			node.getPredicate().apply(this);
		}
		outALabelPredicate(node);
	}
}
