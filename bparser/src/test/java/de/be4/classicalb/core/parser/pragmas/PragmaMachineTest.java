package de.be4.classicalb.core.parser.pragmas;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;

import util.AbstractParseMachineTest;
import util.PolySuite;
import util.PolySuite.Config;
import util.PolySuite.Configuration;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import util.PositionTester;

@RunWith(PolySuite.class)
public class PragmaMachineTest extends AbstractParseMachineTest {

	private static final String PATH = "pragmas";

	private final File machine;

	public PragmaMachineTest(File machine) {
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
	public static Configuration getConfig() {
		final File[] machines;
		try {
			machines = getMachines(PATH);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return new Configuration() {

			public int size() {
				return machines.length;
			}

			public File getTestValue(int index) {
				return machines[index];
			}

			public String getTestName(int index) {
				return machines[index].getName();
			}
		};
	}

}
