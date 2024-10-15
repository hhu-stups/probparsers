package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.be4.classicalb.core.parser.util.Utils;
import de.be4.classicalb.core.preparser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.preparser.node.ADefinition;
import de.be4.classicalb.core.preparser.node.AFileDefinition;
import de.be4.classicalb.core.preparser.node.TStringLiteral;
import de.be4.classicalb.core.preparser.node.Token;

/**
 * Collects the {@link ADefinition} nodes which were found by the PreParser and
 * stores them into a mapping "definition identifer" -&gt; "rhs of definition".
 */
public class DefinitionPreCollector extends DepthFirstAdapter {

	private final Map<Token, Token> definitions = new HashMap<>();
	private final List<TStringLiteral> fileDefinitions = new ArrayList<>();

	@Override
	public void inADefinition(final ADefinition node) {
		Token defName = node.getDefName();
		defName.setText(Utils.unquoteIdentifier(defName.getText()));
		definitions.put(defName, node.getRhs());
	}

	@Override
	public void inAFileDefinition(final AFileDefinition node) {
		fileDefinitions.add(node.getFilename());
	}

	/**
	 * Returns the result of this DFS visitor, i.e. a mapping "definition
	 * identifier" -&gt; "rhs of definition"
	 * 
	 * @return
	 * 		a mapping "definition identifier" to "right hand side of definition"
	 */
	public Map<Token, Token> getDefinitions() {
		return definitions;
	}

	public List<TStringLiteral> getFileDefinitions() {
		return fileDefinitions;
	}
}
