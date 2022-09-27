package de.prob.voparser;


import com.github.krukow.clj_lang.PersistentHashSet;
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

public class VOTypeChecker extends DepthFirstAdapter {

	private final VOParser voParser;

	private boolean error;

	private PersistentHashSet<AnimatorState> modifiedAnimatorState;

	public VOTypeChecker(VOParser voParser) {
		this.voParser = voParser;
		this.error = false;
		this.modifiedAnimatorState = PersistentHashSet.create(AnimatorState.STATE_SPACE, AnimatorState.TRACE);
	}

	public void typeCheck(Start start) throws VOParseException {
		start.apply(this);
		if (error) {
			throw new VOParseException("Type error in VO", VOParseException.ErrorType.TYPECHECKING);
		}
	}

	private PersistentHashSet<AnimatorState> visitVOExpression(Node node, PersistentHashSet<AnimatorState> animatorState) {
		if (node instanceof AIdentifierVo) {
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
		} else {
			throw new RuntimeException("Node type unknown: " + node.getClass());
		}
	}

	@Override
	public void caseAAndVo(AAndVo node) {
		modifiedAnimatorState = visitAndExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet<AnimatorState> visitAndExpression(AAndVo node, PersistentHashSet<AnimatorState> animatorState) {
		PersistentHashSet<AnimatorState> leftAnimatorState = visitVOExpression(node.getLeft(), animatorState);
		PersistentHashSet<AnimatorState> rightAnimatorState = visitVOExpression(node.getRight(), animatorState);
		PersistentHashSet<AnimatorState> resultAnimatorState = leftAnimatorState;
		for(AnimatorState state : leftAnimatorState) {
			if(!rightAnimatorState.contains(state)) {
				resultAnimatorState.remove(state);
			}
		}
		resultAnimatorState = resultAnimatorState.disjoin(AnimatorState.TRACE);
		return resultAnimatorState;
	}

	@Override
	public void caseAOrVo(AOrVo node) {
		modifiedAnimatorState = visitOrExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet<AnimatorState> visitOrExpression(AOrVo node, PersistentHashSet<AnimatorState> animatorState) {
		PersistentHashSet<AnimatorState> leftAnimatorState = visitVOExpression(node.getLeft(), animatorState);
		PersistentHashSet<AnimatorState> rightAnimatorState = visitVOExpression(node.getRight(), animatorState);
		PersistentHashSet<AnimatorState> resultAnimatorState = leftAnimatorState;
		for(AnimatorState state : leftAnimatorState) {
			if(!rightAnimatorState.contains(state)) {
				resultAnimatorState.remove(state);
			}
		}
		resultAnimatorState = resultAnimatorState.disjoin(AnimatorState.STATE_SPACE);
		return resultAnimatorState;
	}

	@Override
	public void caseANotVo(ANotVo node) {
		modifiedAnimatorState = visitNotExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet<AnimatorState> visitNotExpression(ANotVo node, PersistentHashSet<AnimatorState> animatorState) {
		return visitVOExpression(node.getVo(), animatorState);
	}

	@Override
	public void caseAImpliesVo(AImpliesVo node) {
		modifiedAnimatorState = visitImpliesExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet<AnimatorState> visitImpliesExpression(AImpliesVo node, PersistentHashSet<AnimatorState> animatorState) {
		// Remark: left and right node must be cloned before creating a new AST node from them. Somehow, SableCC sometimes sets AST node to null. This is avoided by the clone.
		return visitVOExpression(new AOrVo(new ANotVo(node.getLeft().clone()), node.getRight().clone()), animatorState);
	}

	@Override
	public void caseAEquivalentVo(AEquivalentVo node) {
		modifiedAnimatorState = visitEquivalentExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet<AnimatorState> visitEquivalentExpression(AEquivalentVo node, PersistentHashSet<AnimatorState> animatorState) {
		// Remark: left and right node must be cloned before creating a new AST node from them. Somehow, SableCC sometimes sets AST node to null. This is avoided by the clone.
		return visitVOExpression(new AAndVo(new AImpliesVo(node.getLeft().clone(), node.getRight().clone()), new AImpliesVo(node.getRight().clone(), node.getLeft().clone())), animatorState);
	}

	@Override
	public void caseASequentialVo(ASequentialVo node) {
		modifiedAnimatorState = visitSequentialExpression(node, modifiedAnimatorState);
	}

	private PersistentHashSet<AnimatorState> visitSequentialExpression(ASequentialVo node, PersistentHashSet<AnimatorState> animatorState) {
		PersistentHashSet<AnimatorState> leftAnimatorState = visitVOExpression(node.getLeft(), animatorState);
		return visitVOExpression(node.getRight(), leftAnimatorState);
	}

	@Override
	public void caseAIdentifierVo(AIdentifierVo node) {
		modifiedAnimatorState = visitIdentifierNode(node, modifiedAnimatorState);
	}


	private PersistentHashSet<AnimatorState> visitIdentifierNode(AIdentifierVo node, PersistentHashSet<AnimatorState> animatorState) {
		VTType type = voParser.getTasks().get(node.getIdentifierLiteral().getText());
		PersistentHashSet<AnimatorState> newAnimatorState = animatorState;
		boolean valid = true;
		switch (type) {
			case RELOAD:
				newAnimatorState = newAnimatorState.cons(AnimatorState.TRACE);
				newAnimatorState = newAnimatorState.cons(AnimatorState.STATE_SPACE);
				break;
			case RESET:
				newAnimatorState = newAnimatorState.cons(AnimatorState.TRACE);
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
			case STATE_SPACE_COVERAGE:
				valid = newAnimatorState.contains(AnimatorState.STATE_SPACE);
				break;
			case TRACE_COVERAGE:
				valid = newAnimatorState.contains(AnimatorState.TRACE);
				break;
			case MODEL_CHECKING_GOAL:
				newAnimatorState = newAnimatorState.cons(AnimatorState.TRACE);
				break;
			case MODEL_CHECKING_PROP:
				newAnimatorState = newAnimatorState.disjoin(AnimatorState.TRACE);
				break;
			case MODEL_CHECKING_COMPLETE:
				newAnimatorState = newAnimatorState.disjoin(AnimatorState.TRACE);
				newAnimatorState = newAnimatorState.cons(AnimatorState.STATE_SPACE);
				break;
		}
		if(!valid) {
			error = true;
		}
		return newAnimatorState;
	}
}
