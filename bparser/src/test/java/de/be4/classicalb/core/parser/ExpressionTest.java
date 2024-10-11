package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ExpressionTest {
	@Test
	public void testPower1() throws BCompoundException {
		String testExpression = "2**3**4";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(power_of(none,integer(none,2),power_of(none,integer(none,3),integer(none,4)))).",
				result);
	}

	@Test
	public void testPower2() throws BCompoundException {
		String testExpression = "2**3~";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(power_of(none,integer(none,2),reverse(none,integer(none,3)))).",
				result);
	}

	@Test
	public void testPred1() throws BCompoundException {
		String testExpression = "pred(x)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(function(none,predecessor(none),[identifier(none,x)])).",
				result);
	}

	@Test
	public void testPred2() throws BCompoundException {
		String testExpression = "pred";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals("machine(predecessor(none)).", result);
	}

	@Test
	public void testSucc1() throws BCompoundException {
		String testExpression = "succ(x)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(function(none,successor(none),[identifier(none,x)])).",
				result);
	}

	@Test
	public void testSucc2() throws BCompoundException {
		String testExpression = "succ";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals("machine(successor(none)).", result);
	}

	@Test
	public void testAddExpression() throws BCompoundException {
		String testExpression = "xx.yy + 5";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(add(none,identifier(none,'xx.yy'),integer(none,5))).",
				result);
	}

	@Test
	public void testSubExpression() throws BCompoundException {
		String testExpression = "3 - 5";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(minus_or_set_subtract(none,integer(none,3),integer(none,5))).",
				result);
	}

	@Test
	public void testCoupleExpression() throws BCompoundException {
		String testExpression = "(1, aa)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(couple(none,[integer(none,1),identifier(none,aa)])).",
				result);
	}

	@Test
	public void testCoupleExpression2() throws BCompoundException {
		String testExpression = "(1, aa, bb)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(couple(none,[integer(none,1),identifier(none,aa),identifier(none,bb)])).",
				result);
	}

	@Test
	public void testQuantifiedUnionExpression() {
		String testExpression = "UNION x.y.(x=0 | x )";
		assertThrows(BCompoundException.class, () -> Helpers.getExpressionAsPrologTerm(testExpression));
	}

	@Test
	public void testQuantifiedUnionExpression2() throws BCompoundException {
		String testExpression = "UNION x,y . (x=0 & y=x | (x,y) )";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);
		assertEquals(
				"machine(quantified_union(none,[identifier(none,x),identifier(none,y)],conjunct(none,[equal(none,identifier(none,x),integer(none,0)),equal(none,identifier(none,y),identifier(none,x))]),couple(none,[identifier(none,x),identifier(none,y)]))).",
				result);
	}

	@Test
	public void testLambdaExpression() throws BCompoundException {
		String testExpression = "% x . (x=0 | x )";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);
		assertEquals(
				"machine(lambda(none,[identifier(none,x)],equal(none,identifier(none,x),integer(none,0)),identifier(none,x))).",
				result);
	}

	@Test(expected = BCompoundException.class)
	public void testLambdaExpression2() throws BCompoundException {
		String testExpression = "% x.y.z.(x.y.z=0 | x.y.z )";
		Helpers.getExpressionAsPrologTerm(testExpression);
	}

	@Test
	public void testTotalRelationExpression() throws BCompoundException {
		String testExpression = "A <<-> B";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(total_relation(none,identifier(none,'A'),identifier(none,'B'))).",
				result);
	}

	@Test
	public void testSurjectionRelationExpression() throws BCompoundException {
		String testExpression = "A <->> B";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(surjection_relation(none,identifier(none,'A'),identifier(none,'B'))).",
				result);
	}

	@Test
	public void testTotalSurjectionRelationExpression() throws BCompoundException {
		String testExpression = "A <<->> B";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(total_surjection_relation(none,identifier(none,'A'),identifier(none,'B'))).",
				result);
	}

	@Test
	public void testFunction1() throws BCompoundException {
		String testExpression = "queues(co)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(function(none,identifier(none,queues),[identifier(none,co)])).",
				result);
	}

	@Test
	public void testFunction2() throws BCompoundException {
		String testExpression = "(queues(co))(ii)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(function(none,function(none,identifier(none,queues),[identifier(none,co)]),[identifier(none,ii)])).",
				result);
	}

	@Test
	public void testString1() throws BCompoundException {
		String testExpression = "\"Hello World\"";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals("machine(string(none,'Hello World')).", result);
	}

	@Test
	public void testString2() throws BCompoundException {
		String testSubstitution = "text:=\"Hello World\"";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(assign(none,[identifier(none,text)],[string(none,'Hello World')])).",
				result);
	}

	@Test
	public void testEmptySequence1() throws BCompoundException {
		String testExpression = "[ ]";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals("machine(empty_sequence(none)).", result);
	}

	@Test
	public void testEmptySequence2() throws BCompoundException {
		String testMachine1 = "< >";
		String testMachine2 = "<>";
		String testMachine3 = "[]";
		String result1 = Helpers.getExpressionAsPrologTerm(testMachine1);
		String result2 = Helpers.getExpressionAsPrologTerm(testMachine2);
		String result3 = Helpers.getExpressionAsPrologTerm(testMachine3);

		assertEquals("machine(empty_sequence(none)).", result1);
		assertEquals(result1, result2);
		assertEquals(result1, result3);
		assertEquals(result2, result3);
	}

	@Test
	public void testImage1() throws BCompoundException {
		String testExpression = "sex~[{woman}] - dom(husband)";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(minus_or_set_subtract(none,image(none,reverse(none,identifier(none,sex)),set_extension(none,[identifier(none,woman)])),domain(none,identifier(none,husband)))).",
				result);
	}

	@Test
	public void testImage2() throws BCompoundException {
		String testExpression = "{a |-> b}[{a}]";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(image(none,set_extension(none,[couple(none,[identifier(none,a),identifier(none,b)])]),set_extension(none,[identifier(none,a)]))).",
				result);
	}

	@Test
	public void testImage3() throws BCompoundException {
		String testExpression = "{a |-> b, c |-> d} - {c |-> d}[{a}] - {b}";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(minus_or_set_subtract(none,minus_or_set_subtract(none,set_extension(none,[couple(none,[identifier(none,a),identifier(none,b)]),couple(none,[identifier(none,c),identifier(none,d)])]),image(none,set_extension(none,[couple(none,[identifier(none,c),identifier(none,d)])]),set_extension(none,[identifier(none,a)]))),set_extension(none,[identifier(none,b)]))).",
				result);
	}

	@Test
	public void testIntervalMinus() throws BCompoundException {
		String testExpression = "1..5-1";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(interval(none,integer(none,1),minus_or_set_subtract(none,integer(none,5),integer(none,1)))).",
				result);
	}

	@Test
	public void testCoupleMinus() throws BCompoundException {
		String testExpression = "1 |-> 5-1";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(couple(none,[integer(none,1),minus_or_set_subtract(none,integer(none,5),integer(none,1))])).",
				result);
	}

	@Test
	public void testPlusMinus() throws BCompoundException {
		String testExpression = "1 + 5 - 3";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(minus_or_set_subtract(none,add(none,integer(none,1),integer(none,5)),integer(none,3))).",
				result);
	}

	@Test
	public void testUnionMinus() throws BCompoundException {
		String testExpression = "s1 - {x} \\/ {y}";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals(
				"machine(union(none,minus_or_set_subtract(none,identifier(none,s1),set_extension(none,[identifier(none,x)])),set_extension(none,[identifier(none,y)]))).",
				result);
	}

	@Test
	public void testConcat() throws BCompoundException {
		String result = Helpers.getExpressionAsPrologTerm("s^t");
		assertEquals("machine(concat(none,identifier(none,s),identifier(none,t))).", result);
	}

	@Test
	public void testString() throws BCompoundException {
		String result = Helpers.getExpressionAsPrologTerm("\"test\"");
		assertEquals("machine(string(none,test)).", result);
	}

	@Test
	public void testEmptyString() throws BCompoundException {
		String result = Helpers.getExpressionAsPrologTerm("\"\"");
		assertEquals("machine(string(none,'')).", result);
	}

	@Test
	public void testComprehensionSets() throws BCompoundException {
		String expected = "machine(comprehension_set(none,[identifier(none,i)],greater(none,identifier(none,i),integer(none,0)))).";
		String standard = Helpers.getExpressionAsPrologTerm("{i|i>0}");
		assertEquals(expected, standard);
	}

	@Test
	public void testProverComprehensionSets() {
		Helpers.assertThrowsCompound(BParseException.class, () -> new BParser().parseExpression("SET(i).(i>0)"));
	}

	@Test
	public void testRelationalImagePrio() throws BCompoundException {
		String actual = Helpers.getExpressionAsPrologTerm("c~[s]*x");
		String expected = "machine(mult_or_cart(none,image(none,reverse(none,identifier(none,c)),identifier(none,s)),identifier(none,x))).";
		assertEquals(expected, actual);
	}

	@Test
	public void testLargeInteger() throws BCompoundException {
		String actual = Helpers.getExpressionAsPrologTerm("922337203685477580756");
		String expected = "machine(integer(none,922337203685477580756)).";
		assertEquals(expected, actual);
	}
}
