package de.be4.classicalb.core.parser.analysis.transforming;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.VisitorException;
import de.be4.classicalb.core.parser.node.*;
import de.be4.classicalb.core.parser.util.Utils;

public class SyntaxExtensionTranslator extends OptimizedTraversingAdapter {

	private static String cleanDescriptionText(final String descriptionText) {
		String formatted = descriptionText;
		if (descriptionText.endsWith("*/")) {
			formatted = formatted.substring(0, formatted.length() - 2);
		}
		return formatted.trim();
	}

	/**
	 * Cleans up the text contents of description pragma nodes by removing the end of comment symbols and any whitespace surrounding the description.
	 *
	 * @param node the description pragma node to clean
	 */
	private static void cleanDescriptionNode(final TPragmaFreeText node) {
		node.setText(cleanDescriptionText(node.getText()));
	}

	private static void checkArgumentCount(final AFunctionExpression node, final int count) {
		int paramCount = node.getParameters().size();
		if (paramCount != count) {
			final String funcName = Utils.getAIdentifierAsString((AIdentifierExpression) node.getIdentifier());
			throw new VisitorException(new CheckException("Built-in function " + funcName + " expects exactly " + count + " argument(s), but got " + paramCount, node));
		}
	}

	private static PExpression checkSingleArgument(final AFunctionExpression node) {
		checkArgumentCount(node, 1);
		return node.getParameters().get(0);
	}

	private PPredicate rewriteIfPredicate(PPredicate condition, PPredicate thenBlock, List<PPredicate> elsifs, PPredicate elseBlock) {
		// IF P1 THEN P2 ELSIF P3 THEN P4 ... ELSE Pn END
		// is equivalent to:
		// IF P1 THEN P2 ELSE (IF P3 THEN P4 ... ELSE Pn END) END
		// and that will be translated into:
		// (P1 => P2) & (not(P1) => [(P3 => P4) & (not(P3) => Pn)])
		AImplicationPredicate imp1 = new AImplicationPredicate(condition.clone(), thenBlock.clone());
		imp1.setStartPos(condition.getStartPos());
		imp1.setEndPos(thenBlock.getEndPos());

		PPredicate realElseBlock;
		if (elsifs.isEmpty()) {
			realElseBlock = elseBlock.clone();
		} else {
			AIfElsifPredicatePredicate first = (AIfElsifPredicatePredicate) elsifs.remove(0);
			realElseBlock = rewriteIfPredicate(first.getCondition(), first.getThen(), elsifs, elseBlock);
		}

		AImplicationPredicate imp2 = new AImplicationPredicate(new ANegationPredicate(condition.clone()), realElseBlock);
		return new AConjunctPredicate(imp1, imp2);
	}

	@Override
	public void outAIfPredicatePredicate(AIfPredicatePredicate node) {
		PPredicate result = rewriteIfPredicate(node.getCondition(), node.getThen(), new ArrayList<>(node.getElsifs()), node.getElse());
		result.setStartPos(node.getStartPos());
		result.setEndPos(node.getEndPos());
		node.replaceBy(result);
	}

	@Override
	public void outAMultilineStringExpression(AMultilineStringExpression node) {
		final TMultilineStringContent content = node.getContent();
		final String text = content.getText();
		// multiline strings do not have surrounding "
		final String unescaped = Utils.unescapeStringContents(text);

		AStringExpression stringNode = new AStringExpression(new TStringLiteral(unescaped, content.getLine(), content.getPos()));
		stringNode.setStartPos(node.getStartPos());
		stringNode.setEndPos(node.getEndPos());
		node.replaceBy(stringNode);
	}

	@Override
	public void caseTStringLiteral(TStringLiteral node) {
		// Remove the surrounding quotes "..." from the string token content
		// and process backslash escape sequences.
		final String text = node.getText();
		final String unescaped = Utils.unescapeStringContents(Utils.removeSurroundingQuotes(text, '"'));
		node.setText(unescaped);
	}

	@Override
	public void caseTIdentifierLiteral(final TIdentifierLiteral node) {
		unquoteIdentifierToken(node);
	}

	@Override
	public void caseTDefLiteralPredicate(final TDefLiteralPredicate node) {
		unquoteIdentifierToken(node);
	}

	@Override
	public void caseTDefLiteralSubstitution(final TDefLiteralSubstitution node) {
		unquoteIdentifierToken(node);
	}

	private static void unquoteIdentifierToken(final Token token) {
		final String text = token.getText();
		// Unquote and unescape backquoted identifiers
		if (Utils.isQuoted(text, '`')) {
			if (text.indexOf('.') != -1) {
				final String fixed = String.join("`.`", text.split("\\."));
				throw new VisitorException(new CheckException("A quoted identifier cannot contain a dot. Please quote only the identifiers before and after the dot, but not the dot itself, e. g.: " + fixed, token));
			}
			final String unescapedText = Utils.unescapeStringContents(Utils.removeSurroundingQuotes(text, '`'));
			token.setText(unescapedText);
		}
	}

