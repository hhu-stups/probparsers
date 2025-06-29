package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.checking.DefinitionCollector;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionPreCollector;
import de.be4.classicalb.core.parser.analysis.transforming.OpSubstitutions;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.node.ADefinitionExpression;
import de.be4.classicalb.core.parser.node.ADefinitionPredicate;
import de.be4.classicalb.core.parser.node.ADefinitionSubstitution;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PParseUnit;
import de.be4.classicalb.core.parser.node.TDefLiteralPredicate;
import de.be4.classicalb.core.parser.node.TDefLiteralSubstitution;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.Token;
import de.be4.classicalb.core.parser.util.Utils;
import de.be4.classicalb.core.preparser.lexer.LexerException;
import de.be4.classicalb.core.preparser.node.PPreParseUnit;
import de.be4.classicalb.core.preparser.node.TPreParserDefinitions;
import de.be4.classicalb.core.preparser.node.TPreParserIdentifier;
import de.be4.classicalb.core.preparser.node.TPreParserString;
import de.be4.classicalb.core.preparser.node.TRhsBody;
import de.be4.classicalb.core.preparser.parser.Parser;
import de.be4.classicalb.core.preparser.parser.ParserException;

/**
 * <p>
 * Pre-parsing: find and parse any referenced definition files (.def)
 * and determine the types of all definitions.
 * This is necessary because the parser handles expressions, predicates, and substitutions separately,
 * so different token/node types are needed for definition identifiers depending on whether they are
 * expressions ({@link TIdentifierLiteral}/{@link AIdentifierExpression}),
 * predicates ({@link TDefLiteralPredicate}/{@link ADefinitionPredicate}),
 * or substitutions ({@link TDefLiteralSubstitution}/{@link ADefinitionSubstitution}).
 * The PreParser collects all needed type information into {@link DefinitionTypes},
 * which is used by {@link BLexer} to convert all identifiers to the appropriate token/node types.
 * </p>
 * <p>
 * This is an annoying mess and nobody wants it,
 * but it's more or less necessary with the current parser architecture.
 * We have already tried to avoid/remove this step,
 * but haven't succeeded so far.
 * If you try to remove the PreParser,
 * please update the following counters afterwards:
 * </p>
 * <p>
 * 1 person has tried 4 times to remove the PreParser.
 * </p>
 * 
 * @see BLexer#replaceDefTokens()
 * @see BParser#preParsing(Reader, File, IFileContentProvider)
 * @see DefinitionCollector
 * @see DefinitionPreCollector
 */
public class PreParser {

	private final PushbackReader pushbackReader;
	private final File machineFile;
	private final DefinitionTypes definitionTypes;
	private final IDefinitions defFileDefinitions;
	private final ParseOptions parseOptions;
	private final IFileContentProvider contentProvider;
	private final List<String> definitionFileIncludeStack;

	private int startLine;
	private int startColumn;

	public PreParser(PushbackReader pushbackReader, File machineFile,
			IFileContentProvider contentProvider,
			List<String> definitionFileIncludeStack,
			ParseOptions parseOptions, IDefinitions definitions) {
		this.pushbackReader = pushbackReader;
		this.machineFile = machineFile;
		this.contentProvider = contentProvider;
		this.definitionFileIncludeStack = definitionFileIncludeStack;
		this.parseOptions = parseOptions;
		this.defFileDefinitions = definitions;
		this.definitionTypes = new DefinitionTypes();
		definitionTypes.addAll(definitions.getTypes());

		this.startLine = 1;
		this.startColumn = 1;
	}

	public void setStartPosition(final int line, final int column) {
		this.startLine = line;
		this.startColumn = column;
	}

