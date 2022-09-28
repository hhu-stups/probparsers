package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class OpPatternTest {
	@Test
	public void testNoArgs() throws BCompoundException {
		checkParser("no arguments", "operation1", "oppattern(none,operation1,[])");
	}

	@Test
	public void testSimpleArgument() throws BCompoundException {
		checkParser("simple argument", "operation1(5)",
				"oppattern(none,operation1,[def(none,integer(none,5))])");
	}

	@Test
	public void testSimpleArguments() throws BCompoundException {
		checkParser(
				"simple argument",
				"operation1(5,7)",
				"oppattern(none,operation1,[def(none,integer(none,5)),def(none,integer(none,7))])");
	}

	@Test
	public void testEmptyArguments() throws BCompoundException {
		checkParser("simple argument", "operation1(5,_)",
				"oppattern(none,operation1,[def(none,integer(none,5)),undef(none)])");
	}

	private void checkParser(final String description, final String oppattern,
			final String expected) throws BCompoundException {
		final String parsed = Helpers.getMachineAsPrologTerm(BParser.OPERATION_PATTERN_PREFIX + oppattern);
		assertEquals(description, "machine(" + expected + ").", parsed);
	}
}
