package de.be4.classicalb.core.parser.prios;

import java.util.List;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static de.be4.classicalb.core.parser.analysis.ParseTestUtil.parseExpr;
import static de.be4.classicalb.core.parser.prios.BinaryOperator.createTripleExpr;
import static de.be4.classicalb.core.parser.prios.BinaryOperator.createTripleExprLeft;
import static de.be4.classicalb.core.parser.prios.BinaryOperator.createTripleExprRight;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class OneBinopAssoziativityTests {
	private final String op1;
	private final String op2;
	private final EAssoc associativity;

	public OneBinopAssoziativityTests(final BinaryOperator op) {
		this(op.getSymbol(), op.getSymbol(), op.getAssociativity());
	}

	private OneBinopAssoziativityTests(final String op1, final String op2,
			final EAssoc associativity) {
		this.op1 = op1;
		this.op2 = op2;
		this.associativity = associativity;
	}

	@Parameterized.Parameters(name = "{0}")
	public static List<BinaryOperator> data() {
		return BinaryOperator.OPS;
	}

	@Test
	public void testAssociativity() throws BCompoundException {
		final String expr = createTripleExpr(op1, op2);
		final String left = createTripleExprLeft(op1, op2);
		final String right = createTripleExprRight(op1, op2);

		final String pExpr = parseExpr(expr);
		final String pLeft = parseExpr(left);
		final String pRight = parseExpr(right);

		final boolean isLeft = pExpr.equals(pLeft);
		final boolean isRight = pExpr.equals(pRight);
		assertTrue(" must be either left and right assoziative",
				isLeft != isRight);
		switch (associativity) {
		case LEFT:
			assertTrue("expected to be left assoziative", isLeft);
			break;
		case RIGHT:
			assertTrue("expected to be right assoziative", isRight);
			break;
		}
	}
}
