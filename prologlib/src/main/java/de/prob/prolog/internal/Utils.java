package de.prob.prolog.internal;

public final class Utils {

	private Utils() {}

	public static boolean isPrologVariable(String name) {
		return isPrologVariable(name, false);
	}

	public static boolean isPrologVariable(String name, boolean allowUnicode) {
		if (!isPrologIdentifier(name, allowUnicode)) {
			return false;
		}

		int cp = name.codePointAt(0);
		return cp == '_' || Character.isUpperCase(cp);
	}

	public static boolean isPrologAtom(String name) {
		return isPrologAtom(name, false);
	}

	public static boolean isPrologAtom(String name, boolean allowUnicode) {
		if (!isPrologIdentifier(name, allowUnicode)) {
			return false;
		}

		int cp = name.codePointAt(0);
		return cp != '_' && !Character.isUpperCase(cp);
	}

	private static boolean isPrologIdentifier(String name, boolean allowUnicode) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		int first = name.codePointAt(0);
		if (!isPrologIdentifierStart(first, allowUnicode)) {
			return false;
		}

		int i = Character.charCount(first), len = name.length();
		while (i < len) {
			int cp = name.codePointAt(i);
			if (!isPrologIdentifierPart(cp, allowUnicode)) {
				return false;
			}

			i += Character.charCount(cp);
		}

		return true;
	}

	private static boolean isPrologIdentifierStart(int cp, boolean allowUnicode) {
		if (allowUnicode) {
			return cp == '_' || Character.isUnicodeIdentifierStart(cp);
		} else {
			return cp == '_' || ('a' <= cp && cp <= 'z') || ('A' <= cp && cp <= 'Z');
		}
	}

	private static boolean isPrologIdentifierPart(int cp, boolean allowUnicode) {
		if (allowUnicode) {
			return Character.isUnicodeIdentifierPart(cp);
		} else {
			return cp == '_' || ('a' <= cp && cp <= 'z') || ('A' <= cp && cp <= 'Z') || ('0' <= cp && cp <= '9');
		}
	}
}
