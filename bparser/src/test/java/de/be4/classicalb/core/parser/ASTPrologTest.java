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

	private static void checkAST(final int counter, final String expected, final Node ast) {
		@SuppressWarnings("deprecation")
		de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment nodeids = new de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment();
		ast.apply(nodeids);
		assertEquals(insertNumbers(counter, expected), printAST(ast, nodeids));
		assertEquals(insertNonePositions(expected), printAST(ast, new NodeFileNumbers()));
	}

	private static void checkProlog(final int counter, final String bspec, final String expected) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseMachine(bspec);
		checkAST(counter, expected, startNode);
	}

	private static void checkPredicate(final String pred, final String expected) throws BCompoundException {
		checkProlog(2, BParser.PREDICATE_PREFIX + pred, expected);
	}

	private static void checkExpression(final String expr, final String expected) throws BCompoundException {
		checkProlog(2, BParser.EXPRESSION_PREFIX + expr, expected);
	}

	private static void checkSubstitution(final String subst, final String expected) throws BCompoundException {
		checkProlog(2, BParser.SUBSTITUTION_PREFIX + subst, expected);
	}

	private static void checkOppatterns(final String pattern, final String expected) throws BCompoundException {
		checkProlog(1, BParser.OPERATION_PATTERN_PREFIX + pattern, expected);
	}

	private static String insertNumbers(int counter, final String string) {
		StringBuilder buf = new StringBuilder();
		char[] c = string.toCharArray();
		for (char value : c) {
			switch (value) {
				case '$':
					buf.append(counter);
					counter++;
					break;
				case '%':
					buf.append(counter - 1);
					break;
				default:
					buf.append(value);
					break;
			}
		}
		return buf.toString();
	}

	private static String insertNonePositions(final String string) {
		return string.replaceAll("[$%]", "none");
	}

	@Test
	public void testMachine() throws BCompoundException {
		String m = "MACHINE name OPERATIONS op=skip END";
		// TODO: warum taucht hier die 5 zweimal auf?
		// antwort: weil "identifier(...)" kein Knoten im AST ist, sondern eine Liste an Literalen
		// NodeIdAssignment kann nur AST-Knoten eine ID zuweisen und somit verwendet ASTProlog einfach den Op-Knoten nochmal
		String expected = "abstract_machine($,machine($),machine_header($,name,[]),[operations($,[operation($,identifier(%,op),[],[],skip($))])])";
		checkProlog(1, m, expected);
	}

	@Test
	public void testMachine2() throws BCompoundException {
		String m = "MACHINE mname(P)  SETS S; E={e1,e2}" + "  INCLUDES inc(x),rn.inc2  SEES see,s.see2  VARIABLES x"
				+ "  INVARIANT x:NAT  INITIALISATION x:=5" + "  OPERATIONS op=skip; r,s <-- op2(a,b) = skip  END";
		String expected = "abstract_machine($,machine($),machine_header($,mname,[identifier($,'P')]),"
				+ "[sets($,[deferred_set($,'S'),enumerated_set($,'E',[identifier($,e1),identifier($,e2)])]),"
				+ "includes($,[machine_reference($,inc,[identifier($,x)]),machine_reference($,'rn.inc2',[])]),"
				+ "sees($,[identifier($,see),identifier($,'s.see2')])," + "variables($,[identifier($,x)]),"
				+ "invariant($,member($,identifier($,x),nat_set($))),"
				+ "initialisation($,assign($,[identifier($,x)],[integer($,5)])),"
				+ "operations($,[operation($,identifier(%,op),[],[],skip($)),"
				+ "operation($,identifier(%,op2),[identifier($,r),identifier($,s)],"
				+ "[identifier($,a),identifier($,b)],skip($))])])";

		checkProlog(1, m, expected);
	}

	@Test
	public void testRefinement() throws BCompoundException {
		String ref = "REFINEMENT ref REFINES abstract VARIABLES x END";
		String expected = "refinement_machine($,machine_header($,ref,[]),abstract,[variables($,[identifier($,x)])])";
		checkProlog(1, ref, expected);
	}

	@Test
	public void testEmptyString() throws BCompoundException {
		checkExpression("\"test\"+\"\"", "add($,string($,test),string($,''))");
	}

	@Test
	public void testPredicates() throws BCompoundException {
		checkPredicate("5>r.j", "greater($,integer($,5),identifier($,'r.j'))");
		checkPredicate("!x,y.(x<y)",
				"forall($,[identifier($,x),identifier($,y)],less($,identifier($,x),identifier($,y)))");
	}

	@Test
	public void testExpressions() throws BCompoundException {
		checkExpression("SIGMA x,y.(x:NAT & y:INT | x+y)",
				"general_sum($,[identifier($,x),identifier($,y)],"
						+ "conjunct($,[member($,identifier($,x),nat_set($)),member($,identifier($,y),int_set($))]),"
						+ "add($,identifier($,x),identifier($,y)))");
	}

	@Test
	public void testEmptySet() throws BCompoundException {
		checkExpression("∅", "empty_set($)");
		checkExpression("{}", "empty_set($)");
	}

	@Test
	public void testSetExtension() throws BCompoundException {
		checkExpression("{x}", "set_extension($,[identifier($,x)])");
		checkExpression("{(x)}", "set_extension($,[identifier($,x)])");
		checkExpression("{x,y}", "set_extension($,[identifier($,x),identifier($,y)])");
		checkExpression("{(x,y)}", "set_extension($,[couple($,[identifier($,x),identifier($,y)])])");
		checkExpression("{x,y,z}", "set_extension($,[identifier($,x),identifier($,y),identifier($,z)])");
		checkExpression("{(x,y,z)}", "set_extension($,[couple($,[identifier($,x),identifier($,y),identifier($,z)])])");
	}

	@Test
	public void testComprehensionSet1() throws BCompoundException {
		checkExpression("{x|x<5}", "comprehension_set($,[identifier($,x)],less($,identifier($,x),integer($,5)))");
		checkExpression("{(x)|x<5}", "comprehension_set($,[identifier($,x)],less($,identifier($,x),integer($,5)))");
	}

	@Test
	public void testComprehensionSet2() throws BCompoundException {
		checkExpression("{x,y|x<y&y<5}", "comprehension_set($,[identifier($,x),identifier($,y)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("{(x,y)|x<y&y<5}", "comprehension_set($,[identifier($,x),identifier($,y)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testComprehensionSet3() throws BCompoundException {
		checkExpression("{x,y,z|x<y&y<5}", "comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("{(x,y,z)|x<y&y<5}", "comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testSymbolicComprehensionSet1() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x|x<5}", "symbolic_comprehension_set($,[identifier($,x)],less($,identifier($,x),integer($,5)))");
		checkExpression("/*@symbolic*/ {(x)|x<5}", "symbolic_comprehension_set($,[identifier($,x)],less($,identifier($,x),integer($,5)))");
	}

	@Test
	public void testSymbolicComprehensionSet2() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y|x<y&y<5}", "symbolic_comprehension_set($,[identifier($,x),identifier($,y)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("/*@symbolic*/ {(x,y)|x<y&y<5}", "symbolic_comprehension_set($,[identifier($,x),identifier($,y)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testSymbolicComprehensionSet3() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y,z|x<y&y<5}", "symbolic_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("/*@symbolic*/ {(x,y,z)|x<y&y<5}", "symbolic_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testEventBComprehensionSet1() throws BCompoundException {
		checkExpression("{x·x<5|x*x}", "event_b_comprehension_set($,[identifier($,x)],mult_or_cart($,identifier($,x),identifier($,x)),less($,identifier($,x),integer($,5)))");
		checkExpression("{(x)·x<5|x*x}", "event_b_comprehension_set($,[identifier($,x)],mult_or_cart($,identifier($,x),identifier($,x)),less($,identifier($,x),integer($,5)))");
	}

	@Test
	public void testEventBComprehensionSet2() throws BCompoundException {
		checkExpression("{x,y·x<y&y<5|x+y}", "event_b_comprehension_set($,[identifier($,x),identifier($,y)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("{(x,y)·x<y&y<5|x+y}", "event_b_comprehension_set($,[identifier($,x),identifier($,y)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testEventBComprehensionSet3() throws BCompoundException {
		checkExpression("{x,y,z·x<y&y<5|x+y}", "event_b_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("{(x,y,z)·x<y&y<5|x+y}", "event_b_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testEventBComprehensionSetDot1() throws BCompoundException {
		checkExpression("{(x).x<5|x*x}", "event_b_comprehension_set($,[identifier($,x)],mult_or_cart($,identifier($,x),identifier($,x)),less($,identifier($,x),integer($,5)))");
	}

	@Test
	public void testEventBComprehensionSetDot2() throws BCompoundException {
		checkExpression("{(x,y).x<y&y<5|x+y}", "event_b_comprehension_set($,[identifier($,x),identifier($,y)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testEventBComprehensionSetDot3() throws BCompoundException {
		checkExpression("{(x,y,z).x<y&y<5|x+y}", "event_b_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSet1() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x·x<5|x*x}", "symbolic_event_b_comprehension_set($,[identifier($,x)],mult_or_cart($,identifier($,x),identifier($,x)),less($,identifier($,x),integer($,5)))");
		checkExpression("/*@symbolic*/ {(x)·x<5|x*x}", "symbolic_event_b_comprehension_set($,[identifier($,x)],mult_or_cart($,identifier($,x),identifier($,x)),less($,identifier($,x),integer($,5)))");
	}

	@Test
	public void testSymbolicEventBComprehensionSet2() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set($,[identifier($,x),identifier($,y)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("/*@symbolic*/ {(x,y)·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set($,[identifier($,x),identifier($,y)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSet3() throws BCompoundException {
		checkExpression("/*@symbolic*/ {x,y,z·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
		checkExpression("/*@symbolic*/ {(x,y,z)·x<y&y<5|x+y}", "symbolic_event_b_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSetDot1() throws BCompoundException {
		checkExpression("/*@symbolic*/ {(x).x<5|x*x}", "symbolic_event_b_comprehension_set($,[identifier($,x)],mult_or_cart($,identifier($,x),identifier($,x)),less($,identifier($,x),integer($,5)))");
	}

	@Test
	public void testSymbolicEventBComprehensionSetDot2() throws BCompoundException {
		checkExpression("/*@symbolic*/ {(x,y).x<y&y<5|x+y}", "symbolic_event_b_comprehension_set($,[identifier($,x),identifier($,y)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testSymbolicEventBComprehensionSetDot3() throws BCompoundException {
		checkExpression("/*@symbolic*/ {(x,y,z).x<y&y<5|x+y}", "symbolic_event_b_comprehension_set($,[identifier($,x),identifier($,y),identifier($,z)],add($,identifier($,x),identifier($,y)),conjunct($,[less($,identifier($,x),identifier($,y)),less($,identifier($,y),integer($,5))]))");
	}

	@Test
	public void testSubstitutions() throws BCompoundException {
		checkSubstitution("x,y :: BOOL", "becomes_element_of($,[identifier($,x),identifier($,y)],bool_set($))");
	}

	@Test
	public void testDefinitions() throws BCompoundException {
		String m = "MACHINE Defs  DEFINITIONS  INV == x:INT;" + "  lt(a) == x<7;  dbl(a) == 2*x*a;  ax(a) == x:=a"
				+ "  VARIABLES x  INVARIANT INV & lt(7)" + "  INITIALISATION x:=dbl(3)  OPERATIONS  op1 = ax(6)"
				+ "  END";
		String expected = "abstract_machine($,machine($),machine_header($,'Defs',[]),"
				+ "[definitions($,[predicate_definition($,'INV',[],member($,identifier($,x),int_set($))),"
				+ "predicate_definition($,lt,[identifier($,a)],less($,identifier($,x),integer($,7))),"
				+ "expression_definition($,dbl,[identifier($,a)],mult_or_cart($,mult_or_cart($,integer($,2),identifier($,x)),identifier($,a))),"
				+ "substitution_definition($,ax,[identifier($,a)],assign($,[identifier($,x)],[identifier($,a)]))]),"
				+ "variables($,[identifier($,x)]),"
				+ "invariant($,conjunct($,[definition($,'INV',[]),definition($,lt,[integer($,7)])])),"
				+ "initialisation($,assign($,[identifier($,x)],[definition($,dbl,[integer($,3)])])),"
				+ "operations($,[operation($,identifier(%,op1),[],[],definition($,ax,[integer($,6)]))])])";
		checkProlog(1, m, expected);
	}

	@Test
	public void testRewrite() throws BCompoundException {
		checkPredicate("0 /= -1", "not_equal($,integer($,0),unary_minus($,integer($,1)))");
		checkPredicate("NATURAL <: INTEGER", "subset($,natural_set($),integer_set($))");
		checkPredicate("NATURAL /<: INTEGER", "not_subset($,natural_set($),integer_set($))");
		checkPredicate("NATURAL <<: INTEGER", "subset_strict($,natural_set($),integer_set($))");
		checkPredicate("NATURAL /<<: INTEGER", "not_subset_strict($,natural_set($),integer_set($))");
		checkPredicate("#x.(x>0)", "exists($,[identifier($,x)],greater($,identifier($,x),integer($,0)))");
	}

	@Test
	public void testTrueFalse() throws BCompoundException {
		checkPredicate("TRUE : BOOL", "member($,boolean_true($),bool_set($))");
		checkPredicate("FALSE : BOOL", "member($,boolean_false($),bool_set($))");

		final ADisjunctPredicate disjunction = new ADisjunctPredicate();
		disjunction.setLeft(new ATruthPredicate());
		disjunction.setRight(new AFalsityPredicate());

		checkAST(0, "disjunct($,truth($),falsity($))", disjunction);
	}

	@Test
	public void testBTrue() throws BCompoundException {
		checkPredicate("btrue", "truth($)");
	}

	@Test
	public void testBFalse() throws BCompoundException {
		checkPredicate("bfalse", "falsity($)");
	}

	@Test
	public void testOperationCalls() throws BCompoundException {
		checkSubstitution("do(x)", "operation_call($,identifier($,do),[],[identifier($,x)])");
		checkSubstitution("r <-- do(x)", "operation_call($,identifier(%,do),[identifier($,r)],[identifier($,x)])");
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

		checkAST(0,
				"event_b_model($,mm,[events($,["
						+ "event($,testevent,[abstract1,abstract2],[identifier($,param)],[truth($)],[],"
						+ "[assign($,[identifier($,x)],[identifier($,param)])],"
						+ "[witness($,identifier(%,ab),equal($,identifier($,ab),identifier($,y)))])])])",
				model);
	}

	@Test
	public void testPartition() {
		final PExpression set = createId("set");
		final PExpression one = new AIntegerExpression(new TIntegerLiteral("1"));
		final PExpression two = new AIntegerExpression(new TIntegerLiteral("2"));
		final PExpression three = new AIntegerExpression(new TIntegerLiteral("3"));
		final APartitionPredicate pred = new APartitionPredicate(set, Arrays.asList(one, two, three));
		final String expected = "partition($,identifier($,set),[integer($,1),integer($,2),integer($,3)])";
		checkAST(0, expected, pred);
	}

	@Test
	public void testOppattern() throws BCompoundException {
		final String pattern = "op1(x,_)";
		final String expected = "oppattern($,op1,[def($,identifier($,x)),undef($)])";
		checkOppatterns(pattern, expected);
	}

	@Test
	public void testLargeInteger() throws BCompoundException {
		checkExpression("922337203685477580756", "integer($,922337203685477580756)");
	}

	@Test
	public void testReal() throws BCompoundException {
		// ProB expects an atom
		checkExpression("1.337", "real($,'1.337')");
	}

	@Test
	public void testString() throws BCompoundException {
		checkExpression("\" \"", "string($,' ')");
		checkExpression("\"\"", "string($,'')");
		checkExpression("\"a\"", "string($,a)");
		checkExpression("\"A\"", "string($,'A')");
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

		checkAST(0, "freetypes($,[freetype($,'T',[],[constructor($,multi,pow_subset($,integer_set($))),constructor($,single,integer_set($))])])", clause);
	}

}
