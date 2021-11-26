package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Information about what other machines are referenced by a specific machine.
 * Also contains some other related information,
 * such as the machine's name and import/package information.
 */
public final class ReferencedMachines {
	private final String machineName;
	private final List<MachineReference> references;
	private final PackageName packageName;
	private final File rootPackageDirectory;
	private final Map<PackageName, File> importedPackages;
	
	public ReferencedMachines(final String machineName, final List<MachineReference> references, final PackageName packageName, final File rootPackageDirectory, final Map<PackageName, File> importedPackages) {
		this.machineName = machineName;
		this.references = references;
		this.packageName = packageName;
		this.rootPackageDirectory = rootPackageDirectory;
		this.importedPackages = importedPackages;
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
	public PackageName getPackageName() {
		return this.packageName;
	}
	
	/**
	 * Get the directory of the root package,
	 * or {@code null} if the machine doesn't contain a package pragma.
	 * 
	 * @return directory of the root package
	 */
	public File getRootPackageDirectory() {
		return this.rootPackageDirectory;
	}
	
	/**
	 * Return the names and paths of all packages imported by this machine.
	 * 
	 * @return names and paths of all imported packages
	 */
	public Map<PackageName, File> getImportedPackages() {
		return Collections.unmodifiableMap(this.importedPackages);
	}
}
