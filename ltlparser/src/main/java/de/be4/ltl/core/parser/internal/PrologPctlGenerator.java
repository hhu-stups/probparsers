/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.be4.ltl.core.parser.internal;

import java.util.Locale;

import de.be4.ltl.core.pctlparser.analysis.DepthFirstAdapter;
import de.be4.ltl.core.pctlparser.node.ACurrentPctlState;
import de.be4.ltl.core.pctlparser.node.ADeadlockPctlState;
import de.be4.ltl.core.pctlparser.node.ASinkPctlState;
import de.be4.ltl.core.pctlparser.node.AGoalPctlState;
import de.be4.ltl.core.pctlparser.node.ADetOutputPctlState;
import de.be4.ltl.core.pctlparser.node.AErrorPctlState;
import de.be4.ltl.core.pctlparser.node.AUnparsedPctlState;
import de.be4.ltl.core.pctlparser.node.AFormulaEqualPctlState;
import de.be4.ltl.core.pctlparser.node.AFormulaGreaterPctlState;
import de.be4.ltl.core.pctlparser.node.AFormulaLessPctlState;
import de.be4.ltl.core.pctlparser.node.AFormulaStrictlyGreaterPctlState;
import de.be4.ltl.core.pctlparser.node.AFormulaStrictlyLessPctlState;

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


	/*
	Unfortunately, 'Pctlstate' has 6 more letters than 'ctl' or 'ltl'. Hence, the method 
	 in PrologGeneratorHelper couldn't be used and the method needed to be rewritten again to suit
	 the name of the langage 
	 */
	@Override
	public void defaultIn(final Node node) {
		StringBuilder sb = new StringBuilder(node.getClass().getSimpleName());
		sb.setLength(sb.length() - 9);
		sb.deleteCharAt(0);
		String term = sb.toString().toLowerCase(Locale.ENGLISH);
		p.openTerm(term);
	}

	@Override
	public void caseAUnparsedPctlState(AUnparsedPctlState node) {
		final Token token = node.getPredicate();
		helper.caseUnparsed(UniversalToken.createToken(token));
	}

	@Override
	public void caseAFormulaEqualPctlState(AFormulaEqualPctlState node) {
		inAFormulaEqualPctlState(node);
		final Token token = node.getProbability();
		helper.caseUnparsed(UniversalToken.createToken(token));
		if(node.getCont() != null)
        {
            node.getCont().apply(this);
        }
        outAFormulaEqualPctlState(node);
	}

	@Override
	public void caseAFormulaGreaterPctlState(AFormulaGreaterPctlState node) {
		inAFormulaGreaterPctlState(node);
		final Token token = node.getProbability();
		helper.caseUnparsed(UniversalToken.createToken(token));
		if(node.getCont() != null)
        {
            node.getCont().apply(this);
        }
        outAFormulaGreaterPctlState(node);
	}

	@Override
	public void caseAFormulaLessPctlState(AFormulaLessPctlState node) {
		inAFormulaLessPctlState(node);
		final Token token = node.getProbability();
		helper.caseUnparsed(UniversalToken.createToken(token));
		if(node.getCont() != null)
        {
            node.getCont().apply(this);
        }
        outAFormulaLessPctlState(node);
	}

	@Override
	public void caseAFormulaStrictlyGreaterPctlState(AFormulaStrictlyGreaterPctlState node) {
		inAFormulaStrictlyGreaterPctlState(node);
		final Token token = node.getProbability();
		helper.caseUnparsed(UniversalToken.createToken(token));
		if(node.getCont() != null)
        {
            node.getCont().apply(this);
        }
        outAFormulaStrictlyGreaterPctlState(node);
	}

	@Override
	public void caseAFormulaStrictlyLessPctlState(AFormulaStrictlyLessPctlState node) {
		inAFormulaStrictlyLessPctlState(node);
		final Token token = node.getProbability();
		helper.caseUnparsed(UniversalToken.createToken(token));
		if(node.getCont() != null)
        {
            node.getCont().apply(this);
        }
        outAFormulaStrictlyLessPctlState(node);
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
		helper.det_output();
	}

	@Override
	public void caseAErrorPctlState(final AErrorPctlState node) {
		helper.state_error();
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
