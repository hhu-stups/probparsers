package de.prob.prolog.output;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.FloatPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

abstract class BaseStructuredPrologOutput implements IPrologTermOutput {

	protected final Deque<TermBuilder> termBuilderStack = new ArrayDeque<>();

	protected abstract void addFinishedTerm(PrologTerm term);

	protected abstract void fullStopImpl();

	private void addTerm(PrologTerm term) {
		Objects.requireNonNull(term, "term");
		if (this.termBuilderStack.isEmpty()) {
			this.addFinishedTerm(term);
		} else {
			this.termBuilderStack.peek().addArgument(term);
		}
	}

	@Override
	public IPrologTermOutput closeList() {
		PrologTerm term = this.termBuilderStack.pop().buildList();
		this.addTerm(term);
		return this;
	}

	@Override
	public IPrologTermOutput closeTerm() {
		PrologTerm term = this.termBuilderStack.pop().buildTerm();
		this.addTerm(term);
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		this.termBuilderStack.push(new TermBuilder(null));
		return this;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor, final boolean ignoreIndentation) {
		this.termBuilderStack.push(new TermBuilder(Objects.requireNonNull(functor, "functor")));
		return this;
	}

	@Override
	public IPrologTermOutput printAtom(final String content) {
		this.addTerm(new CompoundPrologTerm(content));
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final long number) {
		this.addTerm(AIntegerPrologTerm.create(number));
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final BigInteger number) {
		this.addTerm(AIntegerPrologTerm.create(number));
		return this;
	}

	@Override
	public IPrologTermOutput printNumber(final double number) {
		this.addTerm(new FloatPrologTerm(number));
		return this;
	}

	@Override
	public IPrologTermOutput printString(final String content) {
		return this.printAtom(content);
	}

	@Override
	public IPrologTermOutput printVariable(final String var) {
		this.addTerm(new VariablePrologTerm(var));
		return this;
	}

	@Override
	public IPrologTermOutput printTerm(final PrologTerm term) {
		this.addTerm(term);
		return this;
	}

	@Override
	public IPrologTermOutput fullstop() {
		if (!this.termBuilderStack.isEmpty()) {
			throw new IllegalArgumentException(this.termBuilderStack.size() + " unclosed term(s) or list(s)");
		}

		this.fullStopImpl();
		return this.flush();
	}

	public void reset() {
		this.termBuilderStack.clear();
	}

	public boolean isBuildingTerm() {
		return !this.termBuilderStack.isEmpty();
	}

	protected static final class TermBuilder {

		final String functor;
		List<PrologTerm> args;

		TermBuilder(String functor) {
			this.functor = functor;
		}

		PrologTerm buildList() {
			if (this.functor != null) {
				throw new IllegalStateException("expected list");
			}

			return ListPrologTerm.fromCollection(this.args);
		}

		PrologTerm buildTerm() {
			if (this.functor == null) {
				throw new IllegalStateException("expected term");
			}

			return CompoundPrologTerm.fromCollection(this.functor, this.args);
		}

		void addArgument(PrologTerm term) {
			if (this.args == null) {
				this.args = new ArrayList<>();
			}

			this.args.add(term);
		}
	}
}
