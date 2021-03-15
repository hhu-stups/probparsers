package de.be4.classicalb.core.parser.exceptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.be4.classicalb.core.parser.lexer.LexerException;
import de.be4.classicalb.core.parser.node.Node;
import de.hhu.stups.sablecc.patch.PositionedNode;

public class BException extends Exception {

	private static final long serialVersionUID = -693107947667081359L;
	private final Throwable cause;
	private final String filename;
	private final List<Location> locations = new ArrayList<>();

	public BException(final String filename, final String message, final Exception cause) {
		super(message);
		this.filename = filename;
		this.cause = cause;
	}

	public BException(String fileName, LexerException e) {
		this(fileName, e.getMessage(), e);
		final Location location = Location.parseFromSableCCMessage(e.getMessage(), fileName);
		if (location != null) {
			locations.add(location);
		}
	}


	public BException(String fileName, BLexerException e) {
		this(fileName, e.getMessage(), e);
		locations.add(new Location(filename, e.getLastLine(), e.getLastPos(), e.getLastLine(), e.getLastPos()));
	}

	public BException(String fileName, BParseException e) {
		this(fileName, e.getMessage(), e);
		if (e.getToken() != null) {
			locations.add(Location.fromNode(fileName, e.getToken()));
		}
	}

	public BException(String fileName, PreParseException e) {
		this(fileName, e.getMessage(), e);
		final Location location = Location.parseFromSableCCMessage(e.getMessage(), fileName);
		if (location != null) {
			locations.add(location);
		}
	}

	public BException(final String filename, final CheckException e) {
		this(filename, e.getMessage(), e);
		//super(e.getMessage());
		//this.filename = filename;
		//this.cause = e.getCause();
		for (Node node : e.getNodesList()) {
			locations.add(Location.fromNode(filename, node));
		}
	}

	public BException(final String filename, final IOException e) {
		this(filename, e.getMessage(), e);
	}

	@Override
	public Throwable getCause() {
		return this.cause;
	}

	public List<Location> getLocations() {
		return this.locations;
	}


	public String getFilename() {
		return filename;
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return cause.getStackTrace();
	}

	public static final class Location implements Serializable {

		private static final long serialVersionUID = -7391092302311266417L;

		private static final Pattern SABLECC_MESSAGE_LOCATION_PATTERN = Pattern.compile("\\[(\\d+),(\\d+)\\].*", Pattern.DOTALL);

		private final String filename;
		private final int startLine;
		private final int startColumn;
		private final int endLine;
		private final int endColumn;

		public Location(final String fileName, final int startLine, final int startColumn, final int endLine,
				final int endColumn) {

			this.filename = fileName;
			this.startLine = startLine;
			this.startColumn = startColumn;
			this.endLine = endLine;
			this.endColumn = endColumn;
		}

		private static Location fromNode(final String filename, final PositionedNode node) {
			return new Location(
				filename,
				node.getStartPos().getLine(),
				node.getStartPos().getPos(),
				node.getEndPos().getLine(),
				node.getEndPos().getPos()
			);
		}

		private static Location parseFromSableCCMessage(final String message, final String filename) {
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
	}
}
