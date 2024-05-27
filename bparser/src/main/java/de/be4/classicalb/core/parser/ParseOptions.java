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
	 * if true the lexer will ignore certain tokens (typically ignored tokens).
	 * This option currently does nothing and will probably be removed in the future.
	 */
	private boolean ignoreUselessTokens = true;

	/**
	 * if true the parser will collect definitions.
	 * <br>
	 * when disabled some checks/transformations will no longer work!
	 */
	private boolean collectDefinitions = true;

	/**
	 * if true, AST transformations are enabled.
	 * <br>
	 * when disabled the AST is unusable because a lot of assumptions no longer hold!
	 */
	private boolean applyASTTransformations = true;

	/**
	 * if true the parser will apply some semantic checks.
	 * <br>
	 * when disabled some errors will not be caught!
	 */
	private boolean applySemanticChecks = true;

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

	public boolean isIgnoreUselessTokens() {
		return ignoreUselessTokens;
	}

	public void setIgnoreUselessTokens(boolean ignoreUselessTokens) {
		this.ignoreUselessTokens = ignoreUselessTokens;
	}

	public boolean isCollectDefinitions() {
		return collectDefinitions;
	}

	public void setCollectDefinitions(boolean collectDefinitions) {
		this.collectDefinitions = collectDefinitions;
	}

	public boolean isApplyASTTransformations() {
		return applyASTTransformations;
	}

	public void setApplyASTTransformations(boolean applyASTTransformations) {
		this.applyASTTransformations = applyASTTransformations;
	}

	public boolean isApplySemanticChecks() {
		return applySemanticChecks;
	}

	public void setApplySemanticChecks(boolean applySemanticChecks) {
		this.applySemanticChecks = applySemanticChecks;
	}
}
