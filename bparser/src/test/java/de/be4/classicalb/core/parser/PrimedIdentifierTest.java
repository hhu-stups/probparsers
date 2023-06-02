package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class PrimedIdentifierTest {
	@Test
	public void testBecomeSuchSubstitution() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION x : (x$0 = x)";
		final String expected = "machine(becomes_such(none,[identifier(none,x)],equal(none,primed_identifier(none,x,0),identifier(none,x)))).";
		final String actual = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(expected, actual);
	}

	@Test
	public void testRestrictedUsage() {
		final String testMachine = "#SUBSTITUTION x : (x = x$5)";
		Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testDontPrimedIdentifiersInQuantifiers() {
		final String testMachine = "#PREDICATE !a$0.(a=5 => b=6)";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Deprecated
	@Test(expected = BCompoundException.class)
	public void testPrimedIdentifiersInQuantifiersRestrictedModeFalse() throws BCompoundException {
		final String testMachine = "#PREDICATE !a$0.(a$0=5 => b=6)";
		final BParser parser = new BParser("testcase");
		parser.getOptions().setRestrictPrimedIdentifiers(false);
		final Start startNode = parser.parse(testMachine, false);
		// this mode is no longer supported
	}

	@Test
	public void testPrimedIdentifiersInQuantifiers() throws BCompoundException {
		final String testMachine = "#PREDICATE !a$0.(a$0=5 => b=6)";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}
}
