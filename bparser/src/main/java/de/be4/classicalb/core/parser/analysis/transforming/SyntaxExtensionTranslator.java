package de.be4.classicalb.core.parser.analysis.transforming;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.AConjunctPredicate;
import de.be4.classicalb.core.parser.node.AHexIntegerExpression;
import de.be4.classicalb.core.parser.node.AIfPredicatePredicate;
import de.be4.classicalb.core.parser.node.AImplicationPredicate;
import de.be4.classicalb.core.parser.node.AIntegerExpression;
import de.be4.classicalb.core.parser.node.AMultilineStringExpression;
import de.be4.classicalb.core.parser.node.ANegationPredicate;
import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.THexLiteral;
import de.be4.classicalb.core.parser.node.TIntegerLiteral;
import de.be4.classicalb.core.parser.node.TMultilineStringContent;
import de.be4.classicalb.core.parser.node.TStringLiteral;
import de.be4.classicalb.core.parser.util.Utils;
import java.math.BigInteger;

import static de.be4.classicalb.core.parser.util.NodeCloner.cloneNode;

public class SyntaxExtensionTranslator extends DepthFirstAdapter {
	@Override
	public void outAIfPredicatePredicate(AIfPredicatePredicate node) {
		// IF P THE P2 ELSE P3 END
		// will be translated into
		// (p => p2) & (not(p) => p3)
		AImplicationPredicate imp1 = new AImplicationPredicate(cloneNode(node.getCondition()),
				cloneNode(node.getThen()));
		AImplicationPredicate imp2 = new AImplicationPredicate(
				new ANegationPredicate(cloneNode(node.getCondition())),
				cloneNode(node.getElse()));
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
	public void caseAStringExpression(AStringExpression node) {
	// fix the fact that String content does not contain the two quotes "..." as content
		TStringLiteral content = node.getContent();
		String text = content.getText();
		TStringLiteral tStringLiteral =
			// for normal string literals we also get the surrounding quotes " as part of the token
			// these need to be removed and the escaping codes dealt with
			new TStringLiteral(Utils.unescapeStringContents(Utils.removeSurroundingQuotes(text)), content.getLine(), content.getPos());
		AStringExpression stringNode = new AStringExpression(tStringLiteral);
		stringNode.setStartPos(node.getStartPos());
		stringNode.setEndPos(node.getEndPos());
		node.replaceBy(stringNode);
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
}
