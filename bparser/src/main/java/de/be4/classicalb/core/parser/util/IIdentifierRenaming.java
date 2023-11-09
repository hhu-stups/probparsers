package de.be4.classicalb.core.parser.util;

/**
 * Allows customizing how {@link PrettyPrinter} outputs identifiers.
 * This is intended for handling identifiers in the AST that are not syntactically valid in B or conflict with keywords.
 */
@FunctionalInterface
public interface IIdentifierRenaming {
	/**
	 * Minimal implementation that returns identifiers literally with no renaming or quoting at all.
	 * This will result in invalid syntax if any identifiers in the AST are not valid in B syntax.
	 */
    IIdentifierRenaming LITERAL = id -> id;
	
	/**
	 * Default implementation that adds backquotes around identifiers that are otherwise not valid in B.
	 * This allows outputting invalid identifiers in a way that ProB can parse again, allowing round-tripping of arbitrary identifiers.
	 * Note that the backquote syntax is specific to ProB and not supported by other B tools.
	 */
    IIdentifierRenaming QUOTE_INVALID = id -> {
		if (Utils.isPlainBIdentifier(id)) {
			return id;
		} else {
			return "`" + Utils.escapeStringContents(id) + "`";
		}
	};
	
	/**
	 * <p>
	 * Determines how to represent the given identifier in the pretty-printed output.
	 * </p>
	 * <p>
	 * Note that for qualified identifiers (e. g. {@code Mch.var}),
	 * this method is called separately for each part of the identifier (e. g. {@code Mch} and {@code var}).
	 * This matches the behavior of ProB's B parser,
	 * which treats each part of a qualified identifier as a separate identifier token.
	 * </p>
	 * <p>
	 * {@link PrettyPrinter} calls this method every time an identifier is encountered.
	 * Complex implementations should consider caching the renamings for better performance.
	 * </p>
	 * 
	 * @param identifier the original identifier encountered by the pretty-printer
	 * @return a possibly different identifier to use in place of the original identifier
	 */
    String renameIdentifier(String identifier);
}
