package de.prob.prolog.output;

import java.io.StringWriter;

/**
 * This class encapsulates the process of creating a Prolog Term as a String.
 */
public final class PrologTermStringOutput extends PrologTermDelegate {

	private final StringWriter sw;

	public PrologTermStringOutput() {
		this(new StringWriter());
	}

	private PrologTermStringOutput(StringWriter sw) {
		super(new PrologTermOutput(sw, false));
		this.sw = sw;
	}

	@Override
	public String toString() {
		pto.flush();
		return sw.toString();
	}
}
