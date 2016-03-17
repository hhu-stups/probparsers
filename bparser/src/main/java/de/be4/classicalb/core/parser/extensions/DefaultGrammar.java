package de.be4.classicalb.core.parser.extensions;

import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.Token;

public class DefaultGrammar implements IGrammar {

	@Override
	public boolean containsAlternativeDefinitionForToken(Token token) {
		return false;
	}

	@Override
	public Token createNewToken(Token token) {
		return null;
	}

	@Override
	public void applyAstTransformation(Start start) {
	}

}