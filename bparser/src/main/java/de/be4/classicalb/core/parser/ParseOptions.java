package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.grammars.DefaultGrammar;
import de.be4.classicalb.core.parser.grammars.IGrammar;

public class ParseOptions {

	/*
	 * The parser must not accept some expressions that are only relevant in PO
	 * files. (E.g. SET(x).(P) )
	 * 
	 * @deprecated The Atelier B prover comprehension set syntax ({@code SET} keyword) will be removed entirely,
	 *     i. e. this option will effectively always be {@code true}.
	 */
	@Deprecated
	private boolean restrictProverExpressions = true;

	/*
	 * The parser should accept a primed identifier ("x$0") only in becomeSuch
	 * substitutions and there only with the integer 0. This option can be set
	 * to false in order to parse PO files of AtelierB.
	 * 
	 * @deprecated The Atelier B prover numbered identifier syntax will be removed entirely,
	 *     i. e. this option will effectively always be {@code true}.
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
	
	/*
	* if true the lexer will ignore certain tokens (typically ignored tokens)
	*/
	private boolean ignoreUselessTokens = true;
	
	private IGrammar grammar = new DefaultGrammar();

	/**
	 * @deprecated The Atelier B prover comprehension set syntax ({@code SET} keyword) will be removed entirely,
	 *     i. e. this option will effectively always be {@code true}.
	 */
	@Deprecated
	public boolean isRestrictProverExpressions() {
		return restrictProverExpressions;
	}

	/**
	 * @deprecated The Atelier B prover comprehension set syntax ({@code SET} keyword) will be removed entirely,
	 *     i. e. this option will effectively always be {@code true}.
	 */
	@Deprecated
	public void setRestrictProverExpressions(boolean restrictProverExpressions) {
		this.restrictProverExpressions = restrictProverExpressions;
	}

	/**
	 * @deprecated The Atelier B prover numbered identifier syntax will be removed entirely,
	 *     i. e. this option will effectively always be {@code true}.
	 */
	public boolean isRestrictPrimedIdentifiers() {
		return restrictPrimedIdentifiers;
	}

	/**
	 * @deprecated The Atelier B prover numbered identifier syntax will be removed entirely,
	 *     i. e. this option will effectively always be {@code true}.
	 */
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

	public boolean isIgnoreUselessTokens() {
		return ignoreUselessTokens;
	}

	public void setIgnoreUselessTokens(boolean ignoreUselessTokens) {
		this.ignoreUselessTokens = ignoreUselessTokens;
	}
}
