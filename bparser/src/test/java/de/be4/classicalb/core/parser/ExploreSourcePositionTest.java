package de.be4.classicalb.core.parser;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

public class ExploreSourcePositionTest {

	@Test
	public void test() throws BCompoundException, IOException {
		Helpers.parseFile("LabelTest.mch");
	}

}
