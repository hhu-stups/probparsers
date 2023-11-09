package de.prob.unicode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.prob.unicode.UnicodeTranslator;

public class ToAsciiTest {

	@Test
	public void TIn() {
        assertEquals(":", UnicodeTranslator.toAscii("\u2208"));
	}

	@Test
	public void TNotsubseteq() {
        assertEquals("/<:", UnicodeTranslator.toAscii("\u2288"));
	}

	@Test
	public void TNotsubset() {
        assertEquals("/<<:", UnicodeTranslator.toAscii("\u2284"));
	}

	@Test
	public void TSubseteq() {
        assertEquals("<:", UnicodeTranslator.toAscii("\u2286"));
	}

	@Test
	public void TSetminus() {
        assertEquals("\\", UnicodeTranslator.toAscii("\u2216"));
	}

	@Test
	public void TDotdot() {
        assertEquals("..", UnicodeTranslator.toAscii("\u2025"));
	}

	@Test
	public void TNat() {
        assertEquals("NAT", UnicodeTranslator.toAscii("\u2115"));
	}

	@Test
	public void TEmptyset() {
        assertEquals("{}", UnicodeTranslator.toAscii("\u2205"));
	}

	@Test
	public void TBcmsuch() {
        assertEquals(":|", UnicodeTranslator.toAscii(":\u2223"));
	}

	@Test
	public void TBfalse() {
        assertEquals("false", UnicodeTranslator.toAscii("\u22a5"));
	}

	@Test
	public void TForall() {
        assertEquals("!", UnicodeTranslator.toAscii("\u2200"));
	}

	@Test
	public void TExists() {
        assertEquals("#", UnicodeTranslator.toAscii("\u2203"));
	}

	@Test
	public void TMapsto() {
        assertEquals("|->", UnicodeTranslator.toAscii("\u21a6"));
	}

	@Test
	public void TBtrue() {
        assertEquals("true", UnicodeTranslator.toAscii("\u22a4"));
	}

	@Test
	public void TSubset() {
        assertEquals("<<:", UnicodeTranslator.toAscii("\u2282"));
	}

	@Test
	public void TBunion() {
        assertEquals("\\/", UnicodeTranslator.toAscii("\u222a"));
	}

	@Test
	public void TBinter() {
        assertEquals("/\\", UnicodeTranslator.toAscii("\u2229"));
	}

	@Test
	public void TDomres() {
        assertEquals("<|", UnicodeTranslator.toAscii("\u25c1"));
	}

	@Test
	public void TRanres() {
        assertEquals("|>", UnicodeTranslator.toAscii("\u25b7"));
	}

	@Test
	public void TDomsub() {
        assertEquals("<<|", UnicodeTranslator.toAscii("\u2a64"));
	}

	@Test
	public void TRansub() {
        assertEquals("|>>", UnicodeTranslator.toAscii("\u2a65"));
	}

	@Test
	public void TLambda() {
        assertEquals("%", UnicodeTranslator.toAscii("\u03bb"));
	}

	@Test
	public void TOftype() {
        assertEquals("oftype", UnicodeTranslator.toAscii("\u2982"));
	}

	@Test
	public void TNotin() {
        assertEquals("/:", UnicodeTranslator.toAscii("\u2209"));
	}

	@Test
	public void TCprod() {
        assertEquals("**", UnicodeTranslator.toAscii("\u00d7"));
	}

	@Test
	public void TUnion() {
        assertEquals("UNION", UnicodeTranslator.toAscii("\u22c3"));
	}

	@Test
	public void TInter() {
        assertEquals("INTER", UnicodeTranslator.toAscii("\u22c2"));
	}

	@Test
	public void TFcomp() {
        assertEquals(";", UnicodeTranslator.toAscii("\u003b"));
	}

	@Test
	public void TBcomp() {
        assertEquals("circ", UnicodeTranslator.toAscii("\u2218"));
	}

	@Test
	public void TTotalSurjectiveRel() {
        assertEquals("<<->>", UnicodeTranslator.toAscii("\ue102"));
	}

	@Test
	public void TDprod() {
        assertEquals("><", UnicodeTranslator.toAscii("\u2297"));
	}

	@Test
	public void TPprod() {
        assertEquals("||", UnicodeTranslator.toAscii("\u2225"));
	}

	@Test
	public void TBcmeq() {
        assertEquals(":=", UnicodeTranslator.toAscii("\u2254"));
	}

	@Test
	public void TBcmin() {
        assertEquals("::", UnicodeTranslator.toAscii(":\u2208"));
	}

	@Test
	public void TIntg() {
        assertEquals("INT", UnicodeTranslator.toAscii("\u2124"));
	}

	@Test
	public void TLand() {
        assertEquals("&", UnicodeTranslator.toAscii("\u2227"));
	}

	@Test
	public void TLimp() {
        assertEquals("=>", UnicodeTranslator.toAscii("\u21d2"));
	}

	@Test
	public void TLeqv() {
        assertEquals("<=>", UnicodeTranslator.toAscii("\u21d4"));
	}

	@Test
	public void TLnot() {
        assertEquals("not", UnicodeTranslator.toAscii("\u00ac"));
	}

	@Test
	public void TQdot() {
        assertEquals(".", UnicodeTranslator.toAscii("\u00b7"));
	}

	@Test
	public void TConv() {
        assertEquals("~", UnicodeTranslator.toAscii("\u223c"));
	}

	@Test
	public void TTotalRel() {
        assertEquals("<<->", UnicodeTranslator.toAscii("\ue100"));
	}

	@Test
	public void TSurjectiveRel() {
        assertEquals("<->>", UnicodeTranslator.toAscii("\ue101"));
	}

	@Test
	public void TPfun() {
        assertEquals("+->", UnicodeTranslator.toAscii("\u21f8"));
	}

	@Test
	public void TTfun() {
        assertEquals("-->", UnicodeTranslator.toAscii("\u2192"));
	}

	@Test
	public void TPinj() {
        assertEquals(">+>", UnicodeTranslator.toAscii("\u2914"));
	}

	@Test
	public void TTinj() {
        assertEquals(">->", UnicodeTranslator.toAscii("\u21a3"));
	}

	@Test
	public void TPsur() {
        assertEquals("+>>", UnicodeTranslator.toAscii("\u2900"));
	}

	@Test
	public void TTsur() {
        assertEquals("->>", UnicodeTranslator.toAscii("\u21a0"));
	}

	@Test
	public void TTbij() {
        assertEquals(">->>", UnicodeTranslator.toAscii("\u2916"));
	}

	@Test
	public void TExpn() {
        assertEquals("^", UnicodeTranslator.toAscii("\u005e"));
	}

	@Test
	public void TLor() {
        assertEquals("or", UnicodeTranslator.toAscii("\u2228"));
	}

	@Test
	public void TPow() {
        assertEquals("POW", UnicodeTranslator.toAscii("\u2119"));
	}

	@Test
	public void TMid() {
        assertEquals("|", UnicodeTranslator.toAscii("\u2223"));
	}

	@Test
	public void TNeq() {
        assertEquals("/=", UnicodeTranslator.toAscii("\u2260"));
	}

	@Test
	public void TRel() {
        assertEquals("<->", UnicodeTranslator.toAscii("\u2194"));
	}

	@Test
	public void TOvl() {
        assertEquals("<+", UnicodeTranslator.toAscii("\ue103"));
	}

	@Test
	public void TLeq() {
        assertEquals("<=", UnicodeTranslator.toAscii("\u2264"));
	}

	@Test
	public void TGeq() {
        assertEquals(">=", UnicodeTranslator.toAscii("\u2265"));
	}

	@Test
	public void TDiv() {
        assertEquals("/", UnicodeTranslator.toAscii("\u00f7"));
	}

	@Test
	public void TMult() {
        assertEquals("*", UnicodeTranslator.toAscii("\u2217"));
	}

	@Test
	public void TMinus() {
        assertEquals("-", UnicodeTranslator.toAscii("\u2212"));
	}

	@Test
	public void TComma() {
        assertEquals(",", UnicodeTranslator.toAscii(","));
	}

	/*--------------------------------------------------------------*/

	@Test
	public void Conjunction() {
        assertEquals("P & Q", UnicodeTranslator.toAscii("P \u2227 Q"));
	}

	@Test
	public void Disjunction() {
        assertEquals("P or Q", UnicodeTranslator.toAscii("P \u2228 Q"));
	}

	@Test
	public void Implication() {
        assertEquals("P => Q", UnicodeTranslator.toAscii("P \u21d2 Q"));
	}

	@Test
	public void Equivalence() {
        assertEquals("P <=> Q", UnicodeTranslator.toAscii("P \u21d4 Q"));
	}

	@Test
	public void Negation() {
        assertEquals("not P", UnicodeTranslator.toAscii("\u00ac P"));
	}

	@Test
	public void UniversalQuantification() {
		// XXX really intended!?
        assertEquals("!(z).(P => Q)", UnicodeTranslator.toAscii("!(z).(P => Q)"));
        assertEquals("!(z).(P => Q)", UnicodeTranslator.toAscii("\u2200(z)\u00b7(P \u21d2 Q)"));
	}

	@Test
	public void UniversalQuantification2() {
        assertEquals("(!z.P => Q)", UnicodeTranslator.toAscii("(\u2200z\u00b7P \u21d2 Q)"));
	}

	@Test
	public void ExistentialQuantification() {
        assertEquals("#(z).(P & Q)", UnicodeTranslator.toAscii("\u2203(z)\u00b7(P \u2227 Q)"));
	}

	@Test
	public void ExistentialQuantification2() {
        assertEquals("(#z.P & Q)", UnicodeTranslator.toAscii("(\u2203z\u00b7P \u2227 Q)"));
	}

	@Test
	public void Substitution() {
        assertEquals("[G] P", UnicodeTranslator.toAscii("[G] P"));
	}

	@Test
	public void Equality() {
        assertEquals("E = F", UnicodeTranslator.toAscii("E = F"));
	}

	@Test
	public void Inequality() {
        assertEquals("E /= F", UnicodeTranslator.toAscii("E \u2260 F"));
	}

	@Test
	public void SingletonSet() {
        assertEquals("{E}", UnicodeTranslator.toAscii("{E}"));
	}

	@Test
	public void SetEnumeration() {
        assertEquals("{E, F}", UnicodeTranslator.toAscii("{E, F}"));
	}

	@Test
	public void EmptySet() {
        assertEquals("{}", UnicodeTranslator.toAscii("\u2205"));
	}

	@Test
	public void SetComprehension() {
        assertEquals("{z | P}", UnicodeTranslator.toAscii("{z \u2223 P}"));
	}

	@Test
	public void SetComprehension2() {
        assertEquals("{z . P | F}", UnicodeTranslator.toAscii("{z \u00b7 P \u2223 F}"));
	}

	@Test
	public void SetComprehension3() {
        assertEquals("{F | P}", UnicodeTranslator.toAscii("{F \u2223 P}"));
	}

	@Test
	public void SetComprehension4() {
        assertEquals("{x | P}", UnicodeTranslator.toAscii("{x \u2223 P}"));
	}

	@Test
	public void Union() {
        assertEquals("S \\/ T", UnicodeTranslator.toAscii("S \u222a T"));
	}

	@Test
	public void Intersection() {
        assertEquals("S /\\ T", UnicodeTranslator.toAscii("S \u2229 T"));
	}

	@Test
	public void Difference() {
        assertEquals("S-T", UnicodeTranslator.toAscii("S\u2212T"));
	}

	@Test
	public void Difference2() {
        assertEquals("S\\T", UnicodeTranslator.toAscii("S\\T"));
	}

	@Test
	public void OrderedPair() {
        assertEquals("E |-> F", UnicodeTranslator.toAscii("E \u21a6 F"));
	}

	@Test
	public void CartesianProduct() {
		// XXX why \u2217 '*' and not \u00d7 'x'?
        assertEquals("S * T", UnicodeTranslator.toAscii("S \u2217 T"));
	}

	@Test
	public void CartesianProduct2() {
        assertEquals("S ** T", UnicodeTranslator.toAscii("S \u00d7 T"));
	}

	@Test
	public void Powerset() {
        assertEquals("POW(S)", UnicodeTranslator.toAscii("\u2119(S)"));
	}

	// XXX NonEmptySubsets not provided? What's the unicode character? \u2119
	// and \u2081
	@Test
	public void NonEmptySubsets() {
        assertEquals("POW1(S)", UnicodeTranslator.toAscii("POW1(S)"));
	}

	// XXX FiniteSets not provided?
	// http://wiki.event-b.org/images/EventB-Summary.pdf
	// S is finite = Unicode, finite S = Ascii
	@Test
	public void FiniteSets() {
        assertEquals("finite S", UnicodeTranslator.toAscii("finite S"));
	}

	// XXX FiniteSubsets not provided? What's the unicode character? \u1D53D ?
	@Test
	public void FiniteSubsets() {
        assertEquals("FIN(S)", UnicodeTranslator.toAscii("FIN(S)"));
	}

	// XXX FiniteNonEmptySubsets not provided? What's the unicode character?
	// \u1D53D and \u2081 ?
	@Test
	public void FiniteNonEmptySubsets() {
        assertEquals("FIN1(S)", UnicodeTranslator.toAscii("FIN1(S)"));
	}

	@Test
	public void Cardinality() {
        assertEquals("card(S)", UnicodeTranslator.toAscii("card(S)"));
	}

	@Test
	public void Partition() {
        assertEquals("partition(S,x,y)", UnicodeTranslator.toAscii("partition(S,x,y)"));
	}

	@Test
	public void GeneralizedUnion() {
        assertEquals("UNION(U)", UnicodeTranslator.toAscii("\u22c3(U)"));
	}

	@Test
	public void GeneralizedUnion2() {
        assertEquals("UNION (z).(P | E)", UnicodeTranslator.toAscii("\u22c3 (z)\u00b7(P \u2223 E)"));
	}

	@Test
	public void GeneralizedUnion3() {
        assertEquals("union(U)", UnicodeTranslator.toAscii("union(U)"));
	}

	@Test
	public void QuantifiedUnion() {
        assertEquals("UNION z.P | S", UnicodeTranslator.toAscii("\u22c3 z\u00b7P \u2223 S"));
	}

	@Test
	public void GeneralizedIntersection() {
        assertEquals("INTER(U)", UnicodeTranslator.toAscii("\u22c2(U)"));
	}

	@Test
	public void GeneralizedIntersection2() {
        assertEquals("INTER (z).(P | E)", UnicodeTranslator.toAscii("\u22c2 (z)\u00b7(P \u2223 E)"));
	}

	@Test
	public void GeneralizedIntersection3() {
        assertEquals("inter(U)", UnicodeTranslator.toAscii("inter(U)"));
	}

	@Test
	public void QuantifiedIntersection() {
        assertEquals("INTER z.P | S", UnicodeTranslator.toAscii("\u22c2 z\u00b7P \u2223 S"));
	}

	@Test
	public void SetMembership() {
        assertEquals("E : S", UnicodeTranslator.toAscii("E \u2208 S"));
	}

	@Test
	public void SetNonMembership() {
        assertEquals("E /: S", UnicodeTranslator.toAscii("E \u2209 S"));
	}

	@Test
	public void Subset() {
        assertEquals("S <: T", UnicodeTranslator.toAscii("S \u2286 T"));
	}

	@Test
	public void NotASubset() {
        assertEquals("S /<: T", UnicodeTranslator.toAscii("S \u2288 T"));
	}

	@Test
	public void ProperSubset() {
        assertEquals("S <<: T", UnicodeTranslator.toAscii("S \u2282 T"));
	}

	@Test
	public void NotAProperSubset() {
        assertEquals("S /<<: T", UnicodeTranslator.toAscii("S \u2284 T"));
	}

	@Test
	public void NaturalNumbers() {
        assertEquals("NAT", UnicodeTranslator.toAscii("\u2115"));
	}

	// XXX PositiveNaturalNumbers not provided? \u2115 and \u2081
	@Test
	public void PositiveNaturalNumbers() {
        assertEquals("NAT1", UnicodeTranslator.toAscii("NAT1"));
	}

	@Test
	public void Minimum() {
        assertEquals("min(S)", UnicodeTranslator.toAscii("min(S)"));
	}

	@Test
	public void Maximum() {
        assertEquals("max(S)", UnicodeTranslator.toAscii("max(S)"));
	}

	@Test
	public void Sum() {
        assertEquals("m + n", UnicodeTranslator.toAscii("m + n"));
	}

	@Test
	public void DifferenceAlt() {
        assertEquals("m - n", UnicodeTranslator.toAscii("m \u2212 n"));
	}

	@Test
	public void Product() {
		// XXX why \u2217 '*' and not \u00d7 'x'?
        assertEquals("m * n", UnicodeTranslator.toAscii("m \u2217 n"));
	}

	@Test
	public void Quotient() {
        assertEquals("m / n", UnicodeTranslator.toAscii("m \u00f7 n"));
	}

	@Test
	public void Remainder() {
        assertEquals("m mod n", UnicodeTranslator.toAscii("m mod n"));
	}

	@Test
	public void Interval() {
        assertEquals("m .. n", UnicodeTranslator.toAscii("m \u2025 n"));
	}

	@Test
	public void SetSummation() {
		// XXX SIGMA not provided (\u2211)
        assertEquals("SIGMA(z).(P | E)", UnicodeTranslator.toAscii("SIGMA(z)\u00b7(P \u2223 E)"));
	}

	@Test
	public void SetProduct() {
		// XXX PI not provided (\u220F)
        assertEquals("PI(z).(P | E)", UnicodeTranslator.toAscii("PI(z)\u00b7(P \u2223 E)"));
	}

	@Test
	public void Greater() {
        assertEquals("m > n", UnicodeTranslator.toAscii("m > n"));
	}

	@Test
	public void Less() {
        assertEquals("m < n", UnicodeTranslator.toAscii("m < n"));
	}

	@Test
	public void GreaterOrEqual() {
        assertEquals("m >= n", UnicodeTranslator.toAscii("m \u2265 n"));
	}

	@Test
	public void LessOrEqual() {
        assertEquals("m <= n", UnicodeTranslator.toAscii("m \u2264 n"));
	}

	@Test
	public void Relations() {
        assertEquals("S <-> T", UnicodeTranslator.toAscii("S \u2194 T"));
	}

	@Test
	public void Domain() {
        assertEquals("dom(r)", UnicodeTranslator.toAscii("dom(r)"));
	}

	@Test
	public void Range() {
        assertEquals("ran(r)", UnicodeTranslator.toAscii("ran(r)"));
	}

	@Test
	public void ForwardComposition() {
		String expected = "p ; q";
		String actual = UnicodeTranslator.toAscii("p ; q");
		assertEquals(expected, actual);

	}

	@Test
	public void BackwardComposition() {
        assertEquals("p circ q", UnicodeTranslator.toAscii("p \u2218 q"));
	}

	@Test
	public void Identity() {
        assertEquals("id(S)", UnicodeTranslator.toAscii("id(S)"));
	}

	@Test
	public void DomainRestriction() {
        assertEquals("S <| r", UnicodeTranslator.toAscii("S \u25c1 r"));
	}

	@Test
	public void DomainSubtraction() {
        assertEquals("S <<| r", UnicodeTranslator.toAscii("S \u2a64 r"));
	}

	@Test
	public void RangeRestriction() {
        assertEquals("r |> T", UnicodeTranslator.toAscii("r \u25b7 T"));
	}

	@Test
	public void RangeSubtraction() {
        assertEquals("r |>> T", UnicodeTranslator.toAscii("r \u2a65 T"));
	}

	@Test
	public void Inverse() {
        assertEquals("r~", UnicodeTranslator.toAscii("r\u223c"));
	}

	@Test
	public void relationalImage() {
        assertEquals("r[S]", UnicodeTranslator.toAscii("r[S]"));
	}

	@Test
	public void RightOverriding() {
        assertEquals("r1 <+ r2", UnicodeTranslator.toAscii("r1 \ue103 r2"));
	}

	/*
	 * XXX java.io.IOException: Pushback buffer overflow LeftOverriding not
	 * provided? How to escape '+>' ?
	 */
	@Test
	public void LeftOverriding() {
        assertEquals("r1 +> r2", UnicodeTranslator.toAscii("r1 +> r2"));
		// assertTrue(UnicodeTranslator.toAscii("r1 +\\> r2").equals("r1 +\\> r2"));
		// // makes "r1 +\> r2", that's not correct
	}

	@Test
	public void DirectProduct() {
        assertEquals("p >< q", UnicodeTranslator.toAscii("p \u2297 q"));
	}

	@Test
	public void ParallelProduct() {
        assertEquals("p || q", UnicodeTranslator.toAscii("p \u2225 q"));
	}

	// XXX Iteration not provided? something like r^n
	@Test
	public void Iteration() {
        assertEquals("iterate(r,n)", UnicodeTranslator.toAscii("iterate(r,n)"));
	}

	@Test
	public void Closure() {
        assertEquals("closure(r)", UnicodeTranslator.toAscii("closure(r)"));
	}

	// XXX reflexibleClosure not provided? something like r^*
	@Test
	public void rClosure() {
        assertEquals("rclosure(r)", UnicodeTranslator.toAscii("rclosure(r)"));
	}

	// XXX irreflexible Closure not provided? something like r^+
	@Test
	public void iClosure() {
        assertEquals("iclosure(r)", UnicodeTranslator.toAscii("iclosure(r)"));
	}

	@Test
	public void Projection1() {
        assertEquals("prj1(S,T)", UnicodeTranslator.toAscii("prj1(S,T)"));
	}

	/*
	 * XXX Projection not provided? But how to translate '2'? Take the whole
	 * 'prj2'.
	 */
	@Test
	public void Projection1_1() {
        assertEquals("prj1", UnicodeTranslator.toAscii("prj1"));
	}

	@Test
	public void Projection2() {
        assertEquals("prj2(S,T)", UnicodeTranslator.toAscii("prj2(S,T)"));
	}

	/*
	 * XXX Projection not provided? But how to translate '2'? Take the whole
	 * 'prj2'.
	 */
	@Test
	public void Projection2_1() {
        assertEquals("prj2", UnicodeTranslator.toAscii("prj2"));
	}

	@Test
	public void PartialFunctions() {
        assertEquals("S +-> T", UnicodeTranslator.toAscii("S \u21f8 T"));
	}

	@Test
	public void TotalFunctions() {
        assertEquals("S --> T", UnicodeTranslator.toAscii("S \u2192 T"));
	}

	@Test
	public void PartialInjections() {
        assertEquals("S >+> T", UnicodeTranslator.toAscii("S \u2914 T"));
	}

	@Test
	public void TotalInjections() {
        assertEquals("S >-> T", UnicodeTranslator.toAscii("S \u21a3 T"));
	}

	// XXX PartialSurjections not provided? What's the unicode character? \u2900
	@Test
	public void PartialSurjections() {
        assertEquals("S +->> T", UnicodeTranslator.toAscii("S +->> T"));
	}

	// XXX TotalSurjections not provided? What's the unicode character? \u21A0
	@Test
	public void TotalSurjections() {
        assertEquals("S -->> T", UnicodeTranslator.toAscii("S -->> T"));
	}

	@Test
	public void Bijections() {
        assertEquals("S >->> T", UnicodeTranslator.toAscii("S \u2916 T"));
	}

	@Test
	public void LambdaAbstraction() {
        assertEquals("%z.(P|E)", UnicodeTranslator.toAscii("\u03bbz\u00b7(P\u2223E)"));
	}

	@Test
	public void FunctionApplication() {
        assertEquals("f(E)", UnicodeTranslator.toAscii("f(E)"));
	}

	@Test
	public void FunctionApplication2() {
        assertEquals("f(E |-> F)", UnicodeTranslator.toAscii("f(E \u21a6 F)"));
	}

	@Test
	public void FiniteSequences() {
        assertEquals("seq S", UnicodeTranslator.toAscii("seq S"));
	}

	@Test
	public void FiniteNonEmptySequences() {
        assertEquals("seq1(S)", UnicodeTranslator.toAscii("seq1(S)"));
	}

	@Test
	public void InjectiveSequences() {
        assertEquals("iseq(S)", UnicodeTranslator.toAscii("iseq(S)"));
	}

	@Test
	public void Permutations() {
        assertEquals("perm(S)", UnicodeTranslator.toAscii("perm(S)"));
	}

	@Test
	public void SequenceConcatenations() {
		// XXX really meant \u005e for sequence concatenation? not \u0311 ?
        assertEquals("s^t", UnicodeTranslator.toAscii("s\u005et"));
	}

	/*
	 * XXX java.io.IOException: Pushback buffer overflow PrependElement not
	 * provided? How to escape '->' ? What's the unicode character? \u2192
	 */
	@Test
	public void PrependElement() {
        assertEquals("E -> s", UnicodeTranslator.toAscii("E -> s"));
		// assertTrue(UnicodeTranslator.toAscii("E -\\> s").equals("E -\\> s"));
		// // makes "E -\> s", that's not correct
	}

	/*
	 * XXX java.io.IOException: Pushback buffer overflow AppendElement not
	 * provided? How to escape '<-' ? What's the unicode character? \u2190
	 */
	@Test
	public void AppendElement() {
		assertEquals(UnicodeTranslator.toAscii("s <- E"), "s <- E");
		// assertTrue(UnicodeTranslator.toAscii("s <\\- E").equals("s <\\- E"));
		// // makes "s <\- E", that's not correct
	}

	@Test
	public void SingletonSequence() {
        assertEquals("[E]", UnicodeTranslator.toAscii("[E]"));
	}

	@Test
	public void SequenceConstruction() {
        assertEquals("[E,F]", UnicodeTranslator.toAscii("[E,F]"));
	}

	@Test
	public void Size() {
        assertEquals("size(s)", UnicodeTranslator.toAscii("size(s)"));
	}

	@Test
	public void Reverse() {
        assertEquals("rev(s)", UnicodeTranslator.toAscii("rev(s)"));
	}

	@Test
	public void Take() {
        assertEquals("s /|\\ n", UnicodeTranslator.toAscii("s /|\\ n"));
	}

	@Test
	public void Drop() {
        assertEquals("s \\|/ n", UnicodeTranslator.toAscii("s \\|/ n"));
	}

	@Test
	public void FirstElement() {
        assertEquals("first(s)", UnicodeTranslator.toAscii("first(s)"));
	}

	@Test
	public void LastElement() {
        assertEquals("last(s)", UnicodeTranslator.toAscii("last(s)"));
	}

	@Test
	public void Tail() {
        assertEquals("tail(s)", UnicodeTranslator.toAscii("tail(s)"));
	}

	@Test
	public void Front() {
        assertEquals("front(s)", UnicodeTranslator.toAscii("front(s)"));
	}

	@Test
	public void GeneralizedConcatenation() {
        assertEquals("conc(ss)", UnicodeTranslator.toAscii("conc(ss)"));
	}

	@Test
	public void Skip() {
        assertEquals("skip", UnicodeTranslator.toAscii("skip"));
	}

	@Test
	public void SimpleSubstitution() {
        assertEquals("x := E", UnicodeTranslator.toAscii("x := E"));
	}

	@Test
	public void BooleanSubstitution() {
        assertEquals("x := bool(P)", UnicodeTranslator.toAscii("x := bool(P)"));
	}

	@Test
	public void ChoiceFromSet() {
        assertEquals("x :: S", UnicodeTranslator.toAscii("x :\u2208 S"));
	}

	@Test
	public void ChoiceByPredicate() {
        assertEquals("x : P", UnicodeTranslator.toAscii("x : P"));
	}

	@Test
	public void ChoiceByPredicate2() {
        assertEquals("x :| P", UnicodeTranslator.toAscii("x :| P"));
	}

