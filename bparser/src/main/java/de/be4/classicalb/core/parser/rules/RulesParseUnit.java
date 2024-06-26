package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ParseOptions;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.grammars.RulesGrammar;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.prolog.output.IPrologTermOutput;

public class RulesParseUnit implements IModel {
	private String machineName;
	private List<MachineReference> machineReferences;

	private String content;
	private File machineFile;
	private BCompoundException bCompoundException;
	private ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
	private BParser bParser;
	private Start start;

	private final List<AbstractOperation> operationList = new ArrayList<>();
	private RulesMachineChecker rulesMachineChecker;
	private RulesMachineReferencesFinder refFinder;

	public RulesParseUnit() {
	}

	public RulesParseUnit(String machineName) {
		this.machineName = machineName;
	}

	public List<AbstractOperation> getOperations() {
		return this.operationList;
	}

	@Override
	public Start getStart() {
		return this.start;
	}

	@Override
	public String getPath() {
		if (this.machineFile != null) {
			return this.machineFile.getAbsolutePath();
		} else {
			return this.machineName;
		}
	}

	public void setMachineAsString(String content) {
		this.content = content;
	}

	public RulesMachineChecker getRulesMachineChecker() {
		return this.rulesMachineChecker;
	}

	public void setParsingBehaviour(final ParsingBehaviour parsingBehaviour) {
		this.parsingBehaviour = parsingBehaviour;
	}

	public void readMachineFromFile(File file) {
		this.machineFile = file;
		try {
			content = Utils.readFile(file);
			this.machineFile = machineFile.getCanonicalFile();
		} catch (IOException e) {
			bCompoundException = new BCompoundException(new BException(file.getAbsolutePath(), e));
		}
	}

	public void parse() {
		if (this.bCompoundException != null) {
			return;
		}
		try {
			bParser = new BParser(machineFile != null ? machineFile.getPath() : null);
			ParseOptions parseOptions = new ParseOptions();
			parseOptions.setGrammar(RulesGrammar.getInstance());
			bParser.setParseOptions(parseOptions);
			start = bParser.parseMachine(content);
			refFinder = new RulesMachineReferencesFinder(machineFile, start);
			refFinder.findReferencedMachines();

			this.machineReferences = refFinder.getReferences();
			this.machineName = refFinder.getName();
			this.rulesMachineChecker = new RulesMachineChecker(machineFile, machineReferences, start);
			rulesMachineChecker.runChecks();
			this.operationList.addAll(rulesMachineChecker.getOperations());

		} catch (BCompoundException e) {
			// store parser exceptions
			this.bCompoundException = e;
		}
	}

	public void translate() {
		final HashMap<String, AbstractOperation> allOperations = new HashMap<>();
		for (AbstractOperation op : operationList) {
			allOperations.put(op.getOriginalName(), op);
		}
		this.translate(allOperations);
	}

	public void translate(Map<String, AbstractOperation> allOperations) {
		if (bCompoundException != null) {
			return;
		}
		final RulesTransformation ruleTransformation = new RulesTransformation(start, bParser, rulesMachineChecker,
				allOperations);
		try {
			ruleTransformation.runTransformation();
		} catch (BCompoundException e) {
			bCompoundException = e;
		}
	}

	@Override
	public String getMachineName() {
		return machineName;
	}

	@Override
	public List<MachineReference> getMachineReferences() {
		if (this.machineReferences == null) {
			return new ArrayList<>();
		} else {
			return this.machineReferences;
		}
	}

	@Override
	public void printAsPrologWithFullstops(final IPrologTermOutput pout, INodeIds nodeIdMapping, boolean withFullstops) {
		assert start != null;
		final ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIdMapping);
		pprinter.setPrintSourcePositions(parsingBehaviour.isAddLineNumbers(), parsingBehaviour.isCompactPrologPositions());
		final ASTProlog prolog = new ASTProlog(pout, pprinter);
		pout.openTerm("machine");
		start.apply(prolog);
		pout.closeTerm();
		if (withFullstops) {
			pout.fullstop();
		} else {
			pout.flush();
		}
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
