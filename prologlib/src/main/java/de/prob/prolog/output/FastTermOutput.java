package de.prob.prolog.output;

import de.prob.prolog.term.PrologTerm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

public final class FastTermOutput extends BaseStructuredPrologOutput {

	private final FastReadWriter out;
	private PrologTerm finishedTerm;

	public FastTermOutput(OutputStream out) {
		this.out = new FastReadWriter(out);
	}

	@Override
	protected void addFinishedTerm(PrologTerm term) {
		if (this.finishedTerm != null) {
			throw new IllegalStateException("cannot add term, expected fullstop");
		} else {
			this.finishedTerm = term;
		}
	}

	@Override
	protected void fullStopImpl() {
		if (this.finishedTerm == null) {
			throw new IllegalStateException("cannot end sentence, expected term");
		}

		try {
			this.out.fastwrite(this.finishedTerm);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		this.finishedTerm = null;
	}

	@Override
	public IPrologTermOutput flush() {
		try {
			this.out.flush();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return this;
	}

	public void reset() {
		super.reset();
		this.finishedTerm = null;
	}
}
