package de.be4.classicalb.core.parser.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;

/**
 * Wraps one or more {@link BException}s that occurred during the parsing process.
 * This allows the parser to report all found errors at once and not just the first one.
 * To get more information about the individual errors and their positions,
 * use {@link #getBExceptions()} and inspect the {@link BException} objects.
 */
@SuppressWarnings("serial")
public class BCompoundException extends Exception {
	private final List<BException> exceptions = new ArrayList<>();

	public BCompoundException(List<BException> list) {
		super(list.get(0).getMessage(), list.get(0));
		this.exceptions.addAll(list);
	}

	public BCompoundException(BException bException) {
		super(bException.getMessage(), bException);
		this.exceptions.add(bException);
	}

	public List<BException> getBExceptions() {
		return this.exceptions;
	}

	public BException getFirstException() {
		return this.exceptions.get(0);
	}

	@Override
	public synchronized Throwable getCause() {
		return this.exceptions.get(0).getCause();
	}

	/**
	 * @return a copy of this exception with all line numbers decremented by one
	 * @deprecated Use {@link BParser#setStartPosition(int, int)} to offset position info during parsing.
	 */
	@Deprecated
	public BCompoundException withLinesOneOff() {
		return new BCompoundException(this.getBExceptions().stream()
			.map(BException::withLinesOneOff)
			.collect(Collectors.toList()));
	}

	/**
	 * Adds the given locations to all wrapped {@link BException}s that don't have any locations yet.
	 * This is useful for cases where no location info is available where the exception is thrown,
	 * but a caller further up the stack can provide useful location info,
	 * e. g. when parsing a file referenced from another file.
	 * 
	 * @param locations location info to add
	 * @return a copy of this exception with locations added to all wrapped {@link BException}s that didn't have any
	 */
	public BCompoundException withMissingLocations(List<BException.Location> locations) {
		return new BCompoundException(this.getBExceptions().stream()
			.map(exc -> {
				if (exc.getLocations().isEmpty()) {
					return new BException(exc.getFilename(), locations, exc.getMessage(), exc.getCause());
				} else {
					return exc;
				}
			})
			.collect(Collectors.toList()));
	}
}
