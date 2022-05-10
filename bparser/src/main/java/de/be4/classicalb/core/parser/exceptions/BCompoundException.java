package de.be4.classicalb.core.parser.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.be4.classicalb.core.parser.BParser;

@SuppressWarnings("serial")
public class BCompoundException extends Exception {
	private final List<BException> exceptions = new ArrayList<>();

	public BCompoundException(List<BException> list) {
		super(list.get(0).getLocalizedMessage(), list.get(0));
		this.exceptions.addAll(list);
	}

	public BCompoundException(BException bException) {
		super(bException.getLocalizedMessage(), bException);
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
	 * This method shouldn't be needed anymore - {@link BParser#setStartPosition(int, int)} can be used to offset all position info during parsing.
	 *
	 * @return a copy of this exception with all line numbers decremented by one
	 */
	public BCompoundException withLinesOneOff() {
		return new BCompoundException(this.getBExceptions().stream()
			.map(BException::withLinesOneOff)
			.collect(Collectors.toList()));
	}
}
