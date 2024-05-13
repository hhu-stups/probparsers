package de.be4.classicalb.core.parser.rules;

import java.util.List;

import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.prolog.output.IPrologTermOutput;

public interface IModel {

	String getMachineName();

	List<MachineReference> getMachineReferences();

	void printAsProlog(final IPrologTermOutput pout, INodeIds nodeIdMapping);

	String getPath();

	boolean hasError();

	BCompoundException getCompoundException();

	Start getStart();
	
}
