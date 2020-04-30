package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.visualisation.PreParserASTPrinter;
import de.be4.classicalb.core.preparser.lexer.LexerException;
import de.be4.classicalb.core.preparser.parser.Parser;
import de.be4.classicalb.core.preparser.parser.ParserException;
import org.junit.Before;
import org.junit.Test;
import util.Helpers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;

public class MultipleRecordsAssignment {

	BParser parser ;

	@Before
	public void setUp() throws Exception {
		parser = new BParser("testcase");
	}

	private de.be4.classicalb.core.parser.node.Start getAst(final String testMachine) throws BCompoundException {
		// System.out.println("Testing \"" + testMachine + "\"");
		final Start startNode = parser.parse(testMachine, false);

		// startNode.apply(new ASTPrinter());
		return startNode;
	}


	@Test
	public void testSimple1() throws Exception {
		//final String testMachine =  "#SUBSTITUTION IF x < 3 THEN skip ELSIF 1=1 THEN skip ELSE skip END";
		final String testMachine1 = "#SUBSTITUTION a := xx'aa'bb  ";
	//	final String testMachine1 = "#SUBSTITUTION xx(5) := 4 ";
	//	final String testMachine = "#SUBSTITUTION xx'aa := 4 ";

		final String testMachine = "#SUBSTITUTION xx'aa'bb := 4 ";
		final Start result = getAst(testMachine);
		final String result2 = Helpers.getMachineAsPrologTerm(testMachine1);

		final String result1 = Helpers.getMachineAsPrologTerm(testMachine);
		System.out.println(result1);
		System.out.println(result2);
	}

}
