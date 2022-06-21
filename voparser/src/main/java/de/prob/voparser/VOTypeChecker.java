package de.prob.voparser;


import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import de.prob.voparser.analysis.DepthFirstAdapter;
import de.prob.voparser.node.AAndVo;
import de.prob.voparser.node.AEquivalentVo;
import de.prob.voparser.node.AIdentifierVo;
import de.prob.voparser.node.AImpliesVo;
import de.prob.voparser.node.ANotVo;
import de.prob.voparser.node.AOrVo;
import de.prob.voparser.node.ASequentialVo;
import de.prob.voparser.node.Node;
import de.prob.voparser.node.Start;
import de.prob.voparser.node.TIdentifierLiteral;

public class VOTypeChecker extends DepthFirstAdapter {

	private static final IFn INTERSECTION;

	private static final IFn UNION;

	private static final IFn DIFFERENCE;

	static {
		RT.var("clojure.core", "require").invoke(Clojure.read("clojure.set"));
		INTERSECTION = RT.var("clojure.set", "intersection");
		UNION = RT.var("clojure.set", "union");
		DIFFERENCE = RT.var("clojure.set", "difference");
	}

	private final VOParser voParser;

	private boolean error;

	private PersistentHashSet modifiedAnimatorState;

	public VOTypeChecker(VOParser voParser) {
		this.voParser = voParser;
		this.error = false;
		this.modifiedAnimatorState = PersistentHashSet.EMPTY;
	}

	public void typeCheck(Start start) throws VOParseException {
		start.apply(this);
		if (error) {
			throw new VOParseException("Type error in VO", VOParseException.ErrorType.TYPECHECKING);
		}
	}

	private PersistentHashSet visitVOExpression(Node node, PersistentHashSet animatorState) {
		if (node instanceof TIdentifierLiteral) {
			return visitIdentifierNode((AIdentifierVo) node, animatorState);
		} else if (node instanceof AAndVo) {
			return visitAndExpression((AAndVo) node, animatorState);
		} else if(node instanceof AOrVo) {
			return visitOrExpression((AOrVo) node, animatorState);
		} else if(node instanceof ANotVo) {
			return visitNotExpression((ANotVo) node, animatorState);
		} else if(node instanceof AEquivalentVo) {
			return visitEquivalentExpression((AEquivalentVo) node, animatorState);
		} else if(node instanceof AImpliesVo) {
			return visitImpliesExpression((AImpliesVo) node, animatorState);
		} else if(node instanceof ASequentialVo) {
			return visitSequentialExpression((ASequentialVo) node, animatorState);
		}
		return animatorState;
	}

