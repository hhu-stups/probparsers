package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.VisitorException;
import de.be4.classicalb.core.parser.exceptions.VisitorIOException;
import de.be4.classicalb.core.parser.node.AExtendsMachineClause;
import de.be4.classicalb.core.parser.node.AFileExpression;
import de.be4.classicalb.core.parser.node.AFileMachineReference;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AImplementationMachineParseUnit;
import de.be4.classicalb.core.parser.node.AImportPackage;
import de.be4.classicalb.core.parser.node.AImportsMachineClause;
import de.be4.classicalb.core.parser.node.AIncludesMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.APackageParseUnit;
import de.be4.classicalb.core.parser.node.AReferencesMachineClause;
import de.be4.classicalb.core.parser.node.ARefinementMachineParseUnit;
import de.be4.classicalb.core.parser.node.ASeesMachineClause;
import de.be4.classicalb.core.parser.node.AUsesMachineClause;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PImportPackage;
import de.be4.classicalb.core.parser.node.PMachineReference;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.TPragmaIdOrString;
import de.be4.classicalb.core.parser.util.Utils;

/**
 * This class finds all references to external machines in a machine definition.
 * Use this class by calling the static method
 * {@link #getSetOfReferencedMachines()}.
 * 
 */
public class ReferencedMachines extends MachineClauseAdapter {
	private final File mainFile;
	private final Node start;
	private final boolean isMachineNameMustMatchFileName;
	private final List<String> pathList = new ArrayList<>();
	private String machineName;
	private String packageName;
	private File rootDirectory;
	private final LinkedHashMap<String, MachineReference> referencesTable;

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
	 * @param isMachineNameMustMatchFileName
	 *            indicates if the corresponding check will be performed or not
	 */
	public ReferencedMachines(File machineFile, Node node, boolean isMachineNameMustMatchFileName) {
		this.referencesTable = new LinkedHashMap<>();
		this.mainFile = machineFile;
		this.start = node;
		this.isMachineNameMustMatchFileName = isMachineNameMustMatchFileName;
	}

	public void findReferencedMachines() throws BException {
		String fileName;
		try {
			fileName = mainFile.getCanonicalPath();
		} catch (IOException e) {
			throw new BException(mainFile.getAbsolutePath(), e);
		}
		try {
			this.start.apply(this);
		} catch (VisitorException e) {
			throw new BException(fileName, e.getException());
		}catch(VisitorIOException e) {
			throw new BException(fileName, e.getException());
		}
	}

	/**
	 * Returns all referenced machine names in the given machine
	 * 
	 * @return a set of machine names, never <code>null</code>
	 */
	public Set<String> getSetOfReferencedMachines() {
		return new HashSet<>(referencesTable.keySet());
	}

	public List<String> getPathList() {
		return this.pathList;
	}


	/**
	 * 
	 * @return the name of the machine, <code>null</code> if no name was found
	 */
	public String getName() {
		return machineName;
	}

	public String getPackage() {
		return packageName;
	}

	public Map<String, MachineReference> getReferencesTable() {
		return new HashMap<>(referencesTable);
	}

	public List<MachineReference> getReferences() {
		return new ArrayList<>(this.referencesTable.values());
	}

	@Override
	public void caseAMachineHeader(AMachineHeader node) {
		if (node.getName().isEmpty()) {
			throw new VisitorException(new CheckException("Machine name cannot be empty", node));
		} else if (node.getName().size() > 1) {
			throw new VisitorException(new CheckException("Machine name cannot contain dots", node.getName().get(1)));
		}
		machineName = Utils.getTIdentifierListAsString(node.getName());
		final String fileNameWithoutExtension = Utils.getFileWithoutExtension(mainFile.getName());
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
		final String[] packageArray = determinePackage(node.getPackage(), node);
		final File pathFile = getFileStartingAtRootDirectory(packageArray);
		final String path = pathFile.getAbsolutePath();
		if (pathFile.exists()) {
			if (!pathFile.isDirectory()) {
				throw new VisitorException(
						new CheckException(String.format("Imported package is not a directory: %s", path), node));
			}
		} else {
			throw new VisitorException(
					new CheckException(String.format("Imported package does not exist: %s", path), node));
		}
		if (this.pathList.contains(path)) {
			throw new VisitorException(new CheckException(
					String.format("Duplicate import statement: %s", node.getPackage().getText()), node));
		}
		this.pathList.add(path);
	}

	private void determineRootDirectory(final TPragmaIdOrString packageTerminal, final Node node) {
		final String text = packageTerminal.getText();
		if (Utils.isQuoted(text, '"')) {
			this.packageName = Utils.removeSurroundingQuotes(text, '"');
		} else {
			this.packageName = text;
		}
		final String[] packageNameArray = determinePackage(packageTerminal, node);
		File dir;
		try {
			dir = mainFile.getCanonicalFile();
		} catch (IOException e) {
			throw new VisitorIOException(e);
		}
		for (int i = packageNameArray.length - 1; i >= 0; i--) {
			final String name1 = packageNameArray[i];
			dir = dir.getParentFile();
			final String name2 = dir.getName();
			if (!name1.equals(name2)) {
				throw new VisitorException(new CheckException(
						String.format("Package declaration '%s' does not match the folder structure: %s vs %s",
								this.packageName, name1, name2),
						node));
			}
		}
		rootDirectory = dir.getParentFile();
	}

