package de.be4.classicalb.core.parser.rules;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.CheckException;

import org.junit.Ignore;
import org.junit.Test;

import util.Helpers;

import java.util.Arrays;

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
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == GET_RULE_COUNTEREXAMPLES(rule1,2) = {} OPERATIONS RULE rule1 ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 1 COUNTEREXAMPLE \"one\" END; RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"two\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,image(none,identifier(none,rule1_Counterexamples),set_extension(none,[integer(none,2)])),empty_set(none))"));
	}

	@Test
	public void testGetCounterexamplesNonLiteral() {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == GET_RULE_COUNTEREXAMPLES(rule1, 1+1) = {} OPERATIONS RULE rule1 ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 1 COUNTEREXAMPLE \"one\" END; RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"two\" END END END";
		final CheckException e = Helpers.assertThrowsCompound(CheckException.class, () -> getRulesProjectAsPrologTerm(testMachine));
		assertEquals("The second argument of GET_RULE_COUNTEREXAMPLES must be an integer literal.", e.getMessage());
	}

	@Test
	public void testSucceededRuleErrorType() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == SUCCEEDED_RULE_ERROR_TYPE(rule1, 2) OPERATIONS RULE rule1 ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 1 COUNTEREXAMPLE \"one\" END; RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"two\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("conjunct(none,[member(none,identifier(none,rule1),set_extension(none,[string(none,'SUCCESS'),string(none,'FAIL')])),equal(none,image(none,identifier(none,rule1_Counterexamples),set_extension(none,[integer(none,2)])),empty_set(none))])"));
	}

	@Test
	public void testFailedRuleErrorType() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE_ERROR_TYPE(rule1, 2) OPERATIONS RULE rule1 ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 1 COUNTEREXAMPLE \"one\" END; RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"two\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("not_equal(none,image(none,identifier(none,rule1_Counterexamples),set_extension(none,[integer(none,2)])),empty_set(none))"));
	}

	@Test
	public void testVarSubstitution() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS COMPUTATION c BODY VAR a,b IN a := 1; b:=1 END END END";
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

	@Test
	public void testRuleListOperators() throws BCompoundException {
		String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE(rule1, rule2) & SUCCEEDED_RULE(rule1, rule2) & NOT_CHECKED_RULE(rule1, rule2) & DISABLED_RULE(rule1, rule2)\n"
				+ "OPERATIONS RULE rule1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END; RULE rule2 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		for (String status : Arrays.asList("FAIL", "SUCCESS", "NOT_CHECKED", "DISABLED")) {
			assertTrue(result.contains("equal(none,identifier(none,rule1),string(none,'" + status + "'))"));
			assertTrue(result.contains("equal(none,identifier(none,rule2),string(none,'" + status + "'))"));
		}
	}

	@Test
	public void testRuleForallWithoutAllTuple() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 BODY RULE_FORALL x WHERE x : 1..10 EXPECT x < 5 COUNTEREXAMPLE \"fail\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		// counterexamples are computed by a single comprehension {x | x : 1..10 & not(x < 5)}
		assertFalse(result.contains("$AllTuple"));
		assertTrue(result.contains("comprehension_set(none,[identifier(none,x)],conjunct(none,[member(none,identifier(none,x),interval(none,integer(none,1),integer(none,10))),negation(none,less(none,identifier(none,x),integer(none,5)))]))"));
	}

	@Test
	public void testRuleForallWithOnSuccessUsesAllTuple() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE rule1 BODY RULE_FORALL x WHERE x : 1..10 EXPECT x < 5 ON_SUCCESS \"ok\" COUNTEREXAMPLE \"fail\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("$AllTuple"));
	}

	@Test
	public void testDependencyGuardsAreSorted() throws BCompoundException {
		final String testMachine = "RULES_MACHINE Test OPERATIONS RULE c BODY RULE_FAIL COUNTEREXAMPLE \"x\" END END; RULE a BODY RULE_FAIL COUNTEREXAMPLE \"x\" END END; RULE b BODY RULE_FAIL COUNTEREXAMPLE \"x\" END END; RULE d DEPENDS_ON_RULE c, a, b BODY RULE_FAIL COUNTEREXAMPLE \"x\" END END END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("select(none,conjunct(none,[equal(none,identifier(none,d),string(none,'NOT_CHECKED')),equal(none,identifier(none,a),string(none,'SUCCESS')),equal(none,identifier(none,b),string(none,'SUCCESS')),equal(none,identifier(none,c),string(none,'SUCCESS'))])"));
	}

	@Test
	public void testPromotesOperationsInDeclarationOrder() throws BCompoundException {
		String testMachine = "RULES_MACHINE Test OPERATIONS\n"
				+ "COMPUTATION c2 BODY skip END;\n"
				+ "RULE r1 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END;\n"
				+ "COMPUTATION c1 BODY skip END;\n"
				+ "RULE r0 BODY RULE_FAIL COUNTEREXAMPLE \"never\" END END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("promotes(none,[identifier(none,c2),identifier(none,r1),identifier(none,c1),identifier(none,r0)])"));
	}

	@Test
	public void testRuleReplacement() throws Exception {
		String testMachine = "RULES_MACHINE Test DEFINITIONS X == SUCCEEDED_RULE_ERROR_TYPE(r1,1) OPERATIONS\n"
				+ "RULE r1 BODY RULE_FAIL COUNTEREXAMPLE \"old\" END END;\n"
				+ "RULE r2 REPLACES r1 BODY RULE_FAIL COUNTEREXAMPLE \"new\" END END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		// the promoted operation r1 must exist and must be implemented by the replacing rule
		assertTrue(result.contains("promotes(none,[identifier(none,r1)])"));
		assertTrue(result.contains("operation(none,identifier(none,r1),[],[]"));
		assertTrue(result.contains("string(none,new)"));
		assertFalse(result.contains("string(none,old)"));
		assertTrue(result.contains("identifier(none,r1_Counterexamples)"));
		assertFalse(result.contains("identifier(none,r2"));
	}

	@Test
	public void testTransitiveRuleReplacement() throws Exception {
		String testMachine = "RULES_MACHINE Test OPERATIONS\n"
				+ "RULE r1 BODY RULE_FAIL COUNTEREXAMPLE \"very_old\" END END;\n"
				+ "RULE r2 REPLACES r1 BODY RULE_FAIL COUNTEREXAMPLE \"old\" END END;\n"
				+ "RULE r3 REPLACES r2 BODY RULE_FAIL COUNTEREXAMPLE \"new\" END END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		// the promoted operation r1 must exist and must be implemented by rule r3
		assertTrue(result.contains("promotes(none,[identifier(none,r1)])"));
		assertTrue(result.contains("operation(none,identifier(none,r1),[],[]"));
		assertTrue(result.contains("string(none,new)"));
		assertFalse(result.contains("string(none,old)"));
		assertFalse(result.contains("string(none,very_old)"));
		assertTrue(result.contains("identifier(none,r1_Counterexamples)"));
		assertFalse(result.contains("r2"));
		assertFalse(result.contains("r3"));
	}

	@Test
	public void testTransitiveRuleReplacementWithUnchecked() throws Exception {
		String testMachine = "RULES_MACHINE Test OPERATIONS\n"
				+ "RULE r1 BODY RULE_FAIL COUNTEREXAMPLE \"very_old\" END END;\n"
				+ "RULE r2 REPLACES r1 BODY RULE_FAIL COUNTEREXAMPLE \"old\" END END;\n"
				+ "RULE r3 REPLACES r2 BODY RULE_FORALL x WHERE x : 1..3 EXPECT x > 0 ON_SUCCESS \"ok\" UNCHECKED \"none\" COUNTEREXAMPLE \"new\" END END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		// all result variables of r3 are the ones of r1
		assertTrue(result.contains("identifier(none,r1_Unchecked)"));
		assertTrue(result.contains("identifier(none,r1_Successful)"));
		assertTrue(result.contains("string(none,ok)"));
		assertTrue(result.contains("string(none,none)"));
		assertFalse(result.contains("r2"));
		assertFalse(result.contains("r3"));
	}

	@Test
	public void testTransitiveComputationReplacement() throws Exception {
		String testMachine = "RULES_MACHINE Test OPERATIONS\n"
				+ "COMPUTATION c1 BODY DEFINE v VALUE {1} END END;\n"
				+ "COMPUTATION c2 REPLACES c1 BODY DEFINE v VALUE {2} END END;\n"
				+ "COMPUTATION c3 REPLACES c2 BODY DEFINE v VALUE {3} END END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("promotes(none,[identifier(none,c1)])"));
		assertTrue(result.contains("operation(none,identifier(none,c1),[],[]"));
		assertTrue(result.contains("set_extension(none,[integer(none,3)])"));
		assertFalse(result.contains("set_extension(none,[integer(none,2)])"));
		assertFalse(result.contains("set_extension(none,[integer(none,1)])"));
		assertFalse(result.contains("c2"));
		assertFalse(result.contains("c3"));
	}

	@Test
	public void testDependencyOnReplacingRule() throws Exception {
		String testMachine = "RULES_MACHINE Test OPERATIONS\n"
				+ "RULE r1 BODY RULE_FAIL COUNTEREXAMPLE \"old\" END END;\n"
				+ "RULE r2 REPLACES r1 BODY RULE_FAIL COUNTEREXAMPLE \"new\" END END;\n"
				+ "COMPUTATION c DEPENDS_ON_RULE r2 BODY skip END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		// the guard of c refers to the status variable r1, which r2 has taken over
		assertTrue(result.contains("equal(none,identifier(none,r1),string(none,'SUCCESS'))"));
		assertFalse(result.contains("r2"));
	}

	@Test
	public void testOperatorsOnReplacedAndReplacingRules() throws Exception {
		String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE(r2) & FAILED_RULE_ALL_ERROR_TYPES(r1) OPERATIONS\n"
				+ "RULE r1 BODY RULE_FAIL COUNTEREXAMPLE \"old\" END END;\n"
				+ "RULE r2 REPLACES r1 ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 1 COUNTEREXAMPLE \"one\" END; RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"two\" END END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,identifier(none,r1),string(none,'FAIL'))"));
		// the error types of the replacing rule are used
		assertTrue(result.contains("equal(none,domain(none,identifier(none,r1_Counterexamples)),interval(none,integer(none,1),integer(none,2)))"));
		assertFalse(result.contains("r2"));
	}

	@Test
	public void testErrorTypesOfLastRuleInReplacementChain() throws Exception {
		// r1 and r2 have one error type, the executed rule r3 has two
		String testMachine = "RULES_MACHINE Test DEFINITIONS GOAL == FAILED_RULE_ALL_ERROR_TYPES(r1) & FAILED_RULE_ERROR_TYPE(r2, 2) OPERATIONS\n"
				+ "RULE r1 BODY RULE_FAIL COUNTEREXAMPLE \"a\" END END;\n"
				+ "RULE r2 REPLACES r1 BODY RULE_FAIL COUNTEREXAMPLE \"b\" END END;\n"
				+ "RULE r3 REPLACES r2 ERROR_TYPES 2 BODY RULE_FAIL ERROR_TYPE 1 COUNTEREXAMPLE \"c\" END; RULE_FAIL ERROR_TYPE 2 COUNTEREXAMPLE \"d\" END END\n"
				+ "END";
		final String result = getRulesProjectAsPrologTerm(testMachine);
		assertTrue(result.contains("equal(none,domain(none,identifier(none,r1_Counterexamples)),interval(none,integer(none,1),integer(none,2)))"));
		assertTrue(result.contains("not_equal(none,image(none,identifier(none,r1_Counterexamples),set_extension(none,[integer(none,2)])),empty_set(none))"));
	}
}
