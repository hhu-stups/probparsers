package util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.ASTProlog;
import de.be4.classicalb.core.parser.analysis.prolog.ClassicalPositionPrinter;
import de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;

import org.junit.Assert;
import org.junit.function.ThrowingRunnable;

public class Helpers {

	public static String getTreeAsString(final String testMachine) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parse(testMachine, false);

		final Ast2String ast2String = new Ast2String();
		startNode.apply(ast2String);
		return ast2String.toString();
	}

	public static String getPrettyPrint(final String testMachine) {
		final BParser parser = new BParser("testcase");
		Start startNode;
		try {
			startNode = parser.parse(testMachine, false);
		} catch (BCompoundException e) {
			throw new RuntimeException(e);
		}
		PrettyPrinter pp = new PrettyPrinter();
		startNode.apply(pp);
		return pp.getPrettyPrint();
	}

	public static String fullParsing(String filename) {
		final ParsingBehaviour parsingBehaviour = new ParsingBehaviour();
		parsingBehaviour.setPrologOutput(true);
		parsingBehaviour.setUseIndention(false);
		parsingBehaviour.setAddLineNumbers(false);
		parsingBehaviour.setVerbose(true);
		parsingBehaviour.setMachineNameMustMatchFileName(true);
		return fullParsing(filename, parsingBehaviour);
	}

	public static String fullParsing(String filename, ParsingBehaviour parsingBehaviour) {
		final File machineFile;
		try {
			machineFile = new File(Helpers.class.getClassLoader().getResource(filename).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		final BParser parser = new BParser(machineFile.getAbsolutePath());

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(output);
		parser.fullParsing(machineFile, parsingBehaviour, printStream, printStream);
		printStream.flush();
		printStream.close();
		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	public static String getMachineAsPrologTerm(String input) throws BCompoundException {
		final BParser parser = new BParser("Test");
		Start start = parser.parse(input, true);
		final PrologTermStringOutput pout = new PrologTermStringOutput();
		printAsProlog(start, pout);
		return pout.toString();
	}

	public static void printAsProlog(final Start start, final IPrologTermOutput pout) {
		final NodeIdAssignment nodeIds = new NodeIdAssignment();
		nodeIds.assignIdentifiers(1, start);
		final ClassicalPositionPrinter pprinter = new ClassicalPositionPrinter(nodeIds);
		final ASTProlog prolog = new ASTProlog(pout, pprinter);

		pout.openTerm("machine");
		start.apply(prolog);
		pout.closeTerm();
		pout.fullstop();
		pout.flush();
	}

	public static void parseFile(final String filename) throws IOException, BCompoundException, URISyntaxException {
		final int dot = filename.lastIndexOf('.');
		if (dot >= 0) {
			final File machineFile = new File(Helpers.class.getClassLoader().getResource(filename).toURI());
			File probFile = File.createTempFile(filename.substring(0, dot), ".prob");

			BParser parser = new BParser(filename);
			Start tree = parser.parseFile(machineFile, false);

			final ParsingBehaviour behaviour = new ParsingBehaviour();
			behaviour.setVerbose(true);

			PrintStream output = new PrintStream(probFile);
			BParser.printASTasProlog(output, parser, machineFile, tree, behaviour, parser.getContentProvider());
			output.close();
		} else
			throw new IllegalArgumentException("Filename '" + filename + "' has no extension");
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
		Assert.assertNotNull("BCompoundException is missing a cause", e);
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
