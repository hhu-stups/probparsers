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
import de.be4.classicalb.core.preparser.node.TLeftPar;
import de.be4.classicalb.core.preparser.node.TMultilineStringEnd;
import de.be4.classicalb.core.preparser.node.TMultilineStringStart;
import de.be4.classicalb.core.preparser.node.TMultilineTemplateEnd;
import de.be4.classicalb.core.preparser.node.TMultilineTemplateStart;
import de.be4.classicalb.core.preparser.node.TOtherClauseBegin;
import de.be4.classicalb.core.preparser.node.TRhsBody;
import de.be4.classicalb.core.preparser.node.TRightPar;
import de.be4.classicalb.core.preparser.node.TSemicolon;
import de.be4.classicalb.core.preparser.node.Token;

public class PreLexer extends Lexer {

	private TRhsBody rhsToken = null;
	private StringBuilder rhsBuffer = null;
	private int otherNestingLevel = 0;
	private int parenNestingLevel = 0;

	private State previousState;

	public PreLexer(final PushbackReader in) {
		super(in);
	}

	public void setPosition(final int line, final int column) {
		this.line = line - 1;
		this.pos = column - 1;
	}
	
	@Override
	protected Token getToken() throws IOException, LexerException {
		try {
			// Please don't put any token processing code here!
			// Use the filter method instead (see below).
			// The only code that needs to be here is for processing the exception,
			// which cannot be done with filter.
			return super.getToken();
		} catch (LexerException e) {
			// System.out.println("Exception: " + e.toString());
			// printState();
			String msg = e.getRealMsg();
			if (state.equals(State.DEFINITIONS) && msg.length()>3) {
				String last = msg.substring(msg.length() - 3).trim(); // string has at least 3 chars
				if(last.equals("="))
					throw new LexerException(e.getLine(), e.getPos(), msg + " in DEFINITIONS clause (use == to define a DEFINITION)", e);
				else
					throw new LexerException(e.getLine(), e.getPos(), msg + " in DEFINITIONS clause", e);
			} else {
				throw e;
			}
		}
	}

	@Override
	protected void filter() throws LexerException, IOException {
		//printState();
		checkComment();
		checkMultiLineString();

		if (token != null) {
			collectRhs();
			// System.out.println("+ TOKEN KEPT");
		}
	}
	
	// small debugging utility:
	private void printState() {
		System.out.println(state);
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
						throw new IOException("Pushback buffer overflow on Token: " + token.getText(), e);
					}

					// prepare rhs_body token to be the current one
					rhsToken.setText(rhsBuffer.toString());
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
		// Recognize clause beginning tokens only outside nesting,
		// because e. g. INVARIANT may also appear in a WHILE loop,
		// where it shouldn't signal the end of the DEFINITIONS block.
		if ((otherNestingLevel == 0 && token instanceof TOtherClauseBegin) || token instanceof EOF) {
			return State.NORMAL;
		}

		// Update nesting levels when there is a parenthesis or a keyword that begins/ends a block.
		if (token instanceof TLeftPar) {
			parenNestingLevel++;
		} else if (token instanceof TRightPar) {
			parenNestingLevel--;
		} else if (token instanceof TBeginNesting) {
			otherNestingLevel++;
		} else if (token instanceof TEndNesting) {
			otherNestingLevel--;
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

	private void checkComment() {
		// Switch to special COMMENT state for block comments
		// and switch back to the previous state after the comment ends.
		// This special logic is necessary because the previous state may be NORMAL, DEFINITIONS, or DEFINITIONS_RHS.
		if (token instanceof TComment) {
			previousState = state;
			state = State.BLOCK_COMMENT;
		} else if (token instanceof TCommentEnd) {
			state = previousState;
			previousState = null;
		}
	}
	
	private void checkMultiLineString() throws LexerException {
		// Switch to special states for multiline strings
		// and switch back to the previous state after the string ends.
		// This special logic is necessary because the previous state may be NORMAL or DEFINITIONS_RHS.
		if (token instanceof TMultilineStringStart) {
			previousState = state;
			state = State.MULTILINE_STRING;
		} else if (token instanceof TMultilineTemplateStart) {
			previousState = state;
			state = State.MULTILINE_TEMPLATE;
		} else if (token instanceof TMultilineStringEnd || token instanceof TMultilineTemplateEnd) {
			if (previousState == null) {
				throw new LexerException("Encountered multiline string end token without corresponding start token");
			}
			state = previousState;
			previousState = null;
		}
	}
}
