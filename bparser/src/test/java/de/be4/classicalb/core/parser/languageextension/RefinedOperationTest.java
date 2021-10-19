package de.be4.classicalb.core.parser.languageextension;

import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertTrue;

public class RefinedOperationTest {

	@Test
	public void testRefKeyword() throws Exception {
		final String testMachine = "MACHINE Test OPERATIONS foo ref fooA = skip END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains("machine(abstract_machine(1,machine(2),machine_header(3,'Test',[]),[operations(4,[refined_operation(5,identifier(5,foo),[],[],fooA,skip(6))])]))."));
	}
	
	@Test
	public void testInvalidKeyword() {
		final String testMachine = "MACHINE Test OPERATIONS foo NotRef fooA = skip END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertTrue(e.getMessage().contains("Expect 'ref' key word in operation definition."));
	}
	
}
