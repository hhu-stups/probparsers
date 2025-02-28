package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.NodeFileNumbers;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.prolog.output.PrologTermStringOutput;

public class RulesUtil {
	public static String getParsedProjectAsPrologTerm(final RulesProject project) throws BCompoundException {
		project.checkAndTranslateProject();
		if (project.hasErrors()) {
			throw new BCompoundException(project.getBExceptionList());
		}

		final PrologTermStringOutput pout = new PrologTermStringOutput();
		project.printProjectAsPrologTerm(pout);
		return pout.toString();
	}

	public static String getRulesProjectAsPrologTerm(final String content) throws BCompoundException {
		RulesProject rulesProject = new RulesProject();
		rulesProject.parseRulesMachines(content);
		return getParsedProjectAsPrologTerm(rulesProject);
	}

	public static String getFileAsPrologTerm(final String fileName) throws BCompoundException {
		return getFileAsPrologTerm(fileName, false);
	}

	public static String getFileAsPrologTerm(final String fileName, boolean addLineNumbers) throws BCompoundException {
		File file;
		try {
			file = new File(RulesUtil.class.getResource("/rules/" + fileName).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(addLineNumbers);
		final RulesProject project = new RulesProject();
		project.setParsingBehaviour(pb);
		project.parseProject(file);
		return getParsedProjectAsPrologTerm(project);
	}

	public static String getRulesMachineAsPrologTerm(final String content) throws BCompoundException {
		RulesParseUnit unit = new RulesParseUnit();
		unit.setMachineAsString(content);
		unit.parse();
		unit.translate();
		if (unit.hasError()) {
			throw unit.getCompoundException();
		}

		final PrologTermStringOutput pout = new PrologTermStringOutput();
		unit.printAsProlog(pout, new NodeFileNumbers());
		return pout.toString();
	}

}
