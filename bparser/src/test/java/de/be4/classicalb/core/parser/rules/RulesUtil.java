package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment;
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.prolog.output.PrologTermStringOutput;

public class RulesUtil {
	private static String checkAndFormatProject(final RulesProject project) throws BCompoundException {
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
		return checkAndFormatProject(rulesProject);
	}

	public static String getFileAsPrologTerm(final String filename) throws BCompoundException {
		File file;
		try {
			file = new File(RulesUtil.class.getClassLoader().getResource(filename).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		final RulesProject project = new RulesProject();
		project.parseProject(file);
		return checkAndFormatProject(project);
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
