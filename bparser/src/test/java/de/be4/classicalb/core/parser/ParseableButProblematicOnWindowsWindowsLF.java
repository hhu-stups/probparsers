package de.be4.classicalb.core.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
			unixMachines[i] = File.createTempFile(machines[i].getName().replace(".mch", "_win"), ".mch");

			try (FileReader in2 = new FileReader(machines[i])) {
				BufferedReader in = new BufferedReader(in2);
				BufferedWriter out = new BufferedWriter(new FileWriter(unixMachines[i]));

				String zeile;
				while ((zeile = in.readLine()) != null) {
					out.write(zeile + "\r\n");
				}

				in.close();
				out.close();
			}

		}

		return unixMachines;
	}

}
