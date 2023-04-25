package de.be4.classicalb.core.parser.prettyprinter;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.IIdentifierRenaming;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.be4.classicalb.core.parser.util.SuffixIdentifierRenaming;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;

@RunWith(Parameterized.class)
public final class PrettyPrinterRenamingPlainTest {
	private static final String[] TESTS = {
		"a + a - a",
		"a + b - a",
		"a + b - c",
		"One /\\ One \\/ One",
		"One /\\ Two \\/ One",
		"One /\\ Two \\/ Three",
		"(x1a, x1a, x1a)",
		"(x1a, y2b, x1a)",
		"(x1a, y2b, z3c)",
		"MACHINE_2 * MACHINE_2 / MACHINE_2",
		"MACHINE_2 * MACHINE_1 / MACHINE_2",
		"MACHINE_2 * MACHINE_1 / MACHINE_3",
		"{max_1, max_1, max_1}",
		"{max_1, max_2, max_1}",
		"{max_1, max_2, max_3}",
	};

	private final String expr;

	public PrettyPrinterRenamingPlainTest(String expr) {
		this.expr = expr;
	}

	@Parameterized.Parameters(name = "{0}")
	public static String[] data() {
		return TESTS;
	}

	@Test
	public void testDefaultRenaming() throws BCompoundException {
		Start ast = new BParser().parseExpression(this.expr);
		PrettyPrinter pp = new PrettyPrinter();
		ast.apply(pp);
		
		Start ast2 = new BParser().parseExpression(pp.getPrettyPrint());
		PrettyPrinter pp2 = new PrettyPrinter();
		ast2.apply(pp2);

		Assert.assertEquals(Helpers.getTreeAsPrologTerm(ast), Helpers.getTreeAsPrologTerm(ast2));
		Assert.assertEquals(pp.getPrettyPrint(), pp2.getPrettyPrint());
	}

	@Test
	public void testSuffixRenaming() throws BCompoundException {
		Start ast = new BParser().parseExpression(this.expr);
		PrettyPrinter pp = new PrettyPrinter();
		pp.setRenaming(new SuffixIdentifierRenaming());
		ast.apply(pp);
		
		Start ast2 = new BParser().parseExpression(pp.getPrettyPrint());
		PrettyPrinter pp2 = new PrettyPrinter();
		pp2.setRenaming(new SuffixIdentifierRenaming());
		ast2.apply(pp2);

		Assert.assertEquals(Helpers.getTreeAsPrologTerm(ast), Helpers.getTreeAsPrologTerm(ast2));
		Assert.assertEquals(pp.getPrettyPrint(), pp2.getPrettyPrint());
	}

	@Test
	public void testLiteralRenaming() throws BCompoundException {
		Start ast = new BParser().parseExpression(this.expr);
		PrettyPrinter pp = new PrettyPrinter();
		pp.setRenaming(IIdentifierRenaming.LITERAL);
		ast.apply(pp);
		
		Start ast2 = new BParser().parseExpression(pp.getPrettyPrint());
		PrettyPrinter pp2 = new PrettyPrinter();
		pp2.setRenaming(IIdentifierRenaming.LITERAL);
		ast2.apply(pp2);

		Assert.assertEquals(Helpers.getTreeAsPrologTerm(ast), Helpers.getTreeAsPrologTerm(ast2));
		Assert.assertEquals(pp.getPrettyPrint(), pp2.getPrettyPrint());
	}
}
