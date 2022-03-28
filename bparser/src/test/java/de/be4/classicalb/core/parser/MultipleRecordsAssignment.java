package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertTrue;

public class MultipleRecordsAssignment {



	@Test
	public void testTipple() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION xx'aa'bb := 4 ";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("machine(assign(none,[record_field(none,record_field(none,identifier(none,xx),identifier(none,aa)),identifier(none,bb))],[integer(none,4)]))"));
	}


	@Test
	public void testDouble() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION xx'aa := 4 ";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("machine(assign(none,[record_field(none,identifier(none,xx),identifier(none,aa))],[integer(none,4)]))"));
	}


	@Test
	public void moreContext() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION xx'aa'bb := 5 ||" + System.lineSeparator() + " out := xx'aa'bb";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("machine(parallel(none,[assign(none,[record_field(none,record_field(none,identifier(none,xx)," +
				"identifier(none,aa)),identifier(none,bb))],[integer(none,5)]),assign(none,[identifier(none,out)],[" +
				"record_field(none,record_field(none,identifier(none,xx),identifier(none,aa)),identifier(none,bb))])]))."));

	}



}