	@Override
	public void outAHexIntegerExpression(AHexIntegerExpression node) {
		// transform hex_integer into integer case (so that Prolog AST does not have to deal with new node):
		THexLiteral literal = node.getLiteral();
		String text = literal.getText().substring(2);
		BigInteger value = new BigInteger(text, 16);

		// generate an integer literal:
		TIntegerLiteral tIntLiteral = new TIntegerLiteral(value.toString(), literal.getLine(), literal.getPos());
		AIntegerExpression intNode = new AIntegerExpression(tIntLiteral);
		intNode.setStartPos(node.getStartPos());
		intNode.setEndPos(node.getEndPos());
		node.replaceBy(intNode);
	}

	@Override
	public void inADescriptionSet(final ADescriptionSet node) {
		cleanDescriptionNode(node.getPragmaFreeText());
	}

	@Override
	public void inADescriptionPredicate(final ADescriptionPredicate node) {
		cleanDescriptionNode(node.getContent());
	}

	@Override
	public void inADescriptionExpression(final ADescriptionExpression node) {
		cleanDescriptionNode(node.getContent());
	}

	@Override
	public void inADescriptionOperation(final ADescriptionOperation node) {
		cleanDescriptionNode(node.getContent());
	}

	@Override
	public void inADescriptionEvent(ADescriptionEvent node) {
		cleanDescriptionNode(node.getContent());
	}

	/**
	 * Recognize calls to built-in functions that are not declared as keywords.
	 * This allows using the keywords as regular identifiers
	 * everywhere except the left side of a function call
	 * (and there they can be backquoted to suppress the built-in meaning).
	 * This is helpful for rarely used keywords that are also common English words
	 * (e. g. left, right, top).
	 *
	 * @param node the function expression to transform
	 */
	@Override
	public void outAFunctionExpression(final AFunctionExpression node) {
		if (!(node.getIdentifier() instanceof AIdentifierExpression)) {
			return;
		}

		final String funcName = Utils.getAIdentifierAsString((AIdentifierExpression) node.getIdentifier());
		if (Utils.isQuoted(funcName, '`')) {
			// Allow suppressing the built-in function by backquoting the function identifier.
			return;
		}

		final Node replacement;
		switch (funcName) {
			case "tree":
				replacement = new ATreeExpression(checkSingleArgument(node));
				break;

			case "btree":
				replacement = new ABtreeExpression(checkSingleArgument(node));
				break;

			case "const":
				checkArgumentCount(node, 2);
				replacement = new AConstExpression(node.getParameters().get(0), node.getParameters().get(1));
				break;

			case "top":
				replacement = new ATopExpression(checkSingleArgument(node));
				break;

			case "sons":
				replacement = new ASonsExpression(checkSingleArgument(node));
				break;

			case "prefix":
				replacement = new APrefixExpression(checkSingleArgument(node));
				break;

			case "postfix":
				replacement = new APostfixExpression(checkSingleArgument(node));
				break;

			case "sizet":
				replacement = new ASizetExpression(checkSingleArgument(node));
				break;

			case "mirror":
				replacement = new AMirrorExpression(checkSingleArgument(node));
				break;

			case "rank":
				checkArgumentCount(node, 2);
				replacement = new ARankExpression(node.getParameters().get(0), node.getParameters().get(1));
				break;

			case "father":
				checkArgumentCount(node, 2);
				replacement = new AFatherExpression(node.getParameters().get(0), node.getParameters().get(1));
				break;

			case "son":
				checkArgumentCount(node, 3);
				replacement = new ASonExpression(node.getParameters().get(0), node.getParameters().get(1), node.getParameters().get(2));
				break;

			case "subtree":
				checkArgumentCount(node, 2);
				replacement = new ASubtreeExpression(node.getParameters().get(0), node.getParameters().get(1));
				break;

			case "arity":
				checkArgumentCount(node, 2);
				replacement = new AArityExpression(node.getParameters().get(0), node.getParameters().get(1));
				break;

			case "bin":
				int paramCount = node.getParameters().size();
				if (paramCount == 1) {
					replacement = new ABinExpression(node.getParameters().get(0), null, null);
				} else if (paramCount == 3) {
					replacement = new ABinExpression(node.getParameters().get(0), node.getParameters().get(1), node.getParameters().get(2));
				} else {
					throw new VisitorException(new CheckException("Built-in function " + funcName + " expects 1 or 3 arguments, but got " + paramCount, node));
				}
				break;

			case "left":
				replacement = new ALeftExpression(checkSingleArgument(node));
				break;

			case "right":
				replacement = new ARightExpression(checkSingleArgument(node));
				break;

			case "infix":
				replacement = new AInfixExpression(checkSingleArgument(node));
				break;

			default:
				return;
		}

		replacement.setStartPos(node.getStartPos());
		replacement.setEndPos(node.getEndPos());
		node.replaceBy(replacement);
	}
}
