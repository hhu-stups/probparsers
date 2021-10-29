package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.HashMap;
import java.util.Map;

import de.be4.classicalb.core.parser.node.Node;

/**
 * <p>Allows assigning file numbers to AST nodes and looking them up later.</p>
 * <p>
 * Unlike {@link NodeIdAssignment}, this class does not assign unique identifiers.
 * This allows a much more efficient implementation, because it does not need to traverse the entire AST and assign an ID to each node.
 * </p>
 */
public final class NodeFileNumbers implements INodeIds {
	private final Map<Node, Integer> nodeToFileNumberMap;
	
	public NodeFileNumbers() {
		super();
		this.nodeToFileNumberMap = new HashMap<>();
	}
	
	/**
	 * Assign the given file number to a syntax tree. This implementation does not assign unique identifiers, only file numbers.
	 *
	 * @param fileNumber the file number which will be assigned to {@code node} and its child nodes
	 * @param node the node to which to assign the file number
	 */
	@Override
	public void assignIdentifiers(final int fileNumber, final Node node) {
		this.nodeToFileNumberMap.put(node, fileNumber);
	}
	
	/**
	 * Always returns {@code null}. This implementation does not assign unique identifiers.
	 *
	 * @param node the node of which we want to have the ID
	 * @return {@code null}
	 */
	@Override
	public Integer lookup(final Node node) {
		return null;
	}
	
	@Override
	public int lookupFileNumber(final Node node) {
		return this.nodeToFileNumberMap.getOrDefault(node, -1);
	}
}
