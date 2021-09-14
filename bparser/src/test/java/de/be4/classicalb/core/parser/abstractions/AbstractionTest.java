package de.be4.classicalb.core.parser.abstractions;

import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.IDefinitionFileProvider;
import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.RecursiveMachineLoader;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.be4.classicalb.core.parser.node.Start;
import de.prob.prolog.output.StructuredPrologOutput;
import de.prob.prolog.term.PrologTerm;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class AbstractionTest {


	@Test
	public void testAbstraction() throws BCompoundException {
		final String machine = "ABSTRACTION M_A ABSTRACTS M ABSTRACTED_VARIABLES a, b, c ABSTRACTED_CONSTANTS g,c,d END";
		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parse(machine, false);

		AbstractionAnalyzerHelper abstractionAnalyzerHelper = new AbstractionAnalyzerHelper();
		abstractionAnalyzerHelper.caseStart(startNode);

		Assertions.assertTrue(abstractionAnalyzerHelper.abstraction);
		Assertions.assertEquals(3, abstractionAnalyzerHelper.variables);
		Assertions.assertEquals(3, abstractionAnalyzerHelper.constants);

	}

	@Test
	public void testAbstraction_to_prob_file() throws BCompoundException, IOException {
		File tempFile = File.createTempFile("tempfiles", ".mch");



		final String machine = "ABSTRACTION M_A ABSTRACTS M ABSTRACTED_VARIABLES a, b, c ABSTRACTED_CONSTANTS g,c,d END";

		FileWriter myWriter = new FileWriter(tempFile);
		myWriter.write(machine);
		myWriter.close();

		final BParser parser = new BParser("testcase");
		final Start startNode = parser.parseFile(tempFile, false);

		AbstractionAnalyzerHelper abstractionAnalyzerHelper = new AbstractionAnalyzerHelper();
		abstractionAnalyzerHelper.caseStart(startNode);


		File probFile = File.createTempFile(tempFile.getName(), ".prob");


		final ParsingBehaviour behaviour = new ParsingBehaviour();
		behaviour.setVerbose(true);

		String result = getASTasFastPrologMachineOnly(parser, probFile, startNode, behaviour, parser.getContentProvider());

		String expected = "machine(abstracted_machine(1,machine_header(2,'M_A',[]),'M',[abstracted_variables(3,identifier(4,a),identifier(5,b),identifier(6,c)),abstracted_constants(7,identifier(8,g),identifier(9,c),identifier(10,d))]))";

		Assertions.assertEquals(expected, result);
	}

	public static String getASTasFastPrologMachineOnly(final BParser parser, final File bfile, final Start tree,
													   final ParsingBehaviour parsingBehaviour, IDefinitionFileProvider contentProvider)
			throws BCompoundException {
		final RecursiveMachineLoader rml = new RecursiveMachineLoader(bfile.getParent(), contentProvider, parsingBehaviour);
		rml.loadAllMachines(bfile, tree, parser.getDefinitions());
		StructuredPrologOutput structuredPrologOutput = new StructuredPrologOutput();
		rml.printAsProlog(structuredPrologOutput);
		PrologTerm sentence = structuredPrologOutput.getSentences().get(structuredPrologOutput.getSentences().size() - 1);

		return sentence.toString();
	}
}
