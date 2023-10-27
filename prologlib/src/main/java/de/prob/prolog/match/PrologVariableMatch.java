package de.prob.prolog.match;

import de.prob.prolog.term.PrologTerm;
import de.prob.prolog.term.VariablePrologTerm;

import java.util.Map;

/**
 * Matches a Prolog variable.
 */
public final class PrologVariableMatch extends PrologMatch {

	private final String varName;

	/**
	 * Matches on a Prolog variable with the given name
	 *
	 * @param name the name, if <code>null</code> it will not be checked
	 */
	private PrologVariableMatch(final String name, final String varName) {
		super(name);
		this.varName = varName;
	}


	public static PrologVariableMatch anonVar() {
		return namedVar(null);
	}

	public static PrologVariableMatch anonVar(String varName) {
		return namedVar(null, varName);
	}

	public static PrologVariableMatch namedVar(String name) {
		return namedVar(name, null);
	}

	public static PrologVariableMatch namedVar(String name, String varName) {
		return new PrologVariableMatch(name, varName);
	}

	@Override
	protected boolean isMatch(PrologTerm term, Map<String, PrologTerm> hits) {
		boolean match = term instanceof VariablePrologTerm;
		if (match && varName != null) {
			match = varName.equals(((VariablePrologTerm) term).getName());
		}
		return match;
	}
}
