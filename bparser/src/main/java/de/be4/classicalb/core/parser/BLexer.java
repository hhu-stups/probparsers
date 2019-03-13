package de.be4.classicalb.core.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.lexer.Lexer;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.*;

public class BLexer extends Lexer {

	// PUSHBACK_BUFFER_SIZE should be more than the max length of any keyword
	public static final int PUSHBACK_BUFFER_SIZE = 99;

	private static Map<Class<? extends Token>, Map<Class<? extends Token>, String>> invalid = new HashMap<>();
	private static Set<Class<? extends Token>> clauseTokenClasses = new HashSet<>();
	private static Set<Class<? extends Token>> binOpTokenClasses = new HashSet<>();

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
				"|| || is not allowed (probably one || too many).");
		addInvalid(TSetSubtraction.class, TEqual.class, "You need to use /= for inequality and not \\=.");
		addInvalid(TSetSubtraction.class, TElementOf.class, "You need to use /: for not membership and not \\:.");
		addInvalid(TSetSubtraction.class, TInclusion.class, "You need to use /<: for not subset and not \\<:.");
		addInvalid(TSetSubtraction.class, TStrictInclusion.class, "You need to use /<<: for not strict subset and not \\<<:.");

		clauseTokenClasses.add(TAssertions.class);
		clauseTokenClasses.add(TConstants.class);
		clauseTokenClasses.add(TInitialisation.class);
		clauseTokenClasses.add(TInvariant.class);
		clauseTokenClasses.add(TOperations.class);
		clauseTokenClasses.add(TVariables.class);
		// ...

		for (Class<? extends Token> clauseTokenClass : clauseTokenClasses) {
			String clauseName = clauseTokenClass.getSimpleName().substring(1).toUpperCase();
			addInvalid(TConjunction.class, clauseTokenClass, "& " + clauseName + " is not allowed.");
			addInvalid(TPragmaLabel.class, clauseTokenClass, "A label pragma must be put before a predicate.");
			addInvalid(clauseTokenClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier.");
		}
		
	   // add some rules for the binary logical operators:
		binOpTokenClasses.add(TConjunction.class);
		binOpTokenClasses.add(TLogicalOr.class);
		binOpTokenClasses.add(TImplies.class);
		binOpTokenClasses.add(TEquivalence.class);

		for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
		 // cover cases like: BINARY_LOGICAL_OPERATOR /*@desc txt */
		    addInvalid(binOpTokenClass, TPragmaDescription.class, "A description pragma must be put *after* a predicate, not *before* it.");
		 // cover cases like:  /*@label txt */ BINARY_LOGICAL_OPERATOR
		    addInvalid(TPragmaLabel.class, binOpTokenClass,  "A label pragma must be put *before* a predicate, not *after* it.");
		}
		
		// now treat rules that apply to binary logical and binary expression operators
		binOpTokenClasses.add(TEqual.class);
		binOpTokenClasses.add(TNotEqual.class);
		binOpTokenClasses.add(TInclusion.class);
		binOpTokenClasses.add(TNonInclusion.class);
		binOpTokenClasses.add(TElementOf.class);
		binOpTokenClasses.add(TNotBelonging.class);
		binOpTokenClasses.add(TIntersection.class);
		binOpTokenClasses.add(TUnion.class);
		binOpTokenClasses.add(TSetSubtraction.class);
		binOpTokenClasses.add(TPlus.class); // Note: TMinus is also unary !  x = -1  or x : -1..10 is possible
		binOpTokenClasses.add(TDivision.class);
		binOpTokenClasses.add(TProduct.class);
		binOpTokenClasses.add(TPowerOf.class);
		binOpTokenClasses.add(TLessEqual.class);
		binOpTokenClasses.add(TGreaterEqual.class);
		// binOpTokenClasses.add(TLess.class);  // The parser currently allows <> for empty sequence, hence x = <> or <> = .. are all possible
		// binOpTokenClasses.add(TGreater.class);
		
		for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
			String opName = binOpTokenClass.getSimpleName().substring(1).toUpperCase();
			// Note: opName is something like CONJUNCTION and not &,...
			
			for (Class<? extends Token> binOpTokenClass2 : binOpTokenClasses) {
				String opName2 = binOpTokenClass2.getSimpleName().substring(1).toUpperCase();
				addInvalid(binOpTokenClass, binOpTokenClass2, opName + " " + opName2 + " is not allowed.");
			}
			
		    // cover cases like:  /*@symbolic */ BINARY_OPERATOR
		    addInvalid(TPragmaSymbolic.class, binOpTokenClass,  "A symbolic pragma must be put *before* a set comprehension or lambda.");
		}
		
		// now treat rules for only binary expression operators
		binOpTokenClasses = new HashSet<>();
		binOpTokenClasses.add(TEqual.class);
		binOpTokenClasses.add(TNotEqual.class);
		binOpTokenClasses.add(TInclusion.class);
		binOpTokenClasses.add(TNonInclusion.class);
		binOpTokenClasses.add(TElementOf.class);
		binOpTokenClasses.add(TNotBelonging.class);
		binOpTokenClasses.add(TIntersection.class);
		binOpTokenClasses.add(TUnion.class);
		binOpTokenClasses.add(TSetSubtraction.class);
		binOpTokenClasses.add(TPlus.class);
		binOpTokenClasses.add(TDivision.class);
		binOpTokenClasses.add(TProduct.class);
		binOpTokenClasses.add(TPowerOf.class);
		//binOpTokenClasses.add(TLess.class);  // The parser currently allows <> for empty sequence
		//binOpTokenClasses.add(TGreater.class);
		binOpTokenClasses.add(TLessEqual.class);
		binOpTokenClasses.add(TGreaterEqual.class);
		
		for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
		    addInvalid(TPragmaLabel.class, binOpTokenClass,  "A label pragma must be put *before* a predicate, not inside it.");
		     addInvalid(binOpTokenClass, TPragmaLabel.class,  "A label pragma must be put before a *predicate*, it cannot be put before expressions.");
		     addInvalid(binOpTokenClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier.");
		}
		
		// override rules above with a more specific error message
		addInvalid(TConjunction.class, TConjunction.class, "& & is not allowed (probably one & too many).");
		addInvalid(TLogicalOr.class, TLogicalOr.class, "or or is not allowed (probably one 'or' too many).");
	}

	private ParseOptions parseOptions = null;

	private Token comment = null;
	private StringBuilder commentBuffer = null;

	private final DefinitionTypes definitions;

	public BLexer(final PushbackReader in, final DefinitionTypes definitions, final int tokenCountPrediction) {
		super(in);
		this.definitions = definitions;
	}

	public BLexer(final PushbackReader in, final DefinitionTypes definitions) {
		this(in, definitions, -1);
	}

	public BLexer(final PushbackReader in) {
		this(in, null);
	}

	private Token lastToken;

	private void findSyntaxError() throws LexerException {
		if (token instanceof TWhiteSpace || token instanceof TLineComment ||
		    token instanceof TPragmaStart || token instanceof TPragmaEnd || token instanceof TPragmaIdOrString) {
			return; // we ignore these tokens for checking for invalid combinations
		} else if (lastToken == null) {
			lastToken = token;
			return;
		}
		Class<? extends Token> lastTokenClass = lastToken.getClass();
		Class<? extends Token> tokenClass = token.getClass();

		checkForInvalidCombinations(lastTokenClass, tokenClass);
		// System.out.println("Ok: " + lastTokenClass + " -> " + tokenClass);
        
		lastToken = token;
	}

	private void checkForInvalidCombinations(Class<? extends Token> lastTokenClass, Class<? extends Token> tokenClass)
			throws LexerException {
		Map<Class<? extends Token>, String> map = invalid.get(lastTokenClass);
		if (map != null) {
			String string = map.get(tokenClass);
			if (string != null) {
				int l = token.getLine();
				int c = token.getPos();
				ThrowDefaultLexerException("Invalid combination of symbols: " + string + "\n", string);
			}
		}

	}
	
	private void ThrowDefaultLexerException(String msg, String string) throws LexerException {
		int l = token.getLine();
		int c = token.getPos();
		throw new BLexerException(token, msg, string, l, c);
	
	}

	private void applyGrammarExtension() {
		if (parseOptions != null && this.parseOptions.getGrammar().containsAlternativeDefinitionForToken(token)) {
			token = this.parseOptions.getGrammar().createNewToken(token);
		}
	}

	@Override
	protected void filter() throws LexerException, IOException {
        // System.out.println("State = " + state + " token = " + token);
        if (parseOptions != null && this.parseOptions.isStrictPragmaChecking() &&
            token instanceof TUnrecognisedPragma) {
			ThrowDefaultLexerException("Pragma '" + token.getText() +"' not recognised; supported pragmas are label, desc, symbolic, generated, package, import-package, file.",token.getText());
        }
		if (state.equals(State.NORMAL)) {
			applyGrammarExtension();
			findSyntaxError();
		} else if (state.equals(State.COMMENT)) {
			collectComment();
		} else if ((state.equals(State.DESCRIPTION) || state.equals(State.PRAGMA_IGNORE)) &&
		            !(token instanceof TPragmaDescription)) {
			collectComment();
		} else if (state.equals(State.DESCRIPTION) || state.equals(State.PRAGMA_CONTENT)) {
			findSyntaxError();
		} else if (state.equals(State.SHEBANG) && token.getLine() != 1) {
			ThrowDefaultLexerException("#! only allowed in first line of the file","#!");
		}

		if (token != null) {
			if (definitions != null) {
				replaceDefTokens();
			}

			buildTokenList();

		}
	}

	private void replaceDefTokens() {
		if (token instanceof TIdentifierLiteral) {
			final Definitions.Type type = definitions.getType(token.getText());

			/*
			 * If no type is set, something went wrong during preparsing.
			 * Probably the right hand side of the definition was not parseble.
			 * But we'll also find this error in the main parser.
			 */
			if (type != null) {
				switch (type) {
				case Predicate:
					final Token predToken = new TDefLiteralPredicate(token.getText());
					predToken.setLine(token.getLine());
					predToken.setPos(token.getPos());
					token = predToken;
					break;

				case Substitution:
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

	private void buildTokenList() {
		if (token != null) {
			// tokenList.add(token);
		}
	}

	private void collectComment() throws LexerException, IOException {
		if (token instanceof EOF) {
			// make sure we don't loose this token, needed for error message
			// tokenList.add(token);
			// final int line = token.getLine() - 1;
			// final int pos = token.getPos() - 1;
			final String text = token.getText();
			throw new BLexerException(comment, "Comment not closed.", text, comment.getLine(), comment.getPos());
		}

		// starting a new comment
		if (comment == null) {
			commentBuffer = new StringBuilder(token.getText());
			comment = token;
			token = null;
		} else {
			commentBuffer.append(token.getText());

			// end of comment reached?
			if (token instanceof TCommentEnd) {
				String text = commentBuffer.toString();
				if (state.equals(State.DESCRIPTION))
					text = text.substring(0, text.length() - 2);
				comment.setText(text.trim());
				token = comment;
				comment = null;
				commentBuffer = null;
				state = State.NORMAL;

			} else {
				token = null;
			}
		}
	}

	public ParseOptions getParseOptions() {
		return parseOptions;
	}

	public void setParseOptions(ParseOptions parseOptions) {
		this.parseOptions = parseOptions;
	}

}
