package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FormulaTest {
	@Test
	public void testFormulaOutput() throws BCompoundException {
		String output = Helpers.getFormulaAsPrologTerm("1+1");
		assertEquals(output, "machine(add(none,integer(none,1),integer(none,1))).");
	}

	@Test
	public void testFomulaExpression() throws Exception {
		String formula = "3 + y";
		new BParser().parseFormula(formula);
	}

	@Test
	public void testFomulaPredicate() throws Exception {
		String formula = "3 = y";
		new BParser().parseFormula(formula);
	}

	@Test(expected = BCompoundException.class)
	public void testBuggyFomulaExpression() throws Exception {
		String formula = "3 + y - ";
		new BParser().parseFormula(formula);
	}

	@Test(expected = BCompoundException.class)
	public void testBuggyFomulaPredicate() throws Exception {
		String formula = "3 = ";
		new BParser().parseFormula(formula);
	}

	// Ensure that JIRA ticket PROB-295 is fixed.
	@Test
	public void ticketProb295() throws BCompoundException {
		// String input = "#x. /* comment */ (x>1000 & x<2**10)";
		String input1 = "#x. /*buh */ (  x>1000 & x<2**10)";
		String input2 = "#x.(/*buh */ x>1000 & x<2**10)";

		String result1 = Helpers.getFormulaAsPrologTerm(input1);
		String result2 = Helpers.getFormulaAsPrologTerm(input2);

		assertEquals(result1, result2);
	}

	@Test
	public void test1002file1() throws BCompoundException {
		String source = "cajas : STRING +-> NATURAL & num : STRING & m : NATURAL & m > cajas(num) & num : dom(cajas) & cajas /= { } & { num |-> ( ( cajas(num) ) - m ) } /= { } & dom({ num |-> ( ( cajas(num) ) - m ) }) <<: dom(cajas)";
		Start result = new BParser().parseFormula(source);
		assertNotNull(result);
	}

	@Test
	public void test1002file2() throws BCompoundException {

		String source = " clients : STRING +-> STRING & balances : STRING +-> NATURAL & owners : STRING <-> STRING & u : STRING & n : STRING & m : NATURAL & m > balances(n) & m < 100000";
		Start result = new BParser().parseFormula(source);
		assertNotNull(result);
	}

	@Test
	public void test1002file3() throws BCompoundException {
		String source = "srv : {\"HS\",\"SC\",\"COM\",\"VOM\",\"SDA\",\"RDA\",\"TD\",\"RA\",\"GMR\",\"LDM\",\"EP\",\"MSP\"} & om : {\"SAFETY\",\"NOM\",\"DIAG\"} & acquiring : {TRUE,FALSE} & prepData : {TRUE,FALSE} & prepDataType : {\"SD\",\"HD\",\"MD\"} & page : NATURAL & ia : NATURAL & fa : NATURAL & lpck : seq(STRING) & lpckDT : {\"CR\",\"CD\",\"OM\",\"ND\",\"ASD\",\"AMD\",\"MSE\",\"ICS\",\"LS\"} & csrs : {\"SD\",\"HD\",\"MD\"} --> NATURAL & totCSR : {\"SD\",\"HD\",\"MD\"} --> NATURAL & csc : NATURAL & mem : 1 .. 1024 --> STRING & modMem : POW(NATURAL) & sdwp : NATURAL & sparam : {\"HGT\",\"ILP\",\"AICST\"} --> NATURAL & time : NATURAL & processingCmd : {TRUE,FALSE} & iain : NATURAL & data : seq(STRING) & c : NATURAL & processingCmd = TRUE & srv = \"LDM\" & om = \"NOM\" & csc > 0 & c = csc - 1 & sparam(\"ILP\") <= iain & iain + card(data) : 0 .. 32 & {x | x-iain:dom(data)} /\\ modMem = { } & {x | x-iain:dom(data)} /= { } & modMem = { }";
		Start result = new BParser().parseFormula(source);
		assertNotNull(result);
	}
}
