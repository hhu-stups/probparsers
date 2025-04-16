package de.prob.cliparser;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ClassicalBParser;
import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.MockedDefinitions;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.NodeFileNumbers;
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.rules.RulesProject;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.be4.ltl.core.parser.CtlParser;
import de.be4.ltl.core.parser.LtlParseException;
import de.be4.ltl.core.parser.LtlParser;
import de.be4.ltl.core.parser.TemporalLogicParser;
import de.prob.parserbase.JoinedParserBase;
import de.prob.parserbase.ProBParserBase;
import de.prob.parserbase.UnparsedParserBase;
import de.prob.prolog.output.FastSicstusTermOutput;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;
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

	private static final UnparsedParserBase UNPARSED_PARSER_BASE = new UnparsedParserBase("unparsed_expr", "unparsed_pred", "unparsed_trans");

	private static int getStackSize(int acc) {
		try {
			return CliBParser.getStackSize(acc + 1);
		} catch (StackOverflowError ignored) {
			return acc;
		}
	}

	/**
	 * Main method wrapper.
	 * This is necessary because of <a href="https://github.com/oracle/graal/issues/3398">a bug with graalvm and musl</a>.
	 * Workaround inspired by: <a href="https://github.com/babashka/babashka/issues/831">babashka/babashka#831</a>
	 */
	public static void main(final String[] args) throws Exception {
		AtomicReference<Exception> maybeException = new AtomicReference<>(null);
		Thread t = new Thread(()->{
			try {
				CliBParser.mainImpl(args);
			} catch (Exception e) {
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
		final ConsoleOptions options = createConsoleOptions(args);
		
		if (options.isOptionSet(CLI_SWITCH_HELP) ||
				options.isOptionSet(CLI_SWITCH_HELP2) ||
				options.isOptionSet(CLI_SWITCH_HELP3)) {
			options.printUsage(System.err);
			System.exit(-1);
			return;
		}
		
		if (options.isOptionSet(CLI_SWITCH_VERSION)) {
			System.out.printf("Version:    %s%n", BParser.getVersion());
			System.out.printf("Git Commit: %s%n", BParser.getGitSha());
			System.exit(0);
			return;
		}

		if (options.isOptionSet(CLI_SWITCH_PRINT_STACK_SIZE)) {
			System.out.format("Local stack size:\t%d\n", CliBParser.getStackSize(0));
			System.exit(0);
			return;
		}

		final String[] arguments = options.getRemainingOptions();
		if (!options.isOptionSet(CLI_SWITCH_PREPL) && arguments.length != 1) {
			System.err.println("\nYou have not provided a file to parse (nor specified the -prepl option).\n");
			System.err.println("Here is how to use the parser:");
			options.printUsage(System.err);
			System.exit(-1);
			return;
		}

		final ParsingBehaviour behaviour = new ParsingBehaviour();
		behaviour.setPrintTime(options.isOptionSet(CLI_SWITCH_TIME));
		behaviour.setPrologOutput(options.isOptionSet(CLI_SWITCH_PROLOG));
		behaviour.setAddLineNumbers(options.isOptionSet(CLI_SWITCH_PROLOG_LINES)); // -lineno flag
		behaviour.setPrettyPrintB(options.isOptionSet(CLI_SWITCH_PP)); // -pp flag
		behaviour.setVerbose(options.isOptionSet(CLI_SWITCH_VERBOSE)); // -v flag
		behaviour.setFastPrologOutput(options.isOptionSet(CLI_SWITCH_FASTPROLOG));
		behaviour.setCompactPrologPositions(options.isOptionSet(CLI_SWITCH_COMPACT_POSITIONS));
		behaviour.setMachineNameMustMatchFileName(options.isOptionSet(CLI_SWITCH_NAME_CHECK));
		// TODO: check if some other flags are not recognised

		if (options.isOptionSet(CLI_SWITCH_PREPL)) {
			runPRepl(behaviour);
		} else {
			// there should be just one remaining argument
			// otherwise no filename was provided, or some arguments were not
			// parsed correctly
			if (options.getRemainingOptions().length != 1) {
				options.printUsage(System.err);
				System.exit(-1);
				return;
			}

			File bfile = new File(options.getRemainingOptions()[0]);
			@SuppressWarnings("ImplicitDefaultCharsetUsage") // System.err really uses the default charset
			PrintWriter err = new PrintWriter(System.err, true);

			if (options.isOptionSet(CLI_SWITCH_OUTPUT)) {
				final String filename = options.getOptions(CLI_SWITCH_OUTPUT)[0];
				try (OutputStream out = Files.newOutputStream(Paths.get(filename))) {
					int returnValue = doFileParsing(behaviour, out, err, bfile);
					out.flush();
					err.flush();
					System.exit(returnValue);
				} catch (IOException e) {
					// Note: This should only catch exceptions from the creation of the OutputStream.
					// All other IOExceptions are caught internally by doFileParsing.
					if (options.isOptionSet(CLI_SWITCH_PROLOG)) {
						PrologExceptionPrinter.printException(System.err, e);
					} else {
						System.err.println("Unable to create file '" + filename + "'");
					}
					System.exit(-1);
				}
			} else {
				int returnValue = doFileParsing(behaviour, System.out, err, bfile);
				System.out.flush();
				err.flush();
				System.exit(returnValue);
			}
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
			case "defaultFileNumber":  // default in ParsingBehaviour.java: -1
				behaviour.setDefaultFileNumber(Integer.parseInt(value));
				break;
			case "startLineNumber":  // default in ParsingBehaviour.java: 1
				behaviour.setStartLineNumber(Integer.parseInt(value));
				break;
			case "startColumnNumber": // default in ParsingBehaviour.java: 1
				behaviour.setStartColumnNumber(Integer.parseInt(value));
				break;
			default:
				// Unknown/unsupported option
				return false;
		}
		return true;
	}
	
	private static void resetVolatilePositionOptions(ParsingBehaviour behaviour) {
		// reset volatile options to default (usually after one parse command)
		// we typically never parse twice at the same location
		// and setting back these options after every formula parse adds a noticeable overhead
		// when many small formulas need to be parsed (e.g., for VisB JSON files)
		behaviour.setStartLineNumber(1);
		behaviour.setStartColumnNumber(1);
	}
	
	private static void runPRepl(ParsingBehaviour behaviour) throws IOException {
		ServerSocket serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
		// write port number as prolog term
		System.out.println(serverSocket.getLocalPort() + ".");
		Socket socket = serverSocket.accept();
		// socket.setTcpNoDelay(true); // does not seem to provide any response benefit

		// with autoFlush
		PrintWriter socketWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);

		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		String line;
		MockedDefinitions context = new MockedDefinitions();
		boolean terminate = false;
		while (!terminate) {
			line = in.readLine();

			EPreplCommands command;
			if (line == null) {
				// the prob instance has been terminated. exit gracefully
				command = EPreplCommands.halt;
			} else {
				command = EPreplCommands.valueOf(line);
			}
			
			debugPrint(behaviour, "Received PREPL command: " + command);
			switch (command) {
				case version:
					socketWriter.println(BParser.getVersion() + "-" + BParser.getGitSha());
					break;
				case shortversion:
					socketWriter.println(BParser.getVersion());
					break;
				case gitsha:
					socketWriter.println(BParser.getGitSha());
					break;
				case commandsupported:
					// Check if the given command is supported by this version of the parser.
					String commandToCheck = in.readLine();
					try {
						EPreplCommands.valueOf(commandToCheck);
					} catch (IllegalArgumentException ignored) {
						socketWriter.println("false.");
						break;
					}
					socketWriter.println("true.");
					break;
				case featuresupported:
					// Check if the given feature is supported by this version of the parser.
					// There are no features defined yet, but we already support this command for future-proofing.
					socketWriter.println("false.");
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
					break;
				// new commands to change parsingBehaviour, analog to command-line switches
				case fastprolog:
					String newFVal = in.readLine();
					debugPrint(behaviour, "Setting fastprolog to " + newFVal);
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
					resetVolatilePositionOptions(behaviour); // no sense in providing col,line; TODO: reset file?
					String filename = in.readLine();
					Path outFile = Paths.get(in.readLine());
					final File bfile = new File(filename);
					final int returnValue;
					try (final OutputStream out = Files.newOutputStream(outFile)) {
						returnValue = doFileParsing(behaviour, out, socketWriter, bfile);
					}
					context = new MockedDefinitions(); // reset definitions

					// Notify probcli that the call finished successfully.
					// If an exception was thrown, doFileParsing will have already printed an appropriate error message/term.
					if (returnValue == 0) {
						socketWriter.println("exit(" + returnValue + ").");
					} else if (returnValue <= -4) { // VM/StackOverflow error occurred; file is probably corrupt
						System.out.println("Erasing file contents of " + outFile);
						Files.write(outFile, Collections.singletonList("% VM Error occurred"));
					}
					break;
				case formula:
				case expression:
				case predicate:
				case substitution:
					String theFormula = in.readLine();
					parseFormula(command, theFormula, context, behaviour, socketWriter);
					resetVolatilePositionOptions(behaviour);
					break;
				case ltl:
					String extension = in.readLine();
					final ProBParserBase extParser = getExtensionParser(extension, context);
					final TemporalLogicParser<?> parser = new LtlParser(extParser);
					parseTemporalFormula(in.readLine(), parser, socketWriter);
					resetVolatilePositionOptions(behaviour); // TODO: pass behaviour to LTL parser above
					break;
				case ctl:
					String extension2 = in.readLine();
					final ProBParserBase extParser2 = getExtensionParser(extension2, context);
					final TemporalLogicParser<?> parser2 = new CtlParser(extParser2);
					parseTemporalFormula(in.readLine(), parser2, socketWriter);
					resetVolatilePositionOptions(behaviour); // TODO: pass behaviour to CTL parser above
					break;
				case halt:
					socket.close();
					serverSocket.close();
					terminate = true;
					break;
				default:
					throw new UnsupportedOperationException("Unsupported Command " + line);
			}
			System.out.flush();
			System.err.flush();
		}
	}

	private static ProBParserBase getExtensionParser(final String pattern, IDefinitions context) {
		final String[] langs = pattern.split(",");
		final ProBParserBase[] sublangs = new ProBParserBase[langs.length];
		for (int i = 0; i < langs.length; i++) {
			final String lang = langs[i];
			final ProBParserBase sub;
			if ("none".equals(lang)) {
				sub = UNPARSED_PARSER_BASE;
			} else if ("B".equals(lang)) {
				BParser bparser = new BParser();
				if (context!=null) {
					bparser.setDefinitions(context); // ensure that DEFINITION predicates, ... are available
				}
				sub = new ClassicalBParser(bparser);
			} else {
				throw new IllegalArgumentException("Unknown language " + lang);
			}
			sublangs[i] = sub;
		}

		if (sublangs.length == 1) {
			return sublangs[0];
		} else {
			return new JoinedParserBase(sublangs);
		}
	}

	private static void parseTemporalFormula(String theFormula, TemporalLogicParser<?> parser, Writer out) {
		final IPrologTermOutput pout = new PrologTermOutput(out, false);
		try {
			final PrologTerm term = parser.generatePrologTerm(theFormula, null);
			pout.openTerm("ltl").printTerm(term).closeTerm();
		} catch (LtlParseException e) {
			pout.openTerm("syntax_error").printAtom(e.getMessage()).closeTerm();
		} catch (Throwable e) {
			PrologExceptionPrinter.printException(pout, new BCompoundException(new BException(null, e.toString(), e)));
		}

		pout.fullstop();
	}

	private static void parseFormula(EPreplCommands command, String theFormula, IDefinitions context, ParsingBehaviour behaviour, Writer out) {
		final IPrologTermOutput pout = new PrologTermOutput(out, false);
		try {
			BParser parser = new BParser();
			parser.setStartPosition(behaviour.getStartLineNumber(), behaviour.getStartColumnNumber());
			parser.setDefinitions(context);
			Start start;
			switch (command) {
				case formula:
					start = parser.parseFormula(theFormula);
					break;

				case expression:
					start = parser.parseExpression(theFormula);
					break;

				case predicate:
					start = parser.parsePredicate(theFormula);
					break;

				case substitution:
					start = parser.parseSubstitution(theFormula);
					break;

				default:
					throw new AssertionError("Unhandled parsing command: " + command);
			}

			INodeIds nodeIds = new NodeFileNumbers();
			if (behaviour.getDefaultFileNumber() != -1) {
				nodeIds.assignIdentifiers(behaviour.getDefaultFileNumber(), start);
			}

			ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIds);
			pprinter.setPrintSourcePositions(behaviour.isAddLineNumbers(), behaviour.isCompactPrologPositions());
			ASTProlog printer = new ASTProlog(pout, pprinter);

			start.apply(printer);
		} catch (BCompoundException e) {
			PrologExceptionPrinter.printException(pout, e);
		} catch (Throwable e) {
			PrologExceptionPrinter.printException(pout, new BCompoundException(new BException(null, e.toString(), e)));
		}

		pout.fullstop();
	}

	private static void debugPrint(ParsingBehaviour parsingBehaviour, String msg) {
		if (parsingBehaviour.isVerbose()) {
			if (parsingBehaviour.shouldPrintProlog()) {
				msg = "% " + msg;
			}
			System.out.println(msg);
		}
	}

	private static int doFileParsing(final ParsingBehaviour behaviour, final OutputStream out, final PrintWriter err, final File bfile) {
		try {
			if (bfile.getName().endsWith(".rmch")) {
				parseRulesProject(bfile, behaviour, out);
			} else {
				fullParsing(bfile, behaviour, out);
			}
			return 0;
		} catch (IOException | UncheckedIOException e) {
			IOException exc;
			if (!(e instanceof IOException)) {
				exc = (IOException) e.getCause();
			} else {
				exc = (IOException) e;
			}
			if (behaviour.shouldPrintProlog()) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(err, exc);
			} else {
				err.println("Error reading input file: " + exc);
			}
			return -2;
		} catch (BCompoundException e) {
			if (behaviour.shouldPrintProlog()) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(err, e);
			} else {
				err.println("Error parsing input file: " + e);
			}
			return -3;
		} catch (Throwable e) {
			if (behaviour.shouldPrintProlog()) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(err, new BCompoundException(new BException(bfile.getAbsolutePath(), e.toString(), e)));
			} else {
				err.println("Error in parser: " + e);
			}
			return -4;
		}
	}

	private static void printPrologAst(ParsingBehaviour parsingBehaviour, OutputStream out, Consumer<? super IPrologTermOutput> printer) {
		final long startOutput = System.currentTimeMillis();
		if (parsingBehaviour.isFastPrologOutput()) { // -fastprolog flag in CliBParser
			// TODO: support swi fastrw format
			printASTasFastProlog(out, printer);
		} else { // -prolog flag in CliBParser
			assert parsingBehaviour.isPrologOutput();
			IPrologTermOutput pto = new PrologTermOutput(out, false);
			printer.accept(pto);
		}
		final long endOutput = System.currentTimeMillis();

		if (parsingBehaviour.isPrintTime() || parsingBehaviour.isVerbose()) {
			System.out.println("% Time for Prolog output: " + (endOutput - startOutput) + " ms");
		}
	}

	private static void fullParsing(final File bfile, final ParsingBehaviour parsingBehaviour, final OutputStream out) throws IOException, BCompoundException {
		final BParser parser = new BParser(bfile.getAbsolutePath());

		final long startParseMain = System.currentTimeMillis();
		debugPrint(parsingBehaviour, "*** Debug: Parsing file '" + bfile + "'");
		final Start tree = parser.parseFile(bfile);
		final long endParseMain = System.currentTimeMillis();

		if (parsingBehaviour.isPrintTime() || parsingBehaviour.isVerbose()) { // -time flag in CliBParser
			System.out.println("% Time for parsing of main file: " + (endParseMain - startParseMain) + " ms");
		}

		if (parsingBehaviour.isPrettyPrintB()) { // -pp flag in CliBParser
			debugPrint(parsingBehaviour, "Pretty printing " + bfile + " in B format:");
			
			PrettyPrinter pp = new PrettyPrinter();
			pp.setUseIndentation(true);
			tree.apply(pp);
			System.out.println(pp.getPrettyPrint());
		}

		// Note: if both -fastprolog and -prolog flag are used; only Fast Prolog AST will be printed
		if (parsingBehaviour.shouldPrintProlog()) {
			final long startParseRecursive = System.currentTimeMillis();
			final RecursiveMachineLoader rml = RecursiveMachineLoader.loadFromAst(parser, tree, parsingBehaviour, parser.getContentProvider());
			final long endParseRecursive = System.currentTimeMillis();

			if (parsingBehaviour.isPrintTime() || parsingBehaviour.isVerbose()) {
				System.out.println("% Time for parsing of referenced files: " + (endParseRecursive - startParseRecursive) + " ms");
			}

			printPrologAst(parsingBehaviour, out, rml::printAsProlog);
		}

		if (parsingBehaviour.isVerbose()) {
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
	*/
	private static void printASTasFastProlog(OutputStream out, Consumer<? super IPrologTermOutput> printer) {
		IPrologTermOutput pto = new FastSicstusTermOutput(new BufferedOutputStream(out));
		printer.accept(pto);
	}

	private static void parseRulesProject(final File mainFile, final ParsingBehaviour parsingBehaviour, final OutputStream out) throws IOException, BCompoundException {
		RulesProject project = new RulesProject();
		project.setParsingBehaviour(parsingBehaviour);
		project.parseProject(mainFile);
		project.checkAndTranslateProject();

		if (project.hasErrors()) {
			throw new BCompoundException(project.getBExceptionList());
		}

		printPrologAst(parsingBehaviour, out, project::printProjectAsPrologTerm);
	}

	private static ConsoleOptions createConsoleOptions(final String[] args) {
		final ConsoleOptions options = new ConsoleOptions();
		options.setIntro("BParser (version " + BParser.getVersion() + ", commit " + BParser.getGitSha()
				+ ")\nusage: java -jar probcliparser.jar [options] <BMachine file>\n\nAvailable options are:");
		options.addOption(CLI_SWITCH_VERBOSE, "Verbose output during lexing and parsing");
		options.addOption(CLI_SWITCH_TIME, "Output time used for complete parsing process");
		options.addOption(CLI_SWITCH_PP, "Pretty Print in B format on standard output");
		options.addOption(CLI_SWITCH_PROLOG, "Show AST as Prolog term");
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
		options.addOption(CLI_SWITCH_PRINT_STACK_SIZE, "print the locally available depth of the call stack at runtime");
		try {
			options.parseOptions(args);
		} catch (final IllegalArgumentException e) {
			System.err.println(e.getMessage());
			options.printUsage(System.err);
			System.exit(-1);
		}
		return options;
	}
}
