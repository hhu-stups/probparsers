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
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class PriorityTests {
	final BinaryOperator lower, higher;

	public PriorityTests(final BinaryOperator lower, final BinaryOperator higher) {
		this.lower = lower;
		this.higher = higher;
	}

	@Parameterized.Parameters(name = "{0}, {1}")
	public static List<BinaryOperator[]> getConfig() {
		List<BinaryOperator> binOps = BinaryOperator.OPS;
		final List<BinaryOperator[]> ops = new ArrayList<>(binOps.size() * binOps.size());

		for (BinaryOperator op1 : binOps) {
			for (BinaryOperator op2 : binOps) {
				if (op1.getPriority() < op2.getPriority()) {
					ops.add(new BinaryOperator[] {op1, op2});
				}
			}
		}

		return ops;
	}

	@Test
	public void testPriority() throws BCompoundException {
		final String symL = lower.getSymbol();
		final String symH = higher.getSymbol();
		final String exprA = createTripleExpr(symH, symL);
		final String exprB = createTripleExpr(symL, symH);
		final String expectedA = createTripleExprLeft(symH, symL);
		final String expectedB = createTripleExprRight(symL, symH);

		final String pExprA = parseExpr(exprA);
		final String pExprB = parseExpr(exprB);
		final String pExpectedA = parseExpr(expectedA);
		final String pExpectedB = parseExpr(expectedB);

		assertEquals(pExpectedA, pExprA);
		assertEquals(pExpectedB, pExprB);
	}

}
