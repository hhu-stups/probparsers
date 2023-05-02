package de.prob.cliparser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.FastReadWriter;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.MockedDefinitions;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.NodeFileNumbers;
import de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment;
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.be4.ltl.core.parser.CtlParser;
import de.be4.ltl.core.parser.LtlParseException;
import de.be4.ltl.core.parser.LtlParser;
import de.be4.ltl.core.parser.TemporalLogicParser;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;

public class CliBParser {

	private static final String CLI_SWITCH_VERBOSE = "-v";
	private static final String CLI_SWITCH_VERSION = "-version";
	private static final String CLI_SWITCH_PRINT_STACK_SIZE = "-printstacksize";
	private static final String CLI_SWITCH_HELP = "-h";
	private static final String CLI_SWITCH_HELP2 = "-help";
	private static final String CLI_SWITCH_HELP3 = "--help";
	private static final String CLI_SWITCH_TIME = "-time";
	private static final String CLI_SWITCH_PP = "-pp";
	private static final String CLI_SWITCH_PROLOG = "-prolog";
	private static final String CLI_SWITCH_FASTPROLOG = "-fastprolog";
	private static final String CLI_SWITCH_COMPACT_POSITIONS = "-compactpos";
	private static final String CLI_SWITCH_PROLOG_LINES = "-lineno";
	private static final String CLI_SWITCH_OUTPUT = "-out";
	private static final String CLI_SWITCH_PREPL = "-prepl";
	private static final String CLI_SWITCH_NAME_CHECK = "-checkname";
	// other interesting parameters: System.getProperty : prob.stdlib

	private static Socket socket;
	private static PrintWriter socketWriter;


	private static int getStackSize(int acc){
		try {
			return CliBParser.getStackSize(acc+1);
		} catch (final StackOverflowError e) {
			return acc;
		}
	}

	/**
	 * Main method wrapper.
	 * This is necessary because of <a href="https://github.com/oracle/graal/issues/3398">a bug with graalvm and musl</a>.
	 * Workaround inspired by: <a href="https://github.com/babashka/babashka/issues/831">babashka/babashka#831</a>
	 */
	public static void main(final String[] args) throws InterruptedException, IOException {
		AtomicReference<IOException> maybeException = new AtomicReference<>(null);
		Thread t = new Thread(()->{
			try {
				CliBParser.mainImpl(args);
			} catch (IOException e) {
				maybeException.set(e);
			}
		});
		t.start();
		t.join();
		if(maybeException.get()!=null)throw maybeException.get();
	}

