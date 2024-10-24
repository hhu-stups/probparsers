package de.be4.classicalb.core.parser;

import java.io.File;

import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;
import util.PositionTester;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ParsableMachineTest {

	private static final String PATH = "parsable";

	private final File machine;

	public ParsableMachineTest(File machine, String ignoredRelativePath) {
		this.machine = machine;
	}

	@Parameterized.Parameters(name = "{1}")
	public static Object[][] data() {
		return Helpers.getMachinesForTestData(PATH);
	}

	@Test
	public void testParsable() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine);
		start.apply(new PositionTester());
		assertNotNull(start);
	}
}
