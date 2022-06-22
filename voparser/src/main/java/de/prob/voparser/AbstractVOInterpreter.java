package de.prob.voparser;

import de.prob.voparser.node.AAndVo;
import de.prob.voparser.node.AEquivalentVo;
import de.prob.voparser.node.AIdentifierVo;
import de.prob.voparser.node.AImpliesVo;
import de.prob.voparser.node.ANotVo;
import de.prob.voparser.node.AOrVo;
import de.prob.voparser.node.ASequentialVo;
import de.prob.voparser.node.PVo;
import de.prob.voparser.node.Start;

public abstract class AbstractVOInterpreter {

	protected final VOParser voParser;

	public AbstractVOInterpreter() {
		this.voParser = new VOParser();
	}

	public void registerTask(String id, VTType type) {
		voParser.registerTask(id, type);
	}

	public void deregisterTask(String id) {
		voParser.deregisterTask(id);
	}

	public void interpretVOExpression(String VO) throws VOParseException {
		Start ast = voParser.parseFormula(VO);
		voParser.semanticCheck(ast);
	}

	public void interpretVOExpression(PVo VO) {

	}

	public void interpretAtomicExpression(AIdentifierVo VO) {

	}

	public void interpretNotExpression(ANotVo VO) {

	}

	public void interpretAndExpression(AAndVo VO) {

	}

	public void interpretOrExpression(AOrVo VO) {

	}

	public void interpretImpliesExpression(AImpliesVo VO) {

	}

	public void interpretEquivalentExpression(AEquivalentVo VO) {

	}

	public void interpretSequentialExpression(ASequentialVo VO) {

	}

}
