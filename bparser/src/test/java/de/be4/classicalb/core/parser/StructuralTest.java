package de.be4.classicalb.core.parser;

import java.util.LinkedList;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PMachineClause;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StructuralTest {

	@Test
	public void testEmptyMachine() throws Exception {
		final String testMachine = "MACHINE SimplyStructure END";

		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(testMachine);

		final AAbstractMachineParseUnit machine = (AAbstractMachineParseUnit) startNode.getPParseUnit();

		final AMachineHeader header = (AMachineHeader) machine.getHeader();
		assertEquals("Machine name not as expected", "SimplyStructure", header.getName().get(0).getText());
		assertNotNull("Machine header parameter list is null", header.getParameters());
		assertTrue("More machine header parameters than expected", header.getParameters().isEmpty());

		final LinkedList<PMachineClause> machineClauses = machine.getMachineClauses();
		assertNotNull("Machine clause list is null", machineClauses);
		assertTrue("More machine clauses than expected", machineClauses.isEmpty());
	}

	@Test
	public void testShebang() throws BCompoundException {
		final String testMachine = "#! /Users/leuschel/git_root/prob_prolog/probcli \n MACHINE SheBang \n END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertNotNull(result);
	}

	@Test(expected = BCompoundException.class)
	public void testWrongPositionedShebang() throws BCompoundException {
		final String testMachine = "\n#! /Users/leuschel/git_root/prob_prolog/probcli \n MACHINE SheBang \n END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);
		assertNotNull(result);
	}

	@Test
	public void testWhiteSpaces() throws BCompoundException {
		final String testMachine = "MACHINE \tSimplyStructure END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals("machine(abstract_machine(none,machine(none),machine_header(none,'SimplyStructure',[]),[])).", result);
	}

	@Test
	public void testIncludesClause() throws BCompoundException {
		final String testMachine = "MACHINE SimplyStructure INCLUDES MachineA, MachineB (aa, bb, MAXINT, cc(dd)) END";
		final String result = Helpers.getMachineAsPrologTerm(testMachine);

		assertEquals(
				"machine(abstract_machine(none,machine(none),machine_header(none,'SimplyStructure',[]),[includes(none,[machine_reference(none,'MachineA',[]),machine_reference(none,'MachineB',[identifier(none,aa),identifier(none,bb),max_int(none),function(none,identifier(none,cc),[identifier(none,dd)])])])])).",
				result);
	}

	@Test
	public void testVariablesClause() throws Exception {
		final String testMachine = "MACHINE SimplyStructure\nVARIABLES aa, b, Cc\nINVARIANT aa : NAT & b : NAT & Cc : NAT\nINITIALISATION aa:=1 || b:=2 || c:=3\nEND";

		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(testMachine);
		assertNotNull(startNode);

		// TODO more tests
	}

	@Test
	public void testConstantsClause() throws Exception {
		final String testMachine = "MACHINE SimplyStructure\nCONSTANTS dd, e, Ff\nPROPERTIES dd : BOOL\nEND";

		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(testMachine);
		assertNotNull(startNode);

		// TODO more tests
	}

	@Test
	public void testSetsClause() throws Exception {
		final String testMachine = "MACHINE SimplyStructure SETS GGG; Hhh; JJ = {dada, dudu, TUTUT}; iII; kkk = {LLL} END";

		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(testMachine);
		assertNotNull(startNode);

		// TODO more tests
	}

	@Test
	public void testClause1() throws Exception {
		final String testMachine = "MACHINE SimplyStructure END";

		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(testMachine);
		assertNotNull(startNode);

		// TODO more tests
	}

	@Test
	public void testClause2() throws BCompoundException {
		String testClause = "VARIABLES xx, Ab, cD";
		String result = Helpers.getMachineClauseAsPrologTerm(testClause);

		assertEquals(
				"machine(variables(none,[identifier(none,xx),identifier(none,'Ab'),identifier(none,cD)])).",
				result);

		String testClause2 = "ABSTRACT_VARIABLES xx, Ab, cD";
		String result2 = Helpers.getMachineClauseAsPrologTerm(testClause2);

		assertEquals(result, result2);
	}

	@Test
	public void testClause3() throws BCompoundException {
		String testClause = "INCLUDES MachineA, MachineB (aa, bb, MAXINT, cc(dd))";
		String result = Helpers.getMachineClauseAsPrologTerm(testClause);

		assertEquals(
				"machine(includes(none,[machine_reference(none,'MachineA',[]),machine_reference(none,'MachineB',[identifier(none,aa),identifier(none,bb),max_int(none),function(none,identifier(none,cc),[identifier(none,dd)])])])).",
				result);
	}

	@Test
	public void testClausesStructure() throws Exception {
		final String testMachine = "MACHINE SimplyStructure\n" + "VARIABLES aa, b, Cc\n" + "INVARIANT aa : NAT\n"
				+ "INITIALISATION aa:=1\n" + "CONSTANTS dd, e, Ff\n" + "PROPERTIES dd : NAT\n"
				+ "SETS GGG; Hhh; JJ = {dada, dudu, TUTUT}; iII; kkk = {LLL}\nEND";

		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(testMachine);
		assertNotNull(startNode);

		// TODO more tests
	}

	@Test
	public void testRefinement1() throws BCompoundException {
		final String emptyMachine = "REFINEMENT RefinementMachine \nREFINES Machine \nEND";
		final String result = Helpers.getMachineAsPrologTerm(emptyMachine);

		assertEquals("machine(refinement_machine(none,machine_header(none,'RefinementMachine',[]),'Machine',[])).", result);
	}

	@Test
	public void testImplementation1() throws BCompoundException {
		final String emptyMachine = "IMPLEMENTATION ImplMachine \nREFINES Machine \nEND";
		final String result = Helpers.getMachineAsPrologTerm(emptyMachine);

		assertEquals("machine(implementation_machine(none,machine_header(none,'ImplMachine',[]),'Machine',[])).", result);
	}

	@Test
	public void testUnclosedComment() {
		final String emptyMachine = "MACHINE ClassicalB\n SETS pp ; qq\n /* CONSTANTS ccc,ddd\n VARIABLES xxx,yyy\n OPERATIONS\n  op1 = BEGIN xxx := 1; v <-- op2(2) END;\n  op2 = ANY q WHERE q : NAT THEN yyy := ccc END\nEND";
		final BLexerException ex = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(emptyMachine));
		// checking the start position of the comment
		assertEquals(3, ex.getLastLine());
		assertEquals(2, ex.getLastPos());
		assertTrue(ex.getMessage().contains("Comment not closed."));
	}

	@Test
	public void testUnclosedUnknownPragma() {
		String testMachine = "MACHINE ClassicalB\n SETS pp ; qq\n /*@supercalifragilisticexpialidocious CONSTANTS ccc,ddd\n VARIABLES xxx,yyy\n OPERATIONS\n  op1 = BEGIN xxx := 1; v <-- op2(2) END;\n  op2 = ANY q WHERE q : NAT THEN yyy := ccc END\nEND";
		BLexerException ex = Helpers.assertThrowsCompound(BLexerException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		// checking the start position of the comment
		assertEquals(3, ex.getLastLine());
		assertEquals(2, ex.getLastPos());
		assertTrue(ex.getMessage().contains("Comment not closed."));
	}

	@Test
	public void testList1() throws BCompoundException {
		String testSubstitution = "IF 1=1 THEN skip ELSIF 1=2 THEN skip ELSIF 1=3 THEN skip END";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(if(none,equal(none,integer(none,1),integer(none,1)),skip(none),[if_elsif(none,equal(none,integer(none,1),integer(none,2)),skip(none)),if_elsif(none,equal(none,integer(none,1),integer(none,3)),skip(none))],skip(none))).",
				result);
	}

	@Test
	public void testList2() throws BCompoundException {
		String testSubstitution = "a, b := 1, 2";
		String result = Helpers.getSubstitutionAsPrologTerm(testSubstitution);

		assertEquals(
				"machine(assign(none,[identifier(none,a),identifier(none,b)],[integer(none,1),integer(none,2)])).",
				result);
	}

	@Test
	public void checkForMissingSemicolon() {
		String s = "MACHINE MissingSemicolon\nOPERATIONS\n Foo=BEGIN skip END\n  BAR= BEGIN r := xx END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(s));
		Node node = e.getNodesList().get(0);
		assertEquals(4, node.getStartPos().getLine());
		assertEquals(3, node.getStartPos().getPos());
		assertTrue(e.getMessage().contains("Semicolon missing"));
	}

	@Test
	public void checkForInvalidSemicolon() {
		String s = "MACHINE MissingSemicolon\nOPERATIONS\n Foo=BEGIN skip END\n;\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(s));
		Node node = e.getNodesList().get(0);
		assertEquals(4, node.getStartPos().getLine());
		assertEquals(1, node.getStartPos().getPos());
		assertTrue(e.getMessage().contains("Invalid semicolon after last operation"));
	}

	@Test
	public void checkForInvalidSemicolonBeforeEnd() {
		String s = "MACHINE MissingSemicolon\nOPERATIONS\n Foo=BEGIN skip\n; END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(s));
		Node node = e.getNodesList().get(0);
		assertEquals(4, node.getStartPos().getLine());
		assertEquals(1, node.getStartPos().getPos());
		assertTrue(e.getMessage().contains("Invalid semicolon after last substitution"));
	}

	@Test
	public void checkForInvalidSemicolonBeforeEnd2() {
		String s = "MACHINE MissingSemicolon\nOPERATIONS\n Foo=BEGIN skip;skip\n; END\nEND";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(s));
		Node node = e.getNodesList().get(0);
		assertEquals(4, node.getStartPos().getLine());
		assertEquals(1, node.getStartPos().getPos());
		assertTrue(e.getMessage().contains("Invalid semicolon after last substitution"));
	}

	@Test
	public void testRepeatingClauses() {
		final String testMachine = "MACHINE TestMachineX\n" + "VARIABLES a,b,c\n" + "CONSTANTS X,Y,Z\n"
				+ "VARIABLES x,y,z\n" + "END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(2, e.getNodesList().size());
	}

	@Test
	public void testMissingProperties() {
		final String testMachine = "MACHINE TestMachineX\n" + "CONSTANTS X,Y,Z\n" + "END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> Helpers.getMachineAsPrologTerm(testMachine));
		assertEquals(1, e.getNodesList().size());
		assertEquals("Clause(s) missing: PROPERTIES", e.getMessage());
	}

	@Test
	public void testHexLiterals() throws BCompoundException {
		String testExpression = "0x12";
		String result = Helpers.getExpressionAsPrologTerm(testExpression);

		assertEquals("machine(integer(none,18)).", result);
	}
}
