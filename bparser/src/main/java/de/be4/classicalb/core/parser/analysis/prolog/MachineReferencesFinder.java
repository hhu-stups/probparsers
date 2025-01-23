package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.VisitorException;
import de.be4.classicalb.core.parser.exceptions.VisitorIOException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.ADefinitionFileParseUnit;
import de.be4.classicalb.core.parser.node.AExtendsMachineClause;
import de.be4.classicalb.core.parser.node.AFileMachineReference;
import de.be4.classicalb.core.parser.node.AFileMachineReferenceNoParams;
import de.be4.classicalb.core.parser.node.AImplementationMachineParseUnit;
import de.be4.classicalb.core.parser.node.AImportPackage;
import de.be4.classicalb.core.parser.node.AImportsMachineClause;
import de.be4.classicalb.core.parser.node.AIncludesMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.AMachineReferenceNoParams;
import de.be4.classicalb.core.parser.node.APackageParseUnit;
import de.be4.classicalb.core.parser.node.AReferencesMachineClause;
import de.be4.classicalb.core.parser.node.ARefinementMachineParseUnit;
import de.be4.classicalb.core.parser.node.ASeesMachineClause;
import de.be4.classicalb.core.parser.node.AUsesMachineClause;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PImportPackage;
import de.be4.classicalb.core.parser.node.PMachineClause;
import de.be4.classicalb.core.parser.node.PMachineReference;
import de.be4.classicalb.core.parser.node.PMachineReferenceNoParams;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.TPragmaIdOrString;
import de.be4.classicalb.core.parser.util.Utils;

/**
 * This class finds all references to external machines in a machine definition.
 * Use this class by calling the static method {@link #findReferencedMachines(Path, Node, boolean)}.
 */
final class MachineReferencesFinder extends MachineClauseAdapter {
	private final Path mainFile;
	private final boolean isMachineNameMustMatchFileName;
	private String machineName;
	private MachineType machineType;
	private PackageName packageName;
	private Path rootDirectory;
	private final Map<PackageName, Path> importedPackages;
	private final List<MachineReference> references;

	private MachineReferencesFinder(Path machineFile, boolean isMachineNameMustMatchFileName) {
		this.references = new ArrayList<>();
		this.mainFile = machineFile;
		this.isMachineNameMustMatchFileName = isMachineNameMustMatchFileName;
		this.importedPackages = new LinkedHashMap<>();
	}

	/**
	 * Searches the syntax tree of a machine for references to external
	 * machines, like in SEES, INCLUDES, etc.
	 * 
	 * @param machineFile
	 *            the file of the parsed B machine. The file will be mainly used
	 *            to report helpful error messages including source code
	 *            positions.
	 * @param node
	 *            the root node of the machine's syntax tree, never
	 *            <code>null</code>
	 * @param machineNameMustMatchFileName
	 *            indicates if the corresponding check will be performed or not
	 * @return information about other machines referenced from the given machine
	 */
	public static ReferencedMachines findReferencedMachines(final Path machineFile, final Node node, final boolean machineNameMustMatchFileName) throws BException {
		final MachineReferencesFinder referenceFinder = new MachineReferencesFinder(machineFile, machineNameMustMatchFileName);
		final String fileName = machineFile.toAbsolutePath().toString();
		try {
			node.apply(referenceFinder);
		} catch (VisitorException e) {
			throw new BException(fileName, e.getException());
		}catch(VisitorIOException e) {
			throw new BException(fileName, e.getException());
		}
		
		if (referenceFinder.machineType == null) {
			throw new BException(fileName, "Could not determine the machine's type. Parse unit class: " + node.getClass(), null);
		}
		
		return new ReferencedMachines(referenceFinder.machineName, referenceFinder.machineType, referenceFinder.references, referenceFinder.packageName, referenceFinder.rootDirectory, referenceFinder.importedPackages);
	}

	@Override
	public void caseAMachineHeader(AMachineHeader node) {
		if (node.getName().isEmpty()) {
			throw new VisitorException(new CheckException("Machine name cannot be empty", node));
		} else if (node.getName().size() > 1) {
			throw new VisitorException(new CheckException("Machine name cannot contain dots", node.getName().get(1)));
		}
		machineName = Utils.getTIdentifierListAsString(node.getName());
		final String fileNameWithoutExtension = Utils.getFileWithoutExtension(mainFile.getFileName().toString());
		if (isMachineNameMustMatchFileName && !machineName.equals(fileNameWithoutExtension)) {
			CheckException ch = new CheckException(
					String.format("Machine name does not match the file name: '%s' vs '%s'", machineName,
							fileNameWithoutExtension),
					node);
			throw new VisitorException(ch);
		}
	}

