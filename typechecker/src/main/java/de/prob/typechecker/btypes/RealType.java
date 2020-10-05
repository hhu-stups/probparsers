package de.prob.typechecker.btypes;

import de.be4.classicalb.core.parser.node.ARealSetExpression;
import de.be4.classicalb.core.parser.node.PExpression;
import de.prob.typechecker.Typechecker;
import de.prob.typechecker.exceptions.UnificationException;

public class RealType implements BType {

	private static RealType instance = new RealType();

	public static RealType getInstance() {
		return instance;
	}

	public BType unify(BType other, ITypechecker typechecker) {
		if (!this.compare(other)) {
			throw new UnificationException();
		}
		if (other instanceof RealType) {
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
		if (other instanceof UntypedType || other instanceof RealType)
			return true;
		if (other instanceof RealOrSetType
				|| other instanceof RealOrSetOfPairType)
			return true;
		return false;
	}

	public boolean containsInfiniteType() {
		return true;
	}

	public PExpression createASTNode(Typechecker typechecker) {
		ARealSetExpression node = new ARealSetExpression();
		typechecker.setType(node, new SetType(this));
		return node;
	}
}
