package de.be4.classicalb.core.parser.exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Node;
import de.be4.classicalb.core.parser.parser.ParserException;
import de.hhu.stups.sablecc.patch.PositionedNode;
import de.hhu.stups.sablecc.patch.SourcePosition;

/**
 * <p>
 * Wrapper around the different kinds of exceptions that may be thrown during parsing.
 * Adds context about the file in which the error occurred
 * and provides a generic interface for getting the error position(s).
 * </p>
 * <p>
 * Note that this exception is normally not thrown directly,
 * but instead wrapped again in a {@link BCompoundException}
 * to allow reporting multiple {@link BException}s at once.
 * </p>
 * <p>
 * The cause of this exception is normally one of the following:
 * </p>
 * <ul>
 * <li>
 * {@link PreParseException}: This exception contains errors that occur during the preparsing.
 * If possible it supplies a token where the error occurred.
 * </li>
 * <li>
 * {@link BLexerException}: Thrown if any error is detected by the customized lexer.
 * Includes the token responsible for the error.
 * </li>
 * <li>
 * {@link LexerException}: Thrown if any error occurs in the SableCC-generated lexer.
 * This class doesn't have any direct way to get the error position,
 * but {@link BException} tries to parse this information from the exception message
 * and provides it via the generic interface if possible.
 * </li>
 * <li>
 * {@link BParseException}: If the parser throws a {@link ParserException},
 * we convert it into a {@link BParseException}.
 * </li>
 * <li>
 * {@link CheckException}: Thrown if any problem occurs while performing AST transformations or semantic checks.
 * We provide one or more nodes that are involved in the problem.
 * For example, if we find duplicate machine clauses,
 * we will list all occurrences in the exception.
 * </li>
 * </ul>
 */
@SuppressWarnings("serial")
public class BException extends Exception {
	private final String filename;
	private final List<Location> locations = new ArrayList<>();

	public BException(final String filename, final List<Location> locations, final String message, final Throwable cause) {
		super(message, cause);
		this.filename = filename;
		this.locations.addAll(locations);
	}

	public BException(final String filename, final String message, final Throwable cause) {
		this(filename, Collections.emptyList(), message, cause);
	}

	public BException(String filename, LexerException e) {
		this(filename, e.getRealMsg(), e);
		if (e.getLine() != 0 && e.getPos() != 0) {
			locations.add(new Location(filename, e.getLine(), e.getPos(), e.getLine(), e.getPos()));
		}
	}


	public BException(String filename, BLexerException e) {
		this(filename, e.getMessage(), e);
		locations.add(new Location(filename, e.getLastLine(), e.getLastPos(), e.getLastLine(), e.getLastPos()));
	}

	public BException(String filename, BParseException e) {
		this(filename, e.getRealMsg(), e);
		if (e.getToken() != null) {
			final Location location = Location.fromNode(filename, e.getToken());
			if (location != null) {
				locations.add(location);
			}
		}
	}

	public BException(String filename, PreParseException e) {
		this(filename, e.getMessage(), e);
		if (e.getTokensList().isEmpty()) {
			if (e.getLine() != 0 && e.getPos() != 0) {
				locations.add(new Location(filename, e.getLine(), e.getPos(), e.getLine(), e.getPos()));
			}
		} else {
			e.getTokensList().forEach(token -> {
				final Location location = Location.fromNode(filename, token);
				if (location != null) {
					locations.add(location);
				}
			});
		}
	}

	public BException(final String filename, final CheckException e) {
		this(filename, e.getMessage(), e);
		//super(e.getMessage());
		//this.filename = filename;
		//this.cause = e.getCause();
		for (Node node : e.getNodesList()) {
			final Location location = Location.fromNode(filename, node);
			if (location != null) {
				locations.add(location);
			}
		}
	}

	public BException(final String filename, final IOException e) {
		this(filename, "File cannot be read: " + e.getMessage(), e);
		// somehow e.getMessage() just returns the file name without any information about
		// what went wrong; typical value would be java.nio.file.NoSuchFileException
	}

	public List<Location> getLocations() {
		return this.locations;
	}


	public String getFilename() {
		return filename;
	}

	/**
	 * @return a copy of this exception with all line numbers decremented by one
	 * @deprecated Use {@link BParser#setStartPosition(int, int)} to offset position info during parsing.
	 */
	@Deprecated
	public BException withLinesOneOff() {
		if (this.getLocations().isEmpty()) {
			return this;
		}

		final List<Location> offsetLocations = this.getLocations().stream()
			.map(Location::withLineOneOff)
			.collect(Collectors.toList());
		return new BException(this.getFilename(), offsetLocations, this.getMessage(), this);
	}

	public static final class Location {
		private final String filename;
		private final int startLine;
		private final int startColumn;
		private final int endLine;
		private final int endColumn;

		public Location(final String filename, final int startLine, final int startColumn, final int endLine,
				final int endColumn) {

			this.filename = filename;
			this.startLine = startLine;
			this.startColumn = startColumn;
			this.endLine = endLine;
			this.endColumn = endColumn;
		}

		public static Location fromNode(final String filename, final PositionedNode node) {
			// Extra checks to handle null locations safely.
			// This *should* not be needed normally,
			// because all nodes from SableCC have position info,
			// but it's better to be safe and avoid NPEs in error handling code.
			final SourcePosition startPos = node.getStartPos();
			if (startPos == null) {
				return null;
			}
			SourcePosition endPos = node.getEndPos();
			if (endPos == null) {
				endPos = startPos;
			}
			return new Location(
				filename,
				startPos.getLine(),
				startPos.getPos(),
				endPos.getLine(),
				endPos.getPos()
			);
		}

		public String getFilename() {
			return this.filename;
		}

		public int getStartLine() {
			return this.startLine;
		}

		public int getStartColumn() {
			return this.startColumn;
		}

		public int getEndLine() {
			return this.endLine;
		}

		public int getEndColumn() {
			return this.endColumn;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder(this.filename);
			sb.append(':');
			sb.append(this.getStartLine());
			sb.append(':');
			sb.append(this.getStartColumn());

			if (this.getStartLine() != this.getEndLine() || this.getStartColumn() != this.getEndColumn()) {
				sb.append(" to ");
				sb.append(this.getEndLine());
				sb.append(':');
				sb.append(this.getEndColumn());
			}

			return sb.toString();
		}

		/**
		 * @return a copy of this position with all line numbers decremented by one
		 * @deprecated Use {@link BParser#setStartPosition(int, int)} to offset position info during parsing.
		 */
		@Deprecated
		public Location withLineOneOff() {
			return new Location(this.getFilename(), this.getStartLine() - 1, this.getStartColumn(), this.getEndLine() - 1, this.getEndColumn());
		}
	}
}
