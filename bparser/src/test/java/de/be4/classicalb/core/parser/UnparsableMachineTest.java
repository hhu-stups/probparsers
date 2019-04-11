package de.be4.classicalb.core.parser;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;

import util.AbstractParseMachineTest;
import util.PolySuite;
import util.PolySuite.Config;
import util.PolySuite.Configuration;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;

@RunWith(PolySuite.class)
public class UnparsableMachineTest extends AbstractParseMachineTest {

	private static final String PATH = "unparsable";

	private final File machine;

	public UnparsableMachineTest(File machine) {
		this.machine = machine;
	}

	@Test(expected = BCompoundException.class)
	public void testParsable() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine, false);
		assertNotNull(start);
	}

	@Config
	public static Configuration getConfig() {
		return buildConfig(PATH);
	}

}
