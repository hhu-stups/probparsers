package de.prob.voparser;

/** 
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

import org.junit.Test;

public class VOParserTest {

	@Test
	public void testAtomic() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC1");
	}

	@Test
	public void testSequential() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC1;TR1");
	}

	@Test
	public void testAnd() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC1 & TR1");
	}

	@Test
	public void testOr() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC1 or TR1");
	}

	@Test
	public void testNot() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("not TR1");
	}

	@Test
	public void testImplies() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC1 => MC2");
	}

	@Test
	public void testEquivalent() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC1 <=> MC2");
	}

	@Test
	public void testDot() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC.1");
	}


	@Test(expected = VOParseException.class)
	public void testParseError() throws VOParseException {
		VOParser voParser = new VOParser();
		voParser.parseFormula("MC.1;");
	}

}