	@Test
	public void FunctionalOverride() {
        assertEquals("f(x) := E", UnicodeTranslator.toAscii("f(x) := E"));
	}

	@Test
	public void MultipleSubstitution() {
        assertEquals("x,y := E,F", UnicodeTranslator.toAscii("x,y := E,F"));
	}

	@Test
	public void ParallelSubstitution() {
        assertEquals("G || H", UnicodeTranslator.toAscii("G \u2225 H"));
	}

	@Test
	public void SequentialSubstitution() {
        assertEquals("G ; H", UnicodeTranslator.toAscii("G ; H"));
	}

	@Test
	public void Precondition() {
        assertEquals("P | G", UnicodeTranslator.toAscii("P \u2223 G"));
	}

	// XXX Guarding not provided? What's the unicode character? \u21D2
	@Test
	public void Guarding() {
        assertEquals("P ==> G", UnicodeTranslator.toAscii("P ==> G"));
	}

	@Test
	public void Alternatives() {
        assertEquals("P [] G", UnicodeTranslator.toAscii("P [] G"));
	}

	@Test
	public void UnboundedChoice() {
        assertEquals("@z . G", UnicodeTranslator.toAscii("@z \u00b7 G"));
	}

	@Test
	public void Context() {
        assertEquals("CONTEXT", UnicodeTranslator.toAscii("CONTEXT"));
	}

	@Test
	public void Extends() {
        assertEquals("EXTENDS", UnicodeTranslator.toAscii("EXTENDS"));
	}

	@Test
	public void Sets() {
        assertEquals("SETS", UnicodeTranslator.toAscii("SETS"));
	}

	@Test
	public void Constants() {
        assertEquals("CONSTANTS", UnicodeTranslator.toAscii("CONSTANTS"));
	}

	@Test
	public void Axioms() {
        assertEquals("AXIOMS", UnicodeTranslator.toAscii("AXIOMS"));
	}

	@Test
	public void Theorems() {
        assertEquals("THEOREMS", UnicodeTranslator.toAscii("THEOREMS"));
	}

	@Test
	public void End() {
        assertEquals("END", UnicodeTranslator.toAscii("END"));
	}

	@Test
	public void Machine() {
        assertEquals("MACHINE", UnicodeTranslator.toAscii("MACHINE"));
	}

	@Test
	public void Refines() {
        assertEquals("REFINES", UnicodeTranslator.toAscii("REFINES"));
	}

	@Test
	public void Sees() {
        assertEquals("SEES", UnicodeTranslator.toAscii("SEES"));
	}

	@Test
	public void Variables() {
        assertEquals("VARIABLES", UnicodeTranslator.toAscii("VARIABLES"));
	}

	@Test
	public void Invariant() {
        assertEquals("INVARIANT", UnicodeTranslator.toAscii("INVARIANT"));
	}

	@Test
	public void Variant() {
        assertEquals("VARIANT", UnicodeTranslator.toAscii("VARIANT"));
	}

	@Test
	public void Events() {
        assertEquals("EVENTS", UnicodeTranslator.toAscii("EVENTS"));
	}

	@Test
	public void Any() {
        assertEquals("ANY", UnicodeTranslator.toAscii("ANY"));
	}

	@Test
	public void Where() {
        assertEquals("WHERE", UnicodeTranslator.toAscii("WHERE"));
	}

	@Test
	public void With() {
        assertEquals("WITH", UnicodeTranslator.toAscii("WITH"));
	}

	@Test
	public void Then() {
        assertEquals("THEN", UnicodeTranslator.toAscii("THEN"));
	}

	/*--------------------------------------------------------------*/

	@Test
	public void Letter() {
        assertEquals("abc", UnicodeTranslator.toAscii("abc"));
	}

	@Test
	public void LetterDigit() {
        assertEquals("abc123", UnicodeTranslator.toAscii("abc123"));
	}

	@Test
	public void LetterUnderscore() {
        assertEquals("abc_", UnicodeTranslator.toAscii("abc_"));
	}

	@Test
	public void LetterANY() {
        assertEquals("abcANY", UnicodeTranslator.toAscii("abcANY"));
        assertEquals("abcany", UnicodeTranslator.toAscii("abcany"));
	}

	@Test
	public void LetterFALSE() {
        assertEquals("abcFALSE", UnicodeTranslator.toAscii("abcFALSE"));
        assertEquals("abcfalse", UnicodeTranslator.toAscii("abcfalse"));
	}

	@Test
	public void LetterINTEGER() {
        assertEquals("abcINTEGER", UnicodeTranslator.toAscii("abcINTEGER"));
        assertEquals("abcinteger", UnicodeTranslator.toAscii("abcinteger"));
	}

	@Test
	public void LetterINTER() {
        assertEquals("abcINTER", UnicodeTranslator.toAscii("abcINTER"));
        assertEquals("abcinter", UnicodeTranslator.toAscii("abcinter"));
	}

	@Test
	public void LetterNAT() {
        assertEquals("abcNAT", UnicodeTranslator.toAscii("abcNAT"));
        assertEquals("abcnat", UnicodeTranslator.toAscii("abcnat"));
	}

	@Test
	public void LetterNAT1() {
        assertEquals("abcNAT1", UnicodeTranslator.toAscii("abcNAT1"));
        assertEquals("abcnat1", UnicodeTranslator.toAscii("abcnat1"));
	}

	@Test
	public void LetterNATURAL() {
        assertEquals("abcNATURAL", UnicodeTranslator.toAscii("abcNATURAL"));
        assertEquals("abcnatural", UnicodeTranslator.toAscii("abcnatural"));
	}

	@Test
	public void LetterNOT() {
        assertEquals("abcNOT", UnicodeTranslator.toAscii("abcNOT"));
        assertEquals("abcnot", UnicodeTranslator.toAscii("abcnot"));
	}

	@Test
	public void LetterOR() {
        assertEquals("abcOR", UnicodeTranslator.toAscii("abcOR"));
        assertEquals("abcor", UnicodeTranslator.toAscii("abcor"));
	}

	@Test
	public void LetterPOW() {
        assertEquals("abcPOW", UnicodeTranslator.toAscii("abcPOW"));
        assertEquals("abcpow", UnicodeTranslator.toAscii("abcpow"));
	}

	@Test
	public void LetterPOW1() {
        assertEquals("abcPOW1", UnicodeTranslator.toAscii("abcPOW1"));
        assertEquals("abcpow1", UnicodeTranslator.toAscii("abcpow1"));
	}

	@Test
	public void LetterTRUE() {
        assertEquals("abcTRUE", UnicodeTranslator.toAscii("abcTRUE"));
        assertEquals("abctrue", UnicodeTranslator.toAscii("abctrue"));
	}

	@Test
	public void LetterUNION() {
        assertEquals("abcUNION", UnicodeTranslator.toAscii("abcUNION"));
        assertEquals("abcunion", UnicodeTranslator.toAscii("abcunion"));
	}

	@Test
	public void LetterDigitUnderscore() {
        assertEquals("abc123_", UnicodeTranslator.toAscii("abc123_"));
	}

	@Test
	public void LetterDigitANY() {
        assertEquals("abc123ANY", UnicodeTranslator.toAscii("abc123ANY"));
        assertEquals("abc123any", UnicodeTranslator.toAscii("abc123any"));
	}

	@Test
	public void LetterDigitFALSE() {
        assertEquals("abc123FALSE", UnicodeTranslator.toAscii("abc123FALSE"));
        assertEquals("abc123false", UnicodeTranslator.toAscii("abc123false"));
	}

	@Test
	public void LetterDigitINTEGER() {
        assertEquals("abc123INTEGER", UnicodeTranslator.toAscii("abc123INTEGER"));
        assertEquals("abc123integer", UnicodeTranslator.toAscii("abc123integer"));
	}

	@Test
	public void LetterDigitINTER() {
        assertEquals("abc123INTER", UnicodeTranslator.toAscii("abc123INTER"));
        assertEquals("abc123inter", UnicodeTranslator.toAscii("abc123inter"));
	}

	@Test
	public void LetterDigitNAT() {
        assertEquals("abc123NAT", UnicodeTranslator.toAscii("abc123NAT"));
        assertEquals("abc123nat", UnicodeTranslator.toAscii("abc123nat"));
	}

	@Test
	public void LetterDigitNAT1() {
        assertEquals("abc123NAT1", UnicodeTranslator.toAscii("abc123NAT1"));
        assertEquals("abc123nat1", UnicodeTranslator.toAscii("abc123nat1"));
	}

	@Test
	public void LetterDigitNATURAL() {
        assertEquals("abc123NATURAL", UnicodeTranslator.toAscii("abc123NATURAL"));
        assertEquals("abc123natural", UnicodeTranslator.toAscii("abc123natural"));
	}

	@Test
	public void LetterDigitNOT() {
        assertEquals("abc123NOT", UnicodeTranslator.toAscii("abc123NOT"));
        assertEquals("abc123not", UnicodeTranslator.toAscii("abc123not"));
	}

	@Test
	public void LetterDigitOR() {
        assertEquals("abc123OR", UnicodeTranslator.toAscii("abc123OR"));
        assertEquals("abc123or", UnicodeTranslator.toAscii("abc123or"));
	}

	@Test
	public void LetterDigitPOW() {
        assertEquals("abc123POW", UnicodeTranslator.toAscii("abc123POW"));
        assertEquals("abc123pow", UnicodeTranslator.toAscii("abc123pow"));
	}

	@Test
	public void LetterDigitPOW1() {
        assertEquals("abc123POW1", UnicodeTranslator.toAscii("abc123POW1"));
        assertEquals("abc123pow1", UnicodeTranslator.toAscii("abc123pow1"));
	}

	@Test
	public void LetterDigitTRUE() {
        assertEquals("abc123TRUE", UnicodeTranslator.toAscii("abc123TRUE"));
        assertEquals("abc123true", UnicodeTranslator.toAscii("abc123true"));
	}

	@Test
	public void LetterDigitUNION() {
        assertEquals("abc123UNION", UnicodeTranslator.toAscii("abc123UNION"));
        assertEquals("abc123union", UnicodeTranslator.toAscii("abc123union"));
	}

	@Test
	public void LetterUnderscoreDigit() {
        assertEquals("abc_123", UnicodeTranslator.toAscii("abc_123"));
	}

	@Test
	public void LetterUnderscoreANY() {
        assertEquals("abc_ANY", UnicodeTranslator.toAscii("abc_ANY"));
        assertEquals("abc_any", UnicodeTranslator.toAscii("abc_any"));
	}

	@Test
	public void LetterUnderscoreFALSE() {
        assertEquals("abc_FALSE", UnicodeTranslator.toAscii("abc_FALSE"));
        assertEquals("abc_false", UnicodeTranslator.toAscii("abc_false"));
	}

	@Test
	public void LetterUnderscoreINTEGER() {
        assertEquals("abc_INTEGER", UnicodeTranslator.toAscii("abc_INTEGER"));
        assertEquals("abc_integer", UnicodeTranslator.toAscii("abc_integer"));
	}

	@Test
	public void LetterUnderscoreINTER() {
        assertEquals("abc_INTER", UnicodeTranslator.toAscii("abc_INTER"));
        assertEquals("abc_inter", UnicodeTranslator.toAscii("abc_inter"));
	}

	@Test
	public void LetterUnderscoreNAT() {
        assertEquals("abc_NAT", UnicodeTranslator.toAscii("abc_NAT"));
        assertEquals("abc_nat", UnicodeTranslator.toAscii("abc_nat"));
	}

	@Test
	public void LetterUnderscoreNAT1() {
        assertEquals("abc_NAT1", UnicodeTranslator.toAscii("abc_NAT1"));
        assertEquals("abc_nat1", UnicodeTranslator.toAscii("abc_nat1"));
	}

	@Test
	public void LetterUnderscoreNATURAL() {
        assertEquals("abc_NATURAL", UnicodeTranslator.toAscii("abc_NATURAL"));
        assertEquals("abc_natural", UnicodeTranslator.toAscii("abc_natural"));
	}

	@Test
	public void LetterUnderscoreNOT() {
        assertEquals("abc_NOT", UnicodeTranslator.toAscii("abc_NOT"));
        assertEquals("abc_not", UnicodeTranslator.toAscii("abc_not"));
	}

	@Test
	public void LetterUnderscoreOR() {
        assertEquals("abc_OR", UnicodeTranslator.toAscii("abc_OR"));
        assertEquals("abc_or", UnicodeTranslator.toAscii("abc_or"));
	}

	@Test
	public void LetterUnderscorePOW() {
        assertEquals("abc_pow", UnicodeTranslator.toAscii("abc_pow"));
        assertEquals("abc_POW", UnicodeTranslator.toAscii("abc_POW"));
	}

	@Test
	public void LetterUnderscorePOW1() {
        assertEquals("abc_POW1", UnicodeTranslator.toAscii("abc_POW1"));
        assertEquals("abc_pow1", UnicodeTranslator.toAscii("abc_pow1"));
	}

	@Test
	public void LetterUnderscoreTRUE() {
        assertEquals("abc_TRUE", UnicodeTranslator.toAscii("abc_TRUE"));
        assertEquals("abc_true", UnicodeTranslator.toAscii("abc_true"));
	}

	@Test
	public void LetterUnderscoreUNION() {
        assertEquals("abc_UNION", UnicodeTranslator.toAscii("abc_UNION"));
        assertEquals("abc_union", UnicodeTranslator.toAscii("abc_union"));
	}

	@Test
	public void LetterANYDigit() {
        assertEquals("abcANY123", UnicodeTranslator.toAscii("abcANY123"));
        assertEquals("abcany123", UnicodeTranslator.toAscii("abcany123"));
	}

	@Test
	public void LetterFALSEDigit() {
        assertEquals("abcFALSE123", UnicodeTranslator.toAscii("abcFALSE123"));
        assertEquals("abcfalse123", UnicodeTranslator.toAscii("abcfalse123"));
	}

	@Test
	public void LetterINTEGERDigit() {
        assertEquals("abcINTEGER123", UnicodeTranslator.toAscii("abcINTEGER123"));
        assertEquals("abcinteger123", UnicodeTranslator.toAscii("abcinteger123"));
	}

	@Test
	public void LetterINTERDigit() {
        assertEquals("abcINTER123", UnicodeTranslator.toAscii("abcINTER123"));
        assertEquals("abcinter123", UnicodeTranslator.toAscii("abcinter123"));
	}

	@Test
	public void LetterNATDigit() {
        assertEquals("abcNAT123", UnicodeTranslator.toAscii("abcNAT123"));
        assertEquals("abcnat123", UnicodeTranslator.toAscii("abcnat123"));
	}

	@Test
	public void LetterNAT1Digit() {
        assertEquals("abcNAT1123", UnicodeTranslator.toAscii("abcNAT1123"));
        assertEquals("abcnat1123", UnicodeTranslator.toAscii("abcnat1123"));
	}

	public void LetterNATURALDigit() {
        assertEquals("abcNATURAL123", UnicodeTranslator.toAscii("abcNATURAL123"));
        assertEquals("abcnatural123", UnicodeTranslator.toAscii("abcnatural123"));
	}

	public void LetterNOTDigit() {
        assertEquals("abcNOT123", UnicodeTranslator.toAscii("abcNOT123"));
        assertEquals("abcnot123", UnicodeTranslator.toAscii("abcnot123"));
	}

	@Test
	public void LetterORDigit() {
        assertEquals("abcOR123", UnicodeTranslator.toAscii("abcOR123"));
        assertEquals("abcor123", UnicodeTranslator.toAscii("abcor123"));
	}

	@Test
	public void LetterPOWDigit() {
        assertEquals("abcPOW123", UnicodeTranslator.toAscii("abcPOW123"));
        assertEquals("abcpow123", UnicodeTranslator.toAscii("abcpow123"));
	}

	@Test
	public void LetterPOW1Digit() {
        assertEquals("abcPOW1123", UnicodeTranslator.toAscii("abcPOW1123"));
        assertEquals("abcpow1123", UnicodeTranslator.toAscii("abcpow1123"));
	}

	@Test
	public void LetterTRUEDigit() {
        assertEquals("abcTRUE123", UnicodeTranslator.toAscii("abcTRUE123"));
        assertEquals("abctrue123", UnicodeTranslator.toAscii("abctrue123"));
	}

	@Test
	public void LetterUNIONDigit() {
        assertEquals("abcUNION123", UnicodeTranslator.toAscii("abcUNION123"));
        assertEquals("abcunion123", UnicodeTranslator.toAscii("abcunion123"));
	}

	@Test
	public void LetterANYUnderscore() {
        assertEquals("abcANY_", UnicodeTranslator.toAscii("abcANY_"));
        assertEquals("abcany_", UnicodeTranslator.toAscii("abcany_"));
	}

	@Test
	public void LetterFALSEUnderscore() {
        assertEquals("abcFALSE_", UnicodeTranslator.toAscii("abcFALSE_"));
        assertEquals("abcfalse_", UnicodeTranslator.toAscii("abcfalse_"));
	}

	@Test
	public void LetterINTEGERUnderscore() {
        assertEquals("abcINTEGER_", UnicodeTranslator.toAscii("abcINTEGER_"));
        assertEquals("abcinteger_", UnicodeTranslator.toAscii("abcinteger_"));
	}

	@Test
	public void LetterINTERUnderscore() {
        assertEquals("abcINTER_", UnicodeTranslator.toAscii("abcINTER_"));
        assertEquals("abcinter_", UnicodeTranslator.toAscii("abcinter_"));
	}

	@Test
	public void LetterNATUnderscore() {
        assertEquals("abcNAT_", UnicodeTranslator.toAscii("abcNAT_"));
        assertEquals("abcnat_", UnicodeTranslator.toAscii("abcnat_"));
	}

	@Test
	public void LetterNAT1Underscore() {
        assertEquals("abcNAT1_", UnicodeTranslator.toAscii("abcNAT1_"));
        assertEquals("abcnat1_", UnicodeTranslator.toAscii("abcnat1_"));
	}

	@Test
	public void LetterNATURALUnderscore() {
        assertEquals("abcNATURAL_", UnicodeTranslator.toAscii("abcNATURAL_"));
        assertEquals("abcnatural_", UnicodeTranslator.toAscii("abcnatural_"));
	}

	@Test
	public void LetterNOTUnderscore() {
        assertEquals("abcNOT_", UnicodeTranslator.toAscii("abcNOT_"));
        assertEquals("abcnot_", UnicodeTranslator.toAscii("abcnot_"));
	}

	@Test
	public void LetterORUnderscore() {
        assertEquals("abcOR_", UnicodeTranslator.toAscii("abcOR_"));
        assertEquals("abcor_", UnicodeTranslator.toAscii("abcor_"));
	}

	@Test
	public void LetterPOWUnderscore() {
        assertEquals("abcPOW_", UnicodeTranslator.toAscii("abcPOW_"));
        assertEquals("abcpow_", UnicodeTranslator.toAscii("abcpow_"));
	}

	@Test
	public void LetterPOW1Underscore() {
        assertEquals("abcPOW1_", UnicodeTranslator.toAscii("abcPOW1_"));
        assertEquals("abcpow1_", UnicodeTranslator.toAscii("abcpow1_"));
	}

	@Test
	public void LetterTRUEUnderscore() {
        assertEquals("abcTRUE_", UnicodeTranslator.toAscii("abcTRUE_"));
        assertEquals("abctrue_", UnicodeTranslator.toAscii("abctrue_"));
	}

	@Test
	public void LetterUNIONUnderscore() {
        assertEquals("abcUNION_", UnicodeTranslator.toAscii("abcUNION_"));
        assertEquals("abcunion_", UnicodeTranslator.toAscii("abcunion_"));
	}

	@Test
	public void LetterDigitUnderscoreANY() {
        assertEquals("abc123_ANY", UnicodeTranslator.toAscii("abc123_ANY"));
        assertEquals("abc123_any", UnicodeTranslator.toAscii("abc123_any"));
	}

	@Test
	public void LetterDigitUnderscoreFALSE() {
        assertEquals("abc123_FALSE", UnicodeTranslator.toAscii("abc123_FALSE"));
        assertEquals("abc123_false", UnicodeTranslator.toAscii("abc123_false"));
	}

	@Test
	public void LetterDigitUnderscoreINTEGER() {
        assertEquals("abc123_INTEGER", UnicodeTranslator.toAscii("abc123_INTEGER"));
        assertEquals("abc123_integer", UnicodeTranslator.toAscii("abc123_integer"));
	}

	@Test
	public void LetterDigitUnderscoreINTER() {
        assertEquals("abc123_INTER", UnicodeTranslator.toAscii("abc123_INTER"));
        assertEquals("abc123_inter", UnicodeTranslator.toAscii("abc123_inter"));
	}

	@Test
	public void LetterDigitUnderscoreNAT() {
        assertEquals("abc123_NAT", UnicodeTranslator.toAscii("abc123_NAT"));
        assertEquals("abc123_nat", UnicodeTranslator.toAscii("abc123_nat"));
	}

	@Test
	public void LetterDigitUnderscoreNAT1() {
        assertEquals("abc123_NAT1", UnicodeTranslator.toAscii("abc123_NAT1"));
        assertEquals("abc123_nat1", UnicodeTranslator.toAscii("abc123_nat1"));
	}

	@Test
	public void LetterDigitUnderscoreNATURAL() {
        assertEquals("abc123_NATURAL", UnicodeTranslator.toAscii("abc123_NATURAL"));
        assertEquals("abc123_natural", UnicodeTranslator.toAscii("abc123_natural"));
	}

	@Test
	public void LetterDigitUnderscoreNOT() {
        assertEquals("abc123_NOT", UnicodeTranslator.toAscii("abc123_NOT"));
        assertEquals("abc123_not", UnicodeTranslator.toAscii("abc123_not"));
	}

	@Test
	public void LetterDigitUnderscoreOR() {
        assertEquals("abc123_OR", UnicodeTranslator.toAscii("abc123_OR"));
        assertEquals("abc123_or", UnicodeTranslator.toAscii("abc123_or"));
	}

	@Test
	public void LetterDigitUnderscorePOW() {
        assertEquals("abc123_POW", UnicodeTranslator.toAscii("abc123_POW"));
        assertEquals("abc123_pow", UnicodeTranslator.toAscii("abc123_pow"));
	}

	@Test
	public void LetterDigitUnderscorePOW1() {
        assertEquals("abc123_POW1", UnicodeTranslator.toAscii("abc123_POW1"));
        assertEquals("abc123_pow1", UnicodeTranslator.toAscii("abc123_pow1"));
	}

	@Test
	public void LetterDigitUnderscoreTRUE() {
        assertEquals("abc123_TRUE", UnicodeTranslator.toAscii("abc123_TRUE"));
        assertEquals("abc123_true", UnicodeTranslator.toAscii("abc123_true"));
	}

	@Test
	public void LetterDigitUnderscoreUNION() {
        assertEquals("abc123_UNION", UnicodeTranslator.toAscii("abc123_UNION"));
        assertEquals("abc123_union", UnicodeTranslator.toAscii("abc123_union"));
	}

	@Test
	public void LetterDigitANYUnderscore() {
        assertEquals("abc123ANY_", UnicodeTranslator.toAscii("abc123ANY_"));
        assertEquals("abc123any_", UnicodeTranslator.toAscii("abc123any_"));
	}

	@Test
	public void LetterDigitFALSEUnderscore() {
        assertEquals("abc123FALSE_", UnicodeTranslator.toAscii("abc123FALSE_"));
        assertEquals("abc123false_", UnicodeTranslator.toAscii("abc123false_"));
	}

	@Test
	public void LetterDigitINTEGERUnderscore() {
        assertEquals("abc123INTEGER_", UnicodeTranslator.toAscii("abc123INTEGER_"));
        assertEquals("abc123integer_", UnicodeTranslator.toAscii("abc123integer_"));
	}

	@Test
	public void LetterDigitINTERUnderscore() {
        assertEquals("abc123INTER_", UnicodeTranslator.toAscii("abc123INTER_"));
        assertEquals("abc123inter_", UnicodeTranslator.toAscii("abc123inter_"));
	}

	@Test
	public void LetterDigitNATUnderscore() {
        assertEquals("abc123NAT_", UnicodeTranslator.toAscii("abc123NAT_"));
        assertEquals("abc123nat_", UnicodeTranslator.toAscii("abc123nat_"));
	}

	@Test
	public void LetterDigitNAT1Underscore() {
        assertEquals("abc123NAT1_", UnicodeTranslator.toAscii("abc123NAT1_"));
        assertEquals("abc123nat1_", UnicodeTranslator.toAscii("abc123nat1_"));
	}

	@Test
	public void LetterDigitNATURALUnderscore() {
        assertEquals("abc123NATURAL_", UnicodeTranslator.toAscii("abc123NATURAL_"));
        assertEquals("abc123natural_", UnicodeTranslator.toAscii("abc123natural_"));
	}

	@Test
	public void LetterDigitNOTUnderscore() {
        assertEquals("abc123NOT_", UnicodeTranslator.toAscii("abc123NOT_"));
        assertEquals("abc123not_", UnicodeTranslator.toAscii("abc123not_"));
	}

	@Test
	public void LetterDigitORUnderscore() {
        assertEquals("abc123OR_", UnicodeTranslator.toAscii("abc123OR_"));
        assertEquals("abc123or_", UnicodeTranslator.toAscii("abc123or_"));
	}

	@Test
	public void LetterDigitPOWUnderscore() {
        assertEquals("abc123POW_", UnicodeTranslator.toAscii("abc123POW_"));
        assertEquals("abc123pow_", UnicodeTranslator.toAscii("abc123pow_"));
	}

	@Test
	public void LetterDigitPOW1Underscore() {
        assertEquals("abc123POW1_", UnicodeTranslator.toAscii("abc123POW1_"));
        assertEquals("abc123pow1_", UnicodeTranslator.toAscii("abc123pow1_"));
	}

	@Test
	public void LetterDigitTRUEUnderscore() {
        assertEquals("abc123TRUE_", UnicodeTranslator.toAscii("abc123TRUE_"));
        assertEquals("abc123true_", UnicodeTranslator.toAscii("abc123true_"));
	}

	@Test
	public void LetterDigitUNIONUnderscore() {
        assertEquals("abc123UNION_", UnicodeTranslator.toAscii("abc123UNION_"));
        assertEquals("abc123union_", UnicodeTranslator.toAscii("abc123union_"));
	}

	@Test
	public void LetterUnderscoreDigitANY() {
        assertEquals("abc_123ANY", UnicodeTranslator.toAscii("abc_123ANY"));
        assertEquals("abc_123any", UnicodeTranslator.toAscii("abc_123any"));
	}

	@Test
	public void LetterUnderscoreDigitFALSE() {
        assertEquals("abc_123FALSE", UnicodeTranslator.toAscii("abc_123FALSE"));
        assertEquals("abc_123false", UnicodeTranslator.toAscii("abc_123false"));
	}

	@Test
	public void LetterUnderscoreDigitINTEGER() {
        assertEquals("abc_123INTEGER", UnicodeTranslator.toAscii("abc_123INTEGER"));
        assertEquals("abc_123integer", UnicodeTranslator.toAscii("abc_123integer"));
	}

	@Test
	public void LetterUnderscoreDigitINTER() {
        assertEquals("abc_123INTER", UnicodeTranslator.toAscii("abc_123INTER"));
        assertEquals("abc_123inter", UnicodeTranslator.toAscii("abc_123inter"));
	}

