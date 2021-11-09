package de.be4.classicalb.core.parser.rules;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class StringFormatOperatorTest {

	@Test
	public void testStringFormat() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test PROPERTIES STRING_FORMAT(\" ~w ~w \", 1, 2) = \" 1 2 \" END";
		String result = RulesUtil.getRulesMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testStringFormatWrongNumberOfArguments() {
		final String testMachine = "RULES_MACHINE Test PROPERTIES STRING_FORMAT(\" ~w ~w \", 1) = \" 1 2 \" END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The number of arguments (1) does not match the number of placeholders (2) in the string.", e.getMessage());
	}

	@Test
	public void testStringFormatWithConcat() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test PROPERTIES STRING_FORMAT(\" ~w \" ^  \"~w \", 1, 2) = \" 1 2 \" END";
		String result = RulesUtil.getRulesMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testStringFormatWithConcatWrongNumberOfArguments() {
		final String testMachine = "RULES_MACHINE Test PROPERTIES STRING_FORMAT(\" ~w \" ^  \"~w \", 1) = \" 1 2 \" END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The number of arguments (1) does not match the number of placeholders (2) in the string.", e.getMessage());
	}

	@Test
	public void testStringFormatConcatWithVariableLeft() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test PROPERTIES STRING_FORMAT(x ^ \" ~w \", 1) = \" 1 2 \" END";
		String result = RulesUtil.getRulesMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testStringFormatConcatWithVariableRight() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test PROPERTIES STRING_FORMAT(\" ~w \" ^ x, 1) = \" 1 2 \" END";
		String result = RulesUtil.getRulesMachineAsPrologTerm(testMachine);
	}

}
