package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.Node;

/**
 * This class implements functionality to assign identifiers to all nodes in a
 * syntax tree. Multiple syntax trees can be used with one instance of this
 * class to guarantee unique identifiers.
 */
public class NodeIdAssignment extends DepthFirstAdapter implements INodeIds {
	private final Map<Node, Integer> nodeToIdentifierMap = new HashMap<>();
	private final ArrayList<Node> nodes = new ArrayList<>(1000);
	private int currentIdentifier = 0;

	private int currentFileNumber = -1;
	private final Map<Node, Integer> nodeToFileNumberMap = new HashMap<>();

	@Override
	public void assignIdentifiers(int fileNumber, Node node) {
		if (fileNumber < 1) {
			throw new IllegalArgumentException("File number should be >= 1");
		}
		this.currentFileNumber = fileNumber;
		node.apply(this);
		this.currentFileNumber = -1;
	}

	@Override
	public Integer lookup(Node node) {
		return nodeToIdentifierMap.get(node);
	}

	public Node lookupById(int id) {
		Node result;
		try {
			result = nodes.get(id);
		} catch (IndexOutOfBoundsException e) {
			throw new AssertionError("Unknown id " + id, e);
		}
		if (result == null) {
			throw new AssertionError("Unknown id " + id);
		}
		return result;
	}

	@Override
	public int lookupFileNumber(Node node) {
		Integer fileNumber = nodeToFileNumberMap.get(node);
		return fileNumber == null ? -1 : fileNumber;
	}

	@Override
	public void defaultIn(Node node) {
		synchronized (nodeToIdentifierMap) {
			nodeToIdentifierMap.put(node, currentIdentifier);
			nodes.add(node);
			if (currentFileNumber > 0) {
				nodeToFileNumberMap.put(node, currentFileNumber);
			}
			currentIdentifier++;
		}
	}
}
