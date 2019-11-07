package de.be4.classicalb.core.parser.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AImplementationMachineParseUnit;
import de.be4.classicalb.core.parser.node.APackageParseUnit;
import de.be4.classicalb.core.parser.node.ARefinementMachineParseUnit;
import de.be4.classicalb.core.parser.node.PParseUnit;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.node.TIdentifierLiteral;
import de.hhu.stups.sablecc.patch.SourcePosition;

public final class Utils {
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
		final String string;
		if (idElements.size() == 1) {
			// faster version for the simple case
			string = idElements.get(0).getText();
		} else {
			final StringBuilder idName = new StringBuilder();

			boolean first = true;
			for (final TIdentifierLiteral e : idElements) {
				if (first) {
					first = false;
				} else {
					idName.append('.');
				}
				idName.append(e.getText());
			}
			string = idName.toString();
		}
		return string.trim();
	}

	public static boolean isCompleteMachine(final Start rootNode) {
		final PParseUnit parseUnit = rootNode.getPParseUnit();
		return (parseUnit instanceof AAbstractMachineParseUnit || parseUnit instanceof ARefinementMachineParseUnit
				|| parseUnit instanceof AImplementationMachineParseUnit || parseUnit instanceof APackageParseUnit);
	}

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
		String res = null;
		int i = f.lastIndexOf('.');
		if (i > 0 && i < f.length() - 1) {
			res = f.substring(0, i);
		} else {
			// there is no file name extension
			res = f;
		}
		return res;
	}

	public static final String readFile(final File filePath) throws IOException {
		String content = null;
		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.forName("UTF-8"));
			final StringBuilder builder = new StringBuilder();
			final char[] buffer = new char[1024];
			int read;
			while ((read = inputStreamReader.read(buffer)) >= 0) {
				builder.append(String.valueOf(buffer, 0, read));
			}
			content = builder.toString();
			inputStreamReader.close();
		}

		// remove utf-8 byte order mark
		// replaceAll \uFEFF did not work for some reason
		// apparently, unix like systems report a single character with the code
		// below
		if (!content.isEmpty() && Character.codePointAt(content, 0) == 65279) {
			content = content.substring(1);
		}
		// while windows splits it up into three characters with the codes below
		if (!content.isEmpty() && Character.codePointAt(content, 0) == 239 && Character.codePointAt(content, 1) == 187
				&& Character.codePointAt(content, 2) == 191) {
			content = content.substring(3);
		}

		return content.replaceAll("\r\n", "\n");
	}
	
	/**
	 * Remove surrounding double quotes from a string. This does not handle backslash escapes, use {@link #unescapeStringContents(String)} afterwards to do this.
	 * 
	 * @param literal the string from which to remove the quotes
	 * @return the string without quotes
	 * @throws IllegalArgumentException if the literal is not a double-quoted string
	 */
	public static String removeSurroundingQuotes(final String literal) {
		/// "foo" gets translated to foo
		if (literal.length() < 2 || literal.charAt(0) != '"' || literal.charAt(literal.length()-1) != '"') {
			throw new IllegalArgumentException("removeSurroundingQuotes argument must be a double-quoted string");
		}
		return literal.substring(1, literal.length() - 1);
	}
	
	public static String unescapeStringContents(String contents) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < contents.length();) {
			final char c = contents.charAt(i);
			if (c == '\\') {
				// Start of escape sequence.
				if (i + 1 >= contents.length()) {
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
}
