package de.be4.classicalb.core.parser;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.analysis.checking.DefinitionCollector;
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
import de.be4.classicalb.core.preparser.node.ADefinitionsPreParserClause;
import de.be4.classicalb.core.preparser.node.AExpressionsPreParserClause;
import de.be4.classicalb.core.preparser.node.AFilePreParserDefinition;
import de.be4.classicalb.core.preparser.node.APreParseUnit;
import de.be4.classicalb.core.preparser.node.APreParserDefinition;
import de.be4.classicalb.core.preparser.node.APredicatesPreParserClause;
import de.be4.classicalb.core.preparser.node.PPreParseUnit;
import de.be4.classicalb.core.preparser.node.PPreParserClause;
import de.be4.classicalb.core.preparser.node.PPreParserDefinition;
import de.be4.classicalb.core.preparser.node.TPreParserIdentifier;
import de.be4.classicalb.core.preparser.node.TPreParserString;
import de.be4.classicalb.core.preparser.node.TRhsBody;
import de.be4.classicalb.core.preparser.parser.Parser;
import de.be4.classicalb.core.preparser.parser.ParserException;
import de.be4.classicalb.core.parser.FileSearchPathProvider;

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

	private static List<PPreParserDefinition> getDefinitionsFromClause(PPreParserClause clause) throws PreParseException {
		if (clause instanceof ADefinitionsPreParserClause) {
			return ((ADefinitionsPreParserClause)clause).getDefinitions();
		} else if (clause instanceof AExpressionsPreParserClause) {
			return ((AExpressionsPreParserClause)clause).getDefinitions();
		} else if (clause instanceof APredicatesPreParserClause) {
			return ((APredicatesPreParserClause)clause).getDefinitions();
		} else {
			throw new PreParseException(clause.getStartPos().getLine(), clause.getStartPos().getPos(), "Unhandled clause in PreParser: " + clause.getClass());
		}
	}

	public void parse() throws PreParseException, IOException, BCompoundException {
		final PreLexer preLexer = new PreLexer(pushbackReader);
		preLexer.setPosition(this.startLine, this.startColumn);

		final Parser preParser = new Parser(preLexer);
		PPreParseUnit preParseUnit;
		try {
			preParseUnit = preParser.parse().getPPreParseUnit();
		} catch (final ParserException e) {
			throw new PreParseException(e.getToken(), e.getRealMsg(), e);
		} catch (final LexerException e) {
			throw new PreParseException(e.getLine(), e.getPos(), e.getRealMsg(), e);
		}

		Map<TPreParserIdentifier, TRhsBody> definitions = new HashMap<>();
		List<TPreParserString> fileDefinitions = new ArrayList<>();

		for (PPreParserClause clause : ((APreParseUnit)preParseUnit).getClauses()) {
			for (PPreParserDefinition node : getDefinitionsFromClause(clause)) {
				if (node instanceof APreParserDefinition) {
					APreParserDefinition defNode = (APreParserDefinition)node;
					definitions.put(defNode.getDefName(), defNode.getRhs());
				} else if (node instanceof AFilePreParserDefinition) {
					AFilePreParserDefinition fileDefNode = (AFilePreParserDefinition)node;
					if (!(clause instanceof ADefinitionsPreParserClause)) {
						throw new PreParseException(fileDefNode.getFilename(), "Definition files can only be included in a DEFINITIONS clause, not in EXPRESSIONS/PREDICATES");
					}
					fileDefinitions.add(fileDefNode.getFilename());
				} else {
					throw new PreParseException(node.getStartPos().getLine(), node.getStartPos().getPos(), "Unhandled definition node in PreParser: " + node.getClass());
				}
			}
		}

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

		evaluateDefinitionFiles(fileDefinitions);

		List<TPreParserIdentifier> sortedDefinitionList = sortDefinitionsByTopologicalOrderAndCheckForCycles(definitions);

		// Determine each definition's abstract type using the main parser.
		// Because of the sorting and cycle check performed above,
		// a definition used by other definitions is processed before all definitions that use it.
		for (TPreParserIdentifier definition : sortedDefinitionList) {
			TRhsBody defRhs = definitions.get(definition);
			definitionTypes.addTyping(definition.getText(), determineType(defRhs));
		}
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
					throw new PreParseException(fileNameString, "Cyclic references in definition files: " + String.join(" -> ", definitionFileIncludeStack));
				}

				IDefinitions definitions;
				if (cache != null && cache.getDefinitions(fileName) != null) {
					definitions = cache.getDefinitions(fileName);
				} else {
					File directory = machineFile == null ? null : machineFile.getParentFile();
					final String content = contentProvider.getFileContent(directory, fileName);
					final File file = contentProvider.getFile(directory, fileName); // will also look in stdlib
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
			} catch (final FileNotFoundException e) {
				if (FileSearchPathProvider.fileNameCouldReferToLibrary(fileName)) {
					// the user was looking for a library definition file; maybe stdlib is set up incorrectly:
					throw new PreParseException(fileNameString, "Definition file not found: " + fileNameString
						+ " prob.stdlib = " + FileSearchPathProvider.getLibraryPath(), e);
				} else {
					throw new PreParseException(fileNameString, "Definition file not found: " + fileNameString, e);
				}
				
			} catch (final IOException e) {
				throw new PreParseException(fileNameString, "Definition file cannot be read: " + e, e);
			} catch (BCompoundException e) {
				throw e.withMissingLocations(BException.Location.locationsFromNodes(fileName, Collections.singletonList(fileNameString)));
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
			TPreParserIdentifier firstDefinitionToken = definitionMap.get(cycle.get(0));
			throw new PreParseException(firstDefinitionToken, "Cyclic references in definitions: " + String.join(" -> ", cycle));
		} else {
			List<TPreParserIdentifier> sortedDefinitionTokens = new ArrayList<>();
			for (String name : sortedDefinitionNames) {
				sortedDefinitionTokens.add(definitionMap.get(name));
			}
			return sortedDefinitionTokens;
		}

	}

	private BLexer makeLexerForDefinitionRhs(DefinitionTypes defTypes, String prefix, TRhsBody rhsToken) {
		Reader reader = new StringReader(prefix + " " + rhsToken.getText());
		BLexer lexer = new BLexer(new PushbackReader(reader, BLexer.PUSHBACK_BUFFER_SIZE), defTypes);
		// Decrease the start column by the size of the implicitly added prefix
		// so that the actual definition content starts at the desired position.
		lexer.setPosition(rhsToken.getLine(), rhsToken.getPos() - (prefix.length() + 1));
		lexer.setParseOptions(parseOptions);
		return lexer;
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
			BLexer lexer = makeLexerForDefinitionRhs(new DefinitionTypes(), BParser.FORMULA_PREFIX, rhsToken);
			Set<String> set = new HashSet<>();
			try {
				Token next = lexer.next();
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
				throw new PreParseException(e.getLastToken().getLine(), e.getLastToken().getPos(), adjustErrorMessage(e.getRealMsg()), e);
			} catch (de.be4.classicalb.core.parser.lexer.LexerException e) {
				throw new PreParseException(e.getLine(), e.getPos(), e.getRealMsg(), e);
			}
			dependencies.put(nameToken.getText(), set);
		}
		return dependencies;
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
	 * Try to choose the better of two parse exceptions
	 * after both attempts at parsing an ambiguous definition (formula or substitution) failed.
	 * This chooses the exception with the higher error position,
	 * in the hope that the parsing attempt that failed later is "more correct"
	 * and will thus have a more useful error message.
	 * 
	 * @param exc1 the first exception
	 * @param exc2 the second exception
	 * @return the exception with the higher position
	 */
	private static de.be4.classicalb.core.parser.parser.ParserException chooseBetterParseException(
		de.be4.classicalb.core.parser.parser.ParserException exc1,
		de.be4.classicalb.core.parser.parser.ParserException exc2
	) {
		if (
			exc1.getToken().getLine() > exc2.getToken().getLine()
			|| (exc1.getToken().getLine() == exc2.getToken().getLine() && exc1.getToken().getPos() >= exc2.getToken().getPos())
		) {
			return exc1;
		} else {
			return exc2;
		}
	}

	/**
	 * Determine the abstract type of the right-hand side of a definition,
	 * i. e. whether it's an expression, a predicate, or a substitution.
	 * The type of all definitions used by this definition must already be known -
	 * otherwise this method may return an incorrect type or parse error.
	 * 
	 * @param rhsToken the right-hand side of the definition (as a single token, merged by the {@link PreLexer})
	 * @return the type of the definition's right-hand side
	 * @throws PreParseException if the definition's right-hand side couldn't be parsed
	 */
	private IDefinitions.Type determineType(TRhsBody rhsToken) throws PreParseException {
		PParseUnit parseunit;
		try {
			// Try parsing the RHS as a Formula, i.e., either expression or predicate
			parseunit = tryParsing(BParser.FORMULA_PREFIX, rhsToken);
		} catch (de.be4.classicalb.core.parser.parser.ParserException formulaParseExc) {
			try {
				// try parsing the RHS now as a substitution:
				tryParsing(BParser.SUBSTITUTION_PREFIX, rhsToken);
				return IDefinitions.Type.Substitution;
			} catch (de.be4.classicalb.core.parser.parser.ParserException substitutionParseExc) {
				de.be4.classicalb.core.parser.parser.ParserException betterExc = chooseBetterParseException(formulaParseExc, substitutionParseExc);
				throw new PreParseException(betterExc.getToken().getLine(), betterExc.getToken().getPos(), adjustErrorMessage(betterExc.getRealMsg()), betterExc);
			} catch (BLexerException substitutionLexerExc) {
				throw new PreParseException(substitutionLexerExc.getLastToken().getLine(), substitutionLexerExc.getLastToken().getPos(), adjustErrorMessage(formulaParseExc.getRealMsg()), formulaParseExc);
			} catch (de.be4.classicalb.core.parser.lexer.LexerException substitutionLexerExc) {
				throw new PreParseException(substitutionLexerExc.getLine(), substitutionLexerExc.getPos(), substitutionLexerExc.getRealMsg(), formulaParseExc);
			} catch (IOException e1) {
				throw new PreParseException(formulaParseExc.toString(), formulaParseExc);
			}
		} catch (BLexerException formulaLexerExc) {
			throw new PreParseException(formulaLexerExc.getLastToken().getLine(), formulaLexerExc.getLastToken().getPos(), adjustErrorMessage(formulaLexerExc.getRealMsg()), formulaLexerExc);
		} catch (de.be4.classicalb.core.parser.lexer.LexerException formulaLexerExc) {
			throw new PreParseException(formulaLexerExc.getLine(), formulaLexerExc.getPos(), formulaLexerExc.getRealMsg(), formulaLexerExc);
		} catch (IOException e) {
			throw new PreParseException(e.toString(), e);
		}

		// check if the result is a Predicate?
		if (parseunit instanceof APredicateParseUnit) {
			return IDefinitions.Type.Predicate;
		}

		AExpressionParseUnit expressionParseUnit = (AExpressionParseUnit) parseunit;
		// check if we have definitely an Expression or an ambiguous Expression/Substitution (e.g. f(x))?
		return getExpressionDefinitionRhsType(expressionParseUnit.getExpression());
	}

	private static String adjustErrorMessage(String message) {
		if (message.contains("expecting: EOF")) {
			return "expecting end of definition";
		} else {
			return message.replace("the end of file", "the end of definition");
		}
	}

	private PParseUnit tryParsing(String prefix, TRhsBody rhsToken)
			throws de.be4.classicalb.core.parser.lexer.LexerException,
			de.be4.classicalb.core.parser.parser.ParserException, IOException {
		BLexer lexer = makeLexerForDefinitionRhs(this.definitionTypes, prefix, rhsToken);
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
