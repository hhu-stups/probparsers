package de.prob.prolog.output;

import de.prob.prolog.term.PrologTerm;

import java.math.BigInteger;

public class PrologTermDelegate implements IPrologTermOutput {

	protected final IPrologTermOutput pto;

	public PrologTermDelegate(final IPrologTermOutput pto) {
		this.pto = pto;
	}

	@Override
	public IPrologTermOutput closeList() {
		pto.closeList();
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		pto.closeTerm();
		return this;
	}

	@Override
	public IPrologTermOutput emptyList() {
		pto.emptyList();
		return this;
	}

	@Override
	public IPrologTermOutput flush() {
		pto.flush();
		return this;
	}

	@Override
	public IPrologTermOutput fullstop() {
		pto.fullstop();
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		pto.openList();
		return this;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor) {
		pto.openTerm(functor);
		return this;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor, final boolean ignoreIndentation) {
		pto.openTerm(functor, ignoreIndentation);
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(final String content) {
		pto.printAtom(content);
		return this;
	}

	@Override
	public IPrologTermOutput printAtomOrNumber(final String content) {
		pto.printAtomOrNumber(content);
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final long number) {
		pto.printNumber(number);
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final BigInteger number) {
		pto.printNumber(number);
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final double number) {
		pto.printNumber(number);
		return this;
	}

	@Override
	public IPrologTermOutput printString(final String content) {
		pto.printString(content);
		return this;
	}

	@Override
	public IPrologTermOutput printVariable(final String var) {
		pto.printVariable(var);
		return this;
	}

	@Override
	public IPrologTermOutput printTerm(final PrologTerm term) {
		pto.printTerm(term);
		return this;
	}
}
