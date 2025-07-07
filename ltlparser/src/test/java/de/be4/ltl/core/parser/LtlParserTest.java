package de.be4.ltl.core.parser;

import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Assert;
import org.junit.Test;

public class LtlParserTest {
	@Test
	public void testTrue() throws Exception {
		check("true", "true");
	}

	@Test
	public void testFalse() throws Exception {
		check("false", "false");
	}

	@Test
	public void testImplication() throws Exception {
		check("false =>   true ", "implies(false,true)");
	}

	@Test
	public void testSink() throws Exception {
		check("sink", "ap(sink)");
	}

	@Test
	public void testDeadlock() throws Exception {
		check("deadlock", "ap(deadlock)");
	}

	@Test
	public void testCurrent() throws Exception {
		check("current", "ap(stateid(root))");
	}

	@Test
	public void testCurrent1() throws Exception {
		check("G (current => true)", "globally(implies(ap(stateid(root)),true))");
	}

	@Test
	public void testAnd() throws Exception {
		check("true &  false", "and(true,false)");
	}

	@Test
	public void testOr() throws Exception {
		check("true or  false", "or(true,false)");
	}

	@Test
	public void testNot() throws Exception {
		check("not true", "not(true)");
	}

	@Test
	public void testAction() throws Exception {
		check("[bla]", "action(dtrans(bla))");
	}

	@Test
	public void testEnabled() throws Exception {
		check("e(bla)", "ap(enabled(dtrans(bla)))");
	}

	@Test
	public void testAvailable() throws Exception {
		check("Av(bla)", "ap(available(dtrans(bla)))");
	}

	@Test
	public void testPredicate() throws Exception {
		check("{blubb}", "ap(dpred(blubb))");
	}

	@Test
	public void testUntil() throws Exception {
		check("false U      true ", "until(false,true)");
	}

	@Test
	public void testWeakUntil() throws Exception {
		check("false W      true ", "weakuntil(false,true)");
	}

	@Test
	public void testRelease() throws Exception {
		check("false R      true ", "release(false,true)");
	}

	@Test
	public void testSince() throws Exception {
		check("false S true ", "since(false,true)");
	}

	@Test
	public void testTrigger() throws Exception {
		check("false T true ", "trigger(false,true)");
	}

	@Test
	public void testGlobally() throws Exception {
		check("G true ", "globally(true)");
	}

	@Test
	public void testFinally() throws Exception {
		check(" F true ", "finally(true)");
	}

	@Test
	public void testNext() throws Exception {
		check("X true ", "next(true)");
	}

	@Test
	public void testHistorically() throws Exception {
		check("H true ", "historically(true)");
	}

	@Test
	public void testOnce() throws Exception {
		check("O true ", "once(true)");
	}

	@Test
	public void testYesterday() throws Exception {
		check("Y true ", "yesterday(true)");
	}

	@Test
	public void testGloballyFinallyAP() throws Exception {
		check("GF {blubb}", "globally(finally(ap(dpred(blubb))))");
	}

	@Test
	public void testComplex2() throws Exception {
		check(
			"not({xxx} => GF {blubb})",
			"not(implies(ap(dpred(xxx)),globally(finally(ap(dpred(blubb))))))"
		);
	}

	@Test
	public void testExistsImplication() throws Exception {
		check(
			"#x. ( {blubb} & G [x])",
			"exists(x,ap(dpred(blubb)),globally(action(dtrans(x))))"
		);
	}

	@Test
	public void testExistsImplicationNested() throws Exception {
		check(
			"# x___1 . ( {blubb} & !y. ({blubb} => G ([x] or [y])))",
			"exists(x___1,ap(dpred(blubb)),forall(y,ap(dpred(blubb)),globally(or(action(dtrans(x)),action(dtrans(y))))))"
		);
	}

	@Test
	public void testForAllImplication() throws Exception {
		check(
			"!xyz. ( {blubb} => G [x])",
			"forall(xyz,ap(dpred(blubb)),globally(action(dtrans(x))))"
		);
	}

	@Test
	public void testWeakFair() throws Exception {
		check(
			"wf(bla) => true",
			"fairnessimplication(weakassumptions(ap(weak_fair(dtrans(bla)))),true)"
		);
	}

	@Test
	public void testWeakFairCapital() throws Exception {
		check(
			"WF(bla) => true",
			"fairnessimplication(weakassumptions(ap(weak_fair(dtrans(bla)))),true)"
		);
	}

