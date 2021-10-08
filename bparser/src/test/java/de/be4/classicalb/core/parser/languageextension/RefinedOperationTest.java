package de.be4.classicalb.core.parser.languageextension;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RefinedOperationTest {

	@Test
	public void testRefKeyword() throws Exception {
		final String testMachine = "MACHINE Test OPERATIONS foo ref fooA = skip END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		System.out.println(result);
		assertTrue(result.contains("machine(abstract_machine(1,machine(2),machine_header(3,'Test',[]),[operations(4,[refined_operation(5,identifier(5,foo),[],[],fooA,skip(6))])]))."));
	}
	
	@Test
	public void testInvalidKeyword() {
		final String testMachine = "MACHINE Test OPERATIONS foo NotRef fooA = skip END";
		try {
			final String result = Helpers.getMachineAsPrologTerm(testMachine);
			System.out.println(result);
			fail("Expected parser exception was not thrown");
		} catch (BCompoundException e) {
			assertTrue(e.getCause() instanceof CheckException);
			assertTrue(e.getCause().getMessage().contains("Expect 'ref' key word in operation definition."));
		}
	}
	
}
