package de.be4.classicalb.core.parser.rules;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.ParsingBehaviour;

public class RulesUtil {
	private static String checkAndFormatProject(final RulesProject project) {
		project.checkAndTranslateProject();
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		project.printPrologOutput(new PrintStream(output), new PrintStream(output));
		return output.toString();
	}

	public static String getRulesProjectAsPrologTerm(final String content) {
		RulesProject rulesProject = new RulesProject();
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(false);
		rulesProject.setParsingBehaviour(pb);
		rulesProject.parseRulesMachines(content, new String[] {});
		return checkAndFormatProject(rulesProject);
	}

	public static String getFileAsPrologTerm(final String file) {
		return getFileAsPrologTerm(file, false);
	}

	public static String getFileAsPrologTerm(final String filename, boolean addLineNumbers) {
		File file;
		try {
			file = new File(RulesUtil.class.getClassLoader().getResource(filename).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(addLineNumbers);
		final RulesProject project = new RulesProject();
		project.setParsingBehaviour(pb);
		project.parseProject(file);
		return checkAndFormatProject(project);
	}

	public static String getRulesMachineAsPrologTerm(final String content) {
		RulesParseUnit unit = new RulesParseUnit();
		unit.setMachineAsString(content);
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(false);
		unit.setParsingBehaviour(pb);
		unit.parse();
		unit.translate();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		unit.printPrologOutput(new PrintStream(output), new PrintStream(output));
		return output.toString();
	}

}
