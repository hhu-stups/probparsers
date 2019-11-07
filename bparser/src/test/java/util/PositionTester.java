package util;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.Start;

import static org.junit.Assert.assertNotNull;

/**
 * Visitor that checks if all AST nodes contain the position information.
 * @author bendisposto
 */
public class PositionTester extends DepthFirstAdapter {
	@Override
	public void defaultIn(final Node node) {
		if (node instanceof Start) return; // start does not have position infos
		assertNotNull(node.getClass().getSimpleName() + " start was null",
				node.getStartPos());
		assertNotNull(node.getClass().getSimpleName() + " end was null",
				node.getEndPos());
	}
}
