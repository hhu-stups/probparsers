package de.be4.classicalb.core.parser.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class RulesProjectTest {

	@Test
	public void testRuleDependencies() {
		String testMachine = "RULES_MACHINE Test OPERATIONS\n";
		testMachine += "COMPUTATION c1 DEPENDS_ON_RULE r1 BODY DEFINE x2 TYPE POW(INTEGER) VALUE x1 END END;\n";
		testMachine += "RULE r1 BODY VAR foo IN foo := x1 END;RULE_FAIL COUNTEREXAMPLE \"never\" END END;\n";
		testMachine += "COMPUTATION c2 BODY DEFINE x3 TYPE POW(INTEGER) VALUE x1 \\/ x2  END END;\n";
		testMachine += "COMPUTATION c3 BODY DEFINE x1 TYPE POW(INTEGER) VALUE {} END END\n";
		testMachine += "END";
		RulesProject p = new RulesProject();
		p.parseRulesMachines(testMachine);
		p.checkAndTranslateProject();
		AbstractOperation abstractOperation = p.getOperationsMap().get("c2");
		List<AbstractOperation> deps = abstractOperation.getSortedListOfTransitiveDependencies();
		assertEquals("c3", deps.get(0).getOriginalName());
		assertEquals("r1", deps.get(1).getOriginalName());
		assertEquals("c1", deps.get(2).getOriginalName());
	}

	@Test
	public void testUncheckedMessageCreatesImplicitDependency() {
		String testMachine = "RULES_MACHINE Test OPERATIONS\n"
				+ "COMPUTATION c BODY DEFINE v TYPE POW(INTEGER) VALUE {1} END END;\n"
				+ "RULE r BODY RULE_FORALL x WHERE x : 1..3 EXPECT x > 0 UNCHECKED v COUNTEREXAMPLE \"fail\" END END\n"
				+ "END";
		RulesProject p = new RulesProject();
		p.parseRulesMachines(testMachine);
		p.checkAndTranslateProject();
		assertEquals(0, p.getBExceptionList().size());
		AbstractOperation rule = p.getOperationsMap().get("r");
		assertEquals(1, rule.getTransitiveDependencies().size());
		assertTrue(rule.getTransitiveDependencies().contains(p.getOperationsMap().get("c")));
	}
}
