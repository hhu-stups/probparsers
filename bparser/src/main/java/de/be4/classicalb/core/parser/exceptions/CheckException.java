package de.be4.classicalb.core.parser.exceptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.parser.node.Node;

@SuppressWarnings("serial")
public class CheckException extends Exception {
	private final List<Node> nodes;

	public CheckException(final String message, final List<Node> nodes) {
		super(message);
		this.nodes = nodes;
	}

	/**
	 * @deprecated Use {@link #CheckException(String, List)} with a {@link List} argument instead.
	 */
	@Deprecated
	public CheckException(final String message, final Node[] nodes) {
		this(message, Arrays.asList(nodes));
	}

	public CheckException(final String message, final Node node) {
		this(message, Collections.singletonList(node));
	}

	public CheckException(String message, Node aStringExpr, Exception e) {
		super(message, e);
		this.nodes = Collections.singletonList(aStringExpr);

	}

	/**
	 * Returns all {@link Node}s that are relevant for this exception. This can
	 * be a list of all nodes which caused this same {@link CheckException}. In
	 * other cases this can be the list of all nodes which caused this exception
	 * together, e.g. all clauses if multiple are present and only one is
	 * allowed.
	 * 
	 * @return The involved {@link Node} objects.
	 */
	public List<Node> getNodesList() {
		return Collections.unmodifiableList(this.nodes);
	}

	/**
	 * Returns all {@link Node}s that are relevant for this exception. This can
	 * be a list of all nodes which caused this same {@link CheckException}. In
	 * other cases this can be the list of all nodes which caused this exception
	 * together, e.g. all clauses if multiple are present and only one is
	 * allowed.
	 * 
	 * @return The involved {@link Node} objects.
	 * 
	 * @deprecated Use {@link #getNodesList()} instead.
	 */
	@Deprecated
	public Node[] getNodes() {
		return this.getNodesList().toArray(new Node[0]);
	}

	/**
	 * @deprecated Use {@code .getNodesList().get(0)} instead.
	 */
	@Deprecated
	public Node getFirstNode() {
		return this.getNodesList().get(0);
	}
}
