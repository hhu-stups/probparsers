package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static final String PARSE_EXCEPTION_PROLOG_TERM = "parse_exception";

	private PrologExceptionPrinter() {
		// this class contains only static methods
	}

	public static void printException(final OutputStream out, final IOException e) {
		printException(out, e, true);
	}

	public static void printException(final OutputStream out, final IOException e, boolean useIndentation) {
		IPrologTermOutput pto = new PrologTermOutput(out, useIndentation);
		pto.openTerm("io_exception");
		printMsg(pto, e, useIndentation);
		pto.closeTerm();
		pto.fullstop();
		pto.flush();
	}

	public static void printException(final OutputStream out, final BCompoundException e) {
		printException(out, e, true, false);
	}

	public static void printException(final OutputStream out, final BCompoundException e, boolean useIndentation, boolean lineOneOff) {
		IPrologTermOutput pto = new PrologTermOutput(out, useIndentation);
		printException(pto, e, useIndentation, lineOneOff);
		pto.fullstop();
		pto.flush();
	}

	public static void printException(final IPrologTermOutput pto, final BCompoundException e, boolean useIndentation, boolean lineOneOff) {
		if (e.getBExceptions().size() > 1) {
			pto.openTerm("compound_exception", true);
			pto.openList();
			for (BException ex : e.getBExceptions()) {
				printBException(pto, ex, useIndentation, lineOneOff);
			}
			pto.closeList();
			pto.closeTerm();
		} else if (e.getBExceptions().size() == 1) {
			// single BException
			printBException(pto, e.getBExceptions().get(0), useIndentation, lineOneOff);
		} else {
			throw new IllegalStateException("Empty compoundException.");
		}
	}

	public static void printBException(IPrologTermOutput pto, final BException e, boolean useIndentation,
			boolean lineOneOff) {
		printGeneralParseException(pto, e, useIndentation, lineOneOff);
	}

	private static void printGeneralParseException(final IPrologTermOutput pto, final BException e, final boolean useIndentation, final boolean lineOneOff) {
		pto.openTerm(PARSE_EXCEPTION_PROLOG_TERM);
		pto.openList();
		for (final BException.Location location : e.getLocations()) {
			pto.openTerm("pos");
			if (lineOneOff) {
				pto.printNumber(location.getStartLine() - 1);
			} else {
				pto.printNumber(location.getStartLine());
			}
			pto.printNumber(location.getStartColumn());
			if (lineOneOff) {
				pto.printNumber(location.getEndLine() - 1);
			} else {
				pto.printNumber(location.getEndLine());
			}
			pto.printNumber(location.getEndColumn());
			pto.printAtom(location.getFilename());
			pto.closeTerm();
		}
		pto.closeList();
		printMsg(pto, e, useIndentation);
		pto.closeTerm();
	}

	private static void printMsg(final IPrologTermOutput pto, final Throwable cause, final boolean useIndentation) {
		String message = cause.getMessage();
		if (useIndentation) {
			pto.printAtom(message);
		} else {
			pto.printAtom(message.replace("\n", " "));

		}
	}

	@SuppressWarnings("unused")
	private static String fixMessageLineOneOff(String message) {
		Pattern p = Pattern.compile("\\[(\\d+)[,](\\d+)\\](.*)", Pattern.DOTALL);
		Matcher m = p.matcher(message);

		if (m.lookingAt()) {
			int actualLineNr = Integer.parseInt(m.group(1)) - 1;
			return message.replaceFirst(m.group(1), Integer.toString(actualLineNr));
		} else {
			// did not match - can not fix line number
			return message;
		}
	}

}
