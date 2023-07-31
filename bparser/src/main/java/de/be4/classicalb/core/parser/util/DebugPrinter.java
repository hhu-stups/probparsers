package de.be4.classicalb.core.parser.util;

import java.io.PrintStream;

/**
 * @deprecated Use {@link System#out} or a proper logging framework instead.
 */
@Deprecated
public class DebugPrinter {

	
	private static PrintStream out = System.out;

	private DebugPrinter() {
	}

	public static void println(String message) {
		out.print("*** Debug: ");
		out.println(message);
		// out.flush(); // default flushes after newline
	}

	public static void print(String message) {
		out.print(message);
	}
}
