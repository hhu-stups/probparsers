package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.ABecomesElementOfSubstitution;
import de.be4.classicalb.core.parser.node.ABecomesSuchSubstitution;
import de.be4.classicalb.core.parser.node.AComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.AEventBComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.ARecEntry;
import de.be4.classicalb.core.parser.node.ARecordFieldExpression;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.Start;

/**
 * <p>
 * In several constructs the BParser only checks if a list of identifiers is a
 * valid list of expressions instead of checking if each entry is an identifier
 * expression. Thus it excepts to many expressions in these cases.
 * </p>
 * <p>
 * This class finds those constructs and checks if the identifier lists only
 * contain {@link AIdentifierExpression} nodes.
 * </p>
 */
public class IdentListCheck extends OptimizedTraversingAdapter implements SemanticCheck {

	private final Set<Node> nonIdentifiers = new HashSet<>();
	private final List<CheckException> exceptions = new ArrayList<>();

	@Override
	public void runChecks(final Start rootNode) {
		nonIdentifiers.clear();

		rootNode.apply(this);

		if (!nonIdentifiers.isEmpty()) {
			// at least one error was found
			exceptions.add(
					new CheckException("Identifier expected", new ArrayList<>(nonIdentifiers)));
		}
	}

	@Override
	public void inAComprehensionSetExpression(final AComprehensionSetExpression node) {
		checkForNonIdentifiers(node.getIdentifiers());
	}

	@Override
	public void inAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node) {
		checkForNonIdentifiers(node.getIdentifiers());
	}

	@Override
	public void inABecomesSuchSubstitution(final ABecomesSuchSubstitution node) {
		checkForNonIdentifiers(node.getIdentifiers());
	}

	@Override
	public void inABecomesElementOfSubstitution(final ABecomesElementOfSubstitution node) {
		checkForNonIdentifiers(node.getIdentifiers());
	}

	/**
	 * Adds all elements of the {@link List} to {@link #nonIdentifiers} that are
	 * not an instance of {@link AIdentifierExpression}.
	 * 
	 * @param identifiers
	 *            {@link List} to check
	 */
	private void checkForNonIdentifiers(final List<PExpression> identifiers) {
		for (final Iterator<PExpression> iterator = identifiers.iterator(); iterator.hasNext();) {
			final PExpression expression = iterator.next();

			if (!(expression instanceof AIdentifierExpression)) {
				nonIdentifiers.add(expression);
			}
		}
	}

	@Override
	public List<CheckException> getCheckExceptions() {
		return this.exceptions;
	}
}
