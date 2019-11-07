package de.be4.classicalb.core.parser.byteordermark;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;

import util.AbstractParseMachineTest;
import util.PolySuite;
import util.PolySuite.Config;
import util.PolySuite.Configuration;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.node.Start;
import util.PositionTester;

/**
 * @author bendisposto
 *
 */
/**
 * @author bendisposto
 * 
 */
@RunWith(PolySuite.class)
public class ByteOrderMarkTests extends AbstractParseMachineTest {

	private static final String PATH = "byteOrderMark";

	private final File machine;

	public ByteOrderMarkTests(File machine) {
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
		return buildConfig(PATH);
	}

}