	private String[] determinePackage(final TPragmaIdOrString packageTerminal, final Node node) {
		String text = packageTerminal.getText();
		// "foo.bar" or foo.bar
		if (Utils.isQuoted(text, '"')) {
			text = Utils.removeSurroundingQuotes(text, '"');
		}
		final String[] packageNameArray = text.split("\\.");
		final Pattern VALID_IDENTIFIER = Pattern.compile("([\\p{L}][\\p{L}\\p{N}_]*)");
		for (int i = 0; i < packageNameArray.length; i++) {
			boolean matches = VALID_IDENTIFIER.matcher(packageNameArray[i]).matches();
			if (!matches) {
				throw new VisitorException(new CheckException("Invalid package pragma: " + text, node));
			}
		}
		return packageNameArray;
	}

	private static MachineReference makeMachineReference(final ReferenceType type, final LinkedList<TIdentifierLiteral> ids, final Node node, final String path) {
		final String name = getIdentifier(ids);
		if (path == null) {
			return new MachineReference(type, name, node);
		} else {
			try {
				return new MachineReference(type, name, node, path);
			} catch (CheckException e) {
				throw new VisitorException(e);
			}
		}
	}

	private void addMachineReference(final ReferenceType type, final LinkedList<TIdentifierLiteral> ids, final Node node, final String path) {
		final MachineReference ref = makeMachineReference(type, ids, node, path);
		this.referencesTable.put(ref.getName(), ref);
	}

	/**
	 * INCLUDES, EXTENDS, IMPORTS, REFERENCES
	 */
	private void addMachineReference(final ReferenceType type, final PMachineReference node) {
		if (node instanceof AFileMachineReference) {
			final AFileMachineReference fileNode = (AFileMachineReference)node;
			final AMachineReference refNode = (AMachineReference)fileNode.getReference();
			String file = fileNode.getFile().getText();
			if (Utils.isQuoted(file, '"')) {
				file = Utils.removeSurroundingQuotes(file, '"');
			}
			addMachineReference(type, refNode.getMachineName(), refNode, file);
		} else if (node instanceof AMachineReference) {
			final AMachineReference refNode = (AMachineReference)node;
			addMachineReference(type, refNode.getMachineName(), refNode, null);
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

	private File getFileStartingAtRootDirectory(String[] array) {
		File f = rootDirectory;
		for (String folder : array) {
			f = new File(f, folder);
		}
		return f;
	}

	private static String getIdentifier(LinkedList<TIdentifierLiteral> list) {
		if (list.size() > 2) {
			// We no longer allow multiple dots inside machine references.
			// The Atelier B reference manual says that at most one dot is allowed in a machine reference,
			// and it's not clear how multiple should be interpreted.
			// ProB previously allowed this and considered all dots except the last one to be part of the renamed machine identifier.
			// For example, "A.B.C.D" was considered a reference to the machine "D", renamed to "A.B.C".
			// This was probably just an oversight though,
			// and it seems that no machines rely on this behavior.
			throw new VisitorException(new CheckException("A machine reference cannot contain more than one dot in the machine identifier", list.get(2)));
		}
		return list.getLast().getText();
	}

	// SEES and USES

	@Override
	public void caseASeesMachineClause(ASeesMachineClause node) {

		registerMachineNames(ReferenceType.SEES, node.getMachineNames());
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node) {

		registerMachineNames(ReferenceType.USES, node.getMachineNames());
	}

	// REFINES
	@Override
	public void caseARefinementMachineParseUnit(ARefinementMachineParseUnit node) {
		node.getHeader().apply(this);
		String name = node.getRefMachine().getText();
		referencesTable.put(name, new MachineReference(ReferenceType.REFINES, name, node.getRefMachine()));

		for (Node mclause : node.getMachineClauses()) {
			mclause.apply(this);

		}
	}

	// IMPLEMENTS
	@Override
	public void caseAImplementationMachineParseUnit(AImplementationMachineParseUnit node) {
		node.getHeader().apply(this);
		String name = node.getRefMachine().getText();
		referencesTable.put(name, new MachineReference(ReferenceType.REFINES, name, node.getRefMachine()));

		for (Node mclause : node.getMachineClauses()) {
			mclause.apply(this);
		}
	}

	private void registerMachineName(ReferenceType type, PExpression machineExpression) {
		if (machineExpression instanceof AIdentifierExpression) {
			AIdentifierExpression identifier = (AIdentifierExpression) machineExpression;
			addMachineReference(type, identifier.getIdentifier(), identifier, null);
		} else if (machineExpression instanceof AFileExpression) {
			final AFileExpression fileNode = (AFileExpression) machineExpression;
			final AIdentifierExpression identifier = (AIdentifierExpression) fileNode.getIdentifier();
			String file = fileNode.getContent().getText();
			if (Utils.isQuoted(file, '"')) {
				file = Utils.removeSurroundingQuotes(file, '"');
			}
			addMachineReference(type, identifier.getIdentifier(), fileNode, file);
		} else {
			throw new AssertionError("Not supported class: " + machineExpression.getClass());
		}
	}
	
	private void registerMachineNames(ReferenceType type, List<PExpression> referencedMachineList) {
		referencedMachineList.forEach(expr -> this.registerMachineName(type, expr));
	}
}
