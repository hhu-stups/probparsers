package de.be4.classicalb.core.parser.abstractions;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import util.Ast2String;

import static util.Helpers.getTreeAsString;

public class AbstractionTest {


	@Test
	public void testAbstraction() throws BCompoundException {
		final String machine = "ABSTRACTION M_A ABSTRACTS M ABSTRACTED_VARIABLES a, b, c ABSTRACTED_CONSTANTS g,c,d END";
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parse(machine, false);

		AbstractionAnalyzerHelper abstractionAnalyzerHelper = new AbstractionAnalyzerHelper();
		abstractionAnalyzerHelper.caseStart(startNode);

		Assertions.assertTrue(abstractionAnalyzerHelper.abstraction);
		Assertions.assertEquals(3, abstractionAnalyzerHelper.variables);
		Assertions.assertEquals(3, abstractionAnalyzerHelper.constants);

	}


}