	/**
	 * Actual main method
	 */
	public static void mainImpl(final String[] args) throws IOException {

		// System.out.println("Ready. Press enter");
		// System.in.read();
		// System.out.println("Starting");
		final ConsoleOptions options = createConsoleOptions(args);
		
		if (options.isOptionSet(CLI_SWITCH_HELP) ||
				options.isOptionSet(CLI_SWITCH_HELP2) ||
				options.isOptionSet(CLI_SWITCH_HELP3)) {
			options.printUsage(System.err);
			System.exit(-1);
		}
		
		if (options.isOptionSet(CLI_SWITCH_VERSION)) {
			System.out.println(String.format("Version:    %s", BParser.getVersion()));
			System.out.println(String.format("Git Commit: %s", BParser.getGitSha()));
			System.exit(0);
		}

		if(options.isOptionSet(CLI_SWITCH_PRINT_STACK_SIZE)) {
			System.out.format("Local stack size:\t%d\n", CliBParser.getStackSize(0));
		}

		final String[] arguments = options.getRemainingOptions();
		if (!options.isOptionSet(CLI_SWITCH_PREPL) && arguments.length != 1) {
			System.err.println("\nYou have not provided a file to parse (nor specified the -prepl option).\n");
			System.err.println("Here is how to use the parser:");
			options.printUsage(System.err);
			System.exit(-1);
		}

		final ParsingBehaviour behaviour = new ParsingBehaviour();

		final OutputStream out;
		if (options.isOptionSet(CLI_SWITCH_OUTPUT)) {
			final String filename = options.getOptions(CLI_SWITCH_OUTPUT)[0];

			try {
				out = new FileOutputStream(filename);
			} catch (final FileNotFoundException e) {
				if (options.isOptionSet(CLI_SWITCH_PROLOG)) {
					PrologExceptionPrinter.printException(System.err, e);
				} else {
					System.err.println("Unable to create file '" + filename + "'");
				}
				System.exit(-1);
				return; // Unreachable, but needed
			}
		} else {
			out = System.out;
		}
		behaviour.setPrintTime(options.isOptionSet(CLI_SWITCH_TIME));
		behaviour.setPrologOutput(options.isOptionSet(CLI_SWITCH_PROLOG));
		behaviour.setAddLineNumbers(options.isOptionSet(CLI_SWITCH_PROLOG_LINES)); // -lineno flag
		behaviour.setPrettyPrintB(options.isOptionSet(CLI_SWITCH_PP)); // -pp flag
		behaviour.setPrintLocalStackSize(options.isOptionSet(CLI_SWITCH_PRINT_STACK_SIZE));
		behaviour.setVerbose(options.isOptionSet(CLI_SWITCH_VERBOSE)); // -v flag
		//behaviour.setVerbose(true); // always set -v flag
		behaviour.setFastPrologOutput(options.isOptionSet(CLI_SWITCH_FASTPROLOG));
		behaviour.setCompactPrologPositions(options.isOptionSet(CLI_SWITCH_COMPACT_POSITIONS));
		behaviour.setMachineNameMustMatchFileName(options.isOptionSet(CLI_SWITCH_NAME_CHECK));
		// TO DO: check if some other flags are not recognised

		if (options.isOptionSet(CLI_SWITCH_PREPL)) {
			runPRepl(behaviour);
		} else {
			// there should be just one remaining argument
			// otherwise no filename was provided, or some arguments were not
			// parsed correctly
			if (options.getRemainingOptions().length != 1) {
				options.printUsage(System.err);
				System.exit(-1);
			}
			String filename = options.getRemainingOptions()[0];
			final File bfile = new File(filename);
			int returnValue = doFileParsing(behaviour, out, new PrintWriter(System.err), bfile);
			if (options.isOptionSet(CLI_SWITCH_OUTPUT)) {
				out.close();
			}
			System.exit(returnValue);
		}
	}
	
	private static String getNamedOption(ParsingBehaviour behaviour, String name) {
		switch (name) {
			case "addLineNumbers":
				return String.valueOf(behaviour.isAddLineNumbers());
			case "verbose":
				return String.valueOf(behaviour.isVerbose());
			case "fastPrologOutput":
				return String.valueOf(behaviour.isFastPrologOutput());
			case "compactPrologPositions":
				return String.valueOf(behaviour.isCompactPrologPositions());
			case "machineNameMustMatchFileName":
				return String.valueOf(behaviour.isMachineNameMustMatchFileName());
			case "defaultFileNumber":
				return String.valueOf(behaviour.getDefaultFileNumber());
			case "startLineNumber":
				return String.valueOf(behaviour.getStartLineNumber());
			case "startColumnNumber":
				return String.valueOf(behaviour.getStartColumnNumber());
			case "printstacksize":
				return String.valueOf(behaviour.isPrintLocalStackSize());
			default:
				// Unknown/unsupported option
				return null;
		}
	}
	
	private static boolean setNamedOption(ParsingBehaviour behaviour, String name, String value) {
		switch (name) {
			case "addLineNumbers":
				behaviour.setAddLineNumbers(Boolean.parseBoolean(value));
				break;
			case "verbose":
				behaviour.setVerbose(Boolean.parseBoolean(value));
				break;
			case "fastPrologOutput":
				behaviour.setFastPrologOutput(Boolean.parseBoolean(value));
				break;
			case "compactPrologPositions":
				behaviour.setCompactPrologPositions(Boolean.parseBoolean(value));
				break;
			case "machineNameMustMatchFileName":
				behaviour.setMachineNameMustMatchFileName(Boolean.parseBoolean(value));
				break;
			case "defaultFileNumber":
				behaviour.setDefaultFileNumber(Integer.parseInt(value));
				break;
			case "startLineNumber":
				behaviour.setStartLineNumber(Integer.parseInt(value));
				break;
			case "startColumnNumber":
				behaviour.setStartColumnNumber(Integer.parseInt(value));
				break;
			case "printstacksize":
				behaviour.setPrintLocalStackSize(Boolean.parseBoolean(value));
				break;
			default:
				// Unknown/unsupported option
				return false;
		}
		return true;
	}
	
