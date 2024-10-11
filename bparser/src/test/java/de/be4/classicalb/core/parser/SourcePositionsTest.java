package de.be4.classicalb.core.parser;

import java.util.LinkedList;
import java.util.List;

import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AAddExpression;
import de.be4.classicalb.core.parser.node.ADescriptionExpression;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIfSubstitution;
import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.ASequenceSubstitution;
import de.be4.classicalb.core.parser.node.ASubstitutionParseUnit;
import de.be4.classicalb.core.parser.node.AVariablesMachineClause;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PMachineClause;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIntegerLiteral;
import de.hhu.stups.sablecc.patch.PositionedNode;
import de.hhu.stups.sablecc.patch.SourcePosition;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SourcePositionsTest {

	private BParser parser;

	// TODO This test case should go into the sablecc-stups project instead
	@Test
	public void testSourcePositionComparable() {
		SourcePosition p1 = new SourcePosition(1, 5);
		SourcePosition p2 = new SourcePosition(2, 10);
		SourcePosition p3 = new SourcePosition(3, 1);
		SourcePosition p4 = new SourcePosition(3, 11);
		
		assertEquals(0, p1.compareTo(p1));
		assertTrue(p1.compareTo(p2) < 0);
		assertTrue(p2.compareTo(p1) > 0);
		assertTrue(p1.compareTo(p3) < 0);
		assertTrue(p1.compareTo(p4) < 0);
		assertTrue(p2.compareTo(p3) < 0);
		assertTrue(p2.compareTo(p4) < 0);
		assertTrue(p3.compareTo(p4) < 0);
		
		
	}

	@Test
	public void testSimpleNode() throws Exception {
		String testExpression = "x";
		Start startNode = parser.parseExpression(testExpression);
		PExpression expression = ((AExpressionParseUnit) startNode.getPParseUnit()).getExpression();

		SourcePosition startPos = expression.getStartPos();
		SourcePosition endPos = expression.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(1, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(2, endPos.getPos());
	}

	@Test
	public void testComposedNode() throws Exception {
		String testExpression = "x+1";
		Start startNode = parser.parseExpression(testExpression);

		// test top node
		PExpression expression = ((AExpressionParseUnit) startNode.getPParseUnit()).getExpression();
		SourcePosition startPos = expression.getStartPos();
		SourcePosition endPos = expression.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(1, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(4, endPos.getPos());

		// test left child: 1-2
		PExpression leftExpr = ((AAddExpression) expression).getLeft();
		startPos = leftExpr.getStartPos();
		endPos = leftExpr.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(1, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(2, endPos.getPos());

		// test right child: 3-4
		PExpression rightExpr = ((AAddExpression) expression).getRight();
		startPos = rightExpr.getStartPos();
		endPos = rightExpr.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(3, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(4, endPos.getPos());
	}

	@Test
	public void testTokenAsPositionedNode() throws Exception {
		String testExpression = "xx + 5";
		Start result = parser.parseExpression(testExpression);
		final AExpressionParseUnit exprParseUnit = (AExpressionParseUnit) result.getPParseUnit();
		final AAddExpression addExpression = (AAddExpression) exprParseUnit.getExpression();
		final AIntegerExpression intExpression = (AIntegerExpression) addExpression.getRight();

		assertTrue(intExpression instanceof PositionedNode);
		assertNotNull(intExpression.getStartPos());
		assertNotNull(intExpression.getEndPos());

		final TIntegerLiteral intLiteral = intExpression.getLiteral();

		assertTrue(intLiteral instanceof PositionedNode);
		assertNotNull(((PositionedNode) intLiteral).getStartPos());
		assertNotNull(((PositionedNode) intLiteral).getEndPos());
	}

	@Test
	public void testAddExpression() throws Exception {
		String testExpression = "xx + 5";
		Start result = parser.parseExpression(testExpression);

		final AExpressionParseUnit exprParseUnit = (AExpressionParseUnit) result.getPParseUnit();

		final AAddExpression addExpression = (AAddExpression) exprParseUnit.getExpression();
		assertEquals(1, addExpression.getStartPos().getLine());
		assertEquals(1, addExpression.getStartPos().getPos());
		assertEquals(1, addExpression.getEndPos().getLine());
		assertEquals(7, addExpression.getEndPos().getPos());

		final AIdentifierExpression varExpression = (AIdentifierExpression) addExpression.getLeft();
		assertEquals(1, varExpression.getStartPos().getLine());
		assertEquals(1, varExpression.getStartPos().getPos());
		assertEquals(1, varExpression.getEndPos().getLine());
		assertEquals(3, varExpression.getEndPos().getPos());

		final AIntegerExpression intExpression = (AIntegerExpression) addExpression.getRight();
		assertEquals(1, intExpression.getStartPos().getLine());
		assertEquals(6, intExpression.getStartPos().getPos());
		assertEquals(1, intExpression.getEndPos().getLine());
		assertEquals(7, intExpression.getEndPos().getPos());
	}

	@Test
	public void testSequenceSubst() throws Exception {
		String testSubstitution = "skip; x:=5; skip";
		Start result = parser.parseSubstitution(testSubstitution);

		final ASubstitutionParseUnit substParseUnit = (ASubstitutionParseUnit) result.getPParseUnit();

		final ASequenceSubstitution sequenceSubst = (ASequenceSubstitution) substParseUnit.getSubstitution();
		assertEquals(1, sequenceSubst.getStartPos().getLine());
		assertEquals(1, sequenceSubst.getStartPos().getPos());
		assertEquals(1, sequenceSubst.getEndPos().getLine());
		assertEquals(testSubstitution.length() + 1, sequenceSubst.getEndPos().getPos());
	}

	@Test
	public void testMultilineSubst() throws Exception {
		String testSubstitution = "IF 1=1\n"
			+ "THEN skip\n"
			+ "ELSE skip\n"
			+ "END";
		Start result = parser.parseSubstitution(testSubstitution);

		final ASubstitutionParseUnit substParseUnit = (ASubstitutionParseUnit) result.getPParseUnit();

		AIfSubstitution ifSubstitution = (AIfSubstitution)substParseUnit.getSubstitution();
		assertEquals(1, ifSubstitution.getStartPos().getLine());
		assertEquals(1, ifSubstitution.getStartPos().getPos());
		assertEquals(4, ifSubstitution.getEndPos().getLine());
		assertEquals(4, ifSubstitution.getEndPos().getPos());
	}

	@Test
	public void testComment1() throws Exception {
		String testExpression = "xx /* comment */ + 5";
		Start result = parser.parseExpression(testExpression);

		final AExpressionParseUnit exprParseUnit = (AExpressionParseUnit) result.getPParseUnit();
		final AAddExpression addExpression = (AAddExpression) exprParseUnit.getExpression();
		final AIntegerExpression intExpression = (AIntegerExpression) addExpression.getRight();
		assertEquals(1, intExpression.getStartPos().getLine());
		assertEquals(20, intExpression.getStartPos().getPos());
		assertEquals(1, intExpression.getEndPos().getLine());
		assertEquals(testExpression.length() + 1, intExpression.getEndPos().getPos());
	}


	@Test
	public void testVariablesSourcePositions() throws Exception {
		final String testMachine = "MACHINE test\n" + "VARIABLES\n" + "  xx,\n" + "    yy\n"
				+ "INVARIANT xx:INT & yy:INT\n" + "INITIALISATION xx,yy:=0,0\n" + "END\n";
		final Start result = parser.parseMachine(testMachine);
		final AAbstractMachineParseUnit machine = (AAbstractMachineParseUnit) result.getPParseUnit();

		AVariablesMachineClause variables = null;
		for (final PMachineClause clause : machine.getMachineClauses()) {
			if (clause instanceof AVariablesMachineClause) {
				variables = (AVariablesMachineClause) clause;
				break;
			}
		}
		if (variables == null) {
			fail("variables clause not found");
		}
		final LinkedList<PExpression> ids = variables.getIdentifiers();
		assertEquals(2, ids.size());
		final AIdentifierExpression x = (AIdentifierExpression) ids.get(0);
		final AIdentifierExpression y = (AIdentifierExpression) ids.get(1);

		// VARIABLES block
		assertEquals(2, variables.getStartPos().getLine());
		assertEquals(1, variables.getStartPos().getPos());
		assertEquals(4, variables.getEndPos().getLine());
		assertEquals(7, variables.getEndPos().getPos());
		

		// variable x declaration
		assertEquals(3, x.getStartPos().getLine());
		assertEquals(3, x.getStartPos().getPos());
		assertEquals(3, x.getEndPos().getLine());
		assertEquals(5, x.getEndPos().getPos());
		
		// variable y declaration
		assertEquals(4, y.getStartPos().getLine());
		assertEquals(5, y.getStartPos().getPos());
		assertEquals(4, y.getEndPos().getLine());
		assertEquals(7, y.getEndPos().getPos());
	}

	@Test
	public void testVariableWithPragmaPositions() throws Exception {
		final String testMachine = "MACHINE SimpleDescPragma\n"
			+ "VARIABLES\n"
			+ "  x, // a variable without description\n"
			+ "  y /*@desc \"The y coordinate\" */,\n"
			+ "  z\n"
			+ "INVARIANT\n"
			+ " x+y+z = 0\n"
			+ "INITIALISATION\n"
			+ " x,y,z := 1,0,-1\n"
			+ "END";
		final Start result = parser.parseMachine(testMachine);
		final AAbstractMachineParseUnit machine = (AAbstractMachineParseUnit) result.getPParseUnit();

		AVariablesMachineClause variables = null;
		for (final PMachineClause clause : machine.getMachineClauses()) {
			if (clause instanceof AVariablesMachineClause) {
				variables = (AVariablesMachineClause) clause;
				break;
			}
		}
		if (variables == null) {
			fail("variables clause not found");
		}

		final List<PExpression> ids = variables.getIdentifiers();
		assertEquals(3, ids.size());
		final AIdentifierExpression x = (AIdentifierExpression) ids.get(0);
		final ADescriptionExpression yDesc = (ADescriptionExpression) ids.get(1);
		final AIdentifierExpression y = (AIdentifierExpression) yDesc.getExpression();
		final AIdentifierExpression z = (AIdentifierExpression) ids.get(2);

		// VARIABLES block
		assertEquals(2, variables.getStartPos().getLine());
		assertEquals(1, variables.getStartPos().getPos());
		assertEquals(5, variables.getEndPos().getLine());
		assertEquals(4, variables.getEndPos().getPos());

		// variable x
		assertEquals(3, x.getStartPos().getLine());
		assertEquals(3, x.getStartPos().getPos());
		assertEquals(3, x.getEndPos().getLine());
		assertEquals(4, x.getEndPos().getPos());

		// variable y with description
		assertEquals(4, yDesc.getStartPos().getLine());
		assertEquals(3, yDesc.getStartPos().getPos());
		assertEquals(4, yDesc.getEndPos().getLine());
		assertEquals(34, yDesc.getEndPos().getPos());

		// variable y itself
		assertEquals(4, y.getStartPos().getLine());
		assertEquals(3, y.getStartPos().getPos());
		assertEquals(4, y.getEndPos().getLine());
		// Ideally this should be column 4, but the grammar parses the identifier and the pragma together,
		// so the identifier gets the same end column as the entire pragma expression.
		assertEquals(34, y.getEndPos().getPos());

		// variable z
		assertEquals(5, z.getStartPos().getLine());
		assertEquals(3, z.getStartPos().getPos());
		assertEquals(5, z.getEndPos().getLine());
		assertEquals(4, z.getEndPos().getPos());
	}

	@Before
	public void setUp() throws Exception {
		parser = new BParser("testcase");
	}

	@After
	public void tearDown() throws Exception {
		parser = null;
	}
}
