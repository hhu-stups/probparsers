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
import de.be4.classicalb.core.parser.lexer.Lexer.State;
import de.be4.classicalb.core.parser.node.*;

public class BLexer extends Lexer {

	// PUSHBACK_BUFFER_SIZE should be more than the max length of any keyword
	public static final int PUSHBACK_BUFFER_SIZE = 99;
	
	private boolean parse_definition=false;

	private static Map<Class<? extends Token>, Map<Class<? extends Token>, String>> invalid = new HashMap<>();
	private static Set<Class<? extends Token>> clauseTokenClasses = new HashSet<>();
	private static Set<Class<? extends Token>> binOpTokenClasses = new HashSet<>();
	private static Set<Class<? extends Token>> funOpKeywordTokenClasses = new HashSet<>();
	private static Set<Class<? extends Token>> literalTokenClasses = new HashSet<>();
	
	public void setLexerPreparse(){
		parse_definition = true; 
	}

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

		// Beginning or end of a section:
		clauseTokenClasses.add(TAssertions.class);
		clauseTokenClasses.add(TConstants.class);
		clauseTokenClasses.add(TAbstractConstants.class);
		clauseTokenClasses.add(TConcreteConstants.class);
		clauseTokenClasses.add(TProperties.class);
		clauseTokenClasses.add(TConstraints.class);
		clauseTokenClasses.add(TVariables.class);
		clauseTokenClasses.add(TAbstractVariables.class);
		clauseTokenClasses.add(TConcreteVariables.class);
		clauseTokenClasses.add(TInvariant.class);
		clauseTokenClasses.add(TInitialisation.class);
		clauseTokenClasses.add(TLocalOperations.class);
		clauseTokenClasses.add(TOperations.class);
		//clauseTokenClasses.add(TEnd.class); // can be end of expression or predicate with IF-THEN-ELSE
		clauseTokenClasses.add(EOF.class);
		// ...

		for (Class<? extends Token> clauseTokenClass : clauseTokenClasses) {
			String clauseName = clauseTokenClass.getSimpleName().substring(1).toUpperCase();
			//addInvalid(TConjunction.class, clauseTokenClass, "& " + clauseName + " is not allowed.");
			addInvalid(TPragmaLabel.class, clauseTokenClass, "A label pragma must be put before a predicate.");
			addInvalid(clauseTokenClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier.");
		}
		
		// add some rules for the binary infix logical operators:
		binOpTokenClasses.add(TConjunction.class);
		binOpTokenClasses.add(TLogicalOr.class);
		binOpTokenClasses.add(TImplies.class);
		binOpTokenClasses.add(TEquivalence.class);

		for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
			String opName = binOpTokenClass.getSimpleName().substring(1).toUpperCase();
			// cover cases like: BINARY_LOGICAL_OPERATOR /*@desc txt */
			addInvalid(binOpTokenClass, TPragmaDescription.class, "A description pragma must be put *after* a predicate, not *before* it.");
			// cover cases like:  /*@label txt */ BINARY_LOGICAL_OPERATOR
			addInvalid(TPragmaLabel.class, binOpTokenClass,  "A label pragma must be put *before* a predicate, not *after* it.");
		}
		
		// now treat rules that apply to binary infix logical and binary expression operators
		AddBinExprOperators();
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
			for (Class<? extends Token> clauseTokenClass : clauseTokenClasses) {
				String clauseName = clauseTokenClass.getSimpleName().substring(1).toUpperCase();
				addInvalid(binOpTokenClass,clauseTokenClass,"Argument to binary operator is missing.");
				addInvalid(clauseTokenClass,binOpTokenClass,"Argument to binary operator is missing.");
			}
			addInvalid(binOpTokenClass, TEnd.class,   "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TElse.class,  "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TElsif.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TThen.class,  "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TRightPar.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TRightBrace.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TRightBracket.class, "Argument to binary operator is missing.");
			addInvalid(binOpTokenClass, TSemicolon.class,"Argument to binary operator is missing.");
			addInvalid(TLeftPar.class, binOpTokenClass,  "Argument to binary operator is missing.");
			addInvalid(TSemicolon.class, binOpTokenClass,"Argument to binary operator is missing.");
			
			// cover cases like:  /*@symbolic */ BINARY_OPERATOR
			addInvalid(TPragmaSymbolic.class, binOpTokenClass,  "A symbolic pragma must be put *before* a set comprehension or lambda.");
		}
		
		// now treat rules for only binary infix expression operators
		binOpTokenClasses = new HashSet<>();
		AddBinExprOperators();
		
		for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
			addInvalid(TPragmaLabel.class, binOpTokenClass,  "A label pragma must be put *before* a predicate, not inside it.");
			addInvalid(binOpTokenClass, TPragmaLabel.class,  "A label pragma must be put before a *predicate*, it cannot be put before expressions.");
			addInvalid(binOpTokenClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier.");
			
			String opName = binOpTokenClass.getSimpleName().substring(1).toUpperCase();
			addInvalid(binOpTokenClass, TEnd.class, "Missing argument for binary operator " + opName + ".");
		}
		
