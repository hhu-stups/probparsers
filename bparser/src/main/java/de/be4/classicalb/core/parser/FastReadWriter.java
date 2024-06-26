package de.be4.classicalb.core.parser;

import java.io.IOException;
import java.io.OutputStream;

import de.prob.prolog.term.PrologTerm;

/**
 * @deprecated please use {@link de.prob.prolog.output.FastReadWriter}
 */
@Deprecated
public final class FastReadWriter {

	private final de.prob.prolog.output.FastReadWriter delegate;

	public FastReadWriter(OutputStream out) {
		this.delegate = new de.prob.prolog.output.FastReadWriter(out);
	}

	public void fastwrite(PrologTerm term) throws IOException {
		this.delegate.fastwrite(term);
	}
}