	@Test
	public void LetterUnderscoreDigitANT() {
        assertEquals("abc_123NAT", UnicodeTranslator.toAscii("abc_123NAT"));
        assertEquals("abc_123nat", UnicodeTranslator.toAscii("abc_123nat"));
	}

	@Test
	public void LetterUnderscoreDigitNAT1() {
        assertEquals("abc_123NAT1", UnicodeTranslator.toAscii("abc_123NAT1"));
        assertEquals("abc_123nat1", UnicodeTranslator.toAscii("abc_123nat1"));
	}

	@Test
	public void LetterUnderscoreDigitNATURAL() {
        assertEquals("abc_123NATURAL", UnicodeTranslator.toAscii("abc_123NATURAL"));
        assertEquals("abc_123natural", UnicodeTranslator.toAscii("abc_123natural"));
	}

	@Test
	public void LetterUnderscoreDigitNOT() {
        assertEquals("abc_123NOT", UnicodeTranslator.toAscii("abc_123NOT"));
        assertEquals("abc_123not", UnicodeTranslator.toAscii("abc_123not"));
	}

	@Test
	public void LetterUnderscoreDigitOR() {
        assertEquals("abc_123OR", UnicodeTranslator.toAscii("abc_123OR"));
        assertEquals("abc_123or", UnicodeTranslator.toAscii("abc_123or"));
	}

	@Test
	public void LetterUnderscoreDigitPOW() {
        assertEquals("abc_123POW", UnicodeTranslator.toAscii("abc_123POW"));
        assertEquals("abc_123pow", UnicodeTranslator.toAscii("abc_123pow"));
	}

	@Test
	public void LetterUnderscoreDigitPOW1() {
        assertEquals("abc_123POW1", UnicodeTranslator.toAscii("abc_123POW1"));
        assertEquals("abc_123pow1", UnicodeTranslator.toAscii("abc_123pow1"));
	}

	@Test
	public void LetterUnderscoreDigitTRUE() {
        assertEquals("abc_123TRUE", UnicodeTranslator.toAscii("abc_123TRUE"));
        assertEquals("abc_123true", UnicodeTranslator.toAscii("abc_123true"));
	}

	@Test
	public void LetterUnderscoreDigitUNION() {
        assertEquals("abc_123UNION", UnicodeTranslator.toAscii("abc_123UNION"));
        assertEquals("abc_123union", UnicodeTranslator.toAscii("abc_123union"));
	}

	@Test
	public void LetterUnderscoreANYDigit() {
        assertEquals("abc_ANY123", UnicodeTranslator.toAscii("abc_ANY123"));
        assertEquals("abc_any123", UnicodeTranslator.toAscii("abc_any123"));
	}

	@Test
	public void LetterUnderscoreFALSEDigit() {
        assertEquals("abc_FALSE123", UnicodeTranslator.toAscii("abc_FALSE123"));
        assertEquals("abc_false123", UnicodeTranslator.toAscii("abc_false123"));
	}

	@Test
	public void LetterUnderscoreINTEGERDigit() {
        assertEquals("abc_INTEGER123", UnicodeTranslator.toAscii("abc_INTEGER123"));
        assertEquals("abc_integer123", UnicodeTranslator.toAscii("abc_integer123"));
	}

	@Test
	public void LetterUnderscoreINTERDigit() {
        assertEquals("abc_INTER123", UnicodeTranslator.toAscii("abc_INTER123"));
        assertEquals("abc_inter123", UnicodeTranslator.toAscii("abc_inter123"));
	}

	@Test
	public void LetterUnderscoreNATDigit() {
        assertEquals("abc_NAT123", UnicodeTranslator.toAscii("abc_NAT123"));
        assertEquals("abc_nat123", UnicodeTranslator.toAscii("abc_nat123"));
	}

	@Test
	public void LetterUnderscoreNAT1Digit() {
        assertEquals("abc_NAT1123", UnicodeTranslator.toAscii("abc_NAT1123"));
        assertEquals("abc_nat1123", UnicodeTranslator.toAscii("abc_nat1123"));
	}

	@Test
	public void LetterUnderscoreNATURALDigit() {
        assertEquals("abc_NATURAL123", UnicodeTranslator.toAscii("abc_NATURAL123"));
        assertEquals("abc_natural123", UnicodeTranslator.toAscii("abc_natural123"));
	}

	@Test
	public void LetterUnderscoreNOTDigit() {
        assertEquals("abc_NOT123", UnicodeTranslator.toAscii("abc_NOT123"));
        assertEquals("abc_not123", UnicodeTranslator.toAscii("abc_not123"));
	}

	@Test
	public void LetterUnderscoreORDigit() {
        assertEquals("abc_OR123", UnicodeTranslator.toAscii("abc_OR123"));
        assertEquals("abc_or123", UnicodeTranslator.toAscii("abc_or123"));
	}

	@Test
	public void LetterUnderscorePOWDigit() {
        assertEquals("abc_POW123", UnicodeTranslator.toAscii("abc_POW123"));
        assertEquals("abc_pow123", UnicodeTranslator.toAscii("abc_pow123"));
	}

	@Test
	public void LetterUnderscorePOW1Digit() {
        assertEquals("abc_POW1123", UnicodeTranslator.toAscii("abc_POW1123"));
        assertEquals("abc_pow1123", UnicodeTranslator.toAscii("abc_pow1123"));
	}

	@Test
	public void LetterUnderscoreTRUEDigit() {
        assertEquals("abc_TRUE123", UnicodeTranslator.toAscii("abc_TRUE123"));
        assertEquals("abc_true123", UnicodeTranslator.toAscii("abc_true123"));
	}

	@Test
	public void LetterUnderscoreUNIONDigit() {
        assertEquals("abc_UNION123", UnicodeTranslator.toAscii("abc_UNION123"));
        assertEquals("abc_union123", UnicodeTranslator.toAscii("abc_union123"));
	}

	@Test
	public void LetterANYDigitUnderscore() {
        assertEquals("abcANY123_", UnicodeTranslator.toAscii("abcANY123_"));
        assertEquals("abcany123_", UnicodeTranslator.toAscii("abcany123_"));
	}

	@Test
	public void LetterFALSEDigitUnderscore() {
        assertEquals("abcFALSE123_", UnicodeTranslator.toAscii("abcFALSE123_"));
        assertEquals("abcfalse123_", UnicodeTranslator.toAscii("abcfalse123_"));
	}

	@Test
	public void LetterINTEGERDigitUnderscore() {
        assertEquals("abcINTEGER123_", UnicodeTranslator.toAscii("abcINTEGER123_"));
        assertEquals("abcinteger123_", UnicodeTranslator.toAscii("abcinteger123_"));
	}

	@Test
	public void LetterINTERDigitUnderscore() {
        assertEquals("abcINTER123_", UnicodeTranslator.toAscii("abcINTER123_"));
        assertEquals("abcinter123_", UnicodeTranslator.toAscii("abcinter123_"));
	}

	@Test
	public void LetterNATDigitUnderscore() {
        assertEquals("abcNAT123_", UnicodeTranslator.toAscii("abcNAT123_"));
        assertEquals("abcnat123_", UnicodeTranslator.toAscii("abcnat123_"));
	}

	@Test
	public void LetterNAT1DigitUnderscore() {
        assertEquals("abcNAT1123_", UnicodeTranslator.toAscii("abcNAT1123_"));
        assertEquals("abcnat1123_", UnicodeTranslator.toAscii("abcnat1123_"));
	}

	@Test
	public void LetterNATURALDigitUnderscore() {
        assertEquals("abcNATURAL123_", UnicodeTranslator.toAscii("abcNATURAL123_"));
        assertEquals("abcnatural123_", UnicodeTranslator.toAscii("abcnatural123_"));
	}

	@Test
	public void LetterNOTDigitUnderscore() {
        assertEquals("abcNOT123_", UnicodeTranslator.toAscii("abcNOT123_"));
        assertEquals("abcnot123_", UnicodeTranslator.toAscii("abcnot123_"));
	}

	@Test
	public void LetterORDigitUnderscore() {
        assertEquals("abcOR123_", UnicodeTranslator.toAscii("abcOR123_"));
        assertEquals("abcor123_", UnicodeTranslator.toAscii("abcor123_"));
	}

	@Test
	public void LetterPOWDigitUnderscore() {
        assertEquals("abcPOW123_", UnicodeTranslator.toAscii("abcPOW123_"));
        assertEquals("abcpow123_", UnicodeTranslator.toAscii("abcpow123_"));
	}

	@Test
	public void LetterPOW1DigitUnderscore() {
        assertEquals("abcPOW1123_", UnicodeTranslator.toAscii("abcPOW1123_"));
        assertEquals("abcpow1123_", UnicodeTranslator.toAscii("abcpow1123_"));
	}

	@Test
	public void LetterTRUEDigitUnderscore() {
        assertEquals("abcTRUE123_", UnicodeTranslator.toAscii("abcTRUE123_"));
        assertEquals("abctrue123_", UnicodeTranslator.toAscii("abctrue123_"));
	}

	@Test
	public void LetterUNIONDigitUnderscore() {
        assertEquals("abcUNION123_", UnicodeTranslator.toAscii("abcUNION123_"));
        assertEquals("abcunion123_", UnicodeTranslator.toAscii("abcunion123_"));
	}

	@Test
	public void LetterANYUnderscoreDigit() {
        assertEquals("abcANY_123", UnicodeTranslator.toAscii("abcANY_123"));
        assertEquals("abcany_123", UnicodeTranslator.toAscii("abcany_123"));
	}

	@Test
	public void LetterFALSEUnderscoreDigit() {
        assertEquals("abcFALSE_123", UnicodeTranslator.toAscii("abcFALSE_123"));
        assertEquals("abcfalse_123", UnicodeTranslator.toAscii("abcfalse_123"));
	}

	@Test
	public void LetterINTEGERUnderscoreDigit() {
        assertEquals("abcINTEGER_123", UnicodeTranslator.toAscii("abcINTEGER_123"));
        assertEquals("abcinteger_123", UnicodeTranslator.toAscii("abcinteger_123"));
	}

	@Test
	public void LetterINTERUnderscoreDigit() {
        assertEquals("abcINTER_123", UnicodeTranslator.toAscii("abcINTER_123"));
        assertEquals("abcinter_123", UnicodeTranslator.toAscii("abcinter_123"));
	}

	@Test
	public void LetterNATUnderscoreDigit() {
        assertEquals("abcNAT_123", UnicodeTranslator.toAscii("abcNAT_123"));
        assertEquals("abcnat_123", UnicodeTranslator.toAscii("abcnat_123"));
	}

	@Test
	public void LetterNAT1UnderscoreDigit() {
        assertEquals("abcNAT1_123", UnicodeTranslator.toAscii("abcNAT1_123"));
        assertEquals("abcnat1_123", UnicodeTranslator.toAscii("abcnat1_123"));
	}

	@Test
	public void LetterNATURALUnderscoreDigit() {
        assertEquals("abcNATURAL_123", UnicodeTranslator.toAscii("abcNATURAL_123"));
        assertEquals("abcnatural_123", UnicodeTranslator.toAscii("abcnatural_123"));
	}

	@Test
	public void LetterNOTUnderscoreDigit() {
        assertEquals("abcNOT_123", UnicodeTranslator.toAscii("abcNOT_123"));
        assertEquals("abcnot_123", UnicodeTranslator.toAscii("abcnot_123"));
	}

	@Test
	public void LetterORUnderscoreDigit() {
        assertEquals("abcOR_123", UnicodeTranslator.toAscii("abcOR_123"));
        assertEquals("abcor_123", UnicodeTranslator.toAscii("abcor_123"));
	}

	@Test
	public void LetterPOWUnderscoreDigit() {
        assertEquals("abcPOW_123", UnicodeTranslator.toAscii("abcPOW_123"));
        assertEquals("abcpow_123", UnicodeTranslator.toAscii("abcpow_123"));
	}

	@Test
	public void LetterPOW1UnderscoreDigit() {
        assertEquals("abcPOW1_123", UnicodeTranslator.toAscii("abcPOW1_123"));
        assertEquals("abcpow1_123", UnicodeTranslator.toAscii("abcpow1_123"));
	}

	@Test
	public void LetterTRUEUnderscoreDigit() {
        assertEquals("abcTRUE_123", UnicodeTranslator.toAscii("abcTRUE_123"));
        assertEquals("abctrue_123", UnicodeTranslator.toAscii("abctrue_123"));
	}

	@Test
	public void LetterUNIONUnderscoreDigit() {
        assertEquals("abcUNION_123", UnicodeTranslator.toAscii("abcUNION_123"));
        assertEquals("abcunion_123", UnicodeTranslator.toAscii("abcunion_123"));
	}

	@Test
	public void Digit() {
        assertEquals("123", UnicodeTranslator.toAscii("123"));
	}

	@Test
	public void DigitLetter() {
        assertEquals("123abc", UnicodeTranslator.toAscii("123abc"));
	}

	@Test
	public void DigitUnderscore() {
        assertEquals("123_", UnicodeTranslator.toAscii("123_"));
	}

	@Test
	public void DigitANY() {
        assertEquals("123ANY", UnicodeTranslator.toAscii("123ANY"));
        assertEquals("123any", UnicodeTranslator.toAscii("123any"));
	}

	@Test
	public void DigitFALSE() {
        assertEquals("123FALSE", UnicodeTranslator.toAscii("123FALSE"));
        assertEquals("123false", UnicodeTranslator.toAscii("123false"));
	}

	@Test
	public void DigitINTEGER() {
        assertEquals("123INTEGER", UnicodeTranslator.toAscii("123INTEGER"));
        assertEquals("123integer", UnicodeTranslator.toAscii("123integer"));
	}

	@Test
	public void DigitINTER() {
        assertEquals("123INTER", UnicodeTranslator.toAscii("123INTER"));
        assertEquals("123inter", UnicodeTranslator.toAscii("123inter"));
	}

	@Test
	public void DigitNAT() {
        assertEquals("123NAT", UnicodeTranslator.toAscii("123NAT"));
        assertEquals("123nat", UnicodeTranslator.toAscii("123nat"));
	}

	@Test
	public void DigitNAT1() {
        assertEquals("123NAT1", UnicodeTranslator.toAscii("123NAT1"));
        assertEquals("123nat1", UnicodeTranslator.toAscii("123nat1"));
	}

	@Test
	public void DigitNATURAL() {
        assertEquals("123NATURAL", UnicodeTranslator.toAscii("123NATURAL"));
        assertEquals("123natural", UnicodeTranslator.toAscii("123natural"));
	}

	@Test
	public void DigitNOT() {
        assertEquals("123NOT", UnicodeTranslator.toAscii("123NOT"));
        assertEquals("123not", UnicodeTranslator.toAscii("123not"));
	}

	@Test
	public void DigitOR() {
        assertEquals("123OR", UnicodeTranslator.toAscii("123OR"));
        assertEquals("123or", UnicodeTranslator.toAscii("123or"));
	}

	@Test
	public void DigitPOW() {
        assertEquals("123POW", UnicodeTranslator.toAscii("123POW"));
        assertEquals("123pow", UnicodeTranslator.toAscii("123pow"));
	}

	@Test
	public void DigitPOW1() {
        assertEquals("123POW1", UnicodeTranslator.toAscii("123POW1"));
        assertEquals("123pow1", UnicodeTranslator.toAscii("123pow1"));
	}

	@Test
	public void DigitTRUE() {
        assertEquals("123TRUE", UnicodeTranslator.toAscii("123TRUE"));
        assertEquals("123true", UnicodeTranslator.toAscii("123true"));
	}

	@Test
	public void DigitUNION() {
        assertEquals("123UNION", UnicodeTranslator.toAscii("123UNION"));
        assertEquals("123union", UnicodeTranslator.toAscii("123union"));
	}

	@Test
	public void DigitLetterUnderscore() {
        assertEquals("123abc_", UnicodeTranslator.toAscii("123abc_"));
	}

	@Test
	public void DigitLetterANY() {
        assertEquals("123abcANY", UnicodeTranslator.toAscii("123abcANY"));
        assertEquals("123abcany", UnicodeTranslator.toAscii("123abcany"));
	}

	@Test
	public void DigitLetterFALSE() {
        assertEquals("123abcFALSE", UnicodeTranslator.toAscii("123abcFALSE"));
        assertEquals("123abcfalse", UnicodeTranslator.toAscii("123abcfalse"));
	}

	@Test
	public void DigitLetterINTEGER() {
        assertEquals("123abcINTEGER", UnicodeTranslator.toAscii("123abcINTEGER"));
        assertEquals("123abcinteger", UnicodeTranslator.toAscii("123abcinteger"));
	}

	@Test
	public void DigitLetterINTER() {
        assertEquals("123abcINTER", UnicodeTranslator.toAscii("123abcINTER"));
        assertEquals("123abcinter", UnicodeTranslator.toAscii("123abcinter"));
	}

	@Test
	public void DigitLetterNAT() {
        assertEquals("123abcNAT", UnicodeTranslator.toAscii("123abcNAT"));
        assertEquals("123abcnat", UnicodeTranslator.toAscii("123abcnat"));
	}

	@Test
	public void DigitLetterNAT1() {
        assertEquals("123abcNAT1", UnicodeTranslator.toAscii("123abcNAT1"));
        assertEquals("123abcnat1", UnicodeTranslator.toAscii("123abcnat1"));
	}

	@Test
	public void DigitLetterNATURAL() {
        assertEquals("123abcNATURAL", UnicodeTranslator.toAscii("123abcNATURAL"));
        assertEquals("123abcnatural", UnicodeTranslator.toAscii("123abcnatural"));
	}

	@Test
	public void DigitLetterNOT() {
        assertEquals("123abcNOT", UnicodeTranslator.toAscii("123abcNOT"));
        assertEquals("123abcnot", UnicodeTranslator.toAscii("123abcnot"));
	}

	@Test
	public void DigitLetterOR() {
        assertEquals("123abcOR", UnicodeTranslator.toAscii("123abcOR"));
        assertEquals("123abcor", UnicodeTranslator.toAscii("123abcor"));
	}

	@Test
	public void DigitLetterPOW() {
        assertEquals("123abcPOW", UnicodeTranslator.toAscii("123abcPOW"));
        assertEquals("123abcpow", UnicodeTranslator.toAscii("123abcpow"));
	}

	@Test
	public void DigitLetterPOW1() {
        assertEquals("123abcPOW1", UnicodeTranslator.toAscii("123abcPOW1"));
        assertEquals("123abcpow1", UnicodeTranslator.toAscii("123abcpow1"));
	}

	@Test
	public void DigitLetterTRUE() {
        assertEquals("123abcTRUE", UnicodeTranslator.toAscii("123abcTRUE"));
        assertEquals("123abctrue", UnicodeTranslator.toAscii("123abctrue"));
	}

	@Test
	public void DigitLetterUNION() {
        assertEquals("123abcUNION", UnicodeTranslator.toAscii("123abcUNION"));
        assertEquals("123abcunion", UnicodeTranslator.toAscii("123abcunion"));
	}

	@Test
	public void DigitUnderscoreLetter() {
        assertEquals("123_abc", UnicodeTranslator.toAscii("123_abc"));
	}

	@Test
	public void DigitUnderscoreANY() {
        assertEquals("123_ANY", UnicodeTranslator.toAscii("123_ANY"));
        assertEquals("123_any", UnicodeTranslator.toAscii("123_any"));
	}

	@Test
	public void DigitUnderscoreFALSE() {
        assertEquals("123_FALSE", UnicodeTranslator.toAscii("123_FALSE"));
        assertEquals("123_false", UnicodeTranslator.toAscii("123_false"));
	}

	@Test
	public void DigitUnderscoreINTEGER() {
        assertEquals("123_INTEGER", UnicodeTranslator.toAscii("123_INTEGER"));
        assertEquals("123_integer", UnicodeTranslator.toAscii("123_integer"));
	}

	@Test
	public void DigitUnderscoreINTER() {
        assertEquals("123_INTER", UnicodeTranslator.toAscii("123_INTER"));
        assertEquals("123_inter", UnicodeTranslator.toAscii("123_inter"));
	}

	@Test
	public void DigitUnderscoreNAT() {
        assertEquals("123_NAT", UnicodeTranslator.toAscii("123_NAT"));
        assertEquals("123_nat", UnicodeTranslator.toAscii("123_nat"));
	}

	@Test
	public void DigitUnderscoreNAT1() {
        assertEquals("123_NAT1", UnicodeTranslator.toAscii("123_NAT1"));
        assertEquals("123_nat1", UnicodeTranslator.toAscii("123_nat1"));
	}

	@Test
	public void DigitUnderscoreNATURAL() {
        assertEquals("123_NATURAL", UnicodeTranslator.toAscii("123_NATURAL"));
        assertEquals("123_natural", UnicodeTranslator.toAscii("123_natural"));
	}

	@Test
	public void DigitUnderscoreNOT() {
        assertEquals("123_NOT", UnicodeTranslator.toAscii("123_NOT"));
        assertEquals("123_not", UnicodeTranslator.toAscii("123_not"));
	}

	@Test
	public void DigitUnderscoreOR() {
        assertEquals("123_OR", UnicodeTranslator.toAscii("123_OR"));
        assertEquals("123_or", UnicodeTranslator.toAscii("123_or"));
	}

	@Test
	public void DigitUnderscorePOW() {
        assertEquals("123_POW", UnicodeTranslator.toAscii("123_POW"));
        assertEquals("123_pow", UnicodeTranslator.toAscii("123_pow"));
	}

	@Test
	public void DigitUnderscorePOW1() {
        assertEquals("123_POW1", UnicodeTranslator.toAscii("123_POW1"));
        assertEquals("123_pow1", UnicodeTranslator.toAscii("123_pow1"));
	}

	@Test
	public void DigitUnderscoreTRUE() {
        assertEquals("123_TRUE", UnicodeTranslator.toAscii("123_TRUE"));
        assertEquals("123_true", UnicodeTranslator.toAscii("123_true"));
	}

	@Test
	public void DigitUnderscoreUNION() {
        assertEquals("123_UNION", UnicodeTranslator.toAscii("123_UNION"));
        assertEquals("123_union", UnicodeTranslator.toAscii("123_union"));
	}

	@Test
	public void DigitANYLetter() {
        assertEquals("123ANYabc", UnicodeTranslator.toAscii("123ANYabc"));
        assertEquals("123anyabc", UnicodeTranslator.toAscii("123anyabc"));
	}

	@Test
	public void DigitFALSELetter() {
        assertEquals("123FALSEabc", UnicodeTranslator.toAscii("123FALSEabc"));
        assertEquals("123falseabc", UnicodeTranslator.toAscii("123falseabc"));
	}

	@Test
	public void DigitINTEGERLetter() {
        assertEquals("123INTEGERabc", UnicodeTranslator.toAscii("123INTEGERabc"));
        assertEquals("123integerabc", UnicodeTranslator.toAscii("123integerabc"));
	}

	@Test
	public void DigitINTERLetter() {
        assertEquals("123INTERabc", UnicodeTranslator.toAscii("123INTERabc"));
        assertEquals("123interabc", UnicodeTranslator.toAscii("123interabc"));
	}

	@Test
	public void DigitNATLetter() {
        assertEquals("123NATabc", UnicodeTranslator.toAscii("123NATabc"));
        assertEquals("123natabc", UnicodeTranslator.toAscii("123natabc"));
	}

	@Test
	public void DigitNAT1Letter() {
        assertEquals("123NAT1abc", UnicodeTranslator.toAscii("123NAT1abc"));
        assertEquals("123nat1abc", UnicodeTranslator.toAscii("123nat1abc"));
	}

	@Test
	public void DigitNATURALLetter() {
        assertEquals("123NATURALabc", UnicodeTranslator.toAscii("123NATURALabc"));
        assertEquals("123naturalabc", UnicodeTranslator.toAscii("123naturalabc"));
	}

	@Test
	public void DigitNOTLetter() {
        assertEquals("123NOTabc", UnicodeTranslator.toAscii("123NOTabc"));
        assertEquals("123notabc", UnicodeTranslator.toAscii("123notabc"));
	}

	@Test
	public void DigitORLetter() {
        assertEquals("123ORabc", UnicodeTranslator.toAscii("123ORabc"));
        assertEquals("123orabc", UnicodeTranslator.toAscii("123orabc"));
	}

	@Test
	public void DigitPOWLetter() {
        assertEquals("123POWabc", UnicodeTranslator.toAscii("123POWabc"));
        assertEquals("123powabc", UnicodeTranslator.toAscii("123powabc"));
	}

	@Test
	public void DigitPOW1Letter() {
        assertEquals("123POW1abc", UnicodeTranslator.toAscii("123POW1abc"));
        assertEquals("123pow1abc", UnicodeTranslator.toAscii("123pow1abc"));
	}

	@Test
	public void DigitTRUELetter() {
        assertEquals("123TRUEabc", UnicodeTranslator.toAscii("123TRUEabc"));
        assertEquals("123trueabc", UnicodeTranslator.toAscii("123trueabc"));
	}

	@Test
	public void DigitUNIONLetter() {
        assertEquals("123UNIONabc", UnicodeTranslator.toAscii("123UNIONabc"));
        assertEquals("123unionabc", UnicodeTranslator.toAscii("123unionabc"));
	}

	@Test
	public void DigitANYUnderscore() {
        assertEquals("123ANY_", UnicodeTranslator.toAscii("123ANY_"));
        assertEquals("123any_", UnicodeTranslator.toAscii("123any_"));
	}

	@Test
	public void DigitFALSEUnderscore() {
        assertEquals("123FALSE_", UnicodeTranslator.toAscii("123FALSE_"));
        assertEquals("123false_", UnicodeTranslator.toAscii("123false_"));
	}

	@Test
	public void DigitINTEGERUnderscore() {
        assertEquals("123INTEGER_", UnicodeTranslator.toAscii("123INTEGER_"));
        assertEquals("123integer_", UnicodeTranslator.toAscii("123integer_"));
	}

	@Test
	public void DigitINTERUnderscore() {
        assertEquals("123INTER_", UnicodeTranslator.toAscii("123INTER_"));
        assertEquals("123inter_", UnicodeTranslator.toAscii("123inter_"));
	}

	@Test
	public void DigitNATUnderscore() {
        assertEquals("123NAT_", UnicodeTranslator.toAscii("123NAT_"));
        assertEquals("123nat_", UnicodeTranslator.toAscii("123nat_"));
	}

	@Test
	public void DigitNAT1Underscore() {
        assertEquals("123NAT1_", UnicodeTranslator.toAscii("123NAT1_"));
        assertEquals("123nat1_", UnicodeTranslator.toAscii("123nat1_"));
	}

	@Test
	public void DigitNATURALUnderscore() {
        assertEquals("123NATURAL_", UnicodeTranslator.toAscii("123NATURAL_"));
        assertEquals("123natural_", UnicodeTranslator.toAscii("123natural_"));
	}

	@Test
	public void DigitNOTUnderscore() {
        assertEquals("123NOT_", UnicodeTranslator.toAscii("123NOT_"));
        assertEquals("123not_", UnicodeTranslator.toAscii("123not_"));
	}

	@Test
	public void DigitORUnderscore() {
        assertEquals("123OR_", UnicodeTranslator.toAscii("123OR_"));
        assertEquals("123or_", UnicodeTranslator.toAscii("123or_"));
	}

	@Test
	public void DigitPOWUnderscore() {
        assertEquals("123POW_", UnicodeTranslator.toAscii("123POW_"));
        assertEquals("123pow_", UnicodeTranslator.toAscii("123pow_"));
	}

	@Test
	public void DigitPOW1Underscore() {
        assertEquals("123POW1_", UnicodeTranslator.toAscii("123POW1_"));
        assertEquals("123pow1_", UnicodeTranslator.toAscii("123pow1_"));
	}

