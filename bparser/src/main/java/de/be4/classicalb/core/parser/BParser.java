package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.analysis.checking.ClausesCheck;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionCollector;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionUsageCheck;
import de.be4.classicalb.core.parser.analysis.checking.IdentListCheck;
import de.be4.classicalb.core.parser.analysis.checking.RefinedOperationCheck;
import de.be4.classicalb.core.parser.analysis.checking.SemanticCheck;
import de.be4.classicalb.core.parser.analysis.checking.SemicolonCheck;
import de.be4.classicalb.core.parser.analysis.transforming.CoupleToIdentifierTransformation;
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
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.Token;
import de.be4.classicalb.core.parser.parser.Parser;
import de.be4.classicalb.core.parser.parser.ParserException;
import de.be4.classicalb.core.parser.util.Utils;

public class BParser {

	public static final String EXPRESSION_PREFIX = "#EXPRESSION";
	public static final String PREDICATE_PREFIX = "#PREDICATE";
	public static final String FORMULA_PREFIX = "#FORMULA";
	public static final String SUBSTITUTION_PREFIX = "#SUBSTITUTION";
	public static final String OPERATION_PATTERN_PREFIX = "#OPPATTERN";
	public static final String MACHINE_CLAUSE_PREFIX = "#MACHINECLAUSE";

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

	private final List<String> definitionFileIncludeStack = new ArrayList<>();

	private final String fileName;

	private int startLine;
	private int startColumn;

	private IFileContentProvider contentProvider;

	public static String getVersion() {
		return buildProperties.getProperty("version");
	}

	public static String getGitSha() {
		return buildProperties.getProperty("git");
	}

