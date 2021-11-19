package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.*;
import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.*;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


/**
 * TODO check for multiple Definition files
 * <p>
 * This class implements the functionality to load and parse all machines that
 * are referenced in any other loaded machine.
 * <p>
 * Cyclic references are detected. If an error occurs in an external machine,
 * the error message is mapped to the uses/includes/etc. statement in the main
 * machine.
 */
public class RecursiveMachineLoader {
	private static final String[] SUFFICES = new String[]{".ref", ".mch", ".sys", ".imp"};
	private final File rootDirectory;
	private final INodeIds nodeIds;
	private final Map<String, Start> parsedMachines = new TreeMap<>();
	private final Map<String, File> parsedFiles = new TreeMap<>();
	private final List<File> machineFilesLoaded = new ArrayList<>();
	private final IFileContentProvider contentProvider;
	private final ParsingBehaviour parsingBehaviour;
	private String main;


	public RecursiveMachineLoader(final String directory, final IDefinitionFileProvider contentProvider,
								  ParsingBehaviour parsingBehaviour) throws BCompoundException {
		this.parsingBehaviour = parsingBehaviour;
		this.rootDirectory = directory == null ? new File(".") : new File(directory);

		if (!rootDirectory.exists()) {
			throw new BCompoundException(
					new BException(null, new IOException("Directory does not exist: " + directory)));
		}

		// In the compact position format, node IDs are not used,
		// so generate them only if the old non-compact format is requested.
		if (parsingBehaviour.isCompactPrologPositions()) {
			this.nodeIds = new NodeFileNumbers();
		} else {
			this.nodeIds = new NodeIdAssignment();
		}

		this.contentProvider = contentProvider;
	}

	public RecursiveMachineLoader(String path, IDefinitionFileProvider contentProvider) throws BCompoundException {
		this(path, contentProvider, new ParsingBehaviour());
	}

	/**
	 * Recursively parse any files referenced by the given already parsed main machine.
	 *
	 * @param parser the {@link BParser} instance that was used to parse the main machine - used for definitions and file name information
	 * @param ast the parsed AST of the main machine
	 * @param parsingBehaviour options controlling the behaviour of {@link RecursiveMachineLoader}
	 * @param contentProvider controls how files referenced by the main file are read
	 * @return a new {@link RecursiveMachineLoader} that has parsed all files referenced by the main machine
	 * @throws BCompoundException if parsing fails in any way
	 */
	public static RecursiveMachineLoader loadFromAst(final BParser parser, final Start ast, final ParsingBehaviour parsingBehaviour, final IDefinitionFileProvider contentProvider) throws BCompoundException {
		final File mainFile = new File(parser.getFileName());
		final String parent = mainFile.getParent() == null ? "." : mainFile.getParent();
		final RecursiveMachineLoader rml = new RecursiveMachineLoader(parent, contentProvider, parsingBehaviour);
		rml.loadAllMachines(mainFile, ast, parser.getDefinitions());
		return rml;
	}

	/**
	 * Recursively parse the given B machine and any other files that it references.
	 * 
	 * @param mainFile the B machine file to parse
	 * @param parsingBehaviour options controlling the behaviour of {@link RecursiveMachineLoader}
	 * @param contentProvider controls how files referenced by the main file are read
	 * @return a new {@link RecursiveMachineLoader} that has parsed the given B machine and referenced files
	 * @throws BCompoundException if parsing fails in any way
	 * @throws IOException if the main machine file cannot be read
	 */
	public static RecursiveMachineLoader loadFile(final File mainFile, final ParsingBehaviour parsingBehaviour, final IDefinitionFileProvider contentProvider) throws BCompoundException, IOException {
		final BParser parser = new BParser(mainFile.toString());
		final Start ast = parser.parseFile(mainFile, parsingBehaviour.isVerbose(), contentProvider);
		return loadFromAst(parser, ast, parsingBehaviour, contentProvider);
	}

