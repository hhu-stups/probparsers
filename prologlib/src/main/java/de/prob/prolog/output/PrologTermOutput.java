package de.prob.prolog.output;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

import de.prob.prolog.term.PrologTerm;

/**
 * Helper class to generate Prolog terms.
 */
public class PrologTermOutput implements IPrologTermOutput {
	private static final char[] VALID_CHARS = validChars();
	private static final char[] VALID_ATOM_CHARS = validAtomChars();

	private final PrintWriter out;

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

	public PrologTermOutput(final PrintWriter out, final boolean useIndentation) {
		this.useIndentation = useIndentation;
		this.out = out;
	}

	public PrologTermOutput(final PrintWriter out) {
		this(out, true);
	}

	public PrologTermOutput(final OutputStream out, final boolean useIndentation) {
		this(new PrintWriter(out), useIndentation);
	}

	public PrologTermOutput(final OutputStream out) {
		this(new PrintWriter(out));
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
	private void escape(final String input, final boolean singleQuotes, final boolean doubleQuotes) {
		for (int i = 0; i < input.length(); i++) {
			final char c = input.charAt(i);
			if (Arrays.binarySearch(VALID_CHARS, c) >= 0) {
				out.print(c);
			} else if (c == '\'') {
				out.print(singleQuotes ? "'" : "\\'");
			} else if (c == '\\') {
				out.print("\\\\");
			} else if (c == '\n') {
				out.print("\\n");
			} else if (c == '"') {
				out.print(doubleQuotes ? "\"" : "\\\"");
			} else {
				out.print('\\');
				out.print(Integer.toOctalString(c));
				out.print('\\');
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

	private void printIndentation() {
		if (useIndentation && ignoreIndentationLevel == 0) {
			// synchronized to speed up printing
			synchronized (out) {
				out.println();
				for (int i = 0; i < indentLevel; i++) {
					out.print(' ');
				}
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
			out.print(')');
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
		synchronized (out) {
			printCommaIfNeeded();
			if (escapeIsNeeded(content)) {
				out.print('\'');
				escape(content, false, true);
				out.print('\'');
			} else {
				out.print(content);
			}
			commaNeeded = true;
		}
		return this;
	}

	@Override
	public IPrologTermOutput printAtomOrNumber(final String content) {
		synchronized (out) {
			try {
				printNumber(Long.parseLong(content));
			} catch (NumberFormatException e) {
				printAtom(content);
			}
		}
		return this;
	}

	@Override
	public IPrologTermOutput printString(final String content) {
		Objects.requireNonNull(content, "String value is null");
		synchronized (out) {
			printCommaIfNeeded();
			out.print('"');
			escape(content, true, false);
			out.print('"');
			commaNeeded = true;
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final long number) {
		synchronized (out) {
			printCommaIfNeeded();
			out.print(number);
			commaNeeded = true;
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final BigInteger number) {
		Objects.requireNonNull(number, "Number is null");
		synchronized (out) {
			printCommaIfNeeded();
			out.print(number);
			commaNeeded = true;
		}
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final double number) {
		synchronized (out) {
			printCommaIfNeeded();
			out.print(number);
			commaNeeded = true;
		}
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		synchronized (out) {
			listCount++;
			printCommaIfNeeded();
			out.print('[');
			commaNeeded = false;
			indentLevel += 1;
		}
		return this;
	}

	@Override
	public IPrologTermOutput closeList() {
		synchronized (out) {
			listCount--;
			if (listCount < 0)
				throw new IllegalStateException(
						"Tried to close a list that has not been opened.");
			out.print(']');
			commaNeeded = true;
			indentLevel -= 1;
		}
		return this;
	}

	@Override
	public IPrologTermOutput emptyList() {
		synchronized (out) {
			printCommaIfNeeded();
			out.print("[]");
			commaNeeded = true;
		}
		return this;
	}

	@Override
	public IPrologTermOutput printVariable(final String var) {
		Objects.requireNonNull(var, "Variable name is null");
		checkVariable(var);
		printCommaIfNeeded();
		out.print(var);
		commaNeeded = true;
		return this;
	}

	private void checkVariable(final String var) {
		boolean ok = var.length() > 0;
		if (ok) {
			char c = var.charAt(0);
			ok = c == '_' || Character.isUpperCase(c);
			for (int i = 1; ok && i < var.length(); i++) {
				c = var.charAt(i);
				ok &= c == '_' || Character.isLetterOrDigit(c);
			}
		}
		if (!ok)
			throw new IllegalArgumentException(
					"Invalid name for Prolog variable '" + var + "'");
	}

	@Override
	public IPrologTermOutput flush() {
		out.flush();
		return this;
	}

	private void printCommaIfNeeded() {
		if (lazyParenthesis) {
			out.print('(');
			lazyParenthesis = false;
		}
		if (commaNeeded) {
			out.print(',');
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
		out.println('.');
		commaNeeded = false;
		return this;
	}

	@Override
	public IPrologTermOutput printTerm(final PrologTerm term) {
		term.toTermOutput(this);
		return this;
	}
}
