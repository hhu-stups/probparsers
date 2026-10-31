package de.be4.classicalb.core.parser.rules;

import de.be4.classicalb.core.parser.exceptions.BParseException;
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
	public void testUnknownRuleInPredicateOperatorException() {
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
		Helpers.assertThrowsCompound(BParseException.class, () -> Helpers.parseFile("rules/project/RulesMachineInOrdinaryMachineFile.mch"));
	}

	@Test
	public void testUnknownIdentifierInReferencedMachine() {
		final String main = "RULES_MACHINE Main END";
		final String other = "RULES_MACHINE Other OPERATIONS RULE r BODY RULE_FAIL WHEN unknownId = 1 COUNTEREXAMPLE \"fail\" END END END";
		RulesProject project = new RulesProject();
		project.parseRulesMachines(main, other);
		project.checkAndTranslateProject();
		assertEquals(1, project.getBExceptionList().size());
		assertEquals("Unknown identifier 'unknownId'.", project.getBExceptionList().get(0).getMessage());
	}

	@Test
	public void testConflictingDefinitionsInDifferentMachines() {
		final String machine1 = "RULES_MACHINE M1 DEFINITIONS MAX == 10 OPERATIONS COMPUTATION c1 BODY DEFINE v1 VALUE MAX END END END";
		final String machine2 = "RULES_MACHINE M2 DEFINITIONS MAX == 20 OPERATIONS COMPUTATION c2 BODY DEFINE v2 VALUE MAX END END END";
		RulesProject project = new RulesProject();
		project.parseRulesMachines(machine1, machine2);
		project.checkAndTranslateProject();
		assertEquals(1, project.getBExceptionList().size());
		assertEquals("Duplicate definition 'MAX' is already included in a referenced machine.", project.getBExceptionList().get(0).getMessage());
	}

	@Test
	public void testLibraryDefinitionInDifferentMachines() {
		final String machine = "RULES_MACHINE M1 DEFINITIONS TO_STRING(sss) == \"0\"; EXTERNAL_FUNCTION_TO_STRING(TO_STRING_TYPE) == (TO_STRING_TYPE --> STRING); END";
		RulesProject project = new RulesProject();
		project.parseRulesMachines(machine, machine);
		project.checkAndTranslateProject();
		assertTrue(project.getBExceptionList().isEmpty());
	}

}
