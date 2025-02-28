package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.node.AIfElsifSubstitution;
import de.be4.classicalb.core.parser.node.ASelectWhenSubstitution;
import de.be4.classicalb.core.parser.node.Node;
import de.prob.prolog.output.IPrologTermOutput;

public class ClassicalPositionPrinter implements PositionPrinter {

	private IPrologTermOutput pout;

	// to look up the identifier of each node
	private final INodeIds nodeIds;

	private boolean printSourcePositions = false;
	private boolean compactPositions = false;

	private int lineOffset = 0;
	private int columnOffset = 0;

	private static final int NO_LINE_OR_COLUMN_VALUE = 0;

	public ClassicalPositionPrinter(final INodeIds nodeIds) {
		this.nodeIds = nodeIds;
	}

	public void setPrintSourcePositions(boolean b, boolean compact) {
		this.printSourcePositions = b;
		this.compactPositions = compact;
	}

	private static boolean uselessPositionInfo (final Node node) {
		// return true for those nodes which do not require a position info
		if (node instanceof AIfElsifSubstitution) { // if_elsif infos not used in ProB
			return true;
		} else if (node instanceof ASelectWhenSubstitution) { // select_when infos not used in ProB
			return true;
		}
		return false;
	}

	@Override
	public void printPosition(Node node) {
		this.printPositionRange(node, node);
	}

	@Override
	public void printPositionRange(Node startNode, Node endNode) {
		if (!printSourcePositions || uselessPositionInfo(startNode)) {
			pout.printAtom("none");
		} else {
			// print full source positions
			int fileNr = nodeIds.lookupFileNumber(startNode);
			if (fileNr == -1 && startNode.getStartPos() == null && endNode.getEndPos() == null) {
				// Workaround for errors about overridden main machine name when loading rules projects.
				// The translated AST has no file numbers associated with it,
				// so probcli incorrectly thinks that it comes from a non-main file.
				// See e. g. probcli test 1831 or any of the rules tests in ProB 2.
				// TODO Change rules machine translation to associate the generated AST with the correct file, or adjust the check in probcli?
				pout.printAtom("none");
				return;
			}
			int startLine = getStartLine(startNode);
			int endLine = getEndLine(endNode);
			if (!compactPositions) { // old pos(UniqueID,FileNr,StartLine,StartCol,Endline,EndCol) term
				pout.openTerm("pos", true);
				// Unique node IDs are no longer supported - all nodes get ID -1.
				pout.printNumber(-1);
				pout.printNumber(fileNr);
				pout.printNumber(startLine);
				pout.printNumber(getStartColumn(startNode));
				pout.printNumber(endLine);
			} else { // new terms with no UniqueID and with less infos if possible
				if (fileNr == 1 && startLine == endLine) {
					pout.openTerm("p3", true);
					pout.printNumber(startLine);
					pout.printNumber(getStartColumn(startNode));
					// we could also provide one case where fileNr=1 and startLine !== endLine
				} else if (startLine == endLine) {
					pout.openTerm("p4", true);
					pout.printNumber(fileNr);
					pout.printNumber(startLine);
					pout.printNumber(getStartColumn(startNode));
				} else {
					pout.openTerm("p5", true);
					pout.printNumber(fileNr);
					pout.printNumber(startLine);
					pout.printNumber(getStartColumn(startNode));
					pout.printNumber(endLine);
				}
			}
			pout.printNumber(getEndColumn(endNode));
			pout.closeTerm();
		}
	}

	private int getStartLine(Node node) {
		if (node.getStartPos() != null) {
			return node.getStartPos().getLine() + lineOffset;
		} else {
			return NO_LINE_OR_COLUMN_VALUE;
		}
	}

	private int getStartColumn(Node node) {
		if (node.getStartPos() != null) {
			return node.getStartPos().getPos() + columnOffset;
		} else {
			return NO_LINE_OR_COLUMN_VALUE;
		}
	}

	private int getEndLine(Node node) {
		if (node.getEndPos() != null) {
			return node.getEndPos().getLine() + lineOffset;
		} else {
			return NO_LINE_OR_COLUMN_VALUE;
		}
	}

	private int getEndColumn(Node node) {
		if (node.getEndPos() != null) {
			return node.getEndPos().getPos() + columnOffset;
		} else {
			return NO_LINE_OR_COLUMN_VALUE;
		}
	}

	@Override
	public void setPrologTermOutput(final IPrologTermOutput pout) {
		this.pout = pout;
	}

}
