package de.prob.prolog.internal;

import java.util.Arrays;

public final class Utils {

	private static final char[] VALID_CHARS = validChars();

	private Utils() {}

	public static boolean isPrologVariable(String name) {
		if (!isPrologIdentifier(name)) {
			return false;
		}

		int first = name.charAt(0);
		return first == '_' || Character.isUpperCase(first);
	}

	public static boolean isPrologAtom(String name) {
		if (!isPrologIdentifier(name)) {
			return false;
		}

		char first = name.charAt(0);
		return first != '_' && !Character.isUpperCase(first);
	}

	private static boolean isPrologIdentifier(String name) {
		if (name == null) {
			return false;
		}

		int len = name.length();
		if (len == 0) {
			return false;
		}

		char first = name.charAt(0);
		if (!isPrologIdentifierStart(first)) {
			return false;
		}

		for (int i = 1; i < len; i++) {
			char c = name.charAt(i);
			if (!isPrologIdentifierPart(c)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isPrologIdentifierStart(char cp) {
		return cp == '_' || ('a' <= cp && cp <= 'z') || ('A' <= cp && cp <= 'Z');
	}

	private static boolean isPrologIdentifierPart(char cp) {
		return cp == '_' || ('a' <= cp && cp <= 'z') || ('A' <= cp && cp <= 'Z') || ('0' <= cp && cp <= '9');
	}

	public static boolean isValidPrologAtom(char c) {
		return Arrays.binarySearch(VALID_CHARS, c) >= 0;
	}

	private static char[] validChars() {
		String buf = "abcdefghijklmnopqrstuvwxyz" +
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
			"0123456789" +
			"_ +-*/^<>=~:.?@#$&!;%(),[]{|}";
		char[] chars = buf.toCharArray();
		Arrays.sort(chars);
		return chars;
	}
}
