package util;

import java.io.File;
import java.net.URISyntaxException;
import java.util.stream.Stream;

public abstract class AbstractParseMachineTest {
	protected static File[] getMachines(String path) {
		final File dir;
		try {
			dir = new File(AbstractParseMachineTest.class.getResource("/" + path).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return dir.listFiles((d, name) -> Stream.of(".mch", ".imp", ".ref", ".def").anyMatch(name::endsWith));
	}
}
