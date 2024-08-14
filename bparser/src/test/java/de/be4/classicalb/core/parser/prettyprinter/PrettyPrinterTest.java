package de.be4.classicalb.core.parser.prettyprinter;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ParseOptions;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class PrettyPrinterTest {

	private static void testRoundtrip(String code, Parser parser) throws BCompoundException {
		Start parse = parser.parse(code);
		PrettyPrinter prettyprinter = new PrettyPrinter();
		parse.apply(prettyprinter);
		String prettyPrint = prettyprinter.getPrettyPrint();

		Start parse2 = parser.parse(prettyPrint);
		PrettyPrinter prettyprinter2 = new PrettyPrinter();
		parse2.apply(prettyprinter2);
		String prettyPrint2 = prettyprinter2.getPrettyPrint();

		assertEquals(Helpers.getTreeAsPrologTerm(parse), Helpers.getTreeAsPrologTerm(parse2));
		assertEquals(prettyPrint, prettyPrint2);
	}

	private static void testRoundtripRaw(String code, Parser parser) throws BCompoundException {
		ParseOptions parseOptions = new ParseOptions();
		parseOptions.setCollectDefinitions(false);
		parseOptions.setApplyASTTransformations(false);
		parseOptions.setApplySemanticChecks(false);
		Start rawParse = parser.parse(code, parseOptions);
		PrettyPrinter prettyprinter = new PrettyPrinter();
		rawParse.apply(prettyprinter);
		String rawPrettyPrint = prettyprinter.getPrettyPrint();

		Start parse1 = parser.parse(code);
		PrettyPrinter prettyprinter1 = new PrettyPrinter();
		parse1.apply(prettyprinter1);
		String prettyPrint1 = prettyprinter1.getPrettyPrint();

		Start parse2 = parser.parse(rawPrettyPrint);
		PrettyPrinter prettyprinter2 = new PrettyPrinter();
		parse2.apply(prettyprinter2);
		String prettyPrint2 = prettyprinter2.getPrettyPrint();

		assertEquals(Helpers.getTreeAsPrologTerm(parse1), Helpers.getTreeAsPrologTerm(parse2));
		assertEquals(prettyPrint1, prettyPrint2);
	}

	@FunctionalInterface
	private interface Parser {
		default Start parse(String code) throws BCompoundException {
			return parse(code, new ParseOptions());
		}

		Start parse(String code, ParseOptions parseOptions) throws BCompoundException;
	}

	@RunWith(Parameterized.class)
	public static class ExpressionTest {

		@Parameterized.Parameter
		public String code;

		@Parameterized.Parameters(name = "{0}")
		public static Object[] data() {
			return new Object[] {
				"5+6",
				"5",
				"5-6",
				"4+5+6",
				"4+5-6",
				"4+5*6",
				"4*5+6*7",
				"-5+6",
				"-5",
				"-5-6",
				"-4+5+6",
				"-4+5-6",
				"-4+5*6",
				"-4*5+6*7",
				"5+-6",
				"5--6",
				"4+-5+6",
				"4+-5-6",
				"4+-5*6",
				"4*-5+6*7",
				"A\\B",
				"5*6",
				"5/6",
				"6 mod 5",
				"6**5",
				"A<->B",
				"A+->B",
				"A-->B",
				"A>+>B",
				"A>->B",
				"A+->>B",
				"A-->>B",
				"A>+>>B",
				"A>->>B",
				"A<<->B",
				"A<->>B",
				"A<<->>B",
				"A<+B",
				"A><B",
				"A^B",
				"A<|B",
				"A<<|B",
				"A|>B",
				"A|>>B",
				"A->B",
				"A<-B",
				"A\\/B",
				"A/\\B",
				"A/|\\B",
				"A\\|/B",
				"(2,3)",
				"5..20",
				"-x",
				"A~",
				"A[B]",
				"a'b",
				"A(b)",
				"(A~)~",
				"A~[B]",
				"(A~)'b", // type error, but syntactically valid
				"A~(b)",
				"A[B]~",
				"A[B][C]",
				"A[B]'c", // type error, but syntactically valid
				"A[B](c)",
				"a'b~",
				"a'b[C]",
				"a'b'c",
				"a'b(c)",
				"A(b)~",
				"A(b)[C]",
				"A(b)'c",
				"A(b)(c)",
				"(A||B)",
				"(f;g)",
				"g∘f",
				"bool(x<0)",
				"max({1,2,3})",
				"min({1,2,3})",
				"card({1,2,3})",
				"floor(1.5)",
				"ceiling(1.5)",
				"real(1)",
				"SIGMA(y,z).(z<5|z*z)",
				"PI(a,b,c).(a<0&x<5|x*a*b*c)",
				"POW(A<->B)",
				"POW1(A<->B)",
				"FIN(A\\/B)",
				"FIN1(B/\\A)",
				"union(z)",
				"inter(A)",
				"id(A)",
				"closure(A)",
				"dom((1,2))",
				"ran((1,2))",
				"%x,y.(x<y|E)",
				"fnc(foo)",
				"rel(A<->POW(B))",
				"seq(S)",
				"seq1(S)",
				"iseq(S)",
				"iseq1(S)",
				"perm(S)",
				"perm([])",
				"size(S)",
				"first(S)",
				"last(S)",
				"front(S)",
				"tail(S)",
				"rev(S)",
				"prj1(S,T)",
				"prj1(1|->2)",
				"@prj1",
				"@prj1[{1|->2}]",
				"prj2(S,T)",
				"prj2(1|->2)",
				"@prj2",
				"@prj2[{1|->2}]",
				"iterate(A<->B,5)",
				"{x}",
				"{x,y}",
				"{x,y,z}",
				"{(x)}",
				"{(x,y)}",
				"{(x,y,z)}",
				"{x|x<y&x<5}",
				"{x,y|x<y&x<5}",
				"{x,y,z|x<y&x<5}",
				"{(x)|x<y&x<5}",
				"{(x,y)|x<y&x<5}",
				"{(x,y,z)|x<y&x<5}",
				"/*@symbolic*/ {x|x<y&x<5}",
				"/*@symbolic*/ {x,y|x<y&x<5}",
				"/*@symbolic*/ {x,y,z|x<y&x<5}",
				"/*@symbolic*/ {(x)|x<y&x<5}",
				"/*@symbolic*/ {(x,y)|x<y&x<5}",
				"/*@symbolic*/ {(x,y,z)|x<y&x<5}",
				"{x·x<y&x<5|x+y}",
				"{x,y·x<y&x<5|x+y}",
				"{x,y,z·x<y&x<5|x+y}",
				"{(x)·x<y&x<5|x+y}",
				"{(x,y)·x<y&x<5|x+y}",
				"{(x,y,z)·x<y&x<5|x+y}",
				"{(x).x<y&x<5|x+y}",
				"{(x,y).x<y&x<5|x+y}",
				"{(x,y,z).x<y&x<5|x+y}",
				"/*@symbolic*/ {x·x<y&x<5|x+y}",
				"/*@symbolic*/ {x,y·x<y&x<5|x+y}",
				"/*@symbolic*/ {x,y,z·x<y&x<5|x+y}",
				"/*@symbolic*/ {(x)·x<y&x<5|x+y}",
				"/*@symbolic*/ {(x,y)·x<y&x<5|x+y}",
				"/*@symbolic*/ {(x,y,z)·x<y&x<5|x+y}",
				"/*@symbolic*/ {(x).x<y&x<5|x+y}",
				"/*@symbolic*/ {(x,y).x<y&x<5|x+y}",
				"/*@symbolic*/ {(x,y,z).x<y&x<5|x+y}",
				"UNION z.(x<y|E)",
				"UNION x,y.(z<w|E)",
				"UNION x,y,z.(z<w|E)",
				"UNION(z).(x<y|E)",
				"UNION(x,y).(z<w|E)",
				"UNION(x,y,z).(z<w|E)",
				"INTER z.(x<y|E)",
				"INTER x,y.(z<w|E)",
				"INTER x,y,z.(z<w|E)",
				"INTER(z).(x<y|E)",
				"INTER(x,y).(z<w|E)",
				"INTER(x,y,z).(z<w|E)",
				"perm([a,b,c])",
				"conc(S)",
				"tree(x)",
				"btree(x)",
				"const(x, y)",
				"top(x)",
				"sons(x)",
				"prefix(x)",
				"postfix(x)",
				"sizet(x)",
				"mirror(x)",
				"rank(x, y)",
				"father(x, y)",
				"son(x, y, z)",
				"subtree(x, y)",
				"arity(x, y)",
				"bin(x)",
				"bin(x, y, z)",
				"left(x)",
				"right(x)",
				"infix(x)",
				"TRUE",
				"MAXINT",
				"MININT",
				"{}",
				"INTEGER",
				"REAL",
				"FLOAT",
				"NATURAL",
				"NATURAL1",
				"NAT",
				"NAT1",
				"INT",
				"BOOL",
				"STRING",
				"\"a String\"",
				"foo(bar,3)",
				"succ <+ {1|->1, 3|->3}(3)",
				"ll(dd'right_sect)",
				"{1|->1} \\/ {3|->3}~",
				"{1|->1} \\/ {3|->3}[INT]",
				"{1|->1} \\/ {3|->3}(3)",
				"({1|->1} \\/ {3|->3})~",
				"({1|->1} \\/ {3|->3})[INT]",
				"({1|->1} \\/ {3|->3})(3)",
				"LET i BE i=1 IN {i|->i} END~",
				"LET i BE i=1 IN {i|->i} END[INT]",
				"LET i BE i=1 IN {i|->i} END(1)",
				"(LET i BE i=1 IN {i|->i} END \\/ LET i BE i=3 IN {i|->i} END)~",
				"(LET i BE i=1 IN {i|->i} END \\/ LET i BE i=3 IN {i|->i} END)[INT]",
				"(LET i BE i=1 IN {i|->i} END \\/ LET i BE i=3 IN {i|->i} END)(1)",
				"LET i BE i=1 IN rec(x:i) END'x",
				"x",
				"rec(left_sect:River1,right_sect:Lock1)",
				"struct(left_sect:River1,right_sect:Lock1)",
				"a-b-c",
				"a+b+c",
				"(a**b)**c",
				"a+(b+c)",
				"A\\(B\\C)",
				"a-(b-c)",
				"a+(b*c)",
				"-a-b-c",
				"-a+b+c",
				"-(a**b)**c",
				"(-(a**b))**c",
				"-a+(b+c)",
				"-a-(b-c)",
				"-a+(b*c)",
				"-(a-b-c)",
				"-(a+b+c)",
				"-((a**b)**c)",
				"-(a+(b+c))",
				"-(a-(b-c))",
				"-(a+(b*c))",
				"a--b-c",
				"a+-b+c",
				"(a**-b)**c",
				"a+-(b+c)",
				"a--(b-c)",
				"a+-(b*c)",
				"a+(-b+c)",
				"a-(-b-c)",
				"a+(-b*c)",
				"dom({((1,2),3), ((2,4),5), ((23,45),23)})",
				"{6} <<| {6|->3, 9|->2}",
				"{1, 2} <| {1 |-> 2, 2 |-> 3, 1 |-> 4, 3 |-> 7, 5 |-> 9}",
				"{1, 2} <<| {1 |-> 2, 2 |-> 3, 1 |-> 4, 3 |-> 7, 5 |-> 9}",
				"{1 |-> 2, 2 |-> 3, 1 |-> 4, 3 |-> 7, 5 |-> 9} |> {4, 7, 9}",
				"{1 |-> 2, 2 |-> 3, 1 |-> 4, 3 |-> 7, 5 |-> 9} |>> {4, 7, 9}",
				"{1 |-> 2, 2 |-> 3, 1 |-> 4, 3 |-> 7, 5 |-> 9}[{1, 2}]",
				"{1 |-> 2, 2 |-> 3, 1 |-> 4, 3 |-> 7, 5 |-> 9}~",
				"{3 |-> 5, 3 |-> 9, 6 |-> 3, 9 |-> 2} <+ {2 |-> 7, 3 |-> 4, 5 |-> 1, 9 |-> 5}",
				"{8|->10, 7|->11, 2|->11, 6|->12}><{1|->20, 7|->20, 2|->21, 1|->22}",
				"(4+5)*6",
				"4*(5+6)",
				"(4+5)*(6+7)",
				"{1}*({2}*{3})",
				"({1}*{2})*{3}",
				"{1}×({2}×{3})",
				"({1}×{2})×{3}",
				"2**(3+4)",
				"2**3**4**5",
				"bool(B=TRUE => (3>2 & 4+4=8))",
				"bool((B=TRUE => 3>2) & 4+4=8)",
				"bool((C=TRUE => 3>2) & (C=FALSE => 2>3))",
				"bool(A=TRUE or B=TRUE & C=TRUE => D=TRUE <=> E=TRUE)",
				"bool((A=TRUE or B=TRUE) & (C=TRUE => (D=TRUE <=> E=TRUE)))",
				"LET x,y BE x=1 & y=2 IN x+y END",
				"bool(LET x,y BE x=1 & y=2 IN x+y=3 END)",
				"`MACHINE`",
				"`or`",
				"`floor`",
				"`identifier with spaces`",
				"`([{}])`",
				"`([{`",
				"`/*`",
				"`MACHINE`.`REFINEMENT`.`IMPLEMENTATION`",
				"`a\\\\b\\nc`",
				"NAT +-> (ID <-> ID)",
				"IF x=1 THEN a ELSE d END",
				"IF x=1 THEN a ELSIF x=2 THEN b ELSE d END",
				"IF x=1 THEN a ELSIF x=2 THEN b ELSIF x=3 THEN c ELSE d END",
				"IF x=1 THEN (IF y=1 THEN a ELSE b END) ELSE d END",
				"IF x=1 THEN a ELSE (IF y=1 THEN d ELSE e END) END",
				"IF x=1 THEN (IF y=1 THEN a ELSE b END) ELSIF x=2 THEN c ELSE d END",
				"IF x=1 THEN a ELSIF x=2 THEN (IF y=1 THEN b ELSE c END) ELSE d END",
				"IF x=1 THEN a ELSIF x=2 THEN b ELSE (IF y=1 THEN d ELSE e END) END",
				"(1,2)",
				"(1,2,3)",
				"((1,2),3)",
				"(1,(2,3))",
				"(1,2,3,4)",
				"((1,2),(3,4))",
				"(1|->2)",
				"(1|->2|->3)",
				"((1|->2)|->3)",
				"(1|->(2|->3))",
				"(1|->2|->3|->4)",
				"((1|->2)|->(3|->4))",
				"LET x /*@desc thing */ BE x=1 IN x END",
				"x-x$0",
				"1.0+2.3",
			};
		}

		@Test
		public void test() throws Exception {
			testRoundtrip(code, (s, o) -> new BParser(null, o).parseExpression(s));
		}
	}

	@RunWith(Parameterized.class)
	public static class PredicateTest {

		@Parameterized.Parameter
		public String code;

		@Parameterized.Parameters(name = "{0}")
		public static Object[] data() {
			return new Object[] {
				"x<y",
				"a<b&b<c",
				"x=y",
				"x:NAT",
				"A<:B",
				"A<<:B",
				"A/<:B",
				"A/<<:B",
				"x/=y",
				"x/:NAT",
				"x<=y",
				"x>y",
				"x>=y",
				"!X,Y.(X:NAT&Y:NAT=>x<y)",
				"#X,Y.(X:NAT&Y:NAT=>x<y)",
				"1=4 or 12=19",
				"1=4 => 12=19",
				"x=1 => y=2 => z=3",
				"x=1 => (y=2 => z=3)",
				"1=4 <=> 12=19",
				"not(7=3)",
				"@finite(B/\\A)",
				"@partition(S)",
				"@partition(S, {a})",
				"@partition(S, {a}, {b})",
				"@partition(S, {a}, {b}, {c,d}, {e,f,g})",
				"!y.(y:DOM => !(x1,x2).(x1:DOM & x1<x2 & x2:DOM  => (Board(x1)(y) /= Board(x2)(y) &	Board(y)(x1) /= Board(y)(x2))))&!(s1,s2).(s1:SUBSQ & s2:SUBSQ => !(x1,y1,x2,y2).( (x1:s1 & x2:s1 & x1>=x2 & (x1=x2 => y1>y2) & y1:s2 & y2:s2 & (x1,y1) /= (x2,y2)) => Board(x1)(y1) /= Board(x2)(y2)))",
				"!(i1,j1,i2,j2).(( i1>0 & i2>0 & j1<=n & j2 <= n & i1<j1 & i2<j2 & (i1,j1) /= (i2,j2) & i1<=i2 & (i1=i2 => j1<j2)) => (a(j1)-a(i1) /= a(j2)-a(i2)))",
				"x+1:NAT",
				"1=1 /*@desc truth */",
				"/*@label axm1 */ 1=1",
				"[a := 1] a=1",
				"btrue",
				"bfalse",
				"IF mode=0 THEN value=1 ELSE value=2 END",
				"IF mode=0 THEN value=1 ELSIF mode=1 THEN value=2 ELSE value=3 END",
				"IF mode=0 THEN value=1 ELSIF mode=1 THEN value=2 ELSIF mode=2 THEN value=3 ELSE value=4 END",
			};
		}

		@Test
		public void test() throws Exception {
			testRoundtrip(code, (s, o) -> new BParser(null, o).parsePredicate(s));
		}
	}

	@RunWith(Parameterized.class)
	public static class RawPredicateTest {

		@Parameterized.Parameter
		public String code;

		@Parameterized.Parameters(name = "{0}")
		public static Object[] data() {
			return new Object[] {
				"1=1",
				"IF mode=0 THEN value=1 ELSE value=2 END",
				"IF mode=0 THEN value=1 ELSIF mode=1 THEN value=2 ELSE value=3 END",
				"IF mode=0 THEN value=1 ELSIF mode=1 THEN value=2 ELSIF mode=2 THEN value=3 ELSE value=4 END",
			};
		}

		@Test
		public void test() throws Exception {
			testRoundtripRaw(code, (s, o) -> new BParser(null, o).parsePredicate(s));
		}
	}

	@RunWith(Parameterized.class)
	public static class SubstitutionTest {

		@Parameterized.Parameter
		public String code;

		@Parameterized.Parameters(name = "{0}")
		public static Object[] data() {
			return new Object[] {
				"x:=3",
				"x,y:=y,x",
				"skip",
				"f(x) := E",
				"x :: S",
				"xxxx : (xxxx=iv)",
				"x,y :: S",
				"x <-- OP(x)",
				"x := 5 || skip",
				"u :: P || x,y := t,x || f(3) := 9",
				"u :: P ; x,y := t,x ; f(3) := 9",
				"ANY x,y WHERE x<8 & y > 4 THEN z := x * y END",
				"LET x,y BE x=3 & y=n IN z := x+y END",
				"VAR x,y IN t := f(x,y) END",
				"PRE p<9 THEN x := 9 END",
				"ASSERT x=9 THEN y := x + 1 END",
				"CHOICE x := 1 OR x := 2 END",
				"CHOICE x := 1 OR x := 2 OR t := 0 END",
				"IF P=9 THEN G:=3 END",
				"IF P=9 THEN G:=3 ELSE G := G -1 END",
				"IF P=9 THEN G:=3 ELSIF u < 9 THEN s := u ELSE G := G -1 END",
				"SELECT P=9 THEN G:=3 WHEN u < 9 THEN s := u END",
				"SELECT P=9 THEN G:=3 WHEN u < 9 THEN s := u ELSE skip END",
				"CASE t OF EITHER 1 THEN y:= 7 OR 2,3,4 THEN q := 9 END END",
				"CASE t OF EITHER 1 THEN y:= 7 OR 2,3,4 THEN q := 9 ELSE a := 0 END END",
				"WHILE x > 9 DO x := x - 1 INVARIANT x > 0 VARIANT x END",
				"BEGIN skip END",
				"BEGIN skip; x := 42 END",
				"BEGIN skip; BEGIN x := 42 END END",
				"BEGIN BEGIN skip END; x := 42 END",
				"BEGIN BEGIN skip END; BEGIN x := 42 END END",
				"WITNESS 1=1 THEN skip END",
			};
		}

		@Test
		public void test() throws Exception {
			testRoundtrip(code, (s, o) -> new BParser(null, o).parseSubstitution(s));
		}
	}
}
