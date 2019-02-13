package de.be4.classicalb.core.parser.analysis.transforming;

import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.parser.node.AConjunctPredicate;
import de.be4.classicalb.core.parser.node.AIfPredicatePredicate;
import de.be4.classicalb.core.parser.node.AImplicationPredicate;
import de.be4.classicalb.core.parser.node.AMultilineStringExpression;
import de.be4.classicalb.core.parser.node.ANegationPredicate;
import de.be4.classicalb.core.parser.node.AStringExpression;
import de.be4.classicalb.core.parser.node.TMultilineStringContent;
import de.be4.classicalb.core.parser.node.TStringLiteral;

import static de.be4.classicalb.core.parser.util.NodeCloner.cloneNode;
import de.hhu.stups.sablecc.patch.*; // for SourcePosition
import java.util.HashMap;
import java.util.Map;

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
	
	
	private static String escapeString(String literal, Boolean remove_surrounding_quotes) {
		// google for howto-unescape-a-java-string-literal-in-java
		// quickfix: we do nothing just strip off the " if surrounding_quotes is true,
		// we now also convert escape codes using stringReplacements
		/*
		 * Note, the text of a TMultilineString token does not start with '''
		 * because the ''' are contained in the TMultilineStringStartEnd token
		 */
		// System.out.println("string token literal = " + literal + " length = " + literal.length());
		
		if (remove_surrounding_quotes && literal.startsWith("\"")) {
			/// we assume literal also ends with \", if string contains less than two characters we get an exception !
			/// "foo" gets translated to foo
			literal = literal.substring(1, literal.length() - 1);
		}
		// System.out.println("string token literal after = " + literal + " length = " + literal.length());

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
