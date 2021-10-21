package util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;

public abstract class AbstractParseMachineTest {
	

	private static final class MachineFilenameFilter implements FilenameFilter {
		private static final String[] MACHINE_SUFFIX = { ".mch", ".imp", ".ref", ".def" };

		public boolean accept(final File dir, final String name) {
			for (int i = 0; i < MACHINE_SUFFIX.length; i++) {
				if (name.endsWith(MACHINE_SUFFIX[i])) {
					return true;
				}
			}
			return false;
		}
	}

	protected static File[] getMachines(String path) throws URISyntaxException {
		final File dir = new File(AbstractParseMachineTest.class.getClassLoader().getResource(path).toURI());
		return dir.listFiles(new MachineFilenameFilter());
	}

	protected static PolySuite.Configuration buildConfig(String path) {
		final File[] machines;
		try {
			machines = getMachines(path);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return new PolySuite.Configuration() {

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