	private static void runPRepl(ParsingBehaviour behaviour) throws IOException, FileNotFoundException {
		ServerSocket serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
		// write port number as prolog term
		System.out.println(serverSocket.getLocalPort() + ".");
		socket = serverSocket.accept();
		// socket.setTcpNoDelay(true); // does not seem to provide any response benefit
		socketWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)));

		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		String line = "";
		MockedDefinitions context = new MockedDefinitions();
		boolean terminate = false;
		while (!terminate) {
			line = in.readLine();

			EPreplCommands command;
			String theFormula;

			if (line == null) {
				// the prob instance has been terminated. exit gracefully
				command = EPreplCommands.halt;
			} else {
				command = EPreplCommands.valueOf(line);
			}
			
			behaviour.debug_print("Received PREPL command: " + command);

			switch (command) {
			case version:
				print(BParser.getVersion() + "-" + BParser.getGitSha() + System.lineSeparator());
				break;
			case shortversion:
				print(BParser.getVersion() + System.lineSeparator());
				break;
			case gitsha:
				print(BParser.getGitSha() + System.lineSeparator());
				break;
			case commandsupported:
				// Check if the given command is supported by this version of the parser.
				String commandToCheck = in.readLine();
				try {
					EPreplCommands.valueOf(commandToCheck);
				} catch (IllegalArgumentException ignored) {
					print("false." + System.lineSeparator());
					break;
				}
				print("true." + System.lineSeparator());
				break;
			case featuresupported:
				// Check if the given feature is supported by this version of the parser.
				// There are no features defined yet, but we already support this command for future-proofing.
				print("false." + System.lineSeparator());
				break;
			case definition:
				// sending a new DEFINITION to the parser
				String name = in.readLine();
				String type = in.readLine();
				String parameterCount = in.readLine();
				context.addMockedDefinition(name, type, parameterCount);
				break;
			case resetdefinitions:
				// remove all DEFINITIONS 
				context = new MockedDefinitions();
				break;
			case getoption:
				// Generic command for getting the current value of an option.
				// Fails safely for unknown/unsupported options.
				String getOptionName = in.readLine();
				String getOptionValue = getNamedOption(behaviour, getOptionName);
				final PrologTermOutput getOptionOut = new PrologTermOutput(socketWriter);
				if (getOptionValue != null) {
					getOptionOut.openTerm("value");
					getOptionOut.printAtom(getNamedOption(behaviour, getOptionName));
					getOptionOut.closeTerm();
				} else {
					getOptionOut.printAtom("unsupported");
				}
				getOptionOut.fullstop();
				getOptionOut.flush();
				break;
			case setoption:
				// Generic command for changing parser options.
				// Fails safely for unknown/unsupported options.
				// Replaces the single-option commands below.
				String setOptionName = in.readLine();
				String setOptionValue = in.readLine();
				String setOptionPrevValue = getNamedOption(behaviour, setOptionName);
				boolean ok = setNamedOption(behaviour, setOptionName, setOptionValue);
				final PrologTermOutput setOptionOut = new PrologTermOutput(socketWriter);
				if (ok) {
					setOptionOut.openTerm("prev_value");
					setOptionOut.printAtom(setOptionPrevValue);
					setOptionOut.closeTerm();
				} else {
					setOptionOut.printAtom("unsupported");
				}
				setOptionOut.fullstop();
				setOptionOut.flush();
				break;
			// new commands to change parsingBehaviour, analog to command-line switches
			case fastprolog:
				String newFVal = in.readLine();
				behaviour.debug_print("Setting fastprolog to "+newFVal);
				behaviour.setFastPrologOutput(Boolean.parseBoolean(newFVal));
				break;
			case compactpos:
				behaviour.setCompactPrologPositions(Boolean.parseBoolean(in.readLine()));
				break;
			case verbose:
				behaviour.setVerbose(Boolean.parseBoolean(in.readLine()));
				break;
			case checkname:
				behaviour.setMachineNameMustMatchFileName(Boolean.parseBoolean(in.readLine()));
				break;
			case lineno:
				behaviour.setAddLineNumbers(Boolean.parseBoolean(in.readLine()));
				break;
			case machine:
				String filename = in.readLine();
				String outFile = in.readLine();
				final File bfile = new File(filename);
				final int returnValue;
				try (final OutputStream out = new FileOutputStream(outFile)) {
					returnValue = doFileParsing(behaviour, out, socketWriter, bfile);
				}
				context = new MockedDefinitions();

				// Notify probcli that the call finished successfully.
				// If an exception was thrown, doFileParsing will have already printed an appropriate error message/term.
				if (returnValue == 0) {
					print("exit(" + returnValue + ")." + System.lineSeparator());
				} else if (returnValue < -4) { // VM/StackOverflow error occurred; file is probably corrupt
					System.out.println("Erasing file contents of " + outFile);
					Files.write(Paths.get(outFile), Collections.singletonList("% VM Error occurred"));
				}
				break;
			case formula:
				theFormula = "#FORMULA\n" + in.readLine();
				parseFormula(theFormula, context, behaviour);
				break;
			case expression:
				theFormula = "#EXPRESSION\n" + in.readLine();
				parseFormula(theFormula, context, behaviour);
				break;
			case predicate:
				theFormula = "#PREDICATE\n" + in.readLine();
				parseFormula(theFormula, context, behaviour);
				break;
			case substitution:
				theFormula = "#SUBSTITUTION\n" + in.readLine();
				parseFormula(theFormula, context, behaviour);
				break;
			case extendedformula:
				theFormula = "#FORMULA\n" + in.readLine();
				parseExtendedFormula(theFormula, context, behaviour);
				break;
			case extendedexpression:
				theFormula = "#EXPRESSION\n" + in.readLine();
				parseExtendedFormula(theFormula, context, behaviour);
				break;
			case extendedpredicate:
				theFormula = "#PREDICATE\n" + in.readLine();
				parseExtendedFormula(theFormula, context, behaviour);
				break;
			case extendedsubstitution:
				theFormula = "#SUBSTITUTION\n" + in.readLine();
				parseExtendedFormula(theFormula, context, behaviour);
				break;
			case ltl:
				String extension = in.readLine();
				final ProBParserBase extParser = LtlConsoleParser.getExtensionParser(extension,context);
				final TemporalLogicParser<?> parser = new LtlParser(extParser);

				parseTemporalFormula(in, parser);

				break;
			case ctl:
				String extension2 = in.readLine();
				final ProBParserBase extParser2 = LtlConsoleParser.getExtensionParser(extension2,context);
				final TemporalLogicParser<?> parser2 = new CtlParser(extParser2);
				parseTemporalFormula(in, parser2);
				break;

			case halt:
				socket.close();
				serverSocket.close();
				terminate = true;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported Command " + line);
			}

		}
	}

	private static void parseTemporalFormula(BufferedReader in, final TemporalLogicParser<?> parser)
			throws IOException {
		String theFormula;
		final IPrologTermOutput pout = new PrologTermOutput(socketWriter, false);
		theFormula = in.readLine();

		try {
			final PrologTerm term = parser.generatePrologTerm(theFormula, null);
			pout.openTerm("ltl").printTerm(term).closeTerm();
		} catch (LtlParseException e) {
			pout.openTerm("syntax_error").printAtom(e.getLocalizedMessage()).closeTerm();
		}

		pout.fullstop();
		pout.flush();
	}

	private static void parseFormulaInternal(String theFormula, IDefinitions context, 
	                                         final ParsingBehaviour behaviour, final boolean extended) {
		final IPrologTermOutput pout = new PrologTermOutput(socketWriter, false);

		try {
			BParser parser = new BParser();
			// Reduce starting line number by one
			// so that the line with a #FORMULA, etc. prefix isn't counted
			// and the actual formula is counted as line 1.
			parser.setStartPosition(behaviour.getStartLineNumber()-1, behaviour.getStartColumnNumber());
			parser.setDefinitions(context);
			Start start;
			if (extended) {
				start = parser.eparse(theFormula, context);
			} else {
				start = parser.parse(theFormula, false, false); // debugOutput=false, preparseNecessary=false
			}

			// In the compact position format, node IDs are not used,
			// so generate them only if the old non-compact format is requested.
			final INodeIds nodeIds;
			if (behaviour.isCompactPrologPositions()) {
				nodeIds = new NodeFileNumbers();
				if (behaviour.getDefaultFileNumber() != -1) {
					nodeIds.assignIdentifiers(behaviour.getDefaultFileNumber(), start);
				}
			} else {
				final NodeIdAssignment na = new NodeIdAssignment();
				if (behaviour.getDefaultFileNumber() == -1) {
					start.apply(na);
				} else {
					na.assignIdentifiers(behaviour.getDefaultFileNumber(), start);
				}
				nodeIds = na;
			}

			ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIds);
			pprinter.setPrintSourcePositions(behaviour.isAddLineNumbers(), behaviour.isCompactPrologPositions());
			ASTProlog printer = new ASTProlog(pout, pprinter);

			start.apply(printer);
		} catch (NullPointerException e) {
			// Not Parseable - Sadly, calling e.getLocalizedMessage() on the
			// NullPointerException returns NULL itself, thus triggering another
			// NullPointerException in the catch statement. Therefore we need a
			// second catch statement with a special case for the
			// NullPointerException instead of catching a general Exception
			// print("EXCEPTION NullPointerException" + System.lineSeparator());
			pout.openTerm("exception").printAtom("NullPointerException").closeTerm();
		} catch (BCompoundException e) {
			PrologExceptionPrinter.printException(pout, e);
		} catch (LexerException e) {
			pout.openTerm("exception").printAtom(e.getLocalizedMessage()).closeTerm();
		} catch (IOException e) {
			PrologExceptionPrinter.printException(pout, e);
		}

		pout.fullstop();
		pout.flush();
	}

	private static void parseExtendedFormula(String theFormula, IDefinitions context, final ParsingBehaviour behaviour) {
		parseFormulaInternal(theFormula, context, behaviour, true);
	}

	private static void parseFormula(String theFormula, IDefinitions context, final ParsingBehaviour behaviour) {
		parseFormulaInternal(theFormula, context, behaviour, false);
	}

	private static void print(String output) {
		socketWriter.print(output);
		socketWriter.flush();
	}

	private static int doFileParsing(final ParsingBehaviour behaviour, final OutputStream out, final PrintWriter err, final File bfile) {
		try {
			behaviour.debug_print("Parsing file: " + bfile);
			if (bfile.getName().endsWith(".rmch")) {
				parseRulesProject(bfile, behaviour, out);
			} else {
				fullParsing(bfile, behaviour, out);
			}
			return 0;
		} catch (final IOException e) {
			if (behaviour.isPrologOutput() ||
					behaviour.isFastPrologOutput() ) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(err, e);
			} else {
				err.println("Error reading input file: " + e.getLocalizedMessage());
			}
			return -2;
		} catch (final BCompoundException e) {
			if (behaviour.isPrologOutput() ||
					behaviour.isFastPrologOutput()) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(err, e);
			} else {
				err.println("Error parsing input file: " + e.getLocalizedMessage());
			}
			return -3;
		} catch (final RuntimeException e) {
			if (behaviour.isPrologOutput() ||
				behaviour.isFastPrologOutput() ) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(err, new BCompoundException(new BException(bfile.getAbsolutePath(), e.getMessage(), e)));
			} else {
				err.println("Error reading input file: " + e.getLocalizedMessage());
			}
			return -4;
		} catch (final StackOverflowError e) { // inherits from VirtualMachineError, Throwable
			if (behaviour.isPrologOutput() ||
				behaviour.isFastPrologOutput() ) { // Note: this will print regular Prolog in FastProlog mode
				System.out.println("Error (StackOverflowError) in parser: " + e.getLocalizedMessage());
				PrologExceptionPrinter.printException(err, new BCompoundException(new BException(bfile.getAbsolutePath(), "StackOverflowError" //+ e.getMessage() // message seems empty
				, e)));
				//e.printStackTrace(System.out); may produce itself a stack overflow??
			} else {
				err.println("Error (StackOverflowError) in parser: " + e.getLocalizedMessage());
			}
			return -5;
		} catch (final VirtualMachineError e) { // inherits from Throwable
			if (behaviour.isPrologOutput() ||
				behaviour.isFastPrologOutput() ) { // Note: this will print regular Prolog in FastProlog mode
				System.out.println("Error (VirtualMachineError) in parser: " + e.getLocalizedMessage());
				PrologExceptionPrinter.printException(err, new BCompoundException(new BException(bfile.getAbsolutePath(), "VirtualMachineError" //+ e.getMessage() // message seems empty
				, e)));
			} else {
				err.println("Error (VirtualMachineError) in parser: " + e.getLocalizedMessage());
			}
			return -6;
		}
	}

	private static IPrologTermOutput prologTermOutputForStream(final OutputStream out) {
		final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
		return new PrologTermOutput(writer, false);
	}

	private static void fullParsing(final File bfile, final ParsingBehaviour parsingBehaviour, final OutputStream out) throws IOException, BCompoundException {
		final BParser parser = new BParser(bfile.getAbsolutePath());

		final long startParseMain = System.currentTimeMillis();
		final Start tree = parser.parseFile(bfile, parsingBehaviour.isVerbose());
		final long endParseMain = System.currentTimeMillis();

		if (parsingBehaviour.isPrintTime() || parsingBehaviour.isVerbose()) { // -time flag in CliBParser
			System.out.println("% Time for parsing of main file: " + (endParseMain - startParseMain) + " ms");
		}

		if (parsingBehaviour.isPrettyPrintB()) { // -pp flag in CliBParser
			parsingBehaviour.debug_print("Pretty printing " + bfile + " in B format:");
			
			PrettyPrinter pp = new PrettyPrinter();
			tree.apply(pp);
			System.out.println(pp.getPrettyPrint());
		}

		// Note: if both -fastprolog and -prolog flag are used; only Fast Prolog AST will be printed
		if (parsingBehaviour.isPrologOutput() || parsingBehaviour.isFastPrologOutput()) {
			final long startParseRecursive = System.currentTimeMillis();
			final RecursiveMachineLoader rml = RecursiveMachineLoader.loadFromAst(parser, tree, parsingBehaviour, parser.getContentProvider());
			final long endParseRecursive = System.currentTimeMillis();

			if (parsingBehaviour.isPrintTime() || parsingBehaviour.isVerbose()) {
				System.out.println("% Time for parsing of referenced files: " + (endParseRecursive - startParseRecursive) + " ms");
			}

			final long startOutput = System.currentTimeMillis();
			if (parsingBehaviour.isFastPrologOutput()) { // -fastprolog flag in CliBParser
				System.out.println("Generating fastrw binary output");
				printASTasFastProlog(out, rml);
			} else { // -prolog flag in CliBParser
				final IPrologTermOutput pout = prologTermOutputForStream(out);
				rml.printAsProlog(pout);
				pout.flush();
			}
			final long endOutput = System.currentTimeMillis();

			if (parsingBehaviour.isPrintTime() || parsingBehaviour.isVerbose()) {
				System.out.println("% Time for Prolog output: " + (endOutput - startOutput) + " ms");
			}
		}

		if (parsingBehaviour.isPrintTime() && !parsingBehaviour.isFastPrologOutput()) {
			System.out.println("% Used memory : " + 
				(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/ 1000 + " KB");
			System.out.println("% Total memory: " + Runtime.getRuntime().totalMemory() / 1000 + " KB");
		}
	}

	/*
	write AST as facts in SICStus fastrw format
	parser_version(VERS).
	classical_b(NAME,[Files,...]).
	machine(). ....
	
	file can be read in Prolog with
	:- use_module(library(fastrw)).
	
	open(FILE,read,S,[type(binary)]),
	fast_read(S,ParserVersionTerm),
	fast_read(S,FilesTerm), ... until end_of_file
	close(S)
	
	TODO: catch StackOverflowError here and then empty/delete the file (to avoid partial terms)
	*/
	private static void printASTasFastProlog(final OutputStream out, final RecursiveMachineLoader rml) throws IOException {
		StructuredPrologOutput structuredPrologOutput = new StructuredPrologOutput();
		rml.printAsProlog(structuredPrologOutput);
		Collection<PrologTerm> sentences = structuredPrologOutput.getSentences();

		final BufferedOutputStream bufOut = new BufferedOutputStream(out);
		FastReadWriter fwriter = new FastReadWriter(bufOut);
		
		for (PrologTerm term : sentences) {
			fwriter.fastwrite(term);
		}
		
		bufOut.flush();
	}
	/* old version with transformer, seems slightly faster even though it builds up unnecessary intermediate term
			for (PrologTerm term : sentences) {
			StructuredPrologOutput output = new StructuredPrologOutput();
			output.printTerm(term);
			output.fullstop();
			FastReadTransformer transformer = new FastReadTransformer(output);
			out.print(transformer.write());
	*/
			
			

	private static void parseRulesProject(final File mainFile, final ParsingBehaviour parsingBehaviour, final OutputStream out) throws BCompoundException {
		RulesProject project = new RulesProject();
		project.setParsingBehaviour(parsingBehaviour);
		project.parseProject(mainFile);
		project.checkAndTranslateProject();

		if (project.hasErrors()) {
			throw new BCompoundException(project.getBExceptionList());
		}

		final IPrologTermOutput pout = prologTermOutputForStream(out);
		project.printProjectAsPrologTerm(pout);
		pout.flush();
	}

	private static ConsoleOptions createConsoleOptions(final String[] args) {
		final ConsoleOptions options = new ConsoleOptions();
		options.setIntro("BParser (version " + BParser.getVersion() + ", commit " + BParser.getGitSha()
				+ ")\nusage: BParser [options] <BMachine file>\n\nAvailable options are:");
		options.addOption(CLI_SWITCH_VERBOSE, "Verbose output during lexing and parsing");
		options.addOption(CLI_SWITCH_TIME, "Output time used for complete parsing process");
		options.addOption(CLI_SWITCH_PP, "Pretty Print in B format on standard output");
		options.addOption(CLI_SWITCH_PROLOG, "Show AST as Prolog term");
		// TO DO: add option for less precise position infos
		options.addOption(CLI_SWITCH_PROLOG_LINES, "Put line numbers into prolog terms");
		options.addOption(CLI_SWITCH_OUTPUT, "Specify output file", 1);
		options.addOption(CLI_SWITCH_VERSION, "Print the parser version and exit");
		options.addOption(CLI_SWITCH_HELP, "Print the parser help and exit");
		options.addOption(CLI_SWITCH_HELP2, "Print the parser help and exit");
		options.addOption(CLI_SWITCH_HELP3, "Print the parser help and exit");
		options.addOption(CLI_SWITCH_COMPACT_POSITIONS, "Use new more compact Prolog position terms");
		options.addOption(CLI_SWITCH_FASTPROLOG,
				"Show AST as Prolog term for fast loading (Do not use this representation in your tool! It depends on internal representation of Sicstus Prolog and will very likely change arbitrarily in the future!)");
		options.addOption(CLI_SWITCH_PREPL, "Enter parser-repl. Should only be used from inside ProB's Prolog Core.");
		options.addOption(CLI_SWITCH_NAME_CHECK,
				"The name of a machine have to match file name (except for the file name extension)");
		options.addOption(CLI_SWITCH_PRINT_STACK_SIZE, "print the locally available size of the call stack at runtime");
		try {
			options.parseOptions(args);
		} catch (final IllegalArgumentException e) {
			System.err.println(e.getLocalizedMessage());
			options.printUsage(System.err);
			System.exit(-1);
		}
		return options;
	}
}
