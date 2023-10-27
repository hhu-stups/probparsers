package de.prob.prolog.output;

import de.prob.prolog.term.*;

import java.math.BigInteger;
import java.util.*;

public class StructuredPrologOutput implements IPrologTermOutput {

	private final List<PrologTerm> sentences = new ArrayList<>();
	private final Deque<TermBuilder> termBuilderStack = new ArrayDeque<>();
	private PrologTerm finishedTerm;

	private void checkTerm() {
		if (this.finishedTerm != null) {
			throw new IllegalStateException("expected fullstop");
		}
	}

	private void addTerm(PrologTerm term) {
		Objects.requireNonNull(term, "term");
		this.checkTerm();
		if (this.termBuilderStack.isEmpty()) {
			this.finishedTerm = term;
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
	public IPrologTermOutput emptyList() {
		this.addTerm(ListPrologTerm.emptyList());
		return this;
	}

	@Override
	public IPrologTermOutput flush() {
		return this;
	}

	@Override
	public IPrologTermOutput fullstop() {
		if (this.finishedTerm == null) {
			throw new IllegalStateException("expected term");
		} else if (!this.termBuilderStack.isEmpty()) {
			throw new IllegalArgumentException(this.termBuilderStack.size() + " unclosed term(s) or list(s)");
		}

		this.sentences.add(this.finishedTerm);
		this.finishedTerm = null;
		return this;
	}

	@Override
	public IPrologTermOutput openList() {
		this.checkTerm();
		this.termBuilderStack.push(new TermBuilder(null));
		return this;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor) {
		this.checkTerm();
		this.termBuilderStack.push(new TermBuilder(Objects.requireNonNull(functor, "functor")));
		return this;
	}

	@Override
	public IPrologTermOutput openTerm(final String functor, final boolean ignoreIndentation) {
		return this.openTerm(functor);
	}

	@Override
	public IPrologTermOutput printAtom(final String content) {
		this.addTerm(new CompoundPrologTerm(content));
		return this;
	}

	@Override
	public IPrologTermOutput printAtomOrNumber(final String content) {
		try {
			this.printNumber(Long.parseLong(content));
		} catch (NumberFormatException ignored) {
			this.printAtom(content);
		}

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

	public List<PrologTerm> getSentences() {
		return new ArrayList<>(this.sentences);
	}

	public PrologTerm getFinishedTerm() {
		if (this.finishedTerm != null) {
			return this.finishedTerm;
		} else {
			throw new IllegalStateException("no unfinished term");
		}
	}

	public PrologTerm getLastSentence() {
		if (!this.sentences.isEmpty()) {
			return this.sentences.get(this.sentences.size() - 1);
		} else {
			throw new IllegalStateException("no sentence");
		}
	}

	public PrologTerm getLastTerm() {
		if (this.finishedTerm != null) {
			return this.finishedTerm;
		} else if (!this.sentences.isEmpty()) {
			return this.sentences.get(this.sentences.size() - 1);
		} else {
			throw new IllegalStateException("no term");
		}
	}

	public void reset() {
		this.clearSentences();
		this.termBuilderStack.clear();
		this.finishedTerm = null;
	}

	public void clearSentences() {
		this.sentences.clear();
	}

	public boolean hasFinishedTerm() {
		return this.finishedTerm != null;
	}

	public boolean isSentenceStarted() {
		return !this.termBuilderStack.isEmpty() || this.finishedTerm != null;
	}

	public boolean hasSentences() {
		return !this.sentences.isEmpty();
	}

	private static final class TermBuilder {

		final String functor;
		List<PrologTerm> args;

		TermBuilder(String functor) {
			this.functor = functor;
		}

		PrologTerm buildList() {
			if (this.functor != null) {
				throw new IllegalStateException("expected term");
			}

			return new ListPrologTerm(this.args);
		}

		PrologTerm buildTerm() {
			if (this.functor == null) {
				throw new IllegalStateException("expected list");
			}

			return new CompoundPrologTerm(this.functor, this.args);
		}

		void addArgument(PrologTerm term) {
			if (this.args == null) {
				this.args = new ArrayList<>();
			}

			this.args.add(term);
		}
	}
}
