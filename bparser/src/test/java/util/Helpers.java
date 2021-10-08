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
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.be4.classicalb.core.parser.util.PrettyPrinter;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.output.PrologTermStringOutput;

public class Helpers {

	public static String getTreeAsString(final String testMachine) throws BCompoundException {
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parse(testMachine, false);

		// startNode.apply(new ASTPrinter());
		final Ast2String ast2String = new Ast2String();
		startNode.apply(ast2String);
		final String string = ast2String.toString();
		// System.out.println(string);
		return string;
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

	public static String parseMachineAndGetPrologOutput(String input) {
		final BParser parser = new BParser("Test");

		final PrologTermStringOutput pout = new PrologTermStringOutput();
		try {
			Start start = parser.parse(input, false);
			printAsProlog(start, pout);
		} catch (BCompoundException e) {
			PrologExceptionPrinter.printException(pout, e, false, false);
		}
		return pout.toString();
	}

	public static String getMachineAsPrologTerm(String input) {
		final BParser parser = new BParser("Test");
		Start start;
		try {
			start = parser.parse(input, true);
		} catch (BCompoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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

}
