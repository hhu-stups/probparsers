package de.be4.classicalb.core.parser.byteordermark;

import java.io.File;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import util.AbstractParseMachineTest;
import util.PositionTester;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class ByteOrderMarkTests extends AbstractParseMachineTest {

	private static final String PATH = "byteOrderMark";

	private final File machine;

	public ByteOrderMarkTests(File machine) {
		this.machine = machine;
	}

	@Parameterized.Parameters(name = "{0}")
	public static File[] data() {
		return getMachines(PATH);
	}

	@Test
	public void testParsable() throws Exception {
		final BParser parser = new BParser(machine.getName());
		Start start = parser.parseFile(machine, false);
		start.apply(new PositionTester());
		assertNotNull(start);
	}
}
