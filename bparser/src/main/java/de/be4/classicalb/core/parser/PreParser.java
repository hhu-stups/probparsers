package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.checking.DefinitionPreCollector;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.node.ADefinitionExpression;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.APredicateParseUnit;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PParseUnit;
import de.be4.classicalb.core.preparser.lexer.LexerException;
import de.be4.classicalb.core.preparser.node.Start;
import de.be4.classicalb.core.preparser.node.Token;
import de.be4.classicalb.core.preparser.parser.Parser;
import de.be4.classicalb.core.preparser.parser.ParserException;

public class PreParser {

	private final PushbackReader pushbackReader;
	private boolean debugOutput = false;
	private DefinitionTypes types;

	private final IDefinitions defFileDefinitions = new Definitions();
	private final ParseOptions parseOptions;
	private final IFileContentProvider contentProvider;
	private final List<String> doneDefFiles;
	private final String modelFileName;
	private final File directory;

	public PreParser(final PushbackReader pushbackReader,
			final IFileContentProvider contentProvider,
			final List<String> doneDefFiles, final String modelFileName,
			final File directory, ParseOptions parseOptions) {
		this.pushbackReader = pushbackReader;
		this.contentProvider = contentProvider;
		this.doneDefFiles = doneDefFiles;
		this.modelFileName = modelFileName;
		this.directory = directory;
		this.parseOptions = parseOptions;
	}

	public void setDebugOutput(final boolean debugOutput) {
		this.debugOutput = debugOutput;
	}

	public DefinitionTypes parse() throws PreParseException, IOException,
			BException {
		types = new DefinitionTypes();

		final PreLexer preLexer = new PreLexer(pushbackReader);

		final Parser preParser = new Parser(preLexer);
		Start rootNode = null;
		try {
			rootNode = preParser.parse();
		} catch (final ParserException e) {
			if (e.getToken() instanceof de.be4.classicalb.core.preparser.node.TDefinitions) {
				final Token errorToken = e.getToken();
				final String message = "[" + errorToken.getLine() + ","
						+ errorToken.getPos() + "] "
						+ "Clause 'DEFINITIONS' is used more than once";
				throw new PreParseException(e.getToken(), message);
			} else {
				throw new PreParseException(e.getToken(),
						e.getLocalizedMessage());
			}
		} catch (final LexerException e) {
			throw new PreParseException(e.getLocalizedMessage());
		}

		final DefinitionPreCollector collector = new DefinitionPreCollector();
		rootNode.apply(collector);

		evaluateDefinitionFiles(collector.getFileDefinitions());
		evaluateTypes(collector.getDefinitions());

		return types;
	}

	private void evaluateDefinitionFiles(final List<Token> list)
			throws PreParseException, BException {

		IDefinitionFileProvider cache = null;
		if (contentProvider instanceof IDefinitionFileProvider) {
			cache = (IDefinitionFileProvider) contentProvider;
		}

		for (final Token fileNameToken : list) {
			final List<String> newDoneList = new ArrayList<String>(doneDefFiles);
			try {
				final String fileName = fileNameToken.getText();
				if (doneDefFiles.contains(fileName)) {
					StringBuilder sb = new StringBuilder();
					for (String string : doneDefFiles) {
						sb.append(string).append(" -> ");
					}
					sb.append(fileName);
					throw new PreParseException(fileNameToken,
							"Cyclic references in definition files: "
									+ sb.toString());
				}

				IDefinitions definitions;
				if (cache != null && cache.getDefinitions(fileName) != null) {
					definitions = cache.getDefinitions(fileName);
				} else {
					final String content = contentProvider.getFileContent(
							directory, fileName);
					newDoneList.add(fileName);
					final File file = contentProvider.getFile(directory,
							fileName);
					String filePath = fileName;
					if (file != null) {
						filePath = file.getCanonicalPath();
					}
					final BParser parser = new BParser(filePath, parseOptions);
					parser.setDirectory(directory);
					parser.setDoneDefFiles(newDoneList);
					parser.parse(content, debugOutput, contentProvider);

					definitions = parser.getDefinitions();

					if (cache != null) {
						cache.storeDefinition(fileName, definitions);
					}
				}

				defFileDefinitions.addAll(definitions);
				defFileDefinitions.addDefinitionFile(contentProvider.getFile(
						directory, fileName));
				types.addAll(definitions.getTypes());
			} catch (final IOException e) {
				throw new PreParseException(fileNameToken,
						"Definition file cannot be read: "
								+ e.getLocalizedMessage()
				// + " used in " + modelFileName
				);
			} finally {
			}
		}
	}

