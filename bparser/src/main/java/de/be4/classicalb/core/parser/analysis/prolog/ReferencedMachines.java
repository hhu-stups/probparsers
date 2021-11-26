package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.Collections;
import java.util.List;

/**
 * Information about what other machines are referenced by a specific machine.
 * Also contains some other related information,
 * such as the machine's name and import/package information.
 */
public final class ReferencedMachines {
	private final String machineName;
	private final List<MachineReference> references;
	private final String packageName;
	private final List<String> pathList;
	
	public ReferencedMachines(final String machineName, final List<MachineReference> references, final String packageName, final List<String> pathList) {
		this.machineName = machineName;
		this.references = references;
		this.packageName = packageName;
		this.pathList = pathList;
	}
	
	/**
	 * Get the name of the machine containing these references.
	 * For DEFINITIONS files (.def), {@code null} is returned.
	 * 
	 * @return name of the machine containing these references
	 */
	public String getMachineName() {
		return this.machineName;
	}
	
	/**
	 * Return information about other machines referenced from this machine
	 * through clauses such as REFINES, EXTENDS, USES, etc.
	 * 
	 * @return information about other machines referenced from this machine
	 */
	public List<MachineReference> getReferences() {
		return Collections.unmodifiableList(this.references);
	}
	
	/**
	 * Return the name of the package in which this machine is located,
	 * or {@code null} if the machine doesn't contain a package pragma.
	 * 
	 * @return name of the package in which this machine is located
	 */
	public String getPackageName() {
		return this.packageName;
	}
	
	/**
	 * Return the file paths of all packages imported by this machine.
	 * 
	 * @return file paths of all imported packages
	 */
	public List<String> getPathList() {
		return Collections.unmodifiableList(pathList);
	}
}
