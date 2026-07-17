package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.File;

/**
 * Contains information about the previous machine that got us to the machine currently being parsed:
 * the name and path of the previous machine and the node inside it that references the current machine.
 */
public class Ancestor {

	private final String name;
	private final File machineFile;
	private final MachineReference machineReference;

	public Ancestor(String name, File machineFile, MachineReference machineReference) {
		this.name = name;
		this.machineFile = machineFile;
		this.machineReference = machineReference;
	}

	public String getName() {
		return name;
	}

	public File getMachineFile() {
		return machineFile;
	}

	public MachineReference getMachineReference() {return machineReference;}
}
