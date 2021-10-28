package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BParseException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class SubstitutionTest {

	@Test
	public void testParallelAssignWithComposedId() throws Exception {
		final String testMachine = "#SUBSTITUTION xx.yy, aa.bb := 5, 3";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(AAssignSubstitution([AIdentifierExpression([xx,yy]),AIdentifierExpression([aa,bb])],[AIntegerExpression(5),AIntegerExpression(3)])))",
				result);
	}

	@Test
	public void testSimultaneousSubstitution() throws Exception {
		final String testMachine = "MACHINE test OPERATIONS foo = skip || skip END";
		final String result = Helpers.getTreeAsString(testMachine);
		assertEquals(
				"Start(AAbstractMachineParseUnit(AMachineHeader([test],[]),[AOperationsMachineClause([AOperation([],[foo],[],AParallelSubstitution([ASkipSubstitution(),ASkipSubstitution()]))])]))",
				result);
	}

	@Test
	public void testParallelAssignWithNonIdentifier() {
		final String testMachine = "#SUBSTITUTION xx,yy,5  := 5, 3, zz";
		final BParseException e = Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.getTreeAsString(testMachine));
		// final CheckException cause = (CheckException) e.getCause();
		// assertEquals(1, e.getNodes().length);
		// assertNotNull(e.getNodes()[0]);
	}

	@Test
	public void testRenamedIdentifierInAnySubstitution() {
		final String testMachine = "#SUBSTITUTION ANY x.y WHERE x.y = 1 THEN skip END ";
		assertThrows(BCompoundException.class, () -> Helpers.getTreeAsString(testMachine));
	}

	@Test
	public void testInvalidIdentifierListInAnySubstitution() throws BCompoundException {
		final String testMachine = "#SUBSTITUTION ANY (x|->y) WHERE x = 1 & y = 1 THEN skip END ";
		assertThrows(BCompoundException.class, () -> Helpers.getTreeAsString(testMachine));
	}

	@Test
	public void testPreconditionBool() throws Exception {
		final String testMachine = "#SUBSTITUTION PRE 1=1 THEN skip END";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(APreconditionSubstitution(AEqualPredicate(AIntegerExpression(1),AIntegerExpression(1)),ASkipSubstitution())))",
				result);
	}

	@Test
	public void testParallelList() throws Exception {
		final String testMachine = "#SUBSTITUTION skip || a:=b || x";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(AParallelSubstitution([ASkipSubstitution(),AAssignSubstitution([AIdentifierExpression([a])],[AIdentifierExpression([b])]),AOpSubstitution(AIdentifierExpression([x]),[])])))",
				result);
	}

	@Test
	public void testSequenceList() throws Exception {
		final String testMachine = "#SUBSTITUTION skip ; x ; y";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(ASequenceSubstitution([ASkipSubstitution(),AOpSubstitution(AIdentifierExpression([x]),[]),AOpSubstitution(AIdentifierExpression([y]),[])])))",
				result);
	}

	@Test
	public void testParallelAndSequence() throws Exception {
		final String testMachine = "#SUBSTITUTION skip || x ; y";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(ASequenceSubstitution([AParallelSubstitution([ASkipSubstitution(),AOpSubstitution(AIdentifierExpression([x]),[])]),AOpSubstitution(AIdentifierExpression([y]),[])])))",
				result);
	}

	@Test
	public void testOperation1() throws Exception {
		final String testMachine = "#SUBSTITUTION op1;op2(x)";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(ASequenceSubstitution([AOpSubstitution(AIdentifierExpression([op1]),[]),AOpSubstitution(AIdentifierExpression([op2]),[AIdentifierExpression([x])])])))",
				result);
	}

	@Test
	public void testOperation2() throws Exception {
		final String testMachine = "#SUBSTITUTION function(x)(y)";
		Helpers.getTreeAsString(testMachine);
	}

	@Test
	public void testFunctionSubstitution() throws Exception {
		final String testMachine = "#SUBSTITUTION\nf(x) := y";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(AAssignSubstitution([AFunctionExpression(AIdentifierExpression([f]),[AIdentifierExpression([x])])],[AIdentifierExpression([y])])))",
				result);

	}

	@Test
	public void testMultiFunctionSubstitution() throws Exception {
		final String testMachine = "#SUBSTITUTION f(x),g(y),h := a,b,c";
		final String result = Helpers.getTreeAsString(testMachine);

		assertEquals(
				"Start(ASubstitutionParseUnit(AAssignSubstitution([AFunctionExpression(AIdentifierExpression([f]),[AIdentifierExpression([x])]),AFunctionExpression(AIdentifierExpression([g]),[AIdentifierExpression([y])]),AIdentifierExpression([h])],[AIdentifierExpression([a]),AIdentifierExpression([b]),AIdentifierExpression([c])])))",
				result);

	}
}
