package de.prob.typechecker.btypes;

import de.be4.classicalb.core.parser.node.PExpression;
import de.prob.typechecker.Typechecker;
import de.prob.typechecker.exceptions.UnificationException;

public class RealOrSetType extends AbstractHasFollowers {

	public BType unify(BType other, ITypechecker typechecker) {
		if (!this.compare(other))
			throw new UnificationException();

		if (other instanceof RealType) {
			this.setFollowersTo(RealType.getInstance(), typechecker);
			return RealType.getInstance();
		}
		if (other instanceof UntypedType) {
			((UntypedType) other).setFollowersTo(this, typechecker);
			return this;
		}
		if (other instanceof SetType) {
			this.setFollowersTo(other, typechecker);
			return other;
		}
		if (other instanceof RealOrSetType) {
			((RealOrSetType) other).setFollowersTo(this, typechecker);
			return this;
		}
		if (other instanceof RealOrSetOfPairType) {
			this.setFollowersTo(other, typechecker);
			return other;
		}
		throw new RuntimeException();
	}

	public boolean isUntyped() {
		return true;
	}

	public boolean compare(BType other) {
		if (other instanceof UntypedType || other instanceof RealType
				|| other instanceof SetType
				|| other instanceof RealOrSetType
				|| other instanceof RealOrSetOfPairType)
			return true;
		else if (other instanceof FunctionType) {
			return other.compare(this);
		} else
			return false;
	}

	@Override
	public boolean contains(BType other) {
		return false;
	}

	public boolean containsInfiniteType() {
		return false;
	}

	public PExpression createASTNode(Typechecker typechecker) {
		return null;
	}
}
