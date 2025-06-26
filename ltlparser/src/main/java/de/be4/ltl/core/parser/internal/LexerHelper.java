package de.be4.ltl.core.parser.internal;

import de.hhu.stups.sablecc.patch.IToken;

abstract class LexerHelper<TOKEN extends IToken, STATE> {
	private int count;
	private TOKEN externalFormula;
	private StringBuilder text;
	private STATE state, lastState;
	private boolean inQuote;

	abstract protected boolean isInAction(final STATE state);

	abstract protected boolean isOpening(final TOKEN token);

	abstract protected boolean isClosing(final TOKEN token);

	abstract protected boolean correctBalancedParenthesis(int count, TOKEN token);

	abstract protected boolean isInActions(final STATE state);

	abstract protected boolean isOpeningActionArg(final TOKEN token);

	abstract protected boolean isClosingActionArg(final TOKEN token);

	abstract protected boolean isBeginningActionsToken(final TOKEN token);

	abstract protected boolean isArgumentClosing(final TOKEN token);

	abstract protected boolean isArgumentSplittingToken(final TOKEN token);

	abstract protected boolean isQuote(final TOKEN token);

	public LexerHelper(final STATE initialState) {
		this.lastState = initialState;
	}

	public TOKEN filter(STATE newState, TOKEN token) {
		if (isQuote(token)) {
			inQuote = !inQuote;
		}

		state = newState;

		if (isInAction(state)) {
			if (externalFormula == null) {
				initialiseActionToken(token);
				return null;
			} else {
				text.append(token.getText());
				if (isOpening(token) && !inQuote) {
					count++;
				} else if (isClosing(token) && !inQuote) {
					count--;
				}

				if (!correctBalancedParenthesis(count, token)) {
					return token;
				}

				if (count != 0) {
					return null;
				} else {
					state = lastState;
					// TODO This almost duplicates updateTokenText (but not exactly)
					text.deleteCharAt(text.length() - 1);
					externalFormula.setText(text.toString());
					TOKEN tok = externalFormula;
					externalFormula = null;
					return tok;
				}
			}
		} else if (isInActions(state)) {
			// ignore the first token in the arguments' list (this is either
			// 'deadlock(' or 'deterministic(')
			if (isBeginningActionsToken(token)) {
				return token;
			} else {
				if (externalFormula == null) {
					initialiseActionToken(token);
					text.append(token.getText());
					return null;
				} else {
					if (isOpeningActionArg(token)) {
						count++;
					} else if (isClosingActionArg(token)) {
						count--;
					}
					if (!correctBalancedParenthesis(count, token)) {
						return token;
					}
					if ((count == 1 && !isArgumentClosing(token)) || count > 1) {
						text.append(token.getText());
					}
					if (count == 1 && isArgumentSplittingToken(token)) {
						return updateTokenText();
					} else if (count == 0) {
						state = lastState;
						return updateTokenText();
					} else {
						return null;
					}
				}
			}
		} else {
			lastState = state;
			return token;
		}
	}

	public void initialiseActionToken(TOKEN token) {
		this.externalFormula = token;
		this.text = new StringBuilder();
		this.count = 1;
		this.inQuote = false;
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