	private void evaluateTypes(final Map<Token, Token> definitions)
			throws PreParseException {
		// use linked list as we rely on pop() and push()
		final LinkedList<Token> remainingDefinitions = sortDefinitions(definitions);
		final LinkedList<Token> currentlyUnparseableDefinitions = new LinkedList<Token>();
		Set<String> todoDefs = new HashSet<String>();
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
				Definitions.Type type = null;
				DefinitionType definitionType = determineType(definition,
						defRhs, todoDefs);
				type = definitionType.type;
				if (type != null) {
					todoDefs.remove(definition.getText());
					oneParsed = true;
					types.addTyping(definition.getText(), type);
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
			DefinitionType definitionType = determineType(definition, defRhs,
					todoDefs);
			if (definitionType.errorMessage != null) {
				throw new PreParseException(definitionType.errorMessage
						+ " in file: " + modelFileName);
				// throw new BParseException(definitionType.errorToken,
				// definitionType.errorMessage + " in file: "
				// + modelFileName);
			} else {
				// fall back message
				throw new PreParseException(
						definition,
						"["
								+ definition.getLine()
								+ ","
								+ definition.getPos()
								+ "] expecting wellformed expression, predicate or substitution as DEFINITION body (DEFINITION arguments assumed to be expressions)");
			}
		}
	}

	private LinkedList<Token> sortDefinitions(
			final Map<Token, Token> definitions) {
		// LinkedList will be used as a queue later on!
		// however, the list is needed for collections.sort
		// we can not use a priority queue to sort, as the sorting is done once
		// afterwards, it has to remain unsorted
		final LinkedList<Token> list = new LinkedList<Token>();

		for (final Iterator<Token> iterator = definitions.keySet().iterator(); iterator
				.hasNext();) {
			final Token definition = iterator.next();
			list.add(definition);
		}

		/*
		 * Sort the definitions in order of their appearance in the sourcecode.
		 * Dependencies in between definitions are handled later when computing
		 * there type
		 */
		Collections.sort(list, new Comparator<Token>() {
			public int compare(final Token o1, final Token o2) {
				if (o1.getLine() == o2.getLine()) {
					if (o1.getPos() == o2.getPos())
						return 0;
					else
						return o1.getPos() - o2.getPos();
				} else
					return o1.getLine() - o2.getLine();
			}
		});
		return list;
	}

	class DefinitionType {
		Definitions.Type type;
		String errorMessage;
		de.be4.classicalb.core.parser.node.Token errorToken;

		DefinitionType() {

		}

		DefinitionType(Definitions.Type t,
				de.be4.classicalb.core.parser.node.Token n) {
			this.type = t;
			this.errorToken = n;
		}

		DefinitionType(Definitions.Type t) {
			this.type = t;
		}

		DefinitionType(String errorMessage,
				de.be4.classicalb.core.parser.node.Token t) {
			this.errorMessage = errorMessage;
			this.errorToken = t;
		}
	}

