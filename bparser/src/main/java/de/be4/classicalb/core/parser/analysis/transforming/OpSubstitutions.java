package de.be4.classicalb.core.parser.analysis.transforming;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.be4.classicalb.core.parser.IDefinitions;
import de.be4.classicalb.core.parser.IDefinitions.Type;
import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.VisitorException;
import de.be4.classicalb.core.parser.node.AAnySubstitution;
import de.be4.classicalb.core.parser.node.AComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.ADefinitionExpression;
import de.be4.classicalb.core.parser.node.ADefinitionSubstitution;
import de.be4.classicalb.core.parser.node.AEventBComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.AExistsPredicate;
import de.be4.classicalb.core.parser.node.AExpressionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.AForallPredicate;
import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.AGeneralProductExpression;
import de.be4.classicalb.core.parser.node.AGeneralSumExpression;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.ALambdaExpression;
import de.be4.classicalb.core.parser.node.ALetExpressionExpression;
import de.be4.classicalb.core.parser.node.ALetPredicatePredicate;
import de.be4.classicalb.core.parser.node.ALetSubstitution;
import de.be4.classicalb.core.parser.node.AOpSubstitution;
import de.be4.classicalb.core.parser.node.AOperationOrDefinitionCallSubstitution;
import de.be4.classicalb.core.parser.node.AQuantifiedIntersectionExpression;
import de.be4.classicalb.core.parser.node.AQuantifiedUnionExpression;
import de.be4.classicalb.core.parser.node.ASubstitutionDefinitionDefinition;
import de.be4.classicalb.core.parser.node.ASymbolicComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.ASymbolicEventBComprehensionSetExpression;
import de.be4.classicalb.core.parser.node.ASymbolicLambdaExpression;
import de.be4.classicalb.core.parser.node.ASymbolicQuantifiedUnionExpression;
import de.be4.classicalb.core.parser.node.AVarSubstitution;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PSubstitution;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TDefLiteralSubstitution;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.util.Utils;

/**
 * <p>
 * This visitor performs two AST transformations which need to be done in one
 * DFS cause they influence each other when definitions are involved. The reason
 * is that in some cases we cannot decide during preparsing and parsing, if the
 * RHS of a definition is an expression or a substitution. Function and
 * operation calls are syntactically the same in most cases. So we postpone the
 * decision until this DFS is made. The first usage of a definition determines
 * which type is assumed. All other uses need to follow this typing otherwise an
 * error is thrown.
 * </p>
 * <p>
 * During parsing, substitutions that are operation/definition calls
 * without return values are recognized as {@link AOperationOrDefinitionCallSubstitution}
 * (as a workaround to avoid shift/reduce conflicts).
 * This visitor finds all {@link AOperationOrDefinitionCallSubstitution} nodes and replaces them
 * with a corresponding {@link AOpSubstitution} or {@link ADefinitionSubstitution} node.
 * </p>
 * <p>
 * If an {@link AOperationOrDefinitionCallSubstitution} contains an {@link AFunctionExpression},
 * it's an operation/definition call with arguments.
 * If an {@link AOperationOrDefinitionCallSubstitution} contains an {@link AIdentifierExpression},
 * it's an operation/definition call without arguments.
 * Other expression types inside {@link AOperationOrDefinitionCallSubstitution} are not allowed by the grammar.
 * </p>
 * <p>
 * Definitions with type Expression are not recognized during parsing. In the
 * resulting AST they are not distinguishable from normal
 * {@link AIdentifierExpression} or {@link AFunctionExpression} nodes.
 * </p>
 * <p>
 * Therefor this visitor searches {@link AIdentifierExpression} and
 * {@link AFunctionExpression} nodes and checks if their name is declared as a
 * definition. If such a node is found, it is replaced by a new node
 * {@link ADefinitionExpression}.
 * </p>
 * <p>
 * The visitor keeps track of scoped variables to ensure, that no node of a
 * scoped variable is replaced.
 * </p>
 */
public class OpSubstitutions extends OptimizedTraversingAdapter {

	private final IDefinitions definitions;
	private final Map<String, Integer> scopedVariables = new HashMap<>();

	private OpSubstitutions(final IDefinitions definitions) {
		this.definitions = definitions;
	}

	public static void transform(Start start, IDefinitions definitions) throws CheckException {
		try {
			start.apply(new OpSubstitutions(definitions));
		} catch (VisitorException e) {
			throw e.getException();
		}
	}

