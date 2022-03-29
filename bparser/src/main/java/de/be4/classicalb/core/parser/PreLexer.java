package de.be4.classicalb.core.parser;

import java.io.IOException;
import java.io.PushbackReader;

import de.be4.classicalb.core.preparser.lexer.Lexer;
import de.be4.classicalb.core.preparser.lexer.LexerException;
import de.be4.classicalb.core.preparser.node.EOF;
import de.be4.classicalb.core.preparser.node.TBeginNesting;
import de.be4.classicalb.core.preparser.node.TComment;
import de.be4.classicalb.core.preparser.node.TCommentEnd;
import de.be4.classicalb.core.preparser.node.TEndNesting;
import de.be4.classicalb.core.preparser.node.TIdentifierLiteral;
import de.be4.classicalb.core.preparser.node.TLeftPar;
import de.be4.classicalb.core.preparser.node.TMultilineStringEnd;
import de.be4.classicalb.core.preparser.node.TMultilineStringStart;
import de.be4.classicalb.core.preparser.node.TOtherClauseBegin;
import de.be4.classicalb.core.preparser.node.TRhsBody;
import de.be4.classicalb.core.preparser.node.TRightPar;
import de.be4.classicalb.core.preparser.node.TSemicolon;
import de.be4.classicalb.core.preparser.node.Token;
import de.be4.classicalb.core.preparser.node.TSomeValue;
import de.be4.classicalb.core.preparser.node.TSomething;

public class PreLexer extends Lexer {

	private TRhsBody rhsToken = null;
	private StringBuilder rhsBuffer = null;
	private int otherNestingLevel = 0;
	private int parenNestingLevel = 0;

	private State previousState;

	public PreLexer(final PushbackReader in) {
		super(in);
	}
	
	
	protected Token getToken() throws IOException, LexerException {
		try {
			// Please don't put any token processing code here!
			// Use the filter method instead (see below).
			// The only code that needs to be here is for processing the exception,
			// which cannot be done with filter.
			return super.getToken();
		} catch (LexerException e) {
			//System.out.println("Exception: " + e.toString());
			// printState();
			String msg = e.getMessage();
			if (state.equals(State.DEFINITIONS) && msg.length()>3) {
				String last = msg.substring(msg.length() - 3).trim(); // string has at least 3 chars
				if(last.equals("="))
					throw new LexerException(msg + " in DEFINITIONS clause (use == to define a DEFINITION)");
				else
					throw new LexerException(msg + " in DEFINITIONS clause");
			} else {
				throw e;
			}
		}
	}

	@Override
	protected void filter() throws LexerException, IOException {
		checkComment();
		checkMultiLineString();
		optimizeToken();

		if (token != null) {
			collectRhs();
		}
	}
	
	// small debugging utility:
	private void printState() {
		if (state.equals(State.DEFINITIONS_RHS)) System.out.println("DEFINITIONS_RHS");
		if (state.equals(State.COMMENT)) System.out.println("State.COMMENT");
		if (state.equals(State.DEFINITIONS)) System.out.println("DEFINITIONS");
		if (state.equals(State.NO_DEFINITIONS)) System.out.println("NO_DEFINITIONS");
		if (state.equals(State.MULTILINE_STRING_STATE)) System.out.println("MULTILINE_STRING_STATE");
		if (state.equals(State.NORMAL)) System.out.println("NORMAL");
		if (token != null) {
			System.out.println("Token = " + token + " at line = " + token.getLine() + ", col = " + token.getPos());
		}
	}

	private void collectRhs() throws LexerException, IOException {
		if (state.equals(State.DEFINITIONS_RHS)
				|| (previousState != null && previousState.equals(State.DEFINITIONS_RHS))) {
			if (rhsToken == null) {
				// starting a new definition rhs
				rhsToken = new TRhsBody("", -1, -1);
				rhsBuffer = new StringBuilder();
			} else {
				final State nextState = getNextState();

				// end of rhs reached?
				if (nextState != null) {
					// push current token back into reader
					try {
						unread(token);
					} catch (IOException e) {

						throw new IOException("Pushback buffer overflow on Token: " + token.getText());
					}

					// prepare rhs_body token to be the current one
					((Token) rhsToken).setText(rhsBuffer.toString());
					token = rhsToken;
					rhsToken = null;
					rhsBuffer = null;
					state = nextState;
				} else {
					// first token after "==" sets start position
					if (rhsToken.getLine() == -1) {
						rhsToken.setLine(token.getLine());
						rhsToken.setPos(token.getPos());
					}
					rhsBuffer.append(token.getText());
					token = null;
				}
			}
		}
	}

	private State getNextState() {
		if (token instanceof TOtherClauseBegin || token instanceof EOF) {
			return State.NORMAL;
		}

		// check for parenthesis first
		if (token instanceof TLeftPar) {
			parenNestingLevel++;
		} else if (token instanceof TRightPar) {
			parenNestingLevel--;
		}
		// then check other tokens which start/end nestings
		else {
			otherNestingLevel += changeNesting();
		}

		if (otherNestingLevel == 0 && parenNestingLevel == 0 && token instanceof TSemicolon) {
			return State.DEFINITIONS;
		}

		if (otherNestingLevel < 0) {
			otherNestingLevel = 0;
			return State.NORMAL;
		}

		return null;
	}

	private int changeNesting() {
		// default if no nesting is started/ended
		int result = 0;

		// is the token starting a nesting?
		if (token instanceof TBeginNesting) {
			result++;
		}

		// is the token ending a nesting?
		if (token instanceof TEndNesting) {
			result--;
		}

		/*
		 * A token can start and end a nesting at the same time. Example
		 * "(PP=TRUE)". So both calls above are needed.
		 */
		return result;
	}

	private void checkComment() {
		if (token instanceof TComment) {
			previousState = state;
			state = State.COMMENT;
		} else if (token instanceof TCommentEnd) {
			state = previousState;
			previousState = null;
		}
	}
	
	private void checkMultiLineString() {
		if (token instanceof TMultilineStringStart) {
			previousState = state;
			state = State.MULTILINE_STRING_STATE;
		} else if (token instanceof TMultilineStringEnd) {
			state = previousState;
			previousState = null;
		}
	}
	
	private void optimizeToken() {
		if (
			token instanceof TIdentifierLiteral
			|| token instanceof TSemicolon
		) {
			token.setText(token.getText().intern());
		} else if (
			token instanceof TSomeValue
			|| token instanceof TSomething
		) {
			// we do not use isIgnoreUselessTokens from ParsingOptions; only in the main lexer
			token = null;
		}
	}
}
