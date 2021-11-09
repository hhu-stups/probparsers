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
		assertTrue(result.contains("machine(abstract_machine(none,machine(none),machine_header(none,foo,[]),[constants(none,[description(none,'konstante nummero uno',identifier(none,c))]),properties(none,equal(none,identifier(none,c),integer(none,5))),variables(none,[description(none,'Hallo du variable',identifier(none,x))]),invariant(none,equal(none,identifier(none,x),integer(none,1))),initialisation(none,assign(none,[identifier(none,x)],[integer(none,1)]))]))."));
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
				"machine(abstract_machine(none,machine(none),machine_header(none,test,[]),[constants(none,[identifier(none,c)]),properties(none,equal(none,identifier(none,c),symbolic_comprehension_set(none,[identifier(none,x)],member(none,identifier(none,x),natural_set(none)))))]))."));
	}
}