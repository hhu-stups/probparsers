package de.be4.classicalb.core.parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import de.be4.classicalb.core.preparser.lexer.LexerException;
import de.be4.classicalb.core.preparser.parser.Parser;
import de.be4.classicalb.core.preparser.parser.ParserException;

import org.junit.Test;

public class PreParserTest {

	private static void preparse(final String testMachine)
			throws ParserException, LexerException, IOException {
		final Parser parser = new Parser(
				new PreLexer(
						new PushbackReader(new StringReader(testMachine), 99)));
		parser.parse();
	}

	@Test
	public void testSimple1() throws Exception {
		final String testMachine = "DEFINITIONS blub == skip";
		preparse(testMachine);
	}

	@Test
	public void testSimple2() throws Exception {
		final String testMachine = "DEFINITIONS blub == skip;bla==blub";
		preparse(testMachine);
	}

	@Test
	public void testParameters1() throws Exception {
		final String testMachine = "DEFINITIONS blub(a,b) == skip";
		preparse(testMachine);
	}

	@Test
	public void testParameters2() throws Exception {
		final String testMachine = "DEFINITIONS blub(a,b) == skip;\n\tbla(x,y) == x+y";
		preparse(testMachine);
	}

	@Test
	public void testComplete1() throws Exception {
		final String testMachine = "MACHINE TestMachine VARIABLES xx DEFINITIONS blub == skip;bla(x,y)==blub END";
		preparse(testMachine);
	}

	@Test
	public void testComplete2() throws Exception {
		final String testMachine = "MACHINE TestMachine VARIABLES xx DEFINITIONS blub == skip;bla==blub INVARIANT xx : NAT END";
		preparse(testMachine);
	}

	@Test
	public void testComments1() throws Exception {
		final String testMachine = "/* comment1 */bla /* comment2 */ DEFINITIONS/* comment3 */ blub/* comment4 */ ==/* comment5 */ skip/* comment6 */";
		preparse(testMachine);
	}

	@Test
	public void testComments2() throws Exception {
		final String testMachine = "/* comment1 * / */bla /* //comment2 */ **DEFINITIONS/* comment3 */ blub/* comment4 */ ==/* comment5 */ skip/* ;comment6 */";
		preparse(testMachine);
	}

	@Test
	public void testComments3() throws Exception {
		final String testMachine = "/* comment1 * / */bla /* //comment2 */ **DEFINITIONS/* comment3 */ blub/* comment4 */ ==/* comment5 */ skip/* comment6 */; bla == x:=5";
		preparse(testMachine);
	}

}
