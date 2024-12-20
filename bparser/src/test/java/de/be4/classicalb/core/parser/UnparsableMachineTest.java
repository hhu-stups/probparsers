package de.be4.classicalb.core.parser;

import java.io.File;

import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.Helpers;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class UnparsableMachineTest {

	private static final String PATH = "unparsable";

	private final File machine;

	public UnparsableMachineTest(File machine, String ignoredRelativePath) {
		this.machine = machine;
	}

	@Parameterized.Parameters(name = "{1}")
	public static Object[][] data() {
		return Helpers.getMachinesForTestData(PATH);
	}

	@Test(expected = BCompoundException.class)
	public void testParsable() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine);
		assertNotNull(start);
	}
}
