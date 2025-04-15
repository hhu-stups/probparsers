package de.prob.prolog.output;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

import de.prob.prolog.term.PrologTerm;

/**
 * @deprecated Use {@link FastSicstusTermOutput} or {@link FastReadWriter} directly.
 */
@Deprecated
public final class FastTermOutput extends BaseStructuredPrologOutput {

	private final FastReadWriter out;
	private PrologTerm finishedTerm;

	public FastTermOutput(OutputStream out) {
		this(new FastReadWriter(out));
	}

	public FastTermOutput(FastReadWriter.PrologSystem flavor, OutputStream out) {
		this(new FastReadWriter(flavor, out));
	}

	public FastTermOutput(FastReadWriter out) {
		this.out = Objects.requireNonNull(out, "out");
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

	@Override
	public void reset() {
		super.reset();
		this.finishedTerm = null;
	}
}
