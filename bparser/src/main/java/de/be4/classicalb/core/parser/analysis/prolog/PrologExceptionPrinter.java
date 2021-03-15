package de.be4.classicalb.core.parser.analysis.prolog;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.exceptions.BLexerException;
import de.be4.classicalb.core.parser.exceptions.BParseException;
import de.be4.classicalb.core.parser.exceptions.CheckException;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.PreParseException;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.node.Token;
import de.hhu.stups.sablecc.patch.SourcePosition;
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
		Throwable cause = e.getCause();
		String filename = e.getFilename();
		if (cause == null) {
			printGeneralParseException(pto, e, useIndentation, lineOneOff);
		} else {
			while (cause.getClass().equals(BException.class) && cause.getCause() != null) {
				BException bex = (BException) cause;
				cause = bex.getCause();
				filename = bex.getFilename();
			}
			if (cause instanceof PreParseException) {
				printPreParseException(pto, (PreParseException) cause, filename, useIndentation, lineOneOff);
			} else {
				printGeneralParseException(pto, e, useIndentation, lineOneOff);
			}
		}
	}

	private static void printLexerException(IPrologTermOutput pto, Exception cause, String filename,
			boolean useIndentation, boolean lineOneOff) {
		// there is no source information / position attached to lexer
		// exceptions -> extract from message
		Pattern p = Pattern.compile("\\[(\\d+)[,](\\d+)\\].*", Pattern.DOTALL);
		Matcher m = p.matcher(cause.getMessage());
		boolean posFound = m.lookingAt();
		final SourcePosition pos;
		if (posFound) {
			pos = new SourcePosition(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
		} else {
			pos = null;
		}
		printExceptionWithSourcePosition(pto, cause, filename, pos, useIndentation, lineOneOff);
	}

	private static void printGeneralParseException(final IPrologTermOutput pto, final BException e, final boolean useIndentation, final boolean lineOneOff) {
		if (e.getLocations().isEmpty()) {
			printExceptionWithSourcePosition(pto, e, e.getFilename(), null, useIndentation, lineOneOff);
		} else {
			// TODO Handle more than one location
			final BException.Location location = e.getLocations().get(0);
			final SourcePosition beginPos = new SourcePosition(location.getStartLine(), location.getStartColumn());
			final SourcePosition endPos = new SourcePosition(location.getEndLine(), location.getEndColumn());
			if (beginPos.compareTo(endPos) == 0) {
				// Begin and end position are equal, so return a single position instead of a range.
				printExceptionWithSourcePosition(pto, e, location.getFilename(), beginPos, useIndentation, lineOneOff);
			} else {
				printExceptionWithSourceRange(pto, e, location.getFilename(), beginPos, endPos, useIndentation, lineOneOff);
			}
		}
	}

	private static void printPreParseException(final IPrologTermOutput pto, final PreParseException e,
			final String filename, final boolean useIndentation, final boolean lineOneOff) {
		if (e.getCause() instanceof BLexerException) {
			printBLexerException(pto, (BLexerException) e.getCause(), filename, useIndentation, lineOneOff);
			return;
		}

		de.be4.classicalb.core.preparser.node.Token[] tokens = e.getTokens();
		if (tokens.length == 0 && e.getCause() instanceof de.be4.classicalb.core.preparser.lexer.LexerException) {
			printLexerException(pto, (Exception) e.getCause(), filename, useIndentation, lineOneOff);
			return;
		}
		pto.openTerm("preparse_exception");
		pto.openList();

		for (int i = 0; i < tokens.length; i++) {
			de.be4.classicalb.core.preparser.node.Token token = tokens[i];
			if (token == null) {
				pto.printAtom("none");
			} else {
				pto.openTerm("pos");
				if (lineOneOff) {
					pto.printNumber((long) token.getLine() - 1);
				} else {
					pto.printNumber(token.getLine());
				}
				pto.printNumber(token.getPos());
				pto.printAtom(filename);
				pto.closeTerm();
			}
		}
		pto.closeList();
		printMsg(pto, e, useIndentation);
		pto.closeTerm();
	}

	private static void printBLexerException(final IPrologTermOutput pto, final BLexerException e,
			final String filename, final boolean useIndentation, final boolean lineOneOff) {
		final Token token = e.getLastToken();
		final SourcePosition pos = token == null ? null : new SourcePosition(token.getLine(), token.getPos());
		printExceptionWithSourcePosition(pto, e, filename, pos, useIndentation, lineOneOff);
	}

	private static void printExceptionWithSourceRange(final IPrologTermOutput pto, final Throwable e,
			final String filename, final SourcePosition beginPos, final SourcePosition endPos,
			final boolean useIndentation, final boolean lineOneOff) {
		pto.openTerm(PARSE_EXCEPTION_PROLOG_TERM);
		if (beginPos == null) {
			pto.printAtom("none");
		} else {
			pto.openTerm("pos");
			if (lineOneOff) {
				pto.printNumber((long) beginPos.getLine() - 1);
			} else {
				pto.printNumber(beginPos.getLine());
			}
			pto.printNumber(beginPos.getPos());
			if (lineOneOff) {
				pto.printNumber((long) endPos.getLine() - 1);
			} else {
				pto.printNumber(endPos.getLine());
			}
			pto.printNumber(endPos.getPos());
			pto.printAtom(filename);
			pto.closeTerm();
		}
		printMsg(pto, e, useIndentation);
		pto.closeTerm();
	}

	private static void printExceptionWithSourcePosition(final IPrologTermOutput pto, final Throwable e,
			final String filename, final SourcePosition pos, final boolean useIndentation, final boolean lineOneOff) {
		pto.openTerm(PARSE_EXCEPTION_PROLOG_TERM);
		if (pos == null) {
			pto.printAtom("none");
		} else {
			pto.openTerm("pos");
			if (lineOneOff) {
				pto.printNumber((long) pos.getLine() - 1);
			} else {
				pto.printNumber(pos.getLine());
			}
			pto.printNumber(pos.getPos());
			pto.printAtom(filename);
			pto.closeTerm();
		}
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