	public BParser() {
		this(null);
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

	/**
	 * Get the currently configured {@link IFileContentProvider} that will/has been used to get the content of referenced definition files.
	 * If you don't use {@link #setContentProvider(IFileContentProvider)} to set one yourself,
	 * then calling {@link #parseFile(File)} or {@link #parseMachine(String)} will set up a default {@link CachingDefinitionFileProvider}.
	 * 
	 * @return the currently configured file content provider,
	 *     or {@code null} if none has been set yet (automatically or manually)
	 */
	public IFileContentProvider getContentProvider() {
		return this.contentProvider;
	}

	/**
	 * Set a custom {@link IFileContentProvider} to be used to get the content of referenced definition files.
	 * If you don't call this method,
	 * then calling {@link #parseFile(File)} or {@link #parseMachine(String)} will set up a default {@link CachingDefinitionFileProvider}.
	 * A useful alternative is {@link NoContentProvider},
	 * which disables reading of external definition files.
	 * 
	 * @param contentProvider used to get the content of referenced files
	 */
	public void setContentProvider(IFileContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}

	/**
	 * Parses the input file.
	 * 
	 * @param machineFile the machine file to be parsed
	 * @return the parsed AST
	 * @throws BCompoundException if the file couldn't be read or parsed
	 * @see #parseMachine(String)
	 */
	public Start parseFile(File machineFile) throws BCompoundException {
		String content;
		try {
			content = Utils.readFile(machineFile);
		} catch (IOException e) {
			throw new BCompoundException(new BException(machineFile.getPath(), e));
		}
		return parseMachine(content, machineFile);
	}

	// Don't delete this deprecated method too soon!
	// It was one of the main parser APIs for a long time.
	/**
	 * Parses the input file.
	 * 
	 * @deprecated Use {@link #parseFile(File)} instead.
	 * @see #parseMachine(String)
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
	@Deprecated
	public Start parseFile(final File machineFile, final boolean verbose) throws IOException, BCompoundException {
		// Don't delete this deprecated method too soon!
		// It was one of the main parser APIs for a long time.
		contentProvider = new CachingDefinitionFileProvider();
		return parseFile(machineFile, verbose, contentProvider);
	}

	// Don't delete this deprecated method too soon!
	// It was one of the main parser APIs for a long time.
	/**
	 * Parses the input file.
	 * 
	 * @deprecated Use {@link #parseFile(File)} and {@link #setContentProvider(IFileContentProvider)} instead.
	 * @see #parseMachine(String)
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
	@Deprecated
	public Start parseFile(final File machineFile, final boolean verbose, final IFileContentProvider contentProvider)
			throws IOException, BCompoundException {
		// Don't delete this deprecated method too soon!
		// It was one of the main parser APIs for a long time.
		if (verbose) {
			System.out.println("*** Debug: Parsing file '" + machineFile.getCanonicalPath() + "'");
		}
		String content = Utils.readFile(machineFile);
		return parseWithPreParsing(new StringReader(content), machineFile, contentProvider);
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
		return parser.parseMachine(input);
	}

	private Start parseWithKindPrefix(String input, String prefix, boolean withPreParsing) throws BCompoundException {
		final String theFormula = prefix + " " + input;
		final int oldStartColumn = this.startColumn;
		try {
			// Decrease the start column by the size of the implicitly added prefix
			// so that the actual user input starts at the desired position.
			this.startColumn -= prefix.length() + 1;
			if (withPreParsing) {
				return this.parseMachine(theFormula);
			} else {
				return this.parseWithoutPreParsing(new StringReader(theFormula));
			}
		} finally {
			this.startColumn = oldStartColumn;
		}
	}

	public Start parseFormula(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, FORMULA_PREFIX, false);
	}

	public Start parseExpression(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, EXPRESSION_PREFIX, false);
	}

	public Start parseSubstitution(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, SUBSTITUTION_PREFIX, false);
	}

	public Start parseTransition(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, OPERATION_PATTERN_PREFIX, false);
	}

	public Start parsePredicate(final String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, PREDICATE_PREFIX, false);
	}

	public Start parseMachineClause(String input) throws BCompoundException {
		return this.parseWithKindPrefix(input, MACHINE_CLAUSE_PREFIX, true);
	}

	// Don't delete this deprecated method too soon!
	// It was one of the main parser APIs for a long time.
	/**
	 * Like {@link #parse(String, boolean, IFileContentProvider)}, but with
	 * {@link NoContentProvider} as last parameter, i.e., loading of referenced
	 * files is not enabled.
	 * 
	 * @deprecated Use {@link #parseMachine(String)} instead.
	 *     Note that this also enables loading of referenced files -
	 *     use {@link #setContentProvider(IFileContentProvider)} to control this.
	 *     The {@code debugOutput} parameter does nothing.
	 * @param input
	 *            the B machine as input string
	 * @param debugOutput ignored
	 * @return the AST node
	 * @throws BCompoundException
	 *             if the B machine cannot be parsed
	 */
	@Deprecated
	public Start parse(final String input, final boolean debugOutput) throws BCompoundException {
		// Don't delete this deprecated method too soon!
		// It was one of the main parser APIs for a long time.
		return parse(input, debugOutput, new NoContentProvider());
	}
	
	// Don't delete this deprecated method too soon!
	// It was one of the main parser APIs for a long time.
	/**
	 * Parses a complete B machine from a string.
	 * 
	 * @deprecated Use {@link #parseMachine(String)} and {@link #setContentProvider(IFileContentProvider)} instead.
	 *     The {@code debugOutput} parameter does nothing.
	 * @param input B machine source code
	 * @param debugOutput ignored
	 * @param contentProvider A {@link IFileContentProvider} that is able to load content of referenced files during the parsing process.
	 *     The content provider is used for referenced definition files for example.
	 * @return the root node of the AST
	 * @throws BCompoundException if the B code could not be parsed
	 *     (see {@link BException} for details)
	 */
	@Deprecated
	public Start parse(final String input, final boolean debugOutput, final IFileContentProvider contentProvider) throws BCompoundException {
		// Don't delete this deprecated method too soon!
		// It was one of the main parser APIs for a long time.
		return parseWithPreParsing(new StringReader(input), this.getMachineFile(), contentProvider);
	}

	private Start parseInternal(Reader reader, File machineFile, DefinitionTypes defTypes) throws BCompoundException {
		String machineFilePath = machineFile == null ? null : machineFile.getPath();
		Start rootNode;
		try {
			/*
			 * Main parser
			 */
			final BLexer lexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE), defTypes);
			lexer.setPosition(this.startLine, this.startColumn);
			lexer.setParseOptions(parseOptions);
			Parser parser = new Parser(lexer);
			rootNode = parser.parse();
		} catch (final BLexerException e) {
			throw new BCompoundException(new BException(machineFilePath, e));
		} catch (final IOException e) {
			throw new BCompoundException(new BException(machineFilePath, e));
		} catch (final ParserException e) {
			final Token token = e.getToken();
			final String msg = e.getMessage();
			final String realMsg = e.getRealMsg();
			throw new BCompoundException(new BException(machineFilePath, new BParseException(token, msg, realMsg, e)));
		} catch (LexerException e) {
			throw new BCompoundException(new BException(machineFilePath, e));
		}

		if (parseOptions.isApplyASTTransformations()) {
			List<CheckException> checkExceptions = applyAstTransformations(rootNode);
			if (!checkExceptions.isEmpty()) {
				throw new BCompoundException(checkExceptions.stream()
					.map(checkException -> new BException(machineFilePath, checkException))
					.collect(Collectors.toList()));
			}
		}