	/**
	 * Recursively parse the given B machine and any other files that it references.
	 * Identical to {@link #loadFile(File, ParsingBehaviour, IDefinitionFileProvider)} with a {@link CachingDefinitionFileProvider}.
	 *
	 * @param mainFile the B machine file to parse
	 * @param parsingBehaviour options controlling the behaviour of {@link RecursiveMachineLoader}
	 * @return a new {@link RecursiveMachineLoader} that has parsed the given B machine and referenced files
	 * @throws BCompoundException if parsing fails in any way
	 * @throws IOException if the main machine file cannot be read
	 */
	public static RecursiveMachineLoader loadFile(final File mainFile, final ParsingBehaviour parsingBehaviour) throws BCompoundException, IOException {
		return loadFile(mainFile, parsingBehaviour, new CachingDefinitionFileProvider());
	}

	/**
	 * Recursively parse the given B machine and any other files that it references.
	 * Identical to {@link #loadFile(File, ParsingBehaviour)} with default {@link ParsingBehaviour} options.
	 *
	 * @param mainFile the B machine file to parse
	 * @return a new {@link RecursiveMachineLoader} that has parsed the given B machine and referenced files
	 * @throws BCompoundException if parsing fails in any way
	 * @throws IOException if the main machine file cannot be read
	 */
	public static RecursiveMachineLoader loadFile(final File mainFile) throws BCompoundException, IOException {
		return loadFile(mainFile, new ParsingBehaviour());
	}

	public void loadAllMachines(final File startFile, final Start start,
								final IDefinitions definitions) throws BCompoundException {
		recursivlyLoadMachine(startFile, start, new ArrayList<>(), true, rootDirectory, definitions);
	}

	private void loadMachine(final List<Ancestor> ancestors, final File machineFile)
			throws BCompoundException, IOException {

		if (machineFilesLoaded.contains(machineFile)) {
			return;
		}
		final BParser parser = new BParser(machineFile.getAbsolutePath());
		Start tree;
		tree = parser.parseFile(machineFile, parsingBehaviour.isVerbose(), contentProvider);
		recursivlyLoadMachine(machineFile, tree, ancestors, false,
				machineFile.getParentFile(), parser.getDefinitions());
	}

	public void printAsProlog(final PrintWriter out) {
		final IPrologTermOutput pout = new PrologTermOutput(out, false);
		printAsProlog(pout);
	}

	public void printAsProlog(final IPrologTermOutput pout) {
		final ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(getNodeIdMapping());
		pprinter.setPrintSourcePositions(parsingBehaviour.isAddLineNumbers(),
		                                 parsingBehaviour.isCompactPrologPositions());
		final ASTProlog prolog = new ASTProlog(pout, pprinter);

		// parser version
		pout.openTerm("parser_version");
		pout.printAtom(BParser.getGitSha());
		pout.closeTerm();
		pout.fullstop();

		// machine
		pout.openTerm("classical_b");
		pout.printAtom(main);
		pout.openList();

		for (final File file : machineFilesLoaded) {
			try {
				pout.printAtom(file.getCanonicalPath());
			} catch (IOException e) {
				pout.printAtom(file.getPath());
			}
		}
		pout.closeList();
		pout.closeTerm();
		pout.fullstop();
		for (final Map.Entry<String, Start> entry : getParsedMachines().entrySet()) {
			pout.openTerm("machine");
			entry.getValue().apply(prolog);
			pout.closeTerm();
			pout.fullstop();
		}

		pout.flush();
	}

