package de.prob.voparser;

import de.prob.voparser.lexer.Lexer;
import de.prob.voparser.lexer.LexerException;
import de.prob.voparser.node.Start;
import de.prob.voparser.parser.Parser;
import de.prob.voparser.parser.ParserException;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class VOParser {

	private final Map<String, VOType> tasks;

	private final VOScopeChecker scopeChecker;

	private final VOTypeChecker typeChecker;

	public VOParser() {
		this.tasks = new HashMap<>();
		this.scopeChecker = new VOScopeChecker(this);
		this.typeChecker = new VOTypeChecker(this);
	}

	public Start parseFormula(String formula) throws VOParseException {
		return parseAST(formula);
	}

	public Start parseAST(String formula) throws VOParseException {
		StringReader reader = new StringReader(formula);
		PushbackReader r = new PushbackReader(reader);
		Lexer l = new Lexer(r);
		Parser p = new Parser(l);
		Start ast = null;
		try {
			ast = p.parse();
		} catch (ParserException e) {
			throw new VOParseException("Parsing VO formula failed", VOParseException.ErrorType.PARSING);
		} catch (IOException e) {
			throw new VOParseException("Parsing VO formula failed", VOParseException.ErrorType.PARSING);
		} catch (LexerException e) {
			throw new VOParseException("Parsing VO formula failed", VOParseException.ErrorType.PARSING);
		}
		return ast;
	}

	public void registerTask(String id, VOType type) {
		tasks.put(id, type);
	}

	public void deregisterTask(String id) {
		tasks.remove(id);
	}

	public void semanticCheck(String formula) throws VOParseException {
		scopeCheck(formula);
		typeCheck(formula);
	}

	private void scopeCheck(String formula) throws VOParseException {
		Start start = parseFormula(formula);
		scopeChecker.scopeCheck(start);
	}

	private void typeCheck(String formula) throws VOParseException {
		Start start = parseFormula(formula);
		typeChecker.typeCheck(start);
	}

	public Map<String, VOType> getTasks() {
		return tasks;
	}
}
