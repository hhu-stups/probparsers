package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.node.Node;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

/**
 * PositionPrinters are used to determine the source position of an AST node and
 * to print that information as a Prolog term.
 */
public interface PositionPrinter {
	/**
	 * Sets the {@link PrologTermOutput} instance that should be used to print
	 * the position information
	 * 
	 * @param pout
	 *            The {@link PrologTermOutput}, never <code>null</code>.
	 */
	void setPrologTermOutput(IPrologTermOutput pout);

	/**
	 * Prints the position info of an AST node as exactly one Prolog term. If no
	 * source position can be found for the node, this function should print
	 * something like an atom "none".
	 * 
	 * @param node
	 *            The AST node, never <code>null</code>
	 */
	void printPosition(Node node);

	/**
	 * Prints the position info of a range of AST nodes.
	 * {@code startNode} should come before {@code endNode} in the source code
	 * and both nodes should come from the same AST,
	 * otherwise the printed position info may be incorrect or nonsense.
	 * 
	 * @param startNode the first node in the range
	 * @param endNode the last node in the range
	 */
	default void printPositionRange(Node startNode, Node endNode) {
		// For backwards compatibility only - implementations should override this method if possible
		// (and probably implement printPosition(node) using printPositionRange(node, node), not the other way around).
		// We use endNode instead of startNode as the fallback,
		// because this gives better results for dotted identifiers,
		// which are currently the main use case for this method.
		this.printPosition(endNode);
	}
}
