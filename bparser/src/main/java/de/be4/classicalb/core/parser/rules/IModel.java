package de.be4.classicalb.core.parser.rules;

import java.util.List;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.MachineReference;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.prolog.output.IPrologTermOutput;

public abstract class IModel {

	private String machineName = "";
	private ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
	private Start start;

	IModel() {}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getMachineName(){
		return machineName;
	}

	public void setParsingBehaviour(ParsingBehaviour parsingBehaviour) {
		this.parsingBehaviour = parsingBehaviour;
	}

	public void setStart(Start start) {
		this.start = start;
	}

	public Start getStart() {
		return start;
	}

	public abstract List<MachineReference> getMachineReferences();

	public void printAsProlog(final IPrologTermOutput pout, INodeIds nodeIdMapping) {
		this.printAsPrologWithFullstops(pout, nodeIdMapping, true);
	}

	public void printAsPrologDirect(final IPrologTermOutput pout, INodeIds nodeIdMapping) {
		this.printAsPrologWithFullstops(pout, nodeIdMapping, false);
	}

	public void printAsPrologWithFullstops(final IPrologTermOutput pout, INodeIds nodeIdMapping, boolean withFullstops) {
		assert start != null;
		final ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIdMapping);
		pprinter.setPrintSourcePositions(parsingBehaviour.isAddLineNumbers(), parsingBehaviour.isCompactPrologPositions());
		final ASTProlog prolog = new ASTProlog(pout, pprinter);
		pout.openTerm("machine");
		start.apply(prolog);
		pout.closeTerm();
		if (withFullstops) {
			pout.fullstop();
		} else {
			pout.flush();
		}
	}

	public abstract String getPath();

	public abstract boolean hasError();

	public abstract BCompoundException getCompoundException();
	
}
