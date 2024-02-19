package de.be4.classicalb.core.parser.grammars;

import de.be4.classicalb.core.parser.node.Token;

public interface IGrammar {

	boolean containsAlternativeDefinitionForToken(Token token);

	Token createNewToken(Token token);

}
