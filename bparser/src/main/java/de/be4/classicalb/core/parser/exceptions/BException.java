package de.be4.classicalb.core.parser.exceptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Node;
import de.hhu.stups.sablecc.patch.PositionedNode;
import de.hhu.stups.sablecc.patch.SourcePosition;

public class BException extends Exception {

	private static final long serialVersionUID = -693107947667081359L;
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
		this(filename, e.getMessage(), e);
		final Location location = Location.parseFromSableCCMessage(filename, e.getMessage());
		if (location != null) {
			locations.add(location);
		}
	}


	public BException(String filename, BLexerException e) {
		this(filename, e.getMessage(), e);
		locations.add(new Location(filename, e.getLastLine(), e.getLastPos(), e.getLastLine(), e.getLastPos()));
	}

	public BException(String filename, BParseException e) {
		this(filename, e.getMessage(), e);
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
			// Fallback for LexerException wrapped in PreParseException.
			// In this case there are no tokens attached to the exception
			// (it's a lexer error, so there can be no token for the error location),
			// but there is position information in the message,
			// which can be extracted.
			final Location location = Location.parseFromSableCCMessage(filename, e.getMessage());
			if (location != null) {
				locations.add(location);
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
		this(filename, e.getMessage(), e);
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

	public static final class Location implements Serializable {

		private static final long serialVersionUID = -7391092302311266417L;

		private static final Pattern SABLECC_MESSAGE_LOCATION_PATTERN = Pattern.compile("\\[(\\d+),(\\d+)\\].*", Pattern.DOTALL);

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

		private static Location fromNode(final String filename, final PositionedNode node) {
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

		private static Location parseFromSableCCMessage(final String filename, final String message) {
			if (message == null) {
				return null;
			}

			final Matcher matcher = SABLECC_MESSAGE_LOCATION_PATTERN.matcher(message);
			if (matcher.lookingAt()) {
				final int line = Integer.parseInt(matcher.group(1));
				final int pos = Integer.parseInt(matcher.group(2));
				return new Location(filename, line, pos, line, pos);
			} else {
				return null;
			}
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
