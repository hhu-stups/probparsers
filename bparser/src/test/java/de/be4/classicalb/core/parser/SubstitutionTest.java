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
		String testSubstitution = "xx.yy, aa.bb := 5, 3";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

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
		String testMachine = "xx,yy,5  := 5, 3, zz";
		BParseException e = Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getSubstitutionAsPrologTerm(testMachine));
		// final CheckException cause = (CheckException) e.getCause();
		// assertEquals(1, e.getNodesList().size());
		// assertNotNull(e.getNodesList().get(0));
	}

	@Test
	public void testRenamedIdentifierInAnySubstitution() {
		String testSubstitution = "ANY x.y WHERE x.y = 1 THEN skip END ";
		assertThrows(BCompoundException.class, () -> Helpers.getSubstitutionAsPrologTerm(testSubstitution));
	}

	@Test
	public void testInvalidIdentifierListInAnySubstitution() {
		String testSubstitution = "ANY (x|->y) WHERE x = 1 & y = 1 THEN skip END ";
		assertThrows(BCompoundException.class, () -> Helpers.getSubstitutionAsPrologTerm(testSubstitution));
	}

	@Test
	public void testPreconditionBool() throws BCompoundException {
		String testSubstitution = "PRE 1=1 THEN skip END";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(precondition(none,equal(none,integer(none,1),integer(none,1)),skip(none))).",
				result);
	}

	@Test
	public void testParallelList() throws BCompoundException {
		String testSubstitution = "skip || a:=b || x";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(parallel(none,[skip(none),assign(none,[identifier(none,a)],[identifier(none,b)]),operation_call(none,identifier(none,x),[],[])])).",
				result);
	}

	@Test
	public void testSequenceList() throws BCompoundException {
		String testSubstitution = "skip ; x ; y";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(sequence(none,[skip(none),operation_call(none,identifier(none,x),[],[]),operation_call(none,identifier(none,y),[],[])])).",
				result);
	}

	@Test
	public void testParallelAndSequence() throws BCompoundException {
		String testSubstitution = "skip || x ; y";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(sequence(none,[parallel(none,[skip(none),operation_call(none,identifier(none,x),[],[])]),operation_call(none,identifier(none,y),[],[])])).",
				result);
	}

	@Test
	public void testOperation1() throws BCompoundException {
		String testSubstitution = "op1;op2(x)";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(sequence(none,[operation_call(none,identifier(none,op1),[],[]),operation_call(none,identifier(none,op2),[],[identifier(none,x)])])).",
				result);
	}

	@Test(expected = BCompoundException.class)
	public void testOperation2() throws BCompoundException {
		String testSubstitution = "function(x)(y)";
		Helpers.getSubstitutionAsPrologTerm(testSubstitution);
	}

	@Test
	public void testOperationQualified() throws BCompoundException {
		String testSubstitution = "M1.op1(x); M2.M3.M4.op2(y)";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
			"machine(sequence(none,[operation_call(none,identifier(none,'M1.op1'),[],[identifier(none,x)]),operation_call(none,identifier(none,'M2.M3.M4.op2'),[],[identifier(none,y)])])).",
			result
		);
	}

	@Test
	public void testFunctionSubstitution() throws BCompoundException {
		String testSubstitution = "f(x) := y";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(assign(none,[function(none,identifier(none,f),[identifier(none,x)])],[identifier(none,y)])).",
				result);

	}

	@Test
	public void testMultiFunctionSubstitution() throws BCompoundException {
		String testSubstitution = "f(x),g(y),h := a,b,c";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(assign(none,[function(none,identifier(none,f),[identifier(none,x)]),function(none,identifier(none,g),[identifier(none,y)]),identifier(none,h)],[identifier(none,a),identifier(none,b),identifier(none,c)])).",
				result);

	}
}
