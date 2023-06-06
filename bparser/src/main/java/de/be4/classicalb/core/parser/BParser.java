package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.be4.classicalb.core.parser.analysis.checking.ClausesCheck;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionCollector;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionUsageCheck;
import de.be4.classicalb.core.parser.analysis.checking.IdentListCheck;
import de.be4.classicalb.core.parser.analysis.checking.RefinedOperationCheck;
import de.be4.classicalb.core.parser.analysis.checking.SemanticCheck;
import de.be4.classicalb.core.parser.analysis.checking.SemicolonCheck;
import de.be4.classicalb.core.parser.analysis.transforming.OpSubstitutions;
import de.be4.classicalb.core.parser.analysis.transforming.SyntaxExtensionTranslator;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.exceptions.VisitorException;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.Token;
import de.be4.classicalb.core.parser.parser.Parser;
import de.be4.classicalb.core.parser.parser.ParserException;
import de.be4.classicalb.core.parser.util.DebugPrinter;
import de.be4.classicalb.core.parser.util.Utils;

public class BParser {

	public static final String EXPRESSION_PREFIX = "#EXPRESSION";
	public static final String PREDICATE_PREFIX = "#PREDICATE";
	public static final String FORMULA_PREFIX = "#FORMULA";
	public static final String SUBSTITUTION_PREFIX = "#SUBSTITUTION";
	public static final String OPERATION_PATTERN_PREFIX = "#OPPATTERN";