	@Test
	public void DigitTRUEUnderscore() {
        assertEquals("123TRUE_", UnicodeTranslator.toAscii("123TRUE_"));
        assertEquals("123true_", UnicodeTranslator.toAscii("123true_"));
	}

	@Test
	public void DigitUNIONUnderscore() {
        assertEquals("123UNION_", UnicodeTranslator.toAscii("123UNION_"));
        assertEquals("123union_", UnicodeTranslator.toAscii("123union_"));
	}

	@Test
	public void DigitLetterUnderscoreANY() {
        assertEquals("123abc_ANY", UnicodeTranslator.toAscii("123abc_ANY"));
        assertEquals("123abc_any", UnicodeTranslator.toAscii("123abc_any"));
	}

	@Test
	public void DigitLetterUnderscoreFALSE() {
        assertEquals("123abc_FALSE", UnicodeTranslator.toAscii("123abc_FALSE"));
        assertEquals("123abc_false", UnicodeTranslator.toAscii("123abc_false"));
	}

	@Test
	public void DigitLetterUnderscoreINTEGER() {
        assertEquals("123abc_INTEGER", UnicodeTranslator.toAscii("123abc_INTEGER"));
        assertEquals("123abc_integer", UnicodeTranslator.toAscii("123abc_integer"));
	}

	@Test
	public void DigitLetterUnderscoreINTER() {
        assertEquals("123abc_INTER", UnicodeTranslator.toAscii("123abc_INTER"));
        assertEquals("123abc_inter", UnicodeTranslator.toAscii("123abc_inter"));
	}

	@Test
	public void DigitLetterUnderscoreNAT() {
        assertEquals("123abc_NAT", UnicodeTranslator.toAscii("123abc_NAT"));
        assertEquals("123abc_nat", UnicodeTranslator.toAscii("123abc_nat"));
	}

	@Test
	public void DigitLetterUnderscoreNAT1() {
        assertEquals("123abc_NAT1", UnicodeTranslator.toAscii("123abc_NAT1"));
        assertEquals("123abc_nat1", UnicodeTranslator.toAscii("123abc_nat1"));
	}

	@Test
	public void DigitLetterUnderscoreNATURAL() {
        assertEquals("123abc_NATURAL", UnicodeTranslator.toAscii("123abc_NATURAL"));
        assertEquals("123abc_natural", UnicodeTranslator.toAscii("123abc_natural"));
	}

	@Test
	public void DigitLetterUnderscoreNOT() {
        assertEquals("123abc_NOT", UnicodeTranslator.toAscii("123abc_NOT"));
        assertEquals("123abc_not", UnicodeTranslator.toAscii("123abc_not"));
	}

	@Test
	public void DigitLetterUnderscoreOR() {
        assertEquals("123abc_OR", UnicodeTranslator.toAscii("123abc_OR"));
        assertEquals("123abc_or", UnicodeTranslator.toAscii("123abc_or"));
	}

	@Test
	public void DigitLetterUnderscorePOW() {
        assertEquals("123abc_POW", UnicodeTranslator.toAscii("123abc_POW"));
        assertEquals("123abc_pow", UnicodeTranslator.toAscii("123abc_pow"));
	}

	@Test
	public void DigitLetterUnderscorePOW1() {
        assertEquals("123abc_POW1", UnicodeTranslator.toAscii("123abc_POW1"));
        assertEquals("123abc_pow1", UnicodeTranslator.toAscii("123abc_pow1"));
	}

	@Test
	public void DigitLetterUnderscoreTRUE() {
        assertEquals("123abc_TRUE", UnicodeTranslator.toAscii("123abc_TRUE"));
        assertEquals("123abc_true", UnicodeTranslator.toAscii("123abc_true"));
	}

	@Test
	public void DigitLetterUnderscoreUNION() {
        assertEquals("123abc_UNION", UnicodeTranslator.toAscii("123abc_UNION"));
        assertEquals("123abc_union", UnicodeTranslator.toAscii("123abc_union"));
	}

	@Test
	public void DigitLetterANYUnderscore() {
        assertEquals("123abcANY_", UnicodeTranslator.toAscii("123abcANY_"));
        assertEquals("123abcany_", UnicodeTranslator.toAscii("123abcany_"));
	}

	@Test
	public void DigitLetterFALSEUnderscore() {
        assertEquals("123abcFALSE_", UnicodeTranslator.toAscii("123abcFALSE_"));
        assertEquals("123abcfalse_", UnicodeTranslator.toAscii("123abcfalse_"));
	}

	@Test
	public void DigitLetterINTEGERUnderscore() {
        assertEquals("123abcINTEGER_", UnicodeTranslator.toAscii("123abcINTEGER_"));
        assertEquals("123abcinteger_", UnicodeTranslator.toAscii("123abcinteger_"));
	}

	@Test
	public void DigitLetterINTERUnderscore() {
        assertEquals("123abcINTER_", UnicodeTranslator.toAscii("123abcINTER_"));
        assertEquals("123abcinter_", UnicodeTranslator.toAscii("123abcinter_"));
	}

	@Test
	public void DigitLetterNATUnderscore() {
        assertEquals("123abcNAT_", UnicodeTranslator.toAscii("123abcNAT_"));
        assertEquals("123abcnat_", UnicodeTranslator.toAscii("123abcnat_"));
	}

	@Test
	public void DigitLetterNAT1Underscore() {
        assertEquals("123abcNAT1_", UnicodeTranslator.toAscii("123abcNAT1_"));
        assertEquals("123abcnat1_", UnicodeTranslator.toAscii("123abcnat1_"));
	}

	@Test
	public void DigitLetterNATURALUnderscore() {
        assertEquals("123abcNATURAL_", UnicodeTranslator.toAscii("123abcNATURAL_"));
        assertEquals("123abcnatural_", UnicodeTranslator.toAscii("123abcnatural_"));
	}

	@Test
	public void DigitLetterNOTUnderscore() {
        assertEquals("123abcNOT_", UnicodeTranslator.toAscii("123abcNOT_"));
        assertEquals("123abcnot_", UnicodeTranslator.toAscii("123abcnot_"));
	}

	@Test
	public void DigitLetterORUnderscore() {
        assertEquals("123abcOR_", UnicodeTranslator.toAscii("123abcOR_"));
        assertEquals("123abcor_", UnicodeTranslator.toAscii("123abcor_"));
	}

	@Test
	public void DigitLetterPOWUnderscore() {
        assertEquals("123abcPOW_", UnicodeTranslator.toAscii("123abcPOW_"));
        assertEquals("123abcpow_", UnicodeTranslator.toAscii("123abcpow_"));
	}

	@Test
	public void DigitLetterPOW1Underscore() {
        assertEquals("123abcPOW1_", UnicodeTranslator.toAscii("123abcPOW1_"));
        assertEquals("123abcpow1_", UnicodeTranslator.toAscii("123abcpow1_"));
	}

	@Test
	public void DigitLetterTRUEUnderscore() {
        assertEquals("123abcTRUE_", UnicodeTranslator.toAscii("123abcTRUE_"));
        assertEquals("123abctrue_", UnicodeTranslator.toAscii("123abctrue_"));
	}

	@Test
	public void DigitLetterUNIONUnderscore() {
        assertEquals("123abcUNION_", UnicodeTranslator.toAscii("123abcUNION_"));
        assertEquals("123abcunion_", UnicodeTranslator.toAscii("123abcunion_"));
	}

	@Test
	public void DigitUnderscoreLetterANY() {
        assertEquals("123_abcANY", UnicodeTranslator.toAscii("123_abcANY"));
        assertEquals("123_abcany", UnicodeTranslator.toAscii("123_abcany"));
	}

	@Test
	public void DigitUnderscoreLetterFALSE() {
        assertEquals("123_abcFALSE", UnicodeTranslator.toAscii("123_abcFALSE"));
        assertEquals("123_abcfalse", UnicodeTranslator.toAscii("123_abcfalse"));
	}

	@Test
	public void DigitUnderscoreLetterINTEGER() {
        assertEquals("123_abcINTEGER", UnicodeTranslator.toAscii("123_abcINTEGER"));
        assertEquals("123_abcinteger", UnicodeTranslator.toAscii("123_abcinteger"));
	}

	@Test
	public void DigitUnderscoreLetterINTER() {
        assertEquals("123_abcINTER", UnicodeTranslator.toAscii("123_abcINTER"));
        assertEquals("123_abcinter", UnicodeTranslator.toAscii("123_abcinter"));
	}

	@Test
	public void DigitUnderscoreLetterNAT() {
        assertEquals("123_abcNAT", UnicodeTranslator.toAscii("123_abcNAT"));
        assertEquals("123_abcnat", UnicodeTranslator.toAscii("123_abcnat"));
	}

	@Test
	public void DigitUnderscoreLetterNAT1() {
        assertEquals("123_abcNAT1", UnicodeTranslator.toAscii("123_abcNAT1"));
        assertEquals("123_abcnat1", UnicodeTranslator.toAscii("123_abcnat1"));
	}

	@Test
	public void DigitUnderscoreLetterNATURAL() {
        assertEquals("123_abcNATURAL", UnicodeTranslator.toAscii("123_abcNATURAL"));
        assertEquals("123_abcnatural", UnicodeTranslator.toAscii("123_abcnatural"));
	}

	@Test
	public void DigitUnderscoreLetterNOT() {
        assertEquals("123_abcNOT", UnicodeTranslator.toAscii("123_abcNOT"));
        assertEquals("123_abcnot", UnicodeTranslator.toAscii("123_abcnot"));
	}

	@Test
	public void DigitUnderscoreLetterOR() {
        assertEquals("123_abcOR", UnicodeTranslator.toAscii("123_abcOR"));
        assertEquals("123_abcor", UnicodeTranslator.toAscii("123_abcor"));
	}

	@Test
	public void DigitUnderscoreLetterPOW() {
        assertEquals("123_abcPOW", UnicodeTranslator.toAscii("123_abcPOW"));
        assertEquals("123_abcpow", UnicodeTranslator.toAscii("123_abcpow"));
	}

	@Test
	public void DigitUnderscoreLetterPOW1() {
        assertEquals("123_abcPOW1", UnicodeTranslator.toAscii("123_abcPOW1"));
        assertEquals("123_abcpow1", UnicodeTranslator.toAscii("123_abcpow1"));
	}

	@Test
	public void DigitUnderscoreLetterTRUE() {
        assertEquals("123_abcTRUE", UnicodeTranslator.toAscii("123_abcTRUE"));
        assertEquals("123_abctrue", UnicodeTranslator.toAscii("123_abctrue"));
	}

	@Test
	public void DigitUnderscoreLetterUNION() {
        assertEquals("123_abcUNION", UnicodeTranslator.toAscii("123_abcUNION"));
        assertEquals("123_abcunion", UnicodeTranslator.toAscii("123_abcunion"));
	}

	@Test
	public void DigitUnderscoreANYLetter() {
        assertEquals("123_ANYabc", UnicodeTranslator.toAscii("123_ANYabc"));
        assertEquals("123_anyabc", UnicodeTranslator.toAscii("123_anyabc"));
	}

	@Test
	public void DigitUnderscoreFALSELetter() {
        assertEquals("123_FALSEabc", UnicodeTranslator.toAscii("123_FALSEabc"));
        assertEquals("123_falseabc", UnicodeTranslator.toAscii("123_falseabc"));
	}

	@Test
	public void DigitUnderscoreINTEGERLetter() {
        assertEquals("123_INTEGERabc", UnicodeTranslator.toAscii("123_INTEGERabc"));
        assertEquals("123_integerabc", UnicodeTranslator.toAscii("123_integerabc"));
	}

	@Test
	public void DigitUnderscoreINTERLetter() {
        assertEquals("123_INTERabc", UnicodeTranslator.toAscii("123_INTERabc"));
        assertEquals("123_interabc", UnicodeTranslator.toAscii("123_interabc"));
	}

	@Test
	public void DigitUnderscoreNATLetter() {
        assertEquals("123_NATabc", UnicodeTranslator.toAscii("123_NATabc"));
        assertEquals("123_natabc", UnicodeTranslator.toAscii("123_natabc"));
	}

	@Test
	public void DigitUnderscoreNAT1Letter() {
        assertEquals("123_NAT1abc", UnicodeTranslator.toAscii("123_NAT1abc"));
        assertEquals("123_nat1abc", UnicodeTranslator.toAscii("123_nat1abc"));
	}

	@Test
	public void DigitUnderscoreNATURALLetter() {
        assertEquals("123_NATURALabc", UnicodeTranslator.toAscii("123_NATURALabc"));
        assertEquals("123_naturalabc", UnicodeTranslator.toAscii("123_naturalabc"));
	}

	@Test
	public void DigitUnderscoreNOTLetter() {
        assertEquals("123_NOTabc", UnicodeTranslator.toAscii("123_NOTabc"));
        assertEquals("123_notabc", UnicodeTranslator.toAscii("123_notabc"));
	}

	@Test
	public void DigitUnderscoreORLetter() {
        assertEquals("123_ORabc", UnicodeTranslator.toAscii("123_ORabc"));
        assertEquals("123_orabc", UnicodeTranslator.toAscii("123_orabc"));
	}

	@Test
	public void DigitUnderscorePOWLetter() {
        assertEquals("123_POWabc", UnicodeTranslator.toAscii("123_POWabc"));
        assertEquals("123_powabc", UnicodeTranslator.toAscii("123_powabc"));
	}

	@Test
	public void DigitUnderscorePOW1Letter() {
        assertEquals("123_POW1abc", UnicodeTranslator.toAscii("123_POW1abc"));
        assertEquals("123_pow1abc", UnicodeTranslator.toAscii("123_pow1abc"));
	}

	@Test
	public void DigitUnderscoreTRUELetter() {
        assertEquals("123_TRUEabc", UnicodeTranslator.toAscii("123_TRUEabc"));
        assertEquals("123_trueabc", UnicodeTranslator.toAscii("123_trueabc"));
	}

	@Test
	public void DigitUnderscoreUNIONLetter() {
        assertEquals("123_UNIONabc", UnicodeTranslator.toAscii("123_UNIONabc"));
        assertEquals("123_unionabc", UnicodeTranslator.toAscii("123_unionabc"));
	}

	@Test
	public void DigitANYLetterUnderscore() {
        assertEquals("123ANYabc_", UnicodeTranslator.toAscii("123ANYabc_"));
        assertEquals("123anyabc_", UnicodeTranslator.toAscii("123anyabc_"));
	}

	@Test
	public void DigitFALSELetterUnderscore() {
        assertEquals("123FALSEabc_", UnicodeTranslator.toAscii("123FALSEabc_"));
        assertEquals("123falseabc_", UnicodeTranslator.toAscii("123falseabc_"));
	}

	@Test
	public void DigitINTEGERLetterUnderscore() {
        assertEquals("123INTEGERabc_", UnicodeTranslator.toAscii("123INTEGERabc_"));
        assertEquals("123integerabc_", UnicodeTranslator.toAscii("123integerabc_"));
	}

	@Test
	public void DigitINTERLetterUnderscore() {
        assertEquals("123INTERabc_", UnicodeTranslator.toAscii("123INTERabc_"));
        assertEquals("123interabc_", UnicodeTranslator.toAscii("123interabc_"));
	}

	@Test
	public void DigitNATLetterUnderscore() {
        assertEquals("123NATabc_", UnicodeTranslator.toAscii("123NATabc_"));
        assertEquals("123natabc_", UnicodeTranslator.toAscii("123natabc_"));
	}

	@Test
	public void DigitNAT1LetterUnderscore() {
        assertEquals("123NAT1abc_", UnicodeTranslator.toAscii("123NAT1abc_"));
        assertEquals("123nat1abc_", UnicodeTranslator.toAscii("123nat1abc_"));
	}

	@Test
	public void DigitNATURALLetterUnderscore() {
        assertEquals("123NATURALabc_", UnicodeTranslator.toAscii("123NATURALabc_"));
        assertEquals("123naturalabc_", UnicodeTranslator.toAscii("123naturalabc_"));
	}

	@Test
	public void DigitNOTLetterUnderscore() {
        assertEquals("123NOTabc_", UnicodeTranslator.toAscii("123NOTabc_"));
        assertEquals("123notabc_", UnicodeTranslator.toAscii("123notabc_"));
	}

	@Test
	public void DigitORLetterUnderscore() {
        assertEquals("123ORabc_", UnicodeTranslator.toAscii("123ORabc_"));
        assertEquals("123orabc_", UnicodeTranslator.toAscii("123orabc_"));
	}

	@Test
	public void DigitPOWLetterUnderscore() {
        assertEquals("123POWabc_", UnicodeTranslator.toAscii("123POWabc_"));
        assertEquals("123powabc_", UnicodeTranslator.toAscii("123powabc_"));
	}

	@Test
	public void DigitPOW1LetterUnderscore() {
        assertEquals("123POW1abc_", UnicodeTranslator.toAscii("123POW1abc_"));
        assertEquals("123pow1abc_", UnicodeTranslator.toAscii("123pow1abc_"));
	}

	@Test
	public void DigitTRUELetterUnderscore() {
        assertEquals("123TRUEabc_", UnicodeTranslator.toAscii("123TRUEabc_"));
        assertEquals("123trueabc_", UnicodeTranslator.toAscii("123trueabc_"));
	}

	@Test
	public void DigitUNIONLetterUnderscore() {
        assertEquals("123UNIONabc_", UnicodeTranslator.toAscii("123UNIONabc_"));
        assertEquals("123unionabc_", UnicodeTranslator.toAscii("123unionabc_"));
	}

	@Test
	public void DigitANYUnderscoreLetter() {
        assertEquals("123ANY_abc", UnicodeTranslator.toAscii("123ANY_abc"));
        assertEquals("123any_abc", UnicodeTranslator.toAscii("123any_abc"));
	}

	@Test
	public void DigitFALSEUnderscoreLetter() {
        assertEquals("123FALSE_abc", UnicodeTranslator.toAscii("123FALSE_abc"));
        assertEquals("123false_abc", UnicodeTranslator.toAscii("123false_abc"));
	}

	@Test
	public void DigitINTEGERUnderscoreLetter() {
        assertEquals("123INTEGER_abc", UnicodeTranslator.toAscii("123INTEGER_abc"));
        assertEquals("123integer_abc", UnicodeTranslator.toAscii("123integer_abc"));
	}

	@Test
	public void DigitINTERUnderscoreLetter() {
        assertEquals("123INTER_abc", UnicodeTranslator.toAscii("123INTER_abc"));
        assertEquals("123inter_abc", UnicodeTranslator.toAscii("123inter_abc"));
	}

	@Test
	public void DigitNATUnderscoreLetter() {
        assertEquals("123NAT_abc", UnicodeTranslator.toAscii("123NAT_abc"));
        assertEquals("123nat_abc", UnicodeTranslator.toAscii("123nat_abc"));
	}

	@Test
	public void DigitNAT1UnderscoreLetter() {
        assertEquals("123NAT1_abc", UnicodeTranslator.toAscii("123NAT1_abc"));
        assertEquals("123nat1_abc", UnicodeTranslator.toAscii("123nat1_abc"));
	}

	@Test
	public void DigitNATURALUnderscoreLetter() {
        assertEquals("123NATURAL_abc", UnicodeTranslator.toAscii("123NATURAL_abc"));
        assertEquals("123natural_abc", UnicodeTranslator.toAscii("123natural_abc"));
	}

	@Test
	public void DigitNOTUnderscoreLetter() {
        assertEquals("123NOT_abc", UnicodeTranslator.toAscii("123NOT_abc"));
        assertEquals("123not_abc", UnicodeTranslator.toAscii("123not_abc"));
	}

	@Test
	public void DigitORUnderscoreLetter() {
        assertEquals("123OR_abc", UnicodeTranslator.toAscii("123OR_abc"));
        assertEquals("123or_abc", UnicodeTranslator.toAscii("123or_abc"));
	}

	@Test
	public void DigitPOWUnderscoreLetter() {
        assertEquals("123POW_abc", UnicodeTranslator.toAscii("123POW_abc"));
        assertEquals("123pow_abc", UnicodeTranslator.toAscii("123pow_abc"));
	}

	@Test
	public void DigitPOW1UnderscoreLetter() {
        assertEquals("123POW1_abc", UnicodeTranslator.toAscii("123POW1_abc"));
        assertEquals("123pow1_abc", UnicodeTranslator.toAscii("123pow1_abc"));
	}

	@Test
	public void DigitTRUEUnderscoreLetter() {
        assertEquals("123TRUE_abc", UnicodeTranslator.toAscii("123TRUE_abc"));
        assertEquals("123true_abc", UnicodeTranslator.toAscii("123true_abc"));
	}

	@Test
	public void DigitUNIONUnderscoreLetter() {
        assertEquals("123UNION_abc", UnicodeTranslator.toAscii("123UNION_abc"));
        assertEquals("123union_abc", UnicodeTranslator.toAscii("123union_abc"));
	}

	@Test
	public void Underscore() {
        assertEquals("_", UnicodeTranslator.toAscii("_"));
	}

	@Test
	public void UnderscoreLetter() {
        assertEquals("_abc", UnicodeTranslator.toAscii("_abc"));
	}

	@Test
	public void UnderscoreDigit() {
        assertEquals("_123", UnicodeTranslator.toAscii("_123"));
	}

	@Test
	public void UnderscoreANY() {
        assertEquals("_ANY", UnicodeTranslator.toAscii("_ANY"));
        assertEquals("_any", UnicodeTranslator.toAscii("_any"));
	}

	@Test
	public void UnderscoreFALSE() {
        assertEquals("_FALSE", UnicodeTranslator.toAscii("_FALSE"));
        assertEquals("_false", UnicodeTranslator.toAscii("_false"));
	}

	@Test
	public void UnderscoreINTEGER() {
        assertEquals("_INTEGER", UnicodeTranslator.toAscii("_INTEGER"));
        assertEquals("_integer", UnicodeTranslator.toAscii("_integer"));
	}

	@Test
	public void UnderscoreINTER() {
        assertEquals("_INTER", UnicodeTranslator.toAscii("_INTER"));
        assertEquals("_inter", UnicodeTranslator.toAscii("_inter"));
	}

	@Test
	public void UnderscoreNAT() {
        assertEquals("_NAT", UnicodeTranslator.toAscii("_NAT"));
        assertEquals("_nat", UnicodeTranslator.toAscii("_nat"));
	}

	@Test
	public void UnderscoreNAT1() {
        assertEquals("_NAT1", UnicodeTranslator.toAscii("_NAT1"));
        assertEquals("_nat1", UnicodeTranslator.toAscii("_nat1"));
	}

	@Test
	public void UnderscoreNATURAL() {
        assertEquals("_NATURAL", UnicodeTranslator.toAscii("_NATURAL"));
        assertEquals("_natural", UnicodeTranslator.toAscii("_natural"));
	}

	@Test
	public void UnderscoreNOT() {
        assertEquals("_NOT", UnicodeTranslator.toAscii("_NOT"));
        assertEquals("_not", UnicodeTranslator.toAscii("_not"));
	}

	@Test
	public void UnderscoreOR() {
        assertEquals("_OR", UnicodeTranslator.toAscii("_OR"));
        assertEquals("_or", UnicodeTranslator.toAscii("_or"));
	}

	@Test
	public void UnderscorePOW() {
        assertEquals("_POW", UnicodeTranslator.toAscii("_POW"));
        assertEquals("_pow", UnicodeTranslator.toAscii("_pow"));
	}

	@Test
	public void UnderscorePOW1() {
        assertEquals("_POW1", UnicodeTranslator.toAscii("_POW1"));
        assertEquals("_pow1", UnicodeTranslator.toAscii("_pow1"));
	}

	@Test
	public void UnderscoreTRUE() {
        assertEquals("_TRUE", UnicodeTranslator.toAscii("_TRUE"));
        assertEquals("_true", UnicodeTranslator.toAscii("_true"));
	}

	@Test
	public void UnderscoreUNION() {
        assertEquals("_UNION", UnicodeTranslator.toAscii("_UNION"));
        assertEquals("_union", UnicodeTranslator.toAscii("_union"));
	}

	@Test
	public void UnderscoreLetterDigit() {
        assertEquals("_abc123", UnicodeTranslator.toAscii("_abc123"));
	}

	@Test
	public void UnderscoreLetterANY() {
        assertEquals("_123ANY", UnicodeTranslator.toAscii("_123ANY"));
        assertEquals("_123any", UnicodeTranslator.toAscii("_123any"));
	}

	@Test
	public void UnderscoreLetterFALSE() {
        assertEquals("_123FALSE", UnicodeTranslator.toAscii("_123FALSE"));
        assertEquals("_123false", UnicodeTranslator.toAscii("_123false"));
	}

	@Test
	public void UnderscoreLetterINTEGER() {
        assertEquals("_123INTEGER", UnicodeTranslator.toAscii("_123INTEGER"));
        assertEquals("_123integer", UnicodeTranslator.toAscii("_123integer"));
	}

	@Test
	public void UnderscoreLetterINTER() {
        assertEquals("_123INTER", UnicodeTranslator.toAscii("_123INTER"));
        assertEquals("_123inter", UnicodeTranslator.toAscii("_123inter"));
	}

	@Test
	public void UnderscoreLetterNAT() {
        assertEquals("_123NAT", UnicodeTranslator.toAscii("_123NAT"));
        assertEquals("_123nat", UnicodeTranslator.toAscii("_123nat"));
	}

	@Test
	public void UnderscoreLetterNAT1() {
        assertEquals("_123NAT1", UnicodeTranslator.toAscii("_123NAT1"));
        assertEquals("_123nat1", UnicodeTranslator.toAscii("_123nat1"));
	}

	@Test
	public void UnderscoreLetterNATURAL() {
        assertEquals("_123NATURAL", UnicodeTranslator.toAscii("_123NATURAL"));
        assertEquals("_123natural", UnicodeTranslator.toAscii("_123natural"));
	}

	@Test
	public void UnderscoreLetterNOT() {
        assertEquals("_123NOT", UnicodeTranslator.toAscii("_123NOT"));
        assertEquals("_123not", UnicodeTranslator.toAscii("_123not"));
	}

	@Test
	public void UnderscoreLetterOR() {
        assertEquals("_123OR", UnicodeTranslator.toAscii("_123OR"));
        assertEquals("_123or", UnicodeTranslator.toAscii("_123or"));
	}

	@Test
	public void UnderscoreLetterPOW() {
        assertEquals("_123POW", UnicodeTranslator.toAscii("_123POW"));
        assertEquals("_123pow", UnicodeTranslator.toAscii("_123pow"));
	}

	@Test
	public void UnderscoreLetterPOW1() {
        assertEquals("_123POW1", UnicodeTranslator.toAscii("_123POW1"));
        assertEquals("_123pow1", UnicodeTranslator.toAscii("_123pow1"));
	}

	@Test
	public void UnderscoreLetterTRUE() {
        assertEquals("_123TRUE", UnicodeTranslator.toAscii("_123TRUE"));
        assertEquals("_123true", UnicodeTranslator.toAscii("_123true"));
	}

	@Test
	public void UnderscoreLetterUNION() {
        assertEquals("_123UNION", UnicodeTranslator.toAscii("_123UNION"));
        assertEquals("_123union", UnicodeTranslator.toAscii("_123union"));
	}

	@Test
	public void UnderscoreDigitLetter() {
        assertEquals("_123abc", UnicodeTranslator.toAscii("_123abc"));
	}

	@Test
	public void UnderscoreDigitANY() {
        assertEquals("_123ANY", UnicodeTranslator.toAscii("_123ANY"));
        assertEquals("_123any", UnicodeTranslator.toAscii("_123any"));
	}

