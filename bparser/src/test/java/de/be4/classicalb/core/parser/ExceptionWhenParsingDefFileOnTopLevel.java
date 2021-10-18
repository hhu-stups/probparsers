package de.be4.classicalb.core.parser;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import de.be4.classicalb.core.parser.exceptions.BException;

public class ExceptionWhenParsingDefFileOnTopLevel {

	private static final String PATH = "LibraryIO.def";
	private PrintStream out;
	private ByteArrayOutputStream baos;
	private File machine;
	private ParsingBehaviour behaviour;

	@Before
	public void before() {
		baos = new ByteArrayOutputStream();
		out = new PrintStream(baos);

		behaviour = new ParsingBehaviour();
		behaviour.setPrologOutput(true);
	}

	@Test
	public void testExceptionCaughtAndPrintedToProlog() throws URISyntaxException {
		machine = new File(this.getClass().getClassLoader().getResource(PATH).toURI());

		final BParser parser = new BParser(machine.getName());
		int returnValue = parser.fullParsing(machine, behaviour, out, out);
        
        // We now permit to load .def files at the top-level; ProB converts them to a virtual abstract_machine
		assertEquals(returnValue, 0); // was -3
		//assertEquals("exception('Expecting a B machine but was a definition file in file: \\'LibraryIO.def\\'').",
		//		baos.toString().trim());
	}
}