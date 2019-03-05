package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.visualisation.PreParserASTPrinter;
import de.be4.classicalb.core.preparser.lexer.LexerException;
import de.be4.classicalb.core.preparser.node.Start;
import de.be4.classicalb.core.preparser.parser.Parser;
import de.be4.classicalb.core.preparser.parser.ParserException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

public class PreParserTest {

    private static void getTreeAsString(final String testMachine)
            throws ParserException, LexerException, IOException {
        final Parser parser = new Parser(
                new PreLexer(
                        new PushbackReader(
                                new InputStreamReader(
                                        new ByteArrayInputStream(
                                                testMachine.getBytes())), 99)));
        final Start startNode = parser.parse();
        System.out.println();

        startNode.apply(new PreParserASTPrinter());
    }

    @Test
    public void testSimple1() throws Exception {
        final String testMachine = "DEFINITIONS blub == skip";
        getTreeAsString(testMachine);
    }

    @Test
    public void testSimple2() throws Exception {
        final String testMachine = "DEFINITIONS blub == skip;bla==blub";
        getTreeAsString(testMachine);
    }

    @Test
    public void testParameters1() throws Exception {
        final String testMachine = "DEFINITIONS blub(a,b) == skip";
        getTreeAsString(testMachine);
    }

    @Test
    public void testParameters2() throws Exception {
        final String testMachine = "DEFINITIONS blub(a,b) == skip;\n\tbla(x,y) == x+y";
        getTreeAsString(testMachine);
    }

    @Test
    public void testComplete1() throws Exception {
        final String testMachine = "MACHINE TestMachine VARIABLES xx DEFINITIONS blub == skip;bla(x,y)==blub END";
        getTreeAsString(testMachine);
    }

    @Test
    public void testComplete2() throws Exception {
        final String testMachine = "MACHINE TestMachine VARIABLES xx DEFINITIONS blub == skip;bla==blub INVARIANT xx : NAT END";
        getTreeAsString(testMachine);
    }

    @Test
    public void testComments1() throws Exception {
        final String testMachine = "/* comment1 */bla /* comment2 */ DEFINITIONS/* comment3 */ blub/* comment4 */ ==/* comment5 */ skip/* comment6 */";
        getTreeAsString(testMachine);
    }

    @Test
    public void testComments2() throws Exception {
        final String testMachine = "/* comment1 * / */bla /* //comment2 */ **DEFINITIONS/* comment3 */ blub/* comment4 */ ==/* comment5 */ skip/* ;comment6 */";
        getTreeAsString(testMachine);
    }

    @Test
    public void testComments3() throws Exception {
        final String testMachine = "/* comment1 * / */bla /* //comment2 */ **DEFINITIONS/* comment3 */ blub/* comment4 */ ==/* comment5 */ skip/* comment6 */; bla == x:=5";
        getTreeAsString(testMachine);
    }

}
