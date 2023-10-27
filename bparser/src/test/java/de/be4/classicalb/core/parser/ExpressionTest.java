package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ExpressionTest {
	@Test
	public void testPower1() throws BCompoundException {
		final String testMachine = "#EXPRESSION 2**3**4";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(power_of(none,integer(none,2),power_of(none,integer(none,3),integer(none,4)))).",
				result);
	}

	@Test
	public void testPower2() throws BCompoundException {
		final String testMachine = "#EXPRESSION 2**3~";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(power_of(none,integer(none,2),reverse(none,integer(none,3)))).",
				result);
	}

	@Test
	public void testPred1() throws BCompoundException {
		final String testMachine = "#EXPRESSION pred(x)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(function(none,predecessor(none),[identifier(none,x)])).",
				result);
	}

	@Test
	public void testPred2() throws BCompoundException {
		final String testMachine = "#EXPRESSION pred";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals("machine(predecessor(none)).", result);
	}

	@Test
	public void testSucc1() throws BCompoundException {
		final String testMachine = "#EXPRESSION succ(x)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(function(none,successor(none),[identifier(none,x)])).",
				result);
	}

	@Test
	public void testSucc2() throws BCompoundException {
		final String testMachine = "#EXPRESSION succ";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals("machine(successor(none)).", result);
	}

	@Test
	public void testAddExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION xx.yy + 5";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(add(none,identifier(none,'xx.yy'),integer(none,5))).",
				result);
	}

	@Test
	public void testSubExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION 3 - 5";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(minus_or_set_subtract(none,integer(none,3),integer(none,5))).",
				result);
	}

	@Test
	public void testCoupleExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION (1, aa)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(couple(none,[integer(none,1),identifier(none,aa)])).",
				result);
	}

	@Test
	public void testCoupleExpression2() throws BCompoundException {
		final String testMachine = "#EXPRESSION (1, aa, bb)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(couple(none,[integer(none,1),identifier(none,aa),identifier(none,bb)])).",
				result);
	}

	@Test
	public void testQuantifiedUnionExpression() {
		final String testMachine = "#EXPRESSION UNION x.y.(x=0 | x )";
		assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testQuantifiedUnionExpression2() throws BCompoundException {

		final String testMachine = "#EXPRESSION UNION x,y . (x=0 & y=x | (x,y) )";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(quantified_union(none,[identifier(none,x),identifier(none,y)],conjunct(none,[equal(none,identifier(none,x),integer(none,0)),equal(none,identifier(none,y),identifier(none,x))]),couple(none,[identifier(none,x),identifier(none,y)]))).",
				result);
	}

	@Test
	public void testLambdaExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION % x . (x=0 | x )";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(lambda(none,[identifier(none,x)],equal(none,identifier(none,x),integer(none,0)),identifier(none,x))).",
				result);
	}

	@Test(expected = BCompoundException.class)
	public void testLambdaExpression2() throws BCompoundException {
		final String testMachine = "#EXPRESSION % x.y.z.(x.y.z=0 | x.y.z )";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testTotalRelationExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION A <<-> B";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(total_relation(none,identifier(none,'A'),identifier(none,'B'))).",
				result);
	}

	@Test
	public void testSurjectionRelationExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION A <->> B";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(surjection_relation(none,identifier(none,'A'),identifier(none,'B'))).",
				result);
	}

	@Test
	public void testTotalSurjectionRelationExpression() throws BCompoundException {
		final String testMachine = "#EXPRESSION A <<->> B";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(total_surjection_relation(none,identifier(none,'A'),identifier(none,'B'))).",
				result);
	}

	@Test
	public void testFunction1() throws BCompoundException {
		final String testMachine = "#EXPRESSION queues(co)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(function(none,identifier(none,queues),[identifier(none,co)])).",
				result);
	}

	@Test
	public void testFunction2() throws BCompoundException {
		final String testMachine = "#EXPRESSION (queues(co))(ii)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(function(none,function(none,identifier(none,queues),[identifier(none,co)]),[identifier(none,ii)])).",
				result);
	}

	@Test
	public void testString1() throws BCompoundException {
		final String testMachine = "#EXPRESSION \"Hello World\"";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals("machine(string(none,'Hello World')).", result);
	}

	@Test
	public void testString2() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION text:=\"Hello World\"";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(assign(none,[identifier(none,text)],[string(none,'Hello World')])).",
				result);
	}

	@Test
	public void testEmptySequence1() throws BCompoundException {
		final String testMachine = "#EXPRESSION [ ]";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals("machine(empty_sequence(none)).", result);
	}

	@Test
	public void testEmptySequence2() throws BCompoundException {
		final String testMachine1 = "#EXPRESSION < >";
		final String testMachine2 = "#EXPRESSION <>";
		final String testMachine3 = "#EXPRESSION []";
		final String result1 = Helpers.getMachineAsPrologTerm(testMachine1);
		final String result2 = Helpers.getMachineAsPrologTerm(testMachine2);
		final String result3 = Helpers.getMachineAsPrologTerm(testMachine3);

		assertEquals("machine(empty_sequence(none)).", result1);
		assertEquals(result1, result2);
		assertEquals(result1, result3);
		assertEquals(result2, result3);
	}

	@Test
	public void testImage1() throws BCompoundException {
		final String testMachine = "#EXPRESSION sex~[{woman}] - dom(husband)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(minus_or_set_subtract(none,image(none,reverse(none,identifier(none,sex)),set_extension(none,[identifier(none,woman)])),domain(none,identifier(none,husband)))).",
				result);
	}

	@Test
	public void testImage2() throws BCompoundException {
		final String testMachine = "#EXPRESSION {a |-> b}[{a}]";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(image(none,set_extension(none,[couple(none,[identifier(none,a),identifier(none,b)])]),set_extension(none,[identifier(none,a)]))).",
				result);
	}

	@Test
	public void testImage3() throws BCompoundException {
		final String testMachine = "#EXPRESSION {a |-> b, c |-> d} - {c |-> d}[{a}] - {b}";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(minus_or_set_subtract(none,minus_or_set_subtract(none,set_extension(none,[couple(none,[identifier(none,a),identifier(none,b)]),couple(none,[identifier(none,c),identifier(none,d)])]),image(none,set_extension(none,[couple(none,[identifier(none,c),identifier(none,d)])]),set_extension(none,[identifier(none,a)]))),set_extension(none,[identifier(none,b)]))).",
				result);
	}

	@Test
	public void testIntervalMinus() throws BCompoundException {
		final String testMachine = "#EXPRESSION 1..5-1";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(interval(none,integer(none,1),minus_or_set_subtract(none,integer(none,5),integer(none,1)))).",
				result);
	}

	@Test
	public void testCoupleMinus() throws BCompoundException {
		final String testMachine = "#EXPRESSION 1 |-> 5-1";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(couple(none,[integer(none,1),minus_or_set_subtract(none,integer(none,5),integer(none,1))])).",
				result);
	}

	@Test
	public void testPlusMinus() throws BCompoundException {
		final String testMachine = "#EXPRESSION 1 + 5 - 3";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(minus_or_set_subtract(none,add(none,integer(none,1),integer(none,5)),integer(none,3))).",
				result);
	}

	@Test
	public void testUnionMinus() throws BCompoundException {
		final String testMachine = "#EXPRESSION s1 - {x} \\/ {y}";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(union(none,minus_or_set_subtract(none,identifier(none,s1),set_extension(none,[identifier(none,x)])),set_extension(none,[identifier(none,y)]))).",
				result);
	}

	@Test
	public void testConcat() throws BCompoundException {
		final String result = Helpers.getMachineAsPrologTerm("#EXPRESSION s^t");
		assertEquals("machine(concat(none,identifier(none,s),identifier(none,t))).", result);
	}

	@Test
	public void testString() throws BCompoundException {
		final String result = Helpers.getMachineAsPrologTerm("#EXPRESSION \"test\"");
		assertEquals("machine(string(none,test)).", result);
	}

	@Test
	public void testEmptyString() throws BCompoundException {
		final String result = Helpers.getMachineAsPrologTerm("#EXPRESSION \"\"");
		assertEquals("machine(string(none,'')).", result);
	}

	@Test
	public void testComprehensionSets() throws BCompoundException {
		final String expected = "machine(comprehension_set(none,[identifier(none,i)],greater(none,identifier(none,i),integer(none,0)))).";
		final String standard = Helpers.getMachineAsPrologTerm("#EXPRESSION {i|i>0}");
		assertEquals(expected, standard);
	}

	@Test
	public void testProverComprehensionSets() {
		Helpers.assertThrowsCompound(BParseException.class, () -> new BParser().parseExpression("SET(i).(i>0)"));
	}

	@Test
	public void testRelationalImagePrio() throws BCompoundException {
		final String actual = Helpers.getMachineAsPrologTerm("#EXPRESSION c~[s]*x");
		final String expected = "machine(mult_or_cart(none,image(none,reverse(none,identifier(none,c)),identifier(none,s)),identifier(none,x))).";
		assertEquals(expected, actual);
	}

	@Test
	public void testLargeInteger() throws BCompoundException {
		final String actual = Helpers.getMachineAsPrologTerm("#EXPRESSION 922337203685477580756");
		final String expected = "machine(integer(none,922337203685477580756)).";
		assertEquals(expected, actual);
	}
}
