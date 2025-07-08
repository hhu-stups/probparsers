/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.be4.ltl.core.parser.internal;

import java.io.PushbackReader;

import de.be4.ltl.core.pctlparser.lexer.Lexer;
import de.be4.ltl.core.pctlparser.node.EOF;
import de.be4.ltl.core.pctlparser.node.TAtomicPropositionBegin;
import de.be4.ltl.core.pctlparser.node.TAtomicPropositionEnd;
import de.be4.ltl.core.pctlparser.node.Token;

public class PctlLexer extends Lexer {

	private final PctlLexerHelper helper = new PctlLexerHelper();

	private final PctlLexerHelper2 helper2 = new PctlLexerHelper2();

	public PctlLexer(final PushbackReader in) {
		super(in);
	}

	@Override
	protected void filter() {
		token = helper.filter(state, token);
		state = helper.getState();
		System.out.println("1"+state.toString());
		if ((state.equals(State.PCTL_STATE)) && (token.toString().equals(null))){
			token = helper2.filter(state, token);
			state = helper2.getState();
			System.out.println("2"+state.toString());
			System.out.println("2"+token.toString());
		}
	}

	public static class PctlLexerHelper extends LexerHelper<Token, State> {

		public PctlLexerHelper() {
			super(State.PCTL_STATE);
		}

		@Override
		protected boolean isOpening(final Token token) {
			return token instanceof TAtomicPropositionBegin
					;
		}

		@Override
		protected boolean isClosing(final Token token) {
			return token instanceof TAtomicPropositionEnd
					;
		}

		@Override
		protected boolean isInActionOrAtomic(State state) {
			return state.equals(State.ATOMIC);
		}

		@Override
		protected boolean correctBalancedParenthesis(int count, Token token) {
			return !(token instanceof EOF) || count == 0;
		}

		@Override
		protected boolean isOpeningActionArg(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isClosingActionArg(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isInActions(State state) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isBeginningActionsToken(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isArgumentClosing(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isArgumentSplittingToken(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isQuote(Token token) {
			return token.getText().equals("\"");
		}

	}
	public static class PctlLexerHelper2 extends LexerHelper<Token, State> {

		public PctlLexerHelper2() {
			super(State.PCTL_PATH);
		}

		@Override
		protected boolean isOpening(final Token token) {
			return token instanceof TAtomicPropositionBegin
					;
		}

		@Override
		protected boolean isClosing(final Token token) {
			return token instanceof TAtomicPropositionEnd
					;
		}

		@Override
		protected boolean isInActionOrAtomic(State state) {
			return state.equals(State.ATOMIC);
		}

		@Override
		protected boolean correctBalancedParenthesis(int count, Token token) {
			return !(token instanceof EOF) || count == 0;
		}

		@Override
		protected boolean isOpeningActionArg(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isClosingActionArg(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isInActions(State state) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isBeginningActionsToken(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isArgumentClosing(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isArgumentSplittingToken(Token token) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		protected boolean isQuote(Token token) {
			return token.getText().equals("\"");
		}

	}
}
