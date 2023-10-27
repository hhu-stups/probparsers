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
import java.util.Arrays;
import java.util.Objects;

import de.prob.prolog.internal.Utils;
import de.prob.prolog.term.PrologTerm;

/**
 * Helper class to generate Prolog terms.
 */
public class PrologTermOutput implements IPrologTermOutput {
	private static final char[] VALID_CHARS = validChars();
	private static final char[] VALID_ATOM_CHARS = validAtomChars();

	private final Writer out;

	// commaNeeded states if the next term can be printed directly (false) or
	// if a separating comma is needed first
	private boolean commaNeeded = false;

	private final boolean useIndentation;
	private int indentLevel = 0;
	private int ignoreIndentationLevel = 0;

	private int termCount = 0;
	private int listCount = 0;

	// flag to enable printing of terms without arguments as atoms.
	// if set, the last printed object was a functor, and if anything is printed
	// before closing the term, an opening parenthesis should be printed.
	private boolean lazyParenthesis = false;

	public PrologTermOutput(Writer out, boolean useIndentation) {
		this.out = out;
		this.useIndentation = useIndentation;
	}

	public PrologTermOutput(final PrintWriter out, final boolean useIndentation) {
		this((Writer)out, useIndentation);
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

	/**
	 * Escapes existing apostrophes by backslashes.
	 * 
	 * @param input
	 *            A string, never <code>null</code>.
	 * @param singleQuotes
	 *            if single quotes may be used
	 * @param doubleQuotes
	 *            if double quotes may be used
	 */
	private void escape(final String input, final boolean singleQuotes, final boolean doubleQuotes) throws IOException {
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (Arrays.binarySearch(VALID_CHARS, c) >= 0) {
				out.write(c);
			} else if (c == '\'') {
				out.write(singleQuotes ? "'" : "\\'");
			} else if (c == '\\') {
				out.write("\\\\");
			} else if (c == '\n') {
				out.write("\\n");
			} else if (c == '"') {
				out.write(doubleQuotes ? "\"" : "\\\"");
			} else {
				out.write('\\');
				out.write(Integer.toOctalString(c));
				out.write('\\');
			}
		}
	}

	private static char[] validChars() {
		StringBuilder buf = new StringBuilder();
		buf.append("abcdefghijklmnopqrstuvwxyz");
		buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		buf.append("0123456789");
		buf.append("_ +-*/^<>=~:.?@#$&!;%(),[]{|}");
		char[] chars = buf.toString().toCharArray();
		Arrays.sort(chars);
		return chars;
	}

	private static char[] validAtomChars() {
		StringBuilder buf = new StringBuilder();
		buf.append("abcdefghijklmnopqrstuvwxyz");
		buf.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		buf.append("0123456789_");
		char[] chars = buf.toString().toCharArray();
		Arrays.sort(chars);
		return chars;
	}

	private static boolean escapeIsNeeded(final String input) {
		final int length = input.length();
		if (length > 0
				&& Arrays.binarySearch(VALID_ATOM_CHARS, input.charAt(0)) >= 0
				&& Character.isLowerCase(input.charAt(0))) {
			for (int i = 1; i < length; i++) {
				char c = input.charAt(i);
				if (Arrays.binarySearch(VALID_ATOM_CHARS, c) < 0)
					return true;
			}
			return false;
		} else
			return true;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor) {
		openTerm(functor, false);
		return this;
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

	private void printIndentation() throws IOException {
		if (useIndentation && ignoreIndentationLevel == 0) {
			out.write(System.lineSeparator());
			for (int i = 0; i < indentLevel; i++) {
				out.write(' ');
			}
		}
	}

	@Override
	public IPrologTermOutput closeTerm() {
		termCount--;
		if (termCount < 0)
			throw new IllegalStateException(
					"Tried to close a term that has not been opened.");
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
			if (escapeIsNeeded(content)) {
				out.write('\'');
				escape(content, false, true);
				out.write('\'');
			} else {
				out.write(content);
			}
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}
		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput printAtomOrNumber(final String content) {
		try {
			printNumber(Long.parseLong(content));
		} catch (NumberFormatException e) {
			printAtom(content);
		}
		return this;
	}

	@Override
	public IPrologTermOutput printString(final String content) {
		Objects.requireNonNull(content, "String value is null");
		try {
			printCommaIfNeeded();
			out.write('"');
			escape(content, true, false);
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
		listCount--;
		if (listCount < 0)
			throw new IllegalStateException(
				"Tried to close a list that has not been opened.");
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
	public IPrologTermOutput emptyList() {
		try {
			printCommaIfNeeded();
			out.write("[]");
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}
		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput printVariable(final String var) {
		Objects.requireNonNull(var, "Variable name is null");
		if (!Utils.isPrologVariable(var)) {
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
	public IPrologTermOutput fullstop() {
		if (listCount != 0)
			throw new IllegalStateException(
					"Number of openList and closeList do not match. openList Counter is "
							+ listCount);
		if (termCount != 0)
			throw new IllegalStateException(
					"Number of openTerm and closeTerm do not match. openTerm Counter is "
							+ termCount);
		try {
			out.write('.');
			out.write(System.lineSeparator());
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
