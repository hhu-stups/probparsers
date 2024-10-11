package de.be4.classicalb.core.parser.aspects;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.AAddExpression;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.Start;
import de.hhu.stups.sablecc.patch.SourcePosition;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PositionAspectTest {
	@Test
	public void testSimpleNode() throws Exception {
		final String testExpression = "x";
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseExpression(testExpression);
		final PExpression expression = ((AExpressionParseUnit) startNode
				.getPParseUnit()).getExpression();

		final SourcePosition startPos = expression.getStartPos();
		final SourcePosition endPos = expression.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(1, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(2, endPos.getPos());
	}

	@Test
	public void testComposedNode() throws Exception {
		final String testExpression = "x+1";
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseExpression(testExpression);

		// test top node
		final PExpression expression = ((AExpressionParseUnit) startNode
				.getPParseUnit()).getExpression();
		SourcePosition startPos = expression.getStartPos();
		SourcePosition endPos = expression.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(1, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(4, endPos.getPos());

		// test left child: 1-2
		final PExpression leftExpr = ((AAddExpression) expression).getLeft();
		startPos = leftExpr.getStartPos();
		endPos = leftExpr.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(1, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(2, endPos.getPos());

		// test right child: 3-4
		final PExpression rightExpr = ((AAddExpression) expression).getRight();
		startPos = rightExpr.getStartPos();
		endPos = rightExpr.getEndPos();

		assertNotNull(startNode);
		assertNotNull(endPos);

		assertEquals(1, startPos.getLine());
		assertEquals(3, startPos.getPos());
		assertEquals(1, endPos.getLine());
		assertEquals(4, endPos.getPos());
	}
}