	@Override
	public void caseAPackageParseUnit(APackageParseUnit node) {
		determineRootDirectory(node.getPackage(), node);
		List<PImportPackage> copy = new ArrayList<>(node.getImports());
		for (PImportPackage e : copy) {
			e.apply(this);
		}
		node.getParseUnit().apply(this);
		// delete this node
		node.replaceBy(node.getParseUnit());
	}

	@Override
	public void caseAImportPackage(AImportPackage node) {
		final PackageName importedPackage = getPackageName(node.getPackage(), node);
		final Path path = importedPackage.getPath(this.rootDirectory);
		if (Files.exists(path)) {
			if (!Files.isDirectory(path)) {
				throw new VisitorException(
						new CheckException(String.format("Imported package is not a directory: %s", path), node));
			}
		} else {
			throw new VisitorException(
					new CheckException(String.format("Imported package does not exist: %s", path), node));
		}
		if (this.importedPackages.containsKey(importedPackage)) {
			throw new VisitorException(new CheckException(
					String.format("Duplicate import statement: %s", node.getPackage().getText()), node));
		}
		this.importedPackages.put(importedPackage, path);
	}

	private void determineRootDirectory(final TPragmaIdOrString packageTerminal, final Node node) {
		this.packageName = getPackageName(packageTerminal, node);
		final Path packageDir;
		try {
			packageDir = mainFile.toRealPath().getParent();
		} catch (IOException e) {
			throw new VisitorIOException(e);
		}
		try {
			rootDirectory = this.packageName.determineRootDirectory(packageDir);
		} catch (IllegalArgumentException e) {
			throw new VisitorException(new CheckException(e.getMessage(), node, e));
		}
	}

	private static PackageName getPackageName(final TPragmaIdOrString packageTerminal, final Node node) {
		try {
			return PackageName.fromPossiblyQuotedName(packageTerminal.getText());
		} catch (IllegalArgumentException e) {
			throw new VisitorException(new CheckException(e.getMessage(), node, e));
		}
	}

	private static MachineReference makeMachineReference(final ReferenceType type, final LinkedList<TIdentifierLiteral> ids, final Node node, final String path) {
		final String name;
		final String renamedName;
		if (ids.size() == 1) {
			name = ids.get(0).getText();
			renamedName = null;
		} else if (ids.size() == 2) {
			name = ids.get(1).getText();
			renamedName = ids.get(0).getText();
		} else {
			// We no longer allow multiple dots inside machine references.
			// The Atelier B reference manual says that at most one dot is allowed in a machine reference,
			// and it's not clear how multiple should be interpreted.
			// ProB previously allowed this and considered all dots except the last one to be part of the renamed machine identifier.
			// For example, "A.B.C.D" was considered a reference to the machine "D", renamed to "A.B.C".
			// This was probably just an oversight though,
			// and it seems that no machines rely on this behavior.
			throw new VisitorException(new CheckException("A machine reference cannot contain more than one dot in the machine identifier", ids.get(2)));
		}

		if (path != null) {
			Path parsedPath = Paths.get(path);
			// Disallow backslashes in relative paths to discourage writing machines that only work on Windows.
			// The portable solution is to use forward slashes, which work on Windows, Mac, and Linux.
			// We still allow backslashes in absolute paths to allow easy copy-pasting of paths on Windows -
			// absolute paths are inherently not portable between systems anyway.
			if (!parsedPath.isAbsolute() && path.contains("\\")) {
				throw new VisitorException(new CheckException(
					"Relative path in file pragma uses backslashes. This is incompatible with non-Windows systems. Please use forward slashes instead.",
					node
				));
			}

			String baseName = Utils.getFileWithoutExtension(parsedPath.getFileName().toString());
			if (!baseName.equals(name)) {
				throw new VisitorException(new CheckException(
					"Declared name in file pragma does not match the machine referenced: " + name + " vs. " + baseName + " in " + path,
					node
				));
			}
		}
		return new MachineReference(type, name, renamedName, node, path);
	}

	/**
	 * For INCLUDES, EXTENDS, IMPORTS, REFERENCES.
	 * Keep this in sync with {@link #addMachineReferenceNoParams(ReferenceType, PMachineReferenceNoParams)} below!
	 */
	private void addMachineReference(final ReferenceType type, final PMachineReference node) {
		if (node instanceof AFileMachineReference) {
			final AFileMachineReference fileNode = (AFileMachineReference)node;
			final AMachineReference refNode = (AMachineReference)fileNode.getReference();
			String file = fileNode.getFile().getText();
			if (Utils.isQuoted(file, '"')) {
				file = Utils.removeSurroundingQuotes(file, '"');
			}
			this.references.add(makeMachineReference(type, refNode.getMachineName(), refNode, file));
		} else if (node instanceof AMachineReference) {
			final AMachineReference refNode = (AMachineReference)node;
			this.references.add(makeMachineReference(type, refNode.getMachineName(), refNode, null));
		} else {
			throw new AssertionError("Unhandled machine reference type: " + node.getClass());
		}
	}

