package de.be4.classicalb.core.parser;

import java.util.Arrays;
import java.util.Collections;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.NodeFileNumbers;
import de.be4.classicalb.core.parser.analysis.prolog.PositionPrinter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.AAssignSubstitution;
import de.be4.classicalb.core.parser.node.AConstructorFreetypeConstructor;
import de.be4.classicalb.core.parser.node.ADisjunctPredicate;
import de.be4.classicalb.core.parser.node.AEqualPredicate;
import de.be4.classicalb.core.parser.node.AEvent;
import de.be4.classicalb.core.parser.node.AEventBModelParseUnit;
import de.be4.classicalb.core.parser.node.AEventsModelClause;
import de.be4.classicalb.core.parser.node.AFalsityPredicate;
import de.be4.classicalb.core.parser.node.AFreetype;
import de.be4.classicalb.core.parser.node.AFreetypesMachineClause;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.AIntegerSetExpression;
import de.be4.classicalb.core.parser.node.APartitionPredicate;
import de.be4.classicalb.core.parser.node.APowSubsetExpression;
import de.be4.classicalb.core.parser.node.ATruthPredicate;
import de.be4.classicalb.core.parser.node.AWitness;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PSubstitution;
import de.be4.classicalb.core.parser.node.PWitness;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.TIntegerLiteral;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ASTPrologTest {
	private static String printAST(final Node node, final INodeIds nodeids) {
		IPrologTermOutput pout = new PrologTermStringOutput();
		PositionPrinter pprinter = new ClassicalPositionPrinter(nodeids);
		ASTProlog prolog = new ASTProlog(pout, pprinter);
		node.apply(prolog);
		return pout.toString();
	}

	private static void checkAST(String expected, Node ast) {
		assertEquals(expected, printAST(ast, new NodeFileNumbers()));
	}

	private static void checkMachine(String bspec, String expected) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(bspec);
		checkAST(expected, startNode);
	}

	private static void checkPredicate(final String pred, final String expected) throws BCompoundException {
		BParser parser = new BParser("testcase");
		Start startNode = parser.parsePredicate(pred);
		checkAST(expected, startNode);
	}

	private static void checkExpression(final String expr, final String expected) throws BCompoundException {
		BParser parser = new BParser("testcase");
		Start startNode = parser.parseExpression(expr);
		checkAST(expected, startNode);
	}

	private static void checkSubstitution(final String subst, final String expected) throws BCompoundException {
		BParser parser = new BParser("testcase");
		Start startNode = parser.parseSubstitution(subst);
		checkAST(expected, startNode);
	}

	private static void checkOppatterns(final String pattern, final String expected) throws BCompoundException {
		BParser parser = new BParser("testcase");
		Start startNode = parser.parseTransition(pattern);
		checkAST(expected, startNode);
	}

	@Test
	public void testMachine() throws BCompoundException {
		String m = "MACHINE name OPERATIONS op=skip END";
		String expected = "abstract_machine(none,machine(none),machine_header(none,name,[]),[operations(none,[operation(none,identifier(none,op),[],[],skip(none))])])";
		checkMachine(m, expected);
	}

	@Test
	public void testMachine2() throws BCompoundException {
		String m = "MACHINE mname(P)  SETS S; E={e1,e2}" + "  INCLUDES inc(x),rn.inc2  SEES see,s.see2  VARIABLES x"
				+ "  INVARIANT x:NAT  INITIALISATION x:=5" + "  OPERATIONS op=skip; r,s <-- op2(a,b) = skip  END";
		String expected = "abstract_machine(none,machine(none),machine_header(none,mname,[identifier(none,'P')]),"
				+ "[sets(none,[deferred_set(none,'S'),enumerated_set(none,'E',[identifier(none,e1),identifier(none,e2)])]),"
				+ "includes(none,[machine_reference(none,inc,[identifier(none,x)]),machine_reference(none,'rn.inc2',[])]),"
				+ "sees(none,[identifier(none,see),identifier(none,'s.see2')])," + "variables(none,[identifier(none,x)]),"
				+ "invariant(none,member(none,identifier(none,x),nat_set(none))),"
				+ "initialisation(none,assign(none,[identifier(none,x)],[integer(none,5)])),"
				+ "operations(none,[operation(none,identifier(none,op),[],[],skip(none)),"
				+ "operation(none,identifier(none,op2),[identifier(none,r),identifier(none,s)],"
				+ "[identifier(none,a),identifier(none,b)],skip(none))])])";

		checkMachine(m, expected);
	}

	@Test
	public void testRefinement() throws BCompoundException {
		String ref = "REFINEMENT ref REFINES abstract VARIABLES x END";
		String expected = "refinement_machine(none,machine_header(none,ref,[]),abstract,[variables(none,[identifier(none,x)])])";
		checkMachine(ref, expected);
	}

	@Test
	public void testEmptyString() throws BCompoundException {
		checkExpression("\"test\"+\"\"", "add(none,string(none,test),string(none,''))");
	}

	@Test
	public void testPredicates() throws BCompoundException {
		checkPredicate("5>r.j", "greater(none,integer(none,5),identifier(none,'r.j'))");
		checkPredicate("!x,y.(x<y)",
				"forall(none,[identifier(none,x),identifier(none,y)],less(none,identifier(none,x),identifier(none,y)))");
	}

	@Test
	public void testExpressions() throws BCompoundException {
		checkExpression("SIGMA x,y.(x:NAT & y:INT | x+y)",
				"general_sum(none,[identifier(none,x),identifier(none,y)],"
						+ "conjunct(none,[member(none,identifier(none,x),nat_set(none)),member(none,identifier(none,y),int_set(none))]),"
						+ "add(none,identifier(none,x),identifier(none,y)))");
	}

	@Test
	public void testEmptySet() throws BCompoundException {
		checkExpression("∅", "empty_set(none)");
		checkExpression("{}", "empty_set(none)");
	}

	@Test
	public void testSetExtension() throws BCompoundException {
		checkExpression("{x}", "set_extension(none,[identifier(none,x)])");
		checkExpression("{(x)}", "set_extension(none,[identifier(none,x)])");
		checkExpression("{x,y}", "set_extension(none,[identifier(none,x),identifier(none,y)])");
		checkExpression("{(x,y)}", "set_extension(none,[couple(none,[identifier(none,x),identifier(none,y)])])");
		checkExpression("{x,y,z}", "set_extension(none,[identifier(none,x),identifier(none,y),identifier(none,z)])");
		checkExpression("{(x,y,z)}", "set_extension(none,[couple(none,[identifier(none,x),identifier(none,y),identifier(none,z)])])");
	}

	@Test
	public void testComprehensionSet1() throws BCompoundException {
		checkExpression("{x|x<5}", "comprehension_set(none,[identifier(none,x)],less(none,identifier(none,x),integer(none,5)))");
		checkExpression("{(x)|x<5}", "comprehension_set(none,[identifier(none,x)],less(none,identifier(none,x),integer(none,5)))");
	}

	@Test
	public void testComprehensionSet2() throws BCompoundException {
		checkExpression("{x,y|x<y&y<5}", "comprehension_set(none,[identifier(none,x),identifier(none,y)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("{(x,y)|x<y&y<5}", "comprehension_set(none,[identifier(none,x),identifier(none,y)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testComprehensionSet3() throws BCompoundException {
		checkExpression("{x,y,z|x<y&y<5}", "comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("{(x,y,z)|x<y&y<5}", "comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testSymbolicComprehensionSet1() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x|x<5}", "symbolic_comprehension_set(none,[identifier(none,x)],less(none,identifier(none,x),integer(none,5)))");
		checkExpression("/*@symbolic*/ {(x)|x<5}", "symbolic_comprehension_set(none,[identifier(none,x)],less(none,identifier(none,x),integer(none,5)))");
	}

	@Test
	public void testSymbolicComprehensionSet2() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y|x<y&y<5}", "symbolic_comprehension_set(none,[identifier(none,x),identifier(none,y)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("/*@symbolic*/ {(x,y)|x<y&y<5}", "symbolic_comprehension_set(none,[identifier(none,x),identifier(none,y)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testSymbolicComprehensionSet3() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y,z|x<y&y<5}", "symbolic_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("/*@symbolic*/ {(x,y,z)|x<y&y<5}", "symbolic_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testEventBComprehensionSet1() throws BCompoundException {
		checkExpression("{x·x<5|x*x}", "event_b_comprehension_set(none,[identifier(none,x)],mult_or_cart(none,identifier(none,x),identifier(none,x)),less(none,identifier(none,x),integer(none,5)))");
		checkExpression("{(x)·x<5|x*x}", "event_b_comprehension_set(none,[identifier(none,x)],mult_or_cart(none,identifier(none,x),identifier(none,x)),less(none,identifier(none,x),integer(none,5)))");
	}

	@Test
	public void testEventBComprehensionSet2() throws BCompoundException {
		checkExpression("{x,y·x<y&y<5|x+y}", "event_b_comprehension_set(none,[identifier(none,x),identifier(none,y)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("{(x,y)·x<y&y<5|x+y}", "event_b_comprehension_set(none,[identifier(none,x),identifier(none,y)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testEventBComprehensionSet3() throws BCompoundException {
		checkExpression("{x,y,z·x<y&y<5|x+y}", "event_b_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("{(x,y,z)·x<y&y<5|x+y}", "event_b_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testEventBComprehensionSetDot1() throws BCompoundException {
		checkExpression("{(x).x<5|x*x}", "event_b_comprehension_set(none,[identifier(none,x)],mult_or_cart(none,identifier(none,x),identifier(none,x)),less(none,identifier(none,x),integer(none,5)))");
	}

	@Test
	public void testEventBComprehensionSetDot2() throws BCompoundException {
		checkExpression("{(x,y).x<y&y<5|x+y}", "event_b_comprehension_set(none,[identifier(none,x),identifier(none,y)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testEventBComprehensionSetDot3() throws BCompoundException {
		checkExpression("{(x,y,z).x<y&y<5|x+y}", "event_b_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSet1() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x·x<5|x*x}", "symbolic_event_b_comprehension_set(none,[identifier(none,x)],mult_or_cart(none,identifier(none,x),identifier(none,x)),less(none,identifier(none,x),integer(none,5)))");
		checkExpression("/*@symbolic*/ {(x)·x<5|x*x}", "symbolic_event_b_comprehension_set(none,[identifier(none,x)],mult_or_cart(none,identifier(none,x),identifier(none,x)),less(none,identifier(none,x),integer(none,5)))");
	}

	@Test
	public void testSymbolicEventBComprehensionSet2() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set(none,[identifier(none,x),identifier(none,y)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("/*@symbolic*/ {(x,y)·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set(none,[identifier(none,x),identifier(none,y)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSet3() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y,z·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
		checkExpression("/*@symbolic*/ {(x,y,z)·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSetDot1() throws BCompoundException {
		checkExpression("/*@symbolic*/ {(x).x<5|x*x}", "symbolic_event_b_comprehension_set(none,[identifier(none,x)],mult_or_cart(none,identifier(none,x),identifier(none,x)),less(none,identifier(none,x),integer(none,5)))");
	}

	@Test
	public void testSymbolicEventBComprehensionSetDot2() throws BCompoundException {
		checkExpression("/*@symbolic*/ {(x,y).x<y&y<5|x+y}", "symbolic_event_b_comprehension_set(none,[identifier(none,x),identifier(none,y)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSetDot3() throws BCompoundException {
		checkExpression("/*@symbolic*/ {(x,y,z).x<y&y<5|x+y}", "symbolic_event_b_comprehension_set(none,[identifier(none,x),identifier(none,y),identifier(none,z)],add(none,identifier(none,x),identifier(none,y)),conjunct(none,[less(none,identifier(none,x),identifier(none,y)),less(none,identifier(none,y),integer(none,5))]))");
	}

	@Test
	public void testSubstitutions() throws BCompoundException {
		checkSubstitution("x,y :: BOOL", "becomes_element_of(none,[identifier(none,x),identifier(none,y)],bool_set(none))");
		checkSubstitution("WITNESS 1=1 THEN skip END", "witness_then(none,equal(none,integer(none,1),integer(none,1)),skip(none))");
	}

	@Test
	public void testDefinitions() throws BCompoundException {
		String m = "MACHINE Defs  DEFINITIONS  INV == x:INT;" + "  lt(a) == x<7;  dbl(a) == 2*x*a;  ax(a) == x:=a"
				+ "  VARIABLES x  INVARIANT INV & lt(7)" + "  INITIALISATION x:=dbl(3)  OPERATIONS  op1 = ax(6)"
				+ "  END";
		String expected = "abstract_machine(none,machine(none),machine_header(none,'Defs',[]),"
				+ "[definitions(none,[predicate_definition(none,'INV',[],member(none,identifier(none,x),int_set(none))),"
				+ "predicate_definition(none,lt,[identifier(none,a)],less(none,identifier(none,x),integer(none,7))),"
				+ "expression_definition(none,dbl,[identifier(none,a)],mult_or_cart(none,mult_or_cart(none,integer(none,2),identifier(none,x)),identifier(none,a))),"
				+ "substitution_definition(none,ax,[identifier(none,a)],assign(none,[identifier(none,x)],[identifier(none,a)]))]),"
				+ "variables(none,[identifier(none,x)]),"
				+ "invariant(none,conjunct(none,[definition(none,'INV',[]),definition(none,lt,[integer(none,7)])])),"
				+ "initialisation(none,assign(none,[identifier(none,x)],[definition(none,dbl,[integer(none,3)])])),"
				+ "operations(none,[operation(none,identifier(none,op1),[],[],definition(none,ax,[integer(none,6)]))])])";
		checkMachine(m, expected);
	}

	@Test
	public void testRewrite() throws BCompoundException {
		checkPredicate("0 /= -1", "not_equal(none,integer(none,0),unary_minus(none,integer(none,1)))");
		checkPredicate("NATURAL <: INTEGER", "subset(none,natural_set(none),integer_set(none))");
		checkPredicate("NATURAL /<: INTEGER", "not_subset(none,natural_set(none),integer_set(none))");
		checkPredicate("NATURAL <<: INTEGER", "subset_strict(none,natural_set(none),integer_set(none))");
		checkPredicate("NATURAL /<<: INTEGER", "not_subset_strict(none,natural_set(none),integer_set(none))");
		checkPredicate("#x.(x>0)", "exists(none,[identifier(none,x)],greater(none,identifier(none,x),integer(none,0)))");
	}

	@Test
	public void testTrueFalse() throws BCompoundException {
		checkPredicate("TRUE : BOOL", "member(none,boolean_true(none),bool_set(none))");
		checkPredicate("FALSE : BOOL", "member(none,boolean_false(none),bool_set(none))");

		final ADisjunctPredicate disjunction = new ADisjunctPredicate();
		disjunction.setLeft(new ATruthPredicate());
		disjunction.setRight(new AFalsityPredicate());

		checkAST("disjunct(none,truth(none),falsity(none))", disjunction);
	}

	@Test
	public void testBTrue() throws BCompoundException {
		checkPredicate("btrue", "truth(none)");
	}

	@Test
	public void testBFalse() throws BCompoundException {
		checkPredicate("bfalse", "falsity(none)");
	}

	@Test
	public void testOperationCalls() throws BCompoundException {
		checkSubstitution("do(x)", "operation_call(none,identifier(none,do),[],[identifier(none,x)])");
		checkSubstitution("r <-- do(x)", "operation_call(none,identifier(none,do),[identifier(none,r)],[identifier(none,x)])");
	}

	@Test
	public void testEvent() throws BCompoundException {
		final AEventBModelParseUnit model = new AEventBModelParseUnit();
		model.setName(new TIdentifierLiteral("mm"));
		final AEventsModelClause events = new AEventsModelClause();
		model.setModelClauses(Collections.singletonList(events));
		AEvent event = new AEvent();
		events.setEvent(Collections.singletonList(event));
		event.setEventName(new TIdentifierLiteral("testevent"));
		event.setVariables(Collections.singletonList(createId("param")));
		event.setGuards(Collections.singletonList(new ATruthPredicate()));
		PSubstitution subst1 = new AAssignSubstitution(Collections.singletonList(createId("x")), Collections.singletonList(createId("param")));
		event.setAssignments(Collections.singletonList(subst1));
		PWitness witness = new AWitness(new TIdentifierLiteral("ab"),
				new AEqualPredicate(createId("ab"), createId("y")));
		event.setWitness(Collections.singletonList(witness));
		event.setRefines(Arrays.asList(new TIdentifierLiteral("abstract1"), new TIdentifierLiteral("abstract2")));

		checkAST(
			"event_b_model(none,mm,[events(none,["
						+ "event(none,testevent,[abstract1,abstract2],[identifier(none,param)],[truth(none)],[],"
						+ "[assign(none,[identifier(none,x)],[identifier(none,param)])],"
						+ "[witness(none,identifier(none,ab),equal(none,identifier(none,ab),identifier(none,y)))])])])",
				model);
	}

	@Test
	public void testPartition() {
		final PExpression set = createId("set");
		final PExpression one = new AIntegerExpression(new TIntegerLiteral("1"));
		final PExpression two = new AIntegerExpression(new TIntegerLiteral("2"));
		final PExpression three = new AIntegerExpression(new TIntegerLiteral("3"));
		final APartitionPredicate pred = new APartitionPredicate(set, Arrays.asList(one, two, three));
		final String expected = "partition(none,identifier(none,set),[integer(none,1),integer(none,2),integer(none,3)])";
		checkAST(expected, pred);
	}

	@Test
	public void testOppattern() throws BCompoundException {
		final String pattern = "op1(x,_)";
		final String expected = "oppattern(none,op1,[def(none,identifier(none,x)),undef(none)])";
		checkOppatterns(pattern, expected);
	}

	@Test
	public void testLargeInteger() throws BCompoundException {
		checkExpression("922337203685477580756", "integer(none,922337203685477580756)");
	}

	@Test
	public void testReal() throws BCompoundException {
		// ProB expects an atom
		checkExpression("1.337", "real(none,'1.337')");
	}

	@Test
	public void testString() throws BCompoundException {
		checkExpression("\" \"", "string(none,' ')");
		checkExpression("\"\"", "string(none,'')");
		checkExpression("\"a\"", "string(none,a)");
		checkExpression("\"A\"", "string(none,'A')");
	}

	private PExpression createId(final String name) {
		return new AIdentifierExpression(Collections.singletonList(new TIdentifierLiteral(name)));
	}

	@Test
	public void testFreeType() throws BCompoundException {
		final AConstructorFreetypeConstructor multi = new AConstructorFreetypeConstructor(
				new TIdentifierLiteral("multi"),
				new APowSubsetExpression(new AIntegerSetExpression())
		);
		final AConstructorFreetypeConstructor single = new AConstructorFreetypeConstructor(
				new TIdentifierLiteral("single"),
				new AIntegerSetExpression()
		);
		final AFreetype freetype = new AFreetype(
				new TIdentifierLiteral("T"),
				Collections.emptyList(),
				Arrays.asList(multi, single)
		);
		final AFreetypesMachineClause clause = new AFreetypesMachineClause(Collections.singletonList(freetype));

		checkAST("freetypes(none,[freetype(none,'T',[],[constructor(none,multi,pow_subset(none,integer_set(none))),constructor(none,single,integer_set(none))])])", clause);
	}

}
