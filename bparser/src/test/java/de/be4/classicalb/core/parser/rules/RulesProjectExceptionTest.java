package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RulesProjectExceptionTest {

	@Test
	public void testDuplicateOperationNameException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END; COMPUTATION foo BODY skip END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Duplicate operation name: 'foo'.", e.getMessage());
	}

	@Test
	public void testDependsOnRuleIsNotARuleException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo DEPENDS_ON_RULE bar BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END; COMPUTATION bar BODY skip END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Operation 'bar' is not a RULE operation.", e.getMessage());
	}

	@Test
	public void testUnkownRuleInPredicateOperatorException() {
		final String testMachine = "RULES_MACHINE test DEFINITIONS GOAL == FAILED_RULE(foo) END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Unknown rule 'foo'.", e.getMessage());
	}

	@Test
	public void testUnknownFunction() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo BODY VAR x IN x <--Foo(1) END;RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Unknown FUNCTION name 'Foo'", e.getMessage());
	}

	@Test
	public void testWritingDefineVariable() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION foo BODY DEFINE v1 TYPE POW(INTEGER) VALUE {1} END; v1 := {2} END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Identifier 'v1' is not a local variable (VAR). Hence, it can not be assigned here.", e.getMessage());
	}

	@Test
	public void testRulesMachineInOrdinaryMachineFileException() throws Exception {
		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			@Override
			public String toString() {
				return this.string.toString();
			}
		};
		PrintStream pStream = new PrintStream(output);
		ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setPrologOutput(true);
		BParser bParser = new BParser("");
		bParser.fullParsing(new File(this.getClass().getClassLoader().getResource("rules/project/RulesMachineInOrdinaryMachineFile.mch").toURI()),
				parsingBehaviour, pStream, pStream);
		assertTrue(output.toString().contains("parse_exception"));
	}
}
