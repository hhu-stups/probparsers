package de.prob.translator;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Node;
import de.prob.translator.types.BObject;

/**
 * @deprecated As of version 2.9.22, replaced by <a href="https://github.com/hhu-stups/value-translator">value-translator</a>.
 *
 * @see <a href="https://github.com/hhu-stups/value-translator">https://github.com/hhu-stups/value-translator</a>
 */
@Deprecated
public class Translator {
	public static BObject translate(String s) throws BCompoundException {
		Node ast = BParser.parse("#EXPRESSION" + s);
		TranslatingVisitor v = new TranslatingVisitor();
		ast.apply(v);
		return v.getResult();
	}
}
