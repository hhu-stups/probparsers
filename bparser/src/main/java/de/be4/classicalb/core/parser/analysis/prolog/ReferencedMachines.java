package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.FileSearchPathProvider;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.exceptions.*;
import de.be4.classicalb.core.parser.node.AConstraintsMachineClause;
import de.be4.classicalb.core.parser.node.ADefinitionsMachineClause;
import de.be4.classicalb.core.parser.node.AFileExpression;
import de.be4.classicalb.core.parser.node.AFileMachineReference;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AImplementationMachineParseUnit;
import de.be4.classicalb.core.parser.node.AImportPackage;
import de.be4.classicalb.core.parser.node.AInitialisationMachineClause;
import de.be4.classicalb.core.parser.node.AInvariantMachineClause;
import de.be4.classicalb.core.parser.node.ALocalOperationsMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AMachineReference;
import de.be4.classicalb.core.parser.node.AOperationsMachineClause;
import de.be4.classicalb.core.parser.node.APackageParseUnit;
import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
import de.be4.classicalb.core.parser.node.ARefinementMachineParseUnit;
import de.be4.classicalb.core.parser.node.ASeesMachineClause;
import de.be4.classicalb.core.parser.node.AUsesMachineClause;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PImportPackage;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.TPragmaIdOrString;
import de.be4.classicalb.core.parser.util.Utils;

/**
 * This class finds all references to external machines in a machine definition.
 * Use this class by calling the static method
 * {@link #getSetOfReferencedMachines()}.
 * 
 */
