package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.PrintStream;

public class ParsingBehaviour {
	// Flags are set, e.g., in CliBParser.java
	private boolean prologOutput = false;       // -prolog flag in CliBParser
	private boolean useIndention = false;       // -indent flag in CliBParser
	private boolean addLineNumbers = false;     // -lineno flag in CliBParser
	private boolean displayGraphically = false; // -ui flag in CliBParser
	private boolean verbose = false; //verbose mode includes debug prints, -v flag in CliBParser
	private boolean printTime = false;          // -time flag in CliBParser
	private PrintStream out = System.out;
	private boolean printAST = false;           // -ast flag in CliBParser
	private boolean prettyPrintB = false;           // -pp flag in CliBParser
	private boolean fastPrologOutput = false;   // -fastprolog flag in CliBParser
	private boolean compactPositions = false;     // false means use old style pos/5 positions
	private File outputFile;
	private boolean machineNameMustMatchFileName = false; // -checkname flag in CliBParser
	

	public boolean isPrologOutput() {
		return prologOutput;
	}

	public void setPrologOutput(boolean prologOutput) {
		this.prologOutput = prologOutput;
	}

	public boolean isUseIndention() {
		return useIndention;
	}

	public void setUseIndention(boolean useIndention) {
		this.useIndention = useIndention;
	}

	public boolean isAddLineNumbers() {
		return addLineNumbers;
	}

	public void setAddLineNumbers(boolean addLineNumbers) {
		this.addLineNumbers = addLineNumbers;
	}

	public boolean isDisplayGraphically() {
		return displayGraphically;
	}

	public void setDisplayGraphically(boolean displayGraphically) {
		this.displayGraphically = displayGraphically;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isPrintTime() {
		return printTime;
	}

	public void setPrintTime(boolean printTime) {
		this.printTime = printTime;
	}

	public PrintStream getOut() {
		return out;
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public boolean isPrintAST() {
		return printAST;
	}
	public void setPrintAST(boolean printAST) {
		this.printAST = printAST;
	}

	public boolean isPrettyPrintB() {
		return prettyPrintB;
	}
	public void setPrettyPrintB(boolean printAST) {
		this.prettyPrintB = printAST;
	}

	public boolean isFastPrologOutput() {
		return fastPrologOutput;
	}

	public void setFastPrologOutput(boolean fastPrologOutput) {
		this.fastPrologOutput = fastPrologOutput;
	}
	
	public boolean isCompactPrologPositions() {
		return compactPositions;
	}

	public void setCompactPrologPositions(boolean compactPositions) {
		this.compactPositions = compactPositions;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public boolean isMachineNameMustMatchFileName() {
		return machineNameMustMatchFileName;
	}

	public void setMachineNameMustMatchFileName(boolean machineNameMustMatchFileName) {
		this.machineNameMustMatchFileName = machineNameMustMatchFileName;
	}

}
