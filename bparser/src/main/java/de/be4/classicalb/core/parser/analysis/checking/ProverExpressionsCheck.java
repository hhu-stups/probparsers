package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.ParseOptions;
import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.AProverComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.Start;

/**
 * Semantic check for expressions that can only be used in the prover, not
 * standard B machines
 */
public class ProverExpressionsCheck extends OptimizedTraversingAdapter implements SemanticCheck {

	private ParseOptions options;
	private final List<CheckException> exceptions = new ArrayList<>();

	@Override
	public void runChecks(Start rootNode) {
		if (options.isRestrictProverExpressions()) {
			rootNode.apply(this);
		}
	}

	@Override
	public void setOptions(ParseOptions options) {
		this.options = options;
	}

	/* todo: ask Jens */
	@Override
	public void caseAProverComprehensionSetExpression(AProverComprehensionSetExpression node) {
		exceptions.add(new CheckException("SET not allowed in ordinary B files", node));
	}

	@Override
	public List<CheckException> getCheckExceptions() {
		return this.exceptions;
	}

}
