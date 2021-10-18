package de.be4.classicalb.core.parser.rules;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import de.be4.classicalb.core.parser.ParsingBehaviour;

public class RulesUtil {

	public static String getRulesProjectAsPrologTerm(final String content) {
		RulesProject rulesProject = new RulesProject();
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(false);
		rulesProject.setParsingBehaviour(pb);
		rulesProject.parseRulesMachines(content, new String[] {});
		rulesProject.checkAndTranslateProject();

		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};
		rulesProject.printPrologOutput(new PrintStream(output), new PrintStream(output));
		return output.toString();
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
		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};
		RulesProject.parseProject(file, pb, new PrintStream(output), new PrintStream(output));
		return output.toString();
	}

	public static String getRulesMachineAsPrologTerm(final String content) {
		RulesParseUnit unit = new RulesParseUnit();
		unit.setMachineAsString(content);
		ParsingBehaviour pb = new ParsingBehaviour();
		pb.setAddLineNumbers(false);
		unit.setParsingBehaviour(pb);
		unit.parse();
		unit.translate();

		OutputStream output = new OutputStream() {
			private StringBuilder string = new StringBuilder();

			@Override
			public void write(int b) throws IOException {
				this.string.append((char) b);
			}

			public String toString() {
				return this.string.toString();
			}
		};
		unit.printPrologOutput(new PrintStream(output), new PrintStream(output));
		return output.toString();
	}

}
