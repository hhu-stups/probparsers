package de.be4.classicalb.core.parser.rules;

import java.util.ArrayList;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.AOperatorExpression;
import de.be4.classicalb.core.parser.node.AOperatorPredicate;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.TKwExpressionOperator;
import de.be4.classicalb.core.parser.node.TKwPredicateOperator;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RulesLanguageExceptionTest {

	@Test
	public void testMachineParameterException() {
		final String testMachine = "RULES_MACHINE Test(a) CONSTRAINTS a : INTEGER END";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		Helpers.assertParseErrorLocation(e, 1, 15, 1, 21);
		assertEquals("A RULES_MACHINE must not have any machine parameters", e.getMessage());
	}

	@Test
	public void testRenamedMachineNameException() {
		final String testMachine = "RULES_MACHINE Test.Foo END";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		Helpers.assertParseErrorLocation(e, 1, 15, 1, 23);
		assertEquals("Renaming of a RULES_MACHINE name is not allowed.", e.getMessage());
	}

	@Test
	public void testOperatorFailedRulesErrorTypeWithSecondArgumentNotAnIntegerLiteral() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE_ERROR_TYPE(rule, 1+1) OPERATIONS RULE rule BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END  END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The second argument of FAILED_RULE_ERROR_TYPE must be an integer literal.", e.getMessage());
	}

	@Test
	public void testChoiceSubstitutionException() {
		final String testMachine = "RULES_MACHINE Test INITIALISATION CHOICE RULE_FAIL COUNTEREXAMPLE \"never\" END OR RULE_FAIL COUNTEREXAMPLE \"never\" END END  END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("A CHOICE substitution is not allowed in a RULES_MACHINE.", e.getMessage());
	}

	@Test
	public void testGoalIsNotAPredicateException() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == skip END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The GOAL definition must be a predicate.", e.getMessage());
	}

	@Test
	public void testGoalIsNotAPredicate2Exception() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == 1 END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The GOAL definition must be a predicate.", e.getMessage());
	}

	@Test
	public void testNoNumberOfErrorTypesDefinedException() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo BODY RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"abc\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The error type exceeded the number of error types specified for this rule operation.", e.getMessage());
	}

	@Test
	public void testErrorTypeIsNotImplemented() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"abc\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Error type '1' is not implemented in rule 'foo'.", e.getMessage());
	}

	@Test
	public void testErrorTypeIsNotImplemented2() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		String result = RulesUtil.getRulesMachineAsPrologTerm(testMachine);
	}

	@Test
	public void testInvalidRuleTag() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo TAGS 1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Expected identifier or string after the TAGS attribute.", e.getMessage());
	}

	@Test
	public void testErrorTypeZeroException() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 0 COUNTEREXAMPLE \"abc\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The ERROR_TYPE must be a natural number greater than zero.", e.getMessage());
	}

	@Test
	public void testRuleForallUsedOutsideOfARuleOperationException() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION foo BODY RULE_FORALL x WHERE x : 1..3 EXPECT 1=1 COUNTEREXAMPLE \"Fail\"END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("RULE_FORALL used outside of a RULE operation.", e.getMessage());
	}
	
	
	@Test
	public void testDefineUsedInRuleException() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION foo BODY FOR i IN {1} DO DEFINE x TYPE POW(INTEGER) VALUE {i} END END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("A DEFINE substitution must not be contained in a loop substitution.", e.getMessage());
	}

	@Test
	public void testRuleFailUsedOutsideOfARuleOperationException() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION foo BODY RULE_FAIL x WHEN x : 1..3 COUNTEREXAMPLE \"Fail\"END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("RULE_FAIL used outside of a RULE operation.", e.getMessage());
	}

	@Test
	public void testErrorTypeExceededTheNumberOfErrorTypesException() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 3 COUNTEREXAMPLE \"abc\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The error type exceeded the number of error types specified for this rule operation.", e.getMessage());
	}

	@Test(expected = AssertionError.class)
	public void testUnkownPredicateOperatorException() throws Exception {
		AOperatorPredicate operator = new AOperatorPredicate(new TKwPredicateOperator("foo"),
				new ArrayList<PExpression>());
		RulesMachineChecker rulesMachineVisitor = new RulesMachineChecker(null, null, null, null);
		operator.apply(rulesMachineVisitor);
	}

	@Test(expected = AssertionError.class)
	public void testUnkownExpressionOperatorException() throws Exception {
		AOperatorExpression operator = new AOperatorExpression(new TKwExpressionOperator("foo"),
				new ArrayList<PExpression>());
		RulesMachineChecker rulesMachineVisitor = new RulesMachineChecker(null, null, null, null);
		operator.apply(rulesMachineVisitor);
	}

	@Test
	public void testSucceededRuleOperatorWrongNumberOfArgumentsException() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == SUCCEEDED_RULE(1,2) END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The SUCCEEDED_RULE predicate operator expects exactly one rule identifier.", e.getMessage());
	}

	@Test
	public void testSucceededRuleErrorTypeOperatorWrongNumberOfArgumentsException() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == SUCCEEDED_RULE_ERROR_TYPE(1,2,3) END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The SUCCEEDED_RULE_ERROR_TYPE predicate operator expects exactly two arguments.", e.getMessage());
	}

	@Test
	public void testFailedRuleOperatorWrongNumberOfArgumentsException() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE(1,2) END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The FAILED_RULE predicate operator expects exactly one rule identifier.", e.getMessage());
	}

	@Test
	public void testFailedRuleErrorTypeOperatorWrongNumberOfArgumentsException() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE_ERROR_TYPE(1,2,3) END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The FAILED_RULE_ERROR_TYPE predicate operator expects exactly two arguments.", e.getMessage());
	}

	@Test
	public void testNotCheckedRuleWrongNumberOfArgumentsException() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == NOT_CHECKED_RULE(1,2) END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The NOT_CHECKED_RULE predicate operator expects exactly one rule identifier.", e.getMessage());
	}

	@Test
	public void testDisabledRuleWrongNumberOfArgumentsException() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == DISABLED_RULE(1,2) END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The DISABLED_RULE predicate operator expects exactly one rule identifier.", e.getMessage());
	}

	@Test
	public void testRenamingIdentifierException() {
		final String testMachine = "RULES_MACHINE test PROPERTIES a.b = 1 END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Identifier renaming is not allowed in a RULES_MACHINE.", e.getMessage());
	}

	@Test
	public void testAssignToANonIdentifierException() {
		final String testMachine = "RULES_MACHINE test INITIALISATION f(1) := 1 END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("There must be an identifier on the left side of the assign substitution. A function assignment 'f(1) := 1' is also not permitted.", e.getMessage());
	}

	@Test
	public void testRenamingOperationCallsException() {
		final String testMachine = "RULES_MACHINE test INITIALISATION f <-- Foo.bar END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Renaming of operation names is not allowed.", e.getMessage());
	}

	@Test
	public void testActivationClauseIsUsedMoreThanOnceException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo ACTIVATION 1=1 ACTIVATION 1=1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("ACTIVATION clause is used more than once in operation 'foo'.", e.getMessage());
	}

	@Test
	public void testPreconditionInRuleOperationException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo PRECONDITION 1=1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("PRECONDITION clause is not allowed for a RULE or COMPUTATION operation.", e.getMessage());
	}

	@Test
	public void testPreconditionClauseIsUsedMoreThanOnceException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS FUNCTION out <-- foo PRECONDITION 1=1 PRECONDITION 1=1 BODY skip END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("PRECONDITION clause is used more than once in operation 'foo'.", e.getMessage());
	}

	@Test
	public void testDependsOnRuleUsedMoreThanOnceException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo DEPENDS_ON_RULE bar DEPENDS_ON_RULE bazz BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("DEPENDS_ON_RULE clause is used more than once in operation 'foo'.", e.getMessage());
	}

	@Test
	public void testDependsOnRuleInvalidIdentifierException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo DEPENDS_ON_RULE 1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Expected a list of identifiers after DEPENDS_ON_RULE.", e.getMessage());
	}

	@Test
	public void testActivationFunction() {
		final String testMachine = "RULES_MACHINE Test CONSTANTS k PROPERTIES k = FALSE OPERATIONS FUNCTION out <-- foo ACTIVATION k = TRUE BODY skip END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("ACTIVATION is not a valid attribute of a FUNCTION operation.", e.getMessage());
	}

	@Test
	public void testDependsOnComputationUsedMoreThanOnceException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo DEPENDS_ON_COMPUTATION bar DEPENDS_ON_COMPUTATION bazz BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("DEPENDS_ON_COMPUTATION clause is used more than once in operation 'foo'.", e.getMessage());
	}

	@Test
	public void testDependsOnComputationInvalidIdentifierException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo DEPENDS_ON_COMPUTATION 1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Expected a list of identifiers after DEPENDS_ON_COMPUTATION.", e.getMessage());
	}

	@Test
	public void testRuleIdAttributeIsUsedMoreThanOnceException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo RULEID req1 RULEID req2 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("RULEID clause is used more than once in operation 'foo'.", e.getMessage());
	}

	@Test
	public void testRuleIdAttributeInvalidIdentifierException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo RULEID 1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Expected exactly one identifier behind RULEID.", e.getMessage());
	}

	@Test
	public void testRuleIdAttributeTwoIdentifierException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo RULEID req1, req2 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Expected exactly one identifier behind RULEID.", e.getMessage());
	}

	@Test
	public void testRuleIdInComputationException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION foo RULEID req1 BODY skip END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("RULEID is not an attribute of a FUNCTION or Computation operation.", e.getMessage());
	}

	@Test
	public void testErrorTypesAttributeNoIntegerValueException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo ERROR_TYPES k BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Expected exactly one integer after ERROR_TYPES.", e.getMessage());
	}

	@Test
	public void testErrorTypesAttributeTwoIntegersException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo ERROR_TYPES 1,2 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Expected exactly one integer after ERROR_TYPES.", e.getMessage());
	}

	@Test
	public void testErrorTypesAttributeInFunctionOrComputationException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION foo ERROR_TYPES 2 BODY skip END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("ERROR_TYPES is not an attribute of a FUNCTION or COMPUTATION operation.", e.getMessage());
	}

	@Test
	public void testSeesException() {
		final String testMachine = "RULES_MACHINE test SEES foo END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The SEES clause is not allowed in a RULES_MACHINE.", e.getMessage());
	}

	@Test
	public void testUsesException() {
		final String testMachine = "RULES_MACHINE test USES foo END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The USES clause is not allowed in a RULES_MACHINE.", e.getMessage());
	}

	@Test
	public void testGetRuleCounterexamplesException() {
		final String testMachine = "RULES_MACHINE test DEFINITIONS GOAL == GET_RULE_COUNTEREXAMPLES(1,2,3) = {} END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Invalid number of arguments. Expected one or two arguments.", e.getMessage());
	}

	@Test
	public void testGetRuleCounterexamples2Exception() {
		final String testMachine = "RULES_MACHINE test DEFINITIONS GOAL == GET_RULE_COUNTEREXAMPLES(1) = {} END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The first argument of GET_RULE_COUNTEREXAMPLES must be an identifier.", e.getMessage());
	}

	@Test
	public void testGetRuleCounterexamples3Exception() {
		final String testMachine = "RULES_MACHINE test DEFINITIONS GOAL == GET_RULE_COUNTEREXAMPLES(foo) = {} END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("'foo' does not match any rule visible to this machine.", e.getMessage());
	}

	@Test
	public void testIdentifierAlreadyExists() {
		final String testMachine = "RULES_MACHINE test CONSTANTS k, k PROPERTIES k = 1 END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Identifier already exists.", e.getMessage());
	}

	@Test
	public void testRenamingIsNotAllowed() {
		final String testMachine = "RULES_MACHINE test CONSTANTS k PROPERTIES k.a = 1 END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Identifier renaming is not allowed in a RULES_MACHINE.", e.getMessage());
	}

	@Test
	public void testRuleFailWithParameterButWithoutWHEN() {
		final String testMachine = "RULES_MACHINE test OPERATIONS RULE foo BODY RULE_FAIL x COUNTEREXAMPLE x END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("The WHEN predicate must be provided if RULE_FAIL has at least one parameter.", e.getMessage());
	}

	@Test
	public void testFunctionReturnValuesNoIdentifierException() {
		final String testMachine = "RULES_MACHINE test OPERATIONS FUNCTION a /*@desc dd */ <-- foo BODY skip END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> RulesUtil.getRulesMachineAsPrologTerm(testMachine));
		assertEquals("Identifier expected.", e.getMessage());
	}
}