	@Test
	public void UnderscoreDigitFALSE() {
        assertEquals("_123FALSE", UnicodeTranslator.toAscii("_123FALSE"));
        assertEquals("_123false", UnicodeTranslator.toAscii("_123false"));
	}

	@Test
	public void UnderscoreDigitINTEGER() {
        assertEquals("_123INTEGER", UnicodeTranslator.toAscii("_123INTEGER"));
        assertEquals("_123integer", UnicodeTranslator.toAscii("_123integer"));
	}

	@Test
	public void UnderscoreDigitINTER() {
        assertEquals("_123INTER", UnicodeTranslator.toAscii("_123INTER"));
        assertEquals("_123inter", UnicodeTranslator.toAscii("_123inter"));
	}

	@Test
	public void UnderscoreDigitNAT() {
        assertEquals("_123NAT", UnicodeTranslator.toAscii("_123NAT"));
        assertEquals("_123nat", UnicodeTranslator.toAscii("_123nat"));
	}

	@Test
	public void UnderscoreDigitNAT1() {
        assertEquals("_123NAT1", UnicodeTranslator.toAscii("_123NAT1"));
        assertEquals("_123nat1", UnicodeTranslator.toAscii("_123nat1"));
	}

	@Test
	public void UnderscoreDigitNATURAL() {
        assertEquals("_123NATURAL", UnicodeTranslator.toAscii("_123NATURAL"));
        assertEquals("_123natural", UnicodeTranslator.toAscii("_123natural"));
	}

	@Test
	public void UnderscoreDigitNOT() {
        assertEquals("_123NOT", UnicodeTranslator.toAscii("_123NOT"));
        assertEquals("_123not", UnicodeTranslator.toAscii("_123not"));
	}

	@Test
	public void UnderscoreDigitOR() {
        assertEquals("_123OR", UnicodeTranslator.toAscii("_123OR"));
        assertEquals("_123or", UnicodeTranslator.toAscii("_123or"));
	}

	@Test
	public void UnderscoreDigitPOW() {
        assertEquals("_123POW", UnicodeTranslator.toAscii("_123POW"));
        assertEquals("_123pow", UnicodeTranslator.toAscii("_123pow"));
	}

	@Test
	public void UnderscoreDigitPOW1() {
        assertEquals("_123POW1", UnicodeTranslator.toAscii("_123POW1"));
        assertEquals("_123pow1", UnicodeTranslator.toAscii("_123pow1"));
	}

	@Test
	public void UnderscoreDigitTRUE() {
        assertEquals("_123TRUE", UnicodeTranslator.toAscii("_123TRUE"));
        assertEquals("_123true", UnicodeTranslator.toAscii("_123true"));
	}

	@Test
	public void UnderscoreDigitUNION() {
        assertEquals("_123UNION", UnicodeTranslator.toAscii("_123UNION"));
        assertEquals("_123union", UnicodeTranslator.toAscii("_123union"));
	}

	@Test
	public void UnderscoreANYLetter() {
        assertEquals("_ANYabc", UnicodeTranslator.toAscii("_ANYabc"));
        assertEquals("_anyabc", UnicodeTranslator.toAscii("_anyabc"));
	}

	@Test
	public void UnderscoreFALSELetter() {
        assertEquals("_FALSEabc", UnicodeTranslator.toAscii("_FALSEabc"));
        assertEquals("_falseabc", UnicodeTranslator.toAscii("_falseabc"));
	}

	@Test
	public void UnderscoreINTEGERLetter() {
        assertEquals("_INTEGERabc", UnicodeTranslator.toAscii("_INTEGERabc"));
        assertEquals("_integerabc", UnicodeTranslator.toAscii("_integerabc"));
	}

	@Test
	public void UnderscoreINTERLetter() {
        assertEquals("_INTERabc", UnicodeTranslator.toAscii("_INTERabc"));
        assertEquals("_interabc", UnicodeTranslator.toAscii("_interabc"));
	}

	@Test
	public void UnderscoreNATLetter() {
        assertEquals("_NATabc", UnicodeTranslator.toAscii("_NATabc"));
        assertEquals("_natabc", UnicodeTranslator.toAscii("_natabc"));
	}

	@Test
	public void UnderscoreNAT1Letter() {
        assertEquals("_NAT1abc", UnicodeTranslator.toAscii("_NAT1abc"));
        assertEquals("_nat1abc", UnicodeTranslator.toAscii("_nat1abc"));
	}

	@Test
	public void UnderscoreNATURALLetter() {
        assertEquals("_NATURALabc", UnicodeTranslator.toAscii("_NATURALabc"));
        assertEquals("_naturalabc", UnicodeTranslator.toAscii("_naturalabc"));
	}

	@Test
	public void UnderscoreNOTLetter() {
        assertEquals("_NOTabc", UnicodeTranslator.toAscii("_NOTabc"));
        assertEquals("_notabc", UnicodeTranslator.toAscii("_notabc"));
	}

	@Test
	public void UnderscoreORLetter() {
        assertEquals("_ORabc", UnicodeTranslator.toAscii("_ORabc"));
        assertEquals("_orabc", UnicodeTranslator.toAscii("_orabc"));
	}

	@Test
	public void UnderscorePOWLetter() {
        assertEquals("_POWabc", UnicodeTranslator.toAscii("_POWabc"));
        assertEquals("_powabc", UnicodeTranslator.toAscii("_powabc"));
	}

	@Test
	public void UnderscorePOW1Letter() {
        assertEquals("_POW1abc", UnicodeTranslator.toAscii("_POW1abc"));
        assertEquals("_pow1abc", UnicodeTranslator.toAscii("_pow1abc"));
	}

	@Test
	public void UnderscoreTRUELetter() {
        assertEquals("_TRUEabc", UnicodeTranslator.toAscii("_TRUEabc"));
        assertEquals("_trueabc", UnicodeTranslator.toAscii("_trueabc"));
	}

	@Test
	public void UnderscoreUNIONLetter() {
        assertEquals("_UNIONabc", UnicodeTranslator.toAscii("_UNIONabc"));
        assertEquals("_unionabc", UnicodeTranslator.toAscii("_unionabc"));
	}

	@Test
	public void UnderscoreANYDigit() {
        assertEquals("_ANY123", UnicodeTranslator.toAscii("_ANY123"));
        assertEquals("_any123", UnicodeTranslator.toAscii("_any123"));
	}

	@Test
	public void UnderscoreFALSEDigit() {
        assertEquals("_FALSE123", UnicodeTranslator.toAscii("_FALSE123"));
        assertEquals("_false123", UnicodeTranslator.toAscii("_false123"));
	}

	@Test
	public void UnderscoreINTEGERDigit() {
        assertEquals("_INTEGER123", UnicodeTranslator.toAscii("_INTEGER123"));
        assertEquals("_integer123", UnicodeTranslator.toAscii("_integer123"));
	}

	@Test
	public void UnderscoreINTERDigit() {
        assertEquals("_INTER123", UnicodeTranslator.toAscii("_INTER123"));
        assertEquals("_inter123", UnicodeTranslator.toAscii("_inter123"));
	}

	@Test
	public void UnderscoreNATDigit() {
        assertEquals("_NAT123", UnicodeTranslator.toAscii("_NAT123"));
        assertEquals("_nat123", UnicodeTranslator.toAscii("_nat123"));
	}

	@Test
	public void UnderscoreNAT1Digit() {
        assertEquals("_NAT1123", UnicodeTranslator.toAscii("_NAT1123"));
        assertEquals("_nat1123", UnicodeTranslator.toAscii("_nat1123"));
	}

	@Test
	public void UnderscoreNATURALDigit() {
        assertEquals("_NATURAL123", UnicodeTranslator.toAscii("_NATURAL123"));
        assertEquals("_natural123", UnicodeTranslator.toAscii("_natural123"));
	}

	@Test
	public void UnderscoreNOTDigit() {
        assertEquals("_NOT123", UnicodeTranslator.toAscii("_NOT123"));
        assertEquals("_not123", UnicodeTranslator.toAscii("_not123"));
	}

	@Test
	public void UnderscoreORDigit() {
        assertEquals("_OR123", UnicodeTranslator.toAscii("_OR123"));
        assertEquals("_or123", UnicodeTranslator.toAscii("_or123"));
	}

	@Test
	public void UnderscorePOWDigit() {
        assertEquals("_POW123", UnicodeTranslator.toAscii("_POW123"));
        assertEquals("_pow123", UnicodeTranslator.toAscii("_pow123"));
	}

	@Test
	public void UnderscorePOW1Digit() {
        assertEquals("_POW1123", UnicodeTranslator.toAscii("_POW1123"));
        assertEquals("_pow1123", UnicodeTranslator.toAscii("_pow1123"));
	}

	@Test
	public void UnderscoreTRUEDigit() {
        assertEquals("_TRUE123", UnicodeTranslator.toAscii("_TRUE123"));
        assertEquals("_true123", UnicodeTranslator.toAscii("_true123"));
	}

	@Test
	public void UnderscoreUNIONDigit() {
        assertEquals("_UNION123", UnicodeTranslator.toAscii("_UNION123"));
        assertEquals("_union123", UnicodeTranslator.toAscii("_union123"));
	}

	@Test
	public void UnderscoreLetterDigitANY() {
        assertEquals("_abc123ANY", UnicodeTranslator.toAscii("_abc123ANY"));
        assertEquals("_abc123any", UnicodeTranslator.toAscii("_abc123any"));
	}

	@Test
	public void UnderscoreLetterDigitFALSE() {
        assertEquals("_abc123FALSE", UnicodeTranslator.toAscii("_abc123FALSE"));
        assertEquals("_abc123false", UnicodeTranslator.toAscii("_abc123false"));
	}

	@Test
	public void UnderscoreLetterDigitINTEGER() {
        assertEquals("_abc123INTEGER", UnicodeTranslator.toAscii("_abc123INTEGER"));
        assertEquals("_abc123integer", UnicodeTranslator.toAscii("_abc123integer"));
	}

	@Test
	public void UnderscoreLetterDigitINTER() {
        assertEquals("_abc123INTER", UnicodeTranslator.toAscii("_abc123INTER"));
        assertEquals("_abc123inter", UnicodeTranslator.toAscii("_abc123inter"));
	}

	@Test
	public void UnderscoreLetterDigitNAT() {
        assertEquals("_abc123NAT", UnicodeTranslator.toAscii("_abc123NAT"));
        assertEquals("_abc123nat", UnicodeTranslator.toAscii("_abc123nat"));
	}

	@Test
	public void UnderscoreLetterDigitNAT1() {
        assertEquals("_abc123NAT1", UnicodeTranslator.toAscii("_abc123NAT1"));
        assertEquals("_abc123nat1", UnicodeTranslator.toAscii("_abc123nat1"));
	}

	@Test
	public void UnderscoreLetterDigitNATURAL() {
        assertEquals("_abc123NATURAL", UnicodeTranslator.toAscii("_abc123NATURAL"));
        assertEquals("_abc123natural", UnicodeTranslator.toAscii("_abc123natural"));
	}

	@Test
	public void UnderscoreLetterDigitNOT() {
        assertEquals("_abc123NOT", UnicodeTranslator.toAscii("_abc123NOT"));
        assertEquals("_abc123not", UnicodeTranslator.toAscii("_abc123not"));
	}

	@Test
	public void UnderscoreLetterDigitOR() {
        assertEquals("_abc123OR", UnicodeTranslator.toAscii("_abc123OR"));
        assertEquals("_abc123or", UnicodeTranslator.toAscii("_abc123or"));
	}

	@Test
	public void UnderscoreLetterDigitPOW() {
        assertEquals("_abc123POW", UnicodeTranslator.toAscii("_abc123POW"));
        assertEquals("_abc123pow", UnicodeTranslator.toAscii("_abc123pow"));
	}

	@Test
	public void UnderscoreLetterDigitPOW1() {
        assertEquals("_abc123POW1", UnicodeTranslator.toAscii("_abc123POW1"));
        assertEquals("_abc123pow1", UnicodeTranslator.toAscii("_abc123pow1"));
	}

	@Test
	public void UnderscoreLetterDigitTRUE() {
        assertEquals("_abc123TRUE", UnicodeTranslator.toAscii("_abc123TRUE"));
        assertEquals("_abc123true", UnicodeTranslator.toAscii("_abc123true"));
	}

	@Test
	public void UnderscoreLetterDigitUNION() {
        assertEquals("_abc123UNION", UnicodeTranslator.toAscii("_abc123UNION"));
        assertEquals("_abc123union", UnicodeTranslator.toAscii("_abc123union"));
	}

	@Test
	public void UnderscoreLetterANYDigit() {
        assertEquals("_abcANY123", UnicodeTranslator.toAscii("_abcANY123"));
        assertEquals("_abcany123", UnicodeTranslator.toAscii("_abcany123"));
	}

	@Test
	public void UnderscoreLetterFALSEDigit() {
        assertEquals("_abcFALSE123", UnicodeTranslator.toAscii("_abcFALSE123"));
        assertEquals("_abcfalse123", UnicodeTranslator.toAscii("_abcfalse123"));
	}

	@Test
	public void UnderscoreLetterINTEGERDigit() {
        assertEquals("_abcINTEGER123", UnicodeTranslator.toAscii("_abcINTEGER123"));
        assertEquals("_abcinteger123", UnicodeTranslator.toAscii("_abcinteger123"));
	}

	@Test
	public void UnderscoreLetterINTERDigit() {
        assertEquals("_abcINTER123", UnicodeTranslator.toAscii("_abcINTER123"));
        assertEquals("_abcinter123", UnicodeTranslator.toAscii("_abcinter123"));
	}

	@Test
	public void UnderscoreLetterNATDigit() {
        assertEquals("_abcNAT123", UnicodeTranslator.toAscii("_abcNAT123"));
        assertEquals("_abcnat123", UnicodeTranslator.toAscii("_abcnat123"));
	}

	@Test
	public void UnderscoreLetterNAT1Digit() {
        assertEquals("_abcNAT1123", UnicodeTranslator.toAscii("_abcNAT1123"));
        assertEquals("_abcnat1123", UnicodeTranslator.toAscii("_abcnat1123"));
	}

	@Test
	public void UnderscoreLetterNATURALDigit() {
        assertEquals("_abcNATURAL123", UnicodeTranslator.toAscii("_abcNATURAL123"));
        assertEquals("_abcnatural123", UnicodeTranslator.toAscii("_abcnatural123"));
	}

	@Test
	public void UnderscoreLetterNOTDigit() {
        assertEquals("_abcNOT123", UnicodeTranslator.toAscii("_abcNOT123"));
        assertEquals("_abcnot123", UnicodeTranslator.toAscii("_abcnot123"));
	}

	@Test
	public void UnderscoreLetterORDigit() {
        assertEquals("_abcOR123", UnicodeTranslator.toAscii("_abcOR123"));
        assertEquals("_abcor123", UnicodeTranslator.toAscii("_abcor123"));
	}

	@Test
	public void UnderscoreLetterPOWDigit() {
        assertEquals("_abcPOW123", UnicodeTranslator.toAscii("_abcPOW123"));
        assertEquals("_abcpow123", UnicodeTranslator.toAscii("_abcpow123"));
	}

	@Test
	public void UnderscoreLetterPOW1Digit() {
        assertEquals("_abcPOW1123", UnicodeTranslator.toAscii("_abcPOW1123"));
        assertEquals("_abcpow1123", UnicodeTranslator.toAscii("_abcpow1123"));
	}

	@Test
	public void UnderscoreLetterTRUEDigit() {
        assertEquals("_abcTRUE123", UnicodeTranslator.toAscii("_abcTRUE123"));
        assertEquals("_abctrue123", UnicodeTranslator.toAscii("_abctrue123"));
	}

	@Test
	public void UnderscoreLetterUNIONDigit() {
        assertEquals("_abcUNION123", UnicodeTranslator.toAscii("_abcUNION123"));
        assertEquals("_abcunion123", UnicodeTranslator.toAscii("_abcunion123"));
	}

	@Test
	public void UnderscoreDigitLetterANY() {
        assertEquals("_123abcANY", UnicodeTranslator.toAscii("_123abcANY"));
        assertEquals("_123abcany", UnicodeTranslator.toAscii("_123abcany"));
	}

	@Test
	public void UnderscoreDigitLetterFALSE() {
        assertEquals("_123abcFALSE", UnicodeTranslator.toAscii("_123abcFALSE"));
        assertEquals("_123abcfalse", UnicodeTranslator.toAscii("_123abcfalse"));
	}

	@Test
	public void UnderscoreDigitLetterINTEGER() {
        assertEquals("_123abcINTEGER", UnicodeTranslator.toAscii("_123abcINTEGER"));
        assertEquals("_123abcinteger", UnicodeTranslator.toAscii("_123abcinteger"));
	}

	@Test
	public void UnderscoreDigitLetterINTER() {
        assertEquals("_123abcINTER", UnicodeTranslator.toAscii("_123abcINTER"));
        assertEquals("_123abcinter", UnicodeTranslator.toAscii("_123abcinter"));
	}

	@Test
	public void UnderscoreDigitLetterNAT() {
        assertEquals("_123abcNAT", UnicodeTranslator.toAscii("_123abcNAT"));
        assertEquals("_123abcnat", UnicodeTranslator.toAscii("_123abcnat"));
	}

	@Test
	public void UnderscoreDigitLetterNAT1() {
        assertEquals("_123abcNAT1", UnicodeTranslator.toAscii("_123abcNAT1"));
        assertEquals("_123abcnat1", UnicodeTranslator.toAscii("_123abcnat1"));
	}

	@Test
	public void UnderscoreDigitLetterNATURAL() {
        assertEquals("_123abcNATURAL", UnicodeTranslator.toAscii("_123abcNATURAL"));
        assertEquals("_123abcnatural", UnicodeTranslator.toAscii("_123abcnatural"));
	}

	@Test
	public void UnderscoreDigitLetterNOT() {
        assertEquals("_123abcNOT", UnicodeTranslator.toAscii("_123abcNOT"));
        assertEquals("_123abcnot", UnicodeTranslator.toAscii("_123abcnot"));
	}

	@Test
	public void UnderscoreDigitLetterOR() {
        assertEquals("_123abcOR", UnicodeTranslator.toAscii("_123abcOR"));
        assertEquals("_123abcor", UnicodeTranslator.toAscii("_123abcor"));
	}

	@Test
	public void UnderscoreDigitLetterPOW() {
        assertEquals("_123abcPOW", UnicodeTranslator.toAscii("_123abcPOW"));
        assertEquals("_123abcpow", UnicodeTranslator.toAscii("_123abcpow"));
	}

	@Test
	public void UnderscoreDigitLetterPOW1() {
        assertEquals("_123abcPOW1", UnicodeTranslator.toAscii("_123abcPOW1"));
        assertEquals("_123abcpow1", UnicodeTranslator.toAscii("_123abcpow1"));
	}

	@Test
	public void UnderscoreDigitLetterTRUE() {
        assertEquals("_123abcTRUE", UnicodeTranslator.toAscii("_123abcTRUE"));
        assertEquals("_123abctrue", UnicodeTranslator.toAscii("_123abctrue"));
	}

	@Test
	public void UnderscoreDigitLetterUNION() {
        assertEquals("_123abcUNION", UnicodeTranslator.toAscii("_123abcUNION"));
        assertEquals("_123abcunion", UnicodeTranslator.toAscii("_123abcunion"));
	}

	@Test
	public void UnderscoreDigitANYLetter() {
        assertEquals("_123ANYabc", UnicodeTranslator.toAscii("_123ANYabc"));
        assertEquals("_123anyabc", UnicodeTranslator.toAscii("_123anyabc"));
	}

	@Test
	public void UnderscoreDigitFALSELetter() {
        assertEquals("_123FALSEabc", UnicodeTranslator.toAscii("_123FALSEabc"));
        assertEquals("_123falseabc", UnicodeTranslator.toAscii("_123falseabc"));
	}

	@Test
	public void UnderscoreDigitINTEGERLetter() {
        assertEquals("_123INTEGERabc", UnicodeTranslator.toAscii("_123INTEGERabc"));
        assertEquals("_123integerabc", UnicodeTranslator.toAscii("_123integerabc"));
	}

	@Test
	public void UnderscoreDigitINTERLetter() {
        assertEquals("_123INTERabc", UnicodeTranslator.toAscii("_123INTERabc"));
        assertEquals("_123interabc", UnicodeTranslator.toAscii("_123interabc"));
	}

	@Test
	public void UnderscoreDigitNATLetter() {
        assertEquals("_123NATabc", UnicodeTranslator.toAscii("_123NATabc"));
        assertEquals("_123natabc", UnicodeTranslator.toAscii("_123natabc"));
	}

	@Test
	public void UnderscoreDigitNAT1Letter() {
        assertEquals("_123NAT1abc", UnicodeTranslator.toAscii("_123NAT1abc"));
        assertEquals("_123nat1abc", UnicodeTranslator.toAscii("_123nat1abc"));
	}

	@Test
	public void UnderscoreDigitNATURALLetter() {
        assertEquals("_123NATURALabc", UnicodeTranslator.toAscii("_123NATURALabc"));
        assertEquals("_123naturalabc", UnicodeTranslator.toAscii("_123naturalabc"));
	}

	@Test
	public void UnderscoreDigitNOTLetter() {
        assertEquals("_123NOTabc", UnicodeTranslator.toAscii("_123NOTabc"));
        assertEquals("_123notabc", UnicodeTranslator.toAscii("_123notabc"));
	}

	@Test
	public void UnderscoreDigitORLetter() {
        assertEquals("_123orabc", UnicodeTranslator.toAscii("_123orabc"));
        assertEquals("_123ORabc", UnicodeTranslator.toAscii("_123ORabc"));
	}

	@Test
	public void UnderscoreDigitPOWLetter() {
        assertEquals("_123POWabc", UnicodeTranslator.toAscii("_123POWabc"));
        assertEquals("_123powabc", UnicodeTranslator.toAscii("_123powabc"));
	}

	@Test
	public void UnderscoreDigitPOW1Letter() {
        assertEquals("_123POW1abc", UnicodeTranslator.toAscii("_123POW1abc"));
        assertEquals("_123pow1abc", UnicodeTranslator.toAscii("_123pow1abc"));
	}

	@Test
	public void UnderscoreDigitTRUELetter() {
        assertEquals("_123TRUEabc", UnicodeTranslator.toAscii("_123TRUEabc"));
        assertEquals("_123trueabc", UnicodeTranslator.toAscii("_123trueabc"));
	}

	@Test
	public void UnderscoreDigitUNIONLetter() {
        assertEquals("_123UNIONabc", UnicodeTranslator.toAscii("_123UNIONabc"));
        assertEquals("_123unionabc", UnicodeTranslator.toAscii("_123unionabc"));
	}

	@Test
	public void UnderscoreANYLetterDigit() {
        assertEquals("_ANYabc123", UnicodeTranslator.toAscii("_ANYabc123"));
        assertEquals("_anyabc123", UnicodeTranslator.toAscii("_anyabc123"));
	}

	@Test
	public void UnderscoreFALSELetterDigit() {
        assertEquals("_FALSEabc123", UnicodeTranslator.toAscii("_FALSEabc123"));
        assertEquals("_falseabc123", UnicodeTranslator.toAscii("_falseabc123"));
	}

	@Test
	public void UnderscoreINTEGERLetterDigit() {
        assertEquals("_INTEGERabc123", UnicodeTranslator.toAscii("_INTEGERabc123"));
        assertEquals("_integerabc123", UnicodeTranslator.toAscii("_integerabc123"));
	}

	@Test
	public void UnderscoreINTERLetterDigit() {
        assertEquals("_INTERabc123", UnicodeTranslator.toAscii("_INTERabc123"));
        assertEquals("_interabc123", UnicodeTranslator.toAscii("_interabc123"));
	}

	@Test
	public void UnderscoreNATLetterDigit() {
        assertEquals("_NATabc123", UnicodeTranslator.toAscii("_NATabc123"));
        assertEquals("_natabc123", UnicodeTranslator.toAscii("_natabc123"));
	}

	@Test
	public void UnderscoreNAT1LetterDigit() {
        assertEquals("_NAT1abc123", UnicodeTranslator.toAscii("_NAT1abc123"));
        assertEquals("_nat1abc123", UnicodeTranslator.toAscii("_nat1abc123"));
	}

	@Test
	public void UnderscoreNATURALLetterDigit() {
        assertEquals("_NATURALabc123", UnicodeTranslator.toAscii("_NATURALabc123"));
        assertEquals("_naturalabc123", UnicodeTranslator.toAscii("_naturalabc123"));
	}

	@Test
	public void UnderscoreNOTLetterDigit() {
        assertEquals("_NOTabc123", UnicodeTranslator.toAscii("_NOTabc123"));
        assertEquals("_notabc123", UnicodeTranslator.toAscii("_notabc123"));
	}

	@Test
	public void UnderscoreORLetterDigit() {
        assertEquals("_ORabc123", UnicodeTranslator.toAscii("_ORabc123"));
        assertEquals("_orabc123", UnicodeTranslator.toAscii("_orabc123"));
	}

	@Test
	public void UnderscorePOWLetterDigit() {
        assertEquals("_POWabc123", UnicodeTranslator.toAscii("_POWabc123"));
        assertEquals("_powabc123", UnicodeTranslator.toAscii("_powabc123"));
	}

	@Test
	public void UnderscorePOW1LetterDigit() {
        assertEquals("_POW1abc123", UnicodeTranslator.toAscii("_POW1abc123"));
        assertEquals("_pow1abc123", UnicodeTranslator.toAscii("_pow1abc123"));
	}

	@Test
	public void UnderscoreTRUELetterDigit() {
        assertEquals("_TRUEabc123", UnicodeTranslator.toAscii("_TRUEabc123"));
        assertEquals("_trueabc123", UnicodeTranslator.toAscii("_trueabc123"));
	}

	@Test
	public void UnderscoreUNIONLetterDigit() {
        assertEquals("_UNIONabc123", UnicodeTranslator.toAscii("_UNIONabc123"));
        assertEquals("_unionabc123", UnicodeTranslator.toAscii("_unionabc123"));
	}

	@Test
	public void UnderscoreANYDigitLetter() {
        assertEquals("_ANY123abc", UnicodeTranslator.toAscii("_ANY123abc"));
        assertEquals("_any123abc", UnicodeTranslator.toAscii("_any123abc"));
	}

	@Test
	public void UnderscoreFALSEDigitLetter() {
        assertEquals("_FALSE123abc", UnicodeTranslator.toAscii("_FALSE123abc"));
        assertEquals("_false123abc", UnicodeTranslator.toAscii("_false123abc"));
	}

	@Test
	public void UnderscoreINTEGERDigitLetter() {
        assertEquals("_INTEGER123abc", UnicodeTranslator.toAscii("_INTEGER123abc"));
        assertEquals("_integer123abc", UnicodeTranslator.toAscii("_integer123abc"));
	}

	@Test
	public void UnderscoreINTERDigitLetter() {
        assertEquals("_INTER123abc", UnicodeTranslator.toAscii("_INTER123abc"));
        assertEquals("_inter123abc", UnicodeTranslator.toAscii("_inter123abc"));
	}

	@Test
	public void UnderscoreNATDigitLetter() {
        assertEquals("_NAT123abc", UnicodeTranslator.toAscii("_NAT123abc"));
        assertEquals("_nat123abc", UnicodeTranslator.toAscii("_nat123abc"));
	}

