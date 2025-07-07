package de.be4.ltl.core.parser.internal;

import java.util.LinkedList;
import java.util.Locale;

import de.be4.ltl.core.parser.LtlParseException;
import de.be4.ltl.core.parser.node.ACtrlLtl;
import de.be4.ltl.core.parser.node.ADetLtl;
import de.be4.ltl.core.parser.node.ADlkLtl;
import de.be4.ltl.core.parser.node.AExistsLtl;
import de.be4.ltl.core.parser.node.AForallLtl;
import de.be4.ltl.core.parser.node.AUnchangedLtl;
import de.be4.ltl.core.parser.node.AChangedLtl;
import de.be4.ltl.core.parser.node.ADecreasingLtl;
import de.be4.ltl.core.parser.node.AIncreasingLtl;
import de.be4.ltl.core.parser.node.ABeforeAfterLtl;
import de.be4.ltl.core.parser.node.PActions;
import de.be4.ltl.core.parser.node.PLtl;
import de.prob.parserbase.ProBParseException;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.IPrologTermOutput;

final class PrologPctlGeneratorHelper {
	private final IPrologTermOutput pto;
	private final String currentStateID;
	private final ProBParserBase specParser;

	public PrologPctlGeneratorHelper(final IPrologTermOutput pto,
			final String currentStateID, final ProBParserBase specParser) {
		super();
		this.pto = pto;
		this.currentStateID = currentStateID;
		this.specParser = specParser;
	}

	public void defaultInPctl(Class<?> clazz) {
		StringBuilder sb = new StringBuilder(clazz.getSimpleName());
		sb.setLength(sb.length() - 9);
		sb.deleteCharAt(0);
		String term = sb.toString().toLowerCase(Locale.ENGLISH);
		pto.openTerm(term);
	}

	public void defaultOutPctl() {
		pto.closeTerm();
	}

	public void caseUnparsedPctl(final UniversalToken token) {
		pto.openTerm("ap");
		try {
			specParser.parsePredicate(pto, token.getText(), true, token.getLine(), token.getColumn());
		} catch (ProBParseException e) {
			throw createAdapterException(token, e);
		} catch (UnsupportedOperationException e) {
			// if the formalism does not support predicates
			throw createAdapterException(token, e);
		}
		pto.closeTerm();
	}

	public void caseUnparsedPredicatePctl(final UniversalToken token) {
		// pto.openTerm("ap");
		try {
			specParser.parsePredicate(pto, token.getText(), true, token.getLine(), token.getColumn());
		} catch (ProBParseException e) {
			throw createAdapterException(token, e);
		} catch (UnsupportedOperationException e) {
			// if the formalism does not support predicates
			throw createAdapterException(token, e);
		}
		//pto.closeTerm();
	}

	public void caseUnparsedExpressionPctl(final UniversalToken token) {
		//pto.openTerm("ae"); // from the context it is clear in the AST that we expect an expression
		try {
			specParser.parseExpression(pto, token.getText(), true, token.getLine(), token.getColumn());
		} catch (ProBParseException e) {
			throw createAdapterException(token, e);
		} catch (UnsupportedOperationException e) {
			// if the formalism does not support expressions
			throw createAdapterException(token, e);
		}
		//pto.closeTerm();
	}