	@Test
	public void testWeakFair_multiple() throws Exception {
		check(
			"wf(bla) or wf(blubb) => true",
			"fairnessimplication(weakassumptions(or(ap(weak_fair(dtrans(bla))),ap(weak_fair(dtrans(blubb))))),true)"
		);
	}

	@Test
	public void testStrongFair() throws Exception {
		check(
			"(sf(bla)) & (wf(blubb)) => true",
			"fairnessimplication(and(strongassumptions(ap(strong_fair(dtrans(bla)))),weakassumptions(ap(weak_fair(dtrans(blubb))))),true)"
		);
	}

	@Test
	public void testStrongFairCapital() throws Exception {
		check(
			"(SF(bla)) & (wf(blubb)) => true",
			"fairnessimplication(and(strongassumptions(ap(strong_fair(dtrans(bla)))),weakassumptions(ap(weak_fair(dtrans(blubb))))),true)"
		);
	}

	@Test
	public void testStrongFair_multiple() throws Exception {
		check(
			"( (sf(bla) & sf(blubb)) => (true))",
			"fairnessimplication(strongassumptions(and(ap(strong_fair(dtrans(bla))),ap(strong_fair(dtrans(blubb))))),true)"
		);
	}

	@Test
	public void testWeakFairAll() throws Exception {
		check("WEF => true", "fairnessimplication(weakassumptions(all),true)");
	}

	@Test
	public void testStrongFairAll() throws Exception {
		check("SEF => true", "fairnessimplication(strongassumptions(all),true)");
	}

	@Test
	public void testWeakStrongFairAll() throws Exception {
		check(
			"SEF & WEF => true",
			"fairnessimplication(and(strongassumptions(all),weakassumptions(all)),true)"
		);
	}

	@Test
	public void testWeakStrongFairAll1() throws Exception {
		check(
			"sf(bla) & WEF => true",
			"fairnessimplication(and(strongassumptions(ap(strong_fair(dtrans(bla)))),weakassumptions(all)),true)"
		);
	}

	@Test
	public void testDLK() throws Exception {
		check("deadlock( bla)", "ap(dlk([dtrans(bla)]))");
	}

	@Test
	public void testDLK2() throws Exception {
		check("deadlock(bla,argg)", "ap(dlk([dtrans(bla),dtrans(argg)]))");
	}

	@Test
	public void testDET() throws Exception {
		check("deterministic(bla   , argg)", "ap(det([dtrans(bla),dtrans(argg)]))");
	}

	@Test
	public void testCtrl() throws Exception {
		check("controller(bla,argg)", "ap(ctrl([dtrans(bla),dtrans(argg)]))");
	}


	@Test(expected = LtlParseException.class)
	public void ticket_parsing_fairness_assumptions() throws Exception {
		String buggy = "SF(bla) & F{blubb} => true";
		parse(buggy);
	}

	@Test(expected = LtlParseException.class)
	public void ticket_parsing_DLK() throws Exception {
		String buggy = "deadlock()";
		parse(buggy);
	}

	@Test(expected = LtlParseException.class)
	public void ticket_parsing_DET() throws Exception {
		String buggy = "deterministic()";
		parse(buggy);
	}

	@Test(expected = LtlParseException.class)
	public void ticket_parserlib_11() throws Exception {
		String buggy = "G {taken= {} ";
		parse(buggy);

	}

	@Test(expected = LtlParseException.class)
	public void ticket_parserlib_exists() throws Exception {
		String buggy = "#x. ( {blubb} => G [x])";
		parse(buggy);

	}

	@Test(expected = LtlParseException.class)
	public void testPredSyntaxError() throws LtlParseException {
		parse("{X}");
	}

	@Test
	public void testParserlib17() throws Exception {
		// Non-dummy version, in case we ever stop using the DummyParser:
		// "globally(ap(bpred(equal(none,string(none,'{'),string(none,'1')))))"
		check("G {\"{\"=\"1\"}", "globally(ap(dpred('\"{\"=\"1\"')))");
	}

	private static void check(String input, String expectedTerm) throws LtlParseException {
		String term = parse(input);
		Assert.assertEquals(expectedTerm, term);
	}

	private static String parse(String input) throws LtlParseException {
		final LtlParser parser = new LtlParser(new DummyParser(true, true));
		PrologTermStringOutput pto = new PrologTermStringOutput();
		parser.printFormulaAsProlog(input, "root", pto);
		return pto.toString();
	}
}