	public void parse() throws PreParseException, IOException, BCompoundException {
		final PreLexer preLexer = new PreLexer(pushbackReader);
		preLexer.setPosition(this.startLine, this.startColumn);

		final Parser preParser = new Parser(preLexer);
		PPreParseUnit preParseUnit;
		try {
			preParseUnit = preParser.parse().getPPreParseUnit();
		} catch (final ParserException e) {
			String message;
			if (e.getToken() instanceof TPreParserDefinitions) {
				message = "Clause 'DEFINITIONS' is used more than once";
			} else {
				message = e.getRealMsg();
			}
			throw new PreParseException(e.getToken(), message, e);
		} catch (final LexerException e) {
			throw new PreParseException(e.getLine(), e.getPos(), e.getRealMsg(), e);
		}

		final DefinitionPreCollector collector = new DefinitionPreCollector();
		preParseUnit.apply(collector);

		Map<TPreParserIdentifier, TRhsBody> definitions = new HashMap<>(collector.getDefinitions());
		for (TPreParserIdentifier nameToken : definitions.keySet()) {
			String name = nameToken.getText();
			if (Utils.isQuoted(name, '`')) {
				try {
					nameToken.setText(Utils.unquoteIdentifier(name));
				} catch (IllegalArgumentException exc) {
					throw new PreParseException(nameToken, exc.getMessage(), exc);
				}
			}
		}

		evaluateDefinitionFiles(collector.getFileDefinitions());

		List<TPreParserIdentifier> sortedDefinitionList = sortDefinitionsByTopologicalOrderAndCheckForCycles(definitions);

		evaluateTypes(sortedDefinitionList, definitions);

	}

	private void evaluateDefinitionFiles(List<TPreParserString> list)
			throws PreParseException, BCompoundException {

		IDefinitionFileProvider cache = null;
		if (contentProvider instanceof IDefinitionFileProvider) {
			cache = (IDefinitionFileProvider) contentProvider;
		}

		for (TPreParserString fileNameString : list) {
			// Unquote and unescape the definition file name string.
			String quotedFilename = fileNameString.getText();
			String fileName = Utils.unescapeStringContents(Utils.removeSurroundingQuotes(quotedFilename, '"'));
			// Note, that the fileName could be a relative path, e.g.
			// ./foo/bar/defs.def or an absolute path
			try {
				if (definitionFileIncludeStack.contains(fileName)) {
					StringBuilder sb = new StringBuilder();
					for (String string : definitionFileIncludeStack) {
						sb.append(string).append(" -> ");
					}
					sb.append(fileName);
					throw new PreParseException(fileNameString,
							"Cyclic references in definition files: " + sb);
				}

				IDefinitions definitions;
				if (cache != null && cache.getDefinitions(fileName) != null) {
					definitions = cache.getDefinitions(fileName);
				} else {
					File directory = machineFile == null ? null : machineFile.getParentFile();
					final String content = contentProvider.getFileContent(directory, fileName);
					final File file = contentProvider.getFile(directory, fileName);
					final BParser parser = new BParser(fileName, parseOptions);
					parser.setContentProvider(contentProvider);
					parser.getDefinitionFileIncludeStack().addAll(definitionFileIncludeStack);
					parser.getDefinitionFileIncludeStack().add(fileName);
					parser.setDefinitions(new Definitions(file));
					parser.parseMachine(content, file);
					definitions = parser.getDefinitions();
					if (cache != null) {
						cache.storeDefinition(fileName, definitions);
					}
				}
				defFileDefinitions.addDefinitions(definitions);
				definitionTypes.addAll(definitions.getTypes());
			} catch (final IOException e) {
				throw new PreParseException(fileNameString, "Definition file cannot be read: " + e, e);
			} catch (BCompoundException e) {
				throw e.withMissingLocations(BException.Location.locationsFromNodes(fileName, Collections.singletonList(fileNameString)));
			}
		}
	}

