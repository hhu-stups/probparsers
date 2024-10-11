package de.be4.classicalb.core.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
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
public class ParseableButProblematicOnWindowsUnixLF {

	private static final String PATH = "problematicOnWindows";

	private final File machine;

	public ParseableButProblematicOnWindowsUnixLF(File machine) {
		this.machine = machine;
	}

	@Test
	public void testParsable() throws Exception {
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

	@Parameterized.Parameters(name = "{0}")
	public static File[] data() throws IOException {
		return Helpers.getMachines(PATH);
	}

}
