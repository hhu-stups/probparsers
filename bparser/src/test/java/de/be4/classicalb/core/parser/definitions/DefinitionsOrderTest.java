package de.be4.classicalb.core.parser.definitions;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

public class DefinitionsOrderTest {

	private static final String PATH = "definitions/";

	private File machine;

	@Test
	public void testLinearOrder() throws IOException, BCompoundException, URISyntaxException {
		machine = new File(this.getClass().getClassLoader().getResource(PATH + "DefinitionsOccurInLinearOrder.mch").toURI());

		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine, false);
		assertNotNull(start);
	}

	@Test
	public void testReordered() throws IOException, BCompoundException, URISyntaxException {
		machine = new File(this.getClass().getClassLoader().getResource(PATH + "DefinitionsOccurReordered.mch").toURI());

		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine, false);
		assertNotNull(start);
	}
}