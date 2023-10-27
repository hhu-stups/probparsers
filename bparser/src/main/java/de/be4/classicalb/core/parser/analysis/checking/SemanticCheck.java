package de.be4.classicalb.core.parser.analysis.checking;

import java.util.List;

import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.node.Start;

/**
 * A common subclass for semantic checks
 */
public interface SemanticCheck {
	void runChecks(Start rootNode);

	List<CheckException> getCheckExceptions();
}
