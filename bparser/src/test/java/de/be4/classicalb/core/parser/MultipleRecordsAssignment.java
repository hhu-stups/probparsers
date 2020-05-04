package de.be4.classicalb.core.parser;

import org.junit.Assert;
import org.junit.Test;
import util.Helpers;

import static org.junit.Assert.assertTrue;

public class MultipleRecordsAssignment {



	@Test
	public void testTipple() {
		final String testMachine = "#SUBSTITUTION xx'aa'bb := 4 ";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("machine(assign(2,[record_field(3,record_field(4,identifier(5,xx),identifier(6,aa)),identifier(7,bb))],[integer(8,4)]))"));
	}


	@Test
	public void testDouble() {
		final String testMachine = "#SUBSTITUTION xx'aa := 4 ";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("machine(assign(2,[record_field(3,identifier(4,xx),identifier(5,aa))],[integer(6,4)]))"));
	}


	@Test
	public void moreContext() {
		final String testMachine = "#SUBSTITUTION xx'aa'bb := 5 ||\n" +
				"\tout := xx'aa'bb";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("machine(parallel(2,[assign(3,[record_field(4,record_field(5,identifier(6,xx),identifier(7,aa)),identifier(8,bb))],[integer(9,5)]),assign(10,[identifier(11,out)],[record_field(12,record_field(13,identifier(14,xx),identifier(15,aa)),identifier(16,bb))])])).\n"));

	}


	@Test
	public void fail_wrong_record_function_assignment() {
		Exception wasThrown = null;
		final String testMachine = "#SUBSTITUTION xx'aa'bb(a) := 4 ";
		try{
			Helpers.getMachineAsPrologTerm(testMachine);
		}catch (Exception e){
			wasThrown = e;
		}finally {
			if(wasThrown != null){
				Assert.assertTrue(true);
			}
			else{
				Assert.fail();
			}
		}

	}

}
