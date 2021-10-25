package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.IOException;
import java.io.OutputStream;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermOutput;

/**
 * Just a helper class to print exceptions in Prolog-Syntax.
 * 
 * Ugly, but the alternative would have been Prolog-References in the exception
 * definitions.
 * 
 * @author plagge
 * 
 */
public final class PrologExceptionPrinter {
	private PrologExceptionPrinter() {
		// this class contains only static methods
	}

	public static void printException(final OutputStream out, final IOException e) {
		IPrologTermOutput pto = new PrologTermOutput(out, false);
		printException(pto, e);
		pto.fullstop();
		pto.flush();
	}

	public static void printException(final IPrologTermOutput pto, final IOException e) {
		pto.openTerm("io_exception");
		pto.printAtom(e.getMessage());
		pto.closeTerm();
	}

	public static void printException(final OutputStream out, final BCompoundException e) {
		IPrologTermOutput pto = new PrologTermOutput(out, false);
		printException(pto, e);
		pto.fullstop();
		pto.flush();
	}

	/**
	 * @deprecated The {@code useIndentation} parameter almost never has an effect
	 *     (use {@link PrologTermOutput} if you really need indentation).
	 *     The {@code lineOneOff} parameter should no longer be needed in most cases
	 *     ({@link BParser} methods that internally add a kind prefix now return correct error locations)
	 *     and if necessary can be replaced with {@link BCompoundException#withLinesOneOff()}.
	 */
	@Deprecated
	public static void printException(final OutputStream out, final BCompoundException e, boolean useIndentation, boolean lineOneOff) {
		IPrologTermOutput pto = new PrologTermOutput(out, useIndentation);
		printException(pto, e, useIndentation, lineOneOff);
		pto.fullstop();
		pto.flush();
	}

	public static void printException(final IPrologTermOutput pto, final BCompoundException e) {
		if (e.getBExceptions().size() > 1) {
			pto.openTerm("compound_exception", true);
			pto.openList();
			for (BException ex : e.getBExceptions()) {
				printBException(pto, ex);
			}
			pto.closeList();
			pto.closeTerm();
		} else if (e.getBExceptions().size() == 1) {
			// single BException
			printBException(pto, e.getBExceptions().get(0));
		} else {
			throw new IllegalStateException("Empty compoundException.");
		}
	}

	/**
	 * @deprecated The {@code useIndentation} parameter has no effect anymore.
	 *     The {@code lineOneOff} parameter should no longer be needed in most cases
	 *     ({@link BParser} methods that internally add a kind prefix now return correct error locations)
	 *     and if necessary can be replaced with {@link BCompoundException#withLinesOneOff()}.
	 */
	@Deprecated
	public static void printException(final IPrologTermOutput pto, final BCompoundException e, boolean useIndentation, boolean lineOneOff) {
		printException(pto, lineOneOff ? e.withLinesOneOff() : e);
	}

	public static void printBException(IPrologTermOutput pto, final BException e) {
		pto.openTerm("parse_exception");
		pto.openList();
		for (final BException.Location location : e.getLocations()) {
			pto.openTerm("pos");
			pto.printNumber(location.getStartLine());
			pto.printNumber(location.getStartColumn());
			pto.printNumber(location.getEndLine());
			pto.printNumber(location.getEndColumn());
			pto.printAtom(location.getFilename());
			pto.closeTerm();
		}
		pto.closeList();
		pto.printAtom(e.getMessage());
		pto.closeTerm();
	}
}
