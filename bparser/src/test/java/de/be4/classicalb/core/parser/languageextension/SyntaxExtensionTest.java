package de.be4.classicalb.core.parser.languageextension;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SyntaxExtensionTest {

	@Test
	public void testMultiLineString() throws Exception {
		final String testMachine = "MACHINE Test PROPERTIES '''foo''' /= '''bar\nbazz''' END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("'bar\\nbazz'"));
	}

	@Test
	public void testMultiLineStringIncludingSingleQuate() throws Exception {
		final String testMachine = "MACHINE Test PROPERTIES '''' ''' = \"b\" END";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testFile() throws IOException, BCompoundException {
		String file = "strings/MultiLineString.mch";
		String result = Helpers.parseFile(file);
		assertTrue(result.contains("'\\n\\'\\na\\n\\'\\'\\'\\n'"));
	}

	@Test
	public void testMultiLineStringIncludingTwoSingleQuates() throws Exception {
		final String testMachine = "MACHINE Test PROPERTIES ''''' ''' = \"b\" END";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testLocalOperations() throws Exception {
		final String testMachine = "MACHINE Test LOCAL_OPERATIONS foo = skip END";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testIfThenElseExpression() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES 1 = IF 1=1 THEN 1 ELSE 2 END END";

		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[properties(none,equal(none,integer(none,1),if_then_else(none,equal(none,integer(none,1),integer(none,1)),integer(none,1),integer(none,2))))])).",
				result);
	}

	@Test
	public void testLetExpression() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES\n LET x BE x = 1 IN x + 1 END = 2 END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[properties(none,equal(none,let_expression(none,[identifier(none,x)],equal(none,identifier(none,x),integer(none,1)),add(none,identifier(none,x),integer(none,1))),integer(none,2)))])).",
				result);
	}

	@Test
	public void testIfThenElsePredicate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES IF 1=1 THEN 2=2 ELSE 3=3 END END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[properties(none,conjunct(none,[implication(none,equal(none,integer(none,1),integer(none,1)),equal(none,integer(none,2),integer(none,2))),implication(none,negation(none,equal(none,integer(none,1),integer(none,1))),equal(none,integer(none,3),integer(none,3)))]))])).",
				result);
	}

	@Test
	public void testIfThenElsePredicate2() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES IF 1=1 THEN 2=2 ELSE 3=3 END & 1=1 END";
		String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertFalse(result.contains("if"));
		assertFalse(result.contains("IF"));
	}

	@Test
	public void testLetPredicate() throws BCompoundException {
		final String testMachine = "MACHINE Test PROPERTIES LET x BE x = 1+1 IN x = 2 END END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'Test',[]),[properties(none,let_predicate(none,[identifier(none,x)],equal(none,identifier(none,x),add(none,integer(none,1),integer(none,1))),equal(none,identifier(none,x),integer(none,2))))])).",
				result);
	}

	@Test
	public void testTreeKeywords() throws BCompoundException {
		assertEquals(
			"machine(abstract_machine(none,machine(none),machine_header(none,'TreeKeywords',[]),[constants(none,[identifier(none,t)]),properties(none,conjunct(none,[member(none,identifier(none,t),btree(none,integer_set(none))),equal(none,identifier(none,t),bin(none,bin(none,integer(none,1)),integer(none,0),bin(none,integer(none,2)))),equal(none,top(none,identifier(none,t)),integer(none,0)),equal(none,top(none,left(none,identifier(none,t))),integer(none,1)),equal(none,top(none,right(none,identifier(none,t))),integer(none,2))]))])).",
			Helpers.getMachineAsPrologTerm("MACHINE TreeKeywords CONSTANTS t PROPERTIES t : btree(INTEGER) & t = bin(bin(1), 0, bin(2)) & top(t) = 0 & top(left(t)) = 1 & top(right(t)) = 2 END")
		);
	}

	@Test
	public void testTreeKeywordsAsRegularIdentifiers() throws BCompoundException {
		String expected = "machine(abstract_machine(none,machine(none),machine_header(none,'TreeKeywords',[]),[sets(none,[enumerated_set(none,'DIRECTION',[identifier(none,top),identifier(none,left),identifier(none,right)])]),constants(none,[identifier(none,bin)]),properties(none,equal(none,identifier(none,bin),set_extension(none,[identifier(none,left),identifier(none,right)])))])).";
		assertEquals(
			expected,
			Helpers.getMachineAsPrologTerm("MACHINE TreeKeywords SETS DIRECTION = {top, left, right} CONSTANTS bin PROPERTIES bin = {left, right} END")
		);
		assertEquals(
			expected,
			Helpers.getMachineAsPrologTerm("MACHINE TreeKeywords SETS DIRECTION = {`top`, `left`, `right`} CONSTANTS `bin` PROPERTIES `bin` = {`left`, `right`} END")
		);
	}

	@Test
	public void testTreeKeywordsAsFunctionIdentifiers() throws BCompoundException {
		assertEquals(
			"machine(abstract_machine(none,machine(none),machine_header(none,'TreeKeywords',[]),[constants(none,[identifier(none,bin),identifier(none,top)]),properties(none,conjunct(none,[equal(none,identifier(none,bin),set_extension(none,[couple(none,[boolean_false(none),integer(none,0)]),couple(none,[boolean_true(none),integer(none,1)])])),equal(none,identifier(none,top),set_extension(none,[couple(none,[integer(none,0),integer(none,1)]),couple(none,[integer(none,1),integer(none,1)])])),equal(none,function(none,identifier(none,bin),[boolean_true(none)]),function(none,identifier(none,top),[integer(none,1)]))]))])).",
			Helpers.getMachineAsPrologTerm("MACHINE TreeKeywords CONSTANTS bin, top PROPERTIES bin = {FALSE|->0, TRUE|->1} & top = {0|->1, 1|->1} & `bin`(TRUE) = `top`(1) END")
		);
	}
}
