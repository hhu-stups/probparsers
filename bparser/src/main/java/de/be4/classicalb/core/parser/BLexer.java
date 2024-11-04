package de.be4.classicalb.core.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.lexer.Lexer;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;

public class BLexer extends Lexer {

	// PUSHBACK_BUFFER_SIZE should be more than the max length of any keyword
	public static final int PUSHBACK_BUFFER_SIZE = 99;

	private static final Collection<Class<? extends Token>> CLAUSE_TOKEN_CLASSES = Collections.unmodifiableCollection(Arrays.asList(
		// Beginning or end of a section:
		TAssertions.class,
		TConstants.class,
		TAbstractConstants.class,
		TConcreteConstants.class,
		TProperties.class,
		TConstraints.class,
		TVariables.class,
		TAbstractVariables.class,
		TConcreteVariables.class,
		TInvariant.class,
		TInitialisation.class,
		TLocalOperations.class,
		TOperations.class,
		//TEnd.class, // can be end of expression or predicate with IF-THEN-ELSE
		EOF.class
		// ...
	));

	private static final Collection<Class<? extends Token>> BIN_EXPR_OPERATORS = Collections.unmodifiableCollection(Arrays.asList(
		TEqual.class,
		TNotEqual.class,
		TInclusion.class,
		TNonInclusion.class,
		TElementOf.class,
		TNotBelonging.class,
		TIntersection.class,
		TUnion.class,
		TSetSubtraction.class,
		TPlus.class,
		// Note: TMinus is also unary! x = -1 or x : -1..10 is possible
		TDivision.class,
		TMod.class,
		TProduct.class,
		TPowerOf.class,
		TLessEqual.class,
		TGreaterEqual.class,
		TLess.class, // The parser currently allows <> for empty sequence, hence x = <> or <> = .. are all possible; but we now treat <> as a single token
		TGreater.class,
		TOverwriteRelation.class,
		TInterval.class,
		TConcatSequence.class,
		TMaplet.class,
		TRangeRestriction.class,
		TRangeSubtraction.class,
		TDomainRestriction.class,
		TDomainSubtraction.class
	));

	private static final Collection<Class<? extends Token>> FUNCTION_OPERATOR_KEYWORDS = Collections.unmodifiableCollection(Arrays.asList(
		// real operators floor(.), ceiling(.), real(.)
		TConvertIntFloor.class,
		TConvertIntCeiling.class,
		TConvertReal.class,
		// regular operators
		TBoolCast.class,
		TCard.class,
		TIterate.class,
		TClosure.class,
		TClosure1.class,
		TRel.class,
		TFnc.class,
		TPerm.class,
		TMin.class,
		TMax.class,
		TDom.class,
		TRan.class,
		TId.class, // ( {2} ; id) not allowed
		// TO DO: prj1, prj2
		// Record operators:
		TStruct.class,
		TRec.class,
		// sequence operators:
		TSize.class,
		TFront.class,
		TFirst.class, //  ( [ [1,2] ] ; first) = [ [1] ] not accepted in Atelier-B
		TTail.class,
		TLast.class,
		TRev.class, // ( [[1,2]] ; rev)  not accepted in Atelier-B
		TConc.class
	));

	private static final Collection<Class<? extends Token>> LITERAL_TOKEN_CLASSES = Collections.unmodifiableCollection(Arrays.asList(
		TIntegerLiteral.class,
		TStringLiteral.class,
		TRealLiteral.class,
		THexLiteral.class
	));

