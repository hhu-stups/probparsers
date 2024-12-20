package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class MultipleRecordsAssignment {



	@Test
	public void testTipple() throws BCompoundException {
		String testSubstitution = "xx'aa'bb := 4 ";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);
		assertEquals("machine(assign(none,[record_field(none,record_field(none,identifier(none,xx),identifier(none,aa)),identifier(none,bb))],[integer(none,4)])).", result);
	}


	@Test
	public void testDouble() throws BCompoundException {
		String testSubstitution = "xx'aa := 4 ";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);
		assertEquals("machine(assign(none,[record_field(none,identifier(none,xx),identifier(none,aa))],[integer(none,4)])).", result);
	}


	@Test
	public void moreContext() throws BCompoundException {
		String testSubstitution = "xx'aa'bb := 5 ||" + System.lineSeparator() + " out := xx'aa'bb";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);
		assertEquals("machine(parallel(none,[assign(none,[record_field(none,record_field(none,identifier(none,xx),identifier(none,aa)),identifier(none,bb))],[integer(none,5)]),assign(none,[identifier(none,out)],[record_field(none,record_field(none,identifier(none,xx),identifier(none,aa)),identifier(none,bb))])])).", result);
	}



}
