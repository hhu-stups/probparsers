package de.be4.classicalb.core.parser.analysis.prolog;

import de.be4.classicalb.core.parser.node.Node;
import de.prob.prolog.output.IPrologTermOutput;


import de.be4.classicalb.core.parser.node.AIfElsifSubstitution;
import de.be4.classicalb.core.parser.node.ASelectWhenSubstitution;

/**
 * @author Daniel Plagge
 */
public class ClassicalPositionPrinter implements PositionPrinter {

	private IPrologTermOutput pout;

	// to look up the identifier of each node
	public final NodeIdAssignment nodeIds;

	private boolean printSourcePositions = false;
	private boolean legacyPositions = false;

	private int lineOffset = 0;
	private int columnOffset = 0;

	private static final int NO_LINE_OR_COLUMN_VALUE = 0;

	public ClassicalPositionPrinter(final NodeIdAssignment nodeIds) {
		this.nodeIds = nodeIds;
	}

	public ClassicalPositionPrinter(final NodeIdAssignment nodeIds, int lineOffset, int columnOffset) {
		this.nodeIds = nodeIds;
		this.lineOffset = lineOffset;
		this.columnOffset = columnOffset;
		this.printSourcePositions = true;
		this.legacyPositions = false;
	}

	public void printSourcePositions(boolean b) {
		this.printSourcePositions = b;
	}

	public void setLineOffset(int lineOffset) {
		this.lineOffset = lineOffset;
	}

	public void setColumnOffset(int columnOffset) {
		this.columnOffset = columnOffset;
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
    
	public void printPosition(final Node node) {
		final Integer id = nodeIds.lookup(node);
		if (id == null) {
			pout.printAtom("none");
		} else if (uselessPositionInfo(node)) {
		   pout.printNumber(id);
		} else {
			if (printSourcePositions) { // print Prolog 
			   int fileNr = nodeIds.lookupFileNumber(node);
			   int startLine = getStartLine(node);
			   int endLine = getEndLine(node);
			   if (legacyPositions) { // old pos(UniqueID,FileNr,StartLine,StartCol,Endline,EndCol) term
					pout.openTerm("pos", true);
					pout.printNumber(id);
					pout.printNumber(fileNr);
					pout.printNumber(startLine);
					pout.printNumber(getStartColumn(node));
					pout.printNumber(endLine);
				} else { // new terms with no UniqueID and with less infos if possible
				    if (fileNr==1 && startLine==endLine){
						pout.openTerm("p3", true);
						pout.printNumber(startLine);
						pout.printNumber(getStartColumn(node));
					// we could also provide one case where fileNr=1 and startLine !== endLine
					} else if (startLine==endLine) {
						pout.openTerm("p4", true);
						pout.printNumber(fileNr);
						pout.printNumber(startLine);
						pout.printNumber(getStartColumn(node));
				    } else {
						pout.openTerm("p5", true);
						pout.printNumber(fileNr);
						pout.printNumber(startLine);
						pout.printNumber(getStartColumn(node));
						pout.printNumber(endLine);
					}
				}
				pout.printNumber(getEndColumn(node));
				pout.closeTerm();
			} else {
				// only print the id
				pout.printNumber(id);
			}
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

	public void setPrologTermOutput(final IPrologTermOutput pout) {
		this.pout = pout;
	}

}
