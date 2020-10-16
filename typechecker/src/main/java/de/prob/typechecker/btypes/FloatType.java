package de.prob.typechecker.btypes;

import de.be4.classicalb.core.parser.node.AFloatSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.prob.typechecker.Typechecker;
import de.prob.typechecker.exceptions.UnificationException;

public class FloatType implements BType {

	private static FloatType instance = new FloatType();

	public static FloatType getInstance() {
		return instance;
	}

	public BType unify(BType other, ITypechecker typechecker) {
		if (!this.compare(other)) {
			throw new UnificationException();
		}
		if (other instanceof FloatType) {
			return getInstance();
		}
		if (other instanceof UntypedType) {
			((UntypedType) other).setFollowersTo(this, typechecker);
			return getInstance();
		}
		throw new UnificationException();
	}

	@Override
	public String toString() {
		return "REAL";
	}
	
	
	public boolean isUntyped() {
		return false;
	}

	public boolean compare(BType other) {
		if (other instanceof UntypedType || other instanceof FloatType)
			return true;
		if (other instanceof FloatOrSetType
				|| other instanceof FloatOrSetOfPairType)
			return true;
		return false;
	}

	public boolean containsInfiniteType() {
		return true;
	}

	public PExpression createASTNode(Typechecker typechecker) {
		AFloatSetExpression node = new AFloatSetExpression();
		typechecker.setType(node, new SetType(this));
		return node;
	}
}
