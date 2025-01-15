package de.be4.classicalb.core.parser.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.*;
import de.hhu.stups.sablecc.patch.SourcePosition;

public final class Utils {
	private static final Set<String> AMBIGUOUS_KEYWORDS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		// Tree-related keywords/operators/functions - handled in SyntaxExtensionTranslator
		"tree",
		"btree",
		"const",
		"top",
		"sons",
		"prefix",
		"postfix",
		"sizet",
		"mirror",
		"rank",
		"father",
		"son",
		"subtree",
		"arity",
		"bin",
		"left",
		"right",
		"infix"
	)));

	private static final Map<Character, Character> STRING_ESCAPE_REPLACEMENTS;
	private static final Map<Character, Character> STRING_ESCAPE_REPLACEMENTS_REVERSE;

	static {
		// replacements in strings '\' + ..
		// e.g. '\' + 'n' is replaced by '\n'
		final Map<Character, Character> stringEscapeReplacements = new HashMap<>();
		stringEscapeReplacements.put('"', '"');
		stringEscapeReplacements.put('\'', '\'');
		stringEscapeReplacements.put('n', '\n');
		stringEscapeReplacements.put('r', '\r');
		stringEscapeReplacements.put('t', '\t');
		stringEscapeReplacements.put('\\', '\\');
		STRING_ESCAPE_REPLACEMENTS = Collections.unmodifiableMap(stringEscapeReplacements);

		final Map<Character, Character> stringEscapeReplacementsReverse = new HashMap<>();
		for (final Map.Entry<Character, Character> entry : stringEscapeReplacements.entrySet()) {
			stringEscapeReplacementsReverse.put(entry.getValue(), entry.getKey());
		}
		STRING_ESCAPE_REPLACEMENTS_REVERSE = Collections.unmodifiableMap(stringEscapeReplacementsReverse);
	}

	private Utils() {
	}

	public static String getAIdentifierAsString(AIdentifierExpression idExpr) {
		return getTIdentifierListAsString(idExpr.getIdentifier());
	}

	public static String getTIdentifierListAsString(final List<TIdentifierLiteral> idElements) {
		if (idElements.size() == 1) {
			// faster version for the simple case
			return idElements.get(0).getText();
		} else {
			return idElements.stream()
				.map(Token::getText)
				.collect(Collectors.joining("."));
		}
	}

	/**
	 * Check whether the given identifier is an ambiguous keyword.
	 * Such a keyword can usually be used as a regular identifier,
	 * but is recognized as a keyword in specific contexts.
	 * To guarantee that such a keyword is unambiguously parsed as a regular identifier,
	 * it must be backquoted, like when using any other keyword as an identifier.
	 *
	 * @param identifier the string to check
	 * @return whether {@code identifier} is an ambiguous keyword
	 */
	public static boolean isAmbiguousKeyword(String identifier) {
		return AMBIGUOUS_KEYWORDS.contains(identifier);
	}

	/**
	 * Check whether the given identifier is a valid plain B identifier
	 * that does not need to be quoted.
	 *
	 * @param identifier the string to check
	 * @return whether {@code identifier} is a plain B identifier
	 */
	public static boolean isPlainBIdentifier(final String identifier) {
		if (isAmbiguousKeyword(identifier)) {
			return false;
		}

		// Try to parse the identifier string
		// and check if it results in a single identifier expression
		// with the same name as was passed in.
		// FIXME This is a bit inefficient, because it uses the full parser.
		// We can't use just the lexer,
		// because some keywords (such as "floor") are lexed as identifier literals
		// and only later recognized as keywords/operators by the parser.
		final Start ast;
		try {
			ast = new BParser().parseExpression(identifier);
		} catch (final BCompoundException ignored) {
			return false;
		}
		final PExpression expr = ((AExpressionParseUnit) ast.getPParseUnit()).getExpression();
		if (!(expr instanceof AIdentifierExpression)) {
			return false;
		}
		final List<TIdentifierLiteral> parsedId = ((AIdentifierExpression) expr).getIdentifier();
		return parsedId.size() == 1 && identifier.equals(parsedId.get(0).getText());
	}

	/**
	 * <p>
	 * Check whether a definition name has some kind of special meaning to ProB.
	 * A definition with such a name should be preserved in the AST even if it seems to be otherwise unused.
	 * </p>
	 * <p>
	 * The name patterns for special definitions are <i>not</i> fixed or stable.
	 * The exact names checked by this method <i>will</i> change in future releases
	 * as new special definitions are added to ProB.
	 * They are also declared in prob_prolog in bvisual2.pl and in main_prob_tcltk_gui.tcl
	 * </p>
	 * <p>
	 * This method is currently not used by the B parser itself.
	 * It is meant for use by other libraries, such as tla2bAST and tlc4b.
	 * </p>
	 *
	 * @param identifier the definition name to check
	 * @return whether the given definition name has special meaning to ProB
	 */
	public static boolean isProBSpecialDefinitionName(String identifier) {
		return "GOAL".equals(identifier)
			|| "SHIELD_INTERVENTION".equals(identifier) // SimB reinforcement learning shield (in ProB 2 UI)
			|| "VISB_JSON_FILE".equals(identifier)
			|| "VISB_DEFINITIONS_FILE".equals(identifier)
			|| "VISB_SVG_FILE".equals(identifier)
			|| identifier.startsWith("ANIMATION_") // ANIMATION_FUNCTION, ANIMATION_IMGxxx
			|| identifier.startsWith("ASSERT_CTL")
			|| identifier.startsWith("ASSERT_LTL")
			|| identifier.equals("CUSTOM_GRAPH")
			|| identifier.startsWith("CUSTOM_GRAPH_") // CUSTOM_GRAPH_NODES, CUSGOM_GRAPH_EDGES
			|| identifier.startsWith("FORCE_SYMMETRY_")
			|| identifier.startsWith("GAME_") // GAME_OVER, GAME_PLAYER, GAME_MCTS_RUNS
			|| identifier.startsWith("HEURISTIC_FUNCTION")
			|| identifier.startsWith("MAX_OPERATIONS_")
			|| identifier.startsWith("OPERATION_REUSE_OFF_")
			|| identifier.equals("PROB_REQUIRED_VERSION")
			|| identifier.equals("SCOPE")
			|| identifier.startsWith("scope_")
			|| identifier.startsWith("SET_PREF_")
			|| identifier.startsWith("SEQUENCE_CHART_")
			|| identifier.startsWith("VISB_SVG_") // VISB_SVG_OBJECTS, VISB_SVG_UPDATES, VISB_SVG_HOVERS, VISB_SVG_BOX, ...
			;
	}

	public static boolean isCompleteMachine(final Start rootNode) {
		final PParseUnit parseUnit = rootNode.getPParseUnit();
		return (parseUnit instanceof AAbstractMachineParseUnit || parseUnit instanceof ARefinementMachineParseUnit
			|| parseUnit instanceof AImplementationMachineParseUnit || parseUnit instanceof APackageParseUnit);
	}

	@Deprecated
	public static String getSourcePositionAsString(SourcePosition sourcePos) {
		return "[" + sourcePos.getLine() + "," + sourcePos.getPos() + "]";
	}

	public static <T> List<T> sortByTopologicalOrder(final Map<T, Set<T>> dependencies) {
		final Set<T> allValues = new HashSet<>(dependencies.keySet());
		ArrayList<T> sortedList = new ArrayList<>();
		boolean newRun = true;
		while (newRun) {
			newRun = false;
			final ArrayList<T> todo = new ArrayList<>(allValues);
			todo.removeAll(sortedList);
			for (T element : todo) {
				Set<T> deps = new HashSet<>(dependencies.get(element));
				deps.removeAll(sortedList);
				if (deps.isEmpty()) {
					sortedList.add(element);
					newRun = true;
				}
			}
		}
		return sortedList;
	}

	public static <T> List<T> determineCycle(final Set<T> remaining, final Map<T, Set<T>> dependencies) {
		ArrayList<T> cycle = new ArrayList<>();
		Set<T> set = new HashSet<>(remaining);
		boolean newRun = true;
		while (newRun) {
			for (T next : set) {
				if (cycle.contains(next)) {
					newRun = false;
					cycle.add(next);
					break;
				} else if (remaining.contains(next)) {
					cycle.add(next);
					set = new HashSet<>(dependencies.get(next));
					break;
				}
			}
		}
		return cycle;
	}

	public static String getFileWithoutExtension(String f) {
		int i = f.lastIndexOf('.');
		if (i > 0 && i < f.length() - 1) {
			return f.substring(0, i);
		} else {
			// there is no file name extension
			return f;
		}
	}

	public static String readFile(final File filePath) throws IOException {
		return readFile(filePath.toPath());
	}

	private static String readFile(final Path filePath) throws IOException {
		// For now, we accept files that aren't valid UTF-8 and silently replace non-UTF-8 bytes,
		// because some existing machines have comments containing non-ASCII characters in legacy encodings (ISO 8859-1, Windows-1252, MacRoman, etc.).
		// In the future, we should disallow this and report non-UTF-8 input as an error.
		// we want to keep line ending information, thus we cannot use "BufferedReader#readLine()"-based approaches
		// Once we require Java 11, we should consider replacing this method with Files.readString(Path).

		byte[] bytes = Files.readAllBytes(filePath);

		int length = bytes.length;
		int offset = 0;

		// remove utf-8 byte order mark
		if (length >= 3 && bytes[0] == (byte) 0xef && bytes[1] == (byte) 0xbb && bytes[2] == (byte) 0xbf) {
			offset = 3;
			length -= 3;
		}

		return new String(bytes, offset, length, StandardCharsets.UTF_8);
	}

	public static boolean isQuoted(final String string, final char quote) {
		return string.length() >= 2 && string.charAt(0) == quote && string.charAt(string.length() - 1) == quote;
	}

	/**
	 * Remove surrounding quote characters from a string. This does not handle backslash escapes, use {@link #unescapeStringContents(String)} afterwards to do this.
	 *
	 * @param literal the string from which to remove the quotes
	 * @param quote   the quote character to remove
	 * @return the string without quotes
	 * @throws IllegalArgumentException if the literal is not a double-quoted string
	 */
	public static String removeSurroundingQuotes(final String literal, final char quote) {
		// e. g. if quote = '"', then "foo" gets translated to foo
		if (!isQuoted(literal, quote)) {
			throw new IllegalArgumentException(String.format("removeSurroundingQuotes argument must be a quoted string: %c...%c", quote, quote));
		}
		return literal.substring(1, literal.length() - 1);
	}

	/**
	 * <p>
	 * Unescape the contents of a string literal (single-line or multiline).
	 * The string quotes must already be removed (e. g. using {@link #removeSurroundingQuotes(String, char)}).
	 * </p>
	 * <p>
	 * In multiline strings,
	 * unescaped newline sequences of the form CRLF ("\r\n") or CR ("\r") are normalized to LF ("\n")
	 * to ensure consistent string values regardless of the newline style used by the source file.
	 * Escaped newline characters written using the backslash escapes "\r" or "\n" are <em>not</em> normalized.
	 * </p>
	 *
	 * @param contents escaped string contents without surrounding quotes
	 * @return unescaped and normalized string
	 */
	public static String unescapeStringContents(String contents) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, len = contents.length(); i < len; ) {
			final char c = contents.charAt(i);
			if (c == '\\') {
				// Start of escape sequence.
				if (i + 1 >= len) {
					throw new IllegalArgumentException("Unescaped backslash at end of string not allowed");
				}
				final char escapedChar = contents.charAt(i + 1);
				if (STRING_ESCAPE_REPLACEMENTS.containsKey(escapedChar)) {
					sb.append(STRING_ESCAPE_REPLACEMENTS.get(escapedChar));
				} else {
					// If the escaped character is not recognized, both the backslash and the character are treated literally.
					sb.append('\\');
					sb.append(escapedChar);
				}
				// Skip over backslash and the following character.
				i += 2;
			} else if (c == '\r') {
				// This is "\r", apply normalization
				sb.append('\n');
				if (i + 1 < len && contents.charAt(i + 1) == '\n') {
					// This is a "\r\n" sequence, we want to replace it by a single "\n"
					i += 2;
				} else {
					// This is a single "\r", we also want to replace it by a single "\n"
					i++;
				}
			} else {
				// Simple unescaped character.
				sb.append(c);
				i++;
			}
		}
		return sb.toString();
	}

	public static String escapeStringContents(final String contents) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < contents.length(); i++) {
			final char c = contents.charAt(i);
			if (STRING_ESCAPE_REPLACEMENTS_REVERSE.containsKey(c)) {
				sb.append('\\');
				sb.append(STRING_ESCAPE_REPLACEMENTS_REVERSE.get(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String unquotePragmaIdentifier(String text) {
		if (isQuoted(text, '"')) {
			return unescapeStringContents(removeSurroundingQuotes(text, '"'));
		}

		return text;
	}

	public static String unquoteIdentifier(final String name) {
		if (isQuoted(name, '`')) {
			if (name.indexOf('.') != -1) {
				String fixed = name.replace(".", "`.`");
				throw new IllegalArgumentException("A quoted identifier cannot contain a dot. Please quote only the identifiers before and after the dot, but not the dot itself, e. g.: " + fixed);
			}
			return unescapeStringContents(removeSurroundingQuotes(name, '`'));
		}
		return name;
	}
}
