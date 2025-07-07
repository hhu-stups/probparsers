/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.be4.ltl.core.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import de.be4.ltl.core.pctlparser.lexer.Lexer;
import de.be4.ltl.core.pctlparser.lexer.LexerException;
import de.be4.ltl.core.pctlparser.node.Start;
import de.be4.ltl.core.pctlparser.parser.Parser;
import de.be4.ltl.core.pctlparser.parser.ParserException;
import de.be4.ltl.core.parser.internal.PctlLexer;
import de.be4.ltl.core.parser.internal.PrologPctlGenerator;
import de.be4.ltl.core.parser.internal.UniversalToken;
import de.prob.parserbase.ProBParserBase;
import de.prob.prolog.output.StructuredPrologOutput;

public class PctlParser extends TemporalLogicParser<Start> {
	public PctlParser(final ProBParserBase specParser) {
		super(specParser);
	}

	@Override
	protected Start parseFormula(final String formula)
			throws LtlParseException, IOException {
		StringReader reader = new StringReader(formula);
		PushbackReader r = new PushbackReader(reader);
		Lexer l = new PctlLexer(r);
		Parser p = new Parser(l);
		Start ast;
		try {
			ast = p.parse();
		} catch (ParserException e) {
			final UniversalToken token = UniversalToken.createToken(e
					.getToken());
			throw new LtlParseException(token, e);
		} catch (LexerException e) {
			throw new LtlParseException(null, e);
		}
		return ast;
	}

	@Override
	protected void applyPrologGenerator(StructuredPrologOutput pto,
			String stateID, ProBParserBase specParser, Start ast) {
		final PrologPctlGenerator prologGenerator = new PrologPctlGenerator(pto,
				stateID, specParser);
		ast.apply(prologGenerator);
	}
}
