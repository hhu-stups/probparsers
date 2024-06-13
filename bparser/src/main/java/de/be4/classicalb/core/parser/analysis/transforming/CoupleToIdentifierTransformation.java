package de.be4.classicalb.core.parser.analysis.transforming;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.node.AComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.ACoupleExpression;
import de.be4.classicalb.core.parser.node.AEventBComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.ASymbolicComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;

/**
 * This transformer will lift identifiers from a couple expression when a list of expressions is required.
 * This can happen in set comprehensions, because the grammar is very lenient to prevent conflicts.
 * <br>
 * Example: {(x,y)·x&lt;y&amp;x&lt;5|x+y} --&gt; {x,y·x&lt;y&amp;x&lt;5|x+y}
 *
 * @see de.be4.classicalb.core.parser.analysis.checking.IdentListCheck
 */
public final class CoupleToIdentifierTransformation extends OptimizedTraversingAdapter {
	private static List<PExpression> tryLift(List<? extends Node> identifiers) {
		if (identifiers.size() == 1) {
			Node child = identifiers.get(0);
			if (child instanceof ACoupleExpression) {
				// this extensive copying is required for the transformation to work correctly
				return new ArrayList<>(((ACoupleExpression) child).clone().getList());
			}
		}
		return null;
	}

	@Override
	public void inAComprehensionSetExpression(AComprehensionSetExpression node) {
		List<PExpression> lifted = tryLift(node.getIdentifiers());
		if (lifted != null) {
			node.setIdentifiers(lifted);
		}
	}

	@Override
	public void inASymbolicComprehensionSetExpression(ASymbolicComprehensionSetExpression node) {
		List<PExpression> lifted = tryLift(node.getIdentifiers());
		if (lifted != null) {
			node.setIdentifiers(lifted);
		}
	}

	@Override
	public void inAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node) {
		List<PExpression> lifted = tryLift(node.getIdentifiers());
		if (lifted != null) {
			node.setIdentifiers(lifted);
		}
	}
}