	private void evaluateTypes(List<TPreParserIdentifier> sortedDefinitionList, final Map<TPreParserIdentifier, TRhsBody> definitions)
			throws PreParseException {
		// use linked list as we rely on pop() and push()
		LinkedList<TPreParserIdentifier> remainingDefinitions = new LinkedList<>(sortedDefinitionList);
		LinkedList<TPreParserIdentifier> currentlyUnparseableDefinitions = new LinkedList<>();
		Set<String> todoDefs = new HashSet<>();
		for (TPreParserIdentifier token : remainingDefinitions) {
			todoDefs.add(token.getText());
		}
		// use main parser for the rhs of each definition to determine type
		// if a definition can not be typed this way, it may be due to another
		// definition that is not yet parser (because it appears later in the
		// source code)
		// in this case, the definition is appended to the list again
		// the algorithm terminates if the queue is empty or if no definition
		// has been parsed
		boolean oneParsed = true;
		while (oneParsed) {
			oneParsed = false;

			while (!remainingDefinitions.isEmpty()) {
				TPreParserIdentifier definition = remainingDefinitions.pop();

				TRhsBody defRhs = definitions.get(definition);
				Definitions.Type type;
				DefinitionType definitionType = determineType(definition, defRhs, todoDefs);
				type = definitionType.type;
				if (type != null) {
					todoDefs.remove(definition.getText());
					oneParsed = true;
					definitionTypes.addTyping(definition.getText(), type);
					// types.addTyping(definition.getText(), type);
				} else {
					currentlyUnparseableDefinitions.push(definition);
				}
			}

			remainingDefinitions.addAll(currentlyUnparseableDefinitions);
			currentlyUnparseableDefinitions.clear();
		}

		if (!remainingDefinitions.isEmpty()) {
			TPreParserIdentifier definition = remainingDefinitions.pop();
			TRhsBody defRhs = definitions.get(definition);
			DefinitionType definitionType = determineType(definition, defRhs, todoDefs);
			if (definitionType.errorMessage != null) {
				String message = definitionType.errorMessage;
				if (machineFile != null) {
					message += " in file: " + machineFile;
				}
				throw new PreParseException(definitionType.errorToken.getLine(), definitionType.errorToken.getPos(), message);
			} else {
				// fall back message
				throw new PreParseException(definition, "expecting wellformed expression, predicate or substitution as DEFINITION body (DEFINITION arguments assumed to be expressions)");
			}
		}
	}

	private List<TPreParserIdentifier> sortDefinitionsByTopologicalOrderAndCheckForCycles(Map<TPreParserIdentifier, TRhsBody> definitions)
			throws PreParseException {
		Set<String> definitionNames = new HashSet<>();
		Map<String, TPreParserIdentifier> definitionMap = new HashMap<>();
		for (TPreParserIdentifier token : definitions.keySet()) {
			final String definitionName = token.getText();
			definitionNames.add(definitionName);
			definitionMap.put(definitionName, token);
		}
		Map<String, Set<String>> dependencies = determineDependencies(definitionNames, definitions);
		List<String> sortedDefinitionNames = Utils.sortByTopologicalOrder(dependencies);
		if (sortedDefinitionNames.size() < definitionNames.size()) {
			Set<String> remaining = new HashSet<>(definitionNames);
			remaining.removeAll(sortedDefinitionNames);
			List<String> cycle = Utils.determineCycle(remaining, dependencies);
			StringBuilder sb = new StringBuilder();
			for (Iterator<String> iterator = cycle.iterator(); iterator.hasNext();) {
				sb.append(iterator.next());
				if (iterator.hasNext()) {
					sb.append(" -> ");
				}
			}
			TPreParserIdentifier firstDefinitionToken = definitionMap.get(cycle.get(0));
			throw new PreParseException(firstDefinitionToken, "Cyclic references in definitions: " + sb);
		} else {
			List<TPreParserIdentifier> sortedDefinitionTokens = new ArrayList<>();
			for (String name : sortedDefinitionNames) {
				sortedDefinitionTokens.add(definitionMap.get(name));
			}
			return sortedDefinitionTokens;
		}

	}

