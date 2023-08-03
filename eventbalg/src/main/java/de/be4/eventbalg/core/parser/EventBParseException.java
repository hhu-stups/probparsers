package de.be4.eventbalg.core.parser;

import de.be4.eventbalg.core.parser.node.Node;
import de.be4.eventbalg.core.parser.node.Token;

@SuppressWarnings("serial")
public class EventBParseException extends RuntimeException {
	private final Token token;
	@Deprecated
	private final de.hhu.stups.sablecc.patch.SourcecodeRange range;

	/**
	 * @deprecated Use {@link #EventBParseException(Token, String)} instead.
	 *     Source positions are now always stored in the token itself.
	 */
	@Deprecated
	public EventBParseException(final Token token, final de.hhu.stups.sablecc.patch.SourcecodeRange range, final String message) {
		super(message);
		this.token = token;
		this.range = range;
	}

	public EventBParseException(final Token token, final String message) {
		super(message);
		this.token = token;
		this.range = null;
	}

	/**
	 * {@link Token} which caused the parse exception. May be <code>null</code>
	 * if no special token was affected.
	 * 
	 * @return the token which caused the parse exception. May be <code>null</code>.
	 */
	public Token getToken() {
		return token;
	}

	/**
	 * Returns the {@link de.hhu.stups.sablecc.patch.SourcecodeRange} which is causing this exception. Will
	 * be <code>null</code> in case of a real lexing or parsing exception cause
	 * source code ranges for the {@link Node}s of the AST have not yet been
	 * evaluated then.
	 * 
	 * @return the source code range of the node. May be <code>null</code>.
	 * @deprecated Use the position information from {@link #getToken()} instead.
	 */
	@Deprecated
	public de.hhu.stups.sablecc.patch.SourcecodeRange getRange() {
		return range;
	}
}
