package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.ALocalOperationsMachineClause;
import de.be4.classicalb.core.parser.node.AOperationsMachineClause;
import de.be4.classicalb.core.parser.node.ARefinedOperation;
import de.be4.classicalb.core.parser.node.POperation;
import de.be4.classicalb.core.parser.node.Start;

public class RefinedOperationCheck extends MachineClauseAdapter implements SemanticCheck {
	
	/*
	 * In order to support the following operation definition
	 * OPERATIONS foo(a,b) ref fooAbstract = skip
	 * without introducing a new keyword for 'ref' we allow an arbitrary identifier at the corresponding position.
	 * Hence, this class checks that this identifier corresponds to 'ref'.
	 *
	 */
	
	private final List<CheckException> exceptions = new ArrayList<>();

	@Override
	public void runChecks(Start rootNode) {
		rootNode.apply(this);
	}

	@Override
	public List<CheckException> getCheckExceptions() {
		return this.exceptions;
	}

	private void checkRefKeywords(final List<POperation> operations) {
		for (final POperation operation : operations) {
			if (operation instanceof ARefinedOperation) {
				final ARefinedOperation node = (ARefinedOperation)operation;
				if (!node.getRefKw().getText().equals("ref")) {
					exceptions.add(new CheckException("Expect 'ref' key word in operation definition.", node.getRefKw()));
				}
			}
		}
	}

	@Override
	public void caseAOperationsMachineClause(final AOperationsMachineClause node) {
		checkRefKeywords(node.getOperations());
	}

	@Override
	public void caseALocalOperationsMachineClause(final ALocalOperationsMachineClause node) {
		checkRefKeywords(node.getOperations());
	}

}