	public void enabledPctl(final UniversalToken token) {
		pto.openTerm("ap");
		pto.openTerm("enabled");
		parseTransitionPredicatePctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	
	public void availablePctl(final UniversalToken token) {
		pto.openTerm("ap"); // atomic property
		pto.openTerm("available");
		parseTransitionPredicatePctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	
	public void strong_fairPctl(UniversalToken token) {
		pto.openTerm("ap");
		pto.openTerm("strong_fair");
		parseTransitionPredicatePctl(token);
		pto.closeTerm();
		pto.closeTerm();		
	}

	public void weak_fairPctl(UniversalToken token) {
		pto.openTerm("ap");
		pto.openTerm("weak_fair");
		parseTransitionPredicatePctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}

	public void parseTransitionPredicatePctl(final UniversalToken token) {
		try {
			specParser.parseTransitionPredicate(pto, token.getText(), true, token.getLine(), token.getColumn());
		} catch (ProBParseException | UnsupportedOperationException e) {
			throw createAdapterException(token, e);
		}
	}

	public void sinkPctl() {
		pto.openTerm("ap");
		pto.printAtom("sink");
		pto.closeTerm();
	}

	public void goalPctl() {
		pto.openTerm("ap");
		pto.printAtom("goal");
		pto.closeTerm();
	}

	public void det_outputPctl() {
		pto.openTerm("ap");
		pto.printAtom("det_output");
		pto.closeTerm();
	}

	public void state_errorPctl() {
		pto.openTerm("ap");
		pto.printAtom("state_error");
		pto.closeTerm();
	}

	public void deadlockPctl() {
		pto.openTerm("ap");
		pto.printAtom("deadlock");
		pto.closeTerm();
	}

	public void currentPctl() {
		pto.openTerm("ap");
		if (currentStateID != null) {
			pto.openTerm("stateid");
			pto.printAtomOrNumber(currentStateID);
			pto.closeTerm();
		} else {
			pto.printAtom("current");
		}
		pto.closeTerm();
	}

	private LtlAdapterException createAdapterException(
			final UniversalToken token, final Throwable orig) {
		final LtlParseException ex = new LtlParseException(token, orig);
		return new LtlAdapterException(ex);
	}

	public void existsTermPctl(AExistsLtl node, PrologGenerator gen) {
		
		pto.openTerm("exists");
		String identifier = node.getExistsIdentifier().getText();
		pto.printAtom(identifier);
		
		final UniversalToken token = UniversalToken.createToken(node.getPredicate());
		this.caseUnparsedPctl(token);

		node.getLtl().apply(gen);

		pto.closeTerm();
		
	}

	public void forallTermPctl(AForallLtl node, PrologGenerator gen) {
		
		pto.openTerm("forall");
		String identifier = node.getForallIdentifier().getText();
		pto.printAtom(identifier);
		
		final UniversalToken token = UniversalToken.createToken(node.getPredicate());
		this.caseUnparsedPctl(token);

		node.getLtl().apply(gen);

		pto.closeTerm();
		
	}

	public void unchangedTermPctl(AUnchangedLtl node, PrologGenerator gen) {
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("eq");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpressionPctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void changedTermPctl(AChangedLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("neq");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpressionPctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void decreasingTermPctl(ADecreasingLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("gt");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpressionPctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void increasingTermPctl(AIncreasingLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("lt");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpressionPctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void before_afterTermPctl(ABeforeAfterLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("before_after");
		final UniversalToken token = UniversalToken.createToken(node.getPredicate());
		this.caseUnparsedPredicatePctl(token);
		pto.closeTerm();
		pto.closeTerm();
	}

	public void and_fair1Pctl(PLtl left_node, PLtl right_node, PrologGenerator gen) {
		
		pto.openTerm("and");
		
		pto.openTerm("strongassumptions");
		left_node.apply(gen);
		pto.closeTerm();
				
		pto.openTerm("weakassumptions");
		right_node.apply(gen);
		pto.closeTerm();
		
		pto.closeTerm();
	}

	public void and_fair2Pctl(PLtl left_node, PLtl right_node, PrologGenerator gen) {
		
		pto.openTerm("and");
		
		pto.openTerm("weakassumptions");
		left_node.apply(gen);
		pto.closeTerm();
				
		pto.openTerm("strongassumptions");
		right_node.apply(gen);
		pto.closeTerm();
		
		pto.closeTerm();
	}

	public void weak_fair_allPctl() {
		pto.printAtom("all");
	}

	public void strong_fair_allPctl() {
		pto.printAtom("all");
	}

	public void dlkPctl(ADlkLtl node, PrologGenerator gen) {
		LinkedList<PActions> list = node.getArgs();
		pto.openTerm("ap");
		pto.openTerm("dlk");
		pto.openList();
		for (PActions pLtl : list) {
			pLtl.apply(gen);
		}
		pto.closeList();
		pto.closeTerm();
		pto.closeTerm();
	}

	public void detPctl(ADetLtl node, PrologGenerator gen) {
		LinkedList<PActions> list = node.getArgs();
		pto.openTerm("ap");
		pto.openTerm("det");
		pto.openList();
		for (PActions pLtl : list) {
			pLtl.apply(gen);
		}
		pto.closeList();
		pto.closeTerm();
		pto.closeTerm();
	}

	public void ctrlPctl(ACtrlLtl node, PrologGenerator gen) {
		LinkedList<PActions> list = node.getArgs();
		pto.openTerm("ap");
		pto.openTerm("ctrl");
		pto.openList();
		for (PActions pLtl : list) {
			pLtl.apply(gen);
		}
		pto.closeList();
		pto.closeTerm();
		pto.closeTerm();
	}

}
