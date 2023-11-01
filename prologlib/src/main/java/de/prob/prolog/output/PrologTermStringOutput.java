package de.prob.prolog.output;

import java.io.StringWriter;

/**
 * This class encapsulates the process of creating a Prolog Term as a String.
 */
public final class PrologTermStringOutput extends PrologTermDelegate {

	private final StringWriter sw;

	public PrologTermStringOutput() {
		this(false);
	}

	public PrologTermStringOutput(boolean useIndentation) {
		this(new StringWriter(), useIndentation);
	}

	private PrologTermStringOutput(StringWriter sw, boolean useIndentation) {
		super(new PrologTermOutput(sw, useIndentation));
		this.sw = sw;
	}

	@Override
	public String toString() {
		pto.flush();
		return sw.toString();
	}
}
