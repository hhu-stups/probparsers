/*
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen,
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.parser;

public class ResultParserException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ResultParserException(String message) {
		super(message);
	}

	public ResultParserException(String message, Throwable cause) {
		super(message, cause);
	}
}
