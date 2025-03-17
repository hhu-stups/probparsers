package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.FileSearchPathProvider;
import de.be4.classicalb.core.parser.ParseOptions;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReferencesFinder;
import de.be4.classicalb.core.parser.analysis.prolog.PackageName;
import de.be4.classicalb.core.parser.analysis.prolog.ReferencedMachines;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.Node;

public class RulesParseUnit extends IModel {
	private List<MachineReference> machineReferences = new ArrayList<>();

	private String content;
	private File machineFile;
	private BCompoundException bCompoundException;
	private BParser bParser;

	private RulesMachineChecker rulesMachineChecker;

	private RulesParseUnit(String machineContent) {
		super();
		this.content = machineContent;
	}

	private RulesParseUnit(File machineFile) {
		super();
		this.machineFile = machineFile;
	}

	public static RulesParseUnit parse(File machineFile, ParsingBehaviour parsingBehaviour) {
		RulesParseUnit rulesParseUnit = new RulesParseUnit(machineFile);
		rulesParseUnit.setParsingBehaviour(parsingBehaviour);
		rulesParseUnit.parse();
		return rulesParseUnit;
	}

	public static RulesParseUnit parse(String machineString) {
		RulesParseUnit rulesParseUnit = new RulesParseUnit(machineString);
		rulesParseUnit.parse();
		return rulesParseUnit;
	}

	private void parse() {
		if (this.bCompoundException != null) {
			return;
		}
		try {
			bParser = new BParser(machineFile != null ? machineFile.getPath() : null);
			ParseOptions parseOptions = new ParseOptions();
			parseOptions.setGrammar(RulesGrammar.getInstance());
			bParser.setParseOptions(parseOptions);
			this.setStart(machineFile != null ? bParser.parseFile(machineFile) : bParser.parseMachine(content));
			if (machineFile != null) {
				ReferencedMachines machines = MachineReferencesFinder.findReferencedMachines(machineFile.toPath(), this.getStart(), true);
				this.setMachineName(machines.getMachineName());

				this.machineReferences = new ArrayList<>();
				for (MachineReference mr : machines.getReferences()) {
					String filePragma = mr.getPath();
					File referencedFile;
					if (filePragma == null) {
						try {
							referencedFile = lookupFile(mr.getName(), machines.getImportedPackages(), mr.getNode());
						} catch (CheckException e) {
							throw new BException(machineFile.getAbsolutePath(), e);
						}
					} else {
						File p = new File(filePragma);
						referencedFile = p.isAbsolute() ? p : new File(machineFile.getParentFile(), filePragma);
					}
					machineReferences.add(new MachineReference(mr.getType(), mr.getName(), mr.getRenamedName(), mr.getNode(), referencedFile.getAbsolutePath()));
				}
			}
			this.rulesMachineChecker = new RulesMachineChecker(machineFile, machineReferences, this.getStart());
			rulesMachineChecker.runChecks();
		} catch (BCompoundException e) { // store parser exceptions
			this.bCompoundException = e;
		} catch (BException e) {
			this.bCompoundException = new BCompoundException(e);
		}
	}

	public void translate() {
		Map<String, AbstractOperation> allOperations = this.getOperations().stream()
				.collect(Collectors.toMap(AbstractOperation::getOriginalName, op -> op));
		this.translate(allOperations);
	}

	public void translate(Map<String, AbstractOperation> allOperations) {
		if (this.hasError()) {
			return;
		}
		final RulesTransformation ruleTransformation = new RulesTransformation(this.getStart(), bParser, rulesMachineChecker,
				allOperations);
		try {
			ruleTransformation.runTransformation();
		} catch (BCompoundException e) {
			bCompoundException = e;
		}
	}

	private static final String[] SUFFICES = new String[] { ".rmch", ".mch" };

	private File lookupFile(final String name, final Map<PackageName, Path> imports, final Node node) throws CheckException {
		List<String> importPaths = imports.values().stream().map(v -> v.toAbsolutePath().toString()).collect(Collectors.toList());
		for (final String suffix : SUFFICES) {
			try {
				return new FileSearchPathProvider(machineFile.getParentFile().getPath(), name + suffix, importPaths)
						.resolve();
			} catch (IOException e) {
				// could not resolve the combination of prefix, machineName and suffix, trying next one
			}
		}
		throw new CheckException(String.format("Machine not found: '%s'", name), node);
	}

	public List<AbstractOperation> getOperations() {
		return this.rulesMachineChecker == null ? new ArrayList<>() : this.rulesMachineChecker.getOperations();
	}

	@Override
	public String getPath() {
		if (this.machineFile != null) {
			return this.machineFile.getAbsolutePath();
		} else {
			return this.getMachineName();
		}
	}

	public RulesMachineChecker getRulesMachineChecker() {
		return this.rulesMachineChecker;
	}

	@Override
	public List<MachineReference> getMachineReferences() {
		return this.machineReferences == null ? new ArrayList<>() : this.machineReferences;
	}

	@Override
	public boolean hasError() {
		return this.bCompoundException != null;
	}

	@Override
	public BCompoundException getCompoundException() {
		return this.bCompoundException;
	}

	public BParser getBParser() {
		return this.bParser;
	}

}
