package de.be4.classicalb.core.parser.analysis.checking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.be4.classicalb.core.preparser.analysis.DepthFirstAdapter;
import de.be4.classicalb.core.preparser.node.AFilePreParserDefinition;
import de.be4.classicalb.core.preparser.node.APreParserDefinition;
import de.be4.classicalb.core.preparser.node.TPreParserString;
import de.be4.classicalb.core.preparser.node.Token;

/**
 * Collects the {@link APreParserDefinition} nodes which were found by the PreParser and
 * stores them into a mapping "definition identifer" -&gt; "rhs of definition".
 */
public class DefinitionPreCollector extends DepthFirstAdapter {

	private final Map<Token, Token> definitions = new HashMap<>();
	private final List<TPreParserString> fileDefinitions = new ArrayList<>();

	@Override
	public void inAPreParserDefinition(final APreParserDefinition node) {
		definitions.put(node.getDefName(), node.getRhs());
	}

	@Override
	public void inAFilePreParserDefinition(final AFilePreParserDefinition node) {
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

	public List<TPreParserString> getFileDefinitions() {
		return fileDefinitions;
	}
}
