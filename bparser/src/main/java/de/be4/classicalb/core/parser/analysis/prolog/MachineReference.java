package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.node.Node;

public class MachineReference {
	private final ReferenceType type;
	private final String name;
	private final Node node;
	private String filePath;

	public MachineReference(ReferenceType type, String name, Node node) {
		this.type = type;
		this.name = name;
		this.node = node;
	}

	public MachineReference(ReferenceType type, String name, Node node, String path) {
		this(type, name, node);
		this.filePath = path;
	}

	public ReferenceType getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}

	public String getPath() {
		return this.filePath;
	}

	public Node getNode() {
		return this.node;
	}

	@Override
	public String toString() {
		return this.name;
	}
}