package de.prob.prolog.internal;

import java.io.IOException;
import java.io.Writer;

public final class Utils {

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
		if (first != '_' && (first > 'Z' || 'A' > first)) {
			return false;
		}

		for (int i = 1; i < len; i++) {
			if (isInvalidPrologIdentifierChar(name.charAt(i))) {
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
			if (isInvalidPrologIdentifierChar(name.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	private static boolean isInvalidPrologIdentifierChar(char c) {
		return c != '_' && ('a' > c || c > 'z') && ('A' > c || c > 'Z') && ('0' > c || c > '9');
	}

	public static void writeEscapedString(Writer out, String input) throws IOException {
		for (int i = 0, len = input.length(); i < len; i++) {
			final char c = input.charAt(i);
			switch (c) {
				case '\n':
					out.write('\\');
					out.write('n');
					break;
				case '"':
				case '`':
				case '\\':
					out.write('\\');
					out.write(c);
					break;
				case ' ':
				case '!':
				case '#':
				case '$':
				case '%':
				case '&':
				case '\'':
				case '(':
				case ')':
				case '*':
				case '+':
				case ',':
				case '-':
				case '.':
				case '/':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case ':':
				case ';':
				case '<':
				case '=':
				case '>':
				case '?':
				case '@':
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
				case 'H':
				case 'I':
				case 'J':
				case 'K':
				case 'L':
				case 'M':
				case 'N':
				case 'O':
				case 'P':
				case 'Q':
				case 'R':
				case 'S':
				case 'T':
				case 'U':
				case 'V':
				case 'W':
				case 'X':
				case 'Y':
				case 'Z':
				case '[':
				case ']':
				case '^':
				case '_':
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':
				case 'm':
				case 'n':
				case 'o':
				case 'p':
				case 'q':
				case 'r':
				case 's':
				case 't':
				case 'u':
				case 'v':
				case 'w':
				case 'x':
				case 'y':
				case 'z':
				case '{':
				case '|':
				case '}':
				case '~':
					out.write(c);
					break;
				default:
					out.write('\\');
					out.write(Integer.toOctalString(c));
					out.write('\\');
					break;
			}
		}
	}

	public static void writeEscapedAtom(Writer out, String input) throws IOException {
		for (int i = 0, len = input.length(); i < len; i++) {
			final char c = input.charAt(i);
			switch (c) {
				case '\n':
					out.write('\\');
					out.write('n');
					break;
				case '\'':
				case '`':
				case '\\':
					out.write('\\');
					out.write(c);
					break;
				case ' ':
				case '!':
				case '"':
				case '#':
				case '$':
				case '%':
				case '&':
				case '(':
				case ')':
				case '*':
				case '+':
				case ',':
				case '-':
				case '.':
				case '/':
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case ':':
				case ';':
				case '<':
				case '=':
				case '>':
				case '?':
				case '@':
				case 'A':
				case 'B':
				case 'C':
				case 'D':
				case 'E':
				case 'F':
				case 'G':
				case 'H':
				case 'I':
				case 'J':
				case 'K':
				case 'L':
				case 'M':
				case 'N':
				case 'O':
				case 'P':
				case 'Q':
				case 'R':
				case 'S':
				case 'T':
				case 'U':
				case 'V':
				case 'W':
				case 'X':
				case 'Y':
				case 'Z':
				case '[':
				case ']':
				case '^':
				case '_':
				case 'a':
				case 'b':
				case 'c':
				case 'd':
				case 'e':
				case 'f':
				case 'g':
				case 'h':
				case 'i':
				case 'j':
				case 'k':
				case 'l':
				case 'm':
				case 'n':
				case 'o':
				case 'p':
				case 'q':
				case 'r':
				case 's':
				case 't':
				case 'u':
				case 'v':
				case 'w':
				case 'x':
				case 'y':
				case 'z':
				case '{':
				case '|':
				case '}':
				case '~':
					out.write(c);
					break;
				default:
					out.write('\\');
					out.write(Integer.toOctalString(c));
					out.write('\\');
					break;
			}
		}
	}
}
