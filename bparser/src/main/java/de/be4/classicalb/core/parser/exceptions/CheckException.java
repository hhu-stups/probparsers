package de.be4.classicalb.core.parser.exceptions;

import java.util.Collections;
import java.util.List;

import de.be4.classicalb.core.parser.node.Node;

@SuppressWarnings("serial")
public class CheckException extends Exception {
	private final List<Node> nodes;

	public CheckException(final String message, final List<Node> nodes, final Throwable cause) {
		super(message, cause);
		this.nodes = nodes;
	}

	public CheckException(final String message, final List<Node> nodes) {
		this(message, nodes, null);
	}

	public CheckException(final String message, final Node node, final Throwable cause) {
		this(message, Collections.singletonList(node), cause);
	}

	public CheckException(final String message, final Node node) {
		this(message, Collections.singletonList(node));
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
}
