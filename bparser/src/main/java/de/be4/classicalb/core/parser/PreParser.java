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
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.be4.classicalb.core.parser.analysis.checking.DefinitionCollector;
import de.be4.classicalb.core.parser.analysis.checking.DefinitionPreCollector;
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
import de.be4.classicalb.core.parser.util.Utils;
import de.be4.classicalb.core.preparser.lexer.LexerException;
import de.be4.classicalb.core.preparser.node.Start;
import de.be4.classicalb.core.preparser.node.TStringLiteral;
import de.be4.classicalb.core.preparser.node.Token;
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
	private final File modelFile;
	private final DefinitionTypes definitionTypes;
	private final IDefinitions defFileDefinitions;
	private final ParseOptions parseOptions;
	private final IFileContentProvider contentProvider;
	private final List<String> doneDefFiles;

	private int startLine;
	private int startColumn;

	public PreParser(PushbackReader pushbackReader, File modelFile,
			IFileContentProvider contentProvider,
			List<String> doneDefFiles,
			ParseOptions parseOptions, IDefinitions definitions) {
		this.pushbackReader = pushbackReader;
		this.modelFile = modelFile;
		this.contentProvider = contentProvider;
		this.doneDefFiles = doneDefFiles;
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
		Start rootNode;
		try {
			rootNode = preParser.parse();
		} catch (final ParserException e) {
			if (e.getToken() instanceof de.be4.classicalb.core.preparser.node.TDefinitions) {
				final Token errorToken = e.getToken();
				final String message = "[" + errorToken.getLine() + "," + errorToken.getPos() + "] "
						+ "Clause 'DEFINITIONS' is used more than once";
				throw new PreParseException(e.getToken(), message);
			} else {
				throw new PreParseException(e.getToken(), e.getLocalizedMessage(), e);
			}
		} catch (final LexerException e) {
			throw new PreParseException(e.getLocalizedMessage(), e);
		}

		final DefinitionPreCollector collector = new DefinitionPreCollector();
		rootNode.apply(collector);

		evaluateDefinitionFiles(collector.getFileDefinitions());

		List<Token> sortedDefinitionList = sortDefinitionsByTopologicalOrderAndCheckForCycles(
				collector.getDefinitions());

		evaluateTypes(sortedDefinitionList, collector.getDefinitions());

	}

	private void evaluateDefinitionFiles(List<TStringLiteral> list)
			throws PreParseException, BCompoundException {

		IDefinitionFileProvider cache = null;
		if (contentProvider instanceof IDefinitionFileProvider) {
			cache = (IDefinitionFileProvider) contentProvider;
		}

		for (TStringLiteral filenameString : list) {
			// Unquote and unescape the definition file name string.
			String quotedFilename = filenameString.getText();
			String fileName = Utils.unescapeStringContents(Utils.removeSurroundingQuotes(quotedFilename, '"'));
			// Note, that the fileName could be a relative path, e.g.
			// ./foo/bar/defs.def
			try {
				if (doneDefFiles.contains(fileName)) {
					StringBuilder sb = new StringBuilder();
					for (String string : doneDefFiles) {
						sb.append(string).append(" -> ");
					}
					sb.append(fileName);
					throw new PreParseException(filenameString,
							"Cyclic references in definition files: " + sb);
				}

				IDefinitions definitions;
				if (cache != null && cache.getDefinitions(fileName) != null) {
					definitions = cache.getDefinitions(fileName);
				} else {
					File directory = modelFile == null ? null : modelFile.getParentFile();
					final String content = contentProvider.getFileContent(directory, fileName);
					final File file = contentProvider.getFile(directory, fileName);
					final BParser parser = new BParser(fileName, parseOptions);
					parser.setContentProvider(contentProvider);
					parser.getDoneDefFiles().addAll(doneDefFiles);
					parser.getDoneDefFiles().add(fileName);
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
				throw new PreParseException(filenameString, "Definition file cannot be read: " + e, e);
			} catch (BCompoundException e) {
				throw e.withMissingLocations(Collections.singletonList(BException.Location.fromNode(fileName, filenameString)));
			}
		}
	}

	private void evaluateTypes(final List<Token> sortedDefinitionList, final Map<Token, Token> definitions)
			throws PreParseException {
		// use linked list as we rely on pop() and push()
		final LinkedList<Token> remainingDefinitions = new LinkedList<>(sortedDefinitionList);
		final LinkedList<Token> currentlyUnparseableDefinitions = new LinkedList<>();
		Set<String> todoDefs = new HashSet<>();
		for (Token token : remainingDefinitions) {
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
				final Token definition = remainingDefinitions.pop();

				final Token defRhs = definitions.get(definition);
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
			final Token definition = remainingDefinitions.pop();
			final Token defRhs = definitions.get(definition);
			DefinitionType definitionType = determineType(definition, defRhs, todoDefs);
			if (definitionType.errorMessage != null) {
				String message = definitionType.errorMessage;
				if (modelFile != null) {
					message += " in file: " + modelFile;
				}
				throw new PreParseException(message);
			} else {
				// fall back message
				throw new PreParseException(definition, "[" + definition.getLine() + "," + definition.getPos()
						+ "] expecting wellformed expression, predicate or substitution as DEFINITION body (DEFINITION arguments assumed to be expressions)");
			}
		}
	}

	private List<Token> sortDefinitionsByTopologicalOrderAndCheckForCycles(Map<Token, Token> definitions)
			throws PreParseException {
		final Set<String> definitionNames = new HashSet<>();
		final Map<String, Token> definitionMap = new HashMap<>();
		for (Token token : definitions.keySet()) {
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
			final Token firstDefinitionToken = definitionMap.get(cycle.get(0));
			throw new PreParseException(firstDefinitionToken, "Cyclic references in definitions: " + sb);
		} else {
			List<Token> sortedDefinitionTokens = new ArrayList<>();
			for (String name : sortedDefinitionNames) {
				sortedDefinitionTokens.add(definitionMap.get(name));
			}
			return sortedDefinitionTokens;
		}

	}

	private Map<String, Set<String>> determineDependencies(Set<String> definitionNames, Map<Token, Token> definitions)
			throws PreParseException {
		HashMap<String, Set<String>> dependencies = new HashMap<>();
		for (Entry<Token, Token> entry : definitions.entrySet()) {
			Token nameToken = entry.getKey();
			Token rhsToken = entry.getValue();
			// The FORMULA_PREFIX is needed to switch the lexer state from
			// section to normal. Note, that we do not parse the right hand side
			// of the definition here. Hence FORMULA_PREFIX has no further
			// meaning and substitutions can also be handled by the lexer.
			final Reader reader = new StringReader(BParser.FORMULA_PREFIX + "\n" + rhsToken.getText());

			final BLexer lexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE),
					new DefinitionTypes());
			lexer.setParseOptions(parseOptions);
			lexer.setLexerPreparse();
			Set<String> set = new HashSet<>();
			de.be4.classicalb.core.parser.node.Token next;
			try {
				next = lexer.next();
				while (!(next instanceof EOF)) {
					if (next instanceof TIdentifierLiteral) {
						TIdentifierLiteral id = (TIdentifierLiteral) next;
						String name = id.getText();
						if (definitionNames.contains(name)) {
							set.add(name);
						}
					}
					next = lexer.next();
				}
			} catch (IOException e) {
				throw new PreParseException("Error while parsing", e);
			} catch (BLexerException e) {
				de.be4.classicalb.core.parser.node.Token errorToken = e.getLastToken();
				final String newMessage = determineNewErrorMessageWithCorrectedPositionInformations(nameToken, rhsToken,
						errorToken, e.getMessage());
				throw new PreParseException(newMessage, e);
			} catch (de.be4.classicalb.core.parser.lexer.LexerException e) {
				final String newMessage = determineNewErrorMessageWithCorrectedPositionInformationsWithoutToken(
						nameToken, rhsToken, e.getMessage());
				throw new PreParseException(newMessage, e);
			}
			dependencies.put(nameToken.getText(), set);
		}
		return dependencies;
	}

	@SuppressWarnings("unused")
	private LinkedList<Token> sortDefinitionsByPosition(final Map<Token, Token> definitions) {
		// LinkedList will be used as a queue later on!
		// however, the list is needed for collections.sort
		// we can not use a priority queue to sort, as the sorting is done once
		// afterwards, it has to remain unsorted
		final LinkedList<Token> list = new LinkedList<>(definitions.keySet());
		/*
		 * Sort the definitions in order of their appearance in the sourcecode.
		 * Dependencies in between definitions are handled later when computing
		 * there type
		 */
		list.sort((o1, o2) -> {
			if (o1.getLine() == o2.getLine()) {
				if (o1.getPos() == o2.getPos())
					return 0;
				else
					return o1.getPos() - o2.getPos();
			} else
				return o1.getLine() - o2.getLine();
		});
		return list;
	}

	static class DefinitionType {
		Definitions.Type type;
		String errorMessage;
		de.be4.classicalb.core.parser.node.Token errorToken;

		DefinitionType() {

		}

		DefinitionType(Definitions.Type t, de.be4.classicalb.core.parser.node.Token n) {
			this.type = t;
			this.errorToken = n;
		}

		DefinitionType(Definitions.Type t) {
			this.type = t;
		}

		DefinitionType(String errorMessage, de.be4.classicalb.core.parser.node.Token t) {
			this.errorMessage = errorMessage;
			this.errorToken = t;
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
	private DefinitionType determineType(final Token definition, final Token rhsToken,
			final Set<String> untypedDefinitions) throws PreParseException {

		final String definitionRhs = rhsToken.getText();

		de.be4.classicalb.core.parser.node.Start start;
		de.be4.classicalb.core.parser.node.Token errorToken;
		try {
			start = tryParsing(BParser.FORMULA_PREFIX, definitionRhs);
			// Predicate?
			PParseUnit parseunit = start.getPParseUnit();
			if (parseunit instanceof APredicateParseUnit) {
				return new DefinitionType(IDefinitions.Type.Predicate);
			}

			// Expression or Expression/Substituion (e.g. f(x))?
			AExpressionParseUnit expressionParseUnit = (AExpressionParseUnit) parseunit;

			PreParserIdentifierTypeVisitor visitor = new PreParserIdentifierTypeVisitor(untypedDefinitions);
			expressionParseUnit.apply(visitor);

			if (visitor.isUntypedDefinitionUsed()) {
				// the parseunit uses another definition which is not yet typed
				return new DefinitionType();
			}

			PExpression expression = expressionParseUnit.getExpression();
			if ((expression instanceof AIdentifierExpression) || (expression instanceof AFunctionExpression)
					|| (expression instanceof ADefinitionExpression)) {
				return new DefinitionType(IDefinitions.Type.ExprOrSubst);
			}

			return new DefinitionType(IDefinitions.Type.Expression);

		} catch (de.be4.classicalb.core.parser.parser.ParserException e) {
			errorToken = e.getToken();
			try {
				tryParsing(BParser.SUBSTITUTION_PREFIX, definitionRhs);
				return new DefinitionType(IDefinitions.Type.Substitution, errorToken);
			} catch (de.be4.classicalb.core.parser.parser.ParserException ex) {
				final de.be4.classicalb.core.parser.node.Token errorToken2 = ex.getToken();
				if (errorToken.getLine() > errorToken2.getLine() || (errorToken.getLine() == errorToken2.getLine()
						&& errorToken.getPos() >= errorToken2.getPos())) {
					final String newMessage = determineNewErrorMessageWithCorrectedPositionInformations(definition,
							rhsToken, errorToken, e.getMessage());
					return new DefinitionType(newMessage, errorToken);
				} else {
					final String newMessage = determineNewErrorMessageWithCorrectedPositionInformations(definition,
							rhsToken, errorToken2, ex.getMessage());
					return new DefinitionType(newMessage, errorToken2);
				}
			} catch (BLexerException e1) {
				errorToken = e1.getLastToken();
				final String newMessage = determineNewErrorMessageWithCorrectedPositionInformations(definition,
						rhsToken, errorToken, e.getMessage());
				throw new PreParseException(newMessage);
			} catch (de.be4.classicalb.core.parser.lexer.LexerException e3) {
				throw new PreParseException(determineNewErrorMessageWithCorrectedPositionInformationsWithoutToken(
						definition, rhsToken, e3.getMessage()), e);
			} catch (IOException e1) {
				throw new PreParseException(e.toString(), e);
			}
		} catch (BLexerException e) {
			errorToken = e.getLastToken();
			final String newMessage = determineNewErrorMessageWithCorrectedPositionInformations(definition, rhsToken,
					errorToken, e.getMessage());
			throw new PreParseException(newMessage, e);
		} catch (de.be4.classicalb.core.parser.lexer.LexerException e) {
			throw new PreParseException(determineNewErrorMessageWithCorrectedPositionInformationsWithoutToken(
					definition, rhsToken, e.getMessage()), e);
		} catch (IOException e) {
			throw new PreParseException(e.toString(), e);
		}

	}

	private String determineNewErrorMessageWithCorrectedPositionInformations(Token definition, Token rhsToken,
			de.be4.classicalb.core.parser.node.Token errorToken, String oldMessage) {
		// the parsed string starts in the second line, e.g. #formula\n ...
		int line = errorToken.getLine();
		int pos = errorToken.getPos();
		pos = line == 2 ? rhsToken.getPos() + pos - 1 : pos;
		line = definition.getLine() + line - 2;
		final int index = oldMessage.indexOf(']');
		String message = oldMessage.substring(index + 1);
		if (oldMessage.contains("expecting: EOF")) {
			message = "expecting end of definition";
		}
		errorToken.setLine(line);
		errorToken.setPos(pos);
		return "[" + line + "," + pos + "] " + message;
	}

	private String determineNewErrorMessageWithCorrectedPositionInformationsWithoutToken(Token definition,
			Token rhsToken, String oldMessage) {
		Pattern pattern = Pattern.compile("\\d+");
		Matcher m = pattern.matcher(oldMessage);
		m.find();
		int line = Integer.parseInt(m.group());
		m.find();
		int pos = Integer.parseInt(m.group());
		pos = line == 2 ? rhsToken.getPos() + pos - 1 : pos;
		line = definition.getLine() + line - 2;
		final int index = oldMessage.indexOf(']');
		String message = oldMessage.substring(index + 1);
		return "[" + line + "," + pos + "]" + message;
	}

	private de.be4.classicalb.core.parser.node.Start tryParsing(final String prefix, final String definitionRhs)
			throws de.be4.classicalb.core.parser.lexer.LexerException,
			de.be4.classicalb.core.parser.parser.ParserException, IOException {

		final Reader reader = new StringReader(prefix + "\n" + definitionRhs);
		final BLexer lexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE), this.definitionTypes);
		lexer.setParseOptions(parseOptions);
		final de.be4.classicalb.core.parser.parser.Parser parser = new de.be4.classicalb.core.parser.parser.Parser(lexer);
		return parser.parse();
	}

	public IDefinitions getDefFileDefinitions() {
		return defFileDefinitions;
	}

	public DefinitionTypes getDefinitionTypes() {
		return this.definitionTypes;
	}

}