	@Test
	public void UnderscoreNAT1DigitLetter() {
        assertEquals("_NAT1123abc", UnicodeTranslator.toAscii("_NAT1123abc"));
        assertEquals("_nat1123abc", UnicodeTranslator.toAscii("_nat1123abc"));
	}

	@Test
	public void UnderscoreNATURALDigitLetter() {
        assertEquals("_NATURAL123abc", UnicodeTranslator.toAscii("_NATURAL123abc"));
        assertEquals("_natural123abc", UnicodeTranslator.toAscii("_natural123abc"));
	}

	@Test
	public void UnderscoreNOTDigitLetter() {
        assertEquals("_NOT123abc", UnicodeTranslator.toAscii("_NOT123abc"));
        assertEquals("_not123abc", UnicodeTranslator.toAscii("_not123abc"));
	}

	@Test
	public void UnderscoreORDigitLetter() {
        assertEquals("_OR123abc", UnicodeTranslator.toAscii("_OR123abc"));
        assertEquals("_or123abc", UnicodeTranslator.toAscii("_or123abc"));
	}

	@Test
	public void UnderscorePOWDigitLetter() {
        assertEquals("_POW123abc", UnicodeTranslator.toAscii("_POW123abc"));
        assertEquals("_pow123abc", UnicodeTranslator.toAscii("_pow123abc"));
	}

	@Test
	public void UnderscorePOW1DigitLetter() {
        assertEquals("_POW1123abc", UnicodeTranslator.toAscii("_POW1123abc"));
        assertEquals("_pow1123abc", UnicodeTranslator.toAscii("_pow1123abc"));
	}

	@Test
	public void UnderscoreTRUEDigitLetter() {
        assertEquals("_TRUE123abc", UnicodeTranslator.toAscii("_TRUE123abc"));
        assertEquals("_true123abc", UnicodeTranslator.toAscii("_true123abc"));
	}

	@Test
	public void UnderscoreUNIONDigitLetter() {
        assertEquals("_UNION123abc", UnicodeTranslator.toAscii("_UNION123abc"));
        assertEquals("_union123abc", UnicodeTranslator.toAscii("_union123abc"));
	}

	// @Test
	// public void Keyword()
	// {
	// // check, if the keywords have to be translated or not
	// assertTrue(UnicodeTranslator.toAscii("ANY").equals("ANY"));
	// assertFalse(UnicodeTranslator.toAscii("ANY").equals("ANY"));
	// }

	@Test
	public void ANYLetter() {
        assertEquals("ANYabc", UnicodeTranslator.toAscii("ANYabc"));
        assertEquals("anyabc", UnicodeTranslator.toAscii("anyabc"));
	}

	@Test
	public void FALSELetter() {
        assertEquals("FALSEabc", UnicodeTranslator.toAscii("FALSEabc"));
        assertEquals("falseabc", UnicodeTranslator.toAscii("falseabc"));
	}

	@Test
	public void INTEGERLetter() {
        assertEquals("INTEGERabc", UnicodeTranslator.toAscii("INTEGERabc"));
        assertEquals("integerabc", UnicodeTranslator.toAscii("integerabc"));
	}

	@Test
	public void INTERLetter() {
        assertEquals("INTERabc", UnicodeTranslator.toAscii("INTERabc"));
        assertEquals("interabc", UnicodeTranslator.toAscii("interabc"));
	}

	@Test
	public void NATLetter() {
        assertEquals("NATabc", UnicodeTranslator.toAscii("NATabc"));
        assertEquals("natabc", UnicodeTranslator.toAscii("natabc"));
	}

	@Test
	public void NAT1Letter() {
        assertEquals("NAT1abc", UnicodeTranslator.toAscii("NAT1abc"));
        assertEquals("nat1abc", UnicodeTranslator.toAscii("nat1abc"));
	}

	@Test
	public void NATURALLetter() {
        assertEquals("NATURALabc", UnicodeTranslator.toAscii("NATURALabc"));
        assertEquals("naturalabc", UnicodeTranslator.toAscii("naturalabc"));
	}

	@Test
	public void NOTLetter() {
        assertEquals("NOTabc", UnicodeTranslator.toAscii("NOTabc"));
        assertEquals("notabc", UnicodeTranslator.toAscii("notabc"));
	}

	@Test
	public void ORLetter() {
        assertEquals("ORabc", UnicodeTranslator.toAscii("ORabc"));
        assertEquals("orabc", UnicodeTranslator.toAscii("orabc"));
	}

	@Test
	public void POWLetter() {
        assertEquals("POWabc", UnicodeTranslator.toAscii("POWabc"));
        assertEquals("powabc", UnicodeTranslator.toAscii("powabc"));
	}

	@Test
	public void POW1Letter() {
        assertEquals("POW1abc", UnicodeTranslator.toAscii("POW1abc"));
        assertEquals("pow1abc", UnicodeTranslator.toAscii("pow1abc"));
	}

	@Test
	public void TRUELetter() {
        assertEquals("TRUEabc", UnicodeTranslator.toAscii("TRUEabc"));
        assertEquals("trueabc", UnicodeTranslator.toAscii("trueabc"));
	}

	@Test
	public void UNIONLetter() {
        assertEquals("UNIONabc", UnicodeTranslator.toAscii("UNIONabc"));
        assertEquals("unionabc", UnicodeTranslator.toAscii("unionabc"));
	}

	@Test
	public void ANYDigit() {
        assertEquals("ANY123", UnicodeTranslator.toAscii("ANY123"));
        assertEquals("any123", UnicodeTranslator.toAscii("any123"));
	}

	@Test
	public void FALSEDigit() {
        assertEquals("FALSE123", UnicodeTranslator.toAscii("FALSE123"));
        assertEquals("false123", UnicodeTranslator.toAscii("false123"));
	}

	@Test
	public void INTEGERDigit() {
        assertEquals("INTEGER123", UnicodeTranslator.toAscii("INTEGER123"));
        assertEquals("integer123", UnicodeTranslator.toAscii("integer123"));
	}

	@Test
	public void INTERDigit() {
        assertEquals("INTER123", UnicodeTranslator.toAscii("INTER123"));
        assertEquals("inter123", UnicodeTranslator.toAscii("inter123"));
	}

	@Test
	public void NATDigit() {
        assertEquals("NAT123", UnicodeTranslator.toAscii("NAT123"));
        assertEquals("nat123", UnicodeTranslator.toAscii("nat123"));
	}

	@Test
	public void NAT1Digit() {
        assertEquals("NAT1123", UnicodeTranslator.toAscii("NAT1123"));
        assertEquals("nat1123", UnicodeTranslator.toAscii("nat1123"));
	}

	@Test
	public void NATURALDigit() {
        assertEquals("NATURAL123", UnicodeTranslator.toAscii("NATURAL123"));
        assertEquals("natural123", UnicodeTranslator.toAscii("natural123"));
	}

	@Test
	public void NOTDigit() {
        assertEquals("not123", UnicodeTranslator.toAscii("not123"));
        assertEquals("NOT123", UnicodeTranslator.toAscii("NOT123"));
	}

	@Test
	public void ORDigit() {
        assertEquals("or123", UnicodeTranslator.toAscii("or123"));
        assertEquals("OR123", UnicodeTranslator.toAscii("OR123"));
	}

	@Test
	public void POWDigit() {
        assertEquals("POW123", UnicodeTranslator.toAscii("POW123"));
        assertEquals("pow123", UnicodeTranslator.toAscii("pow123"));
	}

	@Test
	public void POW1Digit() {
        assertEquals("POW1123", UnicodeTranslator.toAscii("POW1123"));
        assertEquals("pow1123", UnicodeTranslator.toAscii("pow1123"));
	}

	@Test
	public void TRUEDigit() {
        assertEquals("TRUE123", UnicodeTranslator.toAscii("TRUE123"));
        assertEquals("true123", UnicodeTranslator.toAscii("true123"));
	}

	@Test
	public void UNIONDigit() {
        assertEquals("UNION123", UnicodeTranslator.toAscii("UNION123"));
        assertEquals("union123", UnicodeTranslator.toAscii("union123"));
	}

	@Test
	public void ANYUnderscore() {
        assertEquals("ANY_", UnicodeTranslator.toAscii("ANY_"));
        assertEquals("any_", UnicodeTranslator.toAscii("any_"));
	}

	@Test
	public void FALSEUnderscore() {
        assertEquals("FALSE_", UnicodeTranslator.toAscii("FALSE_"));
        assertEquals("false_", UnicodeTranslator.toAscii("false_"));
	}

	@Test
	public void INTEGERUnderscore() {
        assertEquals("INTEGER_", UnicodeTranslator.toAscii("INTEGER_"));
        assertEquals("integer_", UnicodeTranslator.toAscii("integer_"));
	}

	@Test
	public void INTERUnderscore() {
        assertEquals("INTER_", UnicodeTranslator.toAscii("INTER_"));
        assertEquals("inter_", UnicodeTranslator.toAscii("inter_"));
	}

	@Test
	public void NATUnderscore() {
        assertEquals("NAT_", UnicodeTranslator.toAscii("NAT_"));
        assertEquals("nat_", UnicodeTranslator.toAscii("nat_"));
	}

	@Test
	public void NAT1Underscore() {
        assertEquals("NAT1_", UnicodeTranslator.toAscii("NAT1_"));
        assertEquals("nat1_", UnicodeTranslator.toAscii("nat1_"));
	}

	@Test
	public void NATURALUnderscore() {
        assertEquals("NATURAL_", UnicodeTranslator.toAscii("NATURAL_"));
        assertEquals("natural_", UnicodeTranslator.toAscii("natural_"));
	}

	@Test
	public void NOTUnderscore() {
        assertEquals("NOT_", UnicodeTranslator.toAscii("NOT_"));
        assertEquals("not_", UnicodeTranslator.toAscii("not_"));
	}

	@Test
	public void ORUnderscore() {
        assertEquals("OR_", UnicodeTranslator.toAscii("OR_"));
        assertEquals("or_", UnicodeTranslator.toAscii("or_"));
	}

	@Test
	public void POWUnderscore() {
        assertEquals("POW_", UnicodeTranslator.toAscii("POW_"));
        assertEquals("pow_", UnicodeTranslator.toAscii("pow_"));
	}

	@Test
	public void POW1Underscore() {
        assertEquals("POW1_", UnicodeTranslator.toAscii("POW1_"));
        assertEquals("pow1_", UnicodeTranslator.toAscii("pow1_"));
	}

	@Test
	public void TRUEUnderscore() {
        assertEquals("TRUE_", UnicodeTranslator.toAscii("TRUE_"));
        assertEquals("true_", UnicodeTranslator.toAscii("true_"));
	}

	@Test
	public void UNIONUnderscore() {
        assertEquals("UNION_", UnicodeTranslator.toAscii("UNION_"));
        assertEquals("union_", UnicodeTranslator.toAscii("union_"));
	}

	@Test
	public void ANYLetterDigit() {
        assertEquals("ANYabc123", UnicodeTranslator.toAscii("ANYabc123"));
        assertEquals("anyabc123", UnicodeTranslator.toAscii("anyabc123"));
	}

	@Test
	public void FALSELetterDigit() {
        assertEquals("FALSEabc123", UnicodeTranslator.toAscii("FALSEabc123"));
        assertEquals("falseabc123", UnicodeTranslator.toAscii("falseabc123"));
	}

	@Test
	public void INTEGERLetterDigit() {
        assertEquals("INTEGERabc123", UnicodeTranslator.toAscii("INTEGERabc123"));
        assertEquals("integerabc123", UnicodeTranslator.toAscii("integerabc123"));
	}

	@Test
	public void INTERLetterDigit() {
        assertEquals("INTERabc123", UnicodeTranslator.toAscii("INTERabc123"));
        assertEquals("interabc123", UnicodeTranslator.toAscii("interabc123"));
	}

	@Test
	public void NATLetterDigit() {
        assertEquals("NATabc123", UnicodeTranslator.toAscii("NATabc123"));
        assertEquals("natabc123", UnicodeTranslator.toAscii("natabc123"));
	}

	@Test
	public void NAT1LetterDigit() {
        assertEquals("NAT1abc123", UnicodeTranslator.toAscii("NAT1abc123"));
        assertEquals("nat1abc123", UnicodeTranslator.toAscii("nat1abc123"));
	}

	@Test
	public void NATURALLetterDigit() {
        assertEquals("NATURALabc123", UnicodeTranslator.toAscii("NATURALabc123"));
        assertEquals("naturalabc123", UnicodeTranslator.toAscii("naturalabc123"));
	}

	@Test
	public void NOTLetterDigit() {
        assertEquals("NOTabc123", UnicodeTranslator.toAscii("NOTabc123"));
        assertEquals("notabc123", UnicodeTranslator.toAscii("notabc123"));
	}

	@Test
	public void ORLetterDigit() {
        assertEquals("ORabc123", UnicodeTranslator.toAscii("ORabc123"));
        assertEquals("orabc123", UnicodeTranslator.toAscii("orabc123"));
	}

	@Test
	public void POWLetterDigit() {
        assertEquals("POWabc123", UnicodeTranslator.toAscii("POWabc123"));
        assertEquals("powabc123", UnicodeTranslator.toAscii("powabc123"));
	}

	@Test
	public void POW1LetterDigit() {
        assertEquals("POW1abc123", UnicodeTranslator.toAscii("POW1abc123"));
        assertEquals("pow1abc123", UnicodeTranslator.toAscii("pow1abc123"));
	}

	@Test
	public void TRUELetterDigit() {
        assertEquals("TRUEabc123", UnicodeTranslator.toAscii("TRUEabc123"));
        assertEquals("trueabc123", UnicodeTranslator.toAscii("trueabc123"));
	}

	@Test
	public void UNIONLetterDigit() {
        assertEquals("UNIONabc123", UnicodeTranslator.toAscii("UNIONabc123"));
        assertEquals("unionabc123", UnicodeTranslator.toAscii("unionabc123"));
	}

	@Test
	public void ANYLetterUnderscore() {
        assertEquals("ANYabc_", UnicodeTranslator.toAscii("ANYabc_"));
        assertEquals("anyabc_", UnicodeTranslator.toAscii("anyabc_"));
	}

	@Test
	public void FALSELetterUnderscore() {
        assertEquals("FALSEabc_", UnicodeTranslator.toAscii("FALSEabc_"));
        assertEquals("falseabc_", UnicodeTranslator.toAscii("falseabc_"));
	}

	@Test
	public void INTEGERLetterUnderscore() {
        assertEquals("INTEGERabc_", UnicodeTranslator.toAscii("INTEGERabc_"));
        assertEquals("integerabc_", UnicodeTranslator.toAscii("integerabc_"));
	}

	@Test
	public void INTERLetterUnderscore() {
        assertEquals("INTERabc_", UnicodeTranslator.toAscii("INTERabc_"));
        assertEquals("interabc_", UnicodeTranslator.toAscii("interabc_"));
	}

	@Test
	public void NATLetterUnderscore() {
        assertEquals("NATabc_", UnicodeTranslator.toAscii("NATabc_"));
        assertEquals("natabc_", UnicodeTranslator.toAscii("natabc_"));
	}

	@Test
	public void NAT1LetterUnderscore() {
        assertEquals("NAT1abc_", UnicodeTranslator.toAscii("NAT1abc_"));
        assertEquals("nat1abc_", UnicodeTranslator.toAscii("nat1abc_"));
	}

	@Test
	public void NATURALLetterUnderscore() {
        assertEquals("NATURALabc_", UnicodeTranslator.toAscii("NATURALabc_"));
        assertEquals("naturalabc_", UnicodeTranslator.toAscii("naturalabc_"));
	}

	@Test
	public void NOTLetterUnderscore() {
        assertEquals("NOTabc_", UnicodeTranslator.toAscii("NOTabc_"));
        assertEquals("notabc_", UnicodeTranslator.toAscii("notabc_"));
	}

	@Test
	public void ORLetterUnderscore() {
        assertEquals("ORabc_", UnicodeTranslator.toAscii("ORabc_"));
        assertEquals("orabc_", UnicodeTranslator.toAscii("orabc_"));
	}

	@Test
	public void POWLetterUnderscore() {
        assertEquals("POWabc_", UnicodeTranslator.toAscii("POWabc_"));
        assertEquals("powabc_", UnicodeTranslator.toAscii("powabc_"));
	}

	@Test
	public void POW1LetterUnderscore() {
        assertEquals("POW1abc_", UnicodeTranslator.toAscii("POW1abc_"));
        assertEquals("pow1abc_", UnicodeTranslator.toAscii("pow1abc_"));
	}

	@Test
	public void TRUELetterUnderscore() {
        assertEquals("TRUEabc_", UnicodeTranslator.toAscii("TRUEabc_"));
        assertEquals("trueabc_", UnicodeTranslator.toAscii("trueabc_"));
	}

	@Test
	public void UNIONLetterUnderscore() {
        assertEquals("UNIONabc_", UnicodeTranslator.toAscii("UNIONabc_"));
        assertEquals("unionabc_", UnicodeTranslator.toAscii("unionabc_"));
	}

	@Test
	public void ANYDigitLetter() {
        assertEquals("ANY123abc", UnicodeTranslator.toAscii("ANY123abc"));
        assertEquals("any123abc", UnicodeTranslator.toAscii("any123abc"));
	}

	@Test
	public void FALSEDigitLetter() {
        assertEquals("FALSE123abc", UnicodeTranslator.toAscii("FALSE123abc"));
        assertEquals("false123abc", UnicodeTranslator.toAscii("false123abc"));
	}

	@Test
	public void INTEGERDigitLetter() {
        assertEquals("INTEGER123abc", UnicodeTranslator.toAscii("INTEGER123abc"));
        assertEquals("integer123abc", UnicodeTranslator.toAscii("integer123abc"));
	}

	@Test
	public void INTERDigitLetter() {
        assertEquals("INTER123abc", UnicodeTranslator.toAscii("INTER123abc"));
        assertEquals("inter123abc", UnicodeTranslator.toAscii("inter123abc"));
	}

	@Test
	public void NATDigitLetter() {
        assertEquals("NAT123abc", UnicodeTranslator.toAscii("NAT123abc"));
        assertEquals("nat123abc", UnicodeTranslator.toAscii("nat123abc"));
	}

	@Test
	public void NAT1DigitLetter() {
        assertEquals("NAT1123abc", UnicodeTranslator.toAscii("NAT1123abc"));
        assertEquals("nat1123abc", UnicodeTranslator.toAscii("nat1123abc"));
	}

	@Test
	public void NATURALDigitLetter() {
        assertEquals("NATURAL123abc", UnicodeTranslator.toAscii("NATURAL123abc"));
        assertEquals("natural123abc", UnicodeTranslator.toAscii("natural123abc"));
	}

	@Test
	public void NOTDigitLetter() {
        assertEquals("NOT123abc", UnicodeTranslator.toAscii("NOT123abc"));
        assertEquals("not123abc", UnicodeTranslator.toAscii("not123abc"));
	}

	@Test
	public void ORDigitLetter() {
        assertEquals("OR123abc", UnicodeTranslator.toAscii("OR123abc"));
        assertEquals("or123abc", UnicodeTranslator.toAscii("or123abc"));
	}

	@Test
	public void POWDigitLetter() {
        assertEquals("POW123abc", UnicodeTranslator.toAscii("POW123abc"));
        assertEquals("pow123abc", UnicodeTranslator.toAscii("pow123abc"));
	}

	@Test
	public void POW1DigitLetter() {
        assertEquals("POW1123abc", UnicodeTranslator.toAscii("POW1123abc"));
        assertEquals("pow1123abc", UnicodeTranslator.toAscii("pow1123abc"));
	}

	@Test
	public void TRUEDigitLetter() {
        assertEquals("TRUE123abc", UnicodeTranslator.toAscii("TRUE123abc"));
        assertEquals("true123abc", UnicodeTranslator.toAscii("true123abc"));
	}

	@Test
	public void UNIONDigitLetter() {
        assertEquals("UNION123abc", UnicodeTranslator.toAscii("UNION123abc"));
        assertEquals("union123abc", UnicodeTranslator.toAscii("union123abc"));
	}

	@Test
	public void ANYDigitUnderscore() {
        assertEquals("ANY123_", UnicodeTranslator.toAscii("ANY123_"));
        assertEquals("any123_", UnicodeTranslator.toAscii("any123_"));
	}

	@Test
	public void FALSEDigitUnderscore() {
        assertEquals("FALSE123_", UnicodeTranslator.toAscii("FALSE123_"));
        assertEquals("false123_", UnicodeTranslator.toAscii("false123_"));
	}

	@Test
	public void INTEGERDigitUnderscore() {
        assertEquals("INTEGER123_", UnicodeTranslator.toAscii("INTEGER123_"));
        assertEquals("integer123_", UnicodeTranslator.toAscii("integer123_"));
	}

	@Test
	public void INTERDigitUnderscore() {
        assertEquals("INTER123_", UnicodeTranslator.toAscii("INTER123_"));
        assertEquals("inter123_", UnicodeTranslator.toAscii("inter123_"));
	}

	@Test
	public void NATDigitUnderscore() {
        assertEquals("NAT123_", UnicodeTranslator.toAscii("NAT123_"));
        assertEquals("nat123_", UnicodeTranslator.toAscii("nat123_"));
	}

	@Test
	public void NAT1DigitUnderscore() {
        assertEquals("NAT1123_", UnicodeTranslator.toAscii("NAT1123_"));
        assertEquals("nat1123_", UnicodeTranslator.toAscii("nat1123_"));
	}

	@Test
	public void NATURALDigitUnderscore() {
        assertEquals("NATURAL123_", UnicodeTranslator.toAscii("NATURAL123_"));
        assertEquals("natural123_", UnicodeTranslator.toAscii("natural123_"));
	}

	@Test
	public void NOTDigitUnderscore() {
        assertEquals("NOT123_", UnicodeTranslator.toAscii("NOT123_"));
        assertEquals("not123_", UnicodeTranslator.toAscii("not123_"));
	}

	@Test
	public void ORDigitUnderscore() {
        assertEquals("OR123_", UnicodeTranslator.toAscii("OR123_"));
        assertEquals("or123_", UnicodeTranslator.toAscii("or123_"));
	}

	@Test
	public void POWDigitUnderscore() {
        assertEquals("POW123_", UnicodeTranslator.toAscii("POW123_"));
        assertEquals("pow123_", UnicodeTranslator.toAscii("pow123_"));
	}

	@Test
	public void POW1DigitUnderscore() {
        assertEquals("POW1123_", UnicodeTranslator.toAscii("POW1123_"));
        assertEquals("pow1123_", UnicodeTranslator.toAscii("pow1123_"));
	}

	@Test
	public void TRUEDigitUnderscore() {
        assertEquals("TRUE123_", UnicodeTranslator.toAscii("TRUE123_"));
        assertEquals("true123_", UnicodeTranslator.toAscii("true123_"));
	}

	@Test
	public void UNIONDigitUnderscore() {
        assertEquals("UNION123_", UnicodeTranslator.toAscii("UNION123_"));
        assertEquals("union123_", UnicodeTranslator.toAscii("union123_"));
	}

	@Test
	public void ANYUnderscoreLetter() {
        assertEquals("ANY_abc", UnicodeTranslator.toAscii("ANY_abc"));
        assertEquals("any_abc", UnicodeTranslator.toAscii("any_abc"));
	}

	@Test
	public void FALSEUnderscoreLetter() {
        assertEquals("FALSE_abc", UnicodeTranslator.toAscii("FALSE_abc"));
        assertEquals("false_abc", UnicodeTranslator.toAscii("false_abc"));
	}

	@Test
	public void INTEGERUnderscoreLetter() {
        assertEquals("INTEGER_abc", UnicodeTranslator.toAscii("INTEGER_abc"));
        assertEquals("integer_abc", UnicodeTranslator.toAscii("integer_abc"));
	}

	@Test
	public void INTERUnderscoreLetter() {
        assertEquals("INTER_abc", UnicodeTranslator.toAscii("INTER_abc"));
        assertEquals("inter_abc", UnicodeTranslator.toAscii("inter_abc"));
	}

	@Test
	public void NATUnderscoreLetter() {
        assertEquals("NAT_abc", UnicodeTranslator.toAscii("NAT_abc"));
        assertEquals("nat_abc", UnicodeTranslator.toAscii("nat_abc"));
	}

	@Test
	public void NAT1UnderscoreLetter() {
        assertEquals("NAT1_abc", UnicodeTranslator.toAscii("NAT1_abc"));
        assertEquals("nat1_abc", UnicodeTranslator.toAscii("nat1_abc"));
	}

	@Test
	public void NATURALUnderscoreLetter() {
        assertEquals("NATURAL_abc", UnicodeTranslator.toAscii("NATURAL_abc"));
        assertEquals("natural_abc", UnicodeTranslator.toAscii("natural_abc"));
	}

	@Test
	public void NOTUnderscoreLetter() {
        assertEquals("NOT_abc", UnicodeTranslator.toAscii("NOT_abc"));
        assertEquals("not_abc", UnicodeTranslator.toAscii("not_abc"));
	}

	@Test
	public void ORUnderscoreLetter() {
        assertEquals("OR_abc", UnicodeTranslator.toAscii("OR_abc"));
        assertEquals("or_abc", UnicodeTranslator.toAscii("or_abc"));
	}

	@Test
	public void POWUnderscoreLetter() {
        assertEquals("POW_abc", UnicodeTranslator.toAscii("POW_abc"));
        assertEquals("pow_abc", UnicodeTranslator.toAscii("pow_abc"));
	}

	@Test
	public void POW1UnderscoreLetter() {
        assertEquals("POW1_abc", UnicodeTranslator.toAscii("POW1_abc"));
        assertEquals("pow1_abc", UnicodeTranslator.toAscii("pow1_abc"));
	}

	@Test
	public void TRUEUnderscoreLetter() {
        assertEquals("TRUE_abc", UnicodeTranslator.toAscii("TRUE_abc"));
        assertEquals("true_abc", UnicodeTranslator.toAscii("true_abc"));
	}

	@Test
	public void UNIONUnderscoreLetter() {
        assertEquals("UNION_abc", UnicodeTranslator.toAscii("UNION_abc"));
        assertEquals("union_abc", UnicodeTranslator.toAscii("union_abc"));
	}

	@Test
	public void ANYUnderscoreDigit() {
        assertEquals("ANY_123", UnicodeTranslator.toAscii("ANY_123"));
        assertEquals("any_123", UnicodeTranslator.toAscii("any_123"));
	}

	@Test
	public void FALSEUnderscoreDigit() {
        assertEquals("FALSE_123", UnicodeTranslator.toAscii("FALSE_123"));
        assertEquals("false_123", UnicodeTranslator.toAscii("false_123"));
	}

	@Test
	public void INTEGERUnderscoreDigit() {
        assertEquals("INTEGER_123", UnicodeTranslator.toAscii("INTEGER_123"));
        assertEquals("integer_123", UnicodeTranslator.toAscii("integer_123"));
	}

	@Test
	public void INTERUnderscoreDigit() {
        assertEquals("INTER_123", UnicodeTranslator.toAscii("INTER_123"));
        assertEquals("inter_123", UnicodeTranslator.toAscii("inter_123"));
	}

	@Test
	public void NATUnderscoreDigit() {
        assertEquals("NAT_123", UnicodeTranslator.toAscii("NAT_123"));
        assertEquals("nat_123", UnicodeTranslator.toAscii("nat_123"));
	}

	@Test
	public void NAT1UnderscoreDigit() {
        assertEquals("NAT1_123", UnicodeTranslator.toAscii("NAT1_123"));
        assertEquals("nat1_123", UnicodeTranslator.toAscii("nat1_123"));
	}

