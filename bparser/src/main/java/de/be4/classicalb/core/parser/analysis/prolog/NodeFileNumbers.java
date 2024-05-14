package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.WeakHashMap;

import de.be4.classicalb.core.parser.node.Node;

/**
 * <p>Allows assigning file numbers to AST nodes and looking them up later.</p>
 * <p>
 * Unlike {@link NodeIdAssignment}, this class does not assign unique identifiers.
 * This allows a much more efficient implementation, because it does not need to traverse the entire AST and assign an ID to each node.
 * </p>
 */
public final class NodeFileNumbers implements INodeIds {

	private final WeakHashMap<Node, Integer> nodeToFileNumberMap = new WeakHashMap<>();
	
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
		// Find the first node in the parent-chain that has a file number
		Integer existingFileNumber = null;
		Node currentNode = node;
		while (currentNode != null && (existingFileNumber = this.nodeToFileNumberMap.get(currentNode)) == null) {
			currentNode = currentNode.parent();
		}

		// At this point, we have either found a parent node with a file number,
		// or we reached the top of the AST without finding one (in which case existingFileNumber is null).
		final int fileNumber = existingFileNumber == null ? -1 : existingFileNumber;
		
		// To speed up future lookups,
		// add the found file number to the node
		// and to any intermediate parents that also have no file number yet.
		// FIXME: disabled to save memory, but what is the actual performance impact?
		/*Node currentNode2 = node;
		while (currentNode2 != null && !currentNode2.equals(currentNode)) {
			this.nodeToFileNumberMap.put(currentNode2, fileNumber);
			currentNode2 = currentNode2.parent();
		}*/
		
		return fileNumber;
	}
}
