package de.be4.classicalb.core.parser.analysis.transforming;

import java.math.BigInteger;

import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.VisitorException;
import de.be4.classicalb.core.parser.node.AArityExpression;
import de.be4.classicalb.core.parser.node.ABinExpression;
import de.be4.classicalb.core.parser.node.ABtreeExpression;
import de.be4.classicalb.core.parser.node.AConjunctPredicate;
import de.be4.classicalb.core.parser.node.AConstExpression;
import de.be4.classicalb.core.parser.node.ADescriptionExpression;
import de.be4.classicalb.core.parser.node.ADescriptionPredicate;
import de.be4.classicalb.core.parser.node.ADescriptionSet;
import de.be4.classicalb.core.parser.node.AFatherExpression;
import de.be4.classicalb.core.parser.node.AFunctionExpression;
import de.be4.classicalb.core.parser.node.AHexIntegerExpression;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIfPredicatePredicate;
import de.be4.classicalb.core.parser.node.AImplicationPredicate;
import de.be4.classicalb.core.parser.node.AInfixExpression;
import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.ALeftExpression;
import de.be4.classicalb.core.parser.node.AMirrorExpression;
import de.be4.classicalb.core.parser.node.AMultilineStringExpression;
import de.be4.classicalb.core.parser.node.ANegationPredicate;
import de.be4.classicalb.core.parser.node.APostfixExpression;
import de.be4.classicalb.core.parser.node.APrefixExpression;
import de.be4.classicalb.core.parser.node.ARankExpression;
import de.be4.classicalb.core.parser.node.ARightExpression;
import de.be4.classicalb.core.parser.node.ASizetExpression;
import de.be4.classicalb.core.parser.node.ASonExpression;
import de.be4.classicalb.core.parser.node.ASonsExpression;
import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.ASubtreeExpression;
import de.be4.classicalb.core.parser.node.ATopExpression;
import de.be4.classicalb.core.parser.node.ATreeExpression;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.THexLiteral;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.be4.classicalb.core.parser.node.TIntegerLiteral;
import de.be4.classicalb.core.parser.node.TMultilineStringContent;
import de.be4.classicalb.core.parser.node.TPragmaFreeText;
import de.be4.classicalb.core.parser.node.TStringLiteral;
import de.be4.classicalb.core.parser.util.Utils;

public class SyntaxExtensionTranslator extends OptimizedTraversingAdapter {
	@Override
	public void outAIfPredicatePredicate(AIfPredicatePredicate node) {
		// IF P THE P2 ELSE P3 END
		// will be translated into
		// (p => p2) & (not(p) => p3)
		AImplicationPredicate imp1 = new AImplicationPredicate(node.getCondition().clone(), node.getThen().clone());
		AImplicationPredicate imp2 = new AImplicationPredicate(
				new ANegationPredicate(node.getCondition().clone()),
				node.getElse().clone());
		AConjunctPredicate con = new AConjunctPredicate(imp1, imp2);
		con.setStartPos(node.getStartPos());
		con.setEndPos(node.getEndPos());
		node.replaceBy(con);
	}

	@Override
	public void caseAMultilineStringExpression(AMultilineStringExpression node) {
		final TMultilineStringContent content = node.getContent();
		final String text = content.getText();
			// multiline strings do not have surrounding "
		TStringLiteral tStringLiteral = new TStringLiteral(Utils.unescapeStringContents(text),
				content.getLine(), content.getPos());
		AStringExpression stringNode = new AStringExpression(tStringLiteral);
		stringNode.setStartPos(node.getStartPos());
		stringNode.setEndPos(node.getEndPos());
		node.replaceBy(stringNode);
	}


	@Override
	public void caseTStringLiteral(TStringLiteral node) {
		// Remove the surrounding quotes "..." from the string token content
		// and process backslash escape sequences.
		final String text = node.getText();
		final String unescapedText = Utils.unescapeStringContents(Utils.removeSurroundingQuotes(text, '"'));
		node.replaceBy(new TStringLiteral(unescapedText, node.getLine(), node.getPos()));
	}
	
	@Override
	public void caseTIdentifierLiteral(final TIdentifierLiteral node) {
		final String text = node.getText();
		// Unquote and unescape backquoted identifiers
		if (text.startsWith("`") && text.endsWith("`")) {
			if (text.indexOf('.') != -1) {
				final String fixed = String.join("`.`", text.split("\\."));
				throw new VisitorException(new CheckException("A quoted identifier cannot contain a dot. Please quote only the identifiers before and after the dot, but not the dot itself, e. g.: " + fixed, node));
			}
			final String unescapedText = Utils.unescapeStringContents(Utils.removeSurroundingQuotes(text, '`'));
			node.replaceBy(new TIdentifierLiteral(unescapedText, node.getLine(), node.getPos()));
		}
	}
	
	@Override
	public void caseAHexIntegerExpression(AHexIntegerExpression node) {
	// transform hex_integer into integer case (so that Prolog AST does not have to deal with new node):
		THexLiteral literal = node.getLiteral();
		String text = literal.getText().substring(2);
		//int value = Integer.valueOf(text, 16);
		BigInteger value = new BigInteger(text,16);
		// generate an integer literal:
		TIntegerLiteral tIntLiteral =
			new TIntegerLiteral(value.toString(), literal.getLine(), literal.getPos());
		AIntegerExpression intNode = new AIntegerExpression(tIntLiteral);
		intNode.setStartPos(node.getStartPos());
		intNode.setEndPos(node.getEndPos());
		node.replaceBy(intNode);
	}
	
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
	
	private static void checkArgumentCount(final AFunctionExpression node, final int count) {
		if (node.getParameters().size() != count) {
			final String funcName = Utils.getAIdentifierAsString((AIdentifierExpression)node.getIdentifier());
			throw new VisitorException(new CheckException("Built-in function " + funcName + " expects exactly " + count + " arguments, but got " + node.getParameters().size(), node));
		}
	}
	
	private static PExpression checkSingleArgument(final AFunctionExpression node) {
		if (node.getParameters().size() != 1) {
			final String funcName = Utils.getAIdentifierAsString((AIdentifierExpression)node.getIdentifier());
			throw new VisitorException(new CheckException("Built-in function " + funcName + " expects exactly one argument, but got " + node.getParameters().size(), node));
		}
		return node.getParameters().get(0);
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
	public void caseAFunctionExpression(final AFunctionExpression node) {
		if (!(node.getIdentifier() instanceof AIdentifierExpression)) {
			super.caseAFunctionExpression(node);
			return;
		}
		final String funcName = Utils.getAIdentifierAsString((AIdentifierExpression)node.getIdentifier());
		if (Utils.isQuoted(funcName, '`')) {
			// Allow suppressing the built-in function by backquoting the function identifier.
			super.caseAFunctionExpression(node);
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
				if (node.getParameters().size() == 1) {
					replacement = new ABinExpression(node.getParameters().get(0), null, null);
				} else if (node.getParameters().size() == 3) {
					replacement = new ABinExpression(node.getParameters().get(0), node.getParameters().get(1), node.getParameters().get(2));
				} else {
					throw new VisitorException(new CheckException("Built-in function " + funcName + " expects 1 or 3 arguments, but got " + node.getParameters().size(), node));
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
				super.caseAFunctionExpression(node);
				return;
		}
		
		node.replaceBy(replacement);
		replacement.setStartPos(node.getStartPos());
		replacement.setEndPos(node.getEndPos());
		replacement.apply(this);
	}
}