	private static final Map<String, String> INVALID_UNICODE_SYMBOL_MESSAGES;
	static {
		Map<String, String> invalidUnicodeSymbolMessages = new HashMap<>();
		invalidUnicodeSymbolMessages.put("⋀", "N-ary conjunction not allowed, use '∀' instead - or did you mean '∧' for binary conjunction?");
		invalidUnicodeSymbolMessages.put("⋁", "N-ary disjunction not allowed, use '∃' instead - or did you mean '∨' for binary disjunction?");
		invalidUnicodeSymbolMessages.put("∊", "Small element-of not allowed, use '∈' instead");
		invalidUnicodeSymbolMessages.put("∍", "Small contains as member not allowed, reorder arguments and use '∈' instead");
		invalidUnicodeSymbolMessages.put("∄", "Not-exists not supported, use '¬' and '∃' instead");
		invalidUnicodeSymbolMessages.put("⊢", "Operator not allowed, use implication '⇒' instead");
		invalidUnicodeSymbolMessages.put("⊧", "Operator not allowed, use implication '⇒' instead");
		invalidUnicodeSymbolMessages.put("⊦", "operator not allowed, use implication '⇒' instead");
		invalidUnicodeSymbolMessages.put("⇐", "Inverse implication not supported, reorder arguments and use implication '⇒' instead");
		invalidUnicodeSymbolMessages.put("⟸", "Inverse implication not supported, reorder arguments and use implication '⇒' instead");
		INVALID_UNICODE_SYMBOL_MESSAGES = Collections.unmodifiableMap(invalidUnicodeSymbolMessages);
	}

	private static final Map<Class<? extends Token>, Map<Class<? extends Token>, String>> invalid = new HashMap<>();

	private static void addInvalid(Class<? extends Token> f, Class<? extends Token> s, String message) {
		Map<Class<? extends Token>, String> secs = invalid.get(f);
		if (secs == null)
			secs = new HashMap<>();
		secs.put(s, message);
		invalid.put(f, secs);
	}