		// override rules above with a more specific error message
		addInvalid(TConjunction.class, TConjunction.class, "Probably one & too many.");
		addInvalid(TLogicalOr.class, TLogicalOr.class, "Probably one 'or' too many.");
		addInvalid(TLess.class, TGreater.class, "<> is not allowed anymore, use [] for the empty sequence."); // this is rule is only of limited usefulness, until we remove the empty_sequence token
		
		
		// a few rules for keywords which are unary functions and require an opening parenthesis afterwards
		// real operators floor(.), ceiling(.), real(.)
		funOpKeywordTokenClasses.add(TConvertIntFloor.class);
		funOpKeywordTokenClasses.add(TConvertIntCeiling.class);
		funOpKeywordTokenClasses.add(TConvertReal.class);
		// regular operators
		funOpKeywordTokenClasses.add(TBoolCast.class);
		funOpKeywordTokenClasses.add(TCard.class);
		funOpKeywordTokenClasses.add(TIterate.class);
		funOpKeywordTokenClasses.add(TClosure.class);
		funOpKeywordTokenClasses.add(TClosure1.class);
		funOpKeywordTokenClasses.add(TRel.class);
		funOpKeywordTokenClasses.add(TFnc.class);
		funOpKeywordTokenClasses.add(TPerm.class);
		funOpKeywordTokenClasses.add(TMin.class);
		funOpKeywordTokenClasses.add(TMax.class);
		funOpKeywordTokenClasses.add(TDom.class);
		funOpKeywordTokenClasses.add(TRan.class);
		funOpKeywordTokenClasses.add(TId.class); // ( {2} ; id) not allowed
		// TO DO:  prj1, prj2
		// Record operators:
		funOpKeywordTokenClasses.add(TStruct.class);
		funOpKeywordTokenClasses.add(TRec.class);
		// sequence operators:
		funOpKeywordTokenClasses.add(TSize.class);
		funOpKeywordTokenClasses.add(TFront.class);
		funOpKeywordTokenClasses.add(TFirst.class); //  ( [ [1,2] ] ; first) = [ [1] ] not accepted in Atelier-B
		funOpKeywordTokenClasses.add(TTail.class);
		funOpKeywordTokenClasses.add(TLast.class);
		funOpKeywordTokenClasses.add(TRev.class);  // ( [[1,2]] ; rev)  not accepted in Atelier-B
		funOpKeywordTokenClasses.add(TConc.class);
		// tree operators:
		funOpKeywordTokenClasses.add(TTree.class);
		funOpKeywordTokenClasses.add(TConst.class);
		funOpKeywordTokenClasses.add(TTop.class);
		funOpKeywordTokenClasses.add(TSons.class);
		funOpKeywordTokenClasses.add(TPrefix.class);
		funOpKeywordTokenClasses.add(TPostfix.class);
		funOpKeywordTokenClasses.add(TSizet.class);
		funOpKeywordTokenClasses.add(TMirror.class);
		// tree node operators:
		funOpKeywordTokenClasses.add(TRank.class);
		funOpKeywordTokenClasses.add(TFather.class);
		funOpKeywordTokenClasses.add(TSon.class);
		funOpKeywordTokenClasses.add(TSubtree.class);
		funOpKeywordTokenClasses.add(TArity.class);
		// binary tree operators:
		funOpKeywordTokenClasses.add(TBtree.class);
		funOpKeywordTokenClasses.add(TBin.class);
		funOpKeywordTokenClasses.add(TLeft.class);
		funOpKeywordTokenClasses.add(TRight.class);
		funOpKeywordTokenClasses.add(TInfix.class);
		
		for (Class<? extends Token> funOpClass : funOpKeywordTokenClasses) {
			addInvalid(funOpClass, TPragmaDescription.class, "A description pragma must be put after a predicate or identifier, not a keyword.");
			String opName = funOpClass.getSimpleName().substring(1).toLowerCase(); // TO DO: get real keyword name
			if (funOpClass == TConvertIntFloor.class) {
			   opName = new String("floor");
			} else if( funOpClass == TConvertIntCeiling.class) {
			   opName = new String("ceiling");
			} else if( funOpClass == TConvertReal.class) {
			   opName = new String("real");
			} else if( funOpClass == TBoolCast.class) {
			   opName = new String("bool");
			} 
		    String Errmsg = "This keyword (" + opName + ") must be followed by an opening parenthesis.";
			addInvalid(funOpClass, TRightPar.class, Errmsg);
			addInvalid(funOpClass, TRightBrace.class, Errmsg);
			addInvalid(funOpClass, TRightBracket.class, Errmsg);
			addInvalid(funOpClass, TSemicolon.class, Errmsg);
			addInvalid(funOpClass, TWhere.class, Errmsg);
			addInvalid(funOpClass, TThen.class, Errmsg);
			addInvalid(funOpClass, TElse.class, Errmsg);
			addInvalid(funOpClass, TEnd.class, Errmsg);
			for (Class<? extends Token> binOpTokenClass : binOpTokenClasses) {
			     addInvalid(funOpClass, binOpTokenClass, Errmsg);
			}
			for (Class<? extends Token> clauseTokenClass : clauseTokenClasses) {
				addInvalid(funOpClass,clauseTokenClass, Errmsg);
			}
			
		    String Errmsg2 = "This keyword (" + opName + ") cannot be used as an identifier";
			addInvalid(TAny.class,funOpClass, Errmsg2);
			addInvalid(TConstants.class,funOpClass, Errmsg2);
			addInvalid(TAbstractConstants.class,funOpClass, Errmsg2);
			addInvalid(TConcreteConstants.class,funOpClass, Errmsg2);
			addInvalid(TVariables.class,funOpClass, Errmsg2);
			addInvalid(TAbstractVariables.class,funOpClass, Errmsg2);
			addInvalid(TConcreteVariables.class,funOpClass, Errmsg2);
			addInvalid(TOperations.class,funOpClass, Errmsg2);
		}
		
