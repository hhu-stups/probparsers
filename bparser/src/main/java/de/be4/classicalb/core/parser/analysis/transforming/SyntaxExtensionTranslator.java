package de.be4.classicalb.core.parser.analysis.transforming;

import java.util.HashMap;
import java.util.Map;

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

import static de.be4.classicalb.core.parser.util.NodeCloner.cloneNode;

public class SyntaxExtensionTranslator extends DepthFirstAdapter {

	private static Map<Character, Character> stringReplacements = new HashMap<>();
	
	static {
		// replacements in strings '\' + ..
		// e.g. '\' + 'n' is replaced by '\n'
		stringReplacements.put('"', '"');
		stringReplacements.put('\'', '\'');
		stringReplacements.put('n', '\n');
		stringReplacements.put('r', '\r');
		stringReplacements.put('t', '\t');
		stringReplacements.put('\\', '\\');
	}
	
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
		TStringLiteral tStringLiteral = new TStringLiteral(escapeString(text,false), 
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
			new TStringLiteral(escapeString(text,true), content.getLine(), content.getPos());
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
		int value = Integer.valueOf(text, 16);
		// generate an integer literal:
		TIntegerLiteral tIntLiteral =
			new TIntegerLiteral(Integer.toString(value), literal.getLine(), literal.getPos());
		AIntegerExpression intNode = new AIntegerExpression(tIntLiteral);
		intNode.setStartPos(node.getStartPos());
		intNode.setEndPos(node.getEndPos());
		node.replaceBy(intNode);
	}
	
	
	private static String escapeString(String literal, Boolean removeSurroundingQuotes) {
		/*
		 * Note, the text of a TMultilineString token does not start with '''
		 * because the ''' are contained in the TMultilineStringStartEnd token
		 */
		
		if (removeSurroundingQuotes && literal.startsWith("\"")) {
			/// we assume literal also ends with \", if string contains less than two characters we get an exception !
			/// "foo" gets translated to foo
			literal = literal.substring(1, literal.length() - 1);
		}

		boolean backslashFound = false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < literal.length(); i++) {
			char c = literal.charAt(i);
			if (backslashFound && stringReplacements.containsKey(c)) {
				sb.setLength(sb.length() - 1); // remove backslash
				sb.append(stringReplacements.get(c)); // and replace by this
				backslashFound = false;
				continue;
			}
			if (c == '\\') {
				backslashFound = true;
			} else {
				backslashFound = false;
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	
}
