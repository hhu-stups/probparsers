package de.prob.voparser;

/** 
 * (c) 2022 Lehrstuhl fuer Softwaretechnik und Programmiersprachen,
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

import org.junit.Test;

public class VOScopingTest {

	@Test
	public void testAtomic() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1", VTType.MODEL_CHECKING_INV);
		voParser.scopeCheck("MC1");
	}

	@Test
	public void testSequential() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1", VTType.MODEL_CHECKING_GOAL);
		voParser.registerTask("TR1", VTType.TRACE_REPLAY);
		voParser.scopeCheck("MC1;TR1");
	}

	@Test(expected = VOParseException.class)
	public void testSequentialError() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1", VTType.MODEL_CHECKING_GOAL);
		voParser.scopeCheck("MC1;TR1");
	}

	@Test
	public void testAnd() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1", VTType.MODEL_CHECKING_GOAL);
		voParser.registerTask("TR1", VTType.TRACE_REPLAY);
		voParser.scopeCheck("MC1 & TR1");
	}

	@Test
	public void testOr() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1", VTType.MODEL_CHECKING_GOAL);
		voParser.registerTask("TR1", VTType.TRACE_REPLAY);
		voParser.scopeCheck("MC1 or TR1");
	}

	@Test
	public void testNot() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("TR1", VTType.TRACE_REPLAY);
		voParser.scopeCheck("not TR1");
	}

	@Test
	public void testImplies() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1", VTType.MODEL_CHECKING_INV);
		voParser.registerTask("MC2", VTType.MODEL_CHECKING_INV);
		voParser.scopeCheck("MC1 => MC2");
	}

	@Test
	public void testEquivalent() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1", VTType.MODEL_CHECKING_INV);
		voParser.registerTask("MC2", VTType.MODEL_CHECKING_INV);
		voParser.scopeCheck("MC1 <=> MC2");
	}

	@Test
	public void testDot() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.registerTask("MC1.1", VTType.MODEL_CHECKING_INV);
		voParser.scopeCheck("MC1.1");
	}


	@Test(expected = VOParseException.class)
	public void testScopingError() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.scopeCheck("MC1");
	}

}
