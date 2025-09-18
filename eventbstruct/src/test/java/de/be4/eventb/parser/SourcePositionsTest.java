package de.be4.eventb.parser;

import java.util.List;

import de.be4.eventb.core.parser.EventBParser;
import de.be4.eventb.core.parser.node.AMachineParseUnit;
import de.be4.eventb.core.parser.node.PVariable;
import de.be4.eventb.core.parser.node.Start;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SourcePositionsTest {

	EventBParser parser = new EventBParser();

	@Before
	public void setUp() throws Exception {
		parser = new EventBParser();
	}

	@After
	public void tearDown() throws Exception {
		parser = null;
	}

	@Test
	public void testGetEndLine() throws Exception {
		final Start root = parser.parse(
				"machine\nTestMachine\n\nvariables\nx\n\nend", false);

		final AMachineParseUnit parseUnit = (AMachineParseUnit) root
				.getPParseUnit();
		assertEquals(7, parseUnit.getEndPos().getLine());

		List<PVariable> variables = parseUnit.getVariables();
		assertEquals(5, variables.get(0).getEndPos()
				.getLine());
	}
}
