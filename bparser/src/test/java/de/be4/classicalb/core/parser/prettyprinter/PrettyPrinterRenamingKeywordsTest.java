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
public final class PrettyPrinterRenamingKeywordsTest {
	private static final String[][] TESTS = {
		{"`MACHINE` + `MACHINE` - `MACHINE`", "MACHINE_1+MACHINE_1-MACHINE_1", "MACHINE+MACHINE-MACHINE"},
		{"`MACHINE` + `REFINEMENT` - `MACHINE`", "MACHINE_1+REFINEMENT_1-MACHINE_1", "MACHINE+REFINEMENT-MACHINE"},
		{"`MACHINE` + `REFINEMENT` - `IMPLEMENTATION`", "MACHINE_1+REFINEMENT_1-IMPLEMENTATION_1", "MACHINE+REFINEMENT-IMPLEMENTATION"},
		{"`MACHINE` + MACHINE_1 - MACHINE_2", "MACHINE_1+MACHINE_1_1-MACHINE_2", "MACHINE+MACHINE_1-MACHINE_2"},
		{"MACHINE_1 + `MACHINE` - MACHINE_2", "MACHINE_1+MACHINE_2-MACHINE_2_1", "MACHINE_1+MACHINE-MACHINE_2"},
		{"MACHINE_1 + MACHINE_2 - `MACHINE`", "MACHINE_1+MACHINE_2-MACHINE_3", "MACHINE_1+MACHINE_2-MACHINE"},
		{"`max` /\\ `max` \\/ `max`", "max_1/\\max_1\\/max_1", "max/\\max\\/max"},
		{"`max` /\\ `min` \\/ `max`", "max_1/\\min_1\\/max_1", "max/\\min\\/max"},
		{"`max` /\\ `min` \\/ `mod`", "max_1/\\min_1\\/mod_1", "max/\\min\\/mod"},
		{"`max` /\\ max_1 \\/ max_2", "max_1/\\max_1_1\\/max_2", "max/\\max_1\\/max_2"},
		{"max_1 /\\ `max` \\/ max_2", "max_1/\\max_2\\/max_2_1", "max_1/\\max\\/max_2"},
		{"max_1 /\\ max_2 \\/ `max`", "max_1/\\max_2\\/max_3", "max_1/\\max_2\\/max"},
	};

	private final String expr;
	private final String suffixedPP;
	private final String literalPP;

	public PrettyPrinterRenamingKeywordsTest(String expr, String suffixedPP, String literalPP) {
		this.expr = expr;
		this.suffixedPP = suffixedPP;
		this.literalPP = literalPP;
	}

	@Parameterized.Parameters(name = "{0}")
	public static String[][] data() {
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
		Assert.assertEquals(this.suffixedPP, pp.getPrettyPrint());

		Start ast2 = new BParser().parseExpression(pp.getPrettyPrint());
		PrettyPrinter pp2 = new PrettyPrinter();
		pp2.setRenaming(new SuffixIdentifierRenaming());
		ast2.apply(pp2);
		Assert.assertEquals(this.suffixedPP, pp2.getPrettyPrint());
	}

	@Test
	public void testLiteralRenaming() throws BCompoundException {
		Start ast = new BParser().parseExpression(this.expr);
		PrettyPrinter pp = new PrettyPrinter();
		pp.setRenaming(IIdentifierRenaming.LITERAL);
		ast.apply(pp);
		Assert.assertEquals(this.literalPP, pp.getPrettyPrint());
	}
}
