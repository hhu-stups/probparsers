package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class PredicatesTest {
	@Test
	public void testAndOrPrio() throws BCompoundException {
		final String testMachine1 = "#PREDICATE 1=1 or 1=1 & 1=2";
		final String result1 = Helpers.getMachineAsPrologTerm(testMachine1);
		final String testMachine2 = "#PREDICATE (1=1 or 1=1) & 1=2";
		final String result2 = Helpers.getMachineAsPrologTerm(testMachine2);

		assertEquals(result1, result2);
	}

	@Test
	public void testParallelMember() throws BCompoundException {
		final String testMachine = "#PREDICATE x : ID & y : ID";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(conjunct(none,[member(none,identifier(none,x),identifier(none,'ID')),member(none,identifier(none,y),identifier(none,'ID'))])).",
				result);
	}

	@Test
	public void testParallelBelongs2() {
		final String testMachine = "#PREDICATE x,y : ID";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testInvariant1() throws BCompoundException {
		final String testMachine = "#PREDICATE hasread : READER <-> BOOK & reading : READER >+> COPY & (reading ; copyof) /\\ hasread = {}";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(conjunct(none,[member(none,identifier(none,hasread),relations(none,identifier(none,'READER'),identifier(none,'BOOK'))),member(none,identifier(none,reading),partial_injection(none,identifier(none,'READER'),identifier(none,'COPY'))),equal(none,intersection(none,composition(none,identifier(none,reading),identifier(none,copyof)),identifier(none,hasread)),empty_set(none))])).",
				result);
	}

	@Test
	public void testForall() throws BCompoundException {
		final String testMachine = "#PREDICATE ! a,b. (a=b => a/=b )";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(forall(none,[identifier(none,a),identifier(none,b)],implication(none,equal(none,identifier(none,a),identifier(none,b)),not_equal(none,identifier(none,a),identifier(none,b))))).",
				result);
	}

	@Test
	public void testForallCouple1() throws BCompoundException {
		final String testMachine = "#PREDICATE ! (a,b). (1=1 )";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(forall(none,[identifier(none,a),identifier(none,b)],equal(none,integer(none,1),integer(none,1)))).",
				result);
	}

	@Test(expected = BCompoundException.class)
	public void testTooManyparentheses() throws BCompoundException {
		final String testMachine = "#PREDICATE # ((b,c,d)). ( b > c)";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testExampleThesis1() throws BCompoundException {
		final String testMachine = "#PREDICATE 4 < 5 & 6 >= 7";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(conjunct(none,[less(none,integer(none,4),integer(none,5)),greater_equal(none,integer(none,6),integer(none,7))])).",
				result);
	}

	@Test
	public void testMultiCompositions() throws BCompoundException {
		final String testMachine = "#PREDICATE (p~;F;p) = G";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(equal(none,composition(none,composition(none,reverse(none,identifier(none,p)),identifier(none,'F')),identifier(none,p)),identifier(none,'G'))).",
				result);
	}

	@Test
	public void testWithComposition() throws BCompoundException {
		final String testMachine = "#PREDICATE (dom(ff ; (gg~)) <: dom(ff))";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(subset(none,domain(none,composition(none,identifier(none,ff),reverse(none,identifier(none,gg)))),domain(none,identifier(none,ff)))).",
				result);
	}

	@Test
	public void testEqualVsImplication() throws BCompoundException {
		final String testMachine = "#PREDICATE 1=2 <=> 3=4 => 5=6";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(implication(none,equivalence(none,equal(none,integer(none,1),integer(none,2)),equal(none,integer(none,3),integer(none,4))),equal(none,integer(none,5),integer(none,6)))).",
				result);
	}

	@Test
	public void testEqualVsImplicationFormula() throws BCompoundException {
		final String testMachine = "#FORMULA 1=2 <=> 3=4 => 5=6";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(implication(none,equivalence(none,equal(none,integer(none,1),integer(none,2)),equal(none,integer(none,3),integer(none,4))),equal(none,integer(none,5),integer(none,6)))).",
				result);
	}

	@Test
	public void testBFalse() throws BCompoundException {
		final String actual = Helpers.getMachineAsPrologTerm("#PREDICATE bfalse");
		final String expected = "machine(falsity(none)).";
		assertEquals(expected, actual);
	}

	@Test
	public void testBTrue() throws BCompoundException {
		final String actual = Helpers.getMachineAsPrologTerm("#PREDICATE btrue");
		final String expected = "machine(truth(none)).";
		assertEquals(expected, actual);
	}

	@Test
	public void testNonIdentifiersInQuantification() {
		final String testMachine = "#PREDICATE ! a,5. (a=5 => a/=5 )";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testNonIdentifiersInQuantificationFormula() {
		final String testMachine = "#FORMULA ! a,5. (a=5 => a/=5 )";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testSubstitutionInPredicate() throws BCompoundException {
		final String testMachine = "#PREDICATE (a>5) & [b:=a](b<10)";
		final BParser parser = new BParser("testcase");
		parser.getOptions().setRestrictProverExpressions(false);
		final Start startNode = parser.parse(testMachine, false);
		assertEquals(
				"machine(conjunct(none,[greater(none,identifier(none,a),integer(none,5)),substitution(none,assign(none,[identifier(none,b)],[identifier(none,a)]),less(none,identifier(none,b),integer(none,10)))])).",
			Helpers.getTreeAsPrologTerm(startNode));
	}

	@Test
	public void testNoPredicateSubstitutionsInNormalMode() {
		final String testMachine = "#PREDICATE ! a,5. (a=5 => a/=5 )";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}
}
