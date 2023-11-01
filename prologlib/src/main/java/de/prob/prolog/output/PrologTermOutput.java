package de.prob.prolog.output;

import de.prob.prolog.internal.Utils;
import de.prob.prolog.term.PrologTerm;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
			if (Utils.isPrologAtom(content)) {
				out.write(content);
			} else {
				out.write('\'');
				Utils.writeEscapedAtom(out, content);
				out.write('\'');
			}
		} catch (IOException exc) {
			throw new UncheckedIOException(exc);
		}

		commaNeeded = true;
		return this;
	}

	@Override
	public IPrologTermOutput printAtomOrNumber(final String content) {
		Objects.requireNonNull(content, "Atom or Number value is null");

		try {
			long n = Long.parseLong(content);
			printNumber(n);
		} catch (NumberFormatException ignored) {
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
			Utils.writeEscapedString(out, content);
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
	public IPrologTermOutput emptyList() {
		try {
			printCommaIfNeeded();
			out.write('[');
			out.write(']');
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