	@Test
	public void NATURALUnderscoreDigit() {
        assertEquals("NATURAL_123", UnicodeTranslator.toAscii("NATURAL_123"));
        assertEquals("natural_123", UnicodeTranslator.toAscii("natural_123"));
	}

	@Test
	public void NOTUnderscoreDigit() {
        assertEquals("not_123", UnicodeTranslator.toAscii("not_123"));
        assertEquals("NOT_123", UnicodeTranslator.toAscii("NOT_123"));
	}

	@Test
	public void ORUnderscoreDigit() {
        assertEquals("or_123", UnicodeTranslator.toAscii("or_123"));
        assertEquals("OR_123", UnicodeTranslator.toAscii("OR_123"));
	}

	@Test
	public void POWUnderscoreDigit() {
        assertEquals("POW_123", UnicodeTranslator.toAscii("POW_123"));
        assertEquals("pow_123", UnicodeTranslator.toAscii("pow_123"));
	}

	@Test
	public void POW1UnderscoreDigit() {
        assertEquals("POW1_123", UnicodeTranslator.toAscii("POW1_123"));
        assertEquals("pow1_123", UnicodeTranslator.toAscii("pow1_123"));
	}

	@Test
	public void TRUEUnderscoreDigit() {
        assertEquals("TRUE_123", UnicodeTranslator.toAscii("TRUE_123"));
        assertEquals("true_123", UnicodeTranslator.toAscii("true_123"));
	}

	@Test
	public void UNIONUnderscoreDigit() {
        assertEquals("UNION_123", UnicodeTranslator.toAscii("UNION_123"));
        assertEquals("union_123", UnicodeTranslator.toAscii("union_123"));
	}

	@Test
	public void ANYLetterDigitUnderscore() {
        assertEquals("ANYabc123_", UnicodeTranslator.toAscii("ANYabc123_"));
        assertEquals("anyabc123_", UnicodeTranslator.toAscii("anyabc123_"));
	}

	@Test
	public void FALSELetterDigitUnderscore() {
        assertEquals("FALSEabc123_", UnicodeTranslator.toAscii("FALSEabc123_"));
        assertEquals("falseabc123_", UnicodeTranslator.toAscii("falseabc123_"));
	}

	@Test
	public void INTEGERLetterDigitUnderscore() {
        assertEquals("INTEGERabc123_", UnicodeTranslator.toAscii("INTEGERabc123_"));
        assertEquals("integerabc123_", UnicodeTranslator.toAscii("integerabc123_"));
	}

	@Test
	public void INTERLetterDigitUnderscore() {
        assertEquals("INTERabc123_", UnicodeTranslator.toAscii("INTERabc123_"));
        assertEquals("interabc123_", UnicodeTranslator.toAscii("interabc123_"));
	}

	@Test
	public void NATLetterDigitUnderscore() {
        assertEquals("NATabc123_", UnicodeTranslator.toAscii("NATabc123_"));
        assertEquals("natabc123_", UnicodeTranslator.toAscii("natabc123_"));
	}

	@Test
	public void NAT1LetterDigitUnderscore() {
        assertEquals("NAT1abc123_", UnicodeTranslator.toAscii("NAT1abc123_"));
        assertEquals("nat1abc123_", UnicodeTranslator.toAscii("nat1abc123_"));
	}

	@Test
	public void NATURALLetterDigitUnderscore() {
        assertEquals("NATURALabc123_", UnicodeTranslator.toAscii("NATURALabc123_"));
        assertEquals("naturalabc123_", UnicodeTranslator.toAscii("naturalabc123_"));
	}

	@Test
	public void NOTLetterDigitUnderscore() {
        assertEquals("NOTabc123_", UnicodeTranslator.toAscii("NOTabc123_"));
        assertEquals("notabc123_", UnicodeTranslator.toAscii("notabc123_"));
	}

	@Test
	public void ORLetterDigitUnderscore() {
        assertEquals("ORabc123_", UnicodeTranslator.toAscii("ORabc123_"));
        assertEquals("orabc123_", UnicodeTranslator.toAscii("orabc123_"));
	}

	@Test
	public void POWLetterDigitUnderscore() {
        assertEquals("POWabc123_", UnicodeTranslator.toAscii("POWabc123_"));
        assertEquals("powabc123_", UnicodeTranslator.toAscii("powabc123_"));
	}

	@Test
	public void POW1LetterDigitUnderscore() {
        assertEquals("POW1abc123_", UnicodeTranslator.toAscii("POW1abc123_"));
        assertEquals("pow1abc123_", UnicodeTranslator.toAscii("pow1abc123_"));
	}

	@Test
	public void TRUELetterDigitUnderscore() {
        assertEquals("TRUEabc123_", UnicodeTranslator.toAscii("TRUEabc123_"));
        assertEquals("trueabc123_", UnicodeTranslator.toAscii("trueabc123_"));
	}

	@Test
	public void UNIONLetterDigitUnderscore() {
        assertEquals("UNIONabc123_", UnicodeTranslator.toAscii("UNIONabc123_"));
        assertEquals("unionabc123_", UnicodeTranslator.toAscii("unionabc123_"));
	}

	@Test
	public void ANYLetterUnderscoreDigit() {
        assertEquals("ANYabc_123", UnicodeTranslator.toAscii("ANYabc_123"));
        assertEquals("anyabc_123", UnicodeTranslator.toAscii("anyabc_123"));
	}

	@Test
	public void FALSELetterUnderscoreDigit() {
        assertEquals("FALSEabc_123", UnicodeTranslator.toAscii("FALSEabc_123"));
        assertEquals("falseabc_123", UnicodeTranslator.toAscii("falseabc_123"));
	}

	@Test
	public void INTEGERLetterUnderscoreDigit() {
        assertEquals("INTEGERabc_123", UnicodeTranslator.toAscii("INTEGERabc_123"));
        assertEquals("integerabc_123", UnicodeTranslator.toAscii("integerabc_123"));
	}

	@Test
	public void INTERLetterUnderscoreDigit() {
        assertEquals("INTERabc_123", UnicodeTranslator.toAscii("INTERabc_123"));
        assertEquals("interabc_123", UnicodeTranslator.toAscii("interabc_123"));
	}

	@Test
	public void NATLetterUnderscoreDigit() {
        assertEquals("NATabc_123", UnicodeTranslator.toAscii("NATabc_123"));
        assertEquals("natabc_123", UnicodeTranslator.toAscii("natabc_123"));
	}

	@Test
	public void NAT1LetterUnderscoreDigit() {
        assertEquals("NAT1abc_123", UnicodeTranslator.toAscii("NAT1abc_123"));
        assertEquals("nat1abc_123", UnicodeTranslator.toAscii("nat1abc_123"));
	}

	@Test
	public void NATURALLetterUnderscoreDigit() {
        assertEquals("NATURALabc_123", UnicodeTranslator.toAscii("NATURALabc_123"));
        assertEquals("naturalabc_123", UnicodeTranslator.toAscii("naturalabc_123"));
	}

	@Test
	public void NOTLetterUnderscoreDigit() {
        assertEquals("NOTabc_123", UnicodeTranslator.toAscii("NOTabc_123"));
        assertEquals("notabc_123", UnicodeTranslator.toAscii("notabc_123"));
	}

	@Test
	public void ORLetterUnderscoreDigit() {
        assertEquals("ORabc_123", UnicodeTranslator.toAscii("ORabc_123"));
        assertEquals("orabc_123", UnicodeTranslator.toAscii("orabc_123"));
	}

	@Test
	public void POWLetterUnderscoreDigit() {
        assertEquals("POWabc_123", UnicodeTranslator.toAscii("POWabc_123"));
        assertEquals("powabc_123", UnicodeTranslator.toAscii("powabc_123"));
	}

	@Test
	public void POW1LetterUnderscoreDigit() {
        assertEquals("POW1abc_123", UnicodeTranslator.toAscii("POW1abc_123"));
        assertEquals("pow1abc_123", UnicodeTranslator.toAscii("pow1abc_123"));
	}

	@Test
	public void TRUELetterUnderscoreDigit() {
        assertEquals("TRUEabc_123", UnicodeTranslator.toAscii("TRUEabc_123"));
        assertEquals("trueabc_123", UnicodeTranslator.toAscii("trueabc_123"));
	}

	@Test
	public void UNIONLetterUnderscoreDigit() {
        assertEquals("UNIONabc_123", UnicodeTranslator.toAscii("UNIONabc_123"));
        assertEquals("unionabc_123", UnicodeTranslator.toAscii("unionabc_123"));
	}

	@Test
	public void ANYDigitLetterUnderscore() {
        assertEquals("ANY123abc_", UnicodeTranslator.toAscii("ANY123abc_"));
        assertEquals("any123abc_", UnicodeTranslator.toAscii("any123abc_"));
	}

	@Test
	public void FALSEDigitLetterUnderscore() {
        assertEquals("FALSE123abc_", UnicodeTranslator.toAscii("FALSE123abc_"));
        assertEquals("false123abc_", UnicodeTranslator.toAscii("false123abc_"));
	}

	@Test
	public void INTEGERDigitLetterUnderscore() {
        assertEquals("INTEGER123abc_", UnicodeTranslator.toAscii("INTEGER123abc_"));
        assertEquals("integer123abc_", UnicodeTranslator.toAscii("integer123abc_"));
	}

	@Test
	public void INTERDigitLetterUnderscore() {
        assertEquals("INTER123abc_", UnicodeTranslator.toAscii("INTER123abc_"));
        assertEquals("inter123abc_", UnicodeTranslator.toAscii("inter123abc_"));
	}

	@Test
	public void NATDigitLetterUnderscore() {
        assertEquals("NAT123abc_", UnicodeTranslator.toAscii("NAT123abc_"));
        assertEquals("nat123abc_", UnicodeTranslator.toAscii("nat123abc_"));
	}

	@Test
	public void NAT1DigitLetterUnderscore() {
        assertEquals("NAT1123abc_", UnicodeTranslator.toAscii("NAT1123abc_"));
        assertEquals("nat1123abc_", UnicodeTranslator.toAscii("nat1123abc_"));
	}

	@Test
	public void NATURALDigitLetterUnderscore() {
        assertEquals("NATURAL123abc_", UnicodeTranslator.toAscii("NATURAL123abc_"));
        assertEquals("natural123abc_", UnicodeTranslator.toAscii("natural123abc_"));
	}

	@Test
	public void NOTDigitLetterUnderscore() {
        assertEquals("NOT123abc_", UnicodeTranslator.toAscii("NOT123abc_"));
        assertEquals("not123abc_", UnicodeTranslator.toAscii("not123abc_"));
	}

	@Test
	public void ORDigitLetterUnderscore() {
        assertEquals("OR123abc_", UnicodeTranslator.toAscii("OR123abc_"));
        assertEquals("or123abc_", UnicodeTranslator.toAscii("or123abc_"));
	}

	@Test
	public void POWDigitLetterUnderscore() {
        assertEquals("POW123abc_", UnicodeTranslator.toAscii("POW123abc_"));
        assertEquals("pow123abc_", UnicodeTranslator.toAscii("pow123abc_"));
	}

	@Test
	public void POW1DigitLetterUnderscore() {
        assertEquals("POW1123abc_", UnicodeTranslator.toAscii("POW1123abc_"));
        assertEquals("pow1123abc_", UnicodeTranslator.toAscii("pow1123abc_"));
	}

	@Test
	public void TRUEDigitLetterUnderscore() {
        assertEquals("TRUE123abc_", UnicodeTranslator.toAscii("TRUE123abc_"));
        assertEquals("true123abc_", UnicodeTranslator.toAscii("true123abc_"));
	}

	@Test
	public void UNIONDigitLetterUnderscore() {
        assertEquals("UNION123abc_", UnicodeTranslator.toAscii("UNION123abc_"));
        assertEquals("union123abc_", UnicodeTranslator.toAscii("union123abc_"));
	}

	@Test
	public void ANYDigitUnderscoreLetter() {
        assertEquals("ANY123_abc", UnicodeTranslator.toAscii("ANY123_abc"));
        assertEquals("any123_abc", UnicodeTranslator.toAscii("any123_abc"));
	}

	@Test
	public void FALSEDigitUnderscoreLetter() {
        assertEquals("FALSE123_abc", UnicodeTranslator.toAscii("FALSE123_abc"));
        assertEquals("false123_abc", UnicodeTranslator.toAscii("false123_abc"));
	}

	@Test
	public void INTEGERDigitUnderscoreLetter() {
        assertEquals("INTEGER123_abc", UnicodeTranslator.toAscii("INTEGER123_abc"));
        assertEquals("integer123_abc", UnicodeTranslator.toAscii("integer123_abc"));
	}

	@Test
	public void INTERDigitUnderscoreLetter() {
        assertEquals("INTER123_abc", UnicodeTranslator.toAscii("INTER123_abc"));
        assertEquals("inter123_abc", UnicodeTranslator.toAscii("inter123_abc"));
	}

	@Test
	public void NATDigitUnderscoreLetter() {
        assertEquals("NAT123_abc", UnicodeTranslator.toAscii("NAT123_abc"));
        assertEquals("nat123_abc", UnicodeTranslator.toAscii("nat123_abc"));
	}

	@Test
	public void NAT1DigitUnderscoreLetter() {
        assertEquals("NAT1123_abc", UnicodeTranslator.toAscii("NAT1123_abc"));
        assertEquals("nat1123_abc", UnicodeTranslator.toAscii("nat1123_abc"));
	}

	@Test
	public void NATURALDigitUnderscoreLetter() {
        assertEquals("NATURAL123_abc", UnicodeTranslator.toAscii("NATURAL123_abc"));
        assertEquals("natural123_abc", UnicodeTranslator.toAscii("natural123_abc"));
	}

	@Test
	public void NOTDigitUnderscoreLetter() {
        assertEquals("NOT123_abc", UnicodeTranslator.toAscii("NOT123_abc"));
        assertEquals("not123_abc", UnicodeTranslator.toAscii("not123_abc"));
	}

	@Test
	public void ORDigitUnderscoreLetter() {
        assertEquals("OR123_abc", UnicodeTranslator.toAscii("OR123_abc"));
        assertEquals("or123_abc", UnicodeTranslator.toAscii("or123_abc"));
	}

	@Test
	public void POWDigitUnderscoreLetter() {
        assertEquals("POW123_abc", UnicodeTranslator.toAscii("POW123_abc"));
        assertEquals("pow123_abc", UnicodeTranslator.toAscii("pow123_abc"));
	}

	@Test
	public void POW1DigitUnderscoreLetter() {
        assertEquals("POW1123_abc", UnicodeTranslator.toAscii("POW1123_abc"));
        assertEquals("pow1123_abc", UnicodeTranslator.toAscii("pow1123_abc"));
	}

	@Test
	public void TRUEDigitUnderscoreLetter() {
        assertEquals("TRUE123_abc", UnicodeTranslator.toAscii("TRUE123_abc"));
        assertEquals("true123_abc", UnicodeTranslator.toAscii("true123_abc"));
	}

	@Test
	public void UNIONDigitUnderscoreLetter() {
        assertEquals("UNION123_abc", UnicodeTranslator.toAscii("UNION123_abc"));
        assertEquals("union123_abc", UnicodeTranslator.toAscii("union123_abc"));
	}

	@Test
	public void ANYUnderscoreLetterDigit() {
        assertEquals("ANY_abc123", UnicodeTranslator.toAscii("ANY_abc123"));
        assertEquals("any_abc123", UnicodeTranslator.toAscii("any_abc123"));
	}

	@Test
	public void FALSEUnderscoreLetterDigit() {
        assertEquals("FALSE_abc123", UnicodeTranslator.toAscii("FALSE_abc123"));
        assertEquals("false_abc123", UnicodeTranslator.toAscii("false_abc123"));
	}

	@Test
	public void INTEGERUnderscoreLetterDigit() {
        assertEquals("INTEGER_abc123", UnicodeTranslator.toAscii("INTEGER_abc123"));
        assertEquals("integer_abc123", UnicodeTranslator.toAscii("integer_abc123"));
	}

	@Test
	public void INTERUnderscoreLetterDigit() {
        assertEquals("INTER_abc123", UnicodeTranslator.toAscii("INTER_abc123"));
        assertEquals("inter_abc123", UnicodeTranslator.toAscii("inter_abc123"));
	}

	@Test
	public void NATUnderscoreLetterDigit() {
        assertEquals("NAT_abc123", UnicodeTranslator.toAscii("NAT_abc123"));
        assertEquals("nat_abc123", UnicodeTranslator.toAscii("nat_abc123"));
	}

	@Test
	public void NAT1UnderscoreLetterDigit() {
        assertEquals("NAT1_abc123", UnicodeTranslator.toAscii("NAT1_abc123"));
        assertEquals("nat1_abc123", UnicodeTranslator.toAscii("nat1_abc123"));
	}

	@Test
	public void NATURALUnderscoreLetterDigit() {
        assertEquals("NATURAL_abc123", UnicodeTranslator.toAscii("NATURAL_abc123"));
        assertEquals("natural_abc123", UnicodeTranslator.toAscii("natural_abc123"));
	}

	@Test
	public void NOTUnderscoreLetterDigit() {
        assertEquals("NOT_abc123", UnicodeTranslator.toAscii("NOT_abc123"));
        assertEquals("not_abc123", UnicodeTranslator.toAscii("not_abc123"));
	}

	@Test
	public void ORUnderscoreLetterDigit() {
        assertEquals("OR_abc123", UnicodeTranslator.toAscii("OR_abc123"));
        assertEquals("or_abc123", UnicodeTranslator.toAscii("or_abc123"));
	}

	@Test
	public void POWUnderscoreLetterDigit() {
        assertEquals("POW_abc123", UnicodeTranslator.toAscii("POW_abc123"));
        assertEquals("pow_abc123", UnicodeTranslator.toAscii("pow_abc123"));
	}

	@Test
	public void POW1UnderscoreLetterDigit() {
        assertEquals("POW1_abc123", UnicodeTranslator.toAscii("POW1_abc123"));
        assertEquals("pow1_abc123", UnicodeTranslator.toAscii("pow1_abc123"));
	}

	@Test
	public void TRUEUnderscoreLetterDigit() {
        assertEquals("TRUE_abc123", UnicodeTranslator.toAscii("TRUE_abc123"));
        assertEquals("true_abc123", UnicodeTranslator.toAscii("true_abc123"));
	}

	@Test
	public void UNIONUnderscoreLetterDigit() {
        assertEquals("UNION_abc123", UnicodeTranslator.toAscii("UNION_abc123"));
        assertEquals("union_abc123", UnicodeTranslator.toAscii("union_abc123"));
	}

	@Test
	public void ANYUnderscoreDigitLetter() {
        assertEquals("ANY_123abc", UnicodeTranslator.toAscii("ANY_123abc"));
        assertEquals("any_123abc", UnicodeTranslator.toAscii("any_123abc"));
	}

	@Test
	public void FALSEUnderscoreDigitLetter() {
        assertEquals("FALSE_123abc", UnicodeTranslator.toAscii("FALSE_123abc"));
        assertEquals("false_123abc", UnicodeTranslator.toAscii("false_123abc"));
	}

	@Test
	public void INTEGERUnderscoreDigitLetter() {
        assertEquals("INTEGER_123abc", UnicodeTranslator.toAscii("INTEGER_123abc"));
        assertEquals("integer_123abc", UnicodeTranslator.toAscii("integer_123abc"));
	}

	@Test
	public void INTERUnderscoreDigitLetter() {
        assertEquals("INTER_123abc", UnicodeTranslator.toAscii("INTER_123abc"));
        assertEquals("inter_123abc", UnicodeTranslator.toAscii("inter_123abc"));
	}

	@Test
	public void NATUnderscoreDigitLetter() {
        assertEquals("NAT_123abc", UnicodeTranslator.toAscii("NAT_123abc"));
        assertEquals("nat_123abc", UnicodeTranslator.toAscii("nat_123abc"));
	}

	@Test
	public void NAT1UnderscoreDigitLetter() {
        assertEquals("NAT1_123abc", UnicodeTranslator.toAscii("NAT1_123abc"));
        assertEquals("nat1_123abc", UnicodeTranslator.toAscii("nat1_123abc"));
	}

	@Test
	public void NATURALUnderscoreDigitLetter() {
        assertEquals("NATURAL_123abc", UnicodeTranslator.toAscii("NATURAL_123abc"));
        assertEquals("natural_123abc", UnicodeTranslator.toAscii("natural_123abc"));
	}

	@Test
	public void NOTUnderscoreDigitLetter() {
        assertEquals("NOT_123abc", UnicodeTranslator.toAscii("NOT_123abc"));
        assertEquals("not_123abc", UnicodeTranslator.toAscii("not_123abc"));
	}

	@Test
	public void ORUnderscoreDigitLetter() {
        assertEquals("OR_123abc", UnicodeTranslator.toAscii("OR_123abc"));
        assertEquals("or_123abc", UnicodeTranslator.toAscii("or_123abc"));
	}

	@Test
	public void POWUnderscoreDigitLetter() {
        assertEquals("POW_123abc", UnicodeTranslator.toAscii("POW_123abc"));
        assertEquals("pow_123abc", UnicodeTranslator.toAscii("pow_123abc"));
	}

	@Test
	public void POW1UnderscoreDigitLetter() {
        assertEquals("POW1_123abc", UnicodeTranslator.toAscii("POW1_123abc"));
        assertEquals("pow1_123abc", UnicodeTranslator.toAscii("pow1_123abc"));
	}

	@Test
	public void TRUEUnderscoreDigitLetter() {
        assertEquals("TRUE_123abc", UnicodeTranslator.toAscii("TRUE_123abc"));
        assertEquals("true_123abc", UnicodeTranslator.toAscii("true_123abc"));
	}

	@Test
	public void UNIONUnderscoreDigitLetter() {
        assertEquals("UNION_123abc", UnicodeTranslator.toAscii("UNION_123abc"));
        assertEquals("union_123abc", UnicodeTranslator.toAscii("union_123abc"));
	}

	@Test
	public void UnderscoreDigitUnderscore() {
        assertEquals("_123_", UnicodeTranslator.toAscii("_123_"));
	}

	@Test
	public void UnderscoreLetterUnderscore() {
        assertEquals("_abc_", UnicodeTranslator.toAscii("_abc_"));
	}

	@Test
	public void UnderscoreANYUnderscore() {
        assertEquals("_ANY_", UnicodeTranslator.toAscii("_ANY_"));
        assertEquals("_any_", UnicodeTranslator.toAscii("_any_"));
	}

	@Test
	public void UnderscoreFALSEUnderscore() {
        assertEquals("_FALSE_", UnicodeTranslator.toAscii("_FALSE_"));
        assertEquals("_false_", UnicodeTranslator.toAscii("_false_"));
	}

	@Test
	public void UnderscoreINTEGERUnderscore() {
        assertEquals("_INTEGER_", UnicodeTranslator.toAscii("_INTEGER_"));
        assertEquals("_integer_", UnicodeTranslator.toAscii("_integer_"));
	}

	@Test
	public void UnderscoreINTERUnderscore() {
        assertEquals("_INTER_", UnicodeTranslator.toAscii("_INTER_"));
        assertEquals("_inter_", UnicodeTranslator.toAscii("_inter_"));
	}

	@Test
	public void UnderscoreNATUnderscore() {
        assertEquals("_NAT_", UnicodeTranslator.toAscii("_NAT_"));
        assertEquals("_nat_", UnicodeTranslator.toAscii("_nat_"));
	}

	@Test
	public void UnderscoreNAT1Underscore() {
        assertEquals("_NAT1_", UnicodeTranslator.toAscii("_NAT1_"));
        assertEquals("_nat1_", UnicodeTranslator.toAscii("_nat1_"));
	}

	@Test
	public void UnderscoreNATURALUnderscore() {
        assertEquals("_NATURAL_", UnicodeTranslator.toAscii("_NATURAL_"));
        assertEquals("_natural_", UnicodeTranslator.toAscii("_natural_"));
	}

	@Test
	public void UnderscoreNOTUnderscore() {
        assertEquals("_NOT_", UnicodeTranslator.toAscii("_NOT_"));
        assertEquals("_not_", UnicodeTranslator.toAscii("_not_"));
	}

	@Test
	public void UnderscoreORUnderscore() {
        assertEquals("_OR_", UnicodeTranslator.toAscii("_OR_"));
        assertEquals("_or_", UnicodeTranslator.toAscii("_or_"));
	}

	@Test
	public void UnderscorePOWUnderscore() {
        assertEquals("_POW_", UnicodeTranslator.toAscii("_POW_"));
        assertEquals("_pow_", UnicodeTranslator.toAscii("_pow_"));
	}

	@Test
	public void UnderscorePOW1Underscore() {
        assertEquals("_POW1_", UnicodeTranslator.toAscii("_POW1_"));
        assertEquals("_pow1_", UnicodeTranslator.toAscii("_pow1_"));
	}

	@Test
	public void UnderscoreTRUEUnderscore() {
        assertEquals("_TRUE_", UnicodeTranslator.toAscii("_TRUE_"));
        assertEquals("_true_", UnicodeTranslator.toAscii("_true_"));
	}

	@Test
	public void UnderscoreUNIONUnderscore() {
        assertEquals("_UNION_", UnicodeTranslator.toAscii("_UNION_"));
        assertEquals("_union_", UnicodeTranslator.toAscii("_union_"));
	}

	@Test
	public void LetterUnderscoreDigitUnderscoreLetter() {
        assertEquals("abc_123_abc", UnicodeTranslator.toAscii("abc_123_abc"));
	}

	@Test
	public void LetterUnderscoreLetterUnderscoreLetter() {
        assertEquals("abc_abc_abc", UnicodeTranslator.toAscii("abc_abc_abc"));
	}

	@Test
	public void LetterUnderscoreANYUnderscoreLetter() {
        assertEquals("abc_ANY_abc", UnicodeTranslator.toAscii("abc_ANY_abc"));
        assertEquals("abc_any_abc", UnicodeTranslator.toAscii("abc_any_abc"));
	}

	@Test
	public void LetterUnderscoreFALSEUnderscoreLetter() {
        assertEquals("abc_FALSE_abc", UnicodeTranslator.toAscii("abc_FALSE_abc"));
        assertEquals("abc_false_abc", UnicodeTranslator.toAscii("abc_false_abc"));
	}

	@Test
	public void LetterUnderscoreINTEGERUnderscoreLetter() {
        assertEquals("abc_INTEGER_abc", UnicodeTranslator.toAscii("abc_INTEGER_abc"));
        assertEquals("abc_integer_abc", UnicodeTranslator.toAscii("abc_integer_abc"));
	}

	@Test
	public void LetterUnderscoreINTERUnderscoreLetter() {
        assertEquals("abc_INTER_abc", UnicodeTranslator.toAscii("abc_INTER_abc"));
        assertEquals("abc_inter_abc", UnicodeTranslator.toAscii("abc_inter_abc"));
	}

	@Test
	public void LetterUnderscoreNATUnderscoreLetter() {
        assertEquals("abc_NAT_abc", UnicodeTranslator.toAscii("abc_NAT_abc"));
        assertEquals("abc_nat_abc", UnicodeTranslator.toAscii("abc_nat_abc"));
	}

	@Test
	public void LetterUnderscoreNAT1UnderscoreLetter() {
        assertEquals("abc_NAT1_abc", UnicodeTranslator.toAscii("abc_NAT1_abc"));
        assertEquals("abc_nat1_abc", UnicodeTranslator.toAscii("abc_nat1_abc"));
	}

