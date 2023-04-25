package de.be4.classicalb.core.parser.prettyprinter;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;

@RunWith(Parameterized.class)
public final class PrettyPrinterRenamingInvalidTest {
	private static final String[] TESTS = {
		"`1` + `1` - `1`",
		"`1` + `*` - `1`",
		"`1` + `*` - `a b`",
	};

	private final String expr;

	public PrettyPrinterRenamingInvalidTest(String expr) {
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
	
	// Suffix and literal renaming cannot handle such invalid identifiers
	// and we don't care about their precise behavior in this case.
}
