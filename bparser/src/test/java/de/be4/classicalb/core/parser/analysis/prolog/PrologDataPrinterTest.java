package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.*;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class PrologDataPrinterTest {

	static Stream<Arguments> simpleDataProvider() {
		return Stream.of(
				arguments("42", "int(42)"),
				// TODO: arguments("-42", "int(-42)"), minus is not part of the literal!
				arguments("1.337", "term(floating(1.337))"),
				arguments("TRUE", "pred_true"),
				arguments("FALSE", "pred_false"),
				arguments("\"foobar\"", "string(foobar)"),
				arguments("(1,2)", "','(int(1),int(2))"),
				arguments("(1,2,3)", "','(int(1),int(2),int(3))"), // TODO: is this correct?
				arguments("{}", "[]"),
				arguments("{1,2}", "[int(1),int(2)]"),
				arguments("[]", "[]"),
				arguments("[1,2]", "[','(int(1),int(1)),','(int(2),int(2))]"),
				arguments("rec(b:2,a:1)", "rec([field(a,int(1)),field(b,int(2))])"),
				arguments("rec(d:7,a:rec(b:5),c:rec(e:rec(f:0)))", "rec([field(a,rec([field(b,int(5))])),field(c,rec([field(e,rec([field(f,int(0))]))])),field(d,int(7))])")
		);
	}

	static Stream<Arguments> enumSetsAndFreetypesDataProvider() {
		List<PExpression> elems = Stream.of("elem1","elem2","elem3").map(e ->
			new AIdentifierExpression(Collections.singletonList(new TIdentifierLiteral(e)))).collect(Collectors.toList());
		AEnumeratedSetSet enumSet = new AEnumeratedSetSet(Collections.singletonList(new TIdentifierLiteral("Elems")), elems);
		ASetsMachineClause setClause = new ASetsMachineClause(Collections.singletonList(enumSet));

		List<PFreetypeConstructor> constructors = Arrays.asList(
			new AConstructorFreetypeConstructor(new TIdentifierLiteral("cons1"), new AStringSetExpression()),
			new AConstructorFreetypeConstructor(new TIdentifierLiteral("cons2"), new AIntegerSetExpression()),
			new AElementFreetypeConstructor(new TIdentifierLiteral("cons3")));
		AFreetype freetype = new AFreetype(new TIdentifierLiteral("Constructors"), new ArrayList<>(), constructors);
		AFreetypesMachineClause ftClause = new AFreetypesMachineClause(Collections.singletonList(freetype));

		return Stream.of(
			arguments("{elem1,elem3,elem2}", "[fd(1,'Elems'),fd(3,'Elems'),fd(2,'Elems')]", setClause, ftClause),
			arguments("{cons2(4),cons1(\"test\"),cons3}", "[freeval('Constructors',cons2,int(4)),freeval('Constructors',cons1,string(test)),freeval('Constructors',cons3,term(cons3))]", setClause, ftClause)
		);
	}

	@ParameterizedTest
	@MethodSource("simpleDataProvider")
	void testSimpleData(String input, String expected) throws BCompoundException {
		Start ast = new BParser().parseExpression(input);
		IPrologTermOutput pto = new PrologTermStringOutput();
		ast.apply(new PrologDataPrinter(pto));
		String actual = pto.toString();
		Assertions.assertEquals(expected, actual);
	}

	@ParameterizedTest
	@MethodSource("enumSetsAndFreetypesDataProvider")
	void testEnumSetsAndFreetypes(String input, String expected, ASetsMachineClause setsMachineClause, AFreetypesMachineClause freetypesMachineClause) throws BCompoundException {
		Start ast = new BParser().parseExpression(input);
		IPrologTermOutput pto = new PrologTermStringOutput();
		ast.apply(new PrologDataPrinter(pto, setsMachineClause, freetypesMachineClause));
		String actual = pto.toString();
		Assertions.assertEquals(expected, actual);
	}
}
