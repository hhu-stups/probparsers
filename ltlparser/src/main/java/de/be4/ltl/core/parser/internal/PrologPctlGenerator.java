/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.be4.ltl.core.parser.internal;

import de.be4.ltl.core.pctlparser.analysis.DepthFirstAdapter;
import de.be4.ltl.core.pctlparser.node.ACurrentPctlState;
import de.be4.ltl.core.pctlparser.node.ADeadlockPctlState;
import de.be4.ltl.core.pctlparser.node.ADetOutputPctlState;
import de.be4.ltl.core.pctlparser.node.ADigitExpression;
import de.be4.ltl.core.pctlparser.node.AErrorPctlState;
import de.be4.ltl.core.pctlparser.node.AGoalPctlState;
import de.be4.ltl.core.pctlparser.node.AProbFormulaPctlState;
import de.be4.ltl.core.pctlparser.node.ASinkPctlState;
import de.be4.ltl.core.pctlparser.node.AUnparsedPctlState;
import de.be4.ltl.core.pctlparser.node.Node;
import de.be4.ltl.core.pctlparser.node.Start;
import de.be4.ltl.core.pctlparser.node.Token;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;

public class PrologPctlGenerator extends DepthFirstAdapter {

	private final IPrologTermOutput p;
	private final PrologGeneratorHelper helper;

	public PrologPctlGenerator(final IPrologTermOutput pto,
			final String currentStateID, final ProBParserBase specParser) {
		this.p = pto;
		this.helper = new PrologGeneratorHelper(pto, currentStateID, specParser);
	}

	@Override
	public void defaultOut(final Node node) {
		helper.defaultOut();
	}

	@Override
	public void defaultIn(final Node node) {
		helper.defaultIn(node.getClass());
	}

	@Override
	public void caseAUnparsedPctlState(AUnparsedPctlState node) {
		final Token token = node.getPredicate();
		helper.caseUnparsed(UniversalToken.createToken(token));
	}

	@Override
	public void caseADigitExpression(ADigitExpression node) {
		int n = Integer.parseInt(node.getNumber().getText());
		p.printNumber(n);
	}

	@Override
	public void caseAProbFormulaPctlState(AProbFormulaPctlState node) {
		inAProbFormulaPctlState(node);
		if (node.getOp() != null) {
			node.getOp().apply(this);
		}
		final Token token = node.getProbability();
		helper.caseUnparsedExpression(UniversalToken.createToken(token),false);
		if (node.getCont() != null) {
			node.getCont().apply(this);
		}
		outAProbFormulaPctlState(node);
	}

	@Override
	public void caseASinkPctlState(final ASinkPctlState node) {
		helper.sink();
	}

	@Override
	public void caseAGoalPctlState(final AGoalPctlState node) {
		helper.goal();
	}

	@Override
	public void caseADetOutputPctlState(final ADetOutputPctlState node) {
		helper.detOutput();
	}

	@Override
	public void caseAErrorPctlState(final AErrorPctlState node) {
		helper.stateError();
	}

	@Override
	public void caseADeadlockPctlState(final ADeadlockPctlState node) {
		helper.deadlock();
	}

	@Override
	public void caseACurrentPctlState(final ACurrentPctlState node) {
		helper.current();
	}

	@Override
	public void inStart(final Start node) {
		// Do not call default in Method
	}

	@Override
	public void outStart(final Start node) {
		// Do not call default out Method
	}

}
