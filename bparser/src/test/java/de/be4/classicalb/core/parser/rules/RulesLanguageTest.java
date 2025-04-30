package de.be4.classicalb.core.parser.rules;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Ignore;
import org.junit.Test;

import util.Helpers;

import static de.be4.classicalb.core.parser.rules.RulesUtil.getFileAsPrologTerm;
import static de.be4.classicalb.core.parser.rules.RulesUtil.getRulesProjectAsPrologTerm;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RulesLanguageTest {

	@Test
	public void testSimpleRule() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testRuleClassification() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 CLASSIFICATION SAFETY BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testRuleTags() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 TAGS SAFETY, \"Rule-123\" BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testEnumeratedSet() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test SETS foo= {foo1, foo2} END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("sets(none,[enumerated_set(none,foo,[identifier(none,foo1),identifier(none,foo2)]"));
	}

	@Ignore
	@Test
	// print of counterexamples removed
	public void testRulePrint() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("EXTERNAL_SUBSTITUTION_PRINT"));
	}

	@Test
	public void testForAllPredicate() throws BCompoundException {
		String result = getFileAsPrologTerm("ForAllPredicate.rmch");
	}

	@Test
	public void testRuleForall() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 RULEID id11 BODY RULE_FORALL x WHERE x : 1..3 EXPECT x > 2 COUNTEREXAMPLE \"fail\"END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testDuplicateRuleName() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END;RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> getRulesProjectAsPrologTerm(testMachine));
		assertTrue(e.getMessage().contains("Duplicate operation name"));
	}

	@Test
	public void testRuleFailWithoutParametersAndPredicate() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 RULEID id1 BODY RULE_FAIL COUNTEREXAMPLE \"fail\"END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testRuleFailSequence() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"fail1\"END; RULE_FAIL COUNTEREXAMPLE \"fail2\"END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertFalse(result.contains("$ResultTuple"));
	}

	@Test
	public void testRuleFail2() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 RULEID id1 BODY RULE_FAIL x WHEN x : 1..3 COUNTEREXAMPLE \"fail\"END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testRuleFailWithoutParametersButWitWhenPredicate() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 BODY RULE_FAIL WHEN 1=1 COUNTEREXAMPLE \"fail\"END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testDefinitionInjection() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 \n BODY RULE_FORALL r WHERE r : 1..3 EXPECT 1=2 COUNTEREXAMPLE \"foo\"  END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Does not contain TO_STRING definition.", result.contains("expression_definition(none,'TO_STRING'"));
		assertTrue("Does not contain EXTERNAL_FUNCTION_TO_STRING definition.",
				result.contains("expression_definition(none,'EXTERNAL_FUNCTION_TO_STRING'"));
	}

	@Test
	public void testComputation() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION computeM1 BODY skip END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Missing computation variable.", result.contains("variables(none,[identifier(none,computeM1)])"));
		assertTrue("Missing invariant.", result.contains(
				"invariant(none,member(none,identifier(none,computeM1),set_extension(none,[string(none,'EXECUTED'),string(none,'NOT_EXECUTED'),string(none,'COMPUTATION_DISABLED')])))"));
	}

	@Test
	public void testDefine() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION computeM1 BODY DEFINE foo TYPE POW(INTEGER) VALUE {1} END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Missing DEFINE variable",
				result.contains("variables(none,[identifier(none,computeM1),identifier(none,foo)])"));
	}

	@Test
	public void testDefine2() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION computeM1 BODY DEFINE foo TYPE INTEGER DUMMY_VALUE 1 VALUE 2 END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Missing DEFINE variable",
				result.contains("variables(none,[identifier(none,computeM1),identifier(none,foo)])"));
	}

	@Test
	public void testDefineEventBComprehensionSet() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION computeM1 BODY DEFINE foo TYPE POW(INTEGER) VALUE {x•x:1..3|x} END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Missing DEFINE variable",
			result.contains("variables(none,[identifier(none,computeM1),identifier(none,foo)])"));
	}

	@Test
	public void testRuleOnSuccess() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo BODY RULE_FORALL x WHERE x : 1..100 EXPECT x : 1..10 ON_SUCCESS \"success\" COUNTEREXAMPLE \"fail\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testRuleUnchecked() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo BODY FOR y IN {1,2,3} DO RULE_FORALL x WHERE x : 1..100 EXPECT x : 1..10 UNCHECKED ```unchecked: ${y}``` COUNTEREXAMPLE \"fail\" END END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testRuleCounterexamples() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL== GET_RULE_COUNTEREXAMPLES(foo) /= {} OPERATIONS RULE foo BODY RULE_FAIL COUNTEREXAMPLE \"fail\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testFunction() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS FUNCTION out <-- foo(x) PRECONDITION x : INTEGER BODY out := x + 1 END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("precondition(none,member(none,identifier(none,x),integer_set(none))"));
	}

	@Test
	public void testFunctionMultipleReturn() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS FUNCTION out1, out2 <-- foo(x) PRECONDITION x : INTEGER BODY out1 := x + 1; out2 := succ(out1) END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("operation(none,identifier(none,foo),[identifier(none,out1),identifier(none,out2)],[identifier(none,x)],precondition(none,member(none,identifier(none,x),integer_set(none)),sequence(none,[assign(none,[identifier(none,out1)],[add(none,identifier(none,x),integer(none,1))]),assign(none,[identifier(none,out2)],[function(none,successor(none),[identifier(none,out1)])])])))"));
	}

	@Test
	public void testFunctionPostcondition() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS FUNCTION out <-- foo(x) PRECONDITION x : INTEGER POSTCONDITION out : INTEGER BODY out := x + 1 END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("assertion(none,member(none,identifier(none,out),integer_set(none)),skip(none))"));
	}

	@Test
	public void testRuleId() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo RULEID id2 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Expected RULEID in description of rule operation.", result.contains("rules_info([rule_id(id2)])."));
	}

	@Test
	public void testClassification() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo CLASSIFICATION cl2 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Expected CLASSIFICATION in description of rule operation.", result.contains("rules_info([classification(cl2)])."));
	}

	@Test
	public void testTags() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo TAGS t1,t2,t3 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Expected TAGS in description of rule operation.", result.contains("rules_info([tags([t1,t2,t3])])."));
	}

	@Test
	public void testAllAttributes() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo TAGS t1,t2,t3 CLASSIFICATION c4 RULEID r5 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Expected operation attributes in description of rule operation.", result.contains("rules_info([tags([t1,t2,t3]),classification(c4),rule_id(r5)])."));
	}

	@Test
	public void testActivation() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test CONSTANTS k PROPERTIES k = FALSE OPERATIONS RULE foo ACTIVATION k = TRUE BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("if_then_else(none,equal(none,identifier(none,k),boolean_true(none)),string(none,'NOT_CHECKED'),string(none,'DISABLED'))"));
	}

	@Test
	public void testRulePredicates() throws BCompoundException {
		String testMachine = "RULES_MACHINE Test\n";
		testMachine += "DEFINITIONS GOAL == \n";
		testMachine += "  DISABLED_RULE(rule1)\n";
		testMachine += "& NOT_CHECKED_RULE(rule1) \n";
		testMachine += "& FAILED_RULE(rule1) \n";
		testMachine += "& FAILED_RULE_ERROR_TYPE(rule1,1) \n";
		testMachine += "& SUCCEEDED_RULE(rule1) & SUCCEEDED_RULE_ERROR_TYPE(rule1,1)\n";
		testMachine += "& SUCCEEDED_RULE(rule1)\n";
		testMachine += "OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END\n";
		testMachine += "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testDefineReadItself() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION foo BODY DEFINE xx TYPE POW(INTEGER) VALUE xx END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> getRulesProjectAsPrologTerm(testMachine));
		assertTrue(e.getMessage().contains("Variable 'xx' read before defined."));
	}

	@Test
	public void testVariableDefinedTwice() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION compute_x BODY DEFINE x TYPE POW(INTEGER) VALUE {1} END; \n DEFINE x TYPE POW(INTEGER) VALUE {2} END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Variable 'x' is defined more than once.", e.getMessage());
	}

	@Test
	public void testInvalidComputation() {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION compute_x_y BODY DEFINE x TYPE POW(STRING) VALUE y END \n; DEFINE y TYPE POW(STRING) VALUE {\"foo\"}END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> getRulesProjectAsPrologTerm(testMachine));
		assertEquals("Variable 'y' read before defined.", e.getMessage());
	}

	@Test
	public void testRuleIfThenElse() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo BODY IF 1=1 THEN skip ELSE RULE_FAIL COUNTEREXAMPLE \"never\" END END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testNestedForLoop() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS foo = \nFOR x IN 1..3 \nDO FOR y IN 1..3 \nDO skip END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("var(none,[identifier(none,'$SET0')"));
		assertTrue(result.contains("var(none,[identifier(none,'$SET1')"));
	}

	@Test
	public void testForLoopTwoLoopVariables() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS foo = \nFOR x,y IN {1..3}*{TRUE} \nDO skip END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		// x,y:: {CHOOSE(..)}
		assertTrue(result.contains(
				"becomes_element_of(none,[identifier(none,x),identifier(none,y)],set_extension(none,[definition(none,'CHOOSE',[identifier(none,'$SET0')])"));
	}

	@Test
	public void testDependsOnRule() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE foo BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END\n;"
				+ " RULE foo2 DEPENDS_ON_RULE foo BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testDependsOnComputation() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION compute BODY skip END\n;"
				+ " RULE foo2 DEPENDS_ON_COMPUTATION compute BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testDefinition() throws BCompoundException {
		final String testMachine = "RULES_MACHINE test DEFINITIONS foo == 1 END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue("Invalid definition injector",
				result.contains("expression_definition(none,foo,[],integer(none,1))"));
	}

	@Test
	public void testGoal() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == SUCCEEDED_RULE(rule1) & 1=1 OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testGetCounterexamples() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == GET_RULE_COUNTEREXAMPLES(rule1,2) = {} OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,function(none,lambda(none,[identifier(none,'$x')],member(none,identifier(none,'$x'),interval(none,integer(none,1),integer(none,1))),image(none,identifier(none,rule1_Counterexamples),set_extension(none,[identifier(none,'$x')]))),[integer(none,2)]),empty_set(none))"));
	}

	@Test
	public void testSucceededRuleErrorType() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == SUCCEEDED_RULE_ERROR_TYPE(rule1, 2) OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,function(none,lambda(none,[identifier(none,'$x')],member(none,identifier(none,'$x'),interval(none,integer(none,1),integer(none,1))),image(none,identifier(none,rule1_Counterexamples),set_extension(none,[identifier(none,'$x')]))),[integer(none,2)]),empty_set(none))"));
	}

	@Test
	public void testFailedRuleErrorType() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE_ERROR_TYPE(rule1, 2) OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("not_equal(none,function(none,lambda(none,[identifier(none,'$x')],member(none,identifier(none,'$x'),interval(none,integer(none,1),integer(none,1))),image(none,identifier(none,rule1_Counterexamples),set_extension(none,[identifier(none,'$x')]))),[integer(none,2)]),empty_set(none))"));
	}

	@Test
	public void testVarSubstitution() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test INITIALISATION VAR a,b IN a := 1; b:=1 END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testReplaces() throws BCompoundException {
		String testMachine = "RULES_MACHINE Test\n";
		testMachine += "OPERATIONS\n";
		testMachine += "COMPUTATION comp1 BODY skip END;\n";
		testMachine += "COMPUTATION comp2 REPLACES comp1 BODY skip END\n";
		testMachine += "END\n";
		final String prologOutput = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testPreferences() throws BCompoundException {
		final String testMachine = "RULES_MACHINE test DEFINITIONS SET_PREF_TIME_OUT == 1000; SET_PREF_COMPRESSION == FALSE; SET_PREF_TRY_FIND_ABORT == TRUE; SET_PREF_SOME_PREF == \"foo\" END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("SET_PREF_TIME_OUT"));
	}

	@Test
	public void testDefineSymbolicLambda() throws BCompoundException {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY DEFINE foo TYPE POW(INTEGER) VALUE /*@symbolic */ %x.(x : INTEGER | x ) END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertFalse(result.contains("FORCE"));
	}

	@Test
	public void testDefineSymbolicSetComprehension() throws BCompoundException {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY DEFINE foo TYPE POW(INTEGER) VALUE /*@symbolic */ {x| x : INTEGER}END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertFalse(result.contains("FORCE"));
	}

	@Test
	public void testDefineSymbolicQuantifiedUnion() throws BCompoundException {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY DEFINE foo TYPE POW(INTEGER) VALUE /*@symbolic */ UNION(x).(x<:1..2|x) END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
		assertFalse(result.contains("FORCE"));
	}

	@Test
	public void testFailedRuleAllErrorTypes() throws BCompoundException {
		final String testMachine = "RULES_MACHINE test DEFINITIONS GOAL == FAILED_RULE_ALL_ERROR_TYPES(foo)  OPERATIONS RULE foo BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

	@Test
	public void testComputationWithoutOptionalType() throws BCompoundException {
		final String testMachine = "RULES_MACHINE test OPERATIONS COMPUTATION comp BODY DEFINE foo VALUE 1..2 END END END";
		String result = getRulesProjectAsPrologTerm(testMachine);
	}

}
