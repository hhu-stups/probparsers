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

	public UnparsableMachineTest(File machine) {
		this.machine = machine;
	}

	@Parameterized.Parameters(name = "{0}")
	public static File[] data() {
		return Helpers.getMachines(PATH);
	}

	@Test(expected = BCompoundException.class)
	public void testParsable() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine, false);
		assertNotNull(start);
	}
}