	static {
		addInvalid(TSemicolon.class, TSemicolon.class, "Two succeeding semicolons are not allowed.");
		addInvalid(TDoubleVerticalBar.class, TDoubleVerticalBar.class,
				"is not allowed (probably one || too many).");
		addInvalid(TSetSubtraction.class, TEqual.class, "You need to use /= for inequality and not \\=.");
		addInvalid(TSetSubtraction.class, TElementOf.class, "You need to use /: for not membership and not \\:.");
		addInvalid(TSetSubtraction.class, TInclusion.class, "You need to use /<: for not subset and not \\<:.");
		addInvalid(TSetSubtraction.class, TStrictInclusion.class, "You need to use /<<: for not strict subset and not \\<<:.");

		for (Class<? extends Token> clauseTokenClass : CLAUSE_TOKEN_CLASSES) {
			String clauseName = clauseTokenClass.getSimpleName().substring(1).toUpperCase();
			//addInvalid(TConjunction.class, clauseTokenClass, "& " + clauseName + " is not allowed.");
			addInvalid(TPragmaLabel.class, clauseTokenClass, "A label pragma must be put before a predicate.");
			addInvalid(clauseTokenClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier.");
		
			addInvalid(TLeftPar.class, clauseTokenClass, "Closing parenthesis is missing.");
			addInvalid(TLeftBrace.class, clauseTokenClass, "Closing brace is missing.");
			addInvalid(TLeftBracket.class, clauseTokenClass, "Closing bracket is missing.");
			addInvalid(TBegin.class, clauseTokenClass, "Closing END is missing."); // does not seem to work
		}
		
		addInvalid(TLeftPar.class, TEnd.class, "Closing parenthesis is missing.");
		addInvalid(TLeftPar.class, TRightBrace.class, "Closing parenthesis is missing."); // ( }
		addInvalid(TLeftPar.class, TRightBracket.class, "Closing parenthesis is missing.");
		addInvalid(TLeftBrace.class, TEnd.class, "Closing brace is missing.");
		addInvalid(TLeftBrace.class, TRightPar.class, "Closing brace is missing.");
		addInvalid(TLeftBrace.class, TRightBracket.class, "Closing brace is missing.");
		addInvalid(TLeftBracket.class, TEnd.class, "Closing bracket is missing.");
		addInvalid(TLeftBracket.class, TRightPar.class, "Closing bracket is missing.");
		addInvalid(TLeftBracket.class, TRightBrace.class, "Closing bracket is missing.");
		addInvalid(TBegin.class, TRightPar.class, "Closing END is missing.");
		addInvalid(TBegin.class, TRightBrace.class, "Closing END is missing.");
		addInvalid(TBegin.class, TRightBracket.class, "Closing END is missing.");
		
		// add some rules for the binary infix logical operators:
		Set<Class<? extends Token>> binOpTokenClasses = new HashSet<>();
		binOpTokenClasses.add(TConjunction.class);
		binOpTokenClasses.add(TLogicalOr.class);
		binOpTokenClasses.add(TImplies.class);
		binOpTokenClasses.add(TEquivalence.class);

		for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
			String opName = binOpTokenClass.getSimpleName().substring(1).toUpperCase();
			// cover cases like: BINARY_LOGICAL_OPERATOR /*@desc txt */
			addInvalid(binOpTokenClass, TPragmaDescription.class, "A description pragma must be put *after* a predicate, not *before* it.");
			// cover cases like:  /*@label txt */ BINARY_LOGICAL_OPERATOR
			addInvalid(TPragmaLabel.class, binOpTokenClass, "A label pragma must be put *before* a predicate, not *after* it.");
		}
		
		// now treat rules that apply to binary infix logical and binary expression operators
		binOpTokenClasses.addAll(BIN_EXPR_OPERATORS);
		binOpTokenClasses.add(TComma.class); // this has multiple uses: tuples, definition arguments, separating ids
		
		
		for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
			String opName = binOpTokenClass.getSimpleName().substring(1).toUpperCase();
			// Note: opName is something like CONJUNCTION and not &,...
			
			// rules for two binary operators following each other:
			for (Class<? extends Token> binOpTokenClass2 : binOpTokenClasses) {
				String opName2 = binOpTokenClass2.getSimpleName().substring(1).toUpperCase();
				addInvalid(binOpTokenClass, binOpTokenClass2,"Invalid combination of binary operators.");
			}
			
			// now rules for beginning/end of sections:
			for (Class<? extends Token> clauseTokenClass : CLAUSE_TOKEN_CLASSES) {
				String clauseName = clauseTokenClass.getSimpleName().substring(1).toUpperCase();
				addInvalid(binOpTokenClass,clauseTokenClass,"Argument to binary operator is missing.");
				addInvalid(clauseTokenClass,binOpTokenClass,"Argument to binary operator is missing.");
			}
			addInvalid(binOpTokenClass, TEnd.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TElse.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TElsif.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TThen.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TRightPar.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TRightBrace.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TRightBracket.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TSemicolon.class,"Argument to binary operator is missing.");
			addInvalid(TLeftPar.class, binOpTokenClass, "Argument to binary operator is missing.");
			addInvalid(TSemicolon.class, binOpTokenClass,"Argument to binary operator is missing.");
			
			// cover cases like:  /*@symbolic */ BINARY_OPERATOR
			addInvalid(TPragmaSymbolic.class, binOpTokenClass, "A symbolic pragma must be put *before* a set comprehension or lambda.");
		}
		
