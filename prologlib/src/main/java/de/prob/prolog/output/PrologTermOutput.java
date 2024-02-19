package de.prob.prolog.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import de.prob.prolog.term.PrologTerm;

/**
 * Helper class to generate Prolog terms.
 */
public final class PrologTermOutput implements IPrologTermOutput {

	private final Writer out;
	private final boolean useIndentation;

	private int indentLevel = 0;
	private int ignoreIndentationLevel = 0;
	private int termCount = 0;
	private int listCount = 0;
	/**
	 * commaNeeded states if the next term can be printed directly (false) or
	 * if a separating comma is needed first
	 */
	private boolean commaNeeded = false;
	/**
	 * flag to enable printing of terms without arguments as atoms.
	 * if set, the last printed object was a functor, and if anything is printed
	 * before closing the term, an opening parenthesis should be printed.
	 */
	private boolean lazyParenthesis = false;

	public PrologTermOutput(Writer out, boolean useIndentation) {
		this.out = out;
		this.useIndentation = useIndentation;
	}

	public PrologTermOutput(final PrintWriter out, final boolean useIndentation) {
		this((Writer) out, useIndentation);
	}

	public PrologTermOutput(final PrintWriter out) {
		this(out, true);
	}

	public PrologTermOutput(final OutputStream out, final boolean useIndentation) {
		this(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)), useIndentation);
	}

	public PrologTermOutput(final OutputStream out) {
		this(out, true);
	}

	private static boolean isInvalidPrologIdentifierChar(char c) {
		return c != '_' && ('a' > c || c > 'z') && ('A' > c || c > 'Z') && ('0' > c || c > '9');
	}

	public static boolean isValidPrologVariable(String name) {
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

	public static boolean isUnquotedPrologAtom(String name) {
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

	private void writeEscapedString(String input) throws IOException {
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

	private void writeEscapedAtom(String input) throws IOException {
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

	private void printIndentation() throws IOException {
		if (useIndentation && ignoreIndentationLevel == 0) {
			out.write(System.lineSeparator());
			for (int i = 0, lvl = indentLevel; i < lvl; i++) {
				out.write(' ');
			}
		}
	}

	private void printCommaIfNeeded() throws IOException {
		if (lazyParenthesis) {
			out.write('(');
			lazyParenthesis = false;
		}

		if (commaNeeded) {
			out.write(',');
			printIndentation();
		}
	}

	@Override
	public IPrologTermOutput openTerm(final String functor, final boolean ignoreIndentation) {
		Objects.requireNonNull(functor, "Functor is null");

		termCount++;
		printAtom(functor);
		lazyParenthesis = true;
		commaNeeded = false;

		indentLevel += 2;
		if (ignoreIndentationLevel > 0) {
			ignoreIndentationLevel++;
		} else if (ignoreIndentation) {
			ignoreIndentationLevel = 1;
		}
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		if (--termCount < 0) {
			throw new IllegalStateException("Tried to close a term that has not been opened.");
		}

		if (lazyParenthesis) {
			lazyParenthesis = false;
		} else {
			try {
				out.write(')');
			} catch (IOException exc) {
				throw new UncheckedIOException(exc);
			}
		}

		commaNeeded = true;
		indentLevel -= 2;
		if (ignoreIndentationLevel > 0) {
			ignoreIndentationLevel--;
		}
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(final String content) {
		Objects.requireNonNull(content, "Atom value is null");

		try {
			printCommaIfNeeded();
			if (isUnquotedPrologAtom(content)) {
				out.write(content);
			} else {
				out.write('\'');
				writeEscapedAtom(content);
				out.write('\'');
			}
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput printString(final String content) {
		Objects.requireNonNull(content, "String value is null");

		try {
			printCommaIfNeeded();
			out.write('"');
			writeEscapedString(content);
			out.write('"');
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final long number) {
		try {
			printCommaIfNeeded();
			out.write(Long.toString(number));
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final BigInteger number) {
		Objects.requireNonNull(number, "Number is null");

		try {
			printCommaIfNeeded();
			out.write(number.toString());
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final double number) {
		try {
			printCommaIfNeeded();
			out.write(Double.toString(number));
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		listCount++;

		try {
			printCommaIfNeeded();
			out.write('[');
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = false;
		indentLevel += 1;
		return this;
	}

	@Override
	public IPrologTermOutput closeList() {
		if (--listCount < 0) {
			throw new IllegalStateException("Tried to close a list that has not been opened.");
		}

		try {
			out.write(']');
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		indentLevel -= 1;
		return this;
	}

	@Override
	public IPrologTermOutput printVariable(final String var) {
		Objects.requireNonNull(var, "Variable name is null");
		if (!isValidPrologVariable(var)) {
			throw new IllegalArgumentException("Invalid name for Prolog variable '" + var + "'");
		}

		try {
			printCommaIfNeeded();
			out.write(var);
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput flush() {
		try {
			out.flush();
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}
		return this;
	}

	@Override
	public IPrologTermOutput fullstop() {
		if (listCount != 0) {
			throw new IllegalStateException("Number of openList and closeList do not match. openList Counter is " + listCount);
		} else if (termCount != 0) {
			throw new IllegalStateException("Number of openTerm and closeTerm do not match. openTerm Counter is " + termCount);
		}

		try {
			out.write('.');
			out.write(System.lineSeparator());
			out.flush();
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = false;
		return this;
	}

	@Override
	public IPrologTermOutput printTerm(final PrologTerm term) {
		term.toTermOutput(this);
		return this;
	}
}