public class ReferencedMachines extends DepthFirstAdapter {
	private final File mainFile;
	private final Node start;
	private final boolean isMachineNameMustMatchFileName;
	private final List<String> pathList = new ArrayList<>();
	private final HashMap<String, String> filePathTable = new HashMap<>();
	private String machineName;
	private String packageName;
	private File rootDirectory;
	private final LinkedHashMap<String, MachineReference> referncesTable;
	private final LinkedHashMap<String, MachineReference> siblings;

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
		this.referncesTable = new LinkedHashMap<>();
		this.mainFile = machineFile;
		this.start = node;
		this.isMachineNameMustMatchFileName = isMachineNameMustMatchFileName;
		this.siblings = new LinkedHashMap<>();
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
		return new HashSet<>(referncesTable.keySet());
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
		return new HashMap<>(referncesTable);
	}

	public HashMap<String, MachineReference> getSiblingsTable(){
		return new HashMap<>(siblings);
	}

	public List<MachineReference> getReferences() {
		ArrayList<MachineReference> list = new ArrayList<>();
		for (Entry<String, MachineReference> entry : referncesTable.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}

	public List<MachineReference> getSiblings() {
		ArrayList<MachineReference> list = new ArrayList<>();

		for (Entry<String, MachineReference> entry : siblings.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}

	@Override
	public void caseAMachineHeader(AMachineHeader node) {
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
		if ((text.startsWith("\"") && text.endsWith("\""))) {
			this.packageName = text.replaceAll("\"", "");
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
		if ((text.startsWith("\"") && text.endsWith("\""))) {
			text = text.replaceAll("\"", "");
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

	/**
	 * INCLUDES, EXTENDS, IMPORTS
	 */
	@Override
	public void caseAMachineReference(AMachineReference node) {
		String name = getIdentifier(node.getMachineName());
		if (node.parent() instanceof AFileMachineReference) {
			final AFileMachineReference fileNode = (AFileMachineReference) node.parent();
			String file = fileNode.getFile().getText().replaceAll("\"", "");

			MachineReference ref;
			try {
				ref = new MachineReference(name, node, file);
				referncesTable.put(name, ref);
				siblings.put(name, ref);
			} catch (CheckException e) {
				throw new VisitorException(e);
			}
 	
		} else {

			MachineReference machineReference;
			try {
				String file = findPath(name).getAbsolutePath();
				machineReference = new MachineReference(name, node, file);

			} catch (BCompoundException | CheckException e) {
				machineReference = new MachineReference(name, node);

			}

			if (this.filePathTable.containsKey(name)) {
				machineReference.setDirectoryPath(filePathTable.get(name));
			}
			referncesTable.put(name, machineReference);
			siblings.put(name, machineReference);



		}
	}

	private File getFileStartingAtRootDirectory(String[] array) {
		File f = rootDirectory;
		for (String folder : array) {
			f = new File(f, folder);
		}
		return f;
	}

	private String getIdentifier(LinkedList<TIdentifierLiteral> list) {
		return list.getLast().getText();
	}

	// SEES and USES

	@Override
	public void caseASeesMachineClause(ASeesMachineClause node) {

		registerMachineNames(node.getMachineNames());
	}

	@Override
	public void caseAUsesMachineClause(AUsesMachineClause node) {

		registerMachineNames(node.getMachineNames());
	}

	// REFINES
	@Override
	public void caseARefinementMachineParseUnit(ARefinementMachineParseUnit node) {
		System.out.println("Refines");
		node.getHeader().apply(this);
		String name = node.getRefMachine().getText();
		MachineReference ref = new MachineReference(name, node.getRefMachine());
		MachineReference sibling = new MachineReference(name, node);
		final ARefinementMachineParseUnit siblingNode = (ARefinementMachineParseUnit) node.clone();
		siblingNode.setStartPos(node.getRefMachine().getStartPos());
		siblingNode.setEndPos(node.getRefMachine().getEndPos());
		try {
			File path = findPath(name);
			siblings.put(name, new MachineReference(name, siblingNode, path.getPath()));
			referncesTable.put(name, new MachineReference(name, node.getRefMachine(), path.getPath()));
			filePathTable.put(name, path.getPath());

		} catch (CheckException | BCompoundException e) {
			siblings.put(name, new MachineReference(name, siblingNode));
			referncesTable.put(name, new MachineReference(name, node.getRefMachine()));

		}




		for (Node mclause : node.getMachineClauses()) {
			mclause.apply(this);

		}
	}

	// IMPLEMENTS
	@Override
	public void caseAImplementationMachineParseUnit(AImplementationMachineParseUnit node) {
		node.getHeader().apply(this);
		String name = node.getRefMachine().getText();
		System.out.println("here");
		final AImplementationMachineParseUnit siblingNode = (AImplementationMachineParseUnit) node.clone();
		siblingNode.setStartPos(node.getRefMachine().getStartPos());
		siblingNode.setEndPos(node.getRefMachine().getEndPos());

		try {
			String path = findPath(name).getPath();
			siblings.put(name, new MachineReference(name, siblingNode, path));
			referncesTable.put(name, new MachineReference(name, node.getRefMachine(), path));

		} catch (CheckException | BCompoundException e) {
			siblings.put(name, new MachineReference(name, siblingNode));
			referncesTable.put(name, new MachineReference(name, node.getRefMachine()));

		}

		for (Node mclause : node.getMachineClauses()) {
			mclause.apply(this);
		}
	}

	private void registerMachineNames(List<PExpression> referencedMachineList) {
		for (PExpression machineExpression : referencedMachineList) {

			if (machineExpression instanceof AIdentifierExpression) {

				AIdentifierExpression identifier = (AIdentifierExpression) machineExpression;
				String name = getIdentifier(identifier.getIdentifier());
				final MachineReference machineReference = new MachineReference(name, identifier);

				if (this.filePathTable.containsKey(name)) {
					machineReference.setDirectoryPath(filePathTable.get(name));
				}


				try {
					File path = findPath(name);
					System.out.println("reference is: "+ path);
					MachineReference test = new MachineReference(name, identifier, path.getPath());
					referncesTable.put(name, test);
					siblings.put(name, new MachineReference(name, machineExpression.parent(), path.getPath()));

				} catch (CheckException | BCompoundException e) {
					referncesTable.put(name, new MachineReference(name, identifier));
					siblings.put(name, new MachineReference(name, machineExpression.parent()));

				}


			} else if (machineExpression instanceof AFileExpression) {
				final AFileExpression fileNode = (AFileExpression) machineExpression;
				final AIdentifierExpression identifier = (AIdentifierExpression) fileNode.getIdentifier();
				String file = fileNode.getContent().getText().replaceAll("\"", "");
				String name = getIdentifier(identifier.getIdentifier());
				MachineReference machineReference;
				try {
					machineReference = new MachineReference(name, identifier, file);
					referncesTable.put(name, machineReference);
					siblings.put(name, new MachineReference(name, machineExpression.parent(), file));


				} catch (CheckException e) {
					throw new VisitorException(e);
				}

			} else {
				throw new AssertionError("Not supported class: " + machineExpression.getClass());
			}
		}
	}

	private static final String[] SUFFICES = new String[] { ".ref", ".mch", ".sys", ".imp" };

	private File findPath(String name) throws BCompoundException {
		final List<String> suffixs = Arrays.asList(SUFFICES);

		final List <FileSearchPathProvider> preapedProvider = suffixs.stream()
				.map(suffix ->
						new FileSearchPathProvider(mainFile.getParent(), name + suffix, new ArrayList<>()))
				.collect(Collectors.toList());

		int i = 0;
		File sideResult = null;
		File result = null;
		while(i < preapedProvider.size() && sideResult == null ){
			try{
				sideResult = preapedProvider.get(i).resolve();
			} catch (IOException e) {
				// Result was empty
			}
			if(sideResult!=null){
				result = sideResult;
			}
			i++;
		}



		if(result == null){
			throw new BCompoundException(new BException(name, "File " + "name was not found", null));
		}


		return result;
	}


	/***************************************************************************
	 * exclude large sections of a machine without machine references by doing
	 * nothing
	 */

	@Override
	public void caseAConstraintsMachineClause(AConstraintsMachineClause node) {
		// skip
	}

	@Override
	public void caseAInvariantMachineClause(AInvariantMachineClause node) {
		// skip
	}

	@Override
	public void caseAOperationsMachineClause(AOperationsMachineClause node) {
		// skip
	}

	@Override
	public void caseAPropertiesMachineClause(APropertiesMachineClause node) {
		// skip
	}

	@Override
	public void caseADefinitionsMachineClause(ADefinitionsMachineClause node) {
		// skip
	}

	@Override
	public void caseAInitialisationMachineClause(AInitialisationMachineClause node) {
		// skip
	}

	@Override
	public void caseALocalOperationsMachineClause(ALocalOperationsMachineClause node) {
		// skip
	}
}
