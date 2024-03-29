package de.be4.classicalb.core.parser.prios;

import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static de.be4.classicalb.core.parser.analysis.ParseTestUtil.createTripleExpr;
import static de.be4.classicalb.core.parser.analysis.ParseTestUtil.createTripleExprLeft;
import static de.be4.classicalb.core.parser.analysis.ParseTestUtil.createTripleExprRight;
import static de.be4.classicalb.core.parser.analysis.ParseTestUtil.parseExpr;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TwoBinopAssoziativityTests {
	private final BinaryOperator op1;
	private final BinaryOperator op2;
	private final EAssoc assoc;

	public TwoBinopAssoziativityTests(final BinaryOperator op1, final BinaryOperator op2, final EAssoc assoc) {
		this.op1 = op1;
		this.op2 = op2;
		this.assoc = assoc;
	}

	@Parameterized.Parameters(name = "{0}, {1}")
	public static List<Object[]> getConfig() {
		List<BinaryOperator> binOps = BinaryOperator.OPS;
		final List<Object[]> ops = new ArrayList<>(binOps.size() * binOps.size());

		for (BinaryOperator op1 : binOps) {
			for (BinaryOperator op2 : binOps) {
				if (op1.getPriority() == op2.getPriority()
						&& op1.getAssociativity() == op2.getAssociativity()) {
					ops.add(new Object[] {op1, op2, op1.getAssociativity()});
				}
			}
		}

		return ops;
	}

	@Test
	public void testAssociativity() throws BCompoundException {
		String s1 = op1.getSymbol();
		String s2 = op2.getSymbol();
		final String expr = createTripleExpr(s1, s2);
		final String left = createTripleExprLeft(s1, s2);
		final String right = createTripleExprRight(s1, s2);

		final String pExpr = parseExpr(expr);
		final String pLeft = parseExpr(left);
		final String pRight = parseExpr(right);

		final boolean isLeft = pExpr.equals(pLeft);
		final boolean isRight = pExpr.equals(pRight);
		assertTrue(" must be either left and right assoziative",
				isLeft != isRight);
		switch (assoc) {
		case LEFT:
			assertTrue("expected to be left assoziative", isLeft);
			break;
		case RIGHT:
			assertTrue("expected to be right assoziative", isRight);
			break;
		}
	}
}
