package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.ParsingBehaviour;
import de.be4.classicalb.core.parser.analysis.prolog.NodeIdAssignment;
import de.be4.classicalb.core.parser.analysis.prolog.PrologExceptionPrinter;
import de.be4.classicalb.core.parser.exceptions.BCompoundException;
import de.prob.prolog.output.PrologTermStringOutput;

public class RulesUtil {
	private static String checkAndFormatProject(final RulesProject project) {
		project.checkAndTranslateProject();
		final PrologTermStringOutput pout = new PrologTermStringOutput();
		if (project.hasErrors()) {
			BCompoundException comp = new BCompoundException(project.getBExceptionList());
			PrologExceptionPrinter.printException(pout, comp, false, false);
		} else {
			project.printProjectAsPrologTerm(pout);
			pout.flush();
		}
		return pout.toString();
	}

	public static String getRulesProjectAsPrologTerm(final String content) {
		RulesProject rulesProject = new RulesProject();
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(false);
		rulesProject.setParsingBehaviour(pb);
		rulesProject.parseRulesMachines(content, new String[] {});
		return checkAndFormatProject(rulesProject);
	}

	public static String getFileAsPrologTerm(final String filename) {
		File file;
		try {
			file = new File(RulesUtil.class.getClassLoader().getResource(filename).toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(false);
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