	private void addMachineReferences(final ReferenceType type, final List<? extends PMachineReference> references) {
		references.forEach(ref -> this.addMachineReference(type, ref));
	}

	@Override
	public void caseAIncludesMachineClause(final AIncludesMachineClause node) {
		this.addMachineReferences(ReferenceType.INCLUDES, node.getMachineReferences());
	}

	@Override
	public void caseAExtendsMachineClause(final AExtendsMachineClause node) {
		this.addMachineReferences(ReferenceType.EXTENDS, node.getMachineReferences());
	}

	@Override
	public void caseAImportsMachineClause(final AImportsMachineClause node) {
		this.addMachineReferences(ReferenceType.IMPORTS, node.getMachineReferences());
	}

	@Override
	public void caseAReferencesMachineClause(final AReferencesMachineClause node) {
		this.addMachineReferences(ReferenceType.REFERENCES, node.getMachineReferences());
	}

	/**
	 * For SEES and USES.
	 * Keep this in sync with {@link #addMachineReference(ReferenceType, PMachineReference)} above!
	 */
	private void addMachineReferenceNoParams(final ReferenceType type, final PMachineReferenceNoParams node) {
		if (node instanceof AFileMachineReferenceNoParams) {
			final AFileMachineReferenceNoParams fileNode = (AFileMachineReferenceNoParams)node;
			final AMachineReferenceNoParams refNode = (AMachineReferenceNoParams)fileNode.getReference();
			String file = fileNode.getFile().getText();
			if (Utils.isQuoted(file, '"')) {
				file = Utils.removeSurroundingQuotes(file, '"');
			}
			this.references.add(makeMachineReference(type, refNode.getMachineName(), refNode, file));
		} else if (node instanceof AMachineReferenceNoParams) {
			final AMachineReferenceNoParams refNode = (AMachineReferenceNoParams)node;
			this.references.add(makeMachineReference(type, refNode.getMachineName(), refNode, null));
		} else {
			throw new AssertionError("Unhandled machine reference type: " + node.getClass());
		}
	}

	private void addMachineReferencesNoParams(final ReferenceType type, final List<? extends PMachineReferenceNoParams> references) {
		references.forEach(ref -> this.addMachineReferenceNoParams(type, ref));
	}


	@Override
	public void caseASeesMachineClause(ASeesMachineClause node) {
		this.addMachineReferencesNoParams(ReferenceType.SEES, node.getMachineNames());
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node) {
		this.addMachineReferencesNoParams(ReferenceType.USES, node.getMachineNames());
	}

	// Abstract machine (.mch)
	@Override
	public void caseAAbstractMachineParseUnit(final AAbstractMachineParseUnit node) {
		this.machineType = MachineType.ABSTRACT_MACHINE;
		node.getHeader().apply(this);
		for (final PMachineClause mclause : node.getMachineClauses()) {
			mclause.apply(this);
		}
	}

	// REFINES in REFINEMENT machine (.ref)
	@Override
	public void caseARefinementMachineParseUnit(ARefinementMachineParseUnit node) {
		this.machineType = MachineType.REFINEMENT;
		node.getHeader().apply(this);
		String name = node.getRefMachine().getText();
		references.add(new MachineReference(ReferenceType.REFINES, name, null, node.getRefMachine()));

		for (Node mclause : node.getMachineClauses()) {
			mclause.apply(this);

		}
	}

	// REFINES in IMPLEMENTATION machine (.imp)
	@Override
	public void caseAImplementationMachineParseUnit(AImplementationMachineParseUnit node) {
		this.machineType = MachineType.IMPLEMENTATION;
		node.getHeader().apply(this);
		String name = node.getRefMachine().getText();
		references.add(new MachineReference(ReferenceType.REFINES, name, null, node.getRefMachine()));

		for (Node mclause : node.getMachineClauses()) {
			mclause.apply(this);
		}
	}

	// DEFINITIONS in standalone definition file (.def)
	@Override
	public void caseADefinitionFileParseUnit(final ADefinitionFileParseUnit node) {
		this.machineType = MachineType.DEFINITION_FILE;
	}
}
