package de.be4.classicalb.core.parser;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;

import util.AbstractParseMachineTest;
import util.PolySuite;
import util.PolySuite.Config;
import util.PolySuite.Configuration;
import de.be4.classicalb.core.parser.node.Start;
import util.PositionTester;

@RunWith(PolySuite.class)
public class ParseableButProblematicOnWindowsWindowsLF extends AbstractParseMachineTest {

	private static final String PATH = "problematicOnWindows";

	private final File machine;

	public ParseableButProblematicOnWindowsWindowsLF(File machine) {
		this.machine = machine;
	}

	@Test
	public void testParsable() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine, false);
		start.apply(new PositionTester());
		assertNotNull(start);
	}

	@Config
	public static Configuration getConfig() throws IOException {
		final File[] machines;
		try {
			machines = getMachines(PATH);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
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

		return new Configuration() {

			public int size() {
				return unixMachines.length;
			}

			public File getTestValue(int index) {
				return unixMachines[index];
			}

			public String getTestName(int index) {
				return unixMachines[index].getName();
			}
		};
	}

}
