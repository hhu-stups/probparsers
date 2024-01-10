package de.be4.classicalb.core.parser.prettyprinter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.util.Utils;

import org.junit.Test;

import util.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PrettyMachinePrinterTest {

	@Test
	public void testPrettyPrint() {
		final String testMachine = "MACHINE Test\n" +
			"VARIABLES x, y\n" +
			"INVARIANT x:INTEGER & y:INTEGER\n" +
			"INITIALISATION x := 1 || y := 2\n" +
			"OPERATIONS\n" +
			"foo = skip;\n" +
			"bar = skip\n" +
			"END";
		final String result1 = Helpers.getPrettyPrint(testMachine);

		assertFalse(result1.isEmpty());
		assertEquals(testMachine, result1);
	}

	@Test
	public void testPrettyPrint_Indent() {
		final String testMachine = "MACHINE Test\n" +
			"    VARIABLES\n" +
			"        x,\n" +
			"        y\n" +
			"    INVARIANT\n" +
			"        x:INTEGER & y:INTEGER\n" +
			"    INITIALISATION\n" +
			"        x := 1\n" +
			"        || y := 2\n" +
			"    OPERATIONS\n" +
			"        foo =\n" +
			"            skip;\n" +
			"        bar =\n" +
			"            skip\n" +
			"END";
		final String result1 = Helpers.getPrettyPrintWithIndentation(testMachine);

		assertFalse(result1.isEmpty());
		assertEquals(testMachine, result1);
	}

	@Test
	public void testPrettyPrint2() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrint(testMachine);
		final String result2 = Helpers.getPrettyPrint(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrint2_Indent() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrintWithIndentation(testMachine);
		final String result2 = Helpers.getPrettyPrintWithIndentation(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrint3() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter2.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrint(testMachine);
		final String result2 = Helpers.getPrettyPrint(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrint3_Indent() throws IOException, URISyntaxException {
		final URI uri = this.getClass()
			.getResource("/prettyprinter/PrettyPrinter2.mch")
			.toURI();
		final File file = new File(uri);
		final String testMachine = Utils.readFile(file);
		final String result1 = Helpers.getPrettyPrintWithIndentation(testMachine);
		final String result2 = Helpers.getPrettyPrintWithIndentation(result1);

		assertFalse(result1.isEmpty());
		assertFalse(result2.isEmpty());
		assertEquals(result1, result2);
	}

	@Test
	public void testPrettyPrintGenerated() {
		String testMachine = "/*@generated*/\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint("/*@generated*/ MACHINE Test\nEND"));
	}

	@Test
	public void testPrettyPrintPackage() {
		String testMachine = "/*@package one.two */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}

	@Test
	public void testPrettyPrintPackageString() {
		String testMachine = "/*@package \"one two\" */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}

	@Test
	public void testPrettyPrintPackageImport() {
		String testMachine = "/*@package one.two */\n/*@import-package one.three */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}

	@Test
	public void testPrettyPrintPackageImports() {
		String testMachine = "/*@package one.two */\n/*@import-package one.three */\n/*@import-package \"one.two three\" */\nMACHINE Test\nEND";
		assertEquals(testMachine, Helpers.getPrettyPrint(testMachine));
	}
}
