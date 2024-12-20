package de.be4.eventb.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.StringTokenizer;

import org.junit.Test;

import de.be4.eventb.core.parser.BException;
import de.be4.eventb.core.parser.EventBParseException;
import de.be4.eventb.core.parser.EventBParser;
import de.be4.eventb.core.parser.node.AAction;
import de.be4.eventb.core.parser.node.AEvent;
import de.be4.eventb.core.parser.node.AInvariant;
import de.be4.eventb.core.parser.node.AMachineParseUnit;
import de.be4.eventb.core.parser.node.AVariable;
import de.be4.eventb.core.parser.node.PAction;
import de.be4.eventb.core.parser.node.PInvariant;
import de.be4.eventb.core.parser.node.PVariable;
import de.be4.eventb.core.parser.node.Start;
import de.be4.eventb.core.parser.node.TComment;

public class CommentTest {
	@Test
	public void testCommentPredicates1() throws Exception {
		final Start rootNode = new EventBParser().parse("machine CommentPredicates1 invariants\n @inv1 asdf //MyComment\nend");

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<PInvariant> invariants = parseUnit.getInvariants();
		final AInvariant invariant = (AInvariant) invariants.get(0);

		// correct comment content?
		assertEquals("MyComment", invariant.getComments().get(0).getText());
	}

	@Test
	public void testCommentPredicates2() throws Exception {
		final Start rootNode = new EventBParser().parse("machine CommentPredicates2 invariants\n @inv0 asdf\n @inv1 asdf\n//MyComment\nend");

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<PInvariant> invariants = parseUnit.getInvariants();
		assertEquals(2, invariants.size());

		AInvariant invariant = (AInvariant) invariants.get(0);
		assertEquals(0, invariant.getComments().size());
		assertEquals("inv0", invariant.getName().getText());
		assertEquals("asdf", invariant.getPredicate().getText());

		// correct comment content, i.e., is whitespace in the beginning
		// omitted?
		invariant = (AInvariant) invariants.get(1);
		final LinkedList<TComment> comments = invariant.getComments();
		assertEquals(1, comments.size());
		assertEquals("MyComment", comments.get(0).getText());
		assertEquals("inv1", invariant.getName().getText());
		assertEquals("asdf", invariant.getPredicate().getText());
	}

	@Test
	public void testInvariantsAndMultiComments() throws BException {
		final String input = "machine InvariantsAndMultiComments invariants\n"
				+ "@inv1 1=1\n" + "@inv2 2=2\n" + "/*inv2\ncomment*/\n" + "end";
		final Start rootNode = new EventBParser().parse(input);

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<PInvariant> invariants = parseUnit.getInvariants();
		assertEquals(2, invariants.size());

		AInvariant invariant = (AInvariant) invariants.get(0);
		assertEquals(0, invariant.getComments().size());
		assertEquals("inv1", invariant.getName().getText());
		assertEquals("1=1", invariant.getPredicate().getText());

		invariant = (AInvariant) invariants.get(1);
		final LinkedList<TComment> comments = invariant.getComments();
		assertEquals(1, comments.size());
		assertEquals("inv2\ncomment", comments.get(0).getText());
		assertEquals("inv2", invariant.getName().getText());
		assertEquals("2=2", invariant.getPredicate().getText());
	}

	@Test
	public void testMultiLineComment() throws Exception {
		final Start rootNode = new EventBParser().parse("machine MultiLineComment invariants @inv1 asdf\n/* First line\n  Second line*/\nend");

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<PInvariant> invariants = parseUnit.getInvariants();
		final AInvariant invariant = (AInvariant) invariants.get(0);

		// correct comment content?
		final LinkedList<TComment> comments = invariant.getComments();
		assertEquals(1, comments.size());

		final StringTokenizer tokenizer = new StringTokenizer(comments.get(0)
				.getText(), "\n\r");

		assertEquals(2, tokenizer.countTokens());
		assertEquals("First line", tokenizer.nextToken());
		assertEquals("  Second line", tokenizer.nextToken());

		// correct invariant label?
		assertEquals("inv1", invariant.getName().getText());
		// correct string representation for predicate?
		assertEquals("asdf", invariant.getPredicate().getText());
	}

	@Test
	public void testCommentVariables1() throws Exception {
		final Start rootNode = new EventBParser().parse(
			"machine CommentVariables1 variables\n"
			+ "varA"
			+ "//varA comment\n"
			+ " varB\n"
			+ "varC\n"
			+ "/*varC\ncomment*/"
			+ "end"
		);

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<PVariable> variables = parseUnit.getVariables();

		assertEquals(3, variables.size());

		AVariable variable = (AVariable) variables.get(0);
		assertEquals("varA", variable.getName().getText());
		assertEquals("varA comment", variable.getComments().get(0).getText());

		variable = (AVariable) variables.get(1);
		assertEquals("varB", variable.getName().getText());
		assertEquals(0, variable.getComments().size());

		variable = (AVariable) variables.get(2);
		assertEquals("varC", variable.getName().getText());

		final LinkedList<TComment> comments = variable.getComments();
		assertNotNull(comments);
		assertEquals(1, comments.size());

		final StringTokenizer tokenizer = new StringTokenizer(comments.get(0)
				.getText(), "\n\r");
		assertEquals(2, tokenizer.countTokens());
		assertEquals("varC", tokenizer.nextToken());
		assertEquals("comment", tokenizer.nextToken());
	}

