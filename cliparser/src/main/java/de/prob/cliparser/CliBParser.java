package de.prob.cliparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.FastReadTransformer;
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
import de.prob.prolog.output.PrologTermStringOutput;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;

public class CliBParser {

	private static final String CLI_SWITCH_VERBOSE = "-v";
	private static final String CLI_SWITCH_VERSION = "-version";
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

	private static final String encoding = "UTF-8";

	private static Socket socket;
	private static PrintWriter socketWriter;

	public static void main(final String[] args) throws IOException {
		// System.out.println("Ready. Press enter");
		// System.in.read();
		// System.out.println("Starting");
		final ConsoleOptions options = createConsoleOptions(args);

		if (options.isOptionSet(CLI_SWITCH_VERSION)) {
			System.out.println(String.format("Version:    %s", BParser.getVersion()));
			System.out.println(String.format("Git Commit: %s", BParser.getGitSha()));
			System.exit(0);
		}

		final String[] arguments = options.getRemainingOptions();
		if (!options.isOptionSet(CLI_SWITCH_PREPL) && arguments.length != 1) {
			options.printUsage(System.err);
			System.exit(-1);
		}

		final ParsingBehaviour behaviour = new ParsingBehaviour();

		PrintWriter out;
		if (options.isOptionSet(CLI_SWITCH_OUTPUT)) {
			final String filename = options.getOptions(CLI_SWITCH_OUTPUT)[0];

			try {
				out = new PrintWriter(Files.newBufferedWriter(Paths.get(filename)));
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
			out = new PrintWriter(System.out);
		}
		behaviour.setPrintTime(options.isOptionSet(CLI_SWITCH_TIME));
		behaviour.setPrologOutput(options.isOptionSet(CLI_SWITCH_PROLOG));
		behaviour.setAddLineNumbers(options.isOptionSet(CLI_SWITCH_PROLOG_LINES)); // -lineno flag
		behaviour.setPrettyPrintB(options.isOptionSet(CLI_SWITCH_PP)); // -pp flag
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

	private static void runPRepl(final ParsingBehaviour behaviour) throws IOException, FileNotFoundException {
		ServerSocket serverSocket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
		// write port number as prolog term
		System.out.println(serverSocket.getLocalPort() + ".");
		socket = serverSocket.accept();
		// socket.setTcpNoDelay(true); // does not seem to provide any response benefit
		socketWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), encoding)));

		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), encoding));
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
			case machine:
				String filename = in.readLine();
				String outFile = in.readLine();
				final PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get(outFile)));
				final File bfile = new File(filename);

				final StringWriter err = new StringWriter();
				final PrintWriter pw = new PrintWriter(err);
				int returnValue = doFileParsing(behaviour, out, pw, bfile);
				pw.flush();
				context = new MockedDefinitions();
				out.close();

				if (returnValue == 0) {
					print("exit(" + returnValue + ")." + System.lineSeparator());
				} else {
					String output = err.toString().replace(System.lineSeparator(), " ").trim();
					print(output + System.lineSeparator());
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
		PrologTermStringOutput strOutput = new PrologTermStringOutput();
		theFormula = in.readLine();

		try {
			final PrologTerm term = parser.generatePrologTerm(theFormula, null);
			strOutput.openTerm("ltl").printTerm(term).closeTerm();
		} catch (LtlParseException e) {
			strOutput.openTerm("syntax_error").printAtom(e.getLocalizedMessage()).closeTerm();
		}

		strOutput.fullstop();

		// A Friendly Reminder: strOutput includes a newline!
		print(strOutput.toString());
	}

	private static void parseFormulaInternal(String theFormula, IDefinitions context, final ParsingBehaviour behaviour, final boolean extended) {
		try {
			BParser parser = new BParser();
			parser.setDefinitions(context);
			Start start;
			if (extended) {
				start = parser.eparse(theFormula, context);
			} else {
				start = parser.parse(theFormula, false); // debugOutput=false
			}

			PrologTermStringOutput strOutput = new PrologTermStringOutput();

			// In the compact position format, node IDs are not used,
			// so generate them only if the old non-compact format is requested.
			final INodeIds nodeIds;
			if (behaviour.isCompactPrologPositions()) {
				nodeIds = new NodeFileNumbers();
			} else {
				final NodeIdAssignment na = new NodeIdAssignment();
				start.apply(na);
				nodeIds = na;
			}

			ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIds, -1, 0);
			pprinter.setPrintSourcePositions(behaviour.isAddLineNumbers(),
			                                 behaviour.isCompactPrologPositions());
			ASTProlog printer = new ASTProlog(strOutput, pprinter);

			start.apply(printer);
			strOutput.fullstop();

			// A Friendly Reminder: strOutput includes a newline!
			print(strOutput.toString());
		} catch (NullPointerException e) {
			// Not Parseable - Sadly, calling e.getLocalizedMessage() on the
			// NullPointerException returns NULL itself, thus triggering another
			// NullPointerException in the catch statement. Therefore we need a
			// second catch statement with a special case for the
			// NullPointerException instead of catching a general Exception
			// print("EXCEPTION NullPointerException" + System.lineSeparator());
			PrologTermStringOutput strOutput = new PrologTermStringOutput();
			strOutput.openTerm("exception").printAtom("NullPointerException").closeTerm();
			strOutput.fullstop();
			strOutput.flush();
			print(strOutput.toString());
		} catch (BCompoundException e) {
			final IPrologTermOutput pto = new PrologTermOutput(socketWriter, false);
			PrologExceptionPrinter.printException(pto, e.withLinesOneOff());
			pto.fullstop();
			pto.flush();
		} catch (LexerException e) {
			PrologTermStringOutput strOutput = new PrologTermStringOutput();
			strOutput.openTerm("exception").printAtom(e.getLocalizedMessage()).closeTerm();
			strOutput.fullstop();
			strOutput.flush();
			print(strOutput.toString());
		} catch (IOException e) {
			final IPrologTermOutput pto = new PrologTermOutput(socketWriter, false);
			PrologExceptionPrinter.printException(pto, e);
			pto.fullstop();
			pto.flush();
		}
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

	private static int doFileParsing(final ParsingBehaviour behaviour, final PrintWriter out, final PrintWriter err, final File bfile) {
		try {
			if (bfile.getName().endsWith(".rmch")) {
				parseRulesProject(bfile, behaviour, out);
			} else {
				fullParsing(bfile, behaviour, out);
			}
			return 0;
		} catch (final IOException e) {
			if (behaviour.isPrologOutput() ||
					behaviour.isFastPrologOutput() ) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(new PrologTermOutput(err, false), e);
			} else {
				err.println();
				err.println("Error reading input file: " + e.getLocalizedMessage());
			}
			return -2;
		} catch (final BCompoundException e) {
			if (behaviour.isPrologOutput() ||
					behaviour.isFastPrologOutput()) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(new PrologTermOutput(err, false), e);
			} else {
				err.println();
				err.println("Error parsing input file: " + e.getLocalizedMessage());
			}
			return -3;
		} catch (final RuntimeException e) {
			if (behaviour.isPrologOutput() ||
				behaviour.isFastPrologOutput() ) { // Note: this will print regular Prolog in FastProlog mode
				PrologExceptionPrinter.printException(new PrologTermOutput(err, false), new BCompoundException(new BException(bfile.getAbsolutePath(), e.getMessage(), e)));
			} else {
				err.println();
				err.println("Error reading input file: " + e.getLocalizedMessage());
			}
			return -4;
		}
	}

	private static void fullParsing(final File bfile, final ParsingBehaviour parsingBehaviour, final PrintWriter out) throws IOException, BCompoundException {
		final BParser parser = new BParser(bfile.getAbsolutePath());

		final long startParseMain = System.currentTimeMillis();
		final Start tree = parser.parseFile(bfile, parsingBehaviour.isVerbose());
		final long endParseMain = System.currentTimeMillis();

		if (parsingBehaviour.isPrintTime()) { // -time flag in CliBParser
			out.println("% Time for parsing of main file: " + (endParseMain - startParseMain) + " ms");
		}

		if (parsingBehaviour.isPrettyPrintB()) { // -pp flag in CliBParser
			if (parsingBehaviour.isVerbose()) {
				System.out.println("Pretty printing " + bfile + " in B format:");
			}
			PrettyPrinter pp = new PrettyPrinter();
			tree.apply(pp);
			System.out.println(pp.getPrettyPrint());
		}

		// Note: if both -fastprolog and -prolog flag are used; only Fast Prolog AST will be printed
		if (parsingBehaviour.isPrologOutput() || parsingBehaviour.isFastPrologOutput()) {
			final long startParseRecursive = System.currentTimeMillis();
			final RecursiveMachineLoader rml = RecursiveMachineLoader.loadFromAst(parser, tree, parsingBehaviour, parser.getContentProvider());
			final long endParseRecursive = System.currentTimeMillis();

			if (parsingBehaviour.isPrintTime()) {
				out.println("% Time for parsing of referenced files: " + (endParseRecursive - startParseRecursive) + " ms");
			}

			final long startOutput = System.currentTimeMillis();
			if (parsingBehaviour.isFastPrologOutput()) { // -fastprolog flag in CliBParser
				printASTasFastProlog(out, rml);
			} else { // -prolog flag in CliBParser
				rml.printAsProlog(out);
			}
			final long endOutput = System.currentTimeMillis();

			if (parsingBehaviour.isPrintTime()) {
				out.println("% Time for Prolog output: " + (endOutput - startOutput) + " ms");
			}
		}

		if (parsingBehaviour.isPrintTime()) {
			out.println("% Used memory : " + 
				(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/ 1000 + " KB");
			out.println("% Total memory: " + Runtime.getRuntime().totalMemory() / 1000 + " KB");
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
	private static void printASTasFastProlog(final PrintWriter out, final RecursiveMachineLoader rml) {
		StructuredPrologOutput structuredPrologOutput = new StructuredPrologOutput();
		rml.printAsProlog(structuredPrologOutput);
		Collection<PrologTerm> sentences = structuredPrologOutput.getSentences();

		for (PrologTerm term : sentences) {
			StructuredPrologOutput output = new StructuredPrologOutput();
			output.printTerm(term);
			output.fullstop();
			FastReadTransformer transformer = new FastReadTransformer(output);
			out.print(transformer.write());
		}
	}

	private static void parseRulesProject(final File mainFile, final ParsingBehaviour parsingBehaviour, final PrintWriter out) throws BCompoundException {
		RulesProject project = new RulesProject();
		project.setParsingBehaviour(parsingBehaviour);
		project.parseProject(mainFile);
		project.checkAndTranslateProject();

		if (project.hasErrors()) {
			throw new BCompoundException(project.getBExceptionList());
		}

		final IPrologTermOutput pout = new PrologTermOutput(out, false);
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
		options.addOption(CLI_SWITCH_VERSION, "Print the parser version and exit.");
		options.addOption(CLI_SWITCH_COMPACT_POSITIONS, "Use new more compact Prolog position terms");
		options.addOption(CLI_SWITCH_FASTPROLOG,
				"Show AST as Prolog term for fast loading (Do not use this representation in your tool! It depends on internal representation of Sicstus Prolog and will very likely change arbitrarily in the future!)");
		options.addOption(CLI_SWITCH_PREPL, "Enter parser-repl. Should only be used from inside ProB's Prolog Core.");
		options.addOption(CLI_SWITCH_NAME_CHECK,
				"The name of a machine have to match file name (except for the file name extension)");
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
