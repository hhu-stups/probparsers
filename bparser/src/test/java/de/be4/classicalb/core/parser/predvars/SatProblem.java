package de.be4.classicalb.core.parser.predvars;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.Definitions;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.Utils;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SatProblem {
	@Test
	public void compareSatPredAndPredVars() throws URISyntaxException, IOException, BCompoundException, LexerException {
		final File f1 = new File(this.getClass().getClassLoader().getResource("predvars/sat_predvars").toURI());
		final File f2 = new File(this.getClass().getClassLoader().getResource("predvars/sat_pred").toURI());

		final String test = "#PREDICATE" + Utils.readFile(f1);
		final String reference = "#PREDICATE" + Utils.readFile(f2);

		final String result1 = getMachineAsPrologTermEparse(test);
		final String result2 = Helpers.getMachineAsPrologTerm(reference);

		assertNotNull(result1);
		assertNotNull(result2);
		assertEquals(result1, result2);
	}

	private static String getMachineAsPrologTermEparse(final String testMachine) throws BCompoundException,
			LexerException, IOException {
		final BParser parser = new BParser("testcase");
		Start ast = parser.eparse(testMachine, new Definitions());
		return Helpers.getTreeAsPrologTerm(ast);
	}
}
