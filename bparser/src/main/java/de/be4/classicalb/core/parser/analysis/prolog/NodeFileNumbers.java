package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.WeakHashMap;

import de.be4.classicalb.core.parser.node.Node;

/**
 * <p>Allows assigning file numbers to AST nodes and looking them up later.</p>
 */
public final class NodeFileNumbers implements INodeIds {
	private static final int PARENT_CACHE_DISTANCE = 8;

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
	
	@Override
	public int lookupFileNumber(final Node node) {
		// Find the first node in the parent-chain that has a file number
		Integer existingFileNumber = null;
		Node currentNode = node;
		int distance = 0;
		while (currentNode != null && (existingFileNumber = this.nodeToFileNumberMap.get(currentNode)) == null) {
			currentNode = currentNode.parent();
			distance++;
		}

		// At this point, we have either found a parent node with a file number,
		// or we reached the top of the AST without finding one (in which case existingFileNumber is null).
		final int fileNumber = existingFileNumber == null ? -1 : existingFileNumber;

		// If it took more than a few steps to find a parent with a file number,
		// add the found file number to some of the intermediate parents
		// to speed up future lookups.
		// This is very important for deeply nested ASTs:
		// for example, without this optimization,
		// printing the Prolog AST for public_examples/B/PerformanceTests/Generated/Generated100_Rep400.mch
		// takes 16.5 seconds, compared to less than 0.2 seconds with this optimization.
		// However, this caching consumes some additional memory,
		// so we only do it for some intermediate nodes and not all of them.
		if (distance >= PARENT_CACHE_DISTANCE) {
			Node currentNode2 = node;
			int distance2 = 0;
			while (currentNode2 != null && !currentNode2.equals(currentNode)) {
				distance2++;
				if (distance2 == PARENT_CACHE_DISTANCE) {
					this.nodeToFileNumberMap.put(currentNode2, fileNumber);
					distance2 = 0;
				}
				currentNode2 = currentNode2.parent();
			}
		}

		return fileNumber;
	}
}
