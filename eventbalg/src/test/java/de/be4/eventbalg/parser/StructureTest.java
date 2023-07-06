package de.be4.eventbalg.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;

import org.junit.Test;

import de.be4.eventbalg.core.parser.BException;
import de.be4.eventbalg.core.parser.EventBParseException;
import de.be4.eventbalg.core.parser.EventBParser;
import de.be4.eventbalg.core.parser.lexer.LexerException;
import de.be4.eventbalg.core.parser.node.AEvent;
import de.be4.eventbalg.core.parser.node.AMachineParseUnit;
import de.be4.eventbalg.core.parser.node.AVariable;
import de.be4.eventbalg.core.parser.node.AWitness;
import de.be4.eventbalg.core.parser.node.PEvent;
import de.be4.eventbalg.core.parser.node.PVariable;
import de.be4.eventbalg.core.parser.node.PWitness;
import de.be4.eventbalg.core.parser.node.Start;
import de.be4.eventbalg.core.parser.node.TAt;

public class StructureTest {
	@Test
	public void testEventStructure() throws Exception {
		new EventBParser().parse("machine Test\nevents\n\nconvergent event test //comment\nend\nend");
	}

	@Test
	public void testOptionalConvergence() throws Exception {
		new EventBParser().parse("machine Test\nevents\n\nevent test //comment\nend\nend");
	}

	@Test
	public void testContextExtends() throws Exception {
		new EventBParser().parse("context Context2 extends Context1 end");
	}

	@Test
	public void testIdentifierTick() throws Exception {
		final Start root = new EventBParser().parse("machine Mac variables x' y end");
		final AMachineParseUnit parseUnit = (AMachineParseUnit) root
				.getPParseUnit();

		final LinkedList<PVariable> variables = parseUnit.getVariables();
		assertEquals(2, variables.size());

		assertEquals("x'", ((AVariable) variables.get(0)).getName().getText());
		assertEquals("y", ((AVariable) variables.get(1)).getName().getText());
	}

	@Test
	public void testWitnessTick() throws Exception {
		final Start root = new EventBParser().parse("machine WitnessTick\nevents\nevent Eve\nwith\n@x' x' :: NAT\nend\nend");
		final AMachineParseUnit parseUnit = (AMachineParseUnit) root
				.getPParseUnit();

		final LinkedList<PEvent> events = parseUnit.getEvents();
		assertEquals(1, events.size());

		final AEvent event = (AEvent) events.get(0);
		final LinkedList<PWitness> witnesses = event.getWitnesses();
		assertEquals(1, witnesses.size());

		assertEquals("x'", ((AWitness) witnesses.get(0)).getName().getText());
	}

	@Test
	public void testUnicodeIdentifiers1() throws Exception {
		new EventBParser().parse("context UnicodeIdentifiers1 constants \u00dcber \u00E6 m\u00e4h end");
	}

	@Test
	public void testUnicodeIdentifiers2() {
		try {
			new EventBParser().parse("context UnicodeIdentifiers2 constants \u00dcber @ end");
			fail("Expecting exception");
		} catch (final BException e) {
			final Exception cause = e.getCause();

			assertTrue(
					"Unexpected cause: " + e.getCause() + " - "
							+ e.getLocalizedMessage(),
					cause instanceof EventBParseException);
			assertTrue("Unexpected token: "
					+ ((EventBParseException) cause).getToken().getClass()
							.getSimpleName() + " - " + e.getLocalizedMessage(),
					((EventBParseException) cause).getToken() instanceof TAt);
		}
	}

	@Test(expected = LexerException.class)
	public void testUnicodeIdentifiers3() throws Exception {
		try {
			new EventBParser().parse("context UnicodeIdentifiers3 constants Über ' end");
			fail("Expecting exception");
		} catch (final BException e) {
			final Exception cause = e.getCause();
			throw cause;
		}
	}

	@Test
	public void testMissingAtMessage() {
		try {
			new EventBParser().parse("context MissingAtMessage axioms blub x:=1 end");
			fail("Expecting exception");
		} catch (final BException e) {
			final Exception cause = e.getCause();

			assertTrue(
					"Unexpected cause: " + e.getCause() + " - "
							+ e.getLocalizedMessage(),
					cause instanceof EventBParseException);

			final EventBParseException exception = (EventBParseException) cause;
			assertTrue("Message missing @", exception.getMessage()
					.contains("@"));
		}
	}
}
