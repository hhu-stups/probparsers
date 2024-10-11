package de.be4.classicalb.core.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;
import util.PositionTester;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class ParsableButProblematicOnWindowsTest {

	private static final String PATH = "problematicOnWindows";

	private final File machine;

	public ParsableButProblematicOnWindowsTest(File machine) {
		this.machine = machine;
	}

	private static void convertLineSeparators(File original, File destination, String lineSeparator) throws IOException {
		try (
			BufferedReader in = Files.newBufferedReader(original.toPath());
			BufferedWriter out = Files.newBufferedWriter(destination.toPath());
		) {
			String line;
			while ((line = in.readLine()) != null) {
				out.write(line + lineSeparator);
			}
		}
	}

	private static void assertParsable(File machine) throws BCompoundException {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine);
		start.apply(new PositionTester());
		assertNotNull(start);
	}

	@Parameterized.Parameters(name = "{0}")
	public static File[] data() {
		return Helpers.getMachines(PATH);
	}

	@Test
	public void parsableWithOriginalLineSeparators() throws BCompoundException {
		assertParsable(machine);
	}

	@Test
	public void parsableWithUnixLineSeparators() throws BCompoundException, IOException {
		File unixMachine = File.createTempFile(machine.getName().replace(".mch", "_unix"), ".mch");

		try {
			convertLineSeparators(machine, unixMachine, "\n");
			assertParsable(unixMachine);
		} finally {
			boolean success = unixMachine.delete();
			assertTrue("Failed to delete temporary machine file", success);
		}
	}

	@Test
	public void parsableWithWindowsLineSeparators() throws BCompoundException, IOException {
		File windowsMachine = File.createTempFile(machine.getName().replace(".mch", "_win"), ".mch");

		try {
			convertLineSeparators(machine, windowsMachine, "\r\n");
			assertParsable(windowsMachine);
		} finally {
			boolean success = windowsMachine.delete();
			assertTrue("Failed to delete temporary machine file", success);
		}
	}
}
