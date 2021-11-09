package de.be4.eventbalg.parser;

import de.be4.eventbalg.core.parser.BException;
import de.be4.eventbalg.core.parser.EventBParser;
import de.be4.eventbalg.core.parser.node.Start;

public class AbstractTest {
	protected Start parseInput(final String input, final boolean debugOutput)
			throws BException {
		if (debugOutput) {
			System.out.println();
			System.out.println();
		}

		final EventBParser parser = new EventBParser();
		return parser.parse(input, debugOutput);
	}
}
