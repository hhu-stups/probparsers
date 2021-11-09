package de.be4.classicalb.core.parser.analysis;

import de.be4.classicalb.core.parser.node.AConjunctPredicate;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PPredicate;

/**
 * <p>
 * Optimized version of {@link DepthFirstAdapter}
 * that reduces the recursion depth when traversing certain combinations of nodes,
 * to avoid stack overflows.
 * Specifically, this avoids recursion when traversing structures like {@code T(T(...), ...)} and {@code T(..., T(...))},
 * where {@code T} is a binary node such as {@link AConjunctPredicate}.
 * </p>
 * <p>
 * <strong>WARNING:</strong> These optimizations change the node traversal order in some cases,
 * making it sometimes not depth-first
 * or not following the normal order of the nodes in the tree.
 * <em>This can be unsafe</em> depending on the adapter's other actions,
 * so this optimized version should only be used for adapters
 * that don't depend on traversal order
 * (or at least not for the node types affected by the optimizations).
 * </p>
 * <p>
 * This class is <em>for internal use only</em> by the B parser.
 * External users of the B parser should not use this class directly.
 * </p>
 */
public abstract class OptimizedTraversingAdapter extends DepthFirstAdapter {
	protected OptimizedTraversingAdapter() {
		super();
	}
	
	/**
	 * Not safe to use with {@link OptimizedTraversingAdapter}.
	 * It is not guaranteed that in/out methods will be called for optimized node types.
	 * 
	 * @param node The node that will be traversed.
	 */
	@Override
	public final void defaultIn(final Node node) {}
	
	/**
	 * Not safe to use with {@link OptimizedTraversingAdapter}.
	 * It is not guaranteed that in/out methods will be called for optimized node types.
	 * 
	 * @param node The node that was traversed.
	 */
	@Override
	public final void defaultOut(final Node node) {}
	
	/**
	 * Not safe to use with {@link OptimizedTraversingAdapter}.
	 * It is not guaranteed that in/out methods will be called for optimized node types.
	 *
	 * @param node The node that will be traversed.
	 */
	@Override
	public final void inAConjunctPredicate(final AConjunctPredicate node) {}
	
	/**
	 * Not safe to use with {@link OptimizedTraversingAdapter}.
	 * It is not guaranteed that in/out methods will be called for optimized node types.
	 *
	 * @param node The node that was traversed.
	 */
	@Override
	public final void outAConjunctPredicate(final AConjunctPredicate node) {}
	
	/**
	 * Optimized version that avoids recursive calls
	 * when exactly one of the two operands of {@code node} is another conjunction.
	 *
	 * @param node The node to traverse.
	 */
	@Override
	public void caseAConjunctPredicate(final AConjunctPredicate node) {
		AConjunctPredicate currentNode = node;
		while (true) {
			final PPredicate left = currentNode.getLeft();
			final PPredicate right = currentNode.getRight();
			if (right instanceof AConjunctPredicate) {
				left.apply(this);
				currentNode = (AConjunctPredicate)right;
			} else if (left instanceof AConjunctPredicate) {
				// This traverses in reverse order (right side first).
				right.apply(this);
				currentNode = (AConjunctPredicate)left;
			} else {
				super.caseAConjunctPredicate(currentNode);
				break;
			}
		}
	}
}
