package de.prob.prolog.match;

import de.prob.prolog.term.AIntegerPrologTerm;
import de.prob.prolog.term.PrologTerm;

import java.math.BigInteger;
import java.util.Map;

/**
 * Matches an integer, provides the found integer as BigInt
 */
public final class PrologIntegerMatch extends PrologMatch {

	private final BigInteger number;

	private PrologIntegerMatch(final String name, final BigInteger number) {
		super(name);
		this.number = number;
	}

	public static PrologIntegerMatch anonInt() {
		return namedInt(null, null);
	}

	public static PrologIntegerMatch anonInt(final long number) {
		return namedInt(null, BigInteger.valueOf(number));
	}

	public static PrologIntegerMatch anonInt(final BigInteger number) {
		return namedInt(null, number);
	}

	public static PrologIntegerMatch namedInt(final String name) {
		return namedInt(name, null);
	}

	public static PrologIntegerMatch namedInt(final String name, final long number) {
		return namedInt(name, BigInteger.valueOf(number));
	}

	public static PrologIntegerMatch namedInt(final String name, final BigInteger number) {
		return new PrologIntegerMatch(name, number);
	}

	@Override
	protected boolean isMatch(final PrologTerm term, final Map<String, PrologTerm> hits) {
		boolean match = term instanceof AIntegerPrologTerm;
		if (match && number != null) {
			match = number.equals(((AIntegerPrologTerm) term).getValue());
		}
		return match;
	}
}
