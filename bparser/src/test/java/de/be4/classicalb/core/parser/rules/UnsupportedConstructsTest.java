package de.be4.classicalb.core.parser.rules;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import static de.be4.classicalb.core.parser.rules.RulesUtil.getRulesProjectAsPrologTerm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class UnsupportedConstructsTest {

	@Test
	public void testANYIsNotAllowed() {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY ANY x WHERE x : 1..10 THEN skip END END END";
		try {
			String result = getRulesProjectAsPrologTerm(testMachine);
			fail("Expected exception was not thrown");
		} catch (BCompoundException e) {
			assertTrue(e.getCause() instanceof CheckException);
			assertEquals("The ANY substitution is not allowed in a RULES_MACHINE.", e.getCause().getMessage());
		}
	}

	@Test
	public void testBecomesElementOfIsNotAllowed() {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY VAR x IN x :: {1,2} END END END";
		try {
			String result = getRulesProjectAsPrologTerm(testMachine);
			fail("Expected exception was not thrown");
		} catch (BCompoundException e) {
			assertTrue(e.getCause() instanceof CheckException);
			assertEquals("The BecomesElementOf substitution (a,b:(P)) is not allowed in a RULES_MACHINE.", e.getCause().getMessage());
		}
	}

	@Test
	public void testDeferredSetsAreNotAllowed() {
		final String testMachine = "RULES_MACHINE test SETS D END";
		try {
			String result = getRulesProjectAsPrologTerm(testMachine);
			fail("Expected exception was not thrown");
		} catch (BCompoundException e) {
			assertTrue(e.getCause() instanceof CheckException);
			assertEquals("Deferred sets are not allowed in a RULES_MACHINE.", e.getCause().getMessage());
		}
	}
}
