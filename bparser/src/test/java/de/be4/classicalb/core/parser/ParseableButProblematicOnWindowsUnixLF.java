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
public class ParseableButProblematicOnWindowsUnixLF {

	private static final String PATH = "problematicOnWindows";

	private final File machine;

	public ParseableButProblematicOnWindowsUnixLF(File machine) {
		this.machine = machine;
	}

	@Test
	public void testParsable() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine);
		start.apply(new PositionTester());
		assertNotNull(start);
	}

	@Parameterized.Parameters(name = "{0}")
	public static File[] data() throws IOException {
		final File[] machines = Helpers.getMachines(PATH);
		final File[] unixMachines = new File[machines.length];

		for (int i = 0; i < machines.length; i++) {
			unixMachines[i] = File.createTempFile(machines[i].getName().replace(".mch", "_unix"), ".mch");

			try (
				BufferedReader in = Files.newBufferedReader(machines[i].toPath());
				BufferedWriter out = Files.newBufferedWriter(unixMachines[i].toPath());
			) {
				String zeile;
				while ((zeile = in.readLine()) != null) {
					out.write(zeile + "\n");
				}
			}
		}

		return unixMachines;
	}

}
