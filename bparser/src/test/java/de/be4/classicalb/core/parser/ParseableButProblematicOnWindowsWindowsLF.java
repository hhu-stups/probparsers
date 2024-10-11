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

@RunWith(Parameterized.class)
public class ParseableButProblematicOnWindowsWindowsLF {

	private static final String PATH = "problematicOnWindows";

	private final File machine;

	public ParseableButProblematicOnWindowsWindowsLF(File machine) {
		this.machine = machine;
	}

	@Test
	public void testParsable() throws Exception {
		File windowsMachine = File.createTempFile(machine.getName().replace(".mch", "_win"), ".mch");

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
	}

	@Parameterized.Parameters(name = "{0}")
	public static File[] data() throws IOException {
		return Helpers.getMachines(PATH);
	}

}
