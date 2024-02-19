package de.prob.prolog.output;

import de.prob.prolog.term.PrologTerm;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Consumer;

public interface IPrologTermOutput {

	/**
	 * Start a new term. This method prints the (escaped, if needed) functor
	 * and the opening brackets. All other terms (atoms, numbers, variables,
	 * ...) are printed as arguments of this term until it is closed with
	 * {@link #closeTerm()}.
	 * <p>
	 * Same as {@link #openTerm(String, boolean)} with <code>false</code> as
	 * second argument.
	 *
	 * @param functor the functor, never <code>null</code>.
	 * @return the IPrologTermOutput
	 */
	default IPrologTermOutput openTerm(final String functor) {
		return this.openTerm(functor, false);
	}

	/**
	 * Start a new term. This method prints the (escaped, if needed) functor
	 * and the opening brackets. All other terms (atoms, numbers, variables,
	 * ...) are printed as arguments of this term until it is closed with
	 * {@link #closeTerm()}. You should close all opened terms.
	 * <p>
	 * Use this method instead of {@link #openTerm(String)} if you want to
	 * control whether the arguments of the term should be indented or not. This
	 * is useful to write terms more compact when you know that they are always
	 * short.
	 *
	 * @param functor           the functor, never <code>null</code>
	 * @param ignoreIndentation if this is set to true, the arguments of this term are not subject to indent.
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput openTerm(final String functor, final boolean ignoreIndentation);

	/**
	 * Finish a term that was started with {@link #openTerm(String)}. This
	 * method basically prints the closing parenthesis.
	 *
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput closeTerm();

	/**
	 * Write and close a term. This method prints the (escaped, if needed) functor
	 * and the opening brackets. All other terms (atoms, numbers, variables,
	 * ...) that are printed in the scope-Consumer are arguments of this term.
	 * The term will automatically be closed, do not close it yourself!
	 * <p>
	 * Same as {@link #term(String, boolean, Consumer)} with <code>false</code> as
	 * second argument.
	 *
	 * @param functor the functor, never <code>null</code>
	 * @param scope   receiver of the scoped IPrologTermOutput, use this to add arguments to the term
	 * @return the IPrologTermOutput
	 */
	default IPrologTermOutput term(final String functor, final Consumer<? super IPrologTermOutput> scope) {
		scope.accept(this.openTerm(functor));
		return this.closeTerm();
	}

	/**
	 * Write and close a term. This method prints the (escaped, if needed) functor
	 * and the opening brackets. All other terms (atoms, numbers, variables,
	 * ...) that are printed in the scope-Consumer are arguments of this term.
	 * The term will automatically be closed, do not close it yourself!
	 * <p>
	 * Use this method instead of {@link #term(String, Consumer)} if you want to
	 * control whether the arguments of the term should be indented or not. This
	 * is useful to write terms more compact when you know that they are always
	 * short.
	 *
	 * @param functor           the functor, never <code>null</code>
	 * @param ignoreIndentation if this is set to true, the arguments of this term are not subject to indent.
	 * @param scope             receiver of the scoped IPrologTermOutput, use this to add arguments to the term
	 * @return the IPrologTermOutput
	 */
	default IPrologTermOutput term(final String functor, final boolean ignoreIndentation, final Consumer<? super IPrologTermOutput> scope) {
		scope.accept(this.openTerm(functor, ignoreIndentation));
		return this.closeTerm();
	}

	/**
	 * Print an atom. The atom will be escaped, if needed.
	 *
	 * @param content the name of the atom, never <code>null</code>
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput printAtom(final String content);

	/**
	 * Print an atom or number. Use this for State IDs!
	 *
	 * @param content the name of the atom or the string representation of a number, never <code>null</code>
	 * @return the IPrologTermOutput
	 */
	default IPrologTermOutput printAtomOrNumber(final String content) {
		Objects.requireNonNull(content, "Atom or Number value is null");
		try {
			long n = Long.parseLong(content);
			return this.printNumber(n);
		} catch (NumberFormatException ignored) {
			return this.printAtom(content);
		}
	}

	/**
	 * Print a string. The content of the string will be escaped, if needed.
	 *
	 * @param content the string content, never <code>null</code>
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput printString(final String content);

	/**
	 * Print a number.
	 *
	 * @param number the number to print
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput printNumber(final long number);

	/**
	 * Print a number.
	 *
	 * @param number the number to print
	 * @return IPrologTermOutput, <code>this</code>
	 */
	IPrologTermOutput printNumber(final BigInteger number);

	/**
	 * Print a number.
	 *
	 * @param number the number to print
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput printNumber(final double number);

	/**
	 * Start a new list. All following terms (atoms, numbers, etc.) until the
	 * next call of {@link #closeList()} are put into the list. All opened lists
	 * should be closed. Basically this method prints the opening bracket.
	 *
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput openList();

	/**
	 * Finish a list that was started with {@link #openList()}. Basically this
	 * method prints the closing bracket.
	 *
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput closeList();

	/**
	 * Print an empty list.
	 *
	 * @return the IPrologTermOutput
	 */
	default IPrologTermOutput emptyList() {
		return this.openList().closeList();
	}

	/**
	 * Write and close a list. All following terms (atoms, numbers, etc.) are put into the list.
	 *
	 * @param scope receiver of the scoped IPrologTermOutput, use this to add elements to the list
	 * @return the IPrologTermOutput
	 */
	default IPrologTermOutput list(Consumer<? super IPrologTermOutput> scope) {
		scope.accept(this.openList());
		return this.closeList();
	}

	/**
	 * Print a Prolog variable. Variables should start with an upper case
	 * character (or underscore) and should not contain spaces (and other
	 * illegal characters). For variables, no escaping is done.
	 *
	 * @param var the name of the variable, never <code>null</code>
	 * @return the IPrologTermOutput
	 * @throws IllegalArgumentException if the variable is not a syntactically valid Prolog variable.
	 */
	IPrologTermOutput printVariable(final String var);

	/**
	 * Print a complete Term.
	 *
	 * @param term the term, never <code>null</code>
	 * @return IPrologTermOutput, <code>this</code>
	 */
	IPrologTermOutput printTerm(final PrologTerm term);

	/**
	 * Flush the underlying Writer.
	 *
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput flush();

	/**
	 * Print a Prolog full stop.
	 * This also starts a new line and flushes the underlying Writer.
	 *
	 * @return the IPrologTermOutput
	 */
	IPrologTermOutput fullstop();

	/**
	 * Print a term followed by a Prolog full stop.
	 *
	 * @param term term
	 * @return the IPrologTermOutput
	 */
	default IPrologTermOutput sentence(final PrologTerm term) {
		return this.printTerm(term).fullstop();
	}
}
