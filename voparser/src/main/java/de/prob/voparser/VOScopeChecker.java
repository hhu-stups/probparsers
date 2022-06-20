package de.prob.voparser;

import de.prob.voparser.analysis.DepthFirstAdapter;
import de.prob.voparser.node.Start;
import de.prob.voparser.node.TIdentifierLiteral;


public class VOScopeChecker extends DepthFirstAdapter {

	private final VOParser voParser;

	private boolean error;

	public VOScopeChecker(VOParser voParser) {
		this.voParser = voParser;
		this.error = false;
	}

	public void scopeCheck(Start start) throws VOParseException {
		start.apply(this);
		if(error) {
			throw new VOParseException("Scope error in VO", VOParseException.ErrorType.SCOPING);
		}
	}

	@Override
	public void caseTIdentifierLiteral(TIdentifierLiteral node) {
		if (!voParser.getTasks().containsKey(node.getText())) {
			error = true;
		}
	}
}
