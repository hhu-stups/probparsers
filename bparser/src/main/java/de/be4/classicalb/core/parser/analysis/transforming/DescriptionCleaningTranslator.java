package de.be4.classicalb.core.parser.analysis.transforming;

import de.be4.classicalb.core.parser.analysis.OptimizedTraversingAdapter;
import de.be4.classicalb.core.parser.node.ADescriptionExpression;
import de.be4.classicalb.core.parser.node.ADescriptionPredicate;
import de.be4.classicalb.core.parser.node.ADescriptionSet;
import de.be4.classicalb.core.parser.node.TPragmaFreeText;

/**
 * Cleans up the text contents of description pragma nodes {@link TPragmaFreeText} by removing the end of comment symbols and any whitespace surrounding the description.
 */
public final class DescriptionCleaningTranslator extends OptimizedTraversingAdapter {
	public DescriptionCleaningTranslator() {
		super();
	}
	
	private static String cleanDescriptionText(final String descriptionText) {
		String formatted = descriptionText;
		if (descriptionText.endsWith("*/")) {
			formatted = formatted.substring(0, formatted.length() - 2);
		}
		return formatted.trim();
	}
	
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
}
