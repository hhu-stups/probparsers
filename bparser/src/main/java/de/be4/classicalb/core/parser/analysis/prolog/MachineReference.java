package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.File;

import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.util.Utils;

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

	public MachineReference(ReferenceType type, String name, Node node, String path) throws CheckException {
		this(type, name, node);
		this.filePath = path;


		File file = new File(path);
		String baseName = Utils.getFileWithoutExtension(file.getName());
		if (!baseName.equals(name)) {
			throw new CheckException(
					"Declared name in file pragma does not match with the name of the machine referenced: " + name
							+ " vs. " + baseName + path,
					node);
		}
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