	@Override
	public void caseAOperationOrDefinitionCallSubstitution(AOperationOrDefinitionCallSubstitution node) {
		PExpression expression = node.getExpression();
		PExpression idExpr;
		LinkedList<PExpression> parameters;
		Type type;
		TIdentifierLiteral idToken = null;
		String idString = null;

		if (expression instanceof AFunctionExpression) {
			// the operation was parsed as a function expression
			final AFunctionExpression function = (AFunctionExpression) expression;
			idExpr = function.getIdentifier();

			if (idExpr instanceof AIdentifierExpression) {
				final AIdentifierExpression identifier = (AIdentifierExpression)idExpr;
				idString = Utils.getTIdentifierListAsString(identifier.getIdentifier());
				idToken = identifier.getIdentifier().get(0);
				type = definitions.getType(idString);
			} else {
				type = Type.NoDefinition;
			}

			parameters = new LinkedList<>(function.getParameters());
		} else if (expression instanceof AIdentifierExpression) {
			// the operation was parsed as an identifier expression
			final AIdentifierExpression identifier = (AIdentifierExpression) expression;
			idString = Utils.getTIdentifierListAsString(identifier.getIdentifier());
			idToken = identifier.getIdentifier().get(0);
			type = definitions.getType(idString);

			idExpr = expression;
			parameters = new LinkedList<>();
		} else {
			// some other expression was parsed (NOT allowed)
			throw new BParseException(null, "Expecting operation");
		}

		if (type != Type.NoDefinition && idToken != null) {
			if (type == Type.Substitution || type == Type.ExprOrSubst) {
				// create DefinitionSubstitution
				final ADefinitionSubstitution defSubst = new ADefinitionSubstitution(
						new TDefLiteralSubstitution(idToken.getText(), idToken.getLine(), idToken.getPos()),
						parameters);

				if (type == Type.ExprOrSubst) {
					// type is determined now => set to Substitution
					setTypeSubstDef(node, idString);
				}

				// transfer position information
				defSubst.setStartPos(node.getStartPos());
				defSubst.setEndPos(node.getEndPos());

				node.replaceBy(defSubst);
				defSubst.apply(this);
			} else {
				// finding some other type here is an error!
				throw new VisitorException(new CheckException(
						"Expecting substitution here but found definition with type '" + type + "'", node));
			}
		} else {
			// no def, no problem ;-)
			final AOpSubstitution opSubst = new AOpSubstitution(idExpr, parameters);
			opSubst.setStartPos(idExpr.getStartPos());
			opSubst.setEndPos(idExpr.getEndPos());
			node.replaceBy(opSubst);
			opSubst.apply(this);
		}
	}

	@Override
	public void caseAIdentifierExpression(final AIdentifierExpression node) {
		final String identifierString = Utils.getTIdentifierListAsString(node.getIdentifier());
		final Integer number = scopedVariables.get(identifierString);
		final Type type = definitions.getType(identifierString);

		if (number == null && type != Type.NoDefinition) {
			if (type == Type.Expression || type == Type.ExprOrSubst) {
				/*
				 * getFirst() is enough cause definitions cannot have composed
				 * identifiers
				 */
				replaceWithDefExpression(node, node.getIdentifier().getFirst(), null);

				if (type == Type.ExprOrSubst) {
					// type is determined now => set to Expression
					definitions.setDefinitionType(identifierString, Type.Expression);
				}
			} else {
				// finding some other type here is an error!
				throw new VisitorException(new CheckException(
						"Expecting expression here but found definition with type '" + type + "'", node));
			}
		}
	}

	@Override
	public void caseAFunctionExpression(final AFunctionExpression node) {
		if (node.getIdentifier() != null) {
			node.getIdentifier().apply(this);
		}

		if (node.getIdentifier() instanceof ADefinitionExpression
				&& ((ADefinitionExpression) node.getIdentifier()).getParameters().isEmpty()) {
			final LinkedList<PExpression> paramList = new LinkedList<>(node.getParameters());

			final TIdentifierLiteral identifier = ((ADefinitionExpression) node.getIdentifier()).getDefLiteral();

			if (paramList.size() <= definitions.getParameterCount(identifier.getText())) {
				/*
				 * The parameters seem to belong to this definition, so we need
				 * to replace the FunctionExpression by a
				 * DefinitionFunctionExpression. If not enough parameters were
				 * given this will be found by a later check, i.e.
				 * DefinitionUsageCheck.
				 */
				final ADefinitionExpression newNode = replaceWithDefExpression(node, identifier, paramList);

				final List<PExpression> copy = newNode.getParameters();
				for (final PExpression e : copy) {
					e.apply(this);
				}

				return;
			}
		}

		/*
		 * Reached in case that: Identifier of this FunctionExpression is not a
		 * definition or there were more parameters than the definition needs
		 * (by declaration), so we asume the parameters belong to some other
		 * construct (for example a function a level higher in the AST).
		 */
		final List<PExpression> copy = node.getParameters();
		for (final PExpression e : copy) {
			e.apply(this);
		}
	}

	// predicates

