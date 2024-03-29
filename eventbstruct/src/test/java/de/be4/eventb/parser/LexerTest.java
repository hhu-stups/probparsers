package de.be4.eventb.parser;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.junit.Test;

import de.be4.eventb.core.parser.BException;
import de.be4.eventb.core.parser.EventBParser;
import de.be4.eventb.core.parser.node.AInvariant;
import de.be4.eventb.core.parser.node.AMachineParseUnit;
import de.be4.eventb.core.parser.node.AVariant;
import de.be4.eventb.core.parser.node.PInvariant;
import de.be4.eventb.core.parser.node.Start;

public class LexerTest {
	@Test
	public void testStringLabeledElements() throws BException {
		final Start rootNode = new EventBParser().parse("machine Test invariants \n\t@inv1 asdf \n fdsa \n\t@inv2 qwer: \t rewq \nend");

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<PInvariant> invariants = parseUnit.getInvariants();

		AInvariant invariant = (AInvariant) invariants.get(0);

		// correct invariant label?
		assertEquals("inv1", invariant.getName().getText());
		// correct string representation for predicate?
		assertEquals("asdf \n fdsa", invariant.getPredicate().getText());

		invariant = (AInvariant) invariants.get(1);

		// correct invariant label?
		assertEquals("inv2", invariant.getName().getText());
		// correct string representation for predicate?
		assertEquals("qwer: \t rewq", invariant.getPredicate().getText());
	}

	@Test
	public void testStringVariant() throws BException {
		final Start rootNode = new EventBParser().parse("machine Test\nvariant y-x\nend");

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final AVariant variantClause = (AVariant) parseUnit.getVariant();

		assertEquals("y-x", variantClause.getExpression().getText());
	}

	@Test(expected = BException.class)
	public void testFormulaAtEof() throws BException {
		// Test that this doesn't give an OutOfMemoryError.
		new EventBParser().parse("machine Test\ninvariants\n\t@inv1 1=1");
	}
}
