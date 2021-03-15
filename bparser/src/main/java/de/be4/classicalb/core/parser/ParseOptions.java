package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.grammars.DefaultGrammar;
import de.be4.classicalb.core.parser.grammars.IGrammar;

public class ParseOptions {

	/*
	 * The parser must not accept some expressions that are only relevant in PO
	 * files. (E.g. bfalse or SET(x).(P) )
	 */
	private boolean restrictProverExpressions = true;

	/*
	 * The parser should accept a primed identifier ("x$0") only in becomeSuch
	 * substitutions and there only with the integer 0. This option can be set
	 * to false in order to parse PO files of AtelierB.
	 */
	private boolean restrictPrimedIdentifiers = true;
	
	/*
	 * if true the parser will throw a LexerException when unrecognised pragmas appear
	*/
	private boolean strictPragmaChecking = false;

	/*
	* if true the parser will ignore checking valid combinations
	*/
	private boolean ignoreCheckingValidCombinations = false;
	
	private IGrammar grammar = new DefaultGrammar();

	public boolean isRestrictProverExpressions() {
		return restrictProverExpressions;
	}

	public void setRestrictProverExpressions(boolean restrictProverExpressions) {
		this.restrictProverExpressions = restrictProverExpressions;
	}

	public boolean isRestrictPrimedIdentifiers() {
		return restrictPrimedIdentifiers;
	}

	public void setRestrictPrimedIdentifiers(boolean restrictPrimedIdentifiers) {
		this.restrictPrimedIdentifiers = restrictPrimedIdentifiers;
	}

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
}
