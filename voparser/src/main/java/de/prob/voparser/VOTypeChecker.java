package de.prob.voparser;

import de.prob.voparser.analysis.DepthFirstAdapter;
import de.prob.voparser.node.ASequentialVo;
import de.prob.voparser.node.Start;

public class VOTypeChecker extends DepthFirstAdapter {

	private final VOParser voParser;

	private boolean error;

	public VOTypeChecker(VOParser voParser) {
		this.voParser = voParser;
		this.error = false;
	}

	public void typeCheck(Start start) throws VOParseException {
		start.apply(this);
		if(error) {
			throw new VOParseException("Type error in VO", VOParseException.ErrorType.TYPECHECKING);
		}
	}

	@Override
	public void caseASequentialVo(ASequentialVo node) {
		super.caseASequentialVo(node);
	}
}
