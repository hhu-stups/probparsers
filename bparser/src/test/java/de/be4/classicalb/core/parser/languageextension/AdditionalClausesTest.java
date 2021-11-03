package de.be4.classicalb.core.parser.languageextension;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import util.Helpers;

public class AdditionalClausesTest {

	@Test
	public void testExpressionsClause() throws Exception {
		final String testMachine = "MACHINE test EXPRESSIONS foo(a) == 1; bar == TRUE END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains(
				"[expressions(none,expression(none,foo,identifier(none,a),integer(none,1)),expression(none,bar,boolean_true(none)))]"));
	}

	@Test
	public void testPredicatesClause() throws Exception {
		final String testMachine = "MACHINE test PREDICATES foo(a) == 1=1; bar == 2=2 END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertTrue(result.contains(
				"[predicates(none,predicate(none,foo,identifier(none,a),equal(none,integer(none,1),integer(none,1))),predicate(none,bar,equal(none,integer(none,2),integer(none,2))))]"));
	}
}
