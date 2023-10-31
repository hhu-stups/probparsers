package de.prob.prolog.internal;

import java.util.Arrays;

public final class Utils {

	private static final char[] VALID_CHARS = validChars();

	private Utils() {}

	public static boolean isPrologVariable(String name) {
		if (name == null) {
			return false;
		}

		int len = name.length();
		if (len == 0) {
			return false;
		}

		char first = name.charAt(0);
		if (first != '_' && ('A' > first || first > 'Z')) {
			return false;
		}

		for (int i = 1; i < len; i++) {
			char c = name.charAt(i);
			if (isInvalidPrologIdentifierPart(c)) {
				return false;
			}
		}

		return true;
	}

	public static boolean isPrologAtom(String name) {
		if (name == null) {
			return false;
		}

		int len = name.length();
		if (len == 0) {
			return false;
		}

		char first = name.charAt(0);
		if ('a' > first || first > 'z') {
			return false;
		}

		for (int i = 1; i < len; i++) {
			char c = name.charAt(i);
			if (isInvalidPrologIdentifierPart(c)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isInvalidPrologIdentifierPart(char c) {
		return c != '_' && ('a' > c || c > 'z') && ('A' > c || c > 'Z') && ('0' > c || c > '9');
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
