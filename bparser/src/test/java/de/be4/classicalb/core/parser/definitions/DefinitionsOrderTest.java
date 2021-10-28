package de.be4.classicalb.core.parser.definitions;

import java.io.IOException;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;

import org.junit.Test;

import util.Helpers;

public class DefinitionsOrderTest {

	private static final String PATH = "definitions/";

	@Test
	public void testLinearOrder() throws IOException, BCompoundException {
		Helpers.parseFile(PATH + "DefinitionsOccurInLinearOrder.mch");
	}

	@Test
	public void testReordered() throws IOException, BCompoundException {
		Helpers.parseFile(PATH + "DefinitionsOccurReordered.mch");
	}
}