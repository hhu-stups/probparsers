package de.be4.classicalb.core.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;

import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;
import util.PositionTester;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ParseableButProblematicOnWindows {

	private static final String PATH = "problematicOnWindows";

	private final File machine;

	public ParseableButProblematicOnWindows(File machine) {
		this.machine = machine;
	}

	@Parameterized.Parameters(name = "{0}")
	public static File[] data() {
		return Helpers.getMachines(PATH);
	}

	@Test
	public void parsableWithOriginalLineSeparators() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine);
		start.apply(new PositionTester());
		assertNotNull(start);
	}

	@Test
	public void parsableWithUnixLineSeparators() throws Exception {
		File unixMachine = File.createTempFile(machine.getName().replace(".mch", "_unix"), ".mch");

		try {
			try (
				BufferedReader in = Files.newBufferedReader(machine.toPath());
				BufferedWriter out = Files.newBufferedWriter(unixMachine.toPath());
			) {
				String zeile;
				while ((zeile = in.readLine()) != null) {
					out.write(zeile + "\n");
				}
			}

			final BParser parser = new BParser(unixMachine.getName());
			Start start = parser.parseFile(unixMachine);
			start.apply(new PositionTester());
			assertNotNull(start);
		} finally {
			boolean success = unixMachine.delete();
			assertTrue("Failed to delete temporary machine file", success);
		}
	}

	@Test
	public void parsableWithWindowsLineSeparators() throws Exception {
		File windowsMachine = File.createTempFile(machine.getName().replace(".mch", "_win"), ".mch");

		try {
			try (
				BufferedReader in = Files.newBufferedReader(machine.toPath());
				BufferedWriter out = Files.newBufferedWriter(windowsMachine.toPath());
			) {
				String zeile;
				while ((zeile = in.readLine()) != null) {
					out.write(zeile + "\r\n");
				}
			}

			final BParser parser = new BParser(windowsMachine.getName());
			Start start = parser.parseFile(windowsMachine);
			start.apply(new PositionTester());
			assertNotNull(start);
		} finally {
			boolean success = windowsMachine.delete();
			assertTrue("Failed to delete temporary machine file", success);
		}
	}
}
