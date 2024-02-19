package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.ADefinitionExpression;
import de.be4.classicalb.core.parser.node.ADefinitionPredicate;
import de.be4.classicalb.core.parser.node.ADefinitionSubstitution;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.Utils;

public class DefinitionUsageCheck extends OptimizedTraversingAdapter implements SemanticCheck {

	private final IDefinitions definitions;
	private final List<CheckException> exceptions = new ArrayList<>();

	public DefinitionUsageCheck(final IDefinitions definitions) {
		this.definitions = definitions;
	}

	@Override
	public void runChecks(final Start rootNode) {
		// only need to check complete machines
		if (!Utils.isCompleteMachine(rootNode)) {
			return;
		}

		rootNode.apply(this);
	}

	@Override
	public void inADefinitionPredicate(final ADefinitionPredicate node) {
		check(node, node.getParameters().size(), node.getDefLiteral().getText());
	}

	@Override
	public void inADefinitionSubstitution(final ADefinitionSubstitution node) {
		check(node, node.getParameters().size(), node.getDefLiteral().getText());
	}

	@Override
	public void inADefinitionExpression(final ADefinitionExpression node) {
		check(node, node.getParameters().size(), node.getDefLiteral().getText());
	}

	private void check(final Node node, final int paramCount, final String literal) {
		final int expected = definitions.getParameterCount(literal);

		if (paramCount != expected) {
			exceptions.add(new CheckException("Number of parameters (" + paramCount + ") doesn't match declaration of definition " + literal + " (" + expected + ")", node));
		}
	}

	@Override
	public List<CheckException> getCheckExceptions() {
		return this.exceptions;
	}
}
