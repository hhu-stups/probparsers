package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.grammars.DefaultGrammar;
import de.be4.classicalb.core.parser.grammars.IGrammar;

public class ParseOptions {

	/**
	 * if true the parser will throw a LexerException when unrecognised pragmas appear
	 */
	private boolean strictPragmaChecking = false;

	/**
	 * if true the parser will ignore checking valid combinations
	 */
	private boolean ignoreCheckingValidCombinations = false;

	/**
	 * if true, AST transformations are enabled.
	 * <br>
	 * when disabled the AST is unusable because a lot of assumptions no longer hold!
	 */
	private boolean applyASTTransformations = true;

	private IGrammar grammar = new DefaultGrammar();

	public IGrammar getGrammar() {
		return grammar;
	}

	public void setGrammar(IGrammar grammar) {
		this.grammar = grammar;
	}

	public boolean isStrictPragmaChecking() {
		return strictPragmaChecking;
	}

	public void setStrictPragmaChecking(boolean newVal) {
		this.strictPragmaChecking = newVal;
	}

	public boolean isIgnoreCheckingValidCombinations() {
		return ignoreCheckingValidCombinations;
	}

	public void setIgnoreCheckingValidCombinations(boolean ignoreCheckingValidCombinations) {
		this.ignoreCheckingValidCombinations = ignoreCheckingValidCombinations;
	}

	public boolean isApplyASTTransformations() {
		return applyASTTransformations;
	}

	public void setApplyASTTransformations(boolean applyASTTransformations) {
		this.applyASTTransformations = applyASTTransformations;
	}
}
