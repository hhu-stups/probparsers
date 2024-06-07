package de.be4.classicalb.core.parser.analysis.prolog;

import java.util.stream.Stream;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
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
				arguments("rec(b:2,a:1)", "rec([field(a,int(1)),field(b,int(2))])")
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
}
