package de.prob.prolog.match;

import de.prob.prolog.term.FloatPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.util.Map;

/**
 * Matches an integer, provides the found integer as BigInt
 */
public final class PrologFloatMatch extends PrologMatch {

	private final Double number;
	private final Double delta;

	private PrologFloatMatch(final String name, final Double number, Double delta) {
		super(name);
		if (delta != null && (!Double.isFinite(delta) || delta < 0)) {
			throw new IllegalArgumentException("delta must be finite and non-negative");
		} else if (delta != null && (number == null || !Double.isFinite(number))) {
			throw new IllegalArgumentException("wanted delta is inconsistent with wanted number");
		}

		this.number = number;
		this.delta = delta;
	}

	public static PrologFloatMatch anonFloat() {
		return namedFloat(null);
	}

	public static PrologFloatMatch anonFloat(final Double number) {
		return namedFloat(null, number);
	}

	public static PrologFloatMatch anonFloat(final Double number, final Double delta) {
		return namedFloat(null, number, delta);
	}

	public static PrologFloatMatch namedFloat(final String name) {
		return namedFloat(name, null);
	}

	public static PrologFloatMatch namedFloat(final String name, final Double number) {
		return namedFloat(name, number, null);
	}

	public static PrologFloatMatch namedFloat(final String name, final Double number, final Double delta) {
		return new PrologFloatMatch(name, number, delta);
	}

	@Override
	protected boolean isMatch(final PrologTerm term, final Map<String, PrologTerm> hits) {
		boolean match = term instanceof FloatPrologTerm;
		if (match && number != null) {
			double value = ((FloatPrologTerm) term).getValue();
			if (Double.compare(value, number) != 0) {
				match = delta != null && Math.abs(value - number) <= delta;
			}
		}
		return match;
	}
}
