package de.prob.voparser;

import de.prob.voparser.analysis.DepthFirstAdapter;
import de.prob.voparser.node.AIdentifierVo;
import de.prob.voparser.node.Start;


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
	public void caseAIdentifierVo(AIdentifierVo node) {
		if (!voParser.getTasks().containsKey(node.getIdentifierLiteral().getText())) {
			error = true;
		}
	}

}
