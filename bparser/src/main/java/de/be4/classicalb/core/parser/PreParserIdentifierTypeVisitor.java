package de.be4.classicalb.core.parser;

import java.util.Set;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;

public class PreParserIdentifierTypeVisitor extends DepthFirstAdapter {

	private final Set<String> untypedDefinitions;
	private boolean untypedDefinitionUsed = false;

	public PreParserIdentifierTypeVisitor(Set<String> untypedDefinitions) {
		this.untypedDefinitions = untypedDefinitions;
	}

	@Override
	public void inAIdentifierExpression(AIdentifierExpression node) {
		super.inAIdentifierExpression(node);
		if (untypedDefinitions.contains(node.getIdentifier().get(0).getText()))
		// the definition uses another definition which is not yet typed
			untypedDefinitionUsed = true;
	}

	public boolean isUntypedDefinitionUsed() {
		return untypedDefinitionUsed;
	}

}
