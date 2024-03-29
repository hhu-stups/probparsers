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

final class PrologGeneratorHelper {
	private final IPrologTermOutput pto;
	private final String currentStateID;
	private final ProBParserBase specParser;

	public PrologGeneratorHelper(final IPrologTermOutput pto,
			final String currentStateID, final ProBParserBase specParser) {
		super();
		this.pto = pto;
		this.currentStateID = currentStateID;
		this.specParser = specParser;
	}

	public void defaultIn(Class<?> clazz) {
		StringBuilder sb = new StringBuilder(clazz.getSimpleName());
		sb.setLength(sb.length() - 3);
		sb.deleteCharAt(0);
		String term = sb.toString().toLowerCase(Locale.ENGLISH);
		pto.openTerm(term);
	}

	public void defaultOut() {
		pto.closeTerm();
	}

	public void caseUnparsed(final UniversalToken token) {
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

	public void caseUnparsedPredicate(final UniversalToken token) {
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

	public void caseUnparsedExpression(final UniversalToken token) {
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

	public void enabled(final UniversalToken token) {
		pto.openTerm("ap");
		pto.openTerm("enabled");
		parseTransitionPredicate(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	
	public void available(final UniversalToken token) {
		pto.openTerm("ap"); // atomic property
		pto.openTerm("available");
		parseTransitionPredicate(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	
	public void strong_fair(UniversalToken token) {
		pto.openTerm("ap");
		pto.openTerm("strong_fair");
		parseTransitionPredicate(token);
		pto.closeTerm();
		pto.closeTerm();		
	}

	public void weak_fair(UniversalToken token) {
		pto.openTerm("ap");
		pto.openTerm("weak_fair");
		parseTransitionPredicate(token);
		pto.closeTerm();
		pto.closeTerm();
	}

	public void parseTransitionPredicate(final UniversalToken token) {
		try {
			specParser.parseTransitionPredicate(pto, token.getText(), true, token.getLine(), token.getColumn());
		} catch (ProBParseException | UnsupportedOperationException e) {
			throw createAdapterException(token, e);
		}
	}

	public void sink() {
		pto.openTerm("ap");
		pto.printAtom("sink");
		pto.closeTerm();
	}

	public void goal() {
		pto.openTerm("ap");
		pto.printAtom("goal");
		pto.closeTerm();
	}

	public void det_output() {
		pto.openTerm("ap");
		pto.printAtom("det_output");
		pto.closeTerm();
	}

	public void state_error() {
		pto.openTerm("ap");
		pto.printAtom("state_error");
		pto.closeTerm();
	}

	public void deadlock() {
		pto.openTerm("ap");
		pto.printAtom("deadlock");
		pto.closeTerm();
	}

	public void current() {
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

	public void existsTerm(AExistsLtl node, PrologGenerator gen) {
		
		pto.openTerm("exists");
		String identifier = node.getExistsIdentifier().getText();
		pto.printAtom(identifier);
		
		final UniversalToken token = UniversalToken.createToken(node.getPredicate());
		this.caseUnparsed(token);

		node.getLtl().apply(gen);

		pto.closeTerm();
		
	}

	public void forallTerm(AForallLtl node, PrologGenerator gen) {
		
		pto.openTerm("forall");
		String identifier = node.getForallIdentifier().getText();
		pto.printAtom(identifier);
		
		final UniversalToken token = UniversalToken.createToken(node.getPredicate());
		this.caseUnparsed(token);

		node.getLtl().apply(gen);

		pto.closeTerm();
		
	}

	public void unchangedTerm(AUnchangedLtl node, PrologGenerator gen) {
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("eq");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpression(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void changedTerm(AChangedLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("neq");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpression(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void decreasingTerm(ADecreasingLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("gt");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpression(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void increasingTerm(AIncreasingLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("change_expr");
		pto.printAtom("lt");
		final UniversalToken token = UniversalToken.createToken(node.getExpression());
		this.caseUnparsedExpression(token);
		pto.closeTerm();
		pto.closeTerm();
	}
	public void before_afterTerm(ABeforeAfterLtl node, PrologGenerator gen) {
		
		pto.openTerm("action");
		pto.openTerm("before_after");
		final UniversalToken token = UniversalToken.createToken(node.getPredicate());
		this.caseUnparsedPredicate(token);
		pto.closeTerm();
		pto.closeTerm();
	}

	public void and_fair1(PLtl left_node, PLtl right_node, PrologGenerator gen) {
		
		pto.openTerm("and");
		
		pto.openTerm("strongassumptions");
		left_node.apply(gen);
		pto.closeTerm();
				
		pto.openTerm("weakassumptions");
		right_node.apply(gen);
		pto.closeTerm();
		
		pto.closeTerm();
	}

	public void and_fair2(PLtl left_node, PLtl right_node, PrologGenerator gen) {
		
		pto.openTerm("and");
		
		pto.openTerm("weakassumptions");
		left_node.apply(gen);
		pto.closeTerm();
				
		pto.openTerm("strongassumptions");
		right_node.apply(gen);
		pto.closeTerm();
		
		pto.closeTerm();
	}

	public void weak_fair_all() {
		pto.printAtom("all");
	}

	public void strong_fair_all() {
		pto.printAtom("all");
	}

	public void dlk(ADlkLtl node, PrologGenerator gen) {
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

	public void det(ADetLtl node, PrologGenerator gen) {
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

	public void ctrl(ACtrlLtl node, PrologGenerator gen) {
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
