package de.be4.classicalb.core.parser.analysis.prolog;

/**
 * Contains the name of the previous machine that we read and that got us to the actual machine
 * Contains the node of the reference that we followed to get to the current state
 */
public class Ancestor {

	private final String name;
	private final MachineReference machineReference;

	public Ancestor(String name, MachineReference machineReference) {
		this.name = name;
		this.machineReference = machineReference;
	}

	public String getName() {
		return name;
	}

	public MachineReference getMachineReference() {return machineReference;}

	@Override
	public String toString() {
		return "---" + machineReference.getType().getDescription() + "--->" + machineReference.getName();
	}
}
