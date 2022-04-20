package de.be4.classicalb.core.parser.util;

import java.util.HashMap;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.Node;
import de.hhu.stups.sablecc.patch.SourcePosition;

/**
 * @deprecated Use {@link Node#clone()} instead, which now copies position info.
 */
@Deprecated
public class NodeCloner extends DepthFirstAdapter {
	NodePositionCollector nodeIdSetter;
	Node sourceNode;
	int nodeIdCounter;

	private NodeCloner(Node node) {
		this.nodeIdSetter = new NodePositionCollector();
		this.sourceNode = node;
		this.nodeIdCounter = 0;
		sourceNode.apply(nodeIdSetter);
	}

	/**
	 * @deprecated Use {@link Node#clone()} instead, which now copies position info.
	 */
	@Deprecated
	public static <T extends Node> T cloneNode(T node) {
		@SuppressWarnings("unchecked")
		final T copy = (T)node.clone();
		return copy;
	}

	@Override
	public void defaultIn(Node node) {
		NodePosition nodePosition = nodeIdSetter.getNodePosition(nodeIdCounter);
		node.setStartPos(nodePosition.startPos);
		node.setEndPos(nodePosition.endPos);
		nodeIdCounter++;
	}

}

class NodePositionCollector extends DepthFirstAdapter {
	private int nodeIdCounter;
	private HashMap<Integer, NodePosition> positionMap;

	public NodePosition getNodePosition(int nodeIdCounter) {
		return positionMap.get(nodeIdCounter);
	}

	@Override
	public void defaultIn(Node node) {
		positionMap.put(nodeIdCounter, new NodePosition(node.getStartPos(), node.getEndPos()));
		nodeIdCounter++;
	}

	public NodePositionCollector() {
		positionMap = new HashMap<>();
		nodeIdCounter = 0;
	}
}

class NodePosition {
	SourcePosition startPos;
	SourcePosition endPos;

	public NodePosition(SourcePosition s, SourcePosition e) {
		this.startPos = s;
		this.endPos = e;
	}
}