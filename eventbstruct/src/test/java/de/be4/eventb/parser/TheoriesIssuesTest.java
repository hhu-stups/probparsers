package de.be4.eventb.parser;

import java.util.List;

import de.be4.eventb.core.parser.EventBParser;
import de.be4.eventb.core.parser.node.AAxiom;
import de.be4.eventb.core.parser.node.AContextParseUnit;
import de.be4.eventb.core.parser.node.PAxiom;
import de.be4.eventb.core.parser.node.Start;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TheoriesIssuesTest {
	@Test
	public void testCommentPredicates1() throws Exception {
		final Start rootNode = new EventBParser().parse("context C4 \naxioms\n@axm2 {1↦1,2↦2} = seqAppend({1↦1},2)\nend");

		final AContextParseUnit parseUnit = (AContextParseUnit) rootNode
				.getPParseUnit();
		List<PAxiom> axioms = parseUnit.getAxioms();
		final AAxiom axiom = (AAxiom) axioms.get(0);

		assertEquals("{1↦1,2↦2} = seqAppend({1↦1},2)", axiom.getPredicate()
				.getText());
	}
}