	@Override
	public void inAForallPredicate(AForallPredicate node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAForallPredicate(AForallPredicate node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAExistsPredicate(AExistsPredicate node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAExistsPredicate(AExistsPredicate node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inALetPredicatePredicate(ALetPredicatePredicate node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outALetPredicatePredicate(ALetPredicatePredicate node) {
		leaveScope(node.getIdentifiers());
	}

	// expressions

	@Override
	public void inALetExpressionExpression(ALetExpressionExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outALetExpressionExpression(ALetExpressionExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAGeneralSumExpression(AGeneralSumExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAGeneralSumExpression(AGeneralSumExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAGeneralProductExpression(AGeneralProductExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAGeneralProductExpression(AGeneralProductExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAComprehensionSetExpression(AComprehensionSetExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAComprehensionSetExpression(AComprehensionSetExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inASymbolicComprehensionSetExpression(ASymbolicComprehensionSetExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outASymbolicComprehensionSetExpression(ASymbolicComprehensionSetExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAEventBComprehensionSetExpression(AEventBComprehensionSetExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inASymbolicEventBComprehensionSetExpression(ASymbolicEventBComprehensionSetExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outASymbolicEventBComprehensionSetExpression(ASymbolicEventBComprehensionSetExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAQuantifiedUnionExpression(AQuantifiedUnionExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAQuantifiedUnionExpression(AQuantifiedUnionExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inASymbolicQuantifiedUnionExpression(ASymbolicQuantifiedUnionExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outASymbolicQuantifiedUnionExpression(ASymbolicQuantifiedUnionExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAQuantifiedIntersectionExpression(AQuantifiedIntersectionExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAQuantifiedIntersectionExpression(AQuantifiedIntersectionExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inALambdaExpression(ALambdaExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outALambdaExpression(ALambdaExpression node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inASymbolicLambdaExpression(ASymbolicLambdaExpression node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outASymbolicLambdaExpression(ASymbolicLambdaExpression node) {
		leaveScope(node.getIdentifiers());
	}

	// substitutions

	@Override
	public void inAAnySubstitution(final AAnySubstitution node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAAnySubstitution(final AAnySubstitution node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inALetSubstitution(final ALetSubstitution node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outALetSubstitution(final ALetSubstitution node) {
		leaveScope(node.getIdentifiers());
	}

	@Override
	public void inAVarSubstitution(final AVarSubstitution node) {
		enterScope(node.getIdentifiers());
	}

	@Override
	public void outAVarSubstitution(final AVarSubstitution node) {
		leaveScope(node.getIdentifiers());
	}

	private ADefinitionExpression replaceWithDefExpression(final Node node, TIdentifierLiteral identifier,
			final List<PExpression> paramList) {

		final ADefinitionExpression newNode = new ADefinitionExpression();
		newNode.setDefLiteral(identifier);

		if (paramList != null) {
			newNode.setParameters(paramList);
		}

		newNode.setStartPos(node.getStartPos());
		newNode.setEndPos(node.getEndPos());

		node.replaceBy(newNode);

		return newNode;
	}

	private void enterScope(List<? extends Node> identifiers) {
		for (Node node : identifiers) {
			if (node instanceof AIdentifierExpression) {
				String id = Utils.getTIdentifierListAsString(((AIdentifierExpression) node).getIdentifier());
				scopedVariables.merge(id, 1, Integer::sum);
			} else {
				// IGNORE, typechecking is done later
			}
		}
	}

	private void leaveScope(List<? extends Node> identifiers) {
		for (Node node : identifiers) {
			if (node instanceof AIdentifierExpression) {
				String id = Utils.getTIdentifierListAsString(((AIdentifierExpression) node).getIdentifier());
				scopedVariables.computeIfPresent(id, (k, v) -> {
					if (v > 1) {
						return v - 1;
					} else {
						return null;
					}
				});
			} else {
				// IGNORE, typechecking is done later
			}
		}
	}

	private void setTypeSubstDef(AOperationOrDefinitionCallSubstitution node, String idString) {
		final AExpressionDefinitionDefinition oldDefinition = (AExpressionDefinitionDefinition) definitions
				.getDefinition(idString);
		final Node defRhs = oldDefinition.getRhs();
		final PSubstitution rhsSubst;

		if (defRhs instanceof AFunctionExpression) {
			final AFunctionExpression rhsFunction = (AFunctionExpression) defRhs;
			rhsSubst = new AOpSubstitution(rhsFunction.getIdentifier(),
					new LinkedList<>(rhsFunction.getParameters()));
			rhsSubst.setStartPos(rhsFunction.getStartPos());
			rhsSubst.setEndPos(rhsFunction.getEndPos());
		} else if (defRhs instanceof AIdentifierExpression) {
			final AIdentifierExpression rhsIdent = (AIdentifierExpression) defRhs;
			rhsSubst = new AOpSubstitution(rhsIdent, new LinkedList<>());
			rhsSubst.setStartPos(rhsIdent.getStartPos());
			rhsSubst.setEndPos(rhsIdent.getEndPos());
		} else {
			// some other expression was parsed (NOT allowed)
			throw new VisitorException(new CheckException("Expecting operation", node));
		}

		final TIdentifierLiteral oldDefId = oldDefinition.getName();
		final TDefLiteralSubstitution defId = new TDefLiteralSubstitution(oldDefId.getText(), oldDefId.getLine(),
				oldDefId.getPos());
		final ASubstitutionDefinitionDefinition substDef = new ASubstitutionDefinitionDefinition(defId,
				new LinkedList<>(oldDefinition.getParameters()), rhsSubst);
		substDef.setStartPos(oldDefinition.getStartPos());
		substDef.setEndPos(oldDefinition.getEndPos());
		definitions.replaceDefinition(idString, Type.Substitution, substDef);
		oldDefinition.replaceBy(substDef);
	}
}
