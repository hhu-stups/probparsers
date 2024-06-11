package de.be4.classicalb.core.parser.analysis.prolog;

import java.math.BigInteger;
import java.util.*;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;
import de.prob.prolog.output.IPrologTermOutput;

public class PrologDataPrinter extends DepthFirstAdapter {

	// for ProB's Prolog representation of enumerated set elements: fd(Nr,Set)
	private static class FDSet {
		private final int nr;
		private final String set;

		public FDSet(int nr, String set) {
			this.nr = nr;
			this.set = set;
		}
	}

	private final IPrologTermOutput pout;
	private final Map<String,FDSet> sets = new HashMap<>();
	private final Map<String,String> freetypes = new HashMap<>();
	private final Deque<SortedMap<String, PExpression>> currRecFields = new ArrayDeque<>();

	public PrologDataPrinter(IPrologTermOutput pout) {
		this(pout, null, null);
	}

	public PrologDataPrinter(IPrologTermOutput pout, ASetsMachineClause sets, AFreetypesMachineClause freetypes) {
		this.pout = pout;
		if (sets != null) {
			for (PSet set : sets.getSetDefinitions()) {
				if (set instanceof ADescriptionSet) {
					set = ((ADescriptionSet) set).getSet();
				}
				if (set instanceof AEnumeratedSetSet) {
					AEnumeratedSetSet setSet = (AEnumeratedSetSet) set;
					String setId = setSet.getIdentifier().getFirst().getText();
					int count = 1;
					for (PExpression element : setSet.getElements()) {
						String id = Utils.getAIdentifierAsString((AIdentifierExpression) element);
						this.sets.put(id, new FDSet(count, setId));
						count++;
					}
				} else {
					throw new AssertionError("deferred sets are not supported");
				}
			}
		}
		if (freetypes != null) {
			for (PFreetype freetype : freetypes.getFreetypes()) {
				AFreetype aFreetype = (AFreetype) freetype;
				String ftId = aFreetype.getName().getText();
				for (PFreetypeConstructor constructor : aFreetype.getConstructors()) {
					if (constructor instanceof AConstructorFreetypeConstructor) {
						AConstructorFreetypeConstructor constructorFreetype = (AConstructorFreetypeConstructor) constructor;
						this.freetypes.put(constructorFreetype.getName().getText(), ftId);
					} else if (constructor instanceof AElementFreetypeConstructor) {
						AElementFreetypeConstructor elementFreetype = (AElementFreetypeConstructor) constructor;
						this.freetypes.put(elementFreetype.getName().getText(), ftId);
					} else {
						throw new AssertionError();
					}
				}
			}
		}
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
	public void caseAIdentifierExpression(AIdentifierExpression node) {
		String id = Utils.getAIdentifierAsString(node);
		if (sets.containsKey(id)) {
			FDSet fd = sets.get(id);
			pout.openTerm("fd")
				.printNumber(fd.nr)
				.printAtom(fd.set)
				.closeTerm();
		} else if (freetypes.containsKey(id)) {
			pout.openTerm("freeval")
				.printAtom(freetypes.get(id))
				.printAtom(id)
				.openTerm("term")
				.printAtom(id)
				.closeTerm()
				.closeTerm();
		} else if (sets.isEmpty() && freetypes.isEmpty()) {
			throw new IllegalStateException("identifier expressions can only be translated if the sets or freetypes machine clause is provided and contains a suitable element");
		} else {
			throw new IllegalStateException("no enumerated set item or freetype element constructor found for identifier expression " + id);
		}
	}

	@Override
	public void caseAUnaryMinusExpression(AUnaryMinusExpression node) {
		PExpression expr = node.getExpression();
		if (expr instanceof AIntegerExpression) {
			printInteger((AIntegerExpression) expr, true);
		} else if (expr instanceof ARealExpression) {
			printReal((ARealExpression) expr, true);
		} else {
			throw new AssertionError("unary minus only supported for integers and reals");
		}
	}

	@Override
	public void caseAIntegerExpression(AIntegerExpression node) {
		printInteger(node, false);
	}

	private void printInteger(AIntegerExpression node, boolean negative) {
		pout.openTerm("int");

		// MAX_LONG for java is 9223372036854775807 which is 19 digits,
		// so we set the max length to 18
		String text = (negative ? "-" : "") + node.getLiteral().getText();
		if (text.length() <= 18) {
			pout.printNumber(Long.parseLong(text));
		} else {
			pout.printNumber(new BigInteger(text));
		}

		pout.closeTerm();
	}

	@Override
	public void caseARealExpression(ARealExpression node) {
		printReal(node, false);
	}

	private void printReal(ARealExpression node, boolean negative) {
		String text = (negative ? "-" : "") + node.getLiteral().getText();

		pout.openTerm("term").openTerm("floating");
		pout.printNumber(Double.parseDouble(text));
		pout.closeTerm().closeTerm();
	}

	@Override
	public void caseACoupleExpression(ACoupleExpression node) {
		List<PExpression> expressions = node.getList();
		for (int i = 0; i < expressions.size() - 1; i++) {
			pout.openTerm(",");
		}

		for (int i = 0; i < expressions.size(); i++) {
			expressions.get(i).apply(this);
			if (i != 0) {
				pout.closeTerm();
			}
		}
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
		String id = Utils.getAIdentifierAsString((AIdentifierExpression) node.getIdentifier());
		if (freetypes.containsKey(id)) {
			pout.openTerm("freeval");
			pout.printAtom(freetypes.get(id));
			pout.printAtom(id);
			if (node.getParameters().size() != 1) {
				throw new IllegalArgumentException("expected exactly one parameter for freetype constructor " + id);
			}
			node.getParameters().getFirst().apply(this);
			pout.closeTerm();
		} else if (freetypes.isEmpty()) {
			throw new IllegalStateException("function expressions are only available if the freetypes machine clause is provided and contains a suitable constructor");
		} else {
			throw new IllegalStateException("no freetype constructor found for function expression " + id);
		}
	}

	@Override
	public void caseARecExpression(ARecExpression node) {
		this.currRecFields.addFirst(new TreeMap<>());

		// collect all record fields
		for (PRecEntry recEntry : node.getEntries()) {
			recEntry.apply(this);
		}

		pout.openTerm("rec");
		pout.openList();

		// record fields must be sorted!
		for (Map.Entry<String, PExpression> entry : this.currRecFields.removeFirst().entrySet()) {
			pout.openTerm("field");
			pout.printAtom(entry.getKey());
			entry.getValue().apply(this);
			pout.closeTerm();
		}

		pout.closeList();
		pout.closeTerm();
	}

	@Override
	public void caseARecEntry(ARecEntry node) {
		String id = Utils.getAIdentifierAsString((AIdentifierExpression) node.getIdentifier());
		if (this.currRecFields.getFirst().put(id, node.getValue()) != null) {
			throw new IllegalArgumentException("duplicated rec entry " + id);
		}
	}
}
