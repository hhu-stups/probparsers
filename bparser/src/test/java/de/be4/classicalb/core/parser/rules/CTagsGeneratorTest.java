package de.be4.classicalb.core.parser.rules;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CTagsGeneratorTest {

	@Test
	public void testCTagsGenerator() throws Exception {
		final String machine1 = "RULES_MACHINE Test OPERATIONS RULE foo BODY skip END ; COMPUTATION comp1 BODY DEFINE xx TYPE POW(INTEGER) VALUE {} END END END";
		RulesProject rulesProject = new RulesProject();
		rulesProject.parseRulesMachines(machine1);
		rulesProject.checkAndTranslateProject();
		File file = File.createTempFile("ctags", ".tags");
		try {
			CTagsGenerator.generateCtagsFile(rulesProject, file);
			// no error occurred
		} finally {
			boolean success = file.delete();
			assertTrue("Failed to delete temporary ctags file", success);
		}
	}

	@Test
	public void testCTagsGeneratorInvalidModel() throws Exception {
		final String machine1 = "RULES_MACHINE ;; END";
		RulesProject rulesProject = new RulesProject();
		rulesProject.parseRulesMachines(machine1);
		rulesProject.checkAndTranslateProject();
		File file = File.createTempFile("ctags", ".tags");
		try {
			CTagsGenerator.generateCtagsFile(rulesProject, file);
		} finally {
			boolean success = file.delete();
			assertTrue("Failed to delete temporary ctags file", success);
		}
	}

}