	private Map<String, Set<String>> determineDependencies(Set<String> definitionNames, Map<TPreParserIdentifier, TRhsBody> definitions)
			throws PreParseException {
		HashMap<String, Set<String>> dependencies = new HashMap<>();
		for (Map.Entry<TPreParserIdentifier, TRhsBody> entry : definitions.entrySet()) {
			TPreParserIdentifier nameToken = entry.getKey();
			TRhsBody rhsToken = entry.getValue();
			// The FORMULA_PREFIX is needed to switch the lexer state from
			// section to normal. Note, that we do not parse the right hand side
			// of the definition here. Hence FORMULA_PREFIX has no further
			// meaning and substitutions can also be handled by the lexer.
			final Reader reader = new StringReader(BParser.FORMULA_PREFIX + "\n" + rhsToken.getText());

			final BLexer lexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE),
					new DefinitionTypes());
			lexer.setParseOptions(parseOptions);
			Set<String> set = new HashSet<>();
			Token next;
			try {
				next = lexer.next();
				while (!(next instanceof EOF)) {
					if (next instanceof TIdentifierLiteral) {
						TIdentifierLiteral id = (TIdentifierLiteral) next;
						String name;
						try {
							name = Utils.unquoteIdentifier(id.getText());
						} catch (IllegalArgumentException exc) {
							throw new PreParseException(rhsToken, exc.getMessage(), exc);
						}
						if (definitionNames.contains(name)) {
							set.add(name);
						}
					}
					next = lexer.next();
				}
			} catch (IOException e) {
				throw new PreParseException("Error while parsing", e);
			} catch (BLexerException e) {
				Token errorToken = e.getLastToken();
				correctErrorTokenPosition(nameToken, rhsToken, errorToken);
				throw new PreParseException(errorToken.getLine(), errorToken.getPos(), adjustErrorMessage(e.getRealMsg()), e);
			} catch (de.be4.classicalb.core.parser.lexer.LexerException e) {
				throw wrapLexerExceptionAndCorrectPosition(nameToken, rhsToken, e, e);
			}
			dependencies.put(nameToken.getText(), set);
		}
		return dependencies;
	}

	static class DefinitionType {
		Definitions.Type type;
		String errorMessage;
		Token errorToken;

		DefinitionType() {

		}

		DefinitionType(Definitions.Type t, Token n) {
			this.type = t;
			this.errorToken = n;
		}

		DefinitionType(Definitions.Type t) {
			this.type = t;
		}

		DefinitionType(String errorMessage, Token t) {
			this.errorMessage = errorMessage;
			this.errorToken = t;
		}
	}

	/**
	 * In some cases, we cannot decide during preparsing and parsing
	 * whether the RHS of a definition is an expression or a substitution.
	 * Function and operation calls are syntactically the same in most cases,
	 * so a definition containing only an operation call will usually be detected as an expression.
	 * Such definitions must have their type corrected later - see {@link OpSubstitutions}.
	 * 
	 * @param rhs the right-hand side of the expression definition to check
	 * @return the type of the definition
	 */
	public static IDefinitions.Type getExpressionDefinitionRhsType(PExpression rhs) {
		if (
			rhs instanceof AIdentifierExpression
			|| rhs instanceof AFunctionExpression
			|| rhs instanceof ADefinitionExpression
		) {
			return IDefinitions.Type.ExprOrSubst;
		} else {
			return IDefinitions.Type.Expression;
		}
	}

	/**
	 * Try to determine the abstract type of the right-hand side of a definition,
	 * i. e. whether it's an expression, a predicate, or a substitution.
	 * If the right-hand side references other definitions,
	 * it may not be possible to determine this definition's type yet
	 * if the types of the other definitions aren't known yet.
	 * For such cases,
	 * {@link #evaluateTypes(List, Map)} calls this method repeatedly until the type can be successfully determined.
	 * 
	 * @param definition the definition name token
	 * @param rhsToken the right-hand side of the definition (as a single token, merged by the {@link PreLexer})
	 * @param untypedDefinitions names of all definitions whose types haven't been determined yet
	 * @return the type of the definition's right-hand side, or error information if the type cannot be determined yet
	 *     (but it's expected that the type can be determined later, once some other definitions' types are known)
	 * @throws PreParseException if the definition's right-hand side couldn't be parsed
	 *     (and the parse error is not expected to go away later, even after more definitions' types are known) 
	 */
	private DefinitionType determineType(TPreParserIdentifier definition, TRhsBody rhsToken,
			final Set<String> untypedDefinitions) throws PreParseException {

		final String definitionRhs = rhsToken.getText();

		Token errorToken;
		try {
			// Try parsing the RHS as a Formula, i.e., either expression or predicate
			PParseUnit parseunit = tryParsing(BParser.FORMULA_PREFIX, definitionRhs);

			// check if the result is a Predicate?
			if (parseunit instanceof APredicateParseUnit) {
				return new DefinitionType(IDefinitions.Type.Predicate);
			}

			// check if we have definitely an Expression or an ambiguous Expression/Substitution (e.g. f(x))?

			AExpressionParseUnit expressionParseUnit = (AExpressionParseUnit) parseunit;

			PreParserIdentifierTypeVisitor visitor = new PreParserIdentifierTypeVisitor(untypedDefinitions);
			expressionParseUnit.apply(visitor);

			if (visitor.isUntypedDefinitionUsed()) {
				// the parseunit uses another definition which is not yet typed
				return new DefinitionType();
			}

			return new DefinitionType(getExpressionDefinitionRhsType(expressionParseUnit.getExpression()));
		} catch (de.be4.classicalb.core.parser.parser.ParserException e) {
			errorToken = e.getToken();
			try {
				// try parsing the RHS now as a substitution:
				tryParsing(BParser.SUBSTITUTION_PREFIX, definitionRhs);
				return new DefinitionType(IDefinitions.Type.Substitution, errorToken);
			} catch (de.be4.classicalb.core.parser.parser.ParserException ex) {
				Token errorToken2 = ex.getToken();
				if (errorToken.getLine() > errorToken2.getLine() || (errorToken.getLine() == errorToken2.getLine()
						&& errorToken.getPos() >= errorToken2.getPos())) {
					// use error message from Substitution
					correctErrorTokenPosition(definition, rhsToken, errorToken);
					return new DefinitionType(adjustErrorMessage(e.getRealMsg()), errorToken);
				} else {
					// use error message from Expression/Predicate parsing:
					correctErrorTokenPosition(definition, rhsToken, errorToken2);
					return new DefinitionType(adjustErrorMessage(ex.getRealMsg()), errorToken2);
				}
			} catch (BLexerException e1) {
				errorToken = e1.getLastToken();
				correctErrorTokenPosition(definition, rhsToken, errorToken);
				throw new PreParseException(errorToken.getLine(), errorToken.getPos(), adjustErrorMessage(e.getRealMsg()), e);
			} catch (de.be4.classicalb.core.parser.lexer.LexerException e3) {
				// FIXME Is the cause really supposed to be different here?
				throw wrapLexerExceptionAndCorrectPosition(definition, rhsToken, e3, e);
			} catch (IOException e1) {
				throw new PreParseException(e.toString(), e);
			}
		} catch (BLexerException e) {
			errorToken = e.getLastToken();
			correctErrorTokenPosition(definition, rhsToken, errorToken);
			throw new PreParseException(errorToken.getLine(), errorToken.getPos(), adjustErrorMessage(e.getRealMsg()), e);
		} catch (de.be4.classicalb.core.parser.lexer.LexerException e) {
			throw wrapLexerExceptionAndCorrectPosition(definition, rhsToken, e, e);
		} catch (IOException e) {
			throw new PreParseException(e.toString(), e);
		}

	}

	private static void correctErrorTokenPosition(
		TPreParserIdentifier definition,
		TRhsBody rhsToken,
		Token errorToken
	) {
		// the parsed string starts in the second line, e.g. #formula\n ...
		int line = errorToken.getLine();
		int pos = errorToken.getPos();
		pos = line == 2 ? rhsToken.getPos() + pos - 1 : pos;
		line = definition.getLine() + line - 2;
		errorToken.setLine(line);
		errorToken.setPos(pos);
	}

	private static String adjustErrorMessage(String message) {
		if (message.contains("expecting: EOF")) {
			return "expecting end of definition";
		} else {
			return message.replace("the end of file", "the end of definition");
		}
	}

	private static PreParseException wrapLexerExceptionAndCorrectPosition(
		TPreParserIdentifier definition,
		TRhsBody rhsToken,
		de.be4.classicalb.core.parser.lexer.LexerException exc,
		Throwable cause
	) {
		// the parsed string starts in the second line, e.g. #formula\n ...
		int line = exc.getLine();
		int pos = exc.getPos();
		pos = line == 2 ? rhsToken.getPos() + pos - 1 : pos;
		line = definition.getLine() + line - 2;
		return new PreParseException(line, pos, exc.getRealMsg(), cause);
	}

	private PParseUnit tryParsing(final String prefix, final String definitionRhs)
			throws de.be4.classicalb.core.parser.lexer.LexerException,
			de.be4.classicalb.core.parser.parser.ParserException, IOException {

		final Reader reader = new StringReader(prefix + "\n" + definitionRhs);
		final BLexer lexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE), this.definitionTypes);
		lexer.setParseOptions(parseOptions);
		final de.be4.classicalb.core.parser.parser.Parser parser = new de.be4.classicalb.core.parser.parser.Parser(lexer);
		return parser.parse().getPParseUnit();
	}

	public IDefinitions getDefFileDefinitions() {
		return defFileDefinitions;
	}

	public DefinitionTypes getDefinitionTypes() {
		return this.definitionTypes;
	}

}
