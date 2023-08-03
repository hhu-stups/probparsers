package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.CachingDefinitionFileProvider;
import de.be4.classicalb.core.parser.FileSearchPathProvider;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.IFileContentProvider;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.MachineClauseAdapter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.ADefinitionsMachineClause;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PDefinition;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

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
	private final Map<String, ReferencedMachines> machineReferenceInfo = new TreeMap<>();
	private final Map<String, File> parsedFiles = new TreeMap<>();
	private final List<File> machineFilesLoaded = new ArrayList<>();
	private final IFileContentProvider contentProvider;
	private final ParsingBehaviour parsingBehaviour;
	private String main;


	public RecursiveMachineLoader(final String directory, final IFileContentProvider contentProvider, ParsingBehaviour parsingBehaviour) throws BCompoundException {
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

	public RecursiveMachineLoader(String path, IFileContentProvider contentProvider) throws BCompoundException {
		this(path, contentProvider, new ParsingBehaviour());
	}

	private static void printLoadProgress(File machineFile) {
		System.out.println("*** Debug: Parsing file '" + machineFile + "'");
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
	public static RecursiveMachineLoader loadFromAst(final BParser parser, final Start ast, final ParsingBehaviour parsingBehaviour, final IFileContentProvider contentProvider) throws BCompoundException {
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
	 */
	public static RecursiveMachineLoader loadFile(final File mainFile, final ParsingBehaviour parsingBehaviour, final IFileContentProvider contentProvider) throws BCompoundException {
		if (parsingBehaviour.isVerbose()) {
			printLoadProgress(mainFile);
		}
		final BParser parser = new BParser(mainFile.toString());
		parser.setContentProvider(contentProvider);
		final Start ast = parser.parseFile(mainFile);
		return loadFromAst(parser, ast, parsingBehaviour, contentProvider);
	}

	/**
	 * Recursively parse the given B machine and any other files that it references.
	 * Identical to {@link #loadFile(File, ParsingBehaviour, IFileContentProvider)} with a {@link CachingDefinitionFileProvider}.
	 *
	 * @param mainFile the B machine file to parse
	 * @param parsingBehaviour options controlling the behaviour of {@link RecursiveMachineLoader}
	 * @return a new {@link RecursiveMachineLoader} that has parsed the given B machine and referenced files
	 * @throws BCompoundException if parsing fails in any way
	 */
	public static RecursiveMachineLoader loadFile(final File mainFile, final ParsingBehaviour parsingBehaviour) throws BCompoundException {
		return loadFile(mainFile, parsingBehaviour, new CachingDefinitionFileProvider());
	}

	/**
	 * Recursively parse the given B machine and any other files that it references.
	 * Identical to {@link #loadFile(File, ParsingBehaviour)} with default {@link ParsingBehaviour} options.
	 *
	 * @param mainFile the B machine file to parse
	 * @return a new {@link RecursiveMachineLoader} that has parsed the given B machine and referenced files
	 * @throws BCompoundException if parsing fails in any way
	 */
	public static RecursiveMachineLoader loadFile(final File mainFile) throws BCompoundException {
		return loadFile(mainFile, new ParsingBehaviour());
	}

	public void loadAllMachines(final File startFile, final Start start,
								final IDefinitions definitions) throws BCompoundException {
		recursivlyLoadMachine(startFile, start, new ArrayList<>(), true, rootDirectory, definitions);
	}

	private void loadMachine(final List<Ancestor> ancestors, final File machineFile) throws BCompoundException {
		if (machineFilesLoaded.contains(machineFile)) {
			return;
		}
		if (parsingBehaviour.isVerbose()) {
			printLoadProgress(machineFile);
		}
		final BParser parser = new BParser(machineFile.getAbsolutePath());
		parser.setContentProvider(this.contentProvider);
		Start tree = parser.parseFile(machineFile);
		recursivlyLoadMachine(machineFile, tree, ancestors, false,
				machineFile.getParentFile(), parser.getDefinitions());
	}

	public void printAsProlog(final PrintWriter out) {
		final IPrologTermOutput pout = new PrologTermOutput(out, false);
		printAsProlog(pout);
	}

	public void printAsProlog(final IPrologTermOutput pout) {
		final ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(getNodeIdMapping());
		pprinter.setPrintSourcePositions(parsingBehaviour.isAddLineNumbers(), parsingBehaviour.isCompactPrologPositions());
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
							List<Ancestor> ancestors, Collection<Path> importedDirs) throws CheckException {
		for (final String suffix : SUFFICES) {
			try {
				final List<String> paths = importedDirs.stream()
					.map(Path::toAbsolutePath)
					.map(Path::toString)
					.collect(Collectors.toList());
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
		final boolean machineNameMustMatchFileName = !isMain || parsingBehaviour.isMachineNameMustMatchFileName();
		final ReferencedMachines refMachines;
		try {
			refMachines = MachineReferencesFinder.findReferencedMachines(machineFile.toPath(), currentAst, machineNameMustMatchFileName);
		} catch (BException e) {
			throw new BCompoundException(e);
		}

		String name = refMachines.getMachineName();
		if (refMachines.getType() == MachineType.DEFINITION_FILE) {
			assert name == null;
			if (!isMain) {
				throw new BCompoundException(new BException(machineFile.getName(),
						"Expecting a B machine but was a definition file in file: '" + machineFile.getName() + "'", null));
			}
			// Definition files can be loaded standalone.
			// In this case we need to set a dummy machine name,
			// because definition files don't contain a name in the source code.
			// TODO Perhaps MachineReferenceFinder should instead extract the file name of the .def file?
			name = "DEFINITION_FILE";
		} else {
			Objects.requireNonNull(name, "name");
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

		machineReferenceInfo.put(name, refMachines);

		if (isMain) {
			this.main = name;
		}

		checkForCycles(ancestors, machineFile, name, refMachines);

		final List<MachineReference> references = refMachines.getReferences();
		for (final MachineReference refMachine : references) {
			final List<Ancestor> newAncestors = new ArrayList<>(ancestors);
			newAncestors.add(new Ancestor(name, refMachine));
			final String filePragma = refMachine.getPath();
			File referencedFile;
			if (filePragma == null) {
				try {
					referencedFile = lookupFile(directory, refMachine, newAncestors, refMachines.getImportedPackages().values());
				} catch (CheckException e) {
					throw new BCompoundException(new BException(machineFile.getAbsolutePath(), e));
				}
			} else {
				File p = new File(filePragma);
				if (p.isAbsolute()) {
					referencedFile = p;
				} else {
					referencedFile = new File(directory, filePragma);
				}
			}

			if (referencedFile.exists() && parsedFiles.containsKey(refMachine.getName())) {
				String referencedFileCanonical;
				String alreadyParsedCanonical;
				try {
					referencedFileCanonical = referencedFile.getCanonicalPath();
					alreadyParsedCanonical = parsedFiles.get(refMachine.getName()).getCanonicalPath();
				} catch (IOException e) {
					throw new BCompoundException(new BException(machineFile.getAbsolutePath(), e));
				}
				if (!alreadyParsedCanonical.equals(referencedFileCanonical)) {
					final String message = "Two files with the same name are referenced:\n"
							+ alreadyParsedCanonical + "\n" + referencedFileCanonical;
					throw new BCompoundException(new BException(machineFile.getAbsolutePath(),
							new CheckException(message, refMachine.getNode())));
				}
			}

			if (!getParsedMachines().containsKey(refMachine.getName())) {
				try {
					loadMachine(newAncestors, referencedFile);
				} catch (BCompoundException e) {
					throw e.withMissingLocations(Collections.singletonList(BException.Location.fromNode(machineFile.getAbsolutePath(), refMachine.getNode())));
				}
			}
		}
	}

	private void checkForCycles(List<Ancestor> ancestors, File currentMachineFile, String currentMachineName, ReferencedMachines refMachines ) throws BCompoundException {
		for (MachineReference machineReference : refMachines.getReferences()) {

			final List<Ancestor> tempAncestors = new ArrayList<>(ancestors);
			tempAncestors.add(new Ancestor(currentMachineName, machineReference));

			for (Ancestor ancestor : tempAncestors) {
				checkSiblings(ancestor, currentMachineFile, tempAncestors, new Ancestor(currentMachineName, machineReference));
			}


		}

	}

	private void checkSiblings(Ancestor current, File currentMachineFile, List<Ancestor> ancestors, Ancestor sibling) throws BCompoundException {
		final String name = current.getName();
		final String closeTheCycle = sibling.getMachineReference().getName();

		if (name.equals(closeTheCycle)) {
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

			final Node node = current.getMachineReference().getNode();
			throw new BCompoundException(new BException(path, new CheckException("Cycle in " + current.getMachineReference().getType() + " clause: " + dependency, node)));
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

	public Map<String, ReferencedMachines> getMachineReferenceInfo() {
		return Collections.unmodifiableMap(this.machineReferenceInfo);
	}

	public Map<String, File> getParsedFiles() {
		return parsedFiles;
	}

	public List<File> getMachineFilesLoaded() {
		return machineFilesLoaded;
	}

	public String getMainMachineName() {
		return main;
	}

	private static class DefInjector extends MachineClauseAdapter {
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
	}
}
