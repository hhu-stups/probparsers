package de.be4.classicalb.core.parser.definitions;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class DefinitionsErrorsTest {

	@Test
	public void checkForInvalidSubstitution() {
		String s = "MACHINE Definitions \n DEFINITIONS \n foo == BEGIN\n x=1 END \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[4,3]"));
	}

	@Test
	public void checkAtSymbolInDefinitions() {
		String s = "MACHINE Definitions \n DEFINITIONS \n foo == BEGIN\n @ END \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[4,2]"));
	}

	@Test
	public void checkForInvalidExpression() {
		String s = "MACHINE Definitions \n DEFINITIONS \n foo == 1 + \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		assertTrue(e.getMessage().contains("[4,1]"));
	}
	
	@Test
	public void checkForErrorPositionInDefinitionWithMultilineComments() {
		String s = "MACHINE Definitions \n DEFINITIONS \n foo == 1=1\n /* \n comment\n comment2\n comment3 \n */\n&& 1=1 \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		assertTrue(e.getMessage().contains("[9,2]"));
	}

	@Test
	public void checkForInvalidDefinition() {
		String s = "MACHINE Definitions \n DEFINITIONS \n foo == BEING x :=1 END \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[3,15]"));
		assertTrue(e.getMessage().contains("expecting end of definition")
			// BEING x can be detected as being illegal combination
			|| e.getMessage().contains("Invalid combination of symbols"));
	}

	@Test
	public void checkForInvalidFormula() {
		String s = "MACHINE Definitions \n DEFINITIONS\n foo == \n 1+; \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[4,4]"));
	}

	@Test
	public void checkForInvalidFormula2() {
		String s = "MACHINE Definitions \n DEFINITIONS\n foo == \n 1=; \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[4,4]"));
	}

	@Test
	public void checkForInvalidFormula3() {
		String s = "MACHINE Definitions \n DEFINITIONS\n foo(xx) == (xx : OBJECTS -->(1..card(OBJECTS))\n; \nEND";
		final BCompoundException e = assertThrows(BCompoundException.class, () -> parseString(s));
		// there is no token available, hence the position is in the text
		assertTrue(e.getMessage().contains("[4,1]"));
		assertTrue(e.getMessage().contains("expecting: ')'"));
	}

	private void parseString(final String testMachine) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parse(testMachine, false);
	}
}