		// Other rules:
		addInvalid(TLeftPar.class, TRightPar.class, "Parentheses must contain arguments.");
		
		addInvalid(TComma.class, TRightPar.class, "Missing expression after comma.");
		addInvalid(TComma.class, TRightBrace.class, "Missing expression after comma.");
		addInvalid(TComma.class, TRightBracket.class, "Missing expression after comma.");
		addInvalid(TSemicolon.class, TRightPar.class, "Missing expression after semicolon.");
		addInvalid(TSemicolon.class, TRightBrace.class, "Missing expression after semicolon.");
		addInvalid(TSemicolon.class, TRightBracket.class, "Missing expression after semicolon.");
		
		addInvalid(TComma.class, TPragmaDescription.class, "A description pragma must be put *after* a predicate or identifier.");
		addInvalid(TSemicolon.class, TPragmaDescription.class, "A description pragma must be put *after* a predicate or identifier.");
		addInvalid(TPragmaLabel.class, TComma.class,  "A label pragma must be put *before* a predicate.");
		addInvalid(TPragmaLabel.class, TSemicolon.class,  "A label pragma must be put *before* a predicate.");
		
		// invalid literal combinations:
		
		literalTokenClasses = new HashSet<>();
		literalTokenClasses.add(TIntegerLiteral.class);
		literalTokenClasses.add(TStringLiteral.class);
		literalTokenClasses.add(TRealLiteral.class);
		literalTokenClasses.add(THexLiteral.class);
		for (Class<? extends Token> litClass : literalTokenClasses) {
		    addInvalid(TIdentifierLiteral.class, litClass,
		         "Missing operator or separator between identifier and literal.");
		    addInvalid(litClass,TIdentifierLiteral.class,
		         "Missing operator or separator between literal and identifier.");
			for (Class<? extends Token> litClass2 : literalTokenClasses) {
					 addInvalid(litClass, litClass2, "Missing operator or separator between literals.");
			}
		}
		// addInvalid(TIdentifierLiteral.class, TIdentifierLiteral.class,  "Missing operator or separator between identifier and identifier.");
		// we treat ref in languagextension not as keyword but as identifier; hence we cannot add this rule
		// see test de.be4.classicalb.core.parser.languageextension.RefinedOperationTest
		
	}
	
	private static void AddBinExprOperators() {
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
		binOpTokenClasses.add(TMod.class);
		binOpTokenClasses.add(TProduct.class);
		binOpTokenClasses.add(TPowerOf.class);
		binOpTokenClasses.add(TLessEqual.class);
		binOpTokenClasses.add(TGreaterEqual.class);
		binOpTokenClasses.add(TLess.class);  // The parser currently allows <> for empty sequence, hence x = <> or <> = .. are all possible; but we now treat <> as a single token
		binOpTokenClasses.add(TGreater.class);
		binOpTokenClasses.add(TOverwriteRelation.class);
		binOpTokenClasses.add(TInterval.class);
		binOpTokenClasses.add(TConcatSequence.class);
		binOpTokenClasses.add(TMaplet.class);
		binOpTokenClasses.add(TRangeRestriction.class);
		binOpTokenClasses.add(TRangeSubtraction.class);
		binOpTokenClasses.add(TDomainRestriction.class);
		binOpTokenClasses.add(TDomainSubtraction.class);
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

		if(parseOptions == null || !parseOptions.isIgnoreCheckingValidCombinations()) {
			checkForInvalidCombinations(lastTokenClass, tokenClass);
			// System.out.println("Ok: " + lastTokenClass + " -> " + tokenClass);
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
					if(parse_definition) {
						ThrowDefaultLexerException("Invalid combination of symbols: '"+ lastToken.getText().trim() + "' before the end of definition. " + string + "\n", string);
					} else {
						ThrowDefaultLexerException("Invalid combination of symbols: '"+ lastToken.getText().trim() + "' before the end of file. " + string + "\n", string);
					}	
				} else
					ThrowDefaultLexerException("Invalid combination of symbols: '"+ lastToken.getText().trim() + "' and '" + token.getText().trim() + "'. " + string + "\n", string);
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

	private void collectComment() throws LexerException, IOException {
		if (token instanceof EOF) {
			// make sure we don't loose this token, needed for error message
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
				comment.setText(commentBuffer.toString());
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
