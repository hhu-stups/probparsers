package de.be4.classicalb.core.parser.predvars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.Definitions;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Start;

import util.Helpers;

public class PredVarsTest {

	@Test
	public void testAandB() throws BCompoundException, LexerException, IOException {
		final String testMachine = "#FORMULA A & B";
		String res = "#PREDICATE (A=TRUE) & (B=TRUE)";
		final String result1 = getMachineAsPrologTermEparse(testMachine);
		final String result2 = Helpers.getMachineAsPrologTerm(res);
		assertNotNull(result1);
		assertNotNull(result2);
		assertEquals(result1, result2);
	}

	@Test
	public void testSemiAandB() throws BCompoundException, LexerException, IOException {
		final String testMachine = "#FORMULA A<3 & B";
		String res = "#PREDICATE (A<3) & (B=TRUE)";
		final String result1 = getMachineAsPrologTermEparse(testMachine);
		final String result2 = Helpers.getMachineAsPrologTerm(res);
		assertNotNull(result1);
		assertNotNull(result2);
		assertEquals(result1, result2);
	}

	@Test
	public void testPred() throws BCompoundException, LexerException, IOException {
		final String testMachine = "#FORMULA A<3 & B>9";
		String res = "#PREDICATE (A<3) & (B>9)";
		final String result1 = getMachineAsPrologTermEparse(testMachine);
		final String result2 = Helpers.getMachineAsPrologTerm(res);
		assertNotNull(result1);
		assertNotNull(result2);
		assertEquals(result1, result2);
	}

	@Test
	public void testImpl() throws BCompoundException, LexerException, IOException {
		final String testMachine = "#FORMULA A & (B>9 => C) & D";
		String res = "#PREDICATE (A=TRUE) & (B>9 => C=TRUE) & (D=TRUE)";
		final String result1 = getMachineAsPrologTermEparse(testMachine);
		final String result2 = Helpers.getMachineAsPrologTerm(res);
		assertNotNull(result1);
		assertNotNull(result2);
		assertEquals(result1, result2);
	}

	private static String getMachineAsPrologTermEparse(final String testMachine) throws BCompoundException,
			LexerException, IOException {
		final BParser parser = new BParser("testcase");
		Start ast = parser.eparse(testMachine, new Definitions());
		return Helpers.getTreeAsPrologTerm(ast);
	}
}