		return rootNode;
	}

	private Start parseWithPreParsing(Reader reader, File machineFile, IFileContentProvider provider) throws BCompoundException {
		String machineFilePath = machineFile == null ? null : machineFile.getPath();
		DefinitionTypes defTypes;
		try {
			defTypes = preParsing(reader, machineFile, provider);
		} catch (IOException e) {
			throw new BCompoundException(new BException(machineFilePath, e));
		} catch (PreParseException e) {
			throw new BCompoundException(new BException(machineFilePath, e));
		}
		return parseInternal(reader, machineFile, defTypes);
	}

	private Start parseWithoutPreParsing(Reader reader) throws BCompoundException {
		return parseInternal(reader, null, new DefinitionTypes(definitions.getTypes()));
	}

	/**
	 * Parses a complete B machine from a string.
	 * {@code machineFile} indicates what file (if any) the source code string belongs to.
	 * This is used to resolve definition files and displayed in error messages.
	 * 
	 * @param input B machine source code
	 * @param machineFile file that the source code belongs to
	 * @return the root node of the AST
	 * @throws BCompoundException if the B code could not be parsed
	 *     (see {@link BException} for details)
	 */
	Start parseMachine(String input, File machineFile) throws BCompoundException {
		if (this.contentProvider == null) {
			this.contentProvider = new CachingDefinitionFileProvider();
		}
		return parseWithPreParsing(new StringReader(input), machineFile, this.contentProvider);
	}

	/**
	 * Parses a complete B machine from a string.
	 * 
	 * @param input B machine source code
	 * @return the root node of the AST
	 * @throws BCompoundException if the B code could not be parsed
	 *     (see {@link BException} for details)
	 */
	public Start parseMachine(String input) throws BCompoundException {
		return this.parseMachine(input, this.getMachineFile());
	}

	private File getMachineFile() {
		if (this.fileName == null) {
			return null;
		} else {
			return new File(this.fileName);
		}
	}

	public String getFileName() {
		if (fileName == null) {
			return null;
		}
		File f = new File(fileName);
		if (f.exists()) {
			// Make the path canonical if possible.
			// This is important on Windows,
			// because ProB/SICStus sometimes converts paths to all lowercase,
			// but the "machine name must match file name" check expects the file name to be capitalized like the machine name.
			// getCanonicalPath restores the capitalization as found on the file system.
			try {
				return f.getCanonicalPath();
			} catch (IOException e) {
				return fileName;
			}
		} else {
			return fileName;
		}
	}

	/**
	 * <p>
	 * Pre-parsing: find and parse any referenced definition files (.def)
	 * and determine the types of all definitions.
	 * This step is only necessary when parsing a full machine,
	 * not just a formula, substitution, etc.
	 * </p>
	 * <p>
	 * The definition types are used in the lexer in order to replace an
	 * identifier token by a definition call token. This is required if
	 * the definition is a predicate because an identifier can not be
	 * parsed as a predicate. For example "... SELECT def THEN ... "
	 * would yield to a parse error. The lexer will replace the
	 * identifier token "def" by a TDefLiteralPredicate which will be
	 * excepted by the parser
	 * </p>
	 */
	private DefinitionTypes preParsing(
		final Reader reader,
		final File machineFile,
		final IFileContentProvider contentProvider
	) throws IOException, PreParseException, BCompoundException {
		final PreParser preParser = new PreParser(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE),
			machineFile,
			contentProvider, definitionFileIncludeStack, parseOptions, this.definitions
		);
		// scan for additional new definitions
		preParser.setStartPosition(this.startLine, this.startColumn);
		preParser.parse();
		reader.reset();
		return preParser.getDefinitionTypes();
	}

	private List<CheckException> applyAstTransformations(Start rootNode) {
		List<CheckException> checkExceptions = new ArrayList<>();

		// perform AST transformations that don't require definitions
		// important for unquoting of definition identifiers before collecting them

		// default transformations
		rootNode.apply(new CoupleToIdentifierTransformation());

		try {
			rootNode.apply(new SyntaxExtensionTranslator());
		} catch (VisitorException e) {
			checkExceptions.add(e.getException());
		}

		// Collect available definition declarations. Needs to be done now
		// because they are needed by the following transformations.
		DefinitionCollector collector = new DefinitionCollector(this.definitions);
		collector.collectDefinitions(rootNode);
		checkExceptions.addAll(collector.getExceptions());

		// perform AST transformations that can't be done by SableCC
		try {
			OpSubstitutions.transform(rootNode, getDefinitions());
		} catch (CheckException e) {
			checkExceptions.add(e);
		}
		// more AST transformations?

		// perform some semantic checks which are not done in the parser
		final SemanticCheck[] checks = { new ClausesCheck(), new SemicolonCheck(), new IdentListCheck(),
				new DefinitionUsageCheck(getDefinitions()), new RefinedOperationCheck() };
		// apply more checks?

		for (SemanticCheck check : checks) {
			check.runChecks(rootNode);
			checkExceptions.addAll(check.getCheckExceptions());
		}

		return checkExceptions;
	}

	public IDefinitions getDefinitions() {
		return definitions;
	}

	public void setDefinitions(IDefinitions definitions) {
		this.definitions = definitions;
	}

	/**
	 * For internal use only by the {@link PreParser}.
	 * 
	 * @return the chain/stack of definition files via which the currently parsed definition file was reached, or an empty list when not parsing a definition file
	 */
	List<String> getDefinitionFileIncludeStack() {
		return definitionFileIncludeStack;
	}

	public ParseOptions getOptions() {
		return parseOptions;
	}

	public void setParseOptions(ParseOptions options) {
		this.parseOptions = options;
	}
}