	@Override
	public void caseAAndVo(AAndVo node) {
		modifiedAnimatorState = visitAndExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet visitAndExpression(AAndVo node, PersistentHashSet animatorState) {
		PersistentHashSet leftAnimatorState = visitVOExpression(node.getLeft(), animatorState);
		PersistentHashSet rightAnimatorState = visitVOExpression(node.getRight(), animatorState);
		PersistentHashSet resultAnimatorState = (PersistentHashSet) INTERSECTION.invoke(leftAnimatorState, rightAnimatorState);
		resultAnimatorState = (PersistentHashSet) resultAnimatorState.disjoin(AnimatorState.TRACE);
		return resultAnimatorState;
	}

	@Override
	public void caseAOrVo(AOrVo node) {
		modifiedAnimatorState = visitOrExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet visitOrExpression(AOrVo node, PersistentHashSet animatorState) {
		PersistentHashSet leftAnimatorState = visitVOExpression(node.getLeft(), animatorState);
		PersistentHashSet rightAnimatorState = visitVOExpression(node.getRight(), animatorState);
		PersistentHashSet resultAnimatorState = (PersistentHashSet) INTERSECTION.invoke(leftAnimatorState, rightAnimatorState);
		resultAnimatorState = (PersistentHashSet) resultAnimatorState.disjoin(AnimatorState.STATE_SPACE);
		return resultAnimatorState;
	}

	@Override
	public void caseANotVo(ANotVo node) {
		modifiedAnimatorState = visitNotExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet visitNotExpression(ANotVo node, PersistentHashSet animatorState) {
		return visitVOExpression(node.getVo(), animatorState);
	}

	@Override
	public void caseAImpliesVo(AImpliesVo node) {
		modifiedAnimatorState = visitImpliesExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet visitImpliesExpression(AImpliesVo node, PersistentHashSet animatorState) {
		return visitVOExpression(new AOrVo(new ANotVo(node.getLeft()), node.getRight()), animatorState);
	}

	@Override
	public void caseAEquivalentVo(AEquivalentVo node) {
		modifiedAnimatorState = visitEquivalentExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet visitEquivalentExpression(AEquivalentVo node, PersistentHashSet animatorState) {
		return visitVOExpression(new AAndVo(new AImpliesVo(node.getLeft(), node.getRight()), new AImpliesVo(node.getRight(), node.getLeft())), animatorState);
	}

	@Override
	public void caseASequentialVo(ASequentialVo node) {
		modifiedAnimatorState = visitSequentialExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet visitSequentialExpression(ASequentialVo node, PersistentHashSet animatorState) {
		PersistentHashSet leftAnimatorState = visitVOExpression(node.getLeft(), animatorState);
		return visitVOExpression(node.getRight(), leftAnimatorState);
	}

	@Override
	public void caseAIdentifierVo(AIdentifierVo node) {
		modifiedAnimatorState = visitIdentifierNode(node, modifiedAnimatorState);
	}


	private PersistentHashSet visitIdentifierNode(AIdentifierVo node, PersistentHashSet animatorState) {
		VTType type = voParser.getTasks().get(node.getIdentifierLiteral().getText());
		PersistentHashSet newAnimatorState = animatorState;
		boolean valid = true;
		switch (type) {
			case RELOAD:
				newAnimatorState = (PersistentHashSet) newAnimatorState.cons(AnimatorState.TRACE);
				newAnimatorState = (PersistentHashSet) newAnimatorState.cons(AnimatorState.STATE_SPACE);
				break;
			case RESET:
				newAnimatorState = (PersistentHashSet) newAnimatorState.cons(AnimatorState.TRACE);
				break;
			case TRACE_REPLAY:
				valid = newAnimatorState.contains(AnimatorState.TRACE);
				break;
			case STATE_SPACE_VISUALIZATION:
				valid = newAnimatorState.contains(AnimatorState.STATE_SPACE);
				break;
			case TRACE_VISUALIZATION:
				valid = newAnimatorState.contains(AnimatorState.TRACE);
				break;
			case COVERAGE:
				valid = newAnimatorState.contains(AnimatorState.STATE_SPACE);
				break;
			case TRACE_COVERAGE:
				valid = newAnimatorState.contains(AnimatorState.TRACE);
				break;
			case MODEL_CHECKING_GOAL:
				newAnimatorState = (PersistentHashSet) newAnimatorState.cons(AnimatorState.TRACE);
				break;
			case MODEL_CHECKING_INV:
				newAnimatorState = (PersistentHashSet) newAnimatorState.disjoin(AnimatorState.TRACE);
				break;
			case MODEL_CHECKING_INV_COMPLETE:
				newAnimatorState = (PersistentHashSet) newAnimatorState.disjoin(AnimatorState.TRACE);
				newAnimatorState = (PersistentHashSet) newAnimatorState.cons(AnimatorState.STATE_SPACE);
				break;
			case LTL_MODEL_CHECKING:
				newAnimatorState = (PersistentHashSet) newAnimatorState.disjoin(AnimatorState.TRACE);
				break;
			case LTL_CURRENT:
				valid = newAnimatorState.contains(AnimatorState.TRACE);
				break;
			case LTL_CURRENT_GOAL:
				valid = newAnimatorState.contains(AnimatorState.TRACE);
				newAnimatorState = (PersistentHashSet) newAnimatorState.cons(AnimatorState.TRACE);
				break;
		}
		if(!valid) {
			error = true;
		}
		return newAnimatorState;
	}
}
