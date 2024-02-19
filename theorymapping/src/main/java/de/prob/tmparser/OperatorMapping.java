package de.prob.tmparser;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.prolog.output.IPrologTermOutput;

public class OperatorMapping {
	private final String theoryName;
	private final String operatorName;
	private final TMOperatorType operatorType;
	private final String spec;
	private final Start parsedSpec;

	public OperatorMapping(String theoryName, String operatorName,
			TMOperatorType operatorType, String spec) {
		super();
		this.theoryName = theoryName;
		this.operatorName = operatorName;
		this.operatorType = operatorType;
		this.spec = spec;

		// TODO Should we handle this in the actual parser as a different token/node type?
		if (spec.startsWith("$") && spec.endsWith("$")) {
			String formula = spec.substring(1, spec.length() - 1);
			try {
				this.parsedSpec = new BParser().parseExpression(formula);
			} catch (BCompoundException exc) {
				throw new TheoryMappingException("Parse error in B expression for operator " + operatorName + ": " + exc.getMessage(), exc);
			}
		} else {
			// Spec doesn't contain a B expression
			this.parsedSpec = null;
		}
	}

	public String getTheoryName() {
		return theoryName;
	}

	public String getOperatorName() {
		return operatorName;
	}

	public TMOperatorType getOperatorType() {
		return operatorType;
	}

	public String getSpec() {
		return spec;
	}

	/**
	 * Get a parsed AST for the expression (if any) in the spec.
	 * 
	 * @return B expression AST parsed from the spec, or {@code null} if the spec doesn't contain an expression
	 */
	public Start getParsedSpec() {
		return this.parsedSpec;
	}

	@Override
	public String toString() {
		return "operator '" + operatorName +
			"' of theory " + theoryName +
			" and type " + operatorType.toString() +
			": {" + spec + "}";
	}

	@Override
	public int hashCode() {
		int result = 31 * operatorName.hashCode();
		result = 31 * result + operatorType.hashCode();
		result = 31 * result + spec.hashCode();
		result = 31 * result + theoryName.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperatorMapping other = (OperatorMapping) obj;
		if (operatorName == null) {
			if (other.operatorName != null)
				return false;
		} else if (!operatorName.equals(other.operatorName))
			return false;
		if (operatorType != other.operatorType)
			return false;
		if (spec == null) {
			if (other.spec != null)
				return false;
		} else if (!spec.equals(other.spec))
			return false;
		if (theoryName == null) {
			if (other.theoryName != null)
				return false;
		} else if (!theoryName.equals(other.theoryName))
			return false;
		return true;
	}

	public void printProlog(IPrologTermOutput pout) {
		pout.openTerm("tag");
		pout.printAtom(this.getOperatorName());
		if (this.getParsedSpec() != null) {
			// For B expressions, send the parsed AST so that the Prolog side doesn't have to call the parser again
			pout.openTerm("bexpr");
			ASTProlog.printFormula(this.getParsedSpec(), pout);
			pout.closeTerm();
		} else {
			pout.printAtom(this.getSpec());
		}
		pout.closeTerm();
	}
}
