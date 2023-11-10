package de.be4.eventb.core.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.be4.eventb.core.parser.lexer.Lexer;
import de.be4.eventb.core.parser.lexer.LexerException;
import de.be4.eventb.core.parser.node.*;

public class EventBLexer extends Lexer {

	private boolean debugOutput = false;

	private TMultiCommentStart commentStart = null;
	private StringBuilder commentBuffer = null;
	private TFormula string = null;
	private List<Token> stringBuffer;

	private static final String[] clauseErrorMessages = {
			"'machine' is only allowed at the beginning of a file",
			"Variable declarations are only allowed before invariant declarations",
			"Invariant declarations are only allowed after variables and before the variant",
			"The variant is only allowed after invariants and before events",
			"The events clause is only allowed at the end",
			"'context' is only allowed at the beginning of a file",
			"Set declarations are only allowed before the constants declarations",
			"Constants declarations are only allowed after sets and before axioms",
			"The axioms clause is only allowed at the end" };
	private static final List<Class<? extends Token>> clausesOrder = Collections.unmodifiableList(Arrays.asList(
		TMachine.class,
		TVariables.class,
		TInvariants.class,
		TVariant.class,
		TEvents.class,
		TContext.class,
		TSets.class,
		TConstants.class,
		TAxioms.class
	));
	private int lastClauseIndex;

	private boolean inEvent;
	private static final String[] eventClauseErrorMessages = {
			"Parameter declarations (any) are only allowed at the beginning of an event",
			"Guards (where) are only allowed after parameters and before witnesses",
			"Witnesses (with) are only allowed after guards and before actions",
			"Actions (then) are only allowed at the end of an event" };
	private static final List<Class<? extends Token>> eventClausesOrder = Collections.unmodifiableList(Arrays.asList(
		TAny.class,
		TWhere.class,
		TWith.class,
		TThen.class
	));
	private int lastEventClauseIndex;

	public EventBLexer(final PushbackReader in) {
		super(in);

		lastClauseIndex = 0;
		lastEventClauseIndex = 0;
		inEvent = false;
	}

	@Override
	protected void filter() throws IOException, EventBLexerException {
		checkClauseOrders();
		collectString();
		collectMultiLineComment();

		if (token != null) {
			if (debugOutput && !(token instanceof TWhiteSpace)
					&& !(token instanceof EOF)) {
				System.out.print(token.getClass().getSimpleName() + "('"
						+ token.getText() + "') ");
			}
		}
	}

	private void checkClauseOrders() throws EventBLexerException {
		if (token != null) {
			// entering event?
			if (!inEvent && token instanceof TEvent) {
				inEvent = true;
				lastEventClauseIndex = 0;
				return;
			}
			// leaving event?
			else if (inEvent && token instanceof TEnd) {
				inEvent = false;
				return;
			}

			// check machine/context clauses' order
			if (!inEvent && clausesOrder.contains(token.getClass())) {
				final int nextIndex = clausesOrder.indexOf(token.getClass());

				if (nextIndex < lastClauseIndex) {
					throwClausesOrderException(clauseErrorMessages[nextIndex]);
				}

				lastClauseIndex = nextIndex;
				return;
			}

			// check order within an event
			if (inEvent && eventClausesOrder.contains(token.getClass())) {
				final int nextIndex = eventClausesOrder.indexOf(token.getClass());

				if (nextIndex < lastEventClauseIndex) {
					throwClausesOrderException(eventClauseErrorMessages[nextIndex]);
				}

				lastEventClauseIndex = nextIndex;
			}
		}
	}

	private void throwClausesOrderException(final String message)
			throws EventBLexerException {
		throw new EventBLexerException(token, message, token.getText(), token.getLine(), token.getPos());
	}

	private void collectMultiLineComment() throws EventBLexerException {
		if (state.equals(State.MULTI_COMMENT)) {
			if (token instanceof EOF) {
				// make sure we don't loose this token, needed for error message
				// tokenList.add(token);
				throw new EventBLexerException(token, "Comment not closed");
			}

			/*
			 * Starting a new multiline comment, so first token is
			 * TMultiCommentStart
			 */
			if (commentStart == null) {
				commentStart = (TMultiCommentStart) token;
				commentBuffer = new StringBuilder();
				token = null;
			} else {
				// end of comment reached?
				if (token instanceof TMultiCommentEnd) {
					token = new TComment(commentBuffer.toString(),
							commentStart.getLine(), commentStart.getPos());
					commentStart = null;
					commentBuffer = null;
					state = State.NORMAL;
				} else {
					commentBuffer.append(token.getText());
					token = null;
				}
			}
		}
	}

	private void collectString() throws EventBLexerException {
		if (!(token instanceof EOF) && state.equals(State.FORMULA)) {
			// we are entering state STRING
			if (string == null) {
				beginStringToken();
			}
			// we have already been in state STRING
			else {
				stringBuffer.add(token);
				token = null;
			}
		}
		// we just left state STRING
		else if (string != null) {
			try {
				endStringToken();
			} catch (LexerException e) {
				throw new EventBLexerException(token, e.getMessage());
			}
		}
	}

	private void endStringToken() throws LexerException {
		try {
			/*
			 * Push back current token. We are going to insert our own string
			 * token into the token stream just before the current token. Reset
			 * state so that unread token can be recognized again in next lexer
			 * step.
			 */
			unread(token);
			state = State.NORMAL;

			// create text for string token
			string.setText(createString());
		} catch (final IOException e) {
			throw new LexerException("IOException occured: "
					+ e.getLocalizedMessage());
		}

		token = string;
		string = null;
		stringBuffer = null;
	}

	private void beginStringToken() throws EventBLexerException {
		// expected before actual string begins
		if (token instanceof TColon || token instanceof TWhiteSpace
				|| token instanceof TVariant) {
			return;
		}

		if (!(token instanceof TFormula)) {
			// make sure we don't loose this token, needed for error
			// message
			// tokenList.add(token);
			throw new EventBLexerException(token, "Unexpected token '"
					+ token.getClass().getSimpleName().substring(1) + "'");
		}

		string = (TFormula) token;
		stringBuffer = new ArrayList<>();
		stringBuffer.add(token);
		token = null;
	}

	private String createString() throws IOException {
		// push back not wanted whitespaces at end
		int endPos = stringBuffer.size() - 1;

		while (stringBuffer.get(endPos) instanceof TWhiteSpace) {
			unread(stringBuffer.get(endPos));
			endPos--;
		}

		// create actual string text
		final StringBuilder builder = new StringBuilder();

		for (int i = 0; i <= endPos; i++) {
			builder.append(stringBuffer.get(i).getText());
		}

		return builder.toString();
	}

	public void setDebugOutput(final boolean debugOutput) {
		this.debugOutput = debugOutput;
	}
}