	/**
	 * Tries to find a file containing the machine with the given file name.
	 *
	 * @return reference to a file containing the machine, may be non-existent
	 * but never <code>null</code>.
	 * @throws CheckException if the file cannot be found
	 */
	private File lookupFile(final File parentMachineDirectory, final MachineReference machineRef,
							List<Ancestor> ancestors, List<String> paths) throws CheckException {
		for (final String suffix : SUFFICES) {
			try {
				return new FileSearchPathProvider(parentMachineDirectory.getAbsolutePath(), machineRef.getName() + suffix, paths).resolve();
			} catch (IOException e) {
				// could not resolve the combination of prefix, machineName and
				// suffix, trying next one
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Machine not found: '");
		sb.append(machineRef.getName());
		sb.append("'");
		String fileNameOfErrorMachine = parsedFiles.get(ancestors.get(ancestors.size() - 1).getName()).getName();
		sb.append(" in '").append(fileNameOfErrorMachine).append("'");
		for (int i = ancestors.size() - 2; i >= 0; i--) {
			String name = ancestors.get(i).getName();
			String fileName = parsedFiles.get(name).getName();
			sb.append(" loaded by ").append("'").append(fileName).append("'");
		}
		throw new CheckException(sb.toString(), machineRef.getNode());
	}

	private void recursivlyLoadMachine(final File machineFile, final Start currentAst, final List<Ancestor> ancestors,
			final boolean isMain, File directory, final IDefinitions definitions)
			throws BCompoundException {


		ReferencedMachines refMachines = new ReferencedMachines(machineFile, currentAst,
				!isMain || parsingBehaviour.isMachineNameMustMatchFileName());


		try {
			refMachines.findReferencedMachines();
		} catch (BException e) {
			throw new BCompoundException(e);
		}

		String name = refMachines.getName();
		if (name == null) {
			/*
			 * the parsed file is a definition file, hence the name of the
			 * machine is null
			 */
			if (isMain)
				name = "DEFINITION_FILE";
			else
				throw new BCompoundException(new BException(machineFile.getName(),
						"Expecting a B machine but was a definition file in file: '" + machineFile.getName() + "'", null));
		}

		machineFilesLoaded.add(machineFile);
		final int fileNumber = machineFilesLoaded.indexOf(machineFile) + 1;
		getNodeIdMapping().assignIdentifiers(fileNumber, currentAst);

		definitions.assignIdsToNodes(getNodeIdMapping(), machineFilesLoaded);

		injectDefinitions(currentAst, definitions);

		if (parsedFiles.containsKey(name)) {
			throw new BCompoundException(new BException(machineFile.getName(),
					"Multiple files define the MACHINE '" + name + "' :"
							+ parsedFiles.get(name) + " and " + machineFile.getName(), null));
		} else {
			getParsedMachines().put(name, currentAst);
			parsedFiles.put(name, machineFile);
		}

		if (isMain) {
			this.main = name;
		}

		checkForCycles(ancestors, machineFile, name, refMachines);


		final HashMap<String, MachineReference> siblingTable = refMachines.getSiblingsTable();


		final List<MachineReference> references = refMachines.getReferences();
		for (final MachineReference refMachine : references) {

			try {
				final List<Ancestor> newAncestors = new ArrayList<>(ancestors);
				newAncestors.add(new Ancestor(name, siblingTable.get(refMachine.getName())));
				final String filePragma = refMachine.getPath();
				File file;
				if (filePragma == null) {
					file = lookupFile(directory, refMachine, newAncestors, refMachines.getPathList());
				} else {
					File p = new File(filePragma);
					if (p.isAbsolute()) {
						file = p;
					} else {
						file = new File(directory, filePragma);
					}
				}
				if (file.exists() && parsedFiles.containsKey(refMachine.getName())
						&& !parsedFiles.get(refMachine.getName()).getCanonicalPath().equals(file.getCanonicalPath())) {
					final String message = "Two files with the same name are referenced:\n"
							+ parsedFiles.get(refMachine.getName()).getCanonicalPath() + "\n" + file.getCanonicalPath();
					throw new BException(machineFile.getCanonicalPath(),
							new CheckException(message, refMachine.getNode()));

				}
				if (!getParsedMachines().containsKey(refMachine.getName())) {
					try {
						loadMachine(newAncestors, file);
					} catch (IOException e) {
						throw new BException(machineFile.getCanonicalPath(),
								new CheckException(e.getMessage(), refMachine.getNode(), e));
					}

				}
			} catch (final BException e) {
				throw new BCompoundException(e);
			} catch (final IOException e) {
				throw new BCompoundException(new BException(machineFile.getAbsolutePath(), e));
			} catch (final CheckException e) {
				throw new BCompoundException(new BException(machineFile.getAbsolutePath(), e));
			}
		}
	}

	private void checkForCycles(List<Ancestor> ancestors, File currentMachineFile, String currentMachineName, ReferencedMachines refMachines ) throws BCompoundException {
		final List<MachineReference> siblingList = refMachines.getSiblings();
		for (MachineReference sibling : siblingList) {

			final List<Ancestor> tempAncestors = new ArrayList<>(ancestors);
			tempAncestors.add(new Ancestor(currentMachineName, sibling));

			for (Ancestor ancestor : tempAncestors) {
				checkSiblings(ancestor, currentMachineFile, tempAncestors, new Ancestor(currentMachineName, sibling));
			}


		}

	}

	private void checkSiblings(Ancestor current, File currentMachineFile, List<Ancestor> ancestors, Ancestor sibling) throws BCompoundException {
		final String name = current.getName();
		final String closeTheCycle = sibling.getMachineReference().getName();

		if (name.equals(closeTheCycle)) {

			final Node node = current.getMachineReference().getNode();
			BException resultException = null;

			final StringBuilder dependency = new StringBuilder();
			boolean foundStartOfCycle = false;
			for (final Ancestor ancestor : ancestors) {
				// In case the cycle starts some where in the middle of the list
				if (ancestor.getName().equals(closeTheCycle)) {
					foundStartOfCycle = true;
					dependency.append(ancestor.getName());
				}
				if (foundStartOfCycle) {
					dependency.append(ancestor);
				}
			}

			String path;
			try {
				// TODO Avoid duplicate file lookup here, and instead do only one lookup that is used both when parsing and when reporting cycles
				path = lookupFile(currentMachineFile.getParentFile(), sibling.getMachineReference(), Collections.emptyList(), Collections.emptyList()).toString();
			} catch (CheckException e) {
				throw new BCompoundException(new BException(currentMachineFile.toString(), e));
			}

			if (node instanceof AMachineReference) {
				resultException = new BException(path,
						new CheckException("Cycle in imports/includes/extends statement: " + dependency, node));
			}
			if (node instanceof ASeesMachineClause) {
				resultException = new BException(path,
						new CheckException("Cycle in sees machine clause statement: " + dependency, node));
			}
			if (node instanceof AUsesMachineClause) {
				resultException = new BException(path,
						new CheckException("Cycle in uses machine clause statement: " + dependency, node));
			}
			if (node instanceof AImplementationMachineParseUnit) {
				resultException = new BException(path,
						new CheckException("Cycle in implementation: " + dependency, node));
			}
			if (node instanceof ARefinementMachineParseUnit) {
				resultException = new BException(path,
						new CheckException("Cycle in refinement: " + dependency, node));
			}
			throw new BCompoundException(resultException);
		}
	}


	private void injectDefinitions(final Start tree, final IDefinitions definitions) {
		final DefInjector defInjector = new DefInjector(definitions);
		tree.apply(defInjector);
	}

	public INodeIds getNodeIdMapping() {
		return nodeIds;
	}

	public Map<String, Start> getParsedMachines() {
		return parsedMachines;
	}

	public Map<String, File> getParsedFiles() {
		return parsedFiles;
	}

	public List<File> getMachineFilesLoaded() {
		return machineFilesLoaded;
	}

	private static class DefInjector extends DepthFirstAdapter {
		private final IDefinitions definitions;

		public DefInjector(final IDefinitions definitions) {
			this.definitions = definitions;
		}

		@Override
		public void caseADefinitionsMachineClause(final ADefinitionsMachineClause node) {
			final LinkedList<PDefinition> defList = node.getDefinitions();
			defList.clear();
			for (final String name : definitions.getDefinitionNames()) {
				final PDefinition def = definitions.getDefinition(name);
				defList.add(def);
			}
		}

		// IGNORE most machine parts
		@Override
		public void caseAConstantsMachineClause(final AConstantsMachineClause node) {
			// skip
		}

		@Override
		public void caseAVariablesMachineClause(final AVariablesMachineClause node) {
			// skip
		}

		@Override
		public void caseAPropertiesMachineClause(final APropertiesMachineClause node) {
			// skip
		}

		@Override
		public void caseAInvariantMachineClause(final AInvariantMachineClause node) {
			// skip
		}

		@Override
		public void caseAAssertionsMachineClause(final AAssertionsMachineClause node) {
			// skip
		}

		@Override
		public void caseAInitialisationMachineClause(final AInitialisationMachineClause node) {
			// skip
		}

		@Override
		public void caseAOperationsMachineClause(final AOperationsMachineClause node) {
			// skip
		}

	}
}
