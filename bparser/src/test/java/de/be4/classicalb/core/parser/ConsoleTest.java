package de.be4.classicalb.core.parser;

import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConsoleTest {

	@Test
	public void testFormulaOutput() throws BCompoundException {
		Start start = new BParser().parseFormula("1+1");
		PrologTermStringOutput strOutput = new PrologTermStringOutput();
		ASTProlog printer = new ASTProlog(strOutput, null);
		start.apply(printer);
		strOutput.fullstop();

		// A Friendly Reminder: strOutput includes a newline!
		String output = strOutput.toString().trim();
		assertEquals(output, "add(none,integer(none,1),integer(none,1)).");
	}

}