		// now treat rules for only binary infix expression operators
		for (Class<? extends Token> binOpTokenClass : BIN_EXPR_OPERATORS) {
			addInvalid(TPragmaLabel.class, binOpTokenClass, "A label pragma must be put *before* a predicate, not inside it.");
			addInvalid(binOpTokenClass, TPragmaLabel.class, "A label pragma must be put before a *predicate*, it cannot be put before expressions.");
			addInvalid(binOpTokenClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier.");
			
			String opName = binOpTokenClass.getSimpleName().substring(1).toUpperCase();
			addInvalid(binOpTokenClass, TEnd.class, "Missing argument for binary operator " + opName + ".");
		}
		
		// override rules above with a more specific error message
		addInvalid(TConjunction.class, TConjunction.class, "Probably one & too many.");
		addInvalid(TLogicalOr.class, TLogicalOr.class, "Probably one 'or' too many.");
		addInvalid(TLess.class, TGreater.class, "<> is not allowed anymore, use [] for the empty sequence."); // this is rule is only of limited usefulness, until we remove the empty_sequence token
		
		
		// a few rules for keywords which are unary functions and require an opening parenthesis afterwards
		for (Class<? extends Token> funOpClass : FUNCTION_OPERATOR_KEYWORDS) {
			addInvalid(funOpClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier, not a keyword.");
			String opName = funOpClass.getSimpleName().substring(1).toLowerCase(); // TO DO: get real keyword name
			if (funOpClass == TConvertIntFloor.class) {
				opName = "floor";
			} else if( funOpClass == TConvertIntCeiling.class) {
				opName = "ceiling";
			} else if( funOpClass == TConvertReal.class) {
				opName = "real";
			} else if( funOpClass == TBoolCast.class) {
				opName = "bool";
			}
			String message = "This keyword (" + opName + ") must be followed by an opening parenthesis.";
			addInvalid(funOpClass, TRightPar.class, message);
			addInvalid(funOpClass, TRightBrace.class, message);
			addInvalid(funOpClass, TRightBracket.class, message);
			addInvalid(funOpClass, TSemicolon.class, message);
			addInvalid(funOpClass, TWhere.class, message);
			addInvalid(funOpClass, TThen.class, message);
			addInvalid(funOpClass, TElse.class, message);
			addInvalid(funOpClass, TEnd.class, message);
			for (Class<? extends Token> binOpTokenClass : BIN_EXPR_OPERATORS) {
				addInvalid(funOpClass, binOpTokenClass, message);
			}
			for (Class<? extends Token> clauseTokenClass : CLAUSE_TOKEN_CLASSES) {
				addInvalid(funOpClass,clauseTokenClass, message);
			}
			
			String message2 = "This keyword (" + opName + ") cannot be used as an identifier";
			addInvalid(TAny.class,funOpClass, message2);
			addInvalid(TConstants.class,funOpClass, message2);
			addInvalid(TAbstractConstants.class,funOpClass, message2);
			addInvalid(TConcreteConstants.class,funOpClass, message2);
			addInvalid(TVariables.class,funOpClass, message2);
			addInvalid(TAbstractVariables.class,funOpClass, message2);
			addInvalid(TConcreteVariables.class,funOpClass, message2);
			addInvalid(TOperations.class,funOpClass, message2);
		}
		
		// Other rules:
		addInvalid(TLeftPar.class, TRightPar.class, "Parentheses must contain arguments.");
		addInvalid(TBegin.class, TEnd.class, "Block must contain statements.");
		addInvalid(TIf.class, TEnd.class, "Block must contain statements.");
		addInvalid(TIf.class, TThen.class, "Block must contain statements.");
		addInvalid(TThen.class, TEnd.class, "Block must contain statements.");
		addInvalid(TThen.class, TElse.class, "Block must contain statements.");
		addInvalid(TThen.class, TElsif.class, "Block must contain statements.");
		addInvalid(TElsif.class, TEnd.class, "Block must contain statements.");
		addInvalid(TElsif.class, TElse.class, "Block must contain statements.");
		addInvalid(TElsif.class, TElsif.class, "Block must contain statements.");
		addInvalid(TElse.class, TEnd.class, "Block must contain statements.");
		addInvalid(TSelect.class, TEnd.class, "Block must contain statements.");
		addInvalid(TSelect.class, TThen.class, "Block must contain statements.");
		addInvalid(TChoice.class, TEnd.class, "Block must contain statements.");
		addInvalid(TChoice.class, TOr.class, "Block must contain statements.");
		addInvalid(TOr.class, TEnd.class, "Block must contain statements.");
		// more combination: CASE, WHILE
		
		
		addInvalid(TComma.class, TRightPar.class, "Missing expression after comma.");
		addInvalid(TComma.class, TRightBrace.class, "Missing expression after comma.");
		addInvalid(TComma.class, TRightBracket.class, "Missing expression after comma.");
		addInvalid(TSemicolon.class, TRightPar.class, "Missing expression after semicolon.");
		addInvalid(TSemicolon.class, TRightBrace.class, "Missing expression after semicolon.");
		addInvalid(TSemicolon.class, TRightBracket.class, "Missing expression after semicolon.");
		
		addInvalid(TComma.class, TPragmaDescription.class, "A description pragma must be put *after* a predicate or identifier.");
		addInvalid(TSemicolon.class, TPragmaDescription.class, "A description pragma must be put *after* a predicate or identifier.");
		addInvalid(TPragmaLabel.class, TComma.class, "A label pragma must be put *before* a predicate.");
		addInvalid(TPragmaLabel.class, TSemicolon.class, "A label pragma must be put *before* a predicate.");
		
		// invalid literal combinations:
		for (Class<? extends Token> litClass : LITERAL_TOKEN_CLASSES) {
			addInvalid(TIdentifierLiteral.class, litClass, "Missing operator or separator between identifier and literal.");
			addInvalid(litClass,TIdentifierLiteral.class, "Missing operator or separator between literal and identifier.");
			for (Class<? extends Token> litClass2 : LITERAL_TOKEN_CLASSES) {
				addInvalid(litClass, litClass2, "Missing operator or separator between literals.");
			}
		}
		// addInvalid(TIdentifierLiteral.class, TIdentifierLiteral.class,  "Missing operator or separator between identifier and identifier.");
		// we treat ref in languagextension not as keyword but as identifier; hence we cannot add this rule
		// see test de.be4.classicalb.core.parser.languageextension.RefinedOperationTest
	}

	private final DefinitionTypes definitions;

	private ParseOptions parseOptions = null;

	private Token lastToken;
	private Token comment = null;
	private StringBuilder commentBuffer = null;

	public BLexer(final PushbackReader in, final DefinitionTypes definitions) {
		super(in);
		this.definitions = definitions;
	}

	public BLexer(final PushbackReader in) {
		this(in, null);
	}

	public void setPosition(final int line, final int column) {
		this.line = line - 1;
		this.pos = column - 1;
	}

	private void findSyntaxError() throws LexerException {
		if (
			token == null || state.equals(State.BLOCK_COMMENT)
			|| token instanceof TWhiteSpace || token instanceof TLineComment
			|| token instanceof TComment || token instanceof TCommentEnd
			|| token instanceof TPragmaIdOrString || token instanceof TPragmaFreeText || token instanceof TPragmaEnd
		) {
			return; // we ignore these tokens for checking for invalid combinations
		}

		if (token instanceof TIllegalUnicodeSymbol) {
			String symbol = token.getText();
			String defaultMessage = "Invalid Unicode symbol: '" + symbol + "'.";
			String specificMessage = INVALID_UNICODE_SYMBOL_MESSAGES.get(symbol);
			if (specificMessage != null) {
				throw new BLexerException(token, defaultMessage + " " + specificMessage);
			} else {
				throw new BLexerException(token, defaultMessage);
			}
		}

		Class<? extends Token> tokenClass = token.getClass();
		if (lastToken != null) {
			Class<? extends Token> lastTokenClass = lastToken.getClass();
			
			if(parseOptions == null || !parseOptions.isIgnoreCheckingValidCombinations()) {
				checkForInvalidCombinations(lastTokenClass, tokenClass);
				// System.out.println("Ok: " + lastTokenClass + " -> " + tokenClass);
			}
		}

		lastToken = token;
	}

	private void checkForInvalidCombinations(Class<? extends Token> lastTokenClass, Class<? extends Token> tokenClass)
			throws LexerException {
		Map<Class<? extends Token>, String> map = invalid.get(lastTokenClass);
		if (map != null) {
			String string = map.get(tokenClass);
			if (string != null) {
				if (token instanceof EOF ) {
					throw new BLexerException(token, "Invalid combination of symbols: '" + lastToken.getText().trim() + "' before the end of file. " + string);
				} else {
					throw new BLexerException(token, "Invalid combination of symbols: '" + lastToken.getText().trim() + "' and '" + token.getText().trim() + "'. " + string);
				}
			}
		}

	}

	private void applyGrammarExtension() {
		if (parseOptions != null && token != null && 
			this.parseOptions.getGrammar().containsAlternativeDefinitionForToken(token)) {
			token = this.parseOptions.getGrammar().createNewToken(token);
		}
	}

	@Override
	protected void filter() throws LexerException, IOException {
		// System.out.println("State = " + state + " token = " + token);

		optimizeToken();

		if (parseOptions != null && this.parseOptions.isStrictPragmaChecking() &&
			token instanceof TUnrecognisedPragma) {
			throw new BLexerException(token, "Pragma '" + token.getText() + "' not recognised; supported pragmas are label, desc, symbolic, generated, package, import-package, file.");
		}

		if (token instanceof TCommentEnd) {
			commentBuffer.append(token.getText());
			comment.setText(commentBuffer.toString());
			token = comment;
			comment = null;
			commentBuffer = null;
		} else if (token instanceof TShebang && token.getLine() != 1) {
			throw new BLexerException(token, "#! only allowed in first line of the file", "#!", token.getLine(), token.getPos());
		} else if (state.equals(State.NORMAL)) {
			applyGrammarExtension();
			findSyntaxError(); // check for invalid combinations, ...
		} else if (state.equals(State.BLOCK_COMMENT)) {
			collectComment();
		} else if (state.equals(State.PRAGMA_DESCRIPTION_CONTENT) || state.equals(State.PRAGMA_CONTENT)) {
			findSyntaxError();
		}

		if (token != null) {
			if (definitions != null) {
				replaceDefTokens();
			}

		}
	}

	private void replaceDefTokens() throws LexerException {
		if (token instanceof TIdentifierLiteral) {
			// The identifier might be backquoted and needs to be unquoted before looking up the definition type.
			// This does *not* replace the token text yet - that happens later in SyntaxExtensionTranslator.
			String definitionName;
			try {
				definitionName = Utils.unquoteIdentifier(token.getText());
			} catch (IllegalArgumentException exc) {
				throw new BLexerException(token, exc);
			}
			Definitions.Type type = definitions.getType(definitionName);

			/*
			 * If no type is set, something went wrong during preparsing.
			 * Probably the right hand side of the definition was not parseble.
			 * But we'll also find this error in the main parser.
			 */
			if (type != null) {
				switch (type) {
				case Predicate: // generate def_literal_substitution token
					final Token predToken = new TDefLiteralPredicate(token.getText());
					predToken.setLine(token.getLine());
					predToken.setPos(token.getPos());
					token = predToken;
					break;

				case Substitution:  // generate def_literal_predicate token
					final Token substToken = new TDefLiteralSubstitution(token.getText());
					substToken.setLine(token.getLine());
					substToken.setPos(token.getPos());
					token = substToken;
					break;

				default:
					/*
					 * no replacement for expression definitions (done after
					 * parsing) or for normal identifier
					 */
					break;
				}
			}
		}
	}

	private void collectComment() throws LexerException {
		if (token instanceof EOF) {
			// make sure we don't loose this token, needed for error message
			throw new BLexerException(comment, "Comment not closed.");
		}

		// starting a new comment
		if (comment == null) {
			commentBuffer = new StringBuilder(token.getText());
			comment = token;
		} else {
			commentBuffer.append(token.getText());
			assert !(token instanceof TCommentEnd); // end of comment now handled in filter
		}
		token = null;
	}

	private void optimizeToken() {
		if (token instanceof TIdentifierLiteral) {
			token.setText(token.getText().intern());
		}
	}

	public ParseOptions getParseOptions() {
		return parseOptions;
	}

	public void setParseOptions(ParseOptions parseOptions) {
		this.parseOptions = parseOptions;
	}

}
