package de.prob.prolog.internal;

public final class Utils {

	private Utils() {}

	public static boolean isPrologVariable(String name) {
		if (!isPrologIdentifier(name)) {
			return false;
		}

		int cp = name.codePointAt(0);
		return cp == '_' || Character.isUpperCase(cp);
	}

	public static boolean isPrologIdentifier(String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		int first = name.codePointAt(0);
		if (first != '_' && !Character.isUnicodeIdentifierStart(first)) {
			return false;
		}

		int i = Character.charCount(first), len = name.length();
		while (i < len) {
			int cp = name.codePointAt(i);
			if (!Character.isUnicodeIdentifierPart(cp)) {
				return false;
			}

			i += Character.charCount(cp);
		}

		return true;
	}
}
