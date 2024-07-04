package util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.INodeIds;
import de.be4.classicalb.core.parser.analysis.prolog.NodeFileNumbers;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Assert;
import org.junit.function.ThrowingRunnable;

public class Helpers {
	public static File[] getMachines(String path) {
		final File dir;
		try {
			dir = new File(Helpers.class.getResource("/" + path).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		return dir.listFiles((d, name) -> Stream.of(".mch", ".imp", ".ref", ".def").anyMatch(name::endsWith));
	}

	public static String getPrettyPrint(final String testMachine) {
		final BParser parser = new BParser("testcase");
		Start startNode;
		try {
			startNode = parser.parseMachine(testMachine);
		} catch (BCompoundException e) {
			throw new RuntimeException(e);
		}
		PrettyPrinter pp = new PrettyPrinter();
		startNode.apply(pp);
		return pp.getPrettyPrint();
	}

	public static String getPrettyPrintWithIndentation(final String testMachine) {
		final BParser parser = new BParser("testcase");
		Start startNode;
		try {
			startNode = parser.parseMachine(testMachine);
		} catch (BCompoundException e) {
			throw new RuntimeException(e);
		}
		PrettyPrinter pp = new PrettyPrinter();
		pp.setUseIndentation(true);
		startNode.apply(pp);
		return pp.getPrettyPrint();
	}

	/**
	 * Clean up line separators in a Prolog term string,
	 * so that it can be compared more easily using {@link Assert#assertEquals(Object, Object)} and similar methods.
	 * 
	 * @param term the Prolog term string to postprocess
	 * @return {@code term} with line separators cleaned
	 */
	private static String postprocessPrologTerm(final String term) {
		// Convert native line separators to \n
		String termConv = term.replace(System.lineSeparator(), "\n");
		// Remove trailing line separator (if any)
		if (termConv.endsWith("\n")) {
			termConv = termConv.substring(0, termConv.length() - 1);
		}
		return termConv;
	}

	public static String parseFile(String filename) throws IOException, BCompoundException {
		final ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setMachineNameMustMatchFileName(true);
		return parseFile(filename, parsingBehaviour);
	}

	public static String parseFile(String filename, ParsingBehaviour parsingBehaviour) throws IOException, BCompoundException {
		final File machineFile;
		try {
			machineFile = new File(Helpers.class.getResource("/" + filename).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		final RecursiveMachineLoader rml = RecursiveMachineLoader.loadFile(machineFile, parsingBehaviour);
		final PrologTermStringOutput pout = new PrologTermStringOutput();
		rml.printAsProlog(pout);
		return postprocessPrologTerm(pout.toString());
	}

	public static String getMachineAsPrologTerm(String input) throws BCompoundException {
		final BParser parser = new BParser("Test");
		Start start = parser.parseMachine(input);
		return getTreeAsPrologTerm(start);
	}

	public static String getTreeAsPrologTerm(final Start ast) {
		final PrologTermStringOutput pout = new PrologTermStringOutput();
		printAsProlog(ast, pout);
		return postprocessPrologTerm(pout.toString());
	}

	public static void printAsProlog(final Start start, final IPrologTermOutput pout) {
		final INodeIds nodeIds = new NodeFileNumbers();
		nodeIds.assignIdentifiers(1, start);
		final ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIds);
		final ASTProlog prolog = new ASTProlog(pout, pprinter);

		pout.openTerm("machine");
		start.apply(prolog);
		pout.closeTerm();
		pout.fullstop();
	}

	/**
	 * Get the first {@code machine} term from a sequence of Prolog terms.
	 * 
	 * @param terms a sequence of Prolog terms in string form
	 * @return the first {@code machine} term
	 * @throws AssertionError if {@code terms} didn't contain any machine terms
	 */
	public static String getFirstMachineTerm(final String terms) {
		for (final String line : terms.split("\n")) {
			if (line.startsWith("machine(")) {
				return line;
			}
		}
		throw new AssertionError("No machine term found in string: " + terms);
	}

	/**
	 * Assert that {@code runnable} throws a {@link BCompoundException} whose cause is of type {@code wrappedExceptionType}
	 * (the cause must not be {@code null}).
	 * This method behaves similarly to {@link Assert#assertThrows(Class, ThrowingRunnable)}.
	 * 
	 * @param wrappedExceptionType the expected type of the {@link BCompoundException}'s cause
	 * @param runnable the code that should throw a {@link BCompoundException}
	 * @param <T> the expected type of the {@link BCompoundException}'s cause
	 * @return the cause of the thrown {@link BCompoundException}
	 */
	public static <T extends Throwable> T assertThrowsCompound(final Class<T> wrappedExceptionType, final ThrowingRunnable runnable) {
		final BCompoundException e = Assert.assertThrows(BCompoundException.class, runnable);
		if (e.getCause() == null) {
			throw new AssertionError("BCompoundException is missing a cause", e);
		} else if (!wrappedExceptionType.isInstance(e.getCause())) {
			throw new AssertionError("Expected BCompoundException cause to be " + wrappedExceptionType + ", but got " + e.getCause().getClass(), e);
		}
		return wrappedExceptionType.cast(e.getCause());
	}

	public static void assertParseErrorLocation(final BException.Location loc, final int startLine, final int startColumn, final int endLine, final int endColumn) {
		Assert.assertEquals("Incorrect start line", startLine, loc.getStartLine());
		Assert.assertEquals("Incorrect start column", startColumn, loc.getStartColumn());
		Assert.assertEquals("Incorrect end line", endLine, loc.getEndLine());
		Assert.assertEquals("Incorrect end column", endColumn, loc.getEndColumn());
	}

	public static void assertParseErrorLocation(final BException e, final int startLine, final int startColumn, final int endLine, final int endColumn) {
		Assert.assertEquals(1, e.getLocations().size());
		assertParseErrorLocation(e.getLocations().get(0), startLine, startColumn, endLine, endColumn);
	}

	public static void assertParseErrorLocation(final BCompoundException e, final int startLine, final int startColumn, final int endLine, final int endColumn) {
		assertParseErrorLocation(e.getFirstException(), startLine, startColumn, endLine, endColumn);
	}
}
