package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

public class PredicatesTest {
	@Test
	public void testAndOrPrio() throws BCompoundException {
		String testPredicate1 = "1=1 or 1=1 & 1=2";
		String result1 = Helpers.getPredicateAsPrologTerm(testPredicate1);
		String testPredicate2 = "(1=1 or 1=1) & 1=2";
		String result2 = Helpers.getPredicateAsPrologTerm(testPredicate2);

		assertEquals(result1, result2);
	}

	@Test
	public void testParallelMember() throws BCompoundException {
		String testPredicate = "x : ID & y : ID";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);

		assertEquals(
				"machine(conjunct(none,[member(none,identifier(none,x),identifier(none,'ID')),member(none,identifier(none,y),identifier(none,'ID'))])).",
				result);
	}

	@Test
	public void testParallelBelongs2() {
		String testPredicate = "x,y : ID";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getPredicateAsPrologTerm(testPredicate));
	}

	@Test
	public void testInvariant1() throws BCompoundException {
		String testPredicate = "hasread : READER <-> BOOK & reading : READER >+> COPY & (reading ; copyof) /\\ hasread = {}";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);

		assertEquals(
				"machine(conjunct(none,[member(none,identifier(none,hasread),relations(none,identifier(none,'READER'),identifier(none,'BOOK'))),member(none,identifier(none,reading),partial_injection(none,identifier(none,'READER'),identifier(none,'COPY'))),equal(none,intersection(none,composition(none,identifier(none,reading),identifier(none,copyof)),identifier(none,hasread)),empty_set(none))])).",
				result);
	}

	@Test
	public void testForall() throws BCompoundException {
		String testPredicate = "! a,b. (a=b => a/=b )";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);
		assertEquals(
				"machine(forall(none,[identifier(none,a),identifier(none,b)],implication(none,equal(none,identifier(none,a),identifier(none,b)),not_equal(none,identifier(none,a),identifier(none,b))))).",
				result);
	}

	@Test
	public void testForallCouple1() throws BCompoundException {
		String testPredicate = "! (a,b). (1=1 )";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);
		assertEquals(
				"machine(forall(none,[identifier(none,a),identifier(none,b)],equal(none,integer(none,1),integer(none,1)))).",
				result);
	}

	@Test(expected = BCompoundException.class)
	public void testTooManyparentheses() throws BCompoundException {
		String testPredicate = "# ((b,c,d)). ( b > c)";
		Helpers.getPredicateAsPrologTerm(testPredicate);
	}

	@Test
	public void testExampleThesis1() throws BCompoundException {
		String testPredicate = "4 < 5 & 6 >= 7";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);
		assertEquals(
				"machine(conjunct(none,[less(none,integer(none,4),integer(none,5)),greater_equal(none,integer(none,6),integer(none,7))])).",
				result);
	}

	@Test
	public void testMultiCompositions() throws BCompoundException {
		String testPredicate = "(p~;F;p) = G";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);
		assertEquals(
				"machine(equal(none,composition(none,composition(none,reverse(none,identifier(none,p)),identifier(none,'F')),identifier(none,p)),identifier(none,'G'))).",
				result);
	}

	@Test
	public void testWithComposition() throws BCompoundException {
		String testPredicate = "(dom(ff ; (gg~)) <: dom(ff))";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);
		assertEquals(
				"machine(subset(none,domain(none,composition(none,identifier(none,ff),reverse(none,identifier(none,gg)))),domain(none,identifier(none,ff)))).",
				result);
	}

	@Test
	public void testEqualVsImplication() throws BCompoundException {
		String testPredicate = "1=2 <=> 3=4 => 5=6";
		String result = Helpers.getPredicateAsPrologTerm(testPredicate);
		assertEquals(
				"machine(implication(none,equivalence(none,equal(none,integer(none,1),integer(none,2)),equal(none,integer(none,3),integer(none,4))),equal(none,integer(none,5),integer(none,6)))).",
				result);
	}

	@Test
	public void testEqualVsImplicationFormula() throws BCompoundException {
		String testFormula = "1=2 <=> 3=4 => 5=6";
		String result = Helpers.getFormulaAsPrologTerm(testFormula);
		assertEquals(
				"machine(implication(none,equivalence(none,equal(none,integer(none,1),integer(none,2)),equal(none,integer(none,3),integer(none,4))),equal(none,integer(none,5),integer(none,6)))).",
				result);
	}

	@Test
	public void testBFalse() throws BCompoundException {
		String actual = Helpers.getPredicateAsPrologTerm("bfalse");
		String expected = "machine(falsity(none)).";
		assertEquals(expected, actual);
	}

	@Test
	public void testBTrue() throws BCompoundException {
		String actual = Helpers.getPredicateAsPrologTerm("btrue");
		String expected = "machine(truth(none)).";
		assertEquals(expected, actual);
	}

	@Test
	public void testNonIdentifiersInQuantification() {
		String testPredicate = "! a,5. (a=5 => a/=5 )";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getPredicateAsPrologTerm(testPredicate));
	}

	@Test
	public void testNonIdentifiersInQuantificationFormula() {
		String testFormula = "! a,5. (a=5 => a/=5 )";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getFormulaAsPrologTerm(testFormula));
	}

	@Test
	public void testSubstitutionInPredicate() throws BCompoundException {
		String testPredicate = "(a>5) & [b:=a](b<10)";
		assertEquals(
				"machine(conjunct(none,[greater(none,identifier(none,a),integer(none,5)),substitution(none,assign(none,[identifier(none,b)],[identifier(none,a)]),less(none,identifier(none,b),integer(none,10)))])).",
			Helpers.getPredicateAsPrologTerm(testPredicate));
	}

	@Test
	public void testNoPredicateSubstitutionsInNormalMode() {
		String testPredicate = "! a,5. (a=5 => a/=5 )";
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getPredicateAsPrologTerm(testPredicate));
	}
}
