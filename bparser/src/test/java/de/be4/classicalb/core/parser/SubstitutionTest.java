package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SubstitutionTest {

	@Test
	public void testParallelAssignWithComposedId() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION xx.yy, aa.bb := 5, 3";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(assign(none,[identifier(none,'xx.yy'),identifier(none,'aa.bb')],[integer(none,5),integer(none,3)])).",
				result);
	}

	@Test
	public void testSimultaneousSubstitution() throws BCompoundException {
		final String testMachine = "MACHINE test OPERATIONS foo = skip || skip END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,test,[]),[operations(none,[operation(none,identifier(none,foo),[],[],parallel(none,[skip(none),skip(none)]))])])).",
				result);
	}

	@Test
	public void testParallelAssignWithNonIdentifier() {
		final String testMachine = "#SUBSTITUTION xx,yy,5  := 5, 3, zz";
		final BParseException e = Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		// final CheckException cause = (CheckException) e.getCause();
		// assertEquals(1, e.getNodes().length);
		// assertNotNull(e.getNodes()[0]);
	}

	@Test
	public void testRenamedIdentifierInAnySubstitution() {
		final String testMachine = "#SUBSTITUTION ANY x.y WHERE x.y = 1 THEN skip END ";
		assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testInvalidIdentifierListInAnySubstitution() {
		final String testMachine = "#SUBSTITUTION ANY (x|->y) WHERE x = 1 & y = 1 THEN skip END ";
		assertThrows(BCompoundException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
	}

	@Test
	public void testPreconditionBool() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION PRE 1=1 THEN skip END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(precondition(none,equal(none,integer(none,1),integer(none,1)),skip(none))).",
				result);
	}

	@Test
	public void testParallelList() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION skip || a:=b || x";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(parallel(none,[skip(none),assign(none,[identifier(none,a)],[identifier(none,b)]),operation_call(none,identifier(none,x),[],[])])).",
				result);
	}

	@Test
	public void testSequenceList() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION skip ; x ; y";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(sequence(none,[skip(none),operation_call(none,identifier(none,x),[],[]),operation_call(none,identifier(none,y),[],[])])).",
				result);
	}

	@Test
	public void testParallelAndSequence() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION skip || x ; y";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(sequence(none,[parallel(none,[skip(none),operation_call(none,identifier(none,x),[],[])]),operation_call(none,identifier(none,y),[],[])])).",
				result);
	}

	@Test
	public void testOperation1() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION op1;op2(x)";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(sequence(none,[operation_call(none,identifier(none,op1),[],[]),operation_call(none,identifier(none,op2),[],[identifier(none,x)])])).",
				result);
	}

	@Test
	public void testOperation2() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION function(x)(y)";
		Helpers.getMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testFunctionSubstitution() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION\nf(x) := y";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(assign(none,[function(none,identifier(none,f),[identifier(none,x)])],[identifier(none,y)])).",
				result);

	}

	@Test
	public void testMultiFunctionSubstitution() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION f(x),g(y),h := a,b,c";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(assign(none,[function(none,identifier(none,f),[identifier(none,x)]),function(none,identifier(none,g),[identifier(none,y)]),identifier(none,h)],[identifier(none,a),identifier(none,b),identifier(none,c)])).",
				result);

	}
}
