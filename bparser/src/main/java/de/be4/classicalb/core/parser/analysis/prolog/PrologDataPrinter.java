package de.be4.classicalb.core.parser.analysis.prolog;

import java.math.BigInteger;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.ABooleanFalseExpression;
import de.be4.classicalb.core.parser.node.ABooleanTrueExpression;
import de.be4.classicalb.core.parser.node.ACoupleExpression;
import de.be4.classicalb.core.parser.node.AEmptySequenceExpression;
import de.be4.classicalb.core.parser.node.AEmptySetExpression;
import de.be4.classicalb.core.parser.node.AExpressionParseUnit;
import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.ARealExpression;
import de.be4.classicalb.core.parser.node.ARecEntry;
import de.be4.classicalb.core.parser.node.ARecExpression;
import de.be4.classicalb.core.parser.node.ASequenceExtensionExpression;
import de.be4.classicalb.core.parser.node.ASetExtensionExpression;
import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.EOF;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PRecEntry;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.prolog.output.IPrologTermOutput;

public class PrologDataPrinter extends DepthFirstAdapter {

	private final IPrologTermOutput pout;
	private final SortedMap<String, PExpression> currRecFields = new TreeMap<>();

	public PrologDataPrinter(IPrologTermOutput pout) {
		this.pout = pout;
	}

	@Override
	public void defaultIn(Node node) {
		throw new IllegalArgumentException("unsupported node type: " + node.getClass().getSimpleName());
	}

	@Override
	public void defaultCase(Node node) {
		throw new IllegalArgumentException("unsupported node type: " + node.getClass().getSimpleName());
	}

	@Override
	public void defaultOut(Node node) {
		throw new IllegalArgumentException("unsupported node type: " + node.getClass().getSimpleName());
	}

	@Override
	public void inStart(Start node) {
		// NO-OP
	}

	@Override
	public void outStart(Start node) {
		// NO-OP
	}

	@Override
	public void caseEOF(EOF node) {
		// NO-OP
	}

	@Override
	public void inAExpressionParseUnit(AExpressionParseUnit node) {
		// NO-OP
	}

	@Override
	public void outAExpressionParseUnit(AExpressionParseUnit node) {
		// NO-OP
	}

	@Override
	public void caseAStringExpression(AStringExpression node) {
		pout.openTerm("string");
		pout.printAtom(node.getContent().getText());
		pout.closeTerm();
	}

	@Override
	public void caseABooleanTrueExpression(ABooleanTrueExpression node) {
		pout.printAtom("pred_true");
	}

	@Override
	public void caseABooleanFalseExpression(ABooleanFalseExpression node) {
		pout.printAtom("pred_false");
	}

	@Override
	public void caseAIntegerExpression(AIntegerExpression node) {
		pout.openTerm("int");

		// MAX_LONG for java is 9223372036854775807 which is 19 digits,
		// so we set the max length to 18
		String text = node.getLiteral().getText();
		if (text.length() <= 18) {
			pout.printNumber(Long.parseLong(text));
		} else {
			pout.printNumber(new BigInteger(text));
		}

		pout.closeTerm();
	}

	@Override
	public void caseARealExpression(ARealExpression node) {
		pout.openTerm("term");
		pout.openTerm("floating");
		pout.printNumber(Double.parseDouble(node.getLiteral().getText()));
		pout.closeTerm();
		pout.closeTerm();
	}

	@Override
	public void inACoupleExpression(ACoupleExpression node) {
		// TODO: does this work for couples with more than 2 elements?
		pout.openTerm(",");
	}

	@Override
	public void outACoupleExpression(ACoupleExpression node) {
		pout.closeTerm();
	}

	@Override
	public void caseAEmptySetExpression(AEmptySetExpression node) {
		pout.emptyList();
	}

	@Override
	public void inASetExtensionExpression(ASetExtensionExpression node) {
		pout.openList();
	}

	@Override
	public void outASetExtensionExpression(ASetExtensionExpression node) {
		pout.closeList();
	}

	@Override
	public void caseAEmptySequenceExpression(AEmptySequenceExpression node) {
		pout.emptyList();
	}

	@Override
	public void caseASequenceExtensionExpression(ASequenceExtensionExpression node) {
		pout.openList();
		int index = 1;
		for (PExpression e : node.getExpression()) {
			pout.openTerm(",");
			pout.openTerm("int").printNumber(index).closeTerm();
			e.apply(this);
			pout.closeTerm();
			index++;
		}
		pout.closeList();
	}

	@Override
	public void caseAFunctionExpression(AFunctionExpression node) {
		// TODO: freetype values
		throw new UnsupportedOperationException("freetypes/function calls not supported");
		/*pout.openTerm("freeval");
		pout.printAtom(XML_FREETYPE_ATTRIBUTES_NAME);
		String id = Utils.getAIdentifierAsString((AIdentifierExpression) node.getIdentifier());
		pout.printAtom(id);
		node.getParameters().getFirst().apply(this);
		pout.closeTerm();*/
	}

	@Override
	public void caseARecExpression(ARecExpression node) {
		this.currRecFields.clear();

		// collect all record fields
		for (PRecEntry recEntry : node.getEntries()) {
			recEntry.apply(this);
		}

		pout.openTerm("rec");
		pout.openList();

		// record fields must be sorted!
		for (Map.Entry<String, PExpression> entry : this.currRecFields.entrySet()) {
			pout.openTerm("field");
			pout.printAtom(entry.getKey());
			entry.getValue().apply(this);
			pout.closeTerm();
		}

		pout.closeList();
		pout.closeTerm();
		this.currRecFields.clear();
	}

	@Override
	public void caseARecEntry(ARecEntry node) {
		String id = Utils.getAIdentifierAsString((AIdentifierExpression) node.getIdentifier());
		if (this.currRecFields.put(id, node.getValue()) != null) {
			throw new IllegalArgumentException("duplicated rec entry " + id);
		}
	}
}
