package de.be4.classicalb.core.parser.rules;

import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;

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
	public void testRulesMachineInOrdinaryMachineFileException() {
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.fullParsing("rules/project/RulesMachineInOrdinaryMachineFile.mch"));
	}
}
