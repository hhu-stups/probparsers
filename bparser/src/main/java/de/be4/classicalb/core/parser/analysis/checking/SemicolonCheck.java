package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.ParseOptions;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.AInvalidOperationsClauseMachineClause;
import de.be4.classicalb.core.parser.node.AInvalidSubstitution;
import de.be4.classicalb.core.parser.node.ALocalOperationsMachineClause;
import de.be4.classicalb.core.parser.node.AMissingSemicolonOperation;
import de.be4.classicalb.core.parser.node.AOperationsMachineClause;
import de.be4.classicalb.core.parser.node.Start;

/**
 * This class checks that there is non missing semicolon between two operations.
 */
public class SemicolonCheck implements SemanticCheck {

	private final List<CheckException> exceptions = new ArrayList<>();

	private final class OperationMissingSemicolonWalker extends DepthFirstAdapter {
		@Override
		public void caseAMissingSemicolonOperation(final AMissingSemicolonOperation node) {
			exceptions.add(new CheckException("Semicolon missing between operations", node.getOperation()));
		}

		@Override
		public void caseAInvalidSubstitution(final AInvalidSubstitution node) {
			exceptions.add(new CheckException("Invalid semicolon after last substitution (before END)", node.getSemicolon()));
		}
	}

	private final class MissingSemicolonWalker extends MachineClauseAdapter {
		@Override
		public void caseAInvalidOperationsClauseMachineClause(final AInvalidOperationsClauseMachineClause node) {
			exceptions.add(new CheckException("Invalid semicolon after last operation", node.getSemicolon()));
		}

		@Override
		public void caseAOperationsMachineClause(final AOperationsMachineClause node) {
			node.apply(new OperationMissingSemicolonWalker());
		}

		@Override
		public void caseALocalOperationsMachineClause(final ALocalOperationsMachineClause node) {
			node.apply(new OperationMissingSemicolonWalker());
		}
	}

	@Override
	public void setOptions(ParseOptions options) {
	}

	@Override
	public void runChecks(Start rootNode) {
		MissingSemicolonWalker adapter = new MissingSemicolonWalker();
		rootNode.apply(adapter);
	}

	@Override
	public List<CheckException> getCheckExceptions() {
		return this.exceptions;
	}
}
