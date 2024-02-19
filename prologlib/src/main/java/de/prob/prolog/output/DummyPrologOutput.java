package de.prob.prolog.output;

import de.prob.prolog.term.PrologTerm;

import java.math.BigInteger;

/**
 * An implementation of {@link IPrologTermOutput} that does nothing.
 */
public final class DummyPrologOutput implements IPrologTermOutput {

	public static final DummyPrologOutput DUMMY = new DummyPrologOutput();

	private DummyPrologOutput() {
	}

	@Override
	public IPrologTermOutput closeList() {
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		return this;
	}

	@Override
	public IPrologTermOutput flush() {
		return this;
	}

	@Override
	public IPrologTermOutput fullstop() {
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		return this;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor, final boolean ignoreIndentation) {
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(final String content) {
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final long number) {
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final BigInteger number) {
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final double number) {
		return this;
	}

	@Override
	public IPrologTermOutput printString(final String content) {
		return this;
	}

	@Override
	public IPrologTermOutput printVariable(final String var) {
		return this;
	}

	@Override
	public IPrologTermOutput printTerm(final PrologTerm term) {
		return this;
	}
}
