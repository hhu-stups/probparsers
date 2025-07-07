package de.be4.ltl.core.parser.internal;

import de.hhu.stups.sablecc.patch.IToken;

abstract class ProbabilisticLexerHelper<TOKEN extends IToken, STATE> {
	private TOKEN externalFormula;
	private StringBuilder text;
	private STATE state, lastState;
	private boolean inQuote;

	abstract protected boolean isOpening(final TOKEN token);

	abstract protected boolean isClosing(final TOKEN token);

	abstract protected boolean correctBalancedParenthesis(int count, TOKEN token);

	abstract protected boolean isArgumentClosing(final TOKEN token);

	abstract protected boolean isArgumentSplittingToken(final TOKEN token);

	abstract protected boolean isQuote(final TOKEN token);

	public ProbabilisticLexerHelper(final STATE initialState) {
		this.lastState = initialState;
	}

	public TOKEN filter(STATE newState, TOKEN token) {
		if (isQuote(token)) {
			inQuote = !inQuote;
		}

		state = newState;
		lastState = state;
		return token;
		
	}

	public TOKEN updateTokenText() {
		this.externalFormula.setText(this.text.toString().trim());
		TOKEN tok = externalFormula;
		this.externalFormula = null;
		return tok;
	}

	public TOKEN getIdentifier(TOKEN token, TOKEN ident) {
		String str = token.getText();
		String identifier = str.substring(1, str.length() - 1).trim();
		ident.setText(identifier);
		return ident;
	}

	public STATE getState() {
		return state;
	}

}