	@Test
	public void testMultiLineMachineComment() throws Exception {
		final Start rootNode = new EventBParser().parse(
			"machine MultiLineMachineComment"
			+ "/*\n"
			+ " comment\n"
			+ " in multiple\n"
			+ " lines\n"
			+ "*/\n"
			+ " end"
		);

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final String comments = parseUnit.getComments().get(0).getText();

		final StringTokenizer tokenizer = new StringTokenizer(comments, "\n\r");

		assertEquals(3, tokenizer.countTokens());

		assertEquals(" comment", tokenizer.nextToken());
		assertEquals(" in multiple", tokenizer.nextToken());
		assertEquals(" lines", tokenizer.nextToken());
	}

	@Test
	public void testAtSignInComment() throws Exception {
		final Start rootNode = new EventBParser().parse("machine AtSignInComment\nevents\nevent testEvent\nthen\n@act1 skip\n@act2 skip\n// MyComment@act2\nend\nend");

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final AEvent event = (AEvent) parseUnit.getEvents().get(0);
		final LinkedList<PAction> actions = event.getActions();

		AAction labeledAction = (AAction) actions.get(0);
		assertEquals("act1", labeledAction.getName().getText());
		assertEquals("skip", labeledAction.getAction().getText());
		assertEquals(0, labeledAction.getComments().size());

		labeledAction = (AAction) actions.get(1);
		assertEquals("act2", labeledAction.getName().getText());
		assertEquals("skip", labeledAction.getAction().getText());
		assertNotNull(labeledAction.getComments());
		assertEquals("MyComment@act2", labeledAction.getComments().get(0)
				.getText());
	}

	@Test
	public void testMultipleComments1() throws Exception {
		final Start rootNode = new EventBParser().parse(
			"machine MultipleComments1"
			+ "// line1\n"
			+ "/* line2\nline3*/"
			+ "// line4\n"
			+ "\nend"
		);

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<TComment> comments = parseUnit.getComments();

		assertEquals(3, comments.size());
		assertEquals("line1", comments.get(0).getText());
		assertEquals("line2\nline3", comments.get(1).getText());
		assertEquals("line4", comments.get(2).getText());
	}

	@Test
	public void testMultipleComments2() throws Exception {
		final Start rootNode = new EventBParser().parse(
			"machine MultipleComments2"
			+ "/* line1*/\n"
			+ "/* line2\nline3*/"
			+ "// line4\n"
			+ "\nend"
		);

		final AMachineParseUnit parseUnit = (AMachineParseUnit) rootNode
				.getPParseUnit();
		final LinkedList<TComment> comments = parseUnit.getComments();

		assertEquals(3, comments.size());
		assertEquals("line1", comments.get(0).getText());
		assertEquals("line2\nline3", comments.get(1).getText());
		assertEquals("line4", comments.get(2).getText());
	}

	@Test
	public void testCommentAtBeginErrorMessage() {
		try {
			new EventBParser().parse("/* Comment before machine header */\nmachine CommentAtBeginErrorMessage end");
			fail("Expecting exception here");
		} catch (final BException e) {
			final Exception cause = e.getCause();
			assertTrue(cause instanceof EventBParseException);
			assertTrue(((EventBParseException) cause).getToken() instanceof TComment);
			assertEquals(EventBParser.MSG_COMMENT_PLACEMENT, cause.getMessage());
		}
	}

	@Test
	public void testMisplacedCommentMessage() {
		try {
			new EventBParser().parse("machine MisplacedCommentMessage refines X\n/* Comment before machine header */\n variant 5\nend");
			fail("Expecting exception here");
		} catch (final BException e) {
			final Exception cause = e.getCause();
			assertTrue(
					"Unexpected cause: " + e.getCause() + " - " + e.getMessage(),
					cause instanceof EventBParseException);
			assertTrue(
					"Unexpected token: " + ((EventBParseException) cause).getToken().getClass().getSimpleName() + " - " + e.getMessage(),
					((EventBParseException) cause).getToken() instanceof TComment);
			assertEquals(
					"Unexpected message: " + e.getMessage() + " - " + e.getMessage(),
					EventBParser.MSG_COMMENT_PLACEMENT,
					cause.getMessage());
		}
	}
}
