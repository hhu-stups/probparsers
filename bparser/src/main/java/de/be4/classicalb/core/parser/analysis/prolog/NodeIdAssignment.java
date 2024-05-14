package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.Node;

/**
 * This class implements functionality to assign identifiers to all nodes in a
 * syntax tree. Multiple syntax trees can be used with one instance of this
 * class to guarantee unique identifiers.
 * 
 * @deprecated Use {@link NodeFileNumbers} instead,
 *     which only assigns file numbers and not node IDs,
 *     improving performance and memory usage.
 *     There is no replacement for the node ID functionality.
 */
@Deprecated
public class NodeIdAssignment extends DepthFirstAdapter implements INodeIds {

	private final WeakHashMap<Node, Integer> nodeToIdentifierMap = new WeakHashMap<>();
	private final NodeFileNumbers nodeFileNumbers = new NodeFileNumbers();
	private final AtomicInteger currentIdentifier = new AtomicInteger();

	@Override
	public void assignIdentifiers(int fileNumber, Node node) {
		nodeFileNumbers.assignIdentifiers(fileNumber, node);
		node.apply(this);
	}

	@Override
	public Integer lookup(Node node) {
		return nodeToIdentifierMap.get(node);
	}

	@Deprecated
	public Node lookupById(int id) {
		for (Map.Entry<Node, Integer> entry : nodeToIdentifierMap.entrySet()) {
			if (entry.getValue() == id) {
				return entry.getKey();
			}
		}

		throw new AssertionError("Unknown id " + id);
	}

	@Override
	public int lookupFileNumber(Node node) {
		return nodeFileNumbers.lookupFileNumber(node);
	}

	@Override
	public void defaultIn(Node node) {
		nodeToIdentifierMap.put(node, currentIdentifier.getAndIncrement());
	}
}
