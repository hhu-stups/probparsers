package de.prob.prolog.internal;

public final class Utils {

	private Utils() {}

	public static boolean isPrologVariable(String name) {
		if (!isPrologIdentifier(name)) {
			return false;
		}
		int c = name.codePointAt(0);
		return c == '_' || Character.isUpperCase(c);
	}

	public static boolean isPrologIdentifier(String name) {
		if (name == null || name.isEmpty()) {
			return false;
		}

		int c = name.codePointAt(0);
		if (c != '_' && !Character.isUnicodeIdentifierStart(c)) {
			return false;
		}
		return name.codePoints().skip(1).allMatch(Character::isUnicodeIdentifierPart);
	}
}
