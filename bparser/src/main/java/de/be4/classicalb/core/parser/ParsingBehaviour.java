package de.be4.classicalb.core.parser;

public class ParsingBehaviour {
	// Flags are set, e.g., in CliBParser.java
	private boolean prologOutput = false; // -prolog flag in CliBParser
	private boolean addLineNumbers = false; // -lineno flag in CliBParser
	private boolean verbose = false; // verbose mode includes debug prints, -v flag in CliBParser
	private boolean printTime = false; // -time flag in CliBParser
	private boolean prettyPrintB = false; // -pp flag in CliBParser
	private boolean fastPrologOutput = false; // -fastprolog flag in CliBParser
	private boolean compactPositions = true; // false means use old style pos/5 positions
	private boolean machineNameMustMatchFileName = false; // -checkname flag in CliBParser
	private boolean printLocalStackSize = false; // -printstacksize flag
	private int defaultFileNumber = -1;
	private int startLineNumber = 1;
	private int startColumnNumber = 1;
	

	public boolean isPrologOutput() {
		return prologOutput;
	}

	public void setPrologOutput(boolean prologOutput) {
		this.prologOutput = prologOutput;
	}

	public boolean isAddLineNumbers() {
		return addLineNumbers;
	}

	public void setAddLineNumbers(boolean addLineNumbers) {
		this.addLineNumbers = addLineNumbers;
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

	public boolean isMachineNameMustMatchFileName() {
		return machineNameMustMatchFileName;
	}

	public void setMachineNameMustMatchFileName(boolean machineNameMustMatchFileName) {
		this.machineNameMustMatchFileName = machineNameMustMatchFileName;
	}

	public boolean isPrintLocalStackSize() {
		return printLocalStackSize;
	}
	
	public void setPrintLocalStackSize(boolean printLocalStackSize) {
		this.printLocalStackSize = printLocalStackSize;
	}

	public int getDefaultFileNumber() {
		return this.defaultFileNumber;
	}

	public void setDefaultFileNumber(int defaultFileNumber) {
		this.defaultFileNumber = defaultFileNumber;
	}

	public int getStartLineNumber() {
		return this.startLineNumber;
	}

	public void setStartLineNumber(int startLineNumber) {
		this.startLineNumber = startLineNumber;
	}

	public int getStartColumnNumber() {
		return this.startColumnNumber;
	}

	public void setStartColumnNumber(int startColumnNumber) {
		this.startColumnNumber = startColumnNumber;
	}
}
