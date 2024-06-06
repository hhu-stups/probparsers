package de.prob.prolog.output;

import de.prob.prolog.term.PrologTerm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StructuredPrologOutput extends BaseStructuredPrologOutput {

	private final List<PrologTerm> sentences = new ArrayList<>();
	private PrologTerm finishedTerm;

	@Override
	protected void addFinishedTerm(PrologTerm term) {
		if (this.hasFinishedTerm()) {
			throw new IllegalStateException("cannot add term, expected fullstop");
		} else {
			this.finishedTerm = term;
		}
	}

	@Override
	protected void fullStopImpl() {
		if (!this.hasFinishedTerm()) {
			throw new IllegalStateException("cannot end sentence, expected term");
		}

		this.sentences.add(this.finishedTerm);
		this.finishedTerm = null;
	}

	@Override
	public IPrologTermOutput flush() {
		return this;
	}

	public List<PrologTerm> getSentences() {
		return Collections.unmodifiableList(this.sentences);
	}

	public PrologTerm getFinishedTerm() {
		if (this.hasFinishedTerm()) {
			return this.finishedTerm;
		} else {
			throw new IllegalStateException("no finished term");
		}
	}

	public PrologTerm getLastSentence() {
		if (this.hasSentences()) {
			return this.sentences.get(this.sentences.size() - 1);
		} else {
			throw new IllegalStateException("no sentence");
		}
	}

	public PrologTerm getLastTerm() {
		if (this.hasFinishedTerm()) {
			return this.finishedTerm;
		} else if (this.hasSentences()) {
			return this.sentences.get(this.sentences.size() - 1);
		} else {
			throw new IllegalStateException("no finished term or sentence");
		}
	}

	public void reset() {
		super.reset();
		this.clearSentences();
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
}
