package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment;
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
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
		pout.flush();
		return pout.toString();
	}

	public static String getRulesProjectAsPrologTerm(final String content) throws BCompoundException {
		RulesProject rulesProject = new RulesProject();
		rulesProject.parseRulesMachines(content, new String[] {});
		return getParsedProjectAsPrologTerm(rulesProject);
	}

	public static String getFileAsPrologTerm(final String file) throws BCompoundException {
		return getFileAsPrologTerm(file, false);
	}

	public static String getFileAsPrologTerm(final String filename, boolean addLineNumbers) throws BCompoundException {
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
		return getParsedProjectAsPrologTerm(project);
	}

	public static String getRulesMachineAsPrologTerm(final String content) {
		RulesParseUnit unit = new RulesParseUnit();
		unit.setMachineAsString(content);
		unit.parse();
		unit.translate();

		final PrologTermStringOutput pout = new PrologTermStringOutput();
		if (unit.hasError()) {
			PrologExceptionPrinter.printException(pout, unit.getCompoundException(), false, false);
		} else {
			unit.printAsProlog(pout, new NodeIdAssignment());
			pout.flush();
		}
		return pout.toString();
	}

}
