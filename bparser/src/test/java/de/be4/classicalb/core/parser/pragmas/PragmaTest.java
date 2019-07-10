package de.be4.classicalb.core.parser.pragmas;

import static org.junit.Assert.*;

import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;

import util.Helpers;
import de.be4.classicalb.core.parser.BLexer;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment;
import de.be4.classicalb.core.parser.analysis.prolog.PositionPrinter;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.Token;
import de.be4.classicalb.core.parser.visualisation.ASTPrinter;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

public class PragmaTest {

	@Test
	public void testLexer() throws Exception {
		
		String input = "MACHINE foo CONSTANTS c /*@ desc konstante nummero uno */ PROPERTIES c = 5  VARIABLES x /*@ desc Hallo du variable */ INVARIANT x=1 INITIALISATION x:= 1 END";

		BLexer lex = new BLexer(
				new PushbackReader(new StringReader(input), 500));
		Token t;
		while (!((t = lex.next()) instanceof EOF)) {
			System.out.print(t.getClass().getSimpleName() + "(" + t.getText()
					+ ")");
			System.out.print(" ");
		}

		BParser p = new BParser();

		System.out.println("\n" + input);

		Start ast = p.parse(input, false);

		ASTPrinter pr = new ASTPrinter();
		ast.apply(pr);

		final String result =printAST(ast);
		
		System.out.println(printAST(ast));
		
		assertEquals("abstract_machine(1,machine(2),machine_header(3,foo,[]),[constants(4,[description(5,'konstante nummero uno',identifier(6,c))]),properties(7,equal(8,identifier(9,c),integer(10,5))),variables(11,[description(12,'Hallo du variable',identifier(13,x))]),invariant(14,equal(15,identifier(16,x),integer(17,1))),initialisation(18,assign(19,[identifier(20,x)],[integer(21,1)]))])",result);
	}

	@Test
	public void testLabelIncludingMinusSymbol() throws Exception {
		final String testMachine = "MACHINE test ASSERTIONS /*@label foo-bar*/ 1=1 END";
		final String result = Helpers.getTreeAsString(testMachine);
		System.out.println(result);
		assertEquals(
				"Start(AAbstractMachineParseUnit(AMachineHeader([test],[]),[AAssertionsMachineClause([ALabelPredicate(foo-bar,AEqualPredicate(AIntegerExpression(1),AIntegerExpression(1)))])]))",
				result);
	}

	@Test
	public void testSymbolicSetComprehension() throws Exception {
		final String testMachine = "MACHINE test CONSTANTS c PROPERTIES c = /*@symbolic*/ {x | x : NATURAL}  END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		System.out.println(result);
		assertTrue(result.contains(
				"machine(abstract_machine(1,machine(2),machine_header(3,test,[]),[constants(4,[identifier(5,c)]),properties(6,equal(7,identifier(8,c),symbolic_comprehension_set(9,[identifier(10,x)],member(11,identifier(12,x),natural_set(13)))))]))."));
	}

	private String printAST(final Node node) {
		final StringWriter swriter = new StringWriter();
		NodeIdAssignment nodeids = new NodeIdAssignment();
		node.apply(nodeids);
		IPrologTermOutput pout = new PrologTermOutput(new PrintWriter(swriter),
				false);
		PositionPrinter pprinter = new ClassicalPositionPrinter(nodeids);
		ASTProlog prolog = new ASTProlog(pout, pprinter);
		node.apply(prolog);
		swriter.flush();
		return swriter.toString();
	}

}