	@Test
	public void LetterUnderscoreNATURALUnderscoreLetter() {
        assertEquals("abc_NATURAL_abc", UnicodeTranslator.toAscii("abc_NATURAL_abc"));
        assertEquals("abc_natural_abc", UnicodeTranslator.toAscii("abc_natural_abc"));
	}

	@Test
	public void LetterUnderscoreNOTUnderscoreLetter() {
        assertEquals("abc_NOT_abc", UnicodeTranslator.toAscii("abc_NOT_abc"));
        assertEquals("abc_not_abc", UnicodeTranslator.toAscii("abc_not_abc"));
	}

	@Test
	public void LetterUnderscoreORUnderscoreLetter() {
        assertEquals("abc_OR_abc", UnicodeTranslator.toAscii("abc_OR_abc"));
        assertEquals("abc_or_abc", UnicodeTranslator.toAscii("abc_or_abc"));
	}

	@Test
	public void LetterUnderscorePOWUnderscoreLetter() {
        assertEquals("abc_POW_abc", UnicodeTranslator.toAscii("abc_POW_abc"));
        assertEquals("abc_pow_abc", UnicodeTranslator.toAscii("abc_pow_abc"));
	}

	@Test
	public void LetterUnderscorePOW1UnderscoreLetter() {
        assertEquals("abc_POW1_abc", UnicodeTranslator.toAscii("abc_POW1_abc"));
        assertEquals("abc_pow1_abc", UnicodeTranslator.toAscii("abc_pow1_abc"));
	}

	@Test
	public void LetterUnderscoreTRUEUnderscoreLetter() {
        assertEquals("abc_TRUE_abc", UnicodeTranslator.toAscii("abc_TRUE_abc"));
        assertEquals("abc_true_abc", UnicodeTranslator.toAscii("abc_true_abc"));
	}

	@Test
	public void LetterUnderscoreUNIONUnderscoreLetter() {
        assertEquals("abc_UNION_abc", UnicodeTranslator.toAscii("abc_UNION_abc"));
        assertEquals("abc_union_abc", UnicodeTranslator.toAscii("abc_union_abc"));
	}

	@Test
	public void DigitUnderscoreDigitUnderscoreDigit() {
        assertEquals("123_123_123", UnicodeTranslator.toAscii("123_123_123"));
	}

	@Test
	public void DigitUnderscoreLetterUnderscoreDigit() {
        assertEquals("123_abc_123", UnicodeTranslator.toAscii("123_abc_123"));
	}

	@Test
	public void DigitUnderscoreANYUnderscoreDigit() {
        assertEquals("123_ANY_123", UnicodeTranslator.toAscii("123_ANY_123"));
        assertEquals("123_any_123", UnicodeTranslator.toAscii("123_any_123"));
	}

	@Test
	public void DigitUnderscoreFALSEUnderscoreDigit() {
        assertEquals("123_FALSE_123", UnicodeTranslator.toAscii("123_FALSE_123"));
        assertEquals("123_false_123", UnicodeTranslator.toAscii("123_false_123"));
	}

	@Test
	public void DigitUnderscoreINTEGERUnderscoreDigit() {
        assertEquals("123_INTEGER_123", UnicodeTranslator.toAscii("123_INTEGER_123"));
        assertEquals("123_integer_123", UnicodeTranslator.toAscii("123_integer_123"));
	}

	@Test
	public void DigitUnderscoreINTERUnderscoreDigit() {
        assertEquals("123_INTER_123", UnicodeTranslator.toAscii("123_INTER_123"));
        assertEquals("123_inter_123", UnicodeTranslator.toAscii("123_inter_123"));
	}

	@Test
	public void DigitUnderscoreNATUnderscoreDigit() {
        assertEquals("123_NAT_123", UnicodeTranslator.toAscii("123_NAT_123"));
        assertEquals("123_nat_123", UnicodeTranslator.toAscii("123_nat_123"));
	}

	@Test
	public void DigitUnderscoreNAT1UnderscoreDigit() {
        assertEquals("123_NAT1_123", UnicodeTranslator.toAscii("123_NAT1_123"));
        assertEquals("123_nat1_123", UnicodeTranslator.toAscii("123_nat1_123"));
	}

	@Test
	public void DigitUnderscoreNATURALUnderscoreDigit() {
        assertEquals("123_NATURAL_123", UnicodeTranslator.toAscii("123_NATURAL_123"));
        assertEquals("123_natural_123", UnicodeTranslator.toAscii("123_natural_123"));
	}

	@Test
	public void DigitUnderscoreNOTUnderscoreDigit() {
        assertEquals("123_NOT_123", UnicodeTranslator.toAscii("123_NOT_123"));
        assertEquals("123_not_123", UnicodeTranslator.toAscii("123_not_123"));
	}

	@Test
	public void DigitUnderscoreORUnderscoreDigit() {
        assertEquals("123_OR_123", UnicodeTranslator.toAscii("123_OR_123"));
        assertEquals("123_or_123", UnicodeTranslator.toAscii("123_or_123"));
	}

	@Test
	public void DigitUnderscorePOWUnderscoreDigit() {
        assertEquals("123_POW_123", UnicodeTranslator.toAscii("123_POW_123"));
        assertEquals("123_pow_123", UnicodeTranslator.toAscii("123_pow_123"));
	}

	@Test
	public void DigitUnderscorePOW1UnderscoreDigit() {
        assertEquals("123_POW1_123", UnicodeTranslator.toAscii("123_POW1_123"));
        assertEquals("123_pow1_123", UnicodeTranslator.toAscii("123_pow1_123"));
	}

	@Test
	public void DigitUnderscoreTRUEUnderscoreDigit() {
        assertEquals("123_TRUE_123", UnicodeTranslator.toAscii("123_TRUE_123"));
        assertEquals("123_true_123", UnicodeTranslator.toAscii("123_true_123"));
	}

	@Test
	public void DigitUnderscoreUNIONUnderscoreDigit() {
        assertEquals("123_UNION_123", UnicodeTranslator.toAscii("123_UNION_123"));
        assertEquals("123_union_123", UnicodeTranslator.toAscii("123_union_123"));
	}

	/*--------------------------------------------------------------*/

	@Test
	public void Var_123() {
        assertEquals("var_123", UnicodeTranslator.toAscii("var_123"));
        assertEquals("123_var", UnicodeTranslator.toAscii("123_var"));
        assertEquals("var_123_var", UnicodeTranslator.toAscii("var_123_var"));

        assertEquals("var_", UnicodeTranslator.toAscii("var_"));
        assertEquals("_var", UnicodeTranslator.toAscii("_var"));
        assertEquals("_var_", UnicodeTranslator.toAscii("_var_"));

        assertEquals("123_", UnicodeTranslator.toAscii("123_"));
        assertEquals("_123", UnicodeTranslator.toAscii("_123"));
        assertEquals("_123_", UnicodeTranslator.toAscii("_123_"));
	}

	@Test
	public void Var123() {
        assertEquals("var123", UnicodeTranslator.toAscii("var123"));
        assertEquals("123var", UnicodeTranslator.toAscii("123var"));
        assertEquals("var123var", UnicodeTranslator.toAscii("var123var"));
        assertEquals("123var123", UnicodeTranslator.toAscii("123var123"));
	}

	@Test
	public void VarANY() {
        assertEquals("varANY", UnicodeTranslator.toAscii("varANY"));
        assertEquals("varany", UnicodeTranslator.toAscii("varany"));
        assertEquals("varANYvar", UnicodeTranslator.toAscii("varANYvar"));
        assertEquals("varanyvar", UnicodeTranslator.toAscii("varanyvar"));
        assertEquals("ANYvar", UnicodeTranslator.toAscii("ANYvar"));
        assertEquals("anyvar", UnicodeTranslator.toAscii("anyvar"));

        assertEquals("123any", UnicodeTranslator.toAscii("123any"));
        assertEquals("123ANY", UnicodeTranslator.toAscii("123ANY"));
        assertEquals("123ANY123", UnicodeTranslator.toAscii("123ANY123"));
        assertEquals("123any123", UnicodeTranslator.toAscii("123any123"));
        assertEquals("ANY123", UnicodeTranslator.toAscii("ANY123"));
        assertEquals("any123", UnicodeTranslator.toAscii("any123"));

        assertEquals("_any", UnicodeTranslator.toAscii("_any"));
        assertEquals("_ANY", UnicodeTranslator.toAscii("_ANY"));
        assertEquals("_ANY_", UnicodeTranslator.toAscii("_ANY_"));
        assertEquals("_any_", UnicodeTranslator.toAscii("_any_"));
        assertEquals("ANY_", UnicodeTranslator.toAscii("ANY_"));
        assertEquals("any_", UnicodeTranslator.toAscii("any_"));
	}

	@Test
	public void VarFALSE() {
        assertEquals("varFALSE", UnicodeTranslator.toAscii("varFALSE"));
        assertEquals("varfalse", UnicodeTranslator.toAscii("varfalse"));
        assertEquals("varFALSEvar", UnicodeTranslator.toAscii("varFALSEvar"));
        assertEquals("varfalsevar", UnicodeTranslator.toAscii("varfalsevar"));
        assertEquals("FALSEvar", UnicodeTranslator.toAscii("FALSEvar"));
        assertEquals("falsevar", UnicodeTranslator.toAscii("falsevar"));

        assertEquals("123FALSE", UnicodeTranslator.toAscii("123FALSE"));
        assertEquals("123false", UnicodeTranslator.toAscii("123false"));
        assertEquals("123FALSE123", UnicodeTranslator.toAscii("123FALSE123"));
        assertEquals("123false123", UnicodeTranslator.toAscii("123false123"));
        assertEquals("FALSE123", UnicodeTranslator.toAscii("FALSE123"));
        assertEquals("false123", UnicodeTranslator.toAscii("false123"));

        assertEquals("_FALSE", UnicodeTranslator.toAscii("_FALSE"));
        assertEquals("_false", UnicodeTranslator.toAscii("_false"));
        assertEquals("_FALSE_", UnicodeTranslator.toAscii("_FALSE_"));
        assertEquals("_false_", UnicodeTranslator.toAscii("_false_"));
        assertEquals("FALSE_", UnicodeTranslator.toAscii("FALSE_"));
        assertEquals("false_", UnicodeTranslator.toAscii("false_"));
	}

	@Test
	public void VarINTEGER() {
        assertEquals("varINTEGER", UnicodeTranslator.toAscii("varINTEGER"));
        assertEquals("varinteger", UnicodeTranslator.toAscii("varinteger"));
        assertEquals("varINTEGERvar", UnicodeTranslator.toAscii("varINTEGERvar"));
        assertEquals("varintegervar", UnicodeTranslator.toAscii("varintegervar"));
        assertEquals("INTEGERvar", UnicodeTranslator.toAscii("INTEGERvar"));
        assertEquals("integervar", UnicodeTranslator.toAscii("integervar"));

        assertEquals("123INTEGER", UnicodeTranslator.toAscii("123INTEGER"));
        assertEquals("123integer", UnicodeTranslator.toAscii("123integer"));
        assertEquals("INTEGER123", UnicodeTranslator.toAscii("INTEGER123"));
        assertEquals("integer123", UnicodeTranslator.toAscii("integer123"));
        assertEquals("123INTEGER123", UnicodeTranslator.toAscii("123INTEGER123"));
        assertEquals("123integer123", UnicodeTranslator.toAscii("123integer123"));

        assertEquals("_INTEGER", UnicodeTranslator.toAscii("_INTEGER"));
        assertEquals("_integer", UnicodeTranslator.toAscii("_integer"));
        assertEquals("_INTEGER_", UnicodeTranslator.toAscii("_INTEGER_"));
        assertEquals("_integer_", UnicodeTranslator.toAscii("_integer_"));
        assertEquals("INTEGER_", UnicodeTranslator.toAscii("INTEGER_"));
        assertEquals("integer_", UnicodeTranslator.toAscii("integer_"));
	}

	@Test
	public void VarINTER() {
        assertEquals("varINTER", UnicodeTranslator.toAscii("varINTER"));
        assertEquals("varinter", UnicodeTranslator.toAscii("varinter"));
        assertEquals("varINTERvar", UnicodeTranslator.toAscii("varINTERvar"));
        assertEquals("varintervar", UnicodeTranslator.toAscii("varintervar"));
        assertEquals("INTERvar", UnicodeTranslator.toAscii("INTERvar"));
        assertEquals("intervar", UnicodeTranslator.toAscii("intervar"));

        assertEquals("123inter", UnicodeTranslator.toAscii("123inter"));
        assertEquals("123INTER", UnicodeTranslator.toAscii("123INTER"));
        assertEquals("123INTER123", UnicodeTranslator.toAscii("123INTER123"));
        assertEquals("123inter123", UnicodeTranslator.toAscii("123inter123"));
        assertEquals("INTER123", UnicodeTranslator.toAscii("INTER123"));
        assertEquals("inter123", UnicodeTranslator.toAscii("inter123"));

        assertEquals("_INTER", UnicodeTranslator.toAscii("_INTER"));
        assertEquals("_inter", UnicodeTranslator.toAscii("_inter"));
        assertEquals("_INTER_", UnicodeTranslator.toAscii("_INTER_"));
        assertEquals("_inter_", UnicodeTranslator.toAscii("_inter_"));
        assertEquals("INTER_", UnicodeTranslator.toAscii("INTER_"));
        assertEquals("inter_", UnicodeTranslator.toAscii("inter_"));
	}

	@Test
	public void VarNAT() {
        assertEquals("varNAT", UnicodeTranslator.toAscii("varNAT"));
        assertEquals("varnat", UnicodeTranslator.toAscii("varnat"));
        assertEquals("varNATvar", UnicodeTranslator.toAscii("varNATvar"));
        assertEquals("varnatvar", UnicodeTranslator.toAscii("varnatvar"));
        assertEquals("NATvar", UnicodeTranslator.toAscii("NATvar"));
        assertEquals("natvar", UnicodeTranslator.toAscii("natvar"));

        assertEquals("123NAT", UnicodeTranslator.toAscii("123NAT"));
        assertEquals("123nat", UnicodeTranslator.toAscii("123nat"));
        assertEquals("123NAT123", UnicodeTranslator.toAscii("123NAT123"));
        assertEquals("123nat123", UnicodeTranslator.toAscii("123nat123"));
        assertEquals("NAT123", UnicodeTranslator.toAscii("NAT123"));
        assertEquals("nat123", UnicodeTranslator.toAscii("nat123"));

        assertEquals("_NAT", UnicodeTranslator.toAscii("_NAT"));
        assertEquals("_nat", UnicodeTranslator.toAscii("_nat"));
        assertEquals("_NAT_", UnicodeTranslator.toAscii("_NAT_"));
        assertEquals("_nat_", UnicodeTranslator.toAscii("_nat_"));
        assertEquals("NAT_", UnicodeTranslator.toAscii("NAT_"));
        assertEquals("nat_", UnicodeTranslator.toAscii("nat_"));
	}

	@Test
	public void VarNAT1() {
        assertEquals("varNAT1", UnicodeTranslator.toAscii("varNAT1"));
        assertEquals("varnat1", UnicodeTranslator.toAscii("varnat1"));
        assertEquals("varNAT1var", UnicodeTranslator.toAscii("varNAT1var"));
        assertEquals("varnat1var", UnicodeTranslator.toAscii("varnat1var"));
        assertEquals("NAT1var", UnicodeTranslator.toAscii("NAT1var"));
        assertEquals("nat1var", UnicodeTranslator.toAscii("nat1var"));

        assertEquals("123NAT1", UnicodeTranslator.toAscii("123NAT1"));
        assertEquals("123nat1", UnicodeTranslator.toAscii("123nat1"));
        assertEquals("123NAT1123", UnicodeTranslator.toAscii("123NAT1123"));
        assertEquals("123nat1123", UnicodeTranslator.toAscii("123nat1123"));
        assertEquals("NAT1123", UnicodeTranslator.toAscii("NAT1123"));
        assertEquals("nat1123", UnicodeTranslator.toAscii("nat1123"));

        assertEquals("_NAT1", UnicodeTranslator.toAscii("_NAT1"));
        assertEquals("_nat1", UnicodeTranslator.toAscii("_nat1"));
        assertEquals("_NAT1_", UnicodeTranslator.toAscii("_NAT1_"));
        assertEquals("_nat1_", UnicodeTranslator.toAscii("_nat1_"));
        assertEquals("NAT1_", UnicodeTranslator.toAscii("NAT1_"));
        assertEquals("nat1_", UnicodeTranslator.toAscii("nat1_"));
	}

	@Test
	public void VarNATURAL() {
        assertEquals("varNATURAL", UnicodeTranslator.toAscii("varNATURAL"));
        assertEquals("varnatural", UnicodeTranslator.toAscii("varnatural"));
        assertEquals("varNATURALvar", UnicodeTranslator.toAscii("varNATURALvar"));
        assertEquals("varnaturalvar", UnicodeTranslator.toAscii("varnaturalvar"));
        assertEquals("NATURALvar", UnicodeTranslator.toAscii("NATURALvar"));
        assertEquals("naturalvar", UnicodeTranslator.toAscii("naturalvar"));

        assertEquals("123NATURAL", UnicodeTranslator.toAscii("123NATURAL"));
        assertEquals("123natural", UnicodeTranslator.toAscii("123natural"));
        assertEquals("123NATURAL123", UnicodeTranslator.toAscii("123NATURAL123"));
        assertEquals("123natural123", UnicodeTranslator.toAscii("123natural123"));
        assertEquals("NATURAL123", UnicodeTranslator.toAscii("NATURAL123"));
        assertEquals("natural123", UnicodeTranslator.toAscii("natural123"));

        assertEquals("_NATURAL", UnicodeTranslator.toAscii("_NATURAL"));
        assertEquals("_natural", UnicodeTranslator.toAscii("_natural"));
        assertEquals("_NATURAL_", UnicodeTranslator.toAscii("_NATURAL_"));
        assertEquals("_natural_", UnicodeTranslator.toAscii("_natural_"));
        assertEquals("NATURAL_", UnicodeTranslator.toAscii("NATURAL_"));
        assertEquals("natural_", UnicodeTranslator.toAscii("natural_"));
	}

	@Test
	public void VarNOT() {
        assertEquals("varNOT", UnicodeTranslator.toAscii("varNOT"));
        assertEquals("varnot", UnicodeTranslator.toAscii("varnot"));
        assertEquals("varNOTvar", UnicodeTranslator.toAscii("varNOTvar"));
        assertEquals("varnotvar", UnicodeTranslator.toAscii("varnotvar"));
        assertEquals("NOTvar", UnicodeTranslator.toAscii("NOTvar"));
        assertEquals("notvar", UnicodeTranslator.toAscii("notvar"));

        assertEquals("123NOT", UnicodeTranslator.toAscii("123NOT"));
        assertEquals("123not", UnicodeTranslator.toAscii("123not"));
        assertEquals("123NOT123", UnicodeTranslator.toAscii("123NOT123"));
        assertEquals("123not123", UnicodeTranslator.toAscii("123not123"));
        assertEquals("NOT123", UnicodeTranslator.toAscii("NOT123"));
        assertEquals("not123", UnicodeTranslator.toAscii("not123"));

        assertEquals("_NOT", UnicodeTranslator.toAscii("_NOT"));
        assertEquals("_not", UnicodeTranslator.toAscii("_not"));
        assertEquals("_NOT_", UnicodeTranslator.toAscii("_NOT_"));
        assertEquals("_not_", UnicodeTranslator.toAscii("_not_"));
        assertEquals("NOT_", UnicodeTranslator.toAscii("NOT_"));
        assertEquals("not_", UnicodeTranslator.toAscii("not_"));
	}

	@Test
	public void VarOr() {
        assertEquals("varOR", UnicodeTranslator.toAscii("varOR"));
        assertEquals("varor", UnicodeTranslator.toAscii("varor"));
        assertEquals("varORvar", UnicodeTranslator.toAscii("varORvar"));
        assertEquals("varorvar", UnicodeTranslator.toAscii("varorvar"));
        assertEquals("ORvar", UnicodeTranslator.toAscii("ORvar"));
        assertEquals("orvar", UnicodeTranslator.toAscii("orvar"));

        assertEquals("123OR", UnicodeTranslator.toAscii("123OR"));
        assertEquals("123or", UnicodeTranslator.toAscii("123or"));
        assertEquals("123OR123", UnicodeTranslator.toAscii("123OR123"));
        assertEquals("123or123", UnicodeTranslator.toAscii("123or123"));
        assertEquals("OR123", UnicodeTranslator.toAscii("OR123"));
        assertEquals("or123", UnicodeTranslator.toAscii("or123"));

        assertEquals("_OR", UnicodeTranslator.toAscii("_OR"));
        assertEquals("_or", UnicodeTranslator.toAscii("_or"));
        assertEquals("_OR_", UnicodeTranslator.toAscii("_OR_"));
        assertEquals("_or_", UnicodeTranslator.toAscii("_or_"));
        assertEquals("OR_", UnicodeTranslator.toAscii("OR_"));
        assertEquals("or_", UnicodeTranslator.toAscii("or_"));
	}

	@Test
	public void VarPOW() {
        assertEquals("varPOW", UnicodeTranslator.toAscii("varPOW"));
        assertEquals("varpow", UnicodeTranslator.toAscii("varpow"));
        assertEquals("varPOWvar", UnicodeTranslator.toAscii("varPOWvar"));
        assertEquals("varpowvar", UnicodeTranslator.toAscii("varpowvar"));
        assertEquals("POWvar", UnicodeTranslator.toAscii("POWvar"));
        assertEquals("powvar", UnicodeTranslator.toAscii("powvar"));

        assertEquals("123POW", UnicodeTranslator.toAscii("123POW"));
        assertEquals("123pow", UnicodeTranslator.toAscii("123pow"));
        assertEquals("123POW123", UnicodeTranslator.toAscii("123POW123"));
        assertEquals("123pow123", UnicodeTranslator.toAscii("123pow123"));
        assertEquals("POW123", UnicodeTranslator.toAscii("POW123"));
        assertEquals("pow123", UnicodeTranslator.toAscii("pow123"));

        assertEquals("_POW", UnicodeTranslator.toAscii("_POW"));
        assertEquals("_pow", UnicodeTranslator.toAscii("_pow"));
        assertEquals("_POW_", UnicodeTranslator.toAscii("_POW_"));
        assertEquals("_pow_", UnicodeTranslator.toAscii("_pow_"));
        assertEquals("POW_", UnicodeTranslator.toAscii("POW_"));
        assertEquals("pow_", UnicodeTranslator.toAscii("pow_"));
	}

	@Test
	public void VarPOW1() {
        assertEquals("varPOW1", UnicodeTranslator.toAscii("varPOW1"));
        assertEquals("varpow1", UnicodeTranslator.toAscii("varpow1"));
        assertEquals("varPOW1var", UnicodeTranslator.toAscii("varPOW1var"));
        assertEquals("varpow1var", UnicodeTranslator.toAscii("varpow1var"));
        assertEquals("POW1var", UnicodeTranslator.toAscii("POW1var"));
        assertEquals("pow1var", UnicodeTranslator.toAscii("pow1var"));

        assertEquals("123POW1", UnicodeTranslator.toAscii("123POW1"));
        assertEquals("123pow1", UnicodeTranslator.toAscii("123pow1"));
        assertEquals("123POW1123", UnicodeTranslator.toAscii("123POW1123"));
        assertEquals("123pow1123", UnicodeTranslator.toAscii("123pow1123"));
        assertEquals("POW1123", UnicodeTranslator.toAscii("POW1123"));
        assertEquals("pow1123", UnicodeTranslator.toAscii("pow1123"));

        assertEquals("_POW1", UnicodeTranslator.toAscii("_POW1"));
        assertEquals("_pow1", UnicodeTranslator.toAscii("_pow1"));
        assertEquals("_POW1_", UnicodeTranslator.toAscii("_POW1_"));
        assertEquals("_pow1_", UnicodeTranslator.toAscii("_pow1_"));
        assertEquals("POW1_", UnicodeTranslator.toAscii("POW1_"));
        assertEquals("pow1_", UnicodeTranslator.toAscii("pow1_"));
	}

	@Test
	public void VarTRUE() {
        assertEquals("varTRUE", UnicodeTranslator.toAscii("varTRUE"));
        assertEquals("vartrue", UnicodeTranslator.toAscii("vartrue"));
        assertEquals("varTRUEvar", UnicodeTranslator.toAscii("varTRUEvar"));
        assertEquals("vartruevar", UnicodeTranslator.toAscii("vartruevar"));
        assertEquals("TRUEvar", UnicodeTranslator.toAscii("TRUEvar"));
        assertEquals("truevar", UnicodeTranslator.toAscii("truevar"));

        assertEquals("123TRUE", UnicodeTranslator.toAscii("123TRUE"));
        assertEquals("123true", UnicodeTranslator.toAscii("123true"));
        assertEquals("123TRUE123", UnicodeTranslator.toAscii("123TRUE123"));
        assertEquals("123true123", UnicodeTranslator.toAscii("123true123"));
        assertEquals("TRUE123", UnicodeTranslator.toAscii("TRUE123"));
        assertEquals("true123", UnicodeTranslator.toAscii("true123"));

        assertEquals("_TRUE", UnicodeTranslator.toAscii("_TRUE"));
        assertEquals("_true", UnicodeTranslator.toAscii("_true"));
        assertEquals("_TRUE_", UnicodeTranslator.toAscii("_TRUE_"));
        assertEquals("_true_", UnicodeTranslator.toAscii("_true_"));
        assertEquals("TRUE_", UnicodeTranslator.toAscii("TRUE_"));
        assertEquals("true_", UnicodeTranslator.toAscii("true_"));
	}

	@Test
	public void VarUNION() {
        assertEquals("varUNION", UnicodeTranslator.toAscii("varUNION"));
        assertEquals("varunion", UnicodeTranslator.toAscii("varunion"));
        assertEquals("varUNIONvar", UnicodeTranslator.toAscii("varUNIONvar"));
        assertEquals("varunionvar", UnicodeTranslator.toAscii("varunionvar"));
        assertEquals("UNIONvar", UnicodeTranslator.toAscii("UNIONvar"));
        assertEquals("unionvar", UnicodeTranslator.toAscii("unionvar"));

        assertEquals("123UNION", UnicodeTranslator.toAscii("123UNION"));
        assertEquals("123union", UnicodeTranslator.toAscii("123union"));
        assertEquals("123UNION123", UnicodeTranslator.toAscii("123UNION123"));
        assertEquals("123union123", UnicodeTranslator.toAscii("123union123"));
        assertEquals("UNION123", UnicodeTranslator.toAscii("UNION123"));
        assertEquals("union123", UnicodeTranslator.toAscii("union123"));

        assertEquals("_UNION", UnicodeTranslator.toAscii("_UNION"));
        assertEquals("_union", UnicodeTranslator.toAscii("_union"));
        assertEquals("_UNION_", UnicodeTranslator.toAscii("_UNION_"));
        assertEquals("_union_", UnicodeTranslator.toAscii("_union_"));
        assertEquals("UNION_", UnicodeTranslator.toAscii("UNION_"));
        assertEquals("union_", UnicodeTranslator.toAscii("union_"));
	}

	/*
	 * Problems: UnicodeTranslator.toAscii("p ; q") makes "p , q"
	 * ForwardComposition() UnicodeTranslator.toAscii("G ; H") makes "G , H"
	 * SequentialSubstitution() java.io.IOException: Pushback buffer overflow
	 * LeftOverriding(), PrependElement(), AppendElement()
	 * de.prob.unicode.lexer.LexerException: [1,4] Unknown token: _ Var_123(),
	 * etc.
	 */
}