	private DefinitionType determineType(final Token definition,
	// private Definitions.Type determineType(final Token definition,
			final Token rhsToken, Set<String> definitions)
			throws PreParseException {

		final String definitionRhs = rhsToken.getText();

		de.be4.classicalb.core.parser.node.Start expr;
		de.be4.classicalb.core.parser.node.Token errorToken = null;
		try {
			expr = tryParsing(BParser.FORMULA_PREFIX, definitionRhs);
			// Predicate?
			PParseUnit parseunit = expr.getPParseUnit();
			if (parseunit instanceof APredicateParseUnit) {
				return new DefinitionType(IDefinitions.Type.Predicate);
			}

			// Expression or Expression/Substituion (e.g. f(x))?
			AExpressionParseUnit unit = (AExpressionParseUnit) parseunit;

			PreParserIdentifierTypeVisitor visitor = new PreParserIdentifierTypeVisitor(
					definitions);
			unit.apply(visitor);

			if (visitor.isKaboom()) {
				// return null;
				return new DefinitionType();
			}

			PExpression expression = unit.getExpression();
			if ((expression instanceof AIdentifierExpression)
					|| (expression instanceof AFunctionExpression)
					|| (expression instanceof ADefinitionExpression)) {
				return new DefinitionType(IDefinitions.Type.ExprOrSubst);
			}

			return new DefinitionType(IDefinitions.Type.Expression);

		} catch (de.be4.classicalb.core.parser.parser.ParserException e) {
			errorToken = e.getToken();
			try {
				tryParsing(BParser.SUBSTITUTION_PREFIX, definitionRhs);
				return new DefinitionType(IDefinitions.Type.Substitution,
						errorToken);
			} catch (de.be4.classicalb.core.parser.parser.ParserException ex) {
				final de.be4.classicalb.core.parser.node.Token errorToken2 = ex
						.getToken();
				if (errorToken.getLine() > errorToken2.getLine()
						|| (errorToken.getLine() == errorToken2.getLine() && errorToken
								.getPos() >= errorToken2.getPos())) {
					final String newMessage = determineNewErrorMessage(
							definition, rhsToken, errorToken, e.getMessage());
					return new DefinitionType(newMessage, errorToken);
				} else {
					final String newMessage = determineNewErrorMessage(
							definition, rhsToken, errorToken2, ex.getMessage());
					return new DefinitionType(newMessage, errorToken2);
				}
			} catch (BLexerException e1) {
				errorToken = e1.getLastToken();
				final String newMessage = determineNewErrorMessage(definition,
						rhsToken, errorToken, e.getMessage());
				throw new PreParseException(newMessage);
			} catch (de.be4.classicalb.core.parser.lexer.LexerException e3) {
				throw new PreParseException(e3.getMessage());
			}
		} catch (BLexerException e) {
			errorToken = e.getLastToken();
			final String newMessage = determineNewErrorMessage(definition,
					rhsToken, errorToken, e.getMessage());
			throw new PreParseException(newMessage);
		} catch (de.be4.classicalb.core.parser.lexer.LexerException e) {
			throw new PreParseException(e.getMessage());
		}

	}

	private String determineNewErrorMessage(Token definition, Token rhsToken,
			de.be4.classicalb.core.parser.node.Token errorToken,
			String oldMessage) {
		// the parsed string starts in the second line, e.g. #formula\n ...
		int line = errorToken.getLine();
		int pos = errorToken.getPos();
		pos = line == 2 ? rhsToken.getPos() + pos - 1 : pos;
		line = definition.getLine() + line - 2;
		final int index = oldMessage.indexOf("]");
		String message = oldMessage.substring(index + 1);
		if (oldMessage.contains("expecting: EOF")) {
			message = "expecting end of definition";
		}
		return "[" + line + "," + pos + "] " + message;
	}

	private de.be4.classicalb.core.parser.node.Start tryParsing(
			final String prefix, final String definitionRhs)
			throws de.be4.classicalb.core.parser.lexer.LexerException,
			de.be4.classicalb.core.parser.parser.ParserException {

		final Reader reader = new StringReader(prefix + "\n" + definitionRhs);
		final BLexer lexer = new BLexer(new PushbackReader(reader, 99), types); // FIXME
																				// Magic
																				// number!!!!
		lexer.setParseOptions(parseOptions);
		final de.be4.classicalb.core.parser.parser.Parser parser = new de.be4.classicalb.core.parser.parser.Parser(
				lexer);
		try {
			return parser.parse();
		} catch (final IOException e) {
			// IGNORE
			return null;
		}
	}

	public IDefinitions getDefFileDefinitions() {
		return defFileDefinitions;
	}

}
