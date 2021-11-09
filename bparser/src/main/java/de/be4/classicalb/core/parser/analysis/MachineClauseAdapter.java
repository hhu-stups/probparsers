package de.be4.classicalb.core.parser.analysis;

import de.be4.classicalb.core.parser.node.AAbstractConstantsMachineClause;
import de.be4.classicalb.core.parser.node.AAssertionsMachineClause;
import de.be4.classicalb.core.parser.node.AConcreteVariablesMachineClause;
import de.be4.classicalb.core.parser.node.AConstantsMachineClause;
import de.be4.classicalb.core.parser.node.AConstraintsMachineClause;
import de.be4.classicalb.core.parser.node.ADefinitionsMachineClause;
import de.be4.classicalb.core.parser.node.AExpressionsMachineClause;
import de.be4.classicalb.core.parser.node.AExtendsMachineClause;
import de.be4.classicalb.core.parser.node.AFreetypesMachineClause;
import de.be4.classicalb.core.parser.node.AImportsMachineClause;
import de.be4.classicalb.core.parser.node.AIncludesMachineClause;
import de.be4.classicalb.core.parser.node.AInitialisationMachineClause;
import de.be4.classicalb.core.parser.node.AInvalidOperationsClauseMachineClause;
import de.be4.classicalb.core.parser.node.AInvariantMachineClause;
import de.be4.classicalb.core.parser.node.ALocalOperationsMachineClause;
import de.be4.classicalb.core.parser.node.AOperationsMachineClause;
import de.be4.classicalb.core.parser.node.APredicatesMachineClause;
import de.be4.classicalb.core.parser.node.APromotesMachineClause;
import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
import de.be4.classicalb.core.parser.node.AReferencesMachineClause;
import de.be4.classicalb.core.parser.node.ASeesMachineClause;
import de.be4.classicalb.core.parser.node.ASetsMachineClause;
import de.be4.classicalb.core.parser.node.AUsesMachineClause;
import de.be4.classicalb.core.parser.node.AValuesMachineClause;
import de.be4.classicalb.core.parser.node.AVariablesMachineClause;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.Switch;

/**
 * <p>
 * Variant of {@link DepthFirstAdapter} that does not traverse machine clauses recursively.
 * Useful to avoid a full depth-first traversal of the whole machine AST
 * when the code only needs information from specific clauses or subnodes.
 * </p>
 * <p>
 * To recursively traverse only some of the clauses while skipping over all others,
 * you can {@link Node#apply(Switch) apply} another adapter
 * inside the {@code caseA...MachineClause} methods
 * of the clauses that you are interested in.
 * </p>
 */
public abstract class MachineClauseAdapter extends DepthFirstAdapter {
	protected MachineClauseAdapter() {
		super();
	}
	
	@Override
	public void caseADefinitionsMachineClause(final ADefinitionsMachineClause node) {}
	
	@Override
	public void caseAConstraintsMachineClause(final AConstraintsMachineClause node) {}
	
	@Override
	public void caseASeesMachineClause(final ASeesMachineClause node) {}
	
	@Override
	public void caseAPromotesMachineClause(final APromotesMachineClause node) {}
	
	@Override
	public void caseAUsesMachineClause(final AUsesMachineClause node) {}
	
	@Override
	public void caseAIncludesMachineClause(final AIncludesMachineClause node) {}
	
	@Override
	public void caseAExtendsMachineClause(final AExtendsMachineClause node) {}
	
	@Override
	public void caseAImportsMachineClause(final AImportsMachineClause node) {}
	
	@Override
	public void caseASetsMachineClause(final ASetsMachineClause node) {}
	
	@Override
	public void caseAConstantsMachineClause(final AConstantsMachineClause node) {}
	
	@Override
	public void caseAAbstractConstantsMachineClause(final AAbstractConstantsMachineClause node) {}
	
	@Override
	public void caseAPropertiesMachineClause(final APropertiesMachineClause node) {}
	
	@Override
	public void caseAConcreteVariablesMachineClause(final AConcreteVariablesMachineClause node) {}
	
	@Override
	public void caseAVariablesMachineClause(final AVariablesMachineClause node) {}
	
	@Override
	public void caseAAssertionsMachineClause(final AAssertionsMachineClause node) {}
	
	@Override
	public void caseAInitialisationMachineClause(final AInitialisationMachineClause node) {}
	
	@Override
	public void caseALocalOperationsMachineClause(final ALocalOperationsMachineClause node) {}
	
	@Override
	public void caseAOperationsMachineClause(final AOperationsMachineClause node) {}
	
	@Override
	public void caseAValuesMachineClause(final AValuesMachineClause node) {}
	
	@Override
	public void caseAInvariantMachineClause(final AInvariantMachineClause node) {}
	
	@Override
	public void caseAFreetypesMachineClause(final AFreetypesMachineClause node) {}
	
	@Override
	public void caseAReferencesMachineClause(final AReferencesMachineClause node) {}
	
	@Override
	public void caseAInvalidOperationsClauseMachineClause(final AInvalidOperationsClauseMachineClause node) {}
	
	@Override
	public void caseAExpressionsMachineClause(final AExpressionsMachineClause node) {}
	
	@Override
	public void caseAPredicatesMachineClause(final APredicatesMachineClause node) {}
}
