package de.be4.classicalb.core.parser.pragmas;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PragmaTest {

	@Test
	public void testLexer() throws BCompoundException {
		String input = "MACHINE foo CONSTANTS c /*@ desc konstante nummero uno */ PROPERTIES c = 5  VARIABLES x /*@ desc Hallo du variable */ INVARIANT x=1 INITIALISATION x:= 1 END";
		final String result = Helpers.getMachineAsPrologTerm(input);
		assertEquals("machine(abstract_machine(1,machine(2),machine_header(3,foo,[]),[constants(4,[description(5,'konstante nummero uno',identifier(6,c))]),properties(7,equal(8,identifier(9,c),integer(10,5))),variables(11,[description(12,'Hallo du variable',identifier(13,x))]),invariant(14,equal(15,identifier(16,x),integer(17,1))),initialisation(18,assign(19,[identifier(20,x)],[integer(21,1)]))])).\n",result);
	}

	@Test
	public void testLabelIncludingMinusSymbol() throws Exception {
		final String testMachine = "MACHINE test ASSERTIONS /*@label foo-bar*/ 1=1 END";
		final String result = Helpers.getTreeAsString(testMachine);
		assertEquals(
				"Start(AAbstractMachineParseUnit(AMachineHeader([test],[]),[AAssertionsMachineClause([ALabelPredicate(foo-bar,AEqualPredicate(AIntegerExpression(1),AIntegerExpression(1)))])]))",
				result);
	}

	@Test
	public void testSymbolicSetComprehension() throws Exception {
		final String testMachine = "MACHINE test CONSTANTS c PROPERTIES c = /*@symbolic*/ {x | x : NATURAL}  END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains(
				"machine(abstract_machine(1,machine(2),machine_header(3,test,[]),[constants(4,[identifier(5,c)]),properties(6,equal(7,identifier(8,c),symbolic_comprehension_set(9,[identifier(10,x)],member(11,identifier(12,x),natural_set(13)))))]))."));
	}
}