	private static final Properties buildProperties;
	static {
		buildProperties = new Properties();
		final InputStream is = BParser.class.getResourceAsStream("build.properties");
		if (is == null) {
			throw new IllegalStateException("Build properties not found, this should never happen!");
		} else {
			try (final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8)) {
				buildProperties.load(r);
			} catch (final IOException e) {
				throw new IllegalStateException("IOException while loading build properties, this should never happen!", e);
			}
		}
	}

	private IDefinitions definitions = new Definitions();
	private ParseOptions parseOptions;

	private List<String> doneDefFiles = new ArrayList<>();

	private final String fileName;
	private File directory;

	private int startLine;
	private int startColumn;

	private IDefinitionFileProvider contentProvider;

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}

	public static String getGitSha() {
		return buildProperties.getProperty("git");
	}

	public BParser() {
		this((String) null);
	}

	public BParser(final String fileName) {
		this(fileName, new ParseOptions());
	}

	public BParser(final String fileName, ParseOptions parseOptions) {
		this.fileName = fileName;
		this.parseOptions = parseOptions;

		this.startLine = 1;
		this.startColumn = 1;
	}

	/**
	 * Pretend that the code being parsed starts at a different position than the start of the file (line 1, column 1).
	 * This is useful when the code being parsed is actually part of a larger input,
	 * for example embedded B predicates inside an LTL formula.
	 * The line and column numbers may also be less than 1,
	 * which is useful when the code has an artificial prefix
	 * that shouldn't be counted as part of the actual input.
	 * 
	 * @param line the new starting line number (1-based)
	 * @param column the new starting column number (1-based)
	 */
	public void setStartPosition(final int line, final int column) {
		this.startLine = line;
		this.startColumn = column;
	}

	public IDefinitionFileProvider getContentProvider() {
		return contentProvider;
	}

	/**
	 * Parses the input file.
	 * 
	 * @see #parse(String, boolean, IFileContentProvider)
	 * @param machineFile
	 *            the machine file to be parsed
	 * @param verbose
	 *            print debug information
	 * @return the start AST node
	 * @throws IOException
	 *             if the file cannot be read
	 * @throws BCompoundException
	 *             if the file cannot be parsed
	 */
	public Start parseFile(final File machineFile, final boolean verbose) throws IOException, BCompoundException {
		contentProvider = new CachingDefinitionFileProvider();
		return parseFile(machineFile, verbose, contentProvider);
	}

	/**
	 * Parses the input file.
	 * 
	 * @see #parse(String, boolean)
	 * @param machineFile
	 *            the machine file to be parsed
	 * @param verbose
	 *            print debug information
	 * @param contentProvider
	 *            used to get the content of files
	 * @return the AST node
	 * @throws IOException
	 *             if the file cannot be read
	 * @throws BCompoundException
	 *             if the file cannot be parsed
	 */
	public Start parseFile(final File machineFile, final boolean verbose, final IFileContentProvider contentProvider)
			throws IOException, BCompoundException {
		this.directory = machineFile.getParentFile();
		if (verbose) {
			DebugPrinter.println("Parsing file '" + machineFile.getCanonicalPath() + "'");
		}
		String content = Utils.readFile(machineFile);
		return parse(content, verbose, true, contentProvider);
	}

	/**
	 * Use this method, if you only need to parse small inputs. This only gives
	 * the AST or an Exception, but no information about source positions. If
	 * you need those, call the instance method of BParser instead. Do NOT use
	 * this method to parse formulas, predicates and expression. Use the
	 * corresponding instance methods instead.
	 * 
	 * @param input
	 *            the B machine as input string
	 * @return AST of the input
	 * @throws BCompoundException
	 *             if the B machine can not be parsed
	 */
	public static Start parse(final String input) throws BCompoundException {
		BParser parser = new BParser("String Input");
		return parser.parse(input, false, true, new NoContentProvider());
	}

	private Start parseWithKindPrefix(final String input, final String prefix) throws BCompoundException {
		final String theFormula = prefix + " " + input;
		final int oldStartColumn = this.startColumn;
		try {
			// Decrease the start column by the size of the implicitly added prefix
			// so that the actual user input starts at the desired position.
			this.startColumn -= prefix.length() + 1;
			return this.parse(theFormula, false, 
				false, // pre-parsing is not required; there can be no DEFINITIONS inside
				new NoContentProvider());
		} finally {
			this.startColumn = oldStartColumn;
		}
	}

	public Start parseFormula(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, FORMULA_PREFIX);
	}

	public Start parseExpression(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, EXPRESSION_PREFIX);
	}

	public Start parseSubstitution(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, SUBSTITUTION_PREFIX);
	}

	public Start parseTransition(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, OPERATION_PATTERN_PREFIX);
	}

	public Start parsePredicate(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, PREDICATE_PREFIX);
	}

	@Deprecated
	public Start eparse(String input, IDefinitions context) throws BCompoundException, LexerException, IOException {
		final Reader reader = new StringReader(input);

		Start ast = null;

		List<String> ids = new ArrayList<>();

		final DefinitionTypes defTypes = new DefinitionTypes();
		defTypes.addAll(context.getTypes());

		BLexer bLexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE), defTypes);
		bLexer.setParseOptions(parseOptions);
		Token t;
		do {
			t = bLexer.next();
			if (t instanceof TIdentifierLiteral) {
				if (!ids.contains(t.getText())) {
					ids.add(t.getText());
				}
			}
		} while (!(t instanceof EOF));

		Parser p = new Parser(new EBLexer(input, BigInteger.ZERO, ids, defTypes));
		boolean ok;
		try {
			ast = p.parse();
			ok = true;
		} catch (ParserException ignored) {
			ok = false;
		}

		BigInteger b = new BigInteger("2");
		b = b.pow(ids.size());
		b = b.subtract(BigInteger.ONE);

		while (!ok && b.compareTo(BigInteger.ZERO) > 0) {
			p = new Parser(new EBLexer(input, b, ids, defTypes));
			try {
				ast = p.parse();
				ok = true;
			} catch (ParserException ignored) {
				b = b.subtract(BigInteger.ONE);
			}
		}

		return ast;
	}

	/**
	 * Like {@link #parse(String, boolean, IFileContentProvider)}, but with
	 * {@link NoContentProvider} as last parameter, i.e., loading of referenced
	 * files is not enabled.
	 * 
	 * Use {@link #parse(String, boolean, IFileContentProvider)} instead to be
	 * able to control loading of referenced files.
	 * 
	 * @param input
	 *            the B machine as input string
	 * @param debugOutput
	 *            print debug information
	 * @return the AST node
	 * @throws BCompoundException
	 *             if the B machine cannot be parsed
	 */
	public Start parse(final String input, final boolean debugOutput) throws BCompoundException {
		return parse(input, debugOutput, true, new NoContentProvider());
	}
	
	public Start parse(final String input, final boolean debugOutput, final boolean preparseNecessary) throws BCompoundException {
		return parse(input, debugOutput, preparseNecessary, new NoContentProvider());
	}
	
	public Start parse(final String input, final boolean debugOutput, final IFileContentProvider contentProvider) throws BCompoundException {
		return parse(input, debugOutput, true, contentProvider);
	}

	/**
	 * Parses the input string.
	 * 
	 * @param input
	 *            The {@link String} to be parsed
	 * @param debugOutput
	 *            output debug messages on standard out?
	 * @param preparseNecessary
	 *            should pre-parsing be performed to detect DEFINITION types
	 * @param contentProvider
	 *            A {@link IFileContentProvider} that is able to load content of
	 *            referenced files during the parsing process. The content
	 *            provider is used for referenced definition files for example.
	 * @return the root node of the AST
	 * @throws BCompoundException if the B code could not be parsed
	 *     (see {@link BException} for details)
	 */
	public Start parse(
		final String input,
		final boolean debugOutput,
		final boolean preparseNecessary,
		final IFileContentProvider contentProvider
	) throws BCompoundException {
		final Reader reader = new StringReader(input);
		try {
			/*
			 * Pre-parsing: find and parse any referenced definition files (.def)
			 * and determine the types of all definitions.
			 * 
			 * The definition types are used in the lexer in order to replace an
			 * identifier token by a definition call token. This is required if
			 * the definition is a predicate because an identifier can not be
			 * parsed as a predicate. For example "... SELECT def THEN ... "
			 * would yield to a parse error. The lexer will replace the
			 * identifier token "def" by a TDefLiteralPredicate which will be
			 * excepted by the parser
			 */
			DefinitionTypes defTypes;
			if (preparseNecessary) {
				defTypes = preParsing(debugOutput, reader, contentProvider);
			} else {
				defTypes = new DefinitionTypes(definitions.getTypes());
			}

			/*
			 * Main parser
			 */
			final BLexer lexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE), defTypes);
			lexer.setPosition(this.startLine, this.startColumn);
			lexer.setParseOptions(parseOptions);
			Parser parser = new Parser(lexer);
			final Start rootNode = parser.parse();
			final List<BException> bExceptionList = new ArrayList<>();

			/*
			 * Collect available definition declarations. Needs to be done now
			 * cause they are needed by the following transformations.
			 */
			final DefinitionCollector collector = new DefinitionCollector(this.definitions);
			collector.collectDefinitions(rootNode);
			List<CheckException> definitionsCollectorExceptions = collector.getExceptions();
			for (CheckException checkException : definitionsCollectorExceptions) {
				bExceptionList.add(new BException(getFileName(), checkException));
			}

			// perfom AST transformations that can't be done by SableCC
			try {
				applyAstTransformations(rootNode);
			} catch (CheckException e) {
				throw new BCompoundException(new BException(getFileName(), e));
			}

			// perform some semantic checks which are not done in the parser
			List<CheckException> checkExceptions = performSemanticChecks(rootNode);
			for (CheckException checkException : checkExceptions) {
				bExceptionList.add(new BException(getFileName(), checkException));
			}
			if (!bExceptionList.isEmpty()) {
				throw new BCompoundException(bExceptionList);

			}
			return rootNode;

		} catch (final BLexerException e) {
			throw new BCompoundException(new BException(getFileName(), e));
		} catch (final BParseException e) {
			throw new BCompoundException(new BException(getFileName(), e));
		} catch (final IOException e) {
			throw new BCompoundException(new BException(getFileName(), e));
		} catch (final PreParseException e) {
			throw new BCompoundException(new BException(getFileName(), e));
		} catch (final ParserException e) {
			final Token token = e.getToken();
			final String msg = e.getLocalizedMessage();
			final String realMsg = e.getRealMsg();
			throw new BCompoundException(new BException(getFileName(), new BParseException(token, msg, realMsg, e)));
		} catch (LexerException e) {
			throw new BCompoundException(new BException(getFileName(), e));
		}
	}

	public String getFileName() {
		if (fileName == null) {
			return null;
		}
		File f = new File(fileName);
		if (f.exists()) {
			try {
				return f.getCanonicalPath();
			} catch (IOException e) {
				return fileName;
			}
		} else {
			return fileName;
		}
	}

	private DefinitionTypes preParsing(
		final boolean debugOutput,
		final Reader reader,
		final IFileContentProvider contentProvider
	) throws IOException, PreParseException, BCompoundException {
		final PreParser preParser = new PreParser(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE),
				contentProvider, doneDefFiles, this.fileName, directory, parseOptions, this.definitions);
		// scan for additional new definitions
		preParser.setDebugOutput(debugOutput);
		preParser.setStartPosition(this.startLine, this.startColumn);
		preParser.parse();
		reader.reset();
		return preParser.getDefinitionTypes();
	}

	private void applyAstTransformations(final Start rootNode) throws CheckException {
		// default transformations
		OpSubstitutions.transform(rootNode, getDefinitions());
		try {
			rootNode.apply(new SyntaxExtensionTranslator());
		} catch (VisitorException e) {
			throw e.getException();
		}
		// more AST transformations?

	}

	private List<CheckException> performSemanticChecks(final Start rootNode) {
		final List<CheckException> list = new ArrayList<>();
		@SuppressWarnings("deprecation")
		SemanticCheck primedIdentifierCheck = new de.be4.classicalb.core.parser.analysis.checking.PrimedIdentifierCheck();
		@SuppressWarnings("deprecation")
		SemanticCheck proverExpressionsCheck = new de.be4.classicalb.core.parser.analysis.checking.ProverExpressionsCheck();
		final SemanticCheck[] checks = { new ClausesCheck(), new SemicolonCheck(), new IdentListCheck(),
				new DefinitionUsageCheck(getDefinitions()), primedIdentifierCheck, proverExpressionsCheck, new RefinedOperationCheck() };
		// apply more checks?

		for (SemanticCheck check : checks) {
			check.setOptions(parseOptions);
			check.runChecks(rootNode);
			list.addAll(check.getCheckExceptions());
		}

		return list;
	}

	public IDefinitions getDefinitions() {
		return definitions;
	}

	public void setDefinitions(IDefinitions definitions) {
		this.definitions = definitions;
	}

	public List<String> getDoneDefFiles() {
		return doneDefFiles;
	}

	public void setDoneDefFiles(final List<String> doneDefFiles) {
		this.doneDefFiles = doneDefFiles;
	}

	public ParseOptions getOptions() {
		return parseOptions;
	}

	public void setParseOptions(ParseOptions options) {
		this.parseOptions = options;
	}

	public void setDirectory(final File directory) {
		this.directory = directory;
	}
}
