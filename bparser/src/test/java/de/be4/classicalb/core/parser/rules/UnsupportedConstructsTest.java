package de.be4.classicalb.core.parser.rules;

import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class UnsupportedConstructsTest {

	@Test
	public void testANYIsNotAllowed() {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY ANY x WHERE x : 1..10 THEN skip END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("The ANY substitution is not allowed in a RULES_MACHINE.", e.getMessage());
	}

	@Test
	public void testBecomesElementOfIsNotAllowed() {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY VAR x IN x :: {1,2} END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("The BecomesElementOf substitution (a,b:(P)) is not allowed in a RULES_MACHINE.", e.getMessage());
	}

	@Test
	public void testDeferredSetsAreNotAllowed() {
		final String testMachine = "RULES_MACHINE test SETS D END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Deferred sets are not allowed in a RULES_MACHINE.", e.getMessage());
	}